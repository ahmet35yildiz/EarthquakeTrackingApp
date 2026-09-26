package com.ahmetyildiz.quakealert.features.alerts.di

import com.ahmetyildiz.quakealert.features.alerts.data.repository.NotifiedEarthquakeRepositoryImpl
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertCheckScheduler
import com.ahmetyildiz.quakealert.features.alerts.domain.repository.NotifiedEarthquakeRepository
import com.ahmetyildiz.quakealert.features.alerts.worker.AlertWorkScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface AlertWorkModule {

    @Binds
    fun bindAlertCheckScheduler(scheduler: AlertWorkScheduler): AlertCheckScheduler

    @Binds
    fun bindNotifiedEarthquakeRepository(repository: NotifiedEarthquakeRepositoryImpl): NotifiedEarthquakeRepository
}
