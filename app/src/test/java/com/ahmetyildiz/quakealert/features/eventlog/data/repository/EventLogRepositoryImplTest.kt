package com.ahmetyildiz.quakealert.features.eventlog.data.repository

import com.ahmetyildiz.quakealert.core.database.AnalyticsEventEntity
import com.ahmetyildiz.quakealert.core.database.FakeAnalyticsEventDao
import com.ahmetyildiz.quakealert.features.eventlog.domain.model.LoggedEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class EventLogRepositoryImplTest {

    private val dao = FakeAnalyticsEventDao()
    private val repository = EventLogRepositoryImpl(dao)

    @Test
    fun `stored events are mapped newest first`() = runTest {
        dao.insert(AnalyticsEventEntity(name = "app_opened", params = mapOf("source" to "launcher"), timestampEpochMs = 1_000))
        dao.insert(AnalyticsEventEntity(name = "onboarding_started", params = emptyMap(), timestampEpochMs = 2_000))
        val events: List<LoggedEvent> = repository.observeEvents().first()
        val expected: List<LoggedEvent> = listOf(
            LoggedEvent(2, "onboarding_started", emptyMap(), Instant.ofEpochMilli(2_000)),
            LoggedEvent(1, "app_opened", mapOf("source" to "launcher"), Instant.ofEpochMilli(1_000)),
        )
        assertEquals(expected, events)
    }

    @Test
    fun `clearing deletes every stored event`() = runTest {
        dao.insert(AnalyticsEventEntity(name = "app_opened", params = emptyMap(), timestampEpochMs = 1_000))
        repository.clear()
        assertTrue(repository.observeEvents().first().isEmpty())
    }
}
