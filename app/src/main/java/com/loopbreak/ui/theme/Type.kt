package com.loopbreak.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.loopbreak.R

/** The bundled fonts are variable fonts (API 26+): one file, any weight. */
@OptIn(ExperimentalTextApi::class)
private fun variable(resId: Int, weight: FontWeight) = Font(
    resId = resId,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

private val weights = listOf(FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold)

/** Titles, headings and big numbers: characterful geometric grotesque. */
val DisplayFontFamily = FontFamily(weights.map { variable(R.font.space_grotesk, it) })

/** Body text and labels: neutral and very readable at small sizes. */
val BodyFontFamily = FontFamily(weights.map { variable(R.font.inter, it) })

/** Timestamps and other tabular data. */
val MonoFontFamily = FontFamily(weights.map { variable(R.font.jetbrains_mono, it) })

private fun style(
    family: FontFamily,
    size: Int,
    weight: FontWeight,
    line: Int,
    tracking: Double = 0.0,
) = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp,
)

/** Light-handed scale: medium-weight headings, 14sp body, small medium labels. */
val Typography = Typography(
    displayLarge = style(DisplayFontFamily, 48, FontWeight.SemiBold, 52, -1.2),
    displayMedium = style(DisplayFontFamily, 36, FontWeight.SemiBold, 40, -0.9),
    headlineMedium = style(DisplayFontFamily, 24, FontWeight.Medium, 30, -0.6),
    titleLarge = style(DisplayFontFamily, 20, FontWeight.Medium, 26, -0.4),
    titleMedium = style(DisplayFontFamily, 16, FontWeight.Medium, 22, -0.2),
    titleSmall = style(BodyFontFamily, 14, FontWeight.Medium, 20),
    bodyLarge = style(BodyFontFamily, 15, FontWeight.Normal, 22),
    bodyMedium = style(BodyFontFamily, 14, FontWeight.Normal, 20),
    bodySmall = style(BodyFontFamily, 12, FontWeight.Normal, 16),
    labelLarge = style(BodyFontFamily, 14, FontWeight.Medium, 20),
    labelMedium = style(BodyFontFamily, 12, FontWeight.Medium, 16),
    labelSmall = style(BodyFontFamily, 11, FontWeight.Medium, 14),
)
