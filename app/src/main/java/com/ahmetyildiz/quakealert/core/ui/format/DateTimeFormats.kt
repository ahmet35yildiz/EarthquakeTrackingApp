package com.ahmetyildiz.quakealert.core.ui.format

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ahmetyildiz.quakealert.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private const val ZONE_OFFSET_PATTERN: String = "O"
private const val DAY_LABEL_SKELETON: String = "EEEEdMMMM"
private const val SHORT_DATE_SKELETON: String = "dMMM"
private const val SHORT_WEEKDAY_SKELETON: String = "EEE"

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

@Composable
fun formatDayLabel(date: LocalDate, today: LocalDate): String =
    when (date) {
        today -> stringResource(R.string.day_today)
        today.minusDays(1) -> stringResource(R.string.day_yesterday)
        else -> DateTimeFormatter
            .ofPattern(DateFormat.getBestDateTimePattern(currentLocale(), DAY_LABEL_SKELETON), currentLocale())
            .format(date)
    }

@Composable
fun formatShortDate(date: LocalDate): String = formatWithSkeleton(date, SHORT_DATE_SKELETON)

@Composable
fun formatShortWeekday(date: LocalDate): String = formatWithSkeleton(date, SHORT_WEEKDAY_SKELETON)

@Composable
private fun formatWithSkeleton(date: LocalDate, skeleton: String): String =
    DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(currentLocale(), skeleton), currentLocale()).format(date)
