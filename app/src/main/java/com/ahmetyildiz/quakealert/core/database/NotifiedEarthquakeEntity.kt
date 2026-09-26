package com.ahmetyildiz.quakealert.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notified_earthquakes")
data class NotifiedEarthquakeEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "notified_at_epoch_ms")
    val notifiedAtEpochMs: Long,
)
