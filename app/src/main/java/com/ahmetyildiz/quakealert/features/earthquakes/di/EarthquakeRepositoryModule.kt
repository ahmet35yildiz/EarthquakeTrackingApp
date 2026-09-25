package com.ahmetyildiz.quakealert.features.earthquakes.di

import com.ahmetyildiz.quakealert.features.earthquakes.data.repository.EarthquakeRepositoryImpl
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.EarthquakeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface EarthquakeRepositoryModule {

    @Binds
    @Singleton
    fun bindEarthquakeRepository(repository: EarthquakeRepositoryImpl): EarthquakeRepository
}
