package com.leaseflow.app.data.remote.api

import com.leaseflow.app.data.remote.dto.PropertyDto
import retrofit2.http.GET

interface PropertiesApi {
    @GET("properties")
    suspend fun getProperties(): List<PropertyDto>
}
