package com.ahmetyildiz.quakealert.features.alerts.presentation.screen

import android.content.Context
import android.content.Intent
import android.provider.Settings

internal fun createNotificationSettingsIntent(context: Context): Intent =
    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
