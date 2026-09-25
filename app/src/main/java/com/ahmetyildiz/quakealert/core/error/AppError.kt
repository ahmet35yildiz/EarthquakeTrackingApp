package com.ahmetyildiz.quakealert.core.error

/** Expected failure reasons. The data layer maps exceptions to these; the UI maps them to string resources. */
sealed interface AppError {

    /** No connection, timeout or another I/O problem on the way to the server. */
    data object Network : AppError

    /** The server answered with an HTTP error other than 404. */
    data class Server(val code: Int) : AppError

    /** The requested item does not exist (HTTP 404). */
    data object NotFound : AppError

    /** The response could not be read into our models. */
    data object Parsing : AppError

    /** The device has no geocoding backend (`Geocoder.isPresent()` is false). */
    data object GeocoderUnavailable : AppError

    /** Any other unexpected failure. */
    data object Unknown : AppError
}
