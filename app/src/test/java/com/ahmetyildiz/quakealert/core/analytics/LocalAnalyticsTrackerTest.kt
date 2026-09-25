package com.ahmetyildiz.quakealert.core.analytics

import android.util.Log
import com.ahmetyildiz.quakealert.core.database.AnalyticsEventEntity
import com.ahmetyildiz.quakealert.core.database.FakeAnalyticsEventDao
import com.ahmetyildiz.quakealert.core.time.FakeClock
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.IOException
import java.time.Duration

class LocalAnalyticsTrackerTest {

    private val dao = FakeAnalyticsEventDao()
    private val clock = FakeClock()

    @BeforeEach
    fun mockLogcat() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>(), any()) } returns 0
    }

    @AfterEach
    fun unmockLogcat() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `tracked event is stored with its name, params and the time it was tracked`() = runTrackerTest { tracker ->
        tracker.track(AnalyticsEvent.AlertAreaSet(countryCode = "TR", radiusKm = 250, context = SetupContext.SETTINGS))
        runCurrent()
        val expected = AnalyticsEventEntity(
            id = 1,
            name = "alert_area_set",
            params = mapOf("country_code" to "TR", "radius_km" to "250", "context" to "settings"),
            timestampEpochMs = clock.now().toEpochMilli(),
        )
        assertEquals(listOf(expected), dao.storedEvents)
    }

    @Test
    fun `events are stored in the order they were tracked`() = runTrackerTest { tracker ->
        tracker.track(AnalyticsEvent.AppOpened(AppOpenSource.LAUNCHER))
        clock.advanceBy(Duration.ofSeconds(1))
        tracker.track(AnalyticsEvent.OnboardingStarted)
        runCurrent()
        assertEquals(listOf("app_opened", "onboarding_started"), dao.storedEvents.map { it.name })
    }

    @Test
    fun `a storage failure does not reach the caller and later events are still stored`() = runTrackerTest { tracker ->
        dao.insertFailure = IOException("disk full")
        assertDoesNotThrow {
            tracker.track(AnalyticsEvent.OnboardingStarted)
            runCurrent()
        }
        dao.insertFailure = null
        tracker.track(AnalyticsEvent.DeveloperCheckTriggered)
        runCurrent()
        assertEquals(listOf("developer_check_triggered"), dao.storedEvents.map { it.name })
    }

    private fun runTrackerTest(block: suspend TestScope.(LocalAnalyticsTracker) -> Unit) = runTest {
        val tracker = LocalAnalyticsTracker(dao = dao, clock = clock, scope = backgroundScope)
        block(tracker)
    }
}
