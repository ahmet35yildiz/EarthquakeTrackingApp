package com.ahmetyildiz.quakealert.core.notification

import android.content.Intent
import java.time.Instant

data class AlertNotificationTap(
    val eventId: String,
    val postedAt: Instant,
)

private const val EXTRA_EVENT_ID: String = "com.ahmetyildiz.quakealert.extra.ALERT_EVENT_ID"
private const val EXTRA_POSTED_AT_MILLIS: String = "com.ahmetyildiz.quakealert.extra.ALERT_POSTED_AT_MILLIS"

fun Intent.putAlertNotificationTap(tap: AlertNotificationTap): Intent =
    putExtra(EXTRA_EVENT_ID, tap.eventId).putExtra(EXTRA_POSTED_AT_MILLIS, tap.postedAt.toEpochMilli())

fun Intent.getAlertNotificationTapOrNull(): AlertNotificationTap? {
    val eventId: String = getStringExtra(EXTRA_EVENT_ID) ?: return null
    if (!hasExtra(EXTRA_POSTED_AT_MILLIS)) return null
    return AlertNotificationTap(eventId = eventId, postedAt = Instant.ofEpochMilli(getLongExtra(EXTRA_POSTED_AT_MILLIS, 0)))
}
