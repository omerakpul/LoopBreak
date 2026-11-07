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
    background = Color(0xFFF7F7F9),
    foreground = Color(0xFF2A2A30),
    card = Color(0xFFFFFFFF),
    primary = Color(0xFF237F76),
    primaryForeground = Color(0xFFFFFFFF),
    secondary = Color(0xFFF0F0F3),
    muted = Color(0xFFEEEEF2),
    mutedForeground = Color(0xFF72727C),
    accent = Color(0xFFF0F0F3),
    destructive = Color(0xFFD4524A),
    warning = Color(0xFFC48A2C),
    violet = Color(0xFF7C63D0),
    border = Color(0xFFEBEBF0),
    input = Color(0xFFE6E6EC),
    tabActive = Color(0xFFFFFFFF),
    control = Color(0xFFF4F4F7),
)

val DarkShadColors = ShadColors(
    background = Color(0xFF0B0B0E),
    foreground = Color(0xFFDEDEE3),
    card = Color(0xFF17171B),
    primary = Color(0xFF6DBEB2),
    primaryForeground = Color(0xFF06302B),
    secondary = Color(0xFF232328),
    muted = Color(0xFF202025),
    mutedForeground = Color(0xFF90909A),
    accent = Color(0xFF232328),
    destructive = Color(0xFFDE8F89),
    warning = Color(0xFFD9B779),
    violet = Color(0xFFA79DDB),
    border = Color(0xFF222226),
    input = Color(0xFF2B2B31),
    tabActive = Color(0xFF2F2F36),
    control = Color(0xFF202025),
)

val LocalShadColors = staticCompositionLocalOf { LightShadColors }
