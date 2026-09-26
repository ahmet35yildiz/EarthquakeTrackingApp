package com.ahmetyildiz.quakealert.features.alerts.domain.repository

import java.time.Instant

class FakeNotifiedEarthquakeRepository : NotifiedEarthquakeRepository {

    val notifiedAt: MutableMap<String, Instant> = mutableMapOf()

    override suspend fun findNotifiedIds(ids: Collection<String>): Set<String> = ids.filter(notifiedAt::containsKey).toSet()

    override suspend fun markNotified(ids: Collection<String>, notifiedAt: Instant) {
        ids.forEach { this.notifiedAt[it] = notifiedAt }
    }

    override suspend fun deleteNotifiedBefore(time: Instant) {
        notifiedAt.entries.removeIf { it.value.isBefore(time) }
    }
}
