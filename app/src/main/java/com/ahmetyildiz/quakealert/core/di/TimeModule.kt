package com.ahmetyildiz.quakealert.core.di

import com.ahmetyildiz.quakealert.core.time.Clock
import com.ahmetyildiz.quakealert.core.time.DeviceClock
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface TimeModule {

    @Binds
    fun bindClock(clock: DeviceClock): Clock
}
