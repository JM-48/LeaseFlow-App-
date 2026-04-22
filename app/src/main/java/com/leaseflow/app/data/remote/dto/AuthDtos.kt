package com.leaseflow.app.data.remote.dto

data class LoginRequestDto(
    val email: String,
    val password: String,
)

data class LoginResponseDto(
    val token: String,
)
