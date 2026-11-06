package com.loopbreak.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private fun style(size: Int, weight: FontWeight, line: Int, tracking: Double = 0.0) = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp,
)

/** shadcn-like scale: tight semibold headings, 14sp body, small medium labels. */
val Typography = Typography(
    displayLarge = style(48, FontWeight.Bold, 52, -1.2),
    displayMedium = style(36, FontWeight.Bold, 40, -0.9),
    headlineMedium = style(24, FontWeight.SemiBold, 30, -0.6),
    titleLarge = style(20, FontWeight.SemiBold, 26, -0.4),
    titleMedium = style(16, FontWeight.SemiBold, 22, -0.2),
    titleSmall = style(14, FontWeight.Medium, 20),
    bodyLarge = style(15, FontWeight.Normal, 22),
    bodyMedium = style(14, FontWeight.Normal, 20),
    bodySmall = style(12, FontWeight.Normal, 16),
    labelLarge = style(14, FontWeight.Medium, 20),
    labelMedium = style(12, FontWeight.Medium, 16),
    labelSmall = style(11, FontWeight.Medium, 14),
)
