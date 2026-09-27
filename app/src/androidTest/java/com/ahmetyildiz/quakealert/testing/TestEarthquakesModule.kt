package com.ahmetyildiz.quakealert.testing

import com.ahmetyildiz.quakealert.features.earthquakes.data.source.UsgsApi
import com.ahmetyildiz.quakealert.features.earthquakes.di.EarthquakesModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [EarthquakesModule::class])
object TestEarthquakesModule {

    @Provides
    @Singleton
    fun provideFakeUsgsApi(): FakeUsgsApi = FakeUsgsApi()

    @Provides
    fun provideUsgsApi(fake: FakeUsgsApi): UsgsApi = fake
}
