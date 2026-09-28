package com.ahmetyildiz.quakealert.features.alerts.di

import com.ahmetyildiz.quakealert.features.alerts.data.repository.DeviceLocationRepositoryImpl
import com.ahmetyildiz.quakealert.features.alerts.data.source.AndroidDeviceLocationSource
import com.ahmetyildiz.quakealert.features.alerts.data.source.DeviceLocationSource
import com.ahmetyildiz.quakealert.features.alerts.domain.repository.DeviceLocationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DeviceLocationModule {

    @Binds
    fun bindDeviceLocationSource(source: AndroidDeviceLocationSource): DeviceLocationSource

    @Binds
    fun bindDeviceLocationRepository(repository: DeviceLocationRepositoryImpl): DeviceLocationRepository
}
