package com.ziadmq.dotsandboxes.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Updated to use DeepBackground and GlassSurface from the new Color.kt
private val DarkColorScheme = darkColorScheme(
    primary = Player1Color,
    secondary = Player2Color,
    background = DeepBackground,  // Fixed: Was BackgroundDark
    surface = GlassSurface,       // Fixed: Was SurfaceDark
    onBackground = Color.White,
    onSurface = Color.White
)

// We force the Glass/Dark look even in Light mode for this specific game design,
// or you can keep the light version. Here I've mapped it to the Glass colors
// so the game looks consistent regardless of system settings.
private val LightColorScheme = lightColorScheme(
    primary = Player1Color,
    secondary = Player2Color,
    background = DeepBackground,
    surface = GlassSurface,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun DotsAndBoxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep disabled for consistent game design
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false // Force light text on status bar
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}