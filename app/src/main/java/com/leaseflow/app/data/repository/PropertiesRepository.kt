package com.leaseflow.app.data.repository

import com.leaseflow.app.data.local.dao.PropertyDao
import com.leaseflow.app.data.local.entities.PropertyEntity
import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.remote.api.PropertiesApi
import com.leaseflow.app.data.remote.safeApiCall

class PropertiesRepository(
    private val propertiesApi: PropertiesApi,
    private val propertyDao: PropertyDao,
) {
    suspend fun refreshProperties(): ApiResult<Unit> {
        val result = safeApiCall { propertiesApi.getProperties() }
        return when (result) {
            is ApiResult.Success -> {
                val entities = result.data.map { dto ->
                    PropertyEntity(
                        id = dto.id,
                        name = dto.name,
                        address = dto.address,
                    )
                }
                propertyDao.upsertAll(entities)
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun getLocalProperties(): List<PropertyEntity> = propertyDao.getAll()
}
