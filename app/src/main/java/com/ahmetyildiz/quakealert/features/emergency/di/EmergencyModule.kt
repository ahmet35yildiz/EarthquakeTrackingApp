package com.ahmetyildiz.quakealert.features.emergency.di

import com.ahmetyildiz.quakealert.features.emergency.data.source.CameraTorchController
import com.ahmetyildiz.quakealert.features.emergency.domain.TorchController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface EmergencyModule {

    @Binds
    fun bindTorchController(controller: CameraTorchController): TorchController
}
