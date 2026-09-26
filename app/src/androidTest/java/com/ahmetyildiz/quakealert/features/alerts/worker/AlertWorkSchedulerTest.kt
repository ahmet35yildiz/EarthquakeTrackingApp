package com.ahmetyildiz.quakealert.features.alerts.worker

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertConfig
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertWorkSchedulerTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val scheduler = AlertWorkScheduler(context)

    @Before
    fun setUp() {
        val configuration = Configuration.Builder().setExecutor(SynchronousExecutor()).build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, configuration)
    }

    @Test
    fun periodicCheckRunsEveryIntervalWithNetwork() {
        scheduler.schedulePeriodicCheck()
        val work: WorkInfo = periodicWork().single()
        assertEquals(WorkInfo.State.ENQUEUED, work.state)
        assertEquals(NetworkType.CONNECTED, work.constraints.requiredNetworkType)
        assertEquals(AlertConfig.CHECK_INTERVAL.toMillis(), work.periodicityInfo?.repeatIntervalMillis)
    }

    @Test
    fun schedulingTwiceKeepsOneWork() {
        scheduler.schedulePeriodicCheck()
        scheduler.schedulePeriodicCheck()
        assertEquals(1, periodicWork().count { !it.state.isFinished })
    }

    @Test
    fun cancellingStopsTheWork() {
        scheduler.schedulePeriodicCheck()
        scheduler.cancelPeriodicCheck()
        assertEquals(WorkInfo.State.CANCELLED, periodicWork().single().state)
    }

    private fun periodicWork(): List<WorkInfo> =
        WorkManager.getInstance(context).getWorkInfosForUniqueWork(AlertWorkScheduler.PERIODIC_CHECK_WORK_NAME).get()
}
