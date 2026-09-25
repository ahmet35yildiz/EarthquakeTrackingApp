package com.ahmetyildiz.quakealert.core.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLocale
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.roundToLong

@Composable
fun currentLocale(): Locale = LocalLocale.current.platformLocale

@Composable
fun formatWholeNumber(value: Double): String =
    NumberFormat.getIntegerInstance(currentLocale()).format(value.roundToLong())

@Composable
fun formatDataTime(time: Instant, now: Instant): String {
    val zone: ZoneId = ZoneId.systemDefault()
    val dateTime: ZonedDateTime = time.atZone(zone)
    val isToday: Boolean = dateTime.toLocalDate() == now.atZone(zone).toLocalDate()
    val formatter: DateTimeFormatter = if (isToday) {
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    } else {
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
    }
    return formatter.withLocale(currentLocale()).format(dateTime)
}
