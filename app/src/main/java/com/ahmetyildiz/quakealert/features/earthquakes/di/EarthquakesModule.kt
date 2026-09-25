package com.ahmetyildiz.quakealert.features.earthquakes.di

import com.ahmetyildiz.quakealert.features.earthquakes.data.source.UsgsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EarthquakesModule {

    @Provides
    @Singleton
    fun provideUsgsApi(retrofit: Retrofit): UsgsApi = retrofit.create()
}
