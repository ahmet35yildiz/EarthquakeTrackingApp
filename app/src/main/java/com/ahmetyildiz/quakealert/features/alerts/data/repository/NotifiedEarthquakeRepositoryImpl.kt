package com.ahmetyildiz.quakealert.features.alerts.data.repository

import com.ahmetyildiz.quakealert.core.database.NotifiedEarthquakeDao
import com.ahmetyildiz.quakealert.core.database.NotifiedEarthquakeEntity
import com.ahmetyildiz.quakealert.features.alerts.domain.repository.NotifiedEarthquakeRepository
import java.time.Instant
import javax.inject.Inject

class NotifiedEarthquakeRepositoryImpl @Inject constructor(
    private val notifiedEarthquakeDao: NotifiedEarthquakeDao,
) : NotifiedEarthquakeRepository {

    override suspend fun findNotifiedIds(ids: Collection<String>): Set<String> {
        if (ids.isEmpty()) return emptySet()
        return notifiedEarthquakeDao.findNotifiedIds(ids.toList()).toSet()
    }

    override suspend fun markNotified(ids: Collection<String>, notifiedAt: Instant) {
        val entities: List<NotifiedEarthquakeEntity> = ids.map { NotifiedEarthquakeEntity(it, notifiedAt.toEpochMilli()) }
        notifiedEarthquakeDao.insertAll(entities)
    }

    override suspend fun deleteNotifiedBefore(time: Instant) {
        notifiedEarthquakeDao.deleteNotifiedBefore(time.toEpochMilli())
    }
}
