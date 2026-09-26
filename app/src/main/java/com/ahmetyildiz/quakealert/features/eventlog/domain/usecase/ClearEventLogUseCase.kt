package com.ahmetyildiz.quakealert.features.eventlog.domain.usecase

import com.ahmetyildiz.quakealert.features.eventlog.domain.repository.EventLogRepository
import javax.inject.Inject

class ClearEventLogUseCase @Inject constructor(
    private val eventLogRepository: EventLogRepository,
) {

    suspend operator fun invoke() {
        eventLogRepository.clear()
    }
}
