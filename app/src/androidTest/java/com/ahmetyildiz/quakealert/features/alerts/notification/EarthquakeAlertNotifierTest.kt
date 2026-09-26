package com.ahmetyildiz.quakealert.features.alerts.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.locale.AppCompatLanguageManager
import com.ahmetyildiz.quakealert.core.locale.LocalizedContextProvider
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.notification.NotificationChannels
import com.ahmetyildiz.quakealert.features.alerts.domain.model.EarthquakeAlert
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.DistanceFromCity
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Magnitude
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class EarthquakeAlertNotifierTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val notificationManager: NotificationManager = context.getSystemService(NotificationManager::class.java)
    private val localizedContextProvider = LocalizedContextProvider(context, AppCompatLanguageManager(context))
    private val notifier = EarthquakeAlertNotifier(context, localizedContextProvider) { Instant.parse(POSTED_AT) }
    private val appContext: Context = localizedContextProvider.createLocalizedContext()

    @Before
    fun setUp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InstrumentationRegistry.getInstrumentation().uiAutomation
                .grantRuntimePermission(context.packageName, Manifest.permission.POST_NOTIFICATIONS)
        }
        NotificationChannels(localizedContextProvider).register()
        notificationManager.cancelAll()
    }

    @After
    fun tearDown() {
        notificationManager.cancelAll()
    }

    @Test
    fun alertsAreShownOneByOneWithTheDistanceInTheTitle() {
        notifier.showAlerts(listOf(alert("a", 5.0), alert("b", 5.5), alert("c", 6.0)))
        val titles: Set<String> = waitForNotifications(count = 3).map(::titleOf).toSet()
        val expected: Set<String> = setOf(5.0, 5.5, 6.0).map { distanceTitle(it) }.toSet()
        assertEquals(expected, titles)
    }

    @Test
    fun alertWithoutAreaHasTheShortTitle() {
        notifier.showAlerts(listOf(alert("a", 5.0, hasDistance = false)))
        val expected: String = appContext.getString(R.string.notification_alert_title, magnitude(5.0))
        assertEquals(expected, titleOf(waitForNotifications(count = 1).single()))
    }

    @Test
    fun summaryCountsTheAlertsAboveTheThreshold() {
        notifier.showSummary((1..5).map { alert("id$it", 5.0) }, magnitudeThreshold = 4.5)
        val expected: String = appContext.resources.getQuantityString(R.plurals.notification_summary_title, 5, 5, magnitude(4.5))
        assertEquals(expected, titleOf(waitForNotifications(count = 1).single()))
    }

    @Test
    fun theSameEarthquakeReplacesItsNotification() {
        notifier.showAlerts(listOf(alert("a", 5.0)))
        notifier.showAlerts(listOf(alert("a", 5.0)))
        assertEquals(1, waitForNotifications(count = 1).size)
    }

    @Test
    fun notificationsUseTheAlertsChannel() {
        notifier.showAlerts(listOf(alert("a", 5.0)))
        val notification: Notification = waitForNotifications(count = 1).single()
        assertEquals(NotificationChannels.EARTHQUAKE_ALERTS_CHANNEL_ID, notification.channelId)
    }

    private fun waitForNotifications(count: Int): List<Notification> {
        val deadline: Long = SystemClock.uptimeMillis() + WAIT_TIMEOUT_MILLIS
        while (notificationManager.activeNotifications.size < count && SystemClock.uptimeMillis() < deadline) {
            SystemClock.sleep(POLL_INTERVAL_MILLIS)
        }
        return notificationManager.activeNotifications.map { it.notification }
    }

    private fun titleOf(notification: Notification): String =
        notification.extras.getCharSequence(Notification.EXTRA_TITLE).toString()

    private fun magnitude(value: Double): String = appContext.getString(R.string.magnitude_value, value)

    private fun distanceTitle(value: Double): String =
        appContext.getString(R.string.notification_alert_title_with_distance, magnitude(value), "100", "Center")

    private fun alert(id: String, magnitude: Double, hasDistance: Boolean = true): EarthquakeAlert =
        EarthquakeAlert(
            earthquake = Earthquake(
                id = id,
                magnitude = Magnitude(value = magnitude, type = "mb"),
                place = "Place $id",
                time = Instant.parse(POSTED_AT),
                location = GeoPoint(0.0, 0.9),
                depthKm = 10.0,
                detailUrl = "",
                isReviewed = false,
                hasTsunamiFlag = false,
                feltReportCount = null,
            ),
            distanceFromCity = if (hasDistance) DistanceFromCity("Center", 100.2, 250, true) else null,
        )

    private companion object {
        const val POSTED_AT: String = "2026-09-26T12:00:00Z"
        const val WAIT_TIMEOUT_MILLIS: Long = 2_000
        const val POLL_INTERVAL_MILLIS: Long = 50
    }
}
