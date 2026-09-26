package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsTracker
import com.ahmetyildiz.quakealert.core.analytics.BackgroundCheckFailureReason
import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.preferences.UserPreferences
import com.ahmetyildiz.quakealert.core.preferences.UserPreferencesRepository
import com.ahmetyildiz.quakealert.core.time.Clock
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertConfig
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertMatcher
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertCheckResult
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertMatchCriteria
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertNotificationResult
import com.ahmetyildiz.quakealert.features.alerts.domain.repository.NotifiedEarthquakeRepository
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeQuery
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.EarthquakeRepository
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class CheckForNewAlertsUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val earthquakeRepository: EarthquakeRepository,
    private val notifiedEarthquakeRepository: NotifiedEarthquakeRepository,
    private val alertMatcher: AlertMatcher,
    private val notifyAlerts: NotifyAlertsUseCase,
    private val analyticsTracker: AnalyticsTracker,
    private val clock: Clock,
) {

    suspend operator fun invoke(): AlertCheckResult {
        val checkedAt: Instant = clock.now()
        val preferences: UserPreferences = userPreferencesRepository.userPreferences.first()
        val baselineAt: Instant = preferences.alertBaselineAt ?: return AlertCheckResult.Skipped
        if (!preferences.alertSettings.isEnabled) return AlertCheckResult.Skipped
        val criteria = AlertMatchCriteria(preferences.alertSettings, baselineAt, emptySet(), checkedAt)
        val query: EarthquakeQuery = buildQuery(preferences, baselineAt, checkedAt)
        return when (val result: AppResult<List<Earthquake>> = earthquakeRepository.fetchEarthquakes(query)) {
            is AppResult.Failure -> fail(result.error)
            is AppResult.Success -> complete(result.data, criteria)
        }
    }

    private fun buildQuery(preferences: UserPreferences, baselineAt: Instant, checkedAt: Instant): EarthquakeQuery =
        EarthquakeQuery(
            startTime = checkedAt - AlertConfig.MAX_EVENT_AGE,
            minMagnitude = preferences.alertSettings.magnitudeThreshold,
            area = preferences.alertSettings.area,
            updatedAfter = (preferences.lastCheckedAt ?: baselineAt) - AlertConfig.UPDATED_AFTER_OVERLAP,
        )

    private suspend fun complete(fetched: List<Earthquake>, criteria: AlertMatchCriteria): AlertCheckResult {
        val notifiedIds: Set<String> = notifiedEarthquakeRepository.findNotifiedIds(fetched.map(Earthquake::id))
        val matches: List<Earthquake> = alertMatcher.findMatches(fetched, criteria.copy(notifiedEarthquakeIds = notifiedIds))
        val isPosted: Boolean = notifyAlerts(matches, criteria.settings) == AlertNotificationResult.POSTED
        if (isPosted) notifiedEarthquakeRepository.markNotified(matches.map(Earthquake::id), criteria.checkedAt)
        notifiedEarthquakeRepository.deleteNotifiedBefore(criteria.checkedAt - AlertConfig.NOTIFIED_ID_RETENTION)
        userPreferencesRepository.setLastCheckedAt(criteria.checkedAt)
        val result = AlertCheckResult.Completed(fetched.size, matches.size, notified = if (isPosted) matches.size else 0)
        trackCompleted(result, startedAt = criteria.checkedAt)
        return result
    }

    private fun trackCompleted(result: AlertCheckResult.Completed, startedAt: Instant) {
        val durationMs: Long = Duration.between(startedAt, clock.now()).toMillis()
        analyticsTracker.track(
            AnalyticsEvent.BackgroundCheckCompleted(result.fetched, result.matched, result.notified, durationMs),
        )
    }

    private fun fail(error: AppError): AlertCheckResult {
        analyticsTracker.track(AnalyticsEvent.BackgroundCheckFailed(error.toFailureReason()))
        return AlertCheckResult.Failed(error)
    }

    private fun AppError.toFailureReason(): BackgroundCheckFailureReason =
        when (this) {
            AppError.Network -> BackgroundCheckFailureReason.NETWORK
            is AppError.Server -> BackgroundCheckFailureReason.SERVER
            AppError.Parsing -> BackgroundCheckFailureReason.PARSING
            else -> BackgroundCheckFailureReason.UNKNOWN
        }
}
