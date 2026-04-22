package com.leaseflow.app.data.remote

import retrofit2.HttpException
import java.io.IOException

suspend inline fun <T> safeApiCall(
    crossinline block: suspend () -> T,
): ApiResult<T> {
    return try {
        ApiResult.Success(block())
    } catch (e: HttpException) {
        ApiResult.Error(
            message = e.message(),
            code = e.code(),
            throwable = e,
        )
    } catch (e: IOException) {
        ApiResult.Error(
            message = e.message ?: "Network error",
            throwable = e,
        )
    } catch (e: Throwable) {
        ApiResult.Error(
            message = e.message ?: "Unexpected error",
            throwable = e,
        )
    }
}
