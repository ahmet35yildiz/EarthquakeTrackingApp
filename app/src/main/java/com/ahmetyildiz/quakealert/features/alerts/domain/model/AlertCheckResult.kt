package com.ahmetyildiz.quakealert.features.alerts.domain.model

import com.ahmetyildiz.quakealert.core.error.AppError

sealed interface AlertCheckResult {

    data object Skipped : AlertCheckResult

    data class Completed(val fetched: Int, val matched: Int, val notified: Int) : AlertCheckResult

    data class Failed(val error: AppError) : AlertCheckResult {

        val shouldRetry: Boolean
            get() = error == AppError.Network || error is AppError.Server
    }
}
