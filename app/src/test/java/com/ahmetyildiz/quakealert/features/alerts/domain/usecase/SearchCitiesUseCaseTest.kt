package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.features.alerts.domain.model.CitySearchQuery
import com.ahmetyildiz.quakealert.features.alerts.domain.model.Country
import com.ahmetyildiz.quakealert.features.alerts.domain.repository.FakeCitySearchRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.Locale

class SearchCitiesUseCaseTest {

    private val repository = FakeCitySearchRepository()
    private val useCase = SearchCitiesUseCase(repository)
    private val query = CitySearchQuery(name = "Tokyo", country = Country("JP", "Japan"), locale = Locale.ENGLISH)

    @ParameterizedTest
    @ValueSource(strings = ["", "   "])
    fun `blank name returns no cities without searching`(name: String) = runTest {
        assertEquals(AppResult.Success(emptyList<City>()), useCase(query.copy(name = name)))
        assertTrue(repository.queries.isEmpty())
    }

    @Test
    fun `name is trimmed before searching`() = runTest {
        useCase(query.copy(name = "  Tokyo "))
        assertEquals(listOf(query), repository.queries)
    }

    @Test
    fun `repository result is returned as is`() = runTest {
        repository.result = AppResult.Failure(AppError.Network)
        assertEquals(AppResult.Failure(AppError.Network), useCase(query))
    }

    @Test
    fun `availability comes from the repository`() {
        assertTrue(useCase.isAvailable())
        repository.isAvailable = false
        assertFalse(useCase.isAvailable())
    }
}
