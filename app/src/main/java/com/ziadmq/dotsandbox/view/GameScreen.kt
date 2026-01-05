package com.ziadmq.dotsandbox.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ziadmq.dotsandbox.AdBanner
import com.ziadmq.dotsandbox.R
import com.ziadmq.dotsandbox.model.GameMode
import com.ziadmq.dotsandbox.model.Player
import com.ziadmq.dotsandbox.ui.theme.Player1Color
import com.ziadmq.dotsandbox.ui.theme.Player2Color
import com.ziadmq.dotsandbox.viewmodel.DotsAndBoxesBoard
import com.ziadmq.dotsandbox.viewmodel.GameViewModel

@Composable
fun GameScreen(viewModel: GameViewModel, onShowInterstitial: () -> Unit, onQuit: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val isGameFinished = state.lines.all { it.isSelected }

    LaunchedEffect(isGameFinished) {
        if (isGameFinished) {
            onShowInterstitial()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).padding(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth()) {
                TextButton(onClick = onQuit) { Text(stringResource(R.string.exit_button), color = Color.White.copy(alpha = 0.7f)) }
            }

            Spacer(modifier = Modifier.height(16.dp))
            ScoreBoard(state.scorePlayer1, state.scorePlayer2, state.currentPlayer, viewModel.getGameMode())
            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    GlassCard {
                        DotsAndBoxesBoard(state, { viewModel.onLineClicked(it) })
                    }
                }
            }
        }

        AdBanner(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }

    if (isGameFinished) {
        val winner = when {
            state.scorePlayer1 > state.scorePlayer2 -> stringResource(R.string.p1_wins)
            state.scorePlayer2 > state.scorePlayer1 -> if(viewModel.getGameMode()==GameMode.PvE) stringResource(R.string.ai_wins) else stringResource(R.string.p2_wins)
            else -> stringResource(R.string.draw)
        }

        AlertDialog(
            containerColor = Color(0xFF243B55),
            onDismissRequest = { viewModel.restartGame() },
            title = { Text(stringResource(R.string.game_over), color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(winner, fontSize = 24.sp, color = Player1Color, fontWeight = FontWeight.Bold) },
            confirmButton = { Button(onClick = { viewModel.restartGame() }, colors = ButtonDefaults.buttonColors(containerColor = Player1Color)) { Text(stringResource(R.string.replay), color = Color.Black) } },
            dismissButton = { TextButton(onClick = onQuit) { Text(stringResource(R.string.menu), color = Color.Gray) } }
        )
    }
}
@Composable
fun GlassCard(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}
@Composable
fun ScoreBoard(p1: Int, p2: Int, current: Player, mode: GameMode) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerBadge(stringResource(R.string.player_1), p1, Player1Color, current == Player.PLAYER1)
        Text(stringResource(R.string.vs_text), color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        PlayerBadge(if(mode==GameMode.PvE) stringResource(R.string.ai_label) else stringResource(R.string.player_2), p2, Player2Color, current == Player.PLAYER2 || (mode==GameMode.PvE && current==Player.PLAYER2))
    }
}
@Composable
fun PlayerBadge(name: String, score: Int, color: Color, active: Boolean) {
    val scale by animateFloatAsState(if (active) 1.1f else 1.0f, label = "scale")
    val alpha by animateFloatAsState(if (active) 1f else 0.5f, label = "alpha")

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.scale(scale)) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .border(3.dp, color.copy(alpha = alpha), CircleShape)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(score.toString(), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(name, color = color.copy(alpha = alpha), fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

