package com.ahmetyildiz.quakealert.features.alerts.di

import com.ahmetyildiz.quakealert.features.alerts.domain.AlertNotifier
import com.ahmetyildiz.quakealert.features.alerts.notification.EarthquakeAlertNotifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface AlertNotificationModule {

    @Binds
    fun bindAlertNotifier(notifier: EarthquakeAlertNotifier): AlertNotifier
}
