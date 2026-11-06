package com.loopbreak.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Design tokens from shadcn/ui (zinc base colour), see ui.shadcn.com/docs/theming.
 * Names follow the CSS variables: --background, --card, --muted, --border, ...
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
)

val LightShadColors = ShadColors(
    background = Color(0xFFFFFFFF),
    foreground = Color(0xFF09090B),
    card = Color(0xFFFFFFFF),
    primary = Color(0xFF0F766E),
    primaryForeground = Color(0xFFFFFFFF),
    secondary = Color(0xFFF4F4F5),
    muted = Color(0xFFF4F4F5),
    mutedForeground = Color(0xFF71717A),
    accent = Color(0xFFF4F4F5),
    destructive = Color(0xFFDC2626),
    warning = Color(0xFFD97706),
    violet = Color(0xFF7C3AED),
    border = Color(0xFFE4E4E7),
    input = Color(0xFFE4E4E7),
    tabActive = Color(0xFFFFFFFF),
)

val DarkShadColors = ShadColors(
    background = Color(0xFF09090B),
    foreground = Color(0xFFFAFAFA),
    card = Color(0xFF18181B),
    primary = Color(0xFF2DD4BF),
    primaryForeground = Color(0xFF042F2E),
    secondary = Color(0xFF27272A),
    muted = Color(0xFF27272A),
    mutedForeground = Color(0xFFA1A1AA),
    accent = Color(0xFF27272A),
    destructive = Color(0xFFF87171),
    warning = Color(0xFFFBBF24),
    violet = Color(0xFFA78BFA),
    border = Color(0xFF2F2F33),
    input = Color(0xFF3F3F46),
    tabActive = Color(0xFF18181B),
)

val LocalShadColors = staticCompositionLocalOf { LightShadColors }
