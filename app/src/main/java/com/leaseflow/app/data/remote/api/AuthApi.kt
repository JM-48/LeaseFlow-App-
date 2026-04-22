package com.leaseflow.app.data.remote.api

import com.leaseflow.app.data.remote.dto.LoginRequestDto
import com.leaseflow.app.data.remote.dto.LoginResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): LoginResponseDto
}
