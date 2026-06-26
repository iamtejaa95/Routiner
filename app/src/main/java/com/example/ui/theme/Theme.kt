package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ObsidianDarkColorScheme = darkColorScheme(
    primary = ElegantBronze,
    secondary = CafeCream,
    tertiary = CocoaWarm,
    background = ObsidianBlack,
    surface = VelvetCharcoal,
    onPrimary = Color.White,
    onSecondary = ObsidianBlack,
    onBackground = Color.White,
    onSurface = Color.White
)

private val MutedLightColorScheme = lightColorScheme(
    primary = ElegantBronze,
    secondary = CoffeeWarmLight,
    tertiary = CocoaWarm,
    background = AmberWarmLight,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = ObsidianBlack,
    onSurface = ObsidianBlack
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force premium dark mode by default for that elegant black/brown liquid glass aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ObsidianDarkColorScheme else MutedLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val viewCompat = WindowCompat.getInsetsController(window, view)
            viewCompat.isAppearanceLightStatusBars = !darkTheme
            viewCompat.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
