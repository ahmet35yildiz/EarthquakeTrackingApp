package com.ahmetyildiz.quakealert.features.alerts.domain.repository

import java.time.Instant

interface NotifiedEarthquakeRepository {

    suspend fun findNotifiedIds(ids: Collection<String>): Set<String>

    suspend fun markNotified(ids: Collection<String>, notifiedAt: Instant)

    suspend fun deleteNotifiedBefore(time: Instant)
}
