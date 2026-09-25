package com.ahmetyildiz.quakealert.core.di

import android.content.Context
import androidx.room.Room
import com.ahmetyildiz.quakealert.core.database.AnalyticsEventDao
import com.ahmetyildiz.quakealert.core.database.EarthquakeDao
import com.ahmetyildiz.quakealert.core.database.QuakeAlertDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): QuakeAlertDatabase =
        Room.databaseBuilder(context, QuakeAlertDatabase::class.java, QuakeAlertDatabase.FILE_NAME).build()

    @Provides
    fun provideAnalyticsEventDao(database: QuakeAlertDatabase): AnalyticsEventDao = database.analyticsEventDao()

    @Provides
    fun provideEarthquakeDao(database: QuakeAlertDatabase): EarthquakeDao = database.earthquakeDao()
}
