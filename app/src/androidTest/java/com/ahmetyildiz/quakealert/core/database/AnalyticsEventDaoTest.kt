package com.ahmetyildiz.quakealert.core.database

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnalyticsEventDaoTest {

    private lateinit var database: QuakeAlertDatabase
    private lateinit var dao: AnalyticsEventDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, QuakeAlertDatabase::class.java).build()
        dao = database.analyticsEventDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun storedEventsAreReadBackWithParamsNewestFirst() = runBlocking {
        val opened = event(name = "app_opened", params = mapOf("source" to "launcher"), timestamp = 1_000)
        val started = event(name = "onboarding_started", params = emptyMap(), timestamp = 2_000)
        dao.insert(opened)
        dao.insert(started)
        val stored: List<AnalyticsEventEntity> = dao.observeAll().first()
        assertEquals(listOf(started, opened), stored.map { it.copy(id = 0) })
    }

    @Test
    fun eventsWithTheSameTimestampKeepInsertionOrder() = runBlocking {
        dao.insert(event(name = "first", params = emptyMap(), timestamp = 1_000))
        dao.insert(event(name = "second", params = emptyMap(), timestamp = 1_000))
        val names: List<String> = dao.observeAll().first().map { it.name }
        assertEquals(listOf("second", "first"), names)
    }

    private fun event(name: String, params: Map<String, String>, timestamp: Long) =
        AnalyticsEventEntity(name = name, params = params, timestampEpochMs = timestamp)
}
