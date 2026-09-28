package com.ahmetyildiz.quakealert.features.alerts.domain.repository

import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.features.alerts.domain.model.CitySearchQuery
import java.util.Locale

interface CitySearchRepository {

    fun isCitySearchAvailable(): Boolean

    suspend fun searchCities(query: CitySearchQuery): AppResult<List<City>>

    suspend fun findCityAt(point: GeoPoint, locale: Locale): AppResult<City>
}
