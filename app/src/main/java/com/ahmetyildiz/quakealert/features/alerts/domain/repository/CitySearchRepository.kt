package com.ahmetyildiz.quakealert.features.alerts.domain.repository

import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.features.alerts.domain.model.CitySearchQuery

interface CitySearchRepository {

    fun isCitySearchAvailable(): Boolean

    suspend fun searchCities(query: CitySearchQuery): AppResult<List<City>>
}
