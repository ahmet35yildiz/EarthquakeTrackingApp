package com.ahmetyildiz.quakealert.features.alerts.notification

import android.Manifest
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ahmetyildiz.quakealert.core.locale.LocalizedContextProvider
import com.ahmetyildiz.quakealert.core.time.Clock
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertNotifier
import com.ahmetyildiz.quakealert.features.alerts.domain.model.EarthquakeAlert
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class EarthquakeAlertNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localizedContextProvider: LocalizedContextProvider,
    private val clock: Clock,
) : AlertNotifier {

    override fun showAlerts(alerts: List<EarthquakeAlert>) {
        val builder: AlertNotificationBuilder = createBuilder()
        alerts.forEach { post(id = it.earthquake.id.hashCode(), notification = builder.buildAlert(it)) }
    }

    override fun showSummary(alerts: List<EarthquakeAlert>, magnitudeThreshold: Double) {
        if (alerts.isEmpty()) return
        post(id = SUMMARY_NOTIFICATION_ID, notification = createBuilder().buildSummary(alerts, magnitudeThreshold))
    }

    private fun createBuilder(): AlertNotificationBuilder =
        AlertNotificationBuilder(localizedContextProvider.createLocalizedContext(), clock.now())

    private fun post(id: Int, notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    private companion object {
        const val SUMMARY_NOTIFICATION_ID: Int = 1
    }
}
