package com.ahmetyildiz.quakealert.features.eventlog.domain.usecase

import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.SetupContext
import com.ahmetyildiz.quakealert.features.eventlog.domain.AlertMetricsConfig
import com.ahmetyildiz.quakealert.features.eventlog.domain.model.AlertMetrics
import com.ahmetyildiz.quakealert.features.eventlog.domain.model.LoggedEvent
import com.ahmetyildiz.quakealert.features.eventlog.domain.model.MetricRatio
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class CalculateAlertMetricsUseCaseTest {

    private val calculate = CalculateAlertMetricsUseCase()
    private val start: Instant = Instant.parse("2026-09-29T08:00:00Z")
    private val window: Duration = AlertMetricsConfig.OPT_OUT_WINDOW
    private var nextId: Long = 1

    @Test
    fun `empty log gives zero counts without percentages`() {
        val metrics: AlertMetrics = calculate(emptyList())
        assertEquals(MetricRatio(count = 0, total = 0), metrics.notificationOpens)
        assertNull(metrics.usefulAnswers.percent)
    }

    @Test
    fun `setup completion compares completed with started onboardings`() {
        val events: List<LoggedEvent> = listOf(
            at(0, AnalyticsEvent.OnboardingStarted),
            at(1, AnalyticsEvent.OnboardingStarted),
            at(2, AnalyticsEvent.OnboardingCompleted(threshold = 4.5, radiusKm = null, isNotificationsGranted = true)),
        )
        assertEquals(MetricRatio(count = 1, total = 2), calculate(events).setupCompletion)
    }

    @Test
    fun `opens compare opened with posted notifications`() {
        val events: List<LoggedEvent> = listOf(posted(0, "a"), posted(1, "b"), opened(2, "a"))
        assertEquals(MetricRatio(count = 1, total = 2), calculate(events).notificationOpens)
    }

    @Test
    fun `useful rate counts only answers, opens without an answer are left out`() {
        val events: List<LoggedEvent> = listOf(
            opened(0, "a"),
            opened(1, "b"),
            opened(2, "c"),
            opened(3, "d"),
            at(4, AnalyticsEvent.AlertFeedbackGiven(eventId = "a", isUseful = true)),
            at(5, AnalyticsEvent.AlertFeedbackGiven(eventId = "b", isUseful = false)),
        )
        val metrics: AlertMetrics = calculate(events)
        assertEquals(MetricRatio(count = 1, total = 2), metrics.usefulAnswers)
        assertEquals(50, metrics.usefulAnswers.percent)
        assertEquals(MetricRatio(count = 2, total = 4), metrics.answeredOpens)
    }

    @Test
    fun `turning alerts off or raising the threshold soon after a notification counts`() {
        val events: List<LoggedEvent> = listOf(
            posted(0, "a"),
            at(1, AnalyticsEvent.AlertsToggled(isEnabled = false)),
            at(2, AnalyticsEvent.AlertThresholdChanged(from = 4.5, to = 5.5, context = SetupContext.SETTINGS)),
        )
        val metrics: AlertMetrics = calculate(events)
        assertEquals(1, metrics.alertsTurnedOffAfterNotification)
        assertEquals(1, metrics.thresholdsRaisedAfterNotification)
        assertEquals(MetricRatio(count = 1, total = 1), metrics.notificationsFollowedByOptOut)
    }

    @Test
    fun `changes that are not an opt out, too late or before any notification do not count`() {
        val events: List<LoggedEvent> = listOf(
            at(-1, AnalyticsEvent.AlertsToggled(isEnabled = false)),
            posted(0, "a"),
            at(1, AnalyticsEvent.AlertsToggled(isEnabled = true)),
            at(2, AnalyticsEvent.AlertThresholdChanged(from = 5.5, to = 4.5, context = SetupContext.SETTINGS)),
            at(3, AnalyticsEvent.AlertThresholdChanged(from = 4.5, to = 6.0, context = SetupContext.ONBOARDING)),
            atTime(start + window + Duration.ofMinutes(1), AnalyticsEvent.AlertsToggled(isEnabled = false)),
        )
        val metrics: AlertMetrics = calculate(events)
        assertEquals(0, metrics.alertsTurnedOffAfterNotification)
        assertEquals(0, metrics.thresholdsRaisedAfterNotification)
        assertEquals(MetricRatio(count = 0, total = 1), metrics.notificationsFollowedByOptOut)
    }

    @Test
    fun `a change at the end of the window still counts`() {
        val events: List<LoggedEvent> = listOf(
            posted(0, "a"),
            atTime(start + window, AnalyticsEvent.AlertsToggled(isEnabled = false)),
        )
        assertEquals(1, calculate(events).alertsTurnedOffAfterNotification)
    }

    @Test
    fun `only notifications with an opt out in their window are counted as followed`() {
        val events: List<LoggedEvent> = listOf(
            posted(0, "a"),
            atTime(start + window.multipliedBy(2), AnalyticsEvent.AlertNotificationPosted("b", 5.0, 1)),
            atTime(start + window.multipliedBy(2) + Duration.ofHours(1), AnalyticsEvent.AlertsToggled(isEnabled = false)),
        )
        assertEquals(MetricRatio(count = 1, total = 2), calculate(events).notificationsFollowedByOptOut)
    }

    private fun posted(minutes: Long, eventId: String): LoggedEvent =
        at(minutes, AnalyticsEvent.AlertNotificationPosted(eventId = eventId, magnitude = 5.0, batchSize = 1))

    private fun opened(minutes: Long, eventId: String): LoggedEvent =
        at(minutes, AnalyticsEvent.AlertNotificationOpened(eventId = eventId, delaySeconds = 30))

    private fun at(minutes: Long, event: AnalyticsEvent): LoggedEvent = atTime(start + Duration.ofMinutes(minutes), event)

    private fun atTime(time: Instant, event: AnalyticsEvent): LoggedEvent =
        LoggedEvent(id = nextId++, name = event.name, params = event.params, time = time)
}
