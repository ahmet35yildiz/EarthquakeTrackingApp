package com.ahmetyildiz.quakealert.core.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeAnalyticsEventDao : AnalyticsEventDao {

    private val events = MutableStateFlow<List<AnalyticsEventEntity>>(emptyList())

    var insertFailure: Exception? = null

    val storedEvents: List<AnalyticsEventEntity>
        get() = events.value

    override suspend fun insert(event: AnalyticsEventEntity) {
        insertFailure?.let { throw it }
        events.update { it + event.copy(id = it.size + 1L) }
    }

    override fun observeAll(): Flow<List<AnalyticsEventEntity>> = events.map { it.reversed() }
}
