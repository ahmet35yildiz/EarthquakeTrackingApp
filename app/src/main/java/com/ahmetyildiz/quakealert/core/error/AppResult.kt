package com.ahmetyildiz.quakealert.core.error

/** Outcome of an operation that can fail in an expected way (see [AppError]). */
sealed interface AppResult<out T> {

    data class Success<out T>(val data: T) : AppResult<T>

    data class Failure(val error: AppError) : AppResult<Nothing>
}
