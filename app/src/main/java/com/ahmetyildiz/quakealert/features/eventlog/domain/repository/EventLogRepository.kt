package com.ahmetyildiz.quakealert.features.eventlog.domain.repository

import com.ahmetyildiz.quakealert.features.eventlog.domain.model.LoggedEvent
import kotlinx.coroutines.flow.Flow

interface EventLogRepository {

    fun observeEvents(): Flow<List<LoggedEvent>>

    suspend fun clear()
}
