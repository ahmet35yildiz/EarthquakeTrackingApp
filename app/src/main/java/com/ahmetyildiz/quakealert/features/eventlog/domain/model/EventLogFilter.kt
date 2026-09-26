package com.ahmetyildiz.quakealert.features.eventlog.domain.model

fun List<LoggedEvent>.filterByName(query: String): List<LoggedEvent> {
    val trimmedQuery: String = query.trim()
    if (trimmedQuery.isEmpty()) return this
    return filter { it.name.contains(trimmedQuery, ignoreCase = true) }
}
