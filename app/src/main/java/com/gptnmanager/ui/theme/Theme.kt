package com.gptnmanager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    primary = BlueAccent,
    secondary = Color(0xFF9BB8FF),
    background = Navy,
    surface = NavySoft,
    surfaceContainer = NavyCard,
    surfaceContainerHigh = NavyCardSoft,
    primaryContainer = Color(0xFF203867),
    onPrimary = Color.White,
    onBackground = Color(0xFFF8FAFF),
    onSurface = Color(0xFFF8FAFF),
)

private val LightScheme = lightColorScheme(
    primary = Navy,
    secondary = BlueAccent,
    background = LightBg,
    surface = Color.White,
    surfaceContainer = LightCard,
    surfaceContainerHigh = LightCardSoft,
    primaryContainer = Color(0xFFDCE7FF),
    onPrimary = Color.White,
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF111827),
)

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