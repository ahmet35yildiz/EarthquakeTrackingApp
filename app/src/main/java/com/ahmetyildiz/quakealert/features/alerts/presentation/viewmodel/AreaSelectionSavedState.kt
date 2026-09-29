package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint

private const val KEY_MODE: String = "area_selection_mode"
private const val KEY_RADIUS_KM: String = "area_selection_radius_km"
private const val KEY_CITY_NAME: String = "area_selection_city_name"
private const val KEY_CITY_ADMIN_AREA: String = "area_selection_city_admin_area"
private const val KEY_CITY_COUNTRY_CODE: String = "area_selection_city_country_code"
private const val KEY_CITY_LATITUDE: String = "area_selection_city_latitude"
private const val KEY_CITY_LONGITUDE: String = "area_selection_city_longitude"

fun SavedStateHandle.saveAreaSelection(selection: AreaSelection) {
    val city: City? = selection.city
    this[KEY_MODE] = selection.mode.name
    this[KEY_RADIUS_KM] = selection.radiusKm
    this[KEY_CITY_NAME] = city?.name
    this[KEY_CITY_ADMIN_AREA] = city?.adminArea
    this[KEY_CITY_COUNTRY_CODE] = city?.countryCode
    this[KEY_CITY_LATITUDE] = city?.location?.latitude
    this[KEY_CITY_LONGITUDE] = city?.location?.longitude
}

fun SavedStateHandle.restoreAreaSelectionOrNull(): AreaSelection? {
    val modeName: String = get<String>(KEY_MODE) ?: return null
    val mode: AreaMode = AreaMode.entries.firstOrNull { it.name == modeName } ?: return null
    val radiusKm: Int = get<Int>(KEY_RADIUS_KM) ?: return null
    return AreaSelection(mode = mode, city = restoreCityOrNull(), radiusKm = radiusKm)
}

private fun SavedStateHandle.restoreCityOrNull(): City? {
    val latitude: Double = get<Double>(KEY_CITY_LATITUDE) ?: return null
    val longitude: Double = get<Double>(KEY_CITY_LONGITUDE) ?: return null
    return City(
        name = get<String>(KEY_CITY_NAME) ?: return null,
        adminArea = get<String>(KEY_CITY_ADMIN_AREA),
        countryCode = get<String>(KEY_CITY_COUNTRY_CODE) ?: return null,
        location = GeoPoint.createOrNull(latitude, longitude) ?: return null,
    )
}
