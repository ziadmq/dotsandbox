package com.ziadmq.dotsandbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ziadmq.dotsandbox.model.Player
import com.ziadmq.dotsandbox.viewmodel.DotsAndBoxesBoard
import com.ziadmq.dotsandbox.viewmodel.aiChooseMove
import com.ziadmq.dotsandbox.viewmodel.createInitialGameState
import com.ziadmq.dotsandbox.viewmodel.updateGameAfterLine
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    GameScreen()
                }
            }
        }
    }
}

@Composable
fun GameScreen() {
    var gameState by remember { mutableStateOf(createInitialGameState(4)) }
    var isGameFinished by remember { mutableStateOf(false) }
    var aiThinking by remember { mutableStateOf(false) }   // 🔥 مانع التكرار

    // ❗ اللعبة تنتهي فقط عندما جميع الخطوط مرسومة
    LaunchedEffect(gameState.lines) {
        isGameFinished = gameState.lines.all { it.isSelected }
    }

    // 🔥 AI TURN — لا يلعب إذا كان مشغول
    LaunchedEffect(gameState.currentPlayer) {
        if (!isGameFinished && gameState.currentPlayer == Player.AI && !aiThinking) {
            aiThinking = true           // 🔥 يمنع إعادة الدخول
            delay(500)
            val move = aiChooseMove(gameState)
            if (move != null) {
                gameState = updateGameAfterLine(gameState, move)
            }
            aiThinking = false          // تحرير AI
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Player1: ${gameState.scorePlayer1}  |  AI: ${gameState.scorePlayer2}")

        Spacer(modifier = Modifier.height(16.dp))

        DotsAndBoxesBoard(
            state = gameState,
            onLineClicked = { line ->
                if (!line.isSelected && gameState.currentPlayer == Player.PLAYER1 && !isGameFinished) {
                    gameState = updateGameAfterLine(gameState, line)
                }
            }
        )
    }

    if (isGameFinished) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Game Finished") },
            text = {
                Text(
                    if (gameState.scorePlayer1 > gameState.scorePlayer2) "Player 1 Wins!"
                    else "AI Wins!"
                )
            },
            confirmButton = {
                Button(onClick = {
                    gameState = createInitialGameState(4)
                    isGameFinished = false
                }) {
                    Text("Restart")
                }
            }
        )
    }
}







