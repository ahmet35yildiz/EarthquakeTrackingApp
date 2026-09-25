package com.ahmetyildiz.quakealert.core.analytics

import android.util.Log
import com.ahmetyildiz.quakealert.core.database.AnalyticsEventDao
import com.ahmetyildiz.quakealert.core.database.AnalyticsEventEntity
import com.ahmetyildiz.quakealert.core.di.ApplicationScope
import com.ahmetyildiz.quakealert.core.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class LocalAnalyticsTracker @Inject constructor(
    private val dao: AnalyticsEventDao,
    private val clock: Clock,
    @ApplicationScope private val scope: CoroutineScope,
) : AnalyticsTracker {

    override fun track(event: AnalyticsEvent) {
        val entity = AnalyticsEventEntity(
            name = event.name,
            params = event.params,
            timestampEpochMs = clock.now().toEpochMilli(),
        )
        Log.d(TAG, "${entity.name} ${entity.params}")
        scope.launch { insert(entity) }
    }

    private suspend fun insert(entity: AnalyticsEventEntity) {
        try {
            dao.insert(entity)
        } catch (exception: Exception) {
            Log.w(TAG, "Could not store ${entity.name}", exception)
        }
    }

    private companion object {
        const val TAG: String = "Analytics"
    }
}
