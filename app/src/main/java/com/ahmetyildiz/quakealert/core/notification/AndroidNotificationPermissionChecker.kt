package com.ahmetyildiz.quakealert.core.notification

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidNotificationPermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context,
) : NotificationPermissionChecker {

    override fun areNotificationsAllowed(): Boolean = NotificationManagerCompat.from(context).areNotificationsEnabled()
}
