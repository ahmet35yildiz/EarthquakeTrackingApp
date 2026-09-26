package com.ahmetyildiz.quakealert.features.alerts.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertCheckScheduler
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AlertWorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : AlertCheckScheduler {

    override fun schedulePeriodicCheck() {
        val request: PeriodicWorkRequest = PeriodicWorkRequestBuilder<AlertCheckWorker>(AlertConfig.CHECK_INTERVAL)
            .setConstraints(networkConstraints())
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(PERIODIC_CHECK_WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    override fun cancelPeriodicCheck() {
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_CHECK_WORK_NAME)
    }

    override fun runCheckNow() {
        val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<AlertCheckWorker>()
            .setConstraints(networkConstraints())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(MANUAL_CHECK_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    private fun networkConstraints(): Constraints =
        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    companion object {
        const val PERIODIC_CHECK_WORK_NAME: String = "periodic_alert_check"
        const val MANUAL_CHECK_WORK_NAME: String = "manual_alert_check"
    }
}
