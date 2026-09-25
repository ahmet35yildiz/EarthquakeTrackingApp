package com.ahmetyildiz.quakealert.core.database

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EarthquakeDaoTest {

    private lateinit var database: QuakeAlertDatabase
    private lateinit var dao: EarthquakeDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, QuakeAlertDatabase::class.java).build()
        dao = database.earthquakeDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun earthquakesAreObservedNewestFirst() = runBlocking {
        dao.insertAll(listOf(earthquake(id = "old", time = 1_000), earthquake(id = "new", time = 2_000)))
        val ids: List<String> = dao.observeAll().first().map { it.id }
        assertEquals(listOf("new", "old"), ids)
    }

    @Test
    fun replaceAllRemovesEarthquakesMissingFromTheNewList() = runBlocking {
        dao.insertAll(listOf(earthquake(id = "gone", time = 1_000), earthquake(id = "kept", time = 2_000)))
        val updated: EarthquakeEntity = earthquake(id = "kept", time = 2_000).copy(magnitude = 5.1)
        dao.replaceAll(listOf(updated, earthquake(id = "added", time = 3_000)))
        assertEquals(listOf("added", "kept"), dao.observeAll().first().map { it.id })
        assertEquals(updated, dao.getById("kept"))
        assertNull(dao.getById("gone"))
    }

    @Test
    fun nullableColumnsAreStoredAsNull() = runBlocking {
        val withoutOptionalFields: EarthquakeEntity = earthquake(id = "bare", time = 1_000).copy(
            magnitude = null,
            magnitudeType = null,
            place = null,
            feltReportCount = null,
        )
        dao.insertAll(listOf(withoutOptionalFields))
        assertEquals(withoutOptionalFields, dao.getById("bare"))
    }

    private fun earthquake(id: String, time: Long): EarthquakeEntity =
        EarthquakeEntity(
            id = id,
            magnitude = 4.5,
            magnitudeType = "mb",
            place = "Somewhere",
            timeEpochMs = time,
            latitude = 38.4,
            longitude = 27.1,
            depthKm = 10.0,
            detailUrl = "https://earthquake.usgs.gov/earthquakes/eventpage/$id",
            isReviewed = true,
            hasTsunamiFlag = false,
            feltReportCount = 2,
        )
}
