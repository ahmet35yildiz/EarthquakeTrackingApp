package com.ahmetyildiz.quakealert.features.eventlog.domain.usecase

import com.ahmetyildiz.quakealert.features.eventlog.domain.model.LoggedEvent
import com.ahmetyildiz.quakealert.features.eventlog.domain.repository.EventLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveEventLogUseCase @Inject constructor(
    private val eventLogRepository: EventLogRepository,
) {

    operator fun invoke(): Flow<List<LoggedEvent>> = eventLogRepository.observeEvents()
}
