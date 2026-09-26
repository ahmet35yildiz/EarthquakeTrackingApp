package com.ahmetyildiz.quakealert.core.database

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotifiedEarthquakeDaoTest {

    private lateinit var database: QuakeAlertDatabase
    private lateinit var dao: NotifiedEarthquakeDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, QuakeAlertDatabase::class.java).build()
        dao = database.notifiedEarthquakeDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun onlyAskedIdsThatWereNotifiedAreFound() = runBlocking {
        dao.insertAll(listOf(NotifiedEarthquakeEntity("a", 1_000), NotifiedEarthquakeEntity("b", 1_000)))
        assertEquals(listOf("a"), dao.findNotifiedIds(listOf("a", "c")))
    }

    @Test
    fun notifyingAgainKeepsOneRow() = runBlocking {
        dao.insertAll(listOf(NotifiedEarthquakeEntity("a", 1_000)))
        dao.insertAll(listOf(NotifiedEarthquakeEntity("a", 2_000)))
        assertEquals(listOf("a"), dao.findNotifiedIds(listOf("a")))
    }

    @Test
    fun rowsOlderThanTheLimitAreDeleted() = runBlocking {
        dao.insertAll(listOf(NotifiedEarthquakeEntity("old", 1_000), NotifiedEarthquakeEntity("new", 3_000)))
        dao.deleteNotifiedBefore(2_000)
        assertEquals(listOf("new"), dao.findNotifiedIds(listOf("old", "new")))
    }
}
