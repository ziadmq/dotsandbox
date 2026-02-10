package com.ziadmq.dotsandboxes.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ziadmq.dotsandboxes.model.GameMode
import com.ziadmq.dotsandboxes.model.Player
import com.ziadmq.dotsandboxes.model.PowerUpType
import com.ziadmq.dotsandboxes.ui.theme.Player1Color
import com.ziadmq.dotsandboxes.ui.theme.Player2Color
import com.ziadmq.dotsandboxes.viewmodel.DotsAndBoxesBoard
import com.ziadmq.dotsandboxes.viewmodel.GameViewModel
import com.ziadmq.dotsandboxes.R

@Composable
fun GameScreen(viewModel: GameViewModel, onShowInterstitial: () -> Unit, onQuit: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val isGameFinished = state.lines.all { it.isSelected }
    val mode = viewModel.getGameMode()
    val isHacker = mode == GameMode.HACKER_PvE || mode == GameMode.HACKER_PvP

    LaunchedEffect(isGameFinished) {
        if (isGameFinished) {
            onShowInterstitial()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // شريط علوي بسيط
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onQuit) {
                    Text(
                        stringResource(R.string.exit_button),
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = if (isHacker) stringResource(R.string.hacker_active) else stringResource(
                        R.string.classic_mode
                    ),
                    color = if (isHacker) Player1Color else Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // لوحة النتائج
            ScoreBoard(state.scorePlayer1, state.scorePlayer2, state.currentPlayer, mode)

            Spacer(modifier = Modifier.height(24.dp))

            // منطقة اللعب
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    // تم هنا استخدام الـ modifier والـ contentPadding بشكل صحيح
                    GlassCard(
                        modifier = Modifier.wrapContentSize(),
                        contentPadding = 16.dp
                    ) {
                        DotsAndBoxesBoard(state, { viewModel.onLineClicked(it) })
                    }
                }
            }

            // شريط القدرات لنمط الهاكر
            if (isHacker) {
                Spacer(modifier = Modifier.height(24.dp))
                PowerUpDock(viewModel)
            }

            Spacer(modifier = Modifier.height(60.dp))
        }

    }

    if (isGameFinished) {
        val winner = when {
            state.scorePlayer1 > state.scorePlayer2 -> stringResource(R.string.p1_wins)
            state.scorePlayer2 > state.scorePlayer1 -> if (mode == GameMode.PvE || mode == GameMode.HACKER_PvE) stringResource(
                R.string.ai_wins
            ) else stringResource(R.string.p2_wins)

            else -> stringResource(R.string.draw)
        }

        AlertDialog(
            containerColor = Color(0xFF243B55),
            onDismissRequest = { viewModel.restartGame() },
            title = {
                Text(
                    stringResource(R.string.game_over),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    winner,
                    fontSize = 24.sp,
                    color = Player1Color,
                    fontWeight = FontWeight.Bold
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.restartGame() },
                    colors = ButtonDefaults.buttonColors(containerColor = Player1Color)
                ) { Text(stringResource(R.string.replay), color = Color.Black) }
            },
            dismissButton = {
                TextButton(onClick = onQuit) {
                    Text(
                        stringResource(R.string.menu),
                        color = Color.Gray
                    )
                }
            }
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    contentPadding: androidx.compose.ui.unit.Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(listOf(Color.White.copy(0.1f), Color.Transparent)),
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
fun PowerUpDock(viewModel: GameViewModel) {
    val state by viewModel.uiState.collectAsState()
    val currentPowerUps =
        if (state.currentPlayer == Player.PLAYER1) state.p1PowerUps else state.p2PowerUps
    val activeColor = if (state.currentPlayer == Player.PLAYER1) Player1Color else Player2Color

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PowerUpItem(
                name = stringResource(R.string.emp_name),
                hint = stringResource(R.string.emp_hint),
                icon = Icons.Default.Bolt,
                isAvailable = PowerUpType.EMP in currentPowerUps,
                activeColor = activeColor
            ) { viewModel.useEMP() }

            Divider(modifier = Modifier
                .width(1.dp)
                .height(30.dp), color = Color.White.copy(0.1f))

            // قدرة الـ HACK
            PowerUpItem(
                name = stringResource(R.string.hack_name),
                hint = stringResource(R.string.hack_hint),
                icon = Icons.Default.Terminal,
                isAvailable = PowerUpType.THE_HACK in currentPowerUps,
                activeColor = activeColor
            ) { viewModel.useTheHack() }

            Divider(modifier = Modifier
                .width(1.dp)
                .height(30.dp), color = Color.White.copy(0.1f))

            // قدرة الـ FREEZE
            PowerUpItem(
                name = stringResource(R.string.freeze_name),
                hint = stringResource(R.string.freeze_hint),
                icon = Icons.Default.AcUnit,
                isAvailable = PowerUpType.SYSTEM_FREEZE in currentPowerUps,
                activeColor = activeColor
            ) { viewModel.useSystemFreeze() }
        }
    }
}

@Composable
fun PowerUpItem(
    name: String,
    hint: String,
    icon: ImageVector,
    isAvailable: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .alpha(if (isAvailable) 1f else 0.2f)
            .clickable(enabled = isAvailable) { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isAvailable) activeColor else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Text(name, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Text(hint, color = activeColor.copy(alpha = 0.7f), fontSize = 8.sp)
    }
}

@Composable
fun ScoreBoard(p1: Int, p2: Int, current: Player, mode: GameMode) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerBadge(stringResource(R.string.player_1), p1, Player1Color, current == Player.PLAYER1)
        Text(
            stringResource(R.string.vs_text),
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        PlayerBadge(
            if (mode == GameMode.PvE || mode == GameMode.HACKER_PvE) stringResource(R.string.ai_label) else stringResource(
                R.string.player_2
            ), p2, Player2Color, current == Player.PLAYER2
        )
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
            Text(
                score.toString(),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            name,
            color = color.copy(alpha = alpha),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}