package com.ziadmq.dotsandbox

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ziadmq.dotsandbox.model.*
import com.ziadmq.dotsandbox.ui.theme.*
import com.ziadmq.dotsandbox.viewmodel.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DotsAndBoxTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF141E30), Color(0xFF243B55))
                            )
                        )
                ) {
                    MainApp()
                }
            }
        }
    }
}

@Composable
fun MainApp(viewModel: GameViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val context = LocalContext.current

    // Sound Player
    LaunchedEffect(Unit) {
        viewModel.soundEvents.collect { event ->
            val resId = when (event) {
                SoundType.MOVE -> context.resources.getIdentifier("pop", "raw", context.packageName)
                else -> context.resources.getIdentifier("win", "raw", context.packageName)
            }
            if (resId != 0) {
                MediaPlayer.create(context, resId).apply {
                    setOnCompletionListener { release() }
                    start()
                }
            }
        }
    }

    Crossfade(targetState = currentScreen, label = "ScreenTransition", animationSpec = tween(500)) { screen ->
        when (screen) {
            AppScreen.SPLASH -> SplashScreen { viewModel.finishSplash() }
            AppScreen.HOME -> HomeScreen { size, mode -> viewModel.startGame(size, mode) }
            AppScreen.GAME -> GameScreen(viewModel) { viewModel.quitGame() }
        }
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        alpha.animateTo(1f, tween(500))
        delay(1500)
        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            // Logo Image
            Image(
                painter = painterResource(id = R.drawable.ic_logo_neon),
                contentDescription = "Logo",
                modifier = Modifier.size(160.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "DOTS & BOXES",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
            Text(
                "LOADING...",
                color = Player1Color,
                fontSize = 14.sp,
                letterSpacing = 4.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
fun HomeScreen(onStartGame: (Int, GameMode) -> Unit) {
    var selectedSize by remember { mutableStateOf(4) }
    var selectedMode by remember { mutableStateOf(GameMode.PvE) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- ADDED LOGO HERE ---
        Image(
            painter = painterResource(id = R.drawable.ic_logo_neon),
            contentDescription = "Logo",
            modifier = Modifier.size(180.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("DOTS & BOXES", fontSize = 42.sp, fontWeight = FontWeight.Black, color = Color.White)
        Text("CYBER EDITION", fontSize = 14.sp, color = Player1Color, letterSpacing = 8.sp)

        Spacer(modifier = Modifier.height(48.dp))

        GlassCard {
            Text("GRID SIZE", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf(3, 4, 5).forEach { size ->
                    SelectableButton(
                        text = "${size}x${size}",
                        isSelected = selectedSize == size,
                        onClick = { selectedSize = size }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("OPPONENT", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SelectableButton("AI BOT", selectedMode == GameMode.PvE) { selectedMode = GameMode.PvE }
                SelectableButton("HUMAN", selectedMode == GameMode.PvP) { selectedMode = GameMode.PvP }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { onStartGame(selectedSize, selectedMode) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Player1Color),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("START GAME", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
fun GameScreen(viewModel: GameViewModel, onQuit: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val isGameFinished = state.lines.all { it.isSelected }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth()) {
            TextButton(onClick = onQuit) { Text("← EXIT", color = Color.White.copy(alpha = 0.7f)) }
        }

        Spacer(modifier = Modifier.height(16.dp))
        ScoreBoard(state.scorePlayer1, state.scorePlayer2, state.currentPlayer, viewModel.getGameMode())
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            GlassCard {
                DotsAndBoxesBoard(state, { viewModel.onLineClicked(it) })
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (isGameFinished) {
        val winner = when {
            state.scorePlayer1 > state.scorePlayer2 -> "PLAYER 1 WINS!"
            state.scorePlayer2 > state.scorePlayer1 -> if(viewModel.getGameMode()==GameMode.PvE) "AI WINS!" else "PLAYER 2 WINS!"
            else -> "DRAW!"
        }

        AlertDialog(
            containerColor = Color(0xFF243B55),
            onDismissRequest = { viewModel.restartGame() },
            title = { Text("GAME OVER", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(winner, fontSize = 24.sp, color = Player1Color, fontWeight = FontWeight.Bold) },
            confirmButton = { Button(onClick = { viewModel.restartGame() }, colors = ButtonDefaults.buttonColors(containerColor = Player1Color)) { Text("REPLAY", color = Color.Black) } },
            dismissButton = { TextButton(onClick = onQuit) { Text("MENU", color = Color.Gray) } }
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
        PlayerBadge("P1", p1, Player1Color, current == Player.PLAYER1)
        Text("VS", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        PlayerBadge(if(mode==GameMode.PvE) "AI" else "P2", p2, Player2Color, current == Player.PLAYER2 || (mode==GameMode.PvE && current==Player.PLAYER2))
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

@Composable
fun SelectableButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) Player1Color else Color.Transparent
    val border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
    val textColor = if (isSelected) Color.Black else Color.White

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = border,
        modifier = Modifier.height(45.dp).width(100.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, color = textColor, fontWeight = FontWeight.Bold)
        }
    }
}