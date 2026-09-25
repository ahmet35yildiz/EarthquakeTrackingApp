package com.ahmetyildiz.quakealert.core.network

import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.error.AppResult
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException

class SafeApiCallTest {

    @Test
    fun `returns success with the call's value`() = runTest {
        val result: AppResult<Int> = safeApiCall { 42 }
        assertEquals(AppResult.Success(42), result)
    }

    @Test
    fun `io failure maps to network error`() = runTest {
        val result: AppResult<Int> = safeApiCall { throw IOException("offline") }
        assertEquals(AppResult.Failure(AppError.Network), result)
    }

    @Test
    fun `timeout maps to network error`() = runTest {
        val result: AppResult<Int> = safeApiCall { throw SocketTimeoutException() }
        assertEquals(AppResult.Failure(AppError.Network), result)
    }

    @Test
    fun `http 404 maps to not found`() = runTest {
        val result: AppResult<Int> = safeApiCall { throw httpException(code = 404) }
        assertEquals(AppResult.Failure(AppError.NotFound), result)
    }

    @Test
    fun `other http errors map to server error with the code`() = runTest {
        val result: AppResult<Int> = safeApiCall { throw httpException(code = 503) }
        assertEquals(AppResult.Failure(AppError.Server(503)), result)
    }

    @Test
    fun `serialization failure maps to parsing error`() = runTest {
        val result: AppResult<Int> = safeApiCall { throw SerializationException("bad json") }
        assertEquals(AppResult.Failure(AppError.Parsing), result)
    }

    @Test
    fun `unexpected exception maps to unknown error`() = runTest {
        val result: AppResult<Int> = safeApiCall { throw IllegalStateException() }
        assertEquals(AppResult.Failure(AppError.Unknown), result)
    }

    @Test
    fun `cancellation is not swallowed`() = runTest {
        assertThrows<CancellationException> { safeApiCall<Int> { throw CancellationException() } }
    }

    private fun httpException(code: Int): HttpException =
        HttpException(Response.error<Unit>(code, "".toResponseBody()))
}
