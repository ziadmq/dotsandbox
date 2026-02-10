package com.ziadmq.dotsandboxes.viewmodel

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.ziadmq.dotsandboxes.model.*
import com.ziadmq.dotsandboxes.ui.theme.*
import kotlin.math.abs
import kotlin.math.roundToInt

// لون التجميد الجليدي
val FrozenColor = Color(0xFF80D8FF)

@Composable
fun NeonBurst(active: Boolean, color: Color) {
    if (!active) return
    val transition = rememberInfiniteTransition(label = "particles")
    val sizePx by transition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(animation = tween(600, easing = LinearOutSlowInEasing)),
        label = "size"
    )
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(600)),
        label = "alpha"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        for (i in 0..7) {
            val angle = (i * 45).toDouble()
            val x = center.x + sizePx * Math.cos(Math.toRadians(angle)).toFloat()
            val y = center.y + sizePx * Math.sin(Math.toRadians(angle)).toFloat()
            drawCircle(color = color.copy(alpha = alpha), radius = 4f, center = Offset(x, y))
        }
    }
}

@Composable
fun DotsAndBoxesBoard(
    state: GameState,
    onLineClicked: (Line) -> Unit,
    modifier: Modifier = Modifier
) {
    val dotSize = 16.dp
    val lineThickness = 10.dp
    val spacing = if (state.gridSize > 4) 50.dp else 65.dp
    val density = LocalDensity.current
    val spacingPx = with(density) { spacing.toPx() }
    val dotRadiusPx = with(density) { (dotSize / 2).toPx() }
    val lineThickPx = with(density) { lineThickness.toPx() }
    val haptic = LocalHapticFeedback.current

    var dragStartOffset by remember { mutableStateOf<Offset?>(null) }
    var dragCurrentOffset by remember { mutableStateOf<Offset?>(null) }
    var potentialLine by remember { mutableStateOf<Line?>(null) }

    Box(
        modifier = modifier
            .size(spacing * (state.gridSize - 1) + dotSize)
            .pointerInput(state.frozenLine) {
                detectDragGestures(
                    onDragStart = { offset ->
                        dragStartOffset = offset
                        dragCurrentOffset = offset
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        dragCurrentOffset = change.position
                        dragStartOffset?.let { start ->
                            val startCol = (start.x / spacingPx).roundToInt()
                            val startRow = (start.y / spacingPx).roundToInt()
                            val endCol = (change.position.x / spacingPx).roundToInt()
                            val endRow = (change.position.y / spacingPx).roundToInt()

                            val line = findValidLine(startRow, startCol, endRow, endCol, state)

                            // منع اختيار الخط إذا كان مجمداً للخصم
                            val isFrozen = state.frozenLine?.let { fl ->
                                line?.row == fl.row && line?.col == fl.col && line?.orientation == fl.orientation && state.currentPlayer != state.frozenByPlayer
                            } ?: false

                            potentialLine = if (isFrozen) null else line
                        }
                    },
                    onDragEnd = {
                        potentialLine?.let {
                            if (!it.isSelected) {
                                onLineClicked(it)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
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
        // --- LAYER 1: Grid & Lines & Boxes ---
        for (row in 0 until state.gridSize) {
            for (col in 0 until state.gridSize) {
                val xOffset = spacing * col
                val yOffset = spacing * row

                if (col < state.gridSize - 1) {
                    val line = state.lines.first { it.row == row && it.col == col && it.orientation == LineOrientation.HORIZONTAL }
                    val isFrozen = state.frozenLine == line && state.currentPlayer != state.frozenByPlayer
                    LineItem(
                        line,
                        Modifier.offset(xOffset + dotSize/2, yOffset + (dotSize - lineThickness)/2).width(spacing).height(lineThickness),
                        isFrozen
                    )
                }

                if (row < state.gridSize - 1) {
                    val line = state.lines.first { it.row == row && it.col == col && it.orientation == LineOrientation.VERTICAL }
                    val isFrozen = state.frozenLine == line && state.currentPlayer != state.frozenByPlayer
                    LineItem(
                        line,
                        Modifier.offset(xOffset + (dotSize - lineThickness)/2, yOffset + dotSize/2).width(lineThickness).height(spacing),
                        isFrozen
                    )
                }

                if (row < state.gridSize - 1 && col < state.gridSize - 1) {
                    val box = state.boxes.first { it.row == row && it.col == col }
                    Box(
                        Modifier.offset(xOffset + dotSize, yOffset + dotSize).size(spacing - dotSize),
                        contentAlignment = Alignment.Center
                    ) {
                        if (box.owner != null) {
                            NeonBurst(active = true, color = if(box.owner == Player.PLAYER1) Player1Color else Player2Color)
                            BoxItem(box.owner, Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }

        // --- LAYER 2: Dots ---
        for (row in 0 until state.gridSize) {
            for (col in 0 until state.gridSize) {
                val xOffset = spacing * col
                val yOffset = spacing * row
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

        // --- LAYER 3: Interactive Canvas ---
        Canvas(modifier = Modifier.fillMaxSize()) {
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

// هذه هي الدالة التي كانت مفقودة وتسببت في الخطأ
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
fun LineItem(line: Line, modifier: Modifier, isFrozen: Boolean = false) {
    val animatedColor by animateColorAsState(
        targetValue = when {
            isFrozen -> FrozenColor
            line.isSelected && line.owner == Player.PLAYER1 -> Player1Color
            line.isSelected && line.owner == Player.PLAYER2 -> Player2Color
            else -> Color.White.copy(0.05f)
        },
        animationSpec = tween(400)
    )

    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(animatedColor)
            .then(if (isFrozen) Modifier.border(1.dp, Color.White, RoundedCornerShape(4.dp)) else Modifier)
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