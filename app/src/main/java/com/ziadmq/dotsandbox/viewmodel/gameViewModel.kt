package com.ziadmq.dotsandbox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ziadmq.dotsandbox.model.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

enum class AppScreen { SPLASH, HOME, GAME }

class GameViewModel : ViewModel() {

    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
    val currentScreen = _currentScreen.asStateFlow()

    private val _uiState = MutableStateFlow(createInitialGameState(4, GameMode.PvP))
    val uiState = _uiState.asStateFlow()

    private val _soundChannel = Channel<SoundType>()
    val soundEvents = _soundChannel.receiveAsFlow()

    private var currentGameMode: GameMode = GameMode.PvE
    private var currentGridSize: Int = 4

    // --- Navigation ---
    fun finishSplash() { _currentScreen.value = AppScreen.HOME }
    fun quitGame() { _currentScreen.value = AppScreen.HOME }
    fun getGameMode(): GameMode = currentGameMode

    fun startGame(gridSize: Int, mode: GameMode) {
        currentGridSize = gridSize
        currentGameMode = mode
        _uiState.value = createInitialGameState(gridSize, mode)
        _currentScreen.value = AppScreen.GAME
    }

    fun restartGame() {
        _uiState.value = createInitialGameState(currentGridSize, currentGameMode)
    }

    // --- Hacker Power-ups Logic ---

    fun useEMP() {
        val state = _uiState.value
        val lastLine = state.lastLineDrawn ?: return
        val currentPowerUps = if (state.currentPlayer == Player.PLAYER1) state.p1PowerUps else state.p2PowerUps
        if (!currentPowerUps.contains(PowerUpType.EMP)) return

        val newLines = state.lines.map {
            if (it.row == lastLine.row && it.col == lastLine.col && it.orientation == lastLine.orientation)
                it.copy(isSelected = false, owner = null)
            else it
        }
        updateStateAfterPowerUp(newLines, PowerUpType.EMP)
    }

    fun useTheHack() {
        val state = _uiState.value
        val opponent = if (state.currentPlayer == Player.PLAYER1) Player.PLAYER2 else Player.PLAYER1
        val opponentBoxes = state.boxes.filter { it.owner == opponent }

        if (opponentBoxes.isNotEmpty()) {
            val targetBox = opponentBoxes.random()
            val newBoxes = state.boxes.map {
                if (it.row == targetBox.row && it.col == targetBox.col)
                    it.copy(owner = state.currentPlayer)
                else it
            }
            val s1 = newBoxes.count { it.owner == Player.PLAYER1 }
            val s2 = newBoxes.count { it.owner == Player.PLAYER2 }

            _uiState.value = state.copy(
                boxes = newBoxes,
                scorePlayer1 = s1,
                scorePlayer2 = s2,
                p1PowerUps = if (state.currentPlayer == Player.PLAYER1) state.p1PowerUps - PowerUpType.THE_HACK else state.p1PowerUps,
                p2PowerUps = if (state.currentPlayer == Player.PLAYER2) state.p2PowerUps - PowerUpType.THE_HACK else state.p2PowerUps
            )
            viewModelScope.launch { _soundChannel.send(SoundType.SCORE) }
        }
    }

    fun useSystemFreeze() {
        val state = _uiState.value
        val availableLines = state.lines.filter { !it.isSelected && it != state.frozenLine }
        if (availableLines.isNotEmpty()) {
            val targetLine = availableLines.random()
            _uiState.value = state.copy(
                frozenLine = targetLine,
                frozenByPlayer = state.currentPlayer,
                p1PowerUps = if (state.currentPlayer == Player.PLAYER1) state.p1PowerUps - PowerUpType.SYSTEM_FREEZE else state.p1PowerUps,
                p2PowerUps = if (state.currentPlayer == Player.PLAYER2) state.p2PowerUps - PowerUpType.SYSTEM_FREEZE else state.p2PowerUps
            )
        }
    }

    // --- Core Game Logic ---

    fun onLineClicked(line: Line) {
        val state = _uiState.value
        if (line.isSelected || isGameFinished(state)) return

        state.frozenLine?.let { fl ->
            if (line.row == fl.row && line.col == fl.col && line.orientation == fl.orientation) {
                if (state.currentPlayer != state.frozenByPlayer) return
            }
        }

        if ((currentGameMode == GameMode.PvE || currentGameMode == GameMode.HACKER_PvE)
            && state.currentPlayer == Player.PLAYER2) return

        processMove(line)
    }

    private fun processMove(line: Line) {
        val currentState = _uiState.value
        val newState = updateGameAfterLine(currentState, line)
        val shouldClearFreeze = currentState.frozenLine != null && currentState.currentPlayer != currentState.frozenByPlayer

        val finalState = newState.copy(
            frozenLine = if (shouldClearFreeze) null else currentState.frozenLine,
            frozenByPlayer = if (shouldClearFreeze) null else currentState.frozenByPlayer,
            lastLineDrawn = line
        )
        _uiState.value = finalState
        handleSoundsAndAi(currentState, finalState)
    }

    // --- Helpers ---
    private fun createInitialGameState(gridSize: Int, mode: GameMode): GameState {
        val lines = mutableListOf<Line>()
        val boxes = mutableListOf<Box>()
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize - 1) lines.add(Line(r, c, LineOrientation.HORIZONTAL))
        }
        for (r in 0 until gridSize - 1) {
            for (c in 0 until gridSize) lines.add(Line(r, c, LineOrientation.VERTICAL))
        }
        for (r in 0 until gridSize - 1) {
            for (c in 0 until gridSize - 1) boxes.add(Box(r, c))
        }
        val isHacker = mode == GameMode.HACKER_PvE || mode == GameMode.HACKER_PvP
        val powerUps = if (isHacker) PowerUpType.values().toSet() else emptySet()

        return GameState(gridSize, Player.PLAYER1, lines, boxes, 0, 0, powerUps, powerUps)
    }

    private fun updateGameAfterLine(state: GameState, clickedLine: Line): GameState {
        val newLines = state.lines.map {
            if (it.row == clickedLine.row && it.col == clickedLine.col && it.orientation == clickedLine.orientation)
                it.copy(isSelected = true, owner = state.currentPlayer) else it
        }
        val newBoxes = state.boxes.map { box ->
            if (box.owner == null && isBoxCompleted(box, newLines)) box.copy(owner = state.currentPlayer) else box
        }
        val newlyCompleted = newBoxes.count { it.owner != null } - state.boxes.count { it.owner != null }
        val score1 = state.scorePlayer1 + if (state.currentPlayer == Player.PLAYER1) newlyCompleted else 0
        val score2 = state.scorePlayer2 + if (state.currentPlayer == Player.PLAYER2) newlyCompleted else 0
        val nextPlayer = if (newlyCompleted > 0) state.currentPlayer else if (state.currentPlayer == Player.PLAYER1) Player.PLAYER2 else Player.PLAYER1

        return state.copy(lines = newLines, boxes = newBoxes, scorePlayer1 = score1, scorePlayer2 = score2, currentPlayer = nextPlayer)
    }

    private fun updateStateAfterPowerUp(newLines: List<Line>, type: PowerUpType) {
        val state = _uiState.value
        val newBoxes = state.boxes.map { box -> if (isBoxCompleted(box, newLines)) box else box.copy(owner = null) }
        _uiState.value = state.copy(
            lines = newLines, boxes = newBoxes,
            scorePlayer1 = newBoxes.count { it.owner == Player.PLAYER1 },
            scorePlayer2 = newBoxes.count { it.owner == Player.PLAYER2 },
            lastLineDrawn = null,
            p1PowerUps = if (state.currentPlayer == Player.PLAYER1) state.p1PowerUps - type else state.p1PowerUps,
            p2PowerUps = if (state.currentPlayer == Player.PLAYER2) state.p2PowerUps - type else state.p2PowerUps
        )
    }

    private fun isBoxCompleted(box: Box, lines: List<Line>): Boolean {
        val top = lines.any { it.row == box.row && it.col == box.col && it.orientation == LineOrientation.HORIZONTAL && it.isSelected }
        val bottom = lines.any { it.row == box.row + 1 && it.col == box.col && it.orientation == LineOrientation.HORIZONTAL && it.isSelected }
        val left = lines.any { it.row == box.row && it.col == box.col && it.orientation == LineOrientation.VERTICAL && it.isSelected }
        val right = lines.any { it.row == box.row && it.col == box.col + 1 && it.orientation == LineOrientation.VERTICAL && it.isSelected }
        return top && bottom && left && right
    }

    private fun handleSoundsAndAi(oldState: GameState, newState: GameState) {
        val didScore = (newState.scorePlayer1 > oldState.scorePlayer1) || (newState.scorePlayer2 > oldState.scorePlayer2)
        viewModelScope.launch {
            if (isGameFinished(newState)) _soundChannel.send(SoundType.WIN)
            else if (didScore) _soundChannel.send(SoundType.SCORE)
            else _soundChannel.send(SoundType.MOVE)
        }
        if (!isGameFinished(newState) && (currentGameMode == GameMode.PvE || currentGameMode == GameMode.HACKER_PvE) && newState.currentPlayer == Player.PLAYER2) {
            triggerAiTurn()
        }
    }

    private fun triggerAiTurn() {
        viewModelScope.launch {
            delay(700)
            val currentState = _uiState.value
            val availableLines = currentState.lines.filter { !it.isSelected && !(it == currentState.frozenLine && currentState.currentPlayer != currentState.frozenByPlayer) }
            if (availableLines.isNotEmpty()) {
                val winningMove = availableLines.firstOrNull { line ->
                    updateGameAfterLine(currentState, line).boxes.count { it.owner != null } > currentState.boxes.count { it.owner != null }
                }
                processMove(winningMove ?: availableLines.random())
            }
        }
    }

    private fun isGameFinished(state: GameState) = state.lines.all { it.isSelected }
}