package com.ahmetyildiz.quakealert.features.alerts.domain

import com.ahmetyildiz.quakealert.features.alerts.domain.model.SimulationRequest
import java.time.Duration

class FakeSimulatedAlertScheduler : SimulatedAlertScheduler {

    val scheduled: MutableList<Pair<SimulationRequest, Duration>> = mutableListOf()

    override fun schedule(request: SimulationRequest, delay: Duration) {
        scheduled += request to delay
    }
}
