package com.ahmetyildiz.quakealert.features.alerts.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertCheckResult
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.CheckForNewAlertsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AlertCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parameters: WorkerParameters,
    private val checkForNewAlerts: CheckForNewAlertsUseCase,
) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result =
        when (val result: AlertCheckResult = checkForNewAlerts()) {
            is AlertCheckResult.Failed -> if (result.shouldRetry) Result.retry() else Result.failure()
            AlertCheckResult.Skipped, is AlertCheckResult.Completed -> Result.success()
        }
}
