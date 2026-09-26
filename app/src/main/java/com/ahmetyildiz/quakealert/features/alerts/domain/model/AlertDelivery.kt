package com.ahmetyildiz.quakealert.features.alerts.domain.model

data class AlertDelivery(
    val matched: Int,
    val result: AlertNotificationResult,
) {

    val notified: Int
        get() = if (result == AlertNotificationResult.POSTED) matched else 0
}
