package com.ziadmq.dotsandbox.viewmodel

import com.ziadmq.dotsandbox.model.*

fun createInitialGameState(gridSize: Int): GameState {
    val lines = mutableListOf<Line>()
    val boxes = mutableListOf<Box>()

    for (row in 0 until gridSize) {
        for (col in 0 until gridSize - 1) {
            lines.add(Line(row, col, LineOrientation.HORIZONTAL))
        }
    }

    for (row in 0 until gridSize - 1) {
        for (col in 0 until gridSize) {
            lines.add(Line(row, col, LineOrientation.VERTICAL))
        }
    }

    for (row in 0 until gridSize - 1) {
        for (col in 0 until gridSize - 1) {
            boxes.add(Box(row, col))
        }
    }

    return GameState(
        gridSize = gridSize,
        currentPlayer = Player.PLAYER1,
        lines = lines,
        boxes = boxes
    )
}

fun updateGameAfterLine(state: GameState, clickedLine: Line): GameState {

    val newLines = state.lines.map {
        if (it == clickedLine)
            it.copy(isSelected = true, owner = state.currentPlayer)
        else it
    }

    val newBoxes = state.boxes.map { box ->
        if (box.owner == null && isBoxCompleted(box, newLines)) {
            box.copy(owner = state.currentPlayer)
        } else box
    }

    val oldScore = state.boxes.count { it.owner != null }
    val newScore = newBoxes.count { it.owner != null }
    val newlyCompleted = newScore - oldScore

    val score1 = state.scorePlayer1 + if (state.currentPlayer == Player.PLAYER1) newlyCompleted else 0
    val score2 = state.scorePlayer2 + if (state.currentPlayer == Player.AI) newlyCompleted else 0

    val nextPlayer =
        if (newlyCompleted > 0) state.currentPlayer
        else if (state.currentPlayer == Player.PLAYER1) Player.AI else Player.PLAYER1

    return state.copy(
        lines = newLines,
        boxes = newBoxes,
        scorePlayer1 = score1,
        scorePlayer2 = score2,
        currentPlayer = nextPlayer
    )
}

fun isBoxCompleted(box: Box, lines: List<Line>): Boolean {
    val top = lines.first { it.row == box.row && it.col == box.col && it.orientation == LineOrientation.HORIZONTAL }
    val bottom = lines.first { it.row == box.row + 1 && it.col == box.col && it.orientation == LineOrientation.HORIZONTAL }
    val left = lines.first { it.row == box.row && it.col == box.col && it.orientation == LineOrientation.VERTICAL }
    val right = lines.first { it.row == box.row && it.col == box.col + 1 && it.orientation == LineOrientation.VERTICAL }

    return top.isSelected && bottom.isSelected && left.isSelected && right.isSelected
}
