package com.ahmetyildiz.quakealert.features.eventlog.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId

class EventLogTextTest {

    private val istanbul: ZoneId = ZoneId.of("Europe/Istanbul")

    @Test
    fun `each event becomes one line with local time, name and params`() {
        val events: List<LoggedEvent> = listOf(
            LoggedEvent(2, "language_changed", mapOf("from" to "system", "to" to "tr"), Instant.parse("2026-09-26T18:12:01.126Z")),
            LoggedEvent(1, "onboarding_started", emptyMap(), Instant.parse("2026-09-26T18:00:00Z")),
        )
        val text: String = EventLogText.format(events, istanbul)
        val expected = "2026-09-26T21:12:01+03:00 language_changed from=system, to=tr\n" +
            "2026-09-26T21:00:00+03:00 onboarding_started"
        assertEquals(expected, text)
    }

    @Test
    fun `no events give an empty text`() {
        assertEquals("", EventLogText.format(emptyList(), istanbul))
    }
}
