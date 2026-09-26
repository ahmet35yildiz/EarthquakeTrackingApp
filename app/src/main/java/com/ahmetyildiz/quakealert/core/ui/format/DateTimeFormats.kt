package com.ahmetyildiz.quakealert.core.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ahmetyildiz.quakealert.R
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private const val ZONE_OFFSET_PATTERN: String = "O"

@Composable
fun formatLocalDateTime(time: Instant): String {
    val dateTime: ZonedDateTime = time.atZone(ZoneId.systemDefault())
    val zoneLabel: String = DateTimeFormatter.ofPattern(ZONE_OFFSET_PATTERN, currentLocale()).format(dateTime)
    return stringResource(R.string.detail_local_time_value, formatDateTime(dateTime), zoneLabel)
}

@Composable
fun formatUtcDateTime(time: Instant): String =
    stringResource(R.string.detail_utc_time_value, formatDateTime(time.atZone(ZoneOffset.UTC)))

@Composable
private fun formatDateTime(dateTime: ZonedDateTime): String =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withLocale(currentLocale())
        .format(dateTime)
