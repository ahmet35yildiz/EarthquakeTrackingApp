package com.ahmetyildiz.quakealert.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.preferences.UserPreferences
import com.ahmetyildiz.quakealert.core.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.Instant
import javax.inject.Inject

class DataStoreUserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {

    override val userPreferences: Flow<UserPreferences> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { preferences -> preferences.toUserPreferences() }

    override suspend fun setOnboardingCompleted(isCompleted: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = isCompleted }
    }

    override suspend fun saveAlertSettings(settings: AlertSettings, baselineAt: Instant) {
        dataStore.edit { preferences ->
            preferences[Keys.ALERTS_ENABLED] = settings.isEnabled
            preferences[Keys.MAGNITUDE_THRESHOLD] = settings.magnitudeThreshold
            preferences.writeAlertArea(settings.area)
            preferences[Keys.ALERT_BASELINE_AT] = baselineAt.toEpochMilli()
        }
    }

    override suspend fun setLastCheckedAt(time: Instant) {
        dataStore.edit { it[Keys.LAST_CHECKED_AT] = time.toEpochMilli() }
    }

    override suspend fun setLastRefreshedAt(time: Instant) {
        dataStore.edit { it[Keys.LAST_REFRESHED_AT] = time.toEpochMilli() }
    }

    private fun Preferences.toUserPreferences(): UserPreferences {
        val defaults: UserPreferences = UserPreferences.DEFAULT
        return UserPreferences(
            isOnboardingCompleted = this[Keys.ONBOARDING_COMPLETED] ?: defaults.isOnboardingCompleted,
            alertSettings = AlertSettings(
                isEnabled = this[Keys.ALERTS_ENABLED] ?: defaults.alertSettings.isEnabled,
                magnitudeThreshold = this[Keys.MAGNITUDE_THRESHOLD] ?: defaults.alertSettings.magnitudeThreshold,
                area = readAlertArea(),
            ),
            alertBaselineAt = this[Keys.ALERT_BASELINE_AT]?.let(Instant::ofEpochMilli),
            lastCheckedAt = this[Keys.LAST_CHECKED_AT]?.let(Instant::ofEpochMilli),
            lastRefreshedAt = this[Keys.LAST_REFRESHED_AT]?.let(Instant::ofEpochMilli),
        )
    }

    private fun Preferences.readAlertArea(): AlertArea {
        val city = City(
            name = this[Keys.AREA_CITY_NAME] ?: return AlertArea.WholeWorld,
            adminArea = this[Keys.AREA_ADMIN_AREA],
            countryCode = this[Keys.AREA_COUNTRY_CODE] ?: return AlertArea.WholeWorld,
            location = GeoPoint(
                latitude = this[Keys.AREA_LATITUDE] ?: return AlertArea.WholeWorld,
                longitude = this[Keys.AREA_LONGITUDE] ?: return AlertArea.WholeWorld,
            ),
        )
        val radiusKm: Int = this[Keys.AREA_RADIUS_KM] ?: return AlertArea.WholeWorld
        return AlertArea.AroundCity(city = city, radiusKm = radiusKm)
    }

    private fun MutablePreferences.writeAlertArea(area: AlertArea) {
        when (area) {
            AlertArea.WholeWorld -> Keys.AREA_KEYS.forEach { remove(it) }
            is AlertArea.AroundCity -> writeAroundCity(area)
        }
    }

    private fun MutablePreferences.writeAroundCity(area: AlertArea.AroundCity) {
        this[Keys.AREA_CITY_NAME] = area.city.name
        val adminArea: String? = area.city.adminArea
        if (adminArea != null) this[Keys.AREA_ADMIN_AREA] = adminArea else remove(Keys.AREA_ADMIN_AREA)
        this[Keys.AREA_COUNTRY_CODE] = area.city.countryCode
        this[Keys.AREA_LATITUDE] = area.city.location.latitude
        this[Keys.AREA_LONGITUDE] = area.city.location.longitude
        this[Keys.AREA_RADIUS_KM] = area.radiusKm
    }

    private object Keys {
        val ONBOARDING_COMPLETED: Preferences.Key<Boolean> = booleanPreferencesKey("onboarding_completed")
        val ALERTS_ENABLED: Preferences.Key<Boolean> = booleanPreferencesKey("alerts_enabled")
        val MAGNITUDE_THRESHOLD: Preferences.Key<Double> = doublePreferencesKey("magnitude_threshold")
        val AREA_CITY_NAME: Preferences.Key<String> = stringPreferencesKey("area_city_name")
        val AREA_ADMIN_AREA: Preferences.Key<String> = stringPreferencesKey("area_admin_area")
        val AREA_COUNTRY_CODE: Preferences.Key<String> = stringPreferencesKey("area_country_code")
        val AREA_LATITUDE: Preferences.Key<Double> = doublePreferencesKey("area_latitude")
        val AREA_LONGITUDE: Preferences.Key<Double> = doublePreferencesKey("area_longitude")
        val AREA_RADIUS_KM: Preferences.Key<Int> = intPreferencesKey("area_radius_km")
        val ALERT_BASELINE_AT: Preferences.Key<Long> = longPreferencesKey("alert_baseline_at_epoch_ms")
        val LAST_CHECKED_AT: Preferences.Key<Long> = longPreferencesKey("last_checked_at_epoch_ms")
        val LAST_REFRESHED_AT: Preferences.Key<Long> = longPreferencesKey("last_refreshed_at_epoch_ms")
        val AREA_KEYS: List<Preferences.Key<*>> = listOf(
            AREA_CITY_NAME, AREA_ADMIN_AREA, AREA_COUNTRY_CODE, AREA_LATITUDE, AREA_LONGITUDE, AREA_RADIUS_KM,
        )
    }
}
