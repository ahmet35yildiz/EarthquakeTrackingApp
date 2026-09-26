package com.ahmetyildiz.quakealert.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.locale.LocalizedContextProvider
import javax.inject.Inject

class NotificationChannels @Inject constructor(
    private val localizedContextProvider: LocalizedContextProvider,
) {

    fun register() {
        val context: Context = localizedContextProvider.createLocalizedContext()
        val channel = NotificationChannel(
            EARTHQUAKE_ALERTS_CHANNEL_ID,
            context.getString(R.string.notification_channel_alerts_name),
            NotificationManager.IMPORTANCE_HIGH,
        )
        channel.description = context.getString(R.string.notification_channel_alerts_description)
        context.getSystemService<NotificationManager>()?.createNotificationChannel(channel)
    }

    companion object {
        const val EARTHQUAKE_ALERTS_CHANNEL_ID: String = "earthquake_alerts"
    }
}
