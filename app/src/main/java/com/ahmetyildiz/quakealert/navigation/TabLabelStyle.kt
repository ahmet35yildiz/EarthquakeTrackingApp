package com.ahmetyildiz.quakealert.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val LABEL_FONT_SIZE_STEP: Float = 0.25f

@Composable
fun rememberTabLabelStyle(labelWidth: Dp): TextStyle {
    val textMeasurer: TextMeasurer = rememberTextMeasurer()
    val themeStyle: TextStyle = MaterialTheme.typography.labelMedium
    val labels: List<String> = TopLevelDestination.entries.map { stringResource(it.labelRes) }
    val density: Density = LocalDensity.current
    val maxWidthPx: Int = with(density) { labelWidth.roundToPx() }
    return remember(labels, maxWidthPx, themeStyle, density) {
        val unscaledFontSize: TextUnit = with(density) { themeStyle.fontSize.value.dp.toSp() }
        candidateFontSizes(from = themeStyle.fontSize, downTo = unscaledFontSize)
            .map { themeStyle.withFontSize(it) }
            .firstOrNull { style -> labels.all { textMeasurer.fits(it, style, maxWidthPx) } }
            ?: themeStyle.withFontSize(unscaledFontSize)
    }
}

private fun candidateFontSizes(from: TextUnit, downTo: TextUnit): Sequence<TextUnit> =
    generateSequence(from.value) { it - LABEL_FONT_SIZE_STEP }
        .takeWhile { it >= downTo.value }
        .map { it.sp }

private fun TextStyle.withFontSize(size: TextUnit): TextStyle =
    copy(fontSize = size, letterSpacing = letterSpacing * (size.value / fontSize.value))

private fun TextMeasurer.fits(text: String, style: TextStyle, maxWidthPx: Int): Boolean =
    measure(text = text, style = style, maxLines = 1, softWrap = false).size.width <= maxWidthPx
