package com.leaseflow.app.data.repository

import com.leaseflow.app.data.local.storage.UserPreferencesDataStore
import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.remote.api.AuthApi
import com.leaseflow.app.data.remote.dto.LoginRequestDto
import com.leaseflow.app.data.remote.safeApiCall

class AuthRepository(
    private val authApi: AuthApi,
    private val preferences: UserPreferencesDataStore,
) {
    suspend fun login(email: String, password: String): ApiResult<Unit> {
        val result = safeApiCall {
            authApi.login(LoginRequestDto(email = email, password = password))
        }
        return when (result) {
            is ApiResult.Success -> {
                preferences.setAuthToken(result.data.token)
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> result
        }
    }
}
