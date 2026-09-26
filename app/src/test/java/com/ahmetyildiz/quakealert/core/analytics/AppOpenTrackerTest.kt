package com.ahmetyildiz.quakealert.core.analytics

import com.ahmetyildiz.quakealert.core.notification.AlertNotificationTap
import com.ahmetyildiz.quakealert.core.time.FakeClock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration

class AppOpenTrackerTest {

    private val clock = FakeClock()
    private val analyticsTracker = FakeAnalyticsTracker()
    private val tracker = AppOpenTracker(analyticsTracker, clock)
    private val tap = AlertNotificationTap(eventId = "us7000abcd", postedAt = clock.now() - Duration.ofSeconds(95))
    private val notificationOpen: List<AnalyticsEvent> = listOf(
        AnalyticsEvent.AppOpened(AppOpenSource.NOTIFICATION),
        AnalyticsEvent.AlertNotificationOpened(eventId = "us7000abcd", delaySeconds = 95),
    )

    @Test
    fun `launcher start is tracked as launcher`() {
        tracker.onActivityCreated(isRestored = false, notificationTap = null)
        tracker.onActivityResumed()
        assertEquals(listOf(AnalyticsEvent.AppOpened(AppOpenSource.LAUNCHER)), analyticsTracker.events)
    }

    @Test
    fun `start from a notification tracks the source and the delay`() {
        tracker.onActivityCreated(isRestored = false, notificationTap = tap)
        assertEquals(notificationOpen, analyticsTracker.events)
    }

    @Test
    fun `rotation or language switch in the same process is not an open`() {
        tracker.onActivityCreated(isRestored = false, notificationTap = null)
        tracker.onActivityCreated(isRestored = true, notificationTap = null)
        tracker.onActivityResumed()
        assertEquals(1, analyticsTracker.events.size)
    }

    @Test
    fun `notification tap while the app runs tracks only the opened notification`() {
        tracker.onActivityCreated(isRestored = false, notificationTap = null)
        tracker.onNewIntent(tap)
        tracker.onActivityResumed()
        assertEquals(notificationOpen.last(), analyticsTracker.events.last())
        assertEquals(2, analyticsTracker.events.size)
    }

    @Test
    fun `notification tap after the process was killed is an open from the notification`() {
        tracker.onActivityCreated(isRestored = true, notificationTap = null)
        tracker.onNewIntent(tap)
        tracker.onActivityResumed()
        assertEquals(notificationOpen, analyticsTracker.events)
    }

    @Test
    fun `return from recents after the process was killed is a launcher open`() {
        tracker.onActivityCreated(isRestored = true, notificationTap = null)
        tracker.onActivityResumed()
        tracker.onActivityResumed()
        assertEquals(listOf(AnalyticsEvent.AppOpened(AppOpenSource.LAUNCHER)), analyticsTracker.events)
    }

    @Test
    fun `new intent without a notification tracks nothing`() {
        tracker.onActivityCreated(isRestored = false, notificationTap = null)
        tracker.onNewIntent(notificationTap = null)
        assertEquals(1, analyticsTracker.events.size)
    }

    @Test
    fun `posting time in the future counts as no delay`() {
        tracker.onActivityCreated(isRestored = false, notificationTap = null)
        tracker.onNewIntent(tap.copy(postedAt = clock.now() + Duration.ofMinutes(1)))
        assertEquals(AnalyticsEvent.AlertNotificationOpened("us7000abcd", 0), analyticsTracker.events.last())
    }

    @Test
    fun `nothing is tracked before the activity exists`() {
        tracker.onActivityResumed()
        assertTrue(analyticsTracker.events.isEmpty())
    }
}
