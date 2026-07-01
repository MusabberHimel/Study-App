package com.musabber.pomofocus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface
)

private val ClassicColorScheme = lightColorScheme(
    primary = ClassicPrimary,
    background = ClassicBackground,
    surface = ClassicSurface,
    onBackground = ClassicOnBackground,
    onSurface = ClassicOnSurface
)

private val CyberpunkColorScheme = darkColorScheme(
    primary = CyberpunkPrimary,
    secondary = CyberpunkSecondary,
    background = CyberpunkBackground,
    surface = CyberpunkSurface,
    onBackground = CyberpunkOnBackground,
    onSurface = CyberpunkOnSurface
)

private val RetroColorScheme = lightColorScheme(
    primary = RetroPrimary,
    background = RetroBackground,
    surface = RetroSurface,
    onBackground = RetroOnBackground,
    onSurface = RetroOnSurface
)

private val RoyalGoldColorScheme = darkColorScheme(
    primary = RoyalPrimary,
    background = RoyalBackground,
    surface = RoyalSurface,
    onBackground = RoyalOnBackground,
    onSurface = RoyalOnSurface
)

@Composable
fun PomoFocusTheme(
    themeName: String,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeName) {
        "Classic" -> ClassicColorScheme
        "Cyberpunk" -> CyberpunkColorScheme
        "Retro" -> RetroColorScheme
        "Royal Gold" -> RoyalGoldColorScheme
        else -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}