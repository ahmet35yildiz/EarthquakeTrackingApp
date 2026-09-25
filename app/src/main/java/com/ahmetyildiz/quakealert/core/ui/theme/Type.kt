package com.ahmetyildiz.quakealert.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

// Material 3 type scale with the design system's weights. The system font (Roboto) is used instead of bundling
// Roboto Flex: same metrics and look, no font download or extra APK size.

private fun textStyle(size: TextUnit, lineHeight: TextUnit, weight: FontWeight, letterSpacing: TextUnit): TextStyle =
    TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = size,
        lineHeight = lineHeight,
        fontWeight = weight,
        letterSpacing = letterSpacing,
    )

internal val QuakeAlertTypography: Typography = Typography(
    displayLarge = textStyle(57.sp, 64.sp, FontWeight.Normal, (-0.25).sp),
    displayMedium = textStyle(45.sp, 52.sp, FontWeight.Normal, 0.sp),
    displaySmall = textStyle(36.sp, 44.sp, FontWeight.Normal, 0.sp),
    headlineLarge = textStyle(32.sp, 40.sp, FontWeight.Medium, 0.sp),
    headlineMedium = textStyle(28.sp, 36.sp, FontWeight.Medium, 0.sp),
    headlineSmall = textStyle(24.sp, 32.sp, FontWeight.Medium, 0.sp),
    titleLarge = textStyle(22.sp, 28.sp, FontWeight.Medium, 0.sp),
    titleMedium = textStyle(16.sp, 24.sp, FontWeight.SemiBold, 0.15.sp),
    titleSmall = textStyle(14.sp, 20.sp, FontWeight.SemiBold, 0.1.sp),
    bodyLarge = textStyle(16.sp, 24.sp, FontWeight.Normal, 0.5.sp),
    bodyMedium = textStyle(14.sp, 20.sp, FontWeight.Normal, 0.25.sp),
    bodySmall = textStyle(12.sp, 16.sp, FontWeight.Normal, 0.4.sp),
    labelLarge = textStyle(14.sp, 20.sp, FontWeight.SemiBold, 0.1.sp),
    labelMedium = textStyle(12.sp, 16.sp, FontWeight.SemiBold, 0.5.sp),
    labelSmall = textStyle(11.sp, 16.sp, FontWeight.SemiBold, 0.5.sp),
)

/** Magnitude numbers: bold with tabular figures so values line up in lists. */
val MagnitudeTextStyle: TextStyle = textStyle(18.sp, 22.sp, FontWeight.Bold, (-0.2).sp)
    .copy(fontFeatureSettings = "tnum")

/** Magnitude number in large badges (detail screen). */
val MagnitudeLargeTextStyle: TextStyle = textStyle(28.sp, 36.sp, FontWeight.Bold, 0.sp)
    .copy(fontFeatureSettings = "tnum")
