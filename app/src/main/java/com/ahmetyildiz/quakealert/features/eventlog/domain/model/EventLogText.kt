package com.ahmetyildiz.quakealert.features.eventlog.domain.model

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object EventLogText {

    private const val LINE_SEPARATOR: String = "\n"
    private const val PARAM_SEPARATOR: String = ", "

    fun format(events: List<LoggedEvent>, zoneId: ZoneId): String =
        events.joinToString(LINE_SEPARATOR) { formatLine(it, zoneId) }

    private fun formatLine(event: LoggedEvent, zoneId: ZoneId): String {
        val time: String = DateTimeFormatter.ISO_OFFSET_DATE_TIME
            .format(event.time.atZone(zoneId).truncatedTo(ChronoUnit.SECONDS))
        val params: String = event.params.entries.joinToString(PARAM_SEPARATOR) { "${it.key}=${it.value}" }
        return listOf(time, event.name, params).filter { it.isNotEmpty() }.joinToString(" ")
    }
}
