package com.ahmetyildiz.quakealert.features.alerts.domain

interface AlertCheckScheduler {

    fun schedulePeriodicCheck()

    fun cancelPeriodicCheck()

    fun runCheckNow()
}
