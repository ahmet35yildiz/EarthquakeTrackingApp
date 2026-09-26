package com.ahmetyildiz.quakealert.features.alerts.worker

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ahmetyildiz.quakealert.features.alerts.domain.SimulatedAlertScheduler
import com.ahmetyildiz.quakealert.features.alerts.domain.model.SimulationRequest
import com.ahmetyildiz.quakealert.features.alerts.worker.SimulatedAlertWorker.Companion.toInputData
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import javax.inject.Inject

class SimulatedAlertWorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : SimulatedAlertScheduler {

    override fun schedule(request: SimulationRequest, delay: Duration) {
        val workRequest: OneTimeWorkRequest = OneTimeWorkRequestBuilder<SimulatedAlertWorker>()
            .setInitialDelay(delay)
            .setInputData(request.toInputData())
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
