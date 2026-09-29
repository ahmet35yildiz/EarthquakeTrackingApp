package com.ahmetyildiz.quakealert.core.analytics

enum class AppOpenSource { LAUNCHER, NOTIFICATION }

enum class OnboardingStep { WELCOME, ALERT_SETUP, NOTIFICATIONS }

enum class SetupContext { ONBOARDING, SETTINGS }

enum class CitySearchFailureReason { NETWORK, UNAVAILABLE, UNKNOWN }

enum class CurrentLocationResult { SUCCESS, PERMISSION_DENIED, LOCATION_OFF, NOT_FOUND, NETWORK, UNKNOWN }

enum class RegionFilterValue { WORLD, NEAR_CITY }

enum class MagnitudeFilterValue { ALL, ABOVE_THRESHOLD }

enum class SortOrderValue { NEWEST_FIRST, LARGEST_FIRST, NEAREST_FIRST }

enum class RefreshTrigger { INITIAL, PULL, STALE }

enum class DetailSource { LIST, NOTIFICATION }

enum class DetailAction { MAP, USGS, SHARE }

enum class SafetyGuideSectionValue { BEFORE, DURING, AFTER }

enum class EmergencyToolValue { STROBE, WHISTLE }

enum class SuppressionReason { PERMISSION_DENIED, ALERT_CHANNEL_BLOCKED }

enum class BackgroundCheckFailureReason { NETWORK, SERVER, PARSING, UNKNOWN }

enum class SimulationOutcomeValue { POSTED, ALREADY_NOTIFIED, NOT_MATCHED, NOTIFICATIONS_OFF, ALERTS_OFF, NOTHING_TO_REPEAT }
