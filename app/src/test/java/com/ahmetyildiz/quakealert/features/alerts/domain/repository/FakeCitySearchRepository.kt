package com.ahmetyildiz.quakealert.features.alerts.domain.repository

import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.features.alerts.domain.model.CitySearchQuery
import kotlinx.coroutines.CompletableDeferred

class FakeCitySearchRepository(
    var isAvailable: Boolean = true,
    var result: AppResult<List<City>> = AppResult.Success(emptyList()),
) : CitySearchRepository {

    var pendingResult: CompletableDeferred<AppResult<List<City>>>? = null

    val queries: MutableList<CitySearchQuery> = mutableListOf()

    override fun isCitySearchAvailable(): Boolean = isAvailable

    override suspend fun searchCities(query: CitySearchQuery): AppResult<List<City>> {
        queries += query
        return pendingResult?.await() ?: result
    }
}
