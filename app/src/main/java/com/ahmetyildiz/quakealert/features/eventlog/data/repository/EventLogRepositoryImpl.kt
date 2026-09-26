package com.ahmetyildiz.quakealert.features.eventlog.data.repository

import com.ahmetyildiz.quakealert.core.database.AnalyticsEventDao
import com.ahmetyildiz.quakealert.core.database.AnalyticsEventEntity
import com.ahmetyildiz.quakealert.features.eventlog.domain.model.LoggedEvent
import com.ahmetyildiz.quakealert.features.eventlog.domain.repository.EventLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class EventLogRepositoryImpl @Inject constructor(
    private val dao: AnalyticsEventDao,
) : EventLogRepository {

    override fun observeEvents(): Flow<List<LoggedEvent>> =
        dao.observeAll().map { entities -> entities.map { it.toLoggedEvent() } }

    override suspend fun clear() {
        dao.deleteAll()
    }
}

private fun AnalyticsEventEntity.toLoggedEvent(): LoggedEvent =
    LoggedEvent(id = id, name = name, params = params, time = Instant.ofEpochMilli(timestampEpochMs))
