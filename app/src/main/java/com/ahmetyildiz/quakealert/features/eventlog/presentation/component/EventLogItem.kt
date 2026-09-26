package com.ahmetyildiz.quakealert.features.eventlog.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ahmetyildiz.quakealert.core.ui.format.currentLocale
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
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
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatEventTime(event.time),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (event.params.isNotEmpty()) {
                ParamChips(params = event.params)
            }
        }
    }
}

@Composable
private fun ParamChips(params: Map<String, String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        params.forEach { (key, value) ->
            Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = MaterialTheme.shapes.small) {
                Text(
                    text = "$key=$value",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.extraSmall),
                )
            }
        }
    }
}

@Composable
private fun formatEventTime(time: Instant): String =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM)
        .withLocale(currentLocale())
        .format(time.atZone(ZoneId.systemDefault()))

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
