package com.ahmetyildiz.quakealert.features.earthquakes.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.component.SectionCard
import com.ahmetyildiz.quakealert.core.ui.format.formatShortDate
import com.ahmetyildiz.quakealert.core.ui.format.formatShortWeekday
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.DailyCount
import kotlin.math.min

private val ChartHeight: Dp = 140.dp
private val BarGap: Dp = 2.dp
private val BarCornerRadius: Dp = 4.dp
private val BaselineWidth: Dp = 1.dp
private const val MAX_DAYS_WITH_WEEKDAY_LABELS: Int = 7
private const val DAY_SEPARATOR: String = ", "

@Composable
fun DailyCountCard(dailyCounts: List<DailyCount>, modifier: Modifier = Modifier) {
    val busiestDay: DailyCount = dailyCounts.maxBy { it.count }
    SectionCard(
        title = stringResource(R.string.statistics_daily_title),
        icon = painterResource(R.drawable.ic_schedule),
        modifier = modifier,
    ) {
        DailyCountChart(dailyCounts = dailyCounts, description = chartDescription(dailyCounts))
        DayLabels(dailyCounts = dailyCounts)
        Text(
            text = LocalResources.current.getQuantityString(
                R.plurals.statistics_busiest_day,
                busiestDay.count,
                formatShortDate(busiestDay.date),
                busiestDay.count,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DailyCountChart(dailyCounts: List<DailyCount>, description: String) {
    val barColor: Color = MaterialTheme.colorScheme.primary
    val baselineColor: Color = MaterialTheme.colorScheme.outlineVariant
    val maxCount: Int = dailyCounts.maxOf { it.count }.coerceAtLeast(1)
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(ChartHeight)
            .semantics { contentDescription = description },
    ) {
        val gap: Float = BarGap.toPx()
        val barWidth: Float = (size.width - gap * (dailyCounts.size - 1)) / dailyCounts.size
        dailyCounts.forEachIndexed { index, day ->
            drawBar(left = index * (barWidth + gap), width = barWidth, height = size.height * day.count / maxCount, color = barColor)
        }
        drawLine(
            color = baselineColor,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = BaselineWidth.toPx(),
        )
    }
}

private fun DrawScope.drawBar(left: Float, width: Float, height: Float, color: Color) {
    if (height <= 0f) return
    val radius = CornerRadius(min(BarCornerRadius.toPx(), width / 2))
    val bar = RoundRect(
        left = left,
        top = size.height - height,
        right = left + width,
        bottom = size.height,
        topLeftCornerRadius = radius,
        topRightCornerRadius = radius,
    )
    drawPath(path = Path().apply { addRoundRect(bar) }, color = color)
}

@Composable
private fun DayLabels(dailyCounts: List<DailyCount>) {
    val labelStyle = MaterialTheme.typography.labelSmall
    val labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
    if (dailyCounts.size <= MAX_DAYS_WITH_WEEKDAY_LABELS) {
        Row(modifier = Modifier.fillMaxWidth()) {
            dailyCounts.forEach { day ->
                Text(
                    text = formatShortWeekday(day.date),
                    style = labelStyle,
                    color = labelColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    } else {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = formatShortDate(dailyCounts.first().date), style = labelStyle, color = labelColor)
            Text(text = formatShortDate(dailyCounts.last().date), style = labelStyle, color = labelColor)
        }
    }
}

@Composable
private fun chartDescription(dailyCounts: List<DailyCount>): String {
    val days: String = dailyCounts
        .map { day -> stringResource(R.string.statistics_day_count, formatShortDate(day.date), day.count) }
        .joinToString(DAY_SEPARATOR)
    return stringResource(R.string.statistics_daily_description, days)
}
