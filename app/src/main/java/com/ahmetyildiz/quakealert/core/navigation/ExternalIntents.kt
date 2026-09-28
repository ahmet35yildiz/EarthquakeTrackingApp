package com.ahmetyildiz.quakealert.core.navigation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri

fun browserIntent(url: String): Intent = Intent(Intent.ACTION_VIEW, url.toUri())

fun notificationSettingsIntent(context: Context): Intent =
    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)

fun appSettingsIntent(context: Context): Intent =
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri())

fun locationSettingsIntent(): Intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)

fun shareIntent(text: String, chooserTitle: String): Intent {
    val send: Intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    return Intent.createChooser(send, chooserTitle)
}

fun Context.tryStartActivity(intent: Intent): Boolean =
    try {
        startActivity(intent)
        true
    } catch (exception: ActivityNotFoundException) {
        false
    }
