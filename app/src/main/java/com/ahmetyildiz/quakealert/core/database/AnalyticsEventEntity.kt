package com.ahmetyildiz.quakealert.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analytics_events")
data class AnalyticsEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    /** Stored as a JSON object (see [StringMapConverter]). */
    val params: Map<String, String>,
    @ColumnInfo(name = "timestamp_epoch_ms")
    val timestampEpochMs: Long,
)
