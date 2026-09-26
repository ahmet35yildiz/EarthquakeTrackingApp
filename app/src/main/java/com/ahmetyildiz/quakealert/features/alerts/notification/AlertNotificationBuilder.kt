package com.ahmetyildiz.quakealert.features.alerts.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.navigation.DeepLinkConfig
import com.ahmetyildiz.quakealert.core.notification.AlertNotificationTap
import com.ahmetyildiz.quakealert.core.notification.NotificationChannels
import com.ahmetyildiz.quakealert.core.notification.putAlertNotificationTap
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertConfig
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertNotifier
import com.ahmetyildiz.quakealert.features.alerts.domain.model.EarthquakeAlert
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.DistanceFromCity
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.roundToLong

class AlertNotificationBuilder(
    private val context: Context,
    private val postedAt: Instant,
) {

    private val locale: Locale = context.resources.configuration.locales[0]

    fun buildAlert(alert: EarthquakeAlert): Notification {
        val earthquake: Earthquake = alert.earthquake
        val text: String = context.getString(R.string.notification_alert_text, placeText(earthquake), timeText(earthquake))
        return baseBuilder()
            .setContentTitle(alertTitle(alert))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setWhen(earthquake.time.toEpochMilli())
            .setShowWhen(true)
            .setContentIntent(detailPendingIntent(earthquake.id))
            .build()
    }

    fun buildSummary(alerts: List<EarthquakeAlert>, magnitudeThreshold: Double): Notification {
        val strongestFirst: List<Earthquake> = alerts.map(EarthquakeAlert::earthquake).sortedByDescending { it.magnitude?.value }
        val title: String = context.resources.getQuantityString(
            R.plurals.notification_summary_title,
            alerts.size,
            alerts.size,
            magnitudeText(magnitudeThreshold),
        )
        val style = NotificationCompat.InboxStyle().setBigContentTitle(title)
        strongestFirst.take(AlertConfig.SUMMARY_NOTIFICATION_MAX_LINES).forEach { style.addLine(summaryLine(it)) }
        return baseBuilder()
            .setContentTitle(title)
            .setContentText(summaryLine(strongestFirst.first()))
            .setStyle(style)
            .setContentIntent(appPendingIntent())
            .build()
    }

    private fun baseBuilder(): NotificationCompat.Builder =
        NotificationCompat.Builder(context, NotificationChannels.EARTHQUAKE_ALERTS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_waves)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)

    private fun alertTitle(alert: EarthquakeAlert): String {
        val magnitude: String = magnitudeText(alert.earthquake.magnitude?.value)
        val distance: DistanceFromCity = alert.distanceFromCity
            ?: return context.getString(R.string.notification_alert_title, magnitude)
        return context.getString(
            R.string.notification_alert_title_with_distance,
            magnitude,
            NumberFormat.getIntegerInstance(locale).format(distance.distanceKm.roundToLong()),
            distance.cityName,
        )
    }

    private fun summaryLine(earthquake: Earthquake): String =
        context.getString(R.string.notification_summary_line, magnitudeText(earthquake.magnitude?.value), placeText(earthquake))

    private fun magnitudeText(magnitude: Double?): String =
        magnitude?.let { context.getString(R.string.magnitude_value, it) }
            ?: context.getString(R.string.magnitude_unknown_value)

    private fun placeText(earthquake: Earthquake): String =
        earthquake.place ?: context.getString(R.string.earthquake_unknown_place)

    private fun timeText(earthquake: Earthquake): String =
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(locale)
            .format(earthquake.time.atZone(ZoneId.systemDefault()))

    private fun detailPendingIntent(earthquakeId: String): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW, DeepLinkConfig.createEarthquakeDetailUri(earthquakeId, true))
            .setPackage(context.packageName)
            .putAlertNotificationTap(AlertNotificationTap(eventId = earthquakeId, postedAt = postedAt))
        return createPendingIntent(requestCode = earthquakeId.hashCode(), intent = intent)
    }

    private fun appPendingIntent(): PendingIntent {
        val intent: Intent = (context.packageManager.getLaunchIntentForPackage(context.packageName) ?: Intent())
            .putAlertNotificationTap(AlertNotificationTap(eventId = AlertNotifier.SUMMARY_EVENT_ID, postedAt = postedAt))
        return createPendingIntent(requestCode = SUMMARY_REQUEST_CODE, intent = intent)
    }

    private fun createPendingIntent(requestCode: Int, intent: Intent): PendingIntent =
        PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    private companion object {
        const val SUMMARY_REQUEST_CODE: Int = 0
    }
}
