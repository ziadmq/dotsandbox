package com.ziadmq.dotsandbox.viewmodel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ziadmq.dotsandbox.model.*

@Composable
fun DotsAndBoxesBoard(
    state: GameState,
    onLineClicked: (Line) -> Unit
) {
    val dotSize = 16.dp
    val lineThickness = 12.dp
    val spacing = 48.dp

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        for (row in 0 until state.gridSize) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                for (col in 0 until state.gridSize) {

                    Box(
                        modifier = Modifier
                            .size(dotSize)
                            .clip(CircleShape)
                            .background(Color.Black)
                    )

                    if (col < state.gridSize - 1) {
                        val line = state.lines.first {
                            it.row == row && it.col == col && it.orientation == LineOrientation.HORIZONTAL
                        }
                        Box(
                            modifier = Modifier
                                .height(lineThickness)
                                .width(spacing)
                                .clickable { onLineClicked(line) }
                                .background(
                                    when {
                                        !line.isSelected -> Color.LightGray
                                        line.owner == Player.PLAYER1 -> Color.Blue
                                        else -> Color.Red
                                    }
                                )
                        )
                    }
                }
            }

            if (row < state.gridSize - 1) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    for (col in 0 until state.gridSize) {

                        val line = state.lines.first {
                            it.row == row && it.col == col && it.orientation == LineOrientation.VERTICAL
                        }

                        Box(
                            modifier = Modifier
                                .width(lineThickness)
                                .height(spacing)
                                .clickable { onLineClicked(line) }
                                .background(
                                    when {
                                        !line.isSelected -> Color.LightGray
                                        line.owner == Player.PLAYER1 -> Color.Blue
                                        else -> Color.Red
                                    }
                                )
                        )

                        if (col < state.gridSize - 1) {
                            val boxOwner = state.boxes.first { it.row == row && it.col == col }.owner

                            Box(
                                modifier = Modifier
                                    .size(spacing)
                                    .background(
                                        when (boxOwner) {
                                            Player.PLAYER1 -> Color(0x553497F6)
                                            Player.AI -> Color(0x55E53935)
                                            else -> Color.Transparent
                                        }
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
