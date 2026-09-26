package com.ahmetyildiz.quakealert.features.eventlog.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class EventLogFilterTest {

    private val events: List<LoggedEvent> = listOf("app_opened", "alert_threshold_changed", "alerts_toggled")
        .mapIndexed { index, name -> LoggedEvent(index.toLong(), name, emptyMap(), Instant.EPOCH) }

    @Test
    fun `blank query keeps every event`() {
        assertEquals(events, events.filterByName("  "))
    }

    @Test
    fun `query matches part of the name ignoring case and surrounding spaces`() {
        val names: List<String> = events.filterByName(" ALERT").map { it.name }
        assertEquals(listOf("alert_threshold_changed", "alerts_toggled"), names)
    }

    @Test
    fun `query without a match returns nothing`() {
        assertEquals(emptyList<LoggedEvent>(), events.filterByName("detail"))
    }
}
