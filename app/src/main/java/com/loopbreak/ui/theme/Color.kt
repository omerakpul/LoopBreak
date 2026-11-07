package com.loopbreak.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Design tokens. Names follow the usual CSS-variable vocabulary: background, card, muted, border…
 * The palette is deliberately soft: tinted neutrals instead of pure black/white, quiet borders and
 * slightly desaturated accents, so nothing on screen shouts.
 */
@Immutable
data class ShadColors(
    val background: Color,
    val foreground: Color,
    val card: Color,
    val primary: Color,
    val primaryForeground: Color,
    val secondary: Color,
    val muted: Color,
    val mutedForeground: Color,
    val accent: Color,
    val destructive: Color,
    /** Accents used to tell loop types apart (primary doubles as the re-open colour). */
    val warning: Color,
    val violet: Color,
    val border: Color,
    val input: Color,
    /** Background of the selected trigger in [com.loopbreak.ui.components.ShadTabs]. */
    val tabActive: Color,
    /** Fill of secondary controls (icon buttons, outline buttons) sitting on a card. */
    val control: Color,
)

val LightShadColors = ShadColors(
    background = Color(0xFFF6F6F8),
    foreground = Color(0xFF1A1A1E),
    card = Color(0xFFFFFFFF),
    primary = Color(0xFF0F766E),
    primaryForeground = Color(0xFFFFFFFF),
    secondary = Color(0xFFEFEFF2),
    muted = Color(0xFFEDEDF1),
    mutedForeground = Color(0xFF6E6E78),
    accent = Color(0xFFEFEFF2),
    destructive = Color(0xFFD6453D),
    warning = Color(0xFFC98A1B),
    violet = Color(0xFF7A5BD6),
    border = Color(0xFFE9E9EE),
    input = Color(0xFFE4E4EA),
    tabActive = Color(0xFFFFFFFF),
    control = Color(0xFFF3F3F6),
)

val DarkShadColors = ShadColors(
    background = Color(0xFF0B0B0E),
    foreground = Color(0xFFE9E9EE),
    card = Color(0xFF17171B),
    primary = Color(0xFF5CC7B8),
    primaryForeground = Color(0xFF06302B),
    secondary = Color(0xFF25252B),
    muted = Color(0xFF222227),
    mutedForeground = Color(0xFF9C9CA6),
    accent = Color(0xFF25252B),
    destructive = Color(0xFFE5837D),
    warning = Color(0xFFE3B866),
    violet = Color(0xFFA99BE3),
    border = Color(0xFF242429),
    input = Color(0xFF2E2E34),
    tabActive = Color(0xFF33333A),
    control = Color(0xFF222227),
)

val LocalShadColors = staticCompositionLocalOf { LightShadColors }
