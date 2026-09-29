package com.ahmetyildiz.quakealert.features.eventlog.domain.model

import java.time.Duration

data class AlertMetrics(
    val setupCompletion: MetricRatio,
    val notificationOpens: MetricRatio,
    val answeredOpens: MetricRatio,
    val usefulAnswers: MetricRatio,
    val notificationsFollowedByOptOut: MetricRatio,
    val alertsTurnedOffAfterNotification: Int,
    val thresholdsRaisedAfterNotification: Int,
    val optOutWindow: Duration,
)
