package com.ahmetyildiz.quakealert.core.notification

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidNotificationAccessChecker @Inject constructor(
    @ApplicationContext private val context: Context,
) : NotificationAccessChecker {

    override fun getAlertNotificationAccess(): NotificationAccess {
        val manager: NotificationManagerCompat = NotificationManagerCompat.from(context)
        return when {
            !manager.areNotificationsEnabled() -> NotificationAccess.APP_BLOCKED
            isAlertChannelBlocked(manager) -> NotificationAccess.ALERT_CHANNEL_BLOCKED
            else -> NotificationAccess.ALLOWED
        }
    }

    private fun isAlertChannelBlocked(manager: NotificationManagerCompat): Boolean {
        val channelId: String = NotificationChannels.EARTHQUAKE_ALERTS_CHANNEL_ID
        return manager.getNotificationChannelCompat(channelId)?.importance == NotificationManagerCompat.IMPORTANCE_NONE
    }
}
