package com.ahmetyildiz.quakealert.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [AnalyticsEventEntity::class, EarthquakeEntity::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
@TypeConverters(StringMapConverter::class)
abstract class QuakeAlertDatabase : RoomDatabase() {

    abstract fun analyticsEventDao(): AnalyticsEventDao

    abstract fun earthquakeDao(): EarthquakeDao

    companion object {
        const val FILE_NAME: String = "quakealert.db"
    }
}
