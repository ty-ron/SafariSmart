package com.safarismart.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = SafariGreen,
    onPrimary = Color.White,
    secondary = SafariAmber,
    background = Color(0xFFF7FAF7)
)

private val DarkColors = darkColorScheme(
    primary = SafariGreenLight,
    onPrimary = Color(0xFF00391C),
    secondary = SafariAmber
)

@Composable
fun SafariSmartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}