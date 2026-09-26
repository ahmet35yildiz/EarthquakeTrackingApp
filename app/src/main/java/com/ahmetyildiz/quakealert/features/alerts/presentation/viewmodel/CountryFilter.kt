package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import com.ahmetyildiz.quakealert.features.alerts.domain.model.Country
import java.text.Normalizer
import java.util.Locale

private val COMBINING_MARKS: Regex = Regex("\\p{Mn}+")
private const val DOTLESS_I: Char = 'ı'

fun List<Country>.filterByName(query: String): List<Country> {
    val normalizedQuery: String = query.normalizeForSearch()
    if (normalizedQuery.isEmpty()) return this
    return filter { it.name.normalizeForSearch().contains(normalizedQuery) || it.code.equals(normalizedQuery, true) }
}

private fun String.normalizeForSearch(): String =
    Normalizer.normalize(trim(), Normalizer.Form.NFD)
        .replace(COMBINING_MARKS, "")
        .lowercase(Locale.ROOT)
        .replace(DOTLESS_I, 'i')
