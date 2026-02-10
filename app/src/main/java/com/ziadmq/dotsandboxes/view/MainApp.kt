package com.ziadmq.dotsandboxes.view

import android.media.MediaPlayer
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ziadmq.dotsandboxes.model.SoundType
import com.ziadmq.dotsandboxes.viewmodel.AppScreen
import com.ziadmq.dotsandboxes.viewmodel.GameViewModel

@Composable
fun MainApp(viewModel: GameViewModel = viewModel(), onShowInterstitial: () -> Unit) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val context = LocalContext.current

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
            AppScreen.HOME -> HomeScreen(onShowInterstitial) { size, mode ->
                viewModel.startGame(size, mode)
            }
            AppScreen.GAME -> GameScreen(viewModel, onShowInterstitial) {
                viewModel.quitGame()
            }
        }
    }
}