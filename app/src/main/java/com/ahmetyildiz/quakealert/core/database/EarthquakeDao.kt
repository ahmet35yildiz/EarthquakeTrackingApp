package com.ahmetyildiz.quakealert.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EarthquakeDao {

    @Query("SELECT * FROM earthquakes ORDER BY time_epoch_ms DESC")
    fun observeAll(): Flow<List<EarthquakeEntity>>

    @Query("SELECT * FROM earthquakes WHERE id = :id")
    suspend fun getById(id: String): EarthquakeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(earthquakes: List<EarthquakeEntity>)

    @Update
    suspend fun updateExisting(earthquakes: List<EarthquakeEntity>)

    @Query("DELETE FROM earthquakes")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(earthquakes: List<EarthquakeEntity>) {
        deleteAll()
        insertAll(earthquakes)
    }
}
