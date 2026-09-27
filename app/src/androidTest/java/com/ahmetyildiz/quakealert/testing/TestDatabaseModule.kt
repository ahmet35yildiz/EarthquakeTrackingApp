package com.ahmetyildiz.quakealert.testing

import android.content.Context
import androidx.room.Room
import com.ahmetyildiz.quakealert.core.database.AnalyticsEventDao
import com.ahmetyildiz.quakealert.core.database.EarthquakeDao
import com.ahmetyildiz.quakealert.core.database.NotifiedEarthquakeDao
import com.ahmetyildiz.quakealert.core.database.QuakeAlertDatabase
import com.ahmetyildiz.quakealert.core.di.DatabaseModule
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DatabaseModule::class])
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): QuakeAlertDatabase =
        Room.inMemoryDatabaseBuilder(context, QuakeAlertDatabase::class.java).build()

    @Provides
    fun provideAnalyticsEventDao(database: QuakeAlertDatabase): AnalyticsEventDao = database.analyticsEventDao()

    @Provides
    fun provideEarthquakeDao(database: QuakeAlertDatabase): EarthquakeDao = database.earthquakeDao()

    @Provides
    fun provideNotifiedEarthquakeDao(database: QuakeAlertDatabase): NotifiedEarthquakeDao =
        database.notifiedEarthquakeDao()
}
