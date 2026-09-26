package com.ahmetyildiz.quakealert.features.alerts.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
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
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(PERIODIC_CHECK_WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    override fun cancelPeriodicCheck() {
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_CHECK_WORK_NAME)
    }

    companion object {
        const val PERIODIC_CHECK_WORK_NAME: String = "periodic_alert_check"
    }
}
