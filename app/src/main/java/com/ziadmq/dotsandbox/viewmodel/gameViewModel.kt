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

// Added SPLASH screen
enum class AppScreen { SPLASH, HOME, GAME }

class GameViewModel : ViewModel() {

    // Start with SPLASH screen
    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
    val currentScreen = _currentScreen.asStateFlow()

    private val _uiState = MutableStateFlow(createInitialGameState(4))
    val uiState = _uiState.asStateFlow()

    private val _soundChannel = Channel<SoundType>()
    val soundEvents = _soundChannel.receiveAsFlow()

    private var currentGameMode: GameMode = GameMode.PvE
    private var currentGridSize: Int = 4

    // --- Navigation ---

    // New function to exit Splash Screen
    fun finishSplash() {
        _currentScreen.value = AppScreen.HOME
    }

    fun startGame(gridSize: Int, mode: GameMode) {
        currentGridSize = gridSize
        currentGameMode = mode
        _uiState.value = createInitialGameState(gridSize)
        _currentScreen.value = AppScreen.GAME
    }

    fun quitGame() { _currentScreen.value = AppScreen.HOME }

    fun restartGame() { _uiState.value = createInitialGameState(currentGridSize) }

    fun getGameMode(): GameMode = currentGameMode

    // --- Interaction ---

    fun onLineClicked(line: Line) {
        val currentState = _uiState.value
        if (line.isSelected || isGameFinished(currentState)) return
        if (currentGameMode == GameMode.PvE && currentState.currentPlayer == Player.PLAYER2) return

        processMove(line)
    }

    private fun processMove(line: Line) {
        val currentState = _uiState.value
        val newState = updateGameAfterLine(currentState, line)
        _uiState.value = newState

        val didScore = (newState.scorePlayer1 > currentState.scorePlayer1) ||
                (newState.scorePlayer2 > currentState.scorePlayer2)

        viewModelScope.launch {
            if (isGameFinished(newState)) {
                _soundChannel.send(SoundType.WIN)
            } else if (didScore) {
                _soundChannel.send(SoundType.SCORE)
            } else {
                _soundChannel.send(SoundType.MOVE)
            }
        }

        if (!isGameFinished(newState) && currentGameMode == GameMode.PvE && newState.currentPlayer == Player.PLAYER2) {
            triggerAiTurn()
        }
    }

    private fun triggerAiTurn() {
        viewModelScope.launch {
            delay(700)
            val currentState = _uiState.value
            if (currentState.currentPlayer != Player.PLAYER2 || isGameFinished(currentState)) return@launch

            val move = calculateBestAiMove(currentState)
            if (move != null) processMove(move)
        }
    }

    // --- Game Logic Helpers ---

    private fun calculateBestAiMove(state: GameState): Line? {
        val availableLines = state.lines.filter { !it.isSelected }
        for (line in availableLines) {
            val tempState = updateGameAfterLine(state, line)
            if (tempState.boxes.count { it.owner != null } > state.boxes.count { it.owner != null }) return line
        }
        return availableLines.randomOrNull()
    }

    private fun isGameFinished(state: GameState) = state.lines.all { it.isSelected }

    private fun createInitialGameState(gridSize: Int): GameState {
        val lines = mutableListOf<Line>()
        val boxes = mutableListOf<Box>()
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize - 1) lines.add(Line(row, col, LineOrientation.HORIZONTAL))
        }
        for (row in 0 until gridSize - 1) {
            for (col in 0 until gridSize) lines.add(Line(row, col, LineOrientation.VERTICAL))
        }
        for (row in 0 until gridSize - 1) {
            for (col in 0 until gridSize - 1) boxes.add(Box(row, col))
        }
        return GameState(gridSize, Player.PLAYER1, lines, boxes)
    }

    private fun updateGameAfterLine(state: GameState, clickedLine: Line): GameState {
        val newLines = state.lines.map {
            if (it == clickedLine) it.copy(isSelected = true, owner = state.currentPlayer) else it
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

    private fun isBoxCompleted(box: Box, lines: List<Line>): Boolean {
        val top = lines.firstOrNull { it.row == box.row && it.col == box.col && it.orientation == LineOrientation.HORIZONTAL }
        val bottom = lines.firstOrNull { it.row == box.row + 1 && it.col == box.col && it.orientation == LineOrientation.HORIZONTAL }
        val left = lines.firstOrNull { it.row == box.row && it.col == box.col && it.orientation == LineOrientation.VERTICAL }
        val right = lines.firstOrNull { it.row == box.row && it.col == box.col + 1 && it.orientation == LineOrientation.VERTICAL }
        return top?.isSelected == true && bottom?.isSelected == true && left?.isSelected == true && right?.isSelected == true
    }
}