package com.ahmetyildiz.quakealert.features.alerts.worker

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.ahmetyildiz.quakealert.core.database.AnalyticsEventDao
import com.ahmetyildiz.quakealert.core.database.AnalyticsEventEntity
import com.ahmetyildiz.quakealert.core.di.NotificationModule
import com.ahmetyildiz.quakealert.core.notification.NotificationPermissionChecker
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.preferences.UserPreferencesRepository
import com.ahmetyildiz.quakealert.features.alerts.di.AlertNotificationModule
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertNotifier
import com.ahmetyildiz.quakealert.testing.FakeAlertNotifier
import com.ahmetyildiz.quakealert.testing.FakeUsgsApi
import com.ahmetyildiz.quakealert.testing.UsgsTestData
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(AlertNotificationModule::class, NotificationModule::class)
@RunWith(AndroidJUnit4::class)
class AlertCheckWorkerTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private val fakeNotifier = FakeAlertNotifier()

    @BindValue
    @JvmField
    val notifier: AlertNotifier = fakeNotifier

    @BindValue
    @JvmField
    val permissionChecker: NotificationPermissionChecker = NotificationPermissionChecker { true }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var usgsApi: FakeUsgsApi

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var analyticsEventDao: AnalyticsEventDao

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        hiltRule.inject()
        val configuration = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, configuration)
    }

    @Test
    fun checkIsSkippedUntilAlertSettingsWereSaved() = runBlocking {
        val result: ListenableWorker.Result = runWorker()
        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(0, usgsApi.listRequestCount)
        assertTrue(fakeNotifier.shownAlerts.isEmpty())
    }

    @Test
    fun newMatchingEarthquakeIsNotifiedOnlyOnce() = runBlocking {
        saveAlertSettings(baselineAt = Instant.now() - Duration.ofMinutes(1))
        usgsApi.features = listOf(UsgsTestData.earthquake(EVENT_ID, magnitude = 5.2, place = PLACE, time = Instant.now()))
        assertEquals(ListenableWorker.Result.success(), runWorker())
        assertEquals(ListenableWorker.Result.success(), runWorker())
        assertEquals(listOf(EVENT_ID), fakeNotifier.shownAlerts.map { it.earthquake.id })
        val completed: List<AnalyticsEventEntity> = awaitEvents(BACKGROUND_CHECK_COMPLETED, count = 2)
        assertEquals(setOf("1", "0"), completed.map { it.params.getValue("notified") }.toSet())
    }

    @Test
    fun earthquakeOlderThanTheBaselineIsNotNotified() = runBlocking {
        val baselineAt: Instant = Instant.now()
        saveAlertSettings(baselineAt = baselineAt)
        usgsApi.features = listOf(
            UsgsTestData.earthquake(EVENT_ID, magnitude = 5.2, place = PLACE, time = baselineAt - Duration.ofMinutes(5)),
        )
        assertEquals(ListenableWorker.Result.success(), runWorker())
        assertTrue(fakeNotifier.shownAlerts.isEmpty())
    }

    @Test
    fun networkErrorAsksWorkManagerToRetry() = runBlocking {
        saveAlertSettings(baselineAt = Instant.now())
        usgsApi.failure = IOException("offline")
        assertEquals(ListenableWorker.Result.retry(), runWorker())
        val failed: AnalyticsEventEntity = awaitEvents(BACKGROUND_CHECK_FAILED, count = 1).single()
        assertEquals("network", failed.params["reason"])
    }

    @Test
    fun unreadableResponseFailsWithoutRetry() = runBlocking {
        saveAlertSettings(baselineAt = Instant.now())
        usgsApi.failure = SerializationException("unexpected format")
        assertEquals(ListenableWorker.Result.failure(), runWorker())
    }

    private suspend fun saveAlertSettings(baselineAt: Instant) {
        preferencesRepository.saveAlertSettings(AlertSettings.DEFAULT, baselineAt)
    }

    private suspend fun runWorker(): ListenableWorker.Result =
        TestListenableWorkerBuilder<AlertCheckWorker>(context)
            .setWorkerFactory(workerFactory)
            .build()
            .doWork()

    private suspend fun awaitEvents(name: String, count: Int): List<AnalyticsEventEntity> =
        withTimeout(EVENT_TIMEOUT_MILLIS) {
            analyticsEventDao.observeAll()
                .first { events -> events.count { it.name == name } >= count }
                .filter { it.name == name }
        }

    private companion object {
        const val EVENT_ID: String = "test0001"
        const val PLACE: String = "12 km SW of Seferihisar, Turkey"
        const val BACKGROUND_CHECK_COMPLETED: String = "background_check_completed"
        const val BACKGROUND_CHECK_FAILED: String = "background_check_failed"
        const val EVENT_TIMEOUT_MILLIS: Long = 5_000
    }
}
