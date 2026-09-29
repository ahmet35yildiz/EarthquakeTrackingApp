package com.ahmetyildiz.quakealert.features.eventlog.domain.usecase

import com.ahmetyildiz.quakealert.features.eventlog.domain.AlertMetricsConfig
import com.ahmetyildiz.quakealert.features.eventlog.domain.model.AlertMetrics
import com.ahmetyildiz.quakealert.features.eventlog.domain.model.LoggedEvent
import com.ahmetyildiz.quakealert.features.eventlog.domain.model.MeasuredEvents
import com.ahmetyildiz.quakealert.features.eventlog.domain.model.MetricRatio
import java.time.Instant
import javax.inject.Inject

class CalculateAlertMetricsUseCase @Inject constructor() {

    operator fun invoke(events: List<LoggedEvent>): AlertMetrics {
        val posted: List<LoggedEvent> = events.named(MeasuredEvents.NOTIFICATION_POSTED)
        val openedCount: Int = events.named(MeasuredEvents.NOTIFICATION_OPENED).size
        val answers: List<LoggedEvent> = events.named(MeasuredEvents.FEEDBACK_GIVEN)
        val optOutsAfterNotification: List<LoggedEvent> = events.filter { isOptOut(it) && it.isShortlyAfterAny(posted) }
        return AlertMetrics(
            setupCompletion = MetricRatio(
                count = events.named(MeasuredEvents.ONBOARDING_COMPLETED).size,
                total = events.named(MeasuredEvents.ONBOARDING_STARTED).size,
            ),
            notificationOpens = MetricRatio(count = openedCount, total = posted.size),
            answeredOpens = MetricRatio(count = answers.size, total = openedCount),
            usefulAnswers = MetricRatio(count = answers.count(::isUsefulAnswer), total = answers.size),
            notificationsFollowedByOptOut = MetricRatio(
                count = posted.count { notification -> optOutsAfterNotification.any { it.isShortlyAfter(notification) } },
                total = posted.size,
            ),
            alertsTurnedOffAfterNotification = optOutsAfterNotification.named(MeasuredEvents.ALERTS_TOGGLED).size,
            thresholdsRaisedAfterNotification = optOutsAfterNotification.named(MeasuredEvents.THRESHOLD_CHANGED).size,
            optOutWindow = AlertMetricsConfig.OPT_OUT_WINDOW,
        )
    }

    private fun List<LoggedEvent>.named(name: String): List<LoggedEvent> = filter { it.name == name }

    private fun isUsefulAnswer(event: LoggedEvent): Boolean =
        event.params[MeasuredEvents.PARAM_USEFUL] == MeasuredEvents.VALUE_TRUE

    private fun isOptOut(event: LoggedEvent): Boolean =
        when (event.name) {
            MeasuredEvents.ALERTS_TOGGLED -> event.params[MeasuredEvents.PARAM_ENABLED] == MeasuredEvents.VALUE_FALSE
            MeasuredEvents.THRESHOLD_CHANGED -> isThresholdRaisedInSettings(event)
            else -> false
        }

    private fun isThresholdRaisedInSettings(event: LoggedEvent): Boolean {
        if (event.params[MeasuredEvents.PARAM_CONTEXT] != MeasuredEvents.CONTEXT_SETTINGS) return false
        val from: Double = event.params[MeasuredEvents.PARAM_FROM]?.toDoubleOrNull() ?: return false
        val to: Double = event.params[MeasuredEvents.PARAM_TO]?.toDoubleOrNull() ?: return false
        return to > from
    }

    private fun LoggedEvent.isShortlyAfterAny(notifications: List<LoggedEvent>): Boolean =
        notifications.any { isShortlyAfter(it) }

    private fun LoggedEvent.isShortlyAfter(notification: LoggedEvent): Boolean {
        val windowEnd: Instant = notification.time + AlertMetricsConfig.OPT_OUT_WINDOW
        return !time.isBefore(notification.time) && !time.isAfter(windowEnd)
    }
}
