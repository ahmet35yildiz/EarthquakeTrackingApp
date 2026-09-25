package com.ahmetyildiz.quakealert.core.analytics

import com.ahmetyildiz.quakealert.core.locale.AppLanguage

sealed class AnalyticsEvent(val name: String, val params: Map<String, String> = emptyMap()) {

    data class AppOpened(val source: AppOpenSource) : AnalyticsEvent(
        name = "app_opened",
        params = mapOf("source" to source.paramValue),
    )

    data object OnboardingStarted : AnalyticsEvent(name = "onboarding_started")

    data class OnboardingStepViewed(val step: OnboardingStep) : AnalyticsEvent(
        name = "onboarding_step_viewed",
        params = mapOf("step" to step.paramValue),
    )

    data class OnboardingCompleted(
        val threshold: Double,
        val radiusKm: Int?,
        val isNotificationsGranted: Boolean,
    ) : AnalyticsEvent(
        name = "onboarding_completed",
        params = buildMap {
            put("threshold", threshold.toString())
            put("has_area", (radiusKm != null).toString())
            radiusKm?.let { put("radius_km", it.toString()) }
            put("notifications_granted", isNotificationsGranted.toString())
        },
    )

    data class NotificationPermissionRequested(val context: SetupContext) : AnalyticsEvent(
        name = "notification_permission_requested",
        params = mapOf("context" to context.paramValue),
    )

    data class NotificationPermissionResult(val isGranted: Boolean) : AnalyticsEvent(
        name = "notification_permission_result",
        params = mapOf("granted" to isGranted.toString()),
    )

    data class AlertsToggled(val isEnabled: Boolean) : AnalyticsEvent(
        name = "alerts_toggled",
        params = mapOf("enabled" to isEnabled.toString()),
    )

    data class AlertThresholdChanged(val from: Double, val to: Double, val context: SetupContext) : AnalyticsEvent(
        name = "alert_threshold_changed",
        params = mapOf("from" to from.toString(), "to" to to.toString(), "context" to context.paramValue),
    )

    data class AlertAreaSet(val countryCode: String, val radiusKm: Int, val context: SetupContext) : AnalyticsEvent(
        name = "alert_area_set",
        params = mapOf(
            "country_code" to countryCode,
            "radius_km" to radiusKm.toString(),
            "context" to context.paramValue,
        ),
    )

    data class AlertAreaCleared(val context: SetupContext) : AnalyticsEvent(
        name = "alert_area_cleared",
        params = mapOf("context" to context.paramValue),
    )

    data class CitySearchPerformed(val countryCode: String, val resultCount: Int) : AnalyticsEvent(
        name = "city_search_performed",
        params = mapOf("country_code" to countryCode, "result_count" to resultCount.toString()),
    )

    data class CitySearchFailed(val reason: CitySearchFailureReason) : AnalyticsEvent(
        name = "city_search_failed",
        params = mapOf("reason" to reason.paramValue),
    )

    data class EarthquakeListViewed(
        val regionFilter: RegionFilterValue,
        val magnitudeFilter: MagnitudeFilterValue,
        val sortOrder: SortOrderValue,
    ) : AnalyticsEvent(
        name = "earthquake_list_viewed",
        params = mapOf(
            "region_filter" to regionFilter.paramValue,
            "magnitude_filter" to magnitudeFilter.paramValue,
            "sort" to sortOrder.paramValue,
        ),
    )

    data class EarthquakeListRefreshed(
        val trigger: RefreshTrigger,
        val isSuccessful: Boolean,
        val count: Int,
    ) : AnalyticsEvent(
        name = "earthquake_list_refreshed",
        params = mapOf(
            "trigger" to trigger.paramValue,
            "result" to if (isSuccessful) "success" else "failure",
            "count" to count.toString(),
        ),
    )

    data class RegionFilterChanged(val value: RegionFilterValue) : AnalyticsEvent(
        name = LIST_FILTER_CHANGED,
        params = mapOf("filter" to "region", "value" to value.paramValue),
    )

    data class MagnitudeFilterChanged(val value: MagnitudeFilterValue) : AnalyticsEvent(
        name = LIST_FILTER_CHANGED,
        params = mapOf("filter" to "magnitude", "value" to value.paramValue),
    )

    data class ListSortChanged(val sortOrder: SortOrderValue) : AnalyticsEvent(
        name = "list_sort_changed",
        params = mapOf("sort" to sortOrder.paramValue),
    )

    data class EarthquakeDetailViewed(val source: DetailSource, val magnitude: Double?) : AnalyticsEvent(
        name = "earthquake_detail_viewed",
        params = buildMap {
            put("source", source.paramValue)
            magnitude?.let { put("magnitude", it.toString()) }
        },
    )

    data class DetailActionClicked(val action: DetailAction) : AnalyticsEvent(
        name = "detail_action_clicked",
        params = mapOf("action" to action.paramValue),
    )

    data class AlertNotificationPosted(val eventId: String, val magnitude: Double, val batchSize: Int) : AnalyticsEvent(
        name = "alert_notification_posted",
        params = mapOf(
            "event_id" to eventId,
            "magnitude" to magnitude.toString(),
            "batch_size" to batchSize.toString(),
        ),
    )

    data class AlertNotificationSuppressed(val reason: SuppressionReason) : AnalyticsEvent(
        name = "alert_notification_suppressed",
        params = mapOf("reason" to reason.paramValue),
    )

    data class AlertNotificationOpened(val eventId: String, val delaySeconds: Long) : AnalyticsEvent(
        name = "alert_notification_opened",
        params = mapOf("event_id" to eventId, "delay_seconds" to delaySeconds.toString()),
    )

    data class BackgroundCheckCompleted(
        val fetched: Int,
        val matched: Int,
        val notified: Int,
        val durationMs: Long,
    ) : AnalyticsEvent(
        name = "background_check_completed",
        params = mapOf(
            "fetched" to fetched.toString(),
            "matched" to matched.toString(),
            "notified" to notified.toString(),
            "duration_ms" to durationMs.toString(),
        ),
    )

    data class BackgroundCheckFailed(val reason: BackgroundCheckFailureReason) : AnalyticsEvent(
        name = "background_check_failed",
        params = mapOf("reason" to reason.paramValue),
    )

    data class LanguageChanged(val from: AppLanguage?, val to: AppLanguage?) : AnalyticsEvent(
        name = "language_changed",
        params = mapOf("from" to from.paramValue, "to" to to.paramValue),
    )

    data object DeveloperSimulatedAlert : AnalyticsEvent(name = "developer_simulated_alert")

    data object DeveloperCheckTriggered : AnalyticsEvent(name = "developer_check_triggered")

    private companion object {
        const val LIST_FILTER_CHANGED: String = "list_filter_changed"
    }
}

private val Enum<*>.paramValue: String
    get() = name.lowercase()

private const val SYSTEM_LANGUAGE: String = "system"

private val AppLanguage?.paramValue: String
    get() = this?.tag ?: SYSTEM_LANGUAGE
