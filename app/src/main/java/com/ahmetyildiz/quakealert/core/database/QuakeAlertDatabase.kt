package com.ahmetyildiz.quakealert.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [AnalyticsEventEntity::class, EarthquakeEntity::class, NotifiedEarthquakeEntity::class],
    version = 3,
    autoMigrations = [AutoMigration(from = 1, to = 2), AutoMigration(from = 2, to = 3)],
)
@TypeConverters(StringMapConverter::class)
abstract class QuakeAlertDatabase : RoomDatabase() {

    abstract fun analyticsEventDao(): AnalyticsEventDao

    abstract fun earthquakeDao(): EarthquakeDao

    abstract fun notifiedEarthquakeDao(): NotifiedEarthquakeDao

    companion object {
        const val FILE_NAME: String = "quakealert.db"
    }
}
