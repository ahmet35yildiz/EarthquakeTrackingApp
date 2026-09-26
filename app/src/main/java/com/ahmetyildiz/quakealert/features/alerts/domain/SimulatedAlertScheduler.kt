package com.ahmetyildiz.quakealert.features.alerts.domain

import com.ahmetyildiz.quakealert.features.alerts.domain.model.SimulationRequest
import java.time.Duration

interface SimulatedAlertScheduler {

    fun schedule(request: SimulationRequest, delay: Duration)
}
