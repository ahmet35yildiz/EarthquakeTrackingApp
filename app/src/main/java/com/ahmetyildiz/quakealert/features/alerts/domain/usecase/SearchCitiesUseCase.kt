package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.features.alerts.domain.model.CitySearchQuery
import com.ahmetyildiz.quakealert.features.alerts.domain.repository.CitySearchRepository
import javax.inject.Inject

class SearchCitiesUseCase @Inject constructor(
    private val citySearchRepository: CitySearchRepository,
) {

    fun isAvailable(): Boolean = citySearchRepository.isCitySearchAvailable()

    suspend operator fun invoke(query: CitySearchQuery): AppResult<List<City>> {
        val name: String = query.name.trim()
        if (name.isEmpty()) return AppResult.Success(emptyList())
        return citySearchRepository.searchCities(query.copy(name = name))
    }
}
