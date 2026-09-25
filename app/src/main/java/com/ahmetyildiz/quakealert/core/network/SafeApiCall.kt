package com.ahmetyildiz.quakealert.core.network

import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.error.AppResult
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection
import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> safeApiCall(call: suspend () -> T): AppResult<T> =
    try {
        AppResult.Success(call())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        AppResult.Failure(exception.toAppError())
    }

fun Exception.toAppError(): AppError =
    when (this) {
        is HttpException -> if (code() == HttpURLConnection.HTTP_NOT_FOUND) AppError.NotFound else AppError.Server(code())
        is IOException -> AppError.Network
        is SerializationException -> AppError.Parsing
        else -> AppError.Unknown
    }
