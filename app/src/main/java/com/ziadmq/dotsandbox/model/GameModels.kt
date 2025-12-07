package com.ziadmq.dotsandbox.model

data class Line(
    val row: Int,
    val col: Int,
    val orientation: LineOrientation,
    val isSelected: Boolean = false,
    val owner: Player? = null
)

data class Box(
    val row: Int,
    val col: Int,
    val owner: Player? = null
)

enum class LineOrientation {
    HORIZONTAL,
    VERTICAL
}

enum class Player {
    PLAYER1,
    PLAYER2
}

enum class GameMode {
    PvE, // Player vs AI
    PvP  // Player vs Player
}

data class GameState(
    val gridSize: Int = 4,
    val currentPlayer: Player = Player.PLAYER1,
    val lines: List<Line> = emptyList(),
    val boxes: List<Box> = emptyList(),
    val scorePlayer1: Int = 0,
    val scorePlayer2: Int = 0
)
// --- NEW: Sound Event ---
enum class SoundType {
    MOVE,
    SCORE,
    WIN
}