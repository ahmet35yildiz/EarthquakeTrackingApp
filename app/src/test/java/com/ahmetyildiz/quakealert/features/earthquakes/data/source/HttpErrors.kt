package com.ahmetyildiz.quakealert.features.earthquakes.data.source

import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

private const val HTTP_NOT_FOUND: Int = 404

fun httpNotFound(): HttpException = httpError(HTTP_NOT_FOUND)

fun httpError(code: Int): HttpException = HttpException(Response.error<Unit>(code, "".toResponseBody()))
