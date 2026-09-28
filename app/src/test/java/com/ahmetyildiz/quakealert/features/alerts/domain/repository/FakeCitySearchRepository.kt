package com.ahmetyildiz.quakealert.features.alerts.domain.repository

import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.features.alerts.domain.model.CitySearchQuery
import kotlinx.coroutines.CompletableDeferred
import java.util.Locale

class FakeCitySearchRepository(
    var isAvailable: Boolean = true,
    var result: AppResult<List<City>> = AppResult.Success(emptyList()),
    var cityAtPoint: AppResult<City> = AppResult.Failure(AppError.LocationUnavailable),
) : CitySearchRepository {

    var pendingResult: CompletableDeferred<AppResult<List<City>>>? = null

    val queries: MutableList<CitySearchQuery> = mutableListOf()

    val pointQueries: MutableList<Pair<GeoPoint, Locale>> = mutableListOf()

    override fun isCitySearchAvailable(): Boolean = isAvailable

    override suspend fun searchCities(query: CitySearchQuery): AppResult<List<City>> {
        queries += query
        return pendingResult?.await() ?: result
    }

    override suspend fun findCityAt(point: GeoPoint, locale: Locale): AppResult<City> {
        pointQueries += point to locale
        return cityAtPoint
    }
}
