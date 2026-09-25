package com.ahmetyildiz.quakealert.core.error

sealed interface AppError {

    data object Network : AppError

    data class Server(val code: Int) : AppError

    data object NotFound : AppError

    data object Parsing : AppError

    data object GeocoderUnavailable : AppError

    data object Unknown : AppError
}
