package com.ahmetyildiz.quakealert.core.analytics

import com.ahmetyildiz.quakealert.core.notification.AlertNotificationTap
import com.ahmetyildiz.quakealert.core.time.Clock
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppOpenTracker @Inject constructor(
    private val analyticsTracker: AnalyticsTracker,
    private val clock: Clock,
) {

    private var hasOpenedInThisProcess: Boolean = false
    private var isRestoredOpenPending: Boolean = false

    fun onActivityCreated(isRestored: Boolean, notificationTap: AlertNotificationTap?) {
        if (!isRestored) {
            trackAppOpened(notificationTap)
            return
        }
        isRestoredOpenPending = !hasOpenedInThisProcess
    }

    fun onNewIntent(notificationTap: AlertNotificationTap?) {
        if (isRestoredOpenPending) {
            trackAppOpened(notificationTap)
            return
        }
        notificationTap?.let(::trackNotificationOpened)
    }

    fun onActivityResumed() {
        if (isRestoredOpenPending) trackAppOpened(notificationTap = null)
    }

    private fun trackAppOpened(notificationTap: AlertNotificationTap?) {
        hasOpenedInThisProcess = true
        isRestoredOpenPending = false
        val source: AppOpenSource = if (notificationTap == null) AppOpenSource.LAUNCHER else AppOpenSource.NOTIFICATION
        analyticsTracker.track(AnalyticsEvent.AppOpened(source = source))
        notificationTap?.let(::trackNotificationOpened)
    }

    private fun trackNotificationOpened(tap: AlertNotificationTap) {
        val delaySeconds: Long = Duration.between(tap.postedAt, clock.now()).seconds.coerceAtLeast(0)
        analyticsTracker.track(AnalyticsEvent.AlertNotificationOpened(eventId = tap.eventId, delaySeconds = delaySeconds))
    }
}
