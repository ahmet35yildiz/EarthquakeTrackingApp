package com.ahmetyildiz.quakealert.features.alerts.domain

class FakeAlertCheckScheduler : AlertCheckScheduler {

    var isScheduled: Boolean = false
    var scheduleCount: Int = 0
    var runNowCount: Int = 0

    override fun schedulePeriodicCheck() {
        isScheduled = true
        scheduleCount++
    }

    override fun cancelPeriodicCheck() {
        isScheduled = false
    }

    override fun runCheckNow() {
        runNowCount++
    }
}
