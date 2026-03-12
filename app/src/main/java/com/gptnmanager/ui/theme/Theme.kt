package com.gptnmanager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

private val DarkScheme = darkColorScheme(
    primary = Blue,
    secondary = ColorTokens.Secondary,
    background = Navy,
    surface = NavySurface,
)

private val LightScheme = lightColorScheme(
    primary = Navy,
    secondary = Blue,
    background = LightBg,
    surface = androidx.compose.ui.graphics.Color.White,
)

object ColorTokens {
    val Secondary = androidx.compose.ui.graphics.Color(0xFF7FC8FF)
}

@Composable
fun GPTNManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = Typography,
        content = content,
    )
}