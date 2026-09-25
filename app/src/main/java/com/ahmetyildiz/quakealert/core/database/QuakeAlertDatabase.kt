package com.ahmetyildiz.quakealert.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * The app's single Room database. Schemas are exported to `app/schemas`; a schema change bumps [version] and adds
 * a migration (an `@AutoMigration` where Room can derive it), so existing installs keep their data.
 */
@Database(
    entities = [AnalyticsEventEntity::class],
    version = 1,
)
@TypeConverters(StringMapConverter::class)
abstract class QuakeAlertDatabase : RoomDatabase() {

    abstract fun analyticsEventDao(): AnalyticsEventDao

    companion object {
        const val FILE_NAME: String = "quakealert.db"
    }
}
