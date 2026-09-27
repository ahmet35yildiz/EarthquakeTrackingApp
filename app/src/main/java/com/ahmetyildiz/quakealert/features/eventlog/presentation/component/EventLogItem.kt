package com.ahmetyildiz.quakealert.features.eventlog.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.format.currentLocale
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.eventlog.domain.model.EventCategory
import com.ahmetyildiz.quakealert.features.eventlog.domain.model.LoggedEvent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun EventLogItem(
    event: LoggedEvent,
    modifier: Modifier = Modifier,
) {
    val style: CategoryStyle = EventCategory.fromEventName(event.name).style()
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.large),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CategoryIcon(style = style)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
                ) {
                    Text(text = event.name, style = MaterialTheme.typography.titleSmall, color = style.accentColor)
                    EventTime(time = event.time)
                }
            }
            if (event.params.isNotEmpty()) {
                ParamChips(params = event.params, valueColor = style.accentColor)
            }
        }
    }
}

private data class CategoryStyle(val icon: Painter, val accentColor: Color)

@Composable
private fun EventCategory.style(): CategoryStyle = when (this) {
    EventCategory.ALERT ->
        CategoryStyle(painterResource(R.drawable.ic_notifications), MaterialTheme.colorScheme.error)
    EventCategory.BACKGROUND ->
        CategoryStyle(rememberVectorPainter(Icons.Rounded.Refresh), MaterialTheme.colorScheme.primary)
    EventCategory.SETTINGS ->
        CategoryStyle(painterResource(R.drawable.ic_settings), MaterialTheme.colorScheme.tertiary)
    EventCategory.USAGE ->
        CategoryStyle(rememberVectorPainter(Icons.Rounded.Person), MaterialTheme.colorScheme.secondary)
}

@Composable
private fun CategoryIcon(style: CategoryStyle) {
    Box(
        modifier = Modifier
            .size(CATEGORY_ICON_CONTAINER_SIZE)
            .background(style.accentColor.copy(alpha = CATEGORY_ICON_CONTAINER_ALPHA), MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = style.icon,
            contentDescription = null,
            tint = style.accentColor,
            modifier = Modifier.size(CATEGORY_ICON_SIZE),
        )
    }
}

@Composable
private fun EventTime(time: Instant) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = CircleShape) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.extraSmall),
            horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_schedule),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(TIME_ICON_SIZE),
            )
            Text(
                text = formatEventTime(time),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ParamChips(params: Map<String, String>, valueColor: Color) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        params.forEach { (key, value) ->
            Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = MaterialTheme.shapes.small) {
                Text(
                    text = paramText(key = key, value = value, valueColor = valueColor),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.extraSmall),
                )
            }
        }
    }
}

@Composable
private fun paramText(key: String, value: String, valueColor: Color): AnnotatedString = buildAnnotatedString {
    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) { append(key) }
    withStyle(SpanStyle(color = MaterialTheme.colorScheme.outline)) { append(PARAM_SEPARATOR) }
    withStyle(SpanStyle(color = valueColor, fontWeight = FontWeight.SemiBold)) { append(value) }
}

@Composable
private fun formatEventTime(time: Instant): String =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM)
        .withLocale(currentLocale())
        .format(time.atZone(ZoneId.systemDefault()))

private const val PARAM_SEPARATOR: String = "="

private const val CATEGORY_ICON_CONTAINER_ALPHA: Float = 0.12f

private val CATEGORY_ICON_CONTAINER_SIZE: Dp = 32.dp

private val CATEGORY_ICON_SIZE: Dp = 18.dp

private val TIME_ICON_SIZE: Dp = 14.dp

@PreviewLightDark
@Composable
private fun EventLogItemPreview() {
    QuakeAlertTheme {
        Surface {
            EventLogItem(
                event = LoggedEvent(
                    id = 1,
                    name = "background_check_completed",
                    params = mapOf("fetched" to "12", "matched" to "1", "notified" to "1", "duration_ms" to "480"),
                    time = Instant.parse("2026-09-26T12:02:31Z"),
                ),
                modifier = Modifier.padding(Spacing.large),
            )
        }
    }
}
