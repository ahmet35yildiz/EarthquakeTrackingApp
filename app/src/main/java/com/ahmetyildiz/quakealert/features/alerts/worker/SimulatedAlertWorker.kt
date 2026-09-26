package com.ahmetyildiz.quakealert.features.alerts.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ahmetyildiz.quakealert.features.alerts.domain.model.SimulationRequest
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.SimulateAlertUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SimulatedAlertWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parameters: WorkerParameters,
    private val simulateAlert: SimulateAlertUseCase,
) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        val request: SimulationRequest = inputData.toSimulationRequest() ?: return Result.failure()
        simulateAlert(request, isScheduled = true)
        return Result.success()
    }

    companion object {
        private const val KEY_PLACE: String = "place"
        private const val KEY_MAGNITUDE: String = "magnitude"
        private const val KEY_DISTANCE_KM: String = "distance_km"

        fun SimulationRequest.toInputData(): Data =
            workDataOf(KEY_PLACE to place, KEY_MAGNITUDE to magnitude, KEY_DISTANCE_KM to distanceFromCityKm)

        private fun Data.toSimulationRequest(): SimulationRequest? {
            val place: String = getString(KEY_PLACE) ?: return null
            val magnitude: Double = keyValueMap[KEY_MAGNITUDE] as? Double ?: return null
            return SimulationRequest(place, magnitude, getDouble(KEY_DISTANCE_KM, 0.0))
        }
    }
}
