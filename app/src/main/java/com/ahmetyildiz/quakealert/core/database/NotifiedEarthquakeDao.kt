package com.ahmetyildiz.quakealert.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NotifiedEarthquakeDao {

    @Query("SELECT id FROM notified_earthquakes WHERE id IN (:ids)")
    suspend fun findNotifiedIds(ids: List<String>): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(earthquakes: List<NotifiedEarthquakeEntity>)

    @Query("DELETE FROM notified_earthquakes WHERE notified_at_epoch_ms < :epochMs")
    suspend fun deleteNotifiedBefore(epochMs: Long)
}
