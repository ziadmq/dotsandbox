package com.ziadmq.dotsandbox.viewmodel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.ziadmq.dotsandbox.model.*
import com.ziadmq.dotsandbox.ui.theme.*
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun DotsAndBoxesBoard(
    state: GameState,
    onLineClicked: (Line) -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Dynamic Sizing
    val dotSize = 16.dp
    val lineThickness = 10.dp
    val spacing = if (state.gridSize > 4) 50.dp else 65.dp

    val density = LocalDensity.current
    val spacingPx = with(density) { spacing.toPx() }
    val dotRadiusPx = with(density) { (dotSize / 2).toPx() }
    val lineThickPx = with(density) { lineThickness.toPx() }

    // Board Dimensions
    val boardSize = spacing * (state.gridSize - 1) + dotSize

    // 2. Interaction State
    var dragStartOffset by remember { mutableStateOf<Offset?>(null) }
    var dragCurrentOffset by remember { mutableStateOf<Offset?>(null) }
    var potentialLine by remember { mutableStateOf<Line?>(null) }

    Box(
        modifier = modifier
            .size(boardSize)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        dragStartOffset = offset
                        dragCurrentOffset = offset
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        dragCurrentOffset = change.position

                        // Calculate potential snap
                        dragStartOffset?.let { start ->
                            val startCol = (start.x / spacingPx).roundToInt()
                            val startRow = (start.y / spacingPx).roundToInt()
                            val endCol = (change.position.x / spacingPx).roundToInt()
                            val endRow = (change.position.y / spacingPx).roundToInt()

                            potentialLine = findValidLine(startRow, startCol, endRow, endCol, state)
                        }
                    },
                    onDragEnd = {
                        potentialLine?.let {
                            if (!it.isSelected) onLineClicked(it)
                        }
                        dragStartOffset = null
                        dragCurrentOffset = null
                        potentialLine = null
                    },
                    onDragCancel = {
                        dragStartOffset = null
                        dragCurrentOffset = null
                        potentialLine = null
                    }
                )
            }
    ) {
        // --- LAYER 1: The Grid & Completed Lines ---
        for (row in 0 until state.gridSize) {
            for (col in 0 until state.gridSize) {
                val xOffset = spacing * col
                val yOffset = spacing * row

                // Horizontal Line
                if (col < state.gridSize - 1) {
                    val line = state.lines.first { it.row == row && it.col == col && it.orientation == LineOrientation.HORIZONTAL }
                    LineItem(
                        line = line,
                        modifier = Modifier
                            .offset(x = xOffset + dotSize/2, y = yOffset + (dotSize - lineThickness)/2)
                            .width(spacing)
                            .height(lineThickness)
                    )
                }

                // Vertical Line
                if (row < state.gridSize - 1) {
                    val line = state.lines.first { it.row == row && it.col == col && it.orientation == LineOrientation.VERTICAL }
                    LineItem(
                        line = line,
                        modifier = Modifier
                            .offset(x = xOffset + (dotSize - lineThickness)/2, y = yOffset + dotSize/2)
                            .width(lineThickness)
                            .height(spacing)
                    )
                }

                // Boxes
                if (row < state.gridSize - 1 && col < state.gridSize - 1) {
                    val box = state.boxes.first { it.row == row && it.col == col }
                    if (box.owner != null) {
                        BoxItem(
                            owner = box.owner,
                            modifier = Modifier
                                .offset(x = xOffset + dotSize, y = yOffset + dotSize)
                                .size(spacing - dotSize)
                        )
                    }
                }
            }
        }

        // --- LAYER 2: The Dots (Rendered on top) ---
        for (row in 0 until state.gridSize) {
            for (col in 0 until state.gridSize) {
                val xOffset = spacing * col
                val yOffset = spacing * row

                // Highlight dot if it's the start of a drag
                val isDragStart = dragStartOffset?.let {
                    (it.x / spacingPx).roundToInt() == col && (it.y / spacingPx).roundToInt() == row
                } ?: false

                Box(
                    modifier = Modifier
                        .offset(x = xOffset, y = yOffset)
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(if (isDragStart) Color.White else DotColor)
                )
            }
        }

        // --- LAYER 3: Interactive Drag Preview ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw line from start dot to finger
            val start = dragStartOffset
            val current = dragCurrentOffset

            if (start != null && current != null && potentialLine == null) {
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = start,
                    end = current,
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Draw "Snap" Preview Line if a valid connection is found
            potentialLine?.let { line ->
                val startX = line.col * spacingPx + dotRadiusPx
                val startY = line.row * spacingPx + dotRadiusPx
                val endX = if (line.orientation == LineOrientation.HORIZONTAL) (line.col + 1) * spacingPx + dotRadiusPx else startX
                val endY = if (line.orientation == LineOrientation.VERTICAL) (line.row + 1) * spacingPx + dotRadiusPx else startY

                drawLine(
                    color = if(state.currentPlayer == Player.PLAYER1) Player1Color else Player2Color,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = lineThickPx,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

fun findValidLine(r1: Int, c1: Int, r2: Int, c2: Int, state: GameState): Line? {
    if (r1 == r2 && abs(c1 - c2) == 1) {
        val col = minOf(c1, c2)
        return state.lines.firstOrNull { it.row == r1 && it.col == col && it.orientation == LineOrientation.HORIZONTAL }
    } else if (c1 == c2 && abs(r1 - r2) == 1) {
        val row = minOf(r1, r2)
        return state.lines.firstOrNull { it.row == row && it.col == c1 && it.orientation == LineOrientation.VERTICAL }
    }
    return null
}

@Composable
fun LineItem(line: Line, modifier: Modifier) {
    val alpha = if (line.isSelected) 1f else 0.2f
    val color = when {
        line.isSelected && line.owner == Player.PLAYER1 -> Player1Color
        line.isSelected && line.owner == Player.PLAYER2 -> Player2Color
        else -> UnselectedLineColor
    }

    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = alpha))
    )
}

@Composable
fun BoxItem(owner: Player, modifier: Modifier) {
    val color = if (owner == Player.PLAYER1) Player1Color else Player2Color
    Box(
        modifier = modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.3f))
    )
}