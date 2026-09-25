package com.ahmetyildiz.quakealert.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsEventDao {

    @Insert
    suspend fun insert(event: AnalyticsEventEntity)

    @Query("SELECT * FROM analytics_events ORDER BY timestamp_epoch_ms DESC, id DESC")
    fun observeAll(): Flow<List<AnalyticsEventEntity>>
}
