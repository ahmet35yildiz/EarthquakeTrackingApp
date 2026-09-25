package com.ahmetyildiz.quakealert.core.analytics

// Allowed values of the enum-like event parameters (docs/ANALYTICS.md §2).
// The logged value is the constant name in lower case, e.g. ALERT_SETUP → "alert_setup".

enum class AppOpenSource { LAUNCHER, NOTIFICATION }

enum class OnboardingStep { WELCOME, ALERT_SETUP, NOTIFICATIONS }

/** Where an alert setting or the notification permission was changed. */
enum class SetupContext { ONBOARDING, SETTINGS }

enum class CitySearchFailureReason { NETWORK, UNAVAILABLE, UNKNOWN }

enum class RegionFilterValue { WORLD, NEAR_CITY }

enum class MagnitudeFilterValue { ALL, ABOVE_THRESHOLD }

enum class RefreshTrigger { INITIAL, PULL, STALE }

enum class DetailSource { LIST, NOTIFICATION }

enum class DetailAction { MAP, USGS, SHARE }

enum class SuppressionReason { PERMISSION_DENIED }

enum class BackgroundCheckFailureReason { NETWORK, SERVER, PARSING, UNKNOWN }
