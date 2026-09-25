package com.ahmetyildiz.quakealert.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "earthquakes")
data class EarthquakeEntity(
    @PrimaryKey
    val id: String,
    val magnitude: Double?,
    @ColumnInfo(name = "magnitude_type")
    val magnitudeType: String?,
    val place: String?,
    @ColumnInfo(name = "time_epoch_ms")
    val timeEpochMs: Long,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "depth_km")
    val depthKm: Double,
    @ColumnInfo(name = "detail_url")
    val detailUrl: String,
    @ColumnInfo(name = "is_reviewed")
    val isReviewed: Boolean,
    @ColumnInfo(name = "has_tsunami_flag")
    val hasTsunamiFlag: Boolean,
    @ColumnInfo(name = "felt_report_count")
    val feltReportCount: Int?,
)
