package com.ahmetyildiz.quakealert.features.alerts.di

import com.ahmetyildiz.quakealert.features.alerts.data.repository.CitySearchRepositoryImpl
import com.ahmetyildiz.quakealert.features.alerts.data.source.AndroidCityGeocoder
import com.ahmetyildiz.quakealert.features.alerts.data.source.CityGeocoder
import com.ahmetyildiz.quakealert.features.alerts.domain.repository.CitySearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface CitySearchModule {

    @Binds
    fun bindCityGeocoder(geocoder: AndroidCityGeocoder): CityGeocoder

    @Binds
    fun bindCitySearchRepository(repository: CitySearchRepositoryImpl): CitySearchRepository
}
