package com.ahmetyildiz.quakealert.features.eventlog.domain.model

import java.time.Instant

data class LoggedEvent(
    val id: Long,
    val name: String,
    val params: Map<String, String>,
    val time: Instant,
)
