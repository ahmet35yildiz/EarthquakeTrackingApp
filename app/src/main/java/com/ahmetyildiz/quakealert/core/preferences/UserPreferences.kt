package com.ahmetyildiz.quakealert.core.preferences

import java.time.Instant

/** Everything the app persists about the user's choices and the state of its background work. */
data class UserPreferences(
    val isOnboardingCompleted: Boolean,
    val alertSettings: AlertSettings,
    /** Earthquakes that happened before this moment never alert (set whenever alert settings are saved). */
    val alertBaselineAt: Instant?,
    /** Last successful background alert check. */
    val lastCheckedAt: Instant?,
    /** Last successful refresh of the earthquake list cache. */
    val lastRefreshedAt: Instant?,
) {

    companion object {
        val DEFAULT: UserPreferences = UserPreferences(
            isOnboardingCompleted = false,
            alertSettings = AlertSettings.DEFAULT,
            alertBaselineAt = null,
            lastCheckedAt = null,
            lastRefreshedAt = null,
        )
    }
}
