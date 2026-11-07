package com.loopbreak.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

private fun ShadColors.toMaterial(dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary,
        onPrimary = primaryForeground,
        primaryContainer = secondary,
        onPrimaryContainer = foreground,
        secondary = secondary,
        onSecondary = foreground,
        secondaryContainer = accent,
        onSecondaryContainer = foreground,
        tertiary = primary,
        onTertiary = primaryForeground,
        background = background,
        onBackground = foreground,
        surface = background,
        onSurface = foreground,
        surfaceVariant = muted,
        onSurfaceVariant = mutedForeground,
        surfaceTint = primary,
        outline = input,
        outlineVariant = border,
        error = destructive,
        onError = primaryForeground,
        surfaceContainerLowest = background,
        surfaceContainerLow = card,
        surfaceContainer = card,
        surfaceContainerHigh = card,
        surfaceContainerHighest = muted,
    )
}

/** Generous corners: cards 24dp, controls 14dp. */
private val ShadShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun LoopBreakTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val shad = if (darkTheme) DarkShadColors else LightShadColors
    CompositionLocalProvider(LocalShadColors provides shad) {
        MaterialTheme(
            colorScheme = shad.toMaterial(darkTheme),
            typography = Typography,
            shapes = ShadShapes,
            content = content,
        )
    }
}

/** Convenience accessor: `Shad.colors.border`, `Shad.colors.mutedForeground`, ... */
object Shad {
    val colors: ShadColors
        @Composable get() = LocalShadColors.current
}
