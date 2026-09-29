package com.ahmetyildiz.quakealert.core.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val FONT_SIZE_STEP: Float = 0.25f

@Composable
fun rememberFittingTextStyle(lines: List<String>, maxWidth: Dp, style: TextStyle): TextStyle {
    val textMeasurer: TextMeasurer = rememberTextMeasurer()
    val density: Density = LocalDensity.current
    val maxWidthPx: Int = with(density) { maxWidth.roundToPx() }
    return remember(lines, maxWidthPx, style, density) {
        val unscaledFontSize: TextUnit = with(density) { style.fontSize.value.dp.toSp() }
        candidateFontSizes(from = style.fontSize, downTo = unscaledFontSize)
            .map { style.withFontSize(it) }
            .firstOrNull { candidate -> lines.all { textMeasurer.fitsOnOneLine(it, candidate, maxWidthPx) } }
            ?: style.withFontSize(unscaledFontSize)
    }
}

private fun candidateFontSizes(from: TextUnit, downTo: TextUnit): Sequence<TextUnit> =
    generateSequence(from.value) { it - FONT_SIZE_STEP }
        .takeWhile { it >= downTo.value }
        .map { it.sp }

private fun TextStyle.withFontSize(size: TextUnit): TextStyle {
    val ratio: Float = size.value / fontSize.value
    return copy(fontSize = size, letterSpacing = letterSpacing * ratio, lineHeight = lineHeight * ratio)
}

private fun TextMeasurer.fitsOnOneLine(text: String, style: TextStyle, maxWidthPx: Int): Boolean =
    measure(text = text, style = style, maxLines = 1, softWrap = false).size.width <= maxWidthPx
