package com.ahmetyildiz.quakealert.core.preferences

import java.time.Instant

data class UserPreferences(
    val isOnboardingCompleted: Boolean,
    val alertSettings: AlertSettings,
    val alertBaselineAt: Instant?,
    val lastCheckedAt: Instant?,
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
