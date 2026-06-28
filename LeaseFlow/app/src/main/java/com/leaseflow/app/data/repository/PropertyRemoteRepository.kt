package com.leaseflow.app.data.repository

import android.util.Log
import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.remote.RetrofitClient
import com.leaseflow.app.data.remote.dto.*
import com.leaseflow.app.data.remote.safeApiCall
import okhttp3.MultipartBody

/**
 * Repositorio para comunicacion con Property Service (Puerto 8082).
 *
 * CAMBIO: Metodos protegidos reciben (userId: Long, roleId: Int) y los
 * propagan como headers X-Usuario-Id / X-Rol-Id.
 * Metodos de solo lectura publica no los necesitan.
 */
class PropertyRemoteRepository {

    private val api = RetrofitClient.propertyServiceApi

    companion object {
        private const val TAG = "PropertyRemoteRepo"
    }

    // ==================== PROPIEDADES ====================

    suspend fun crearPropiedad(
        userId: Long,
        roleId: Int,
        propiedad: PropertyRemoteDTO
    ): ApiResult<PropertyRemoteDTO> {
        Log.d(TAG, "Creando propiedad (caller=$userId)")
        return safeApiCall { api.crearPropiedad(userId, roleId, propiedad) }
    }

    suspend fun listarTodasPropiedades(
        page: Int = 0,
        size: Int = 100,
        includeDetails: Boolean = false
    ): ApiResult<PageResponse<PropertyRemoteDTO>> {
        Log.d(TAG, "Listando propiedades: page=$page, size=$size")
        return safeApiCall { api.listarTodasPropiedades(page, size, includeDetails) }
    }

    suspend fun listarPropiedadesPorUsuario(
        userId: Long,
        roleId: Int,
        propietarioId: Long,
        includeDetails: Boolean = false
    ): ApiResult<List<PropertyRemoteDTO>> {
        Log.d(TAG, "Listando propiedades de usuario $propietarioId (caller=$userId)")
        return safeApiCall {
            api.listarPropiedadesPorUsuario(propietarioId, userId, roleId, includeDetails)
        }
    }

    suspend fun obtenerPropiedadPorId(
        id: Long,
        includeDetails: Boolean = true
    ): ApiResult<PropertyRemoteDTO> {
        Log.d(TAG, "Obteniendo propiedad $id")
        return safeApiCall { api.obtenerPropiedadPorId(id, includeDetails) }
    }

    suspend fun obtenerPropiedadPorCodigo(
        codigo: String,
        includeDetails: Boolean = true
    ): ApiResult<PropertyRemoteDTO> {
        Log.d(TAG, "Obteniendo propiedad por codigo: $codigo")
        return safeApiCall { api.obtenerPropiedadPorCodigo(codigo, includeDetails) }
    }

    suspend fun actualizarPropiedad(
        userId: Long,
        roleId: Int,
        id: Long,
        propiedad: PropertyRemoteDTO
    ): ApiResult<PropertyRemoteDTO> {
        Log.d(TAG, "Actualizando propiedad $id (caller=$userId)")
        return safeApiCall { api.actualizarPropiedad(id, userId, roleId, propiedad) }
    }

    suspend fun eliminarPropiedad(
        userId: Long,
        roleId: Int,
        id: Long
    ): ApiResult<Unit> {
        Log.d(TAG, "Eliminando propiedad $id (caller=$userId)")
        return try {
            val response = api.eliminarPropiedad(id, userId, roleId)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error("Error ${response.code()}", response.code())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error de conexion")
        }
    }

    suspend fun buscarPropiedadesConFiltros(
        tipoId: Long? = null,
        comunaId: Long? = null,
        minPrecio: Double? = null,
        maxPrecio: Double? = null,
        nHabit: Int? = null,
        nBanos: Int? = null,
        petFriendly: Boolean? = null,
        includeDetails: Boolean = false
    ): ApiResult<List<PropertyRemoteDTO>> {
        Log.d(TAG, "Buscando propiedades con filtros")
        return safeApiCall {
            api.buscarPropiedadesConFiltros(
                tipoId, comunaId, minPrecio, maxPrecio,
                nHabit, nBanos, petFriendly, includeDetails
            )
        }
    }

    suspend fun existePropiedad(id: Long): ApiResult<Boolean> {
        return safeApiCall { api.existePropiedad(id) }
    }

    // ==================== FOTOS ====================

    suspend fun subirFoto(
        userId: Long,
        roleId: Int,
        propertyId: Long,
        file: MultipartBody.Part
    ): ApiResult<FotoRemoteDTO> {
        Log.d(TAG, "Subiendo foto para propiedad $propertyId (caller=$userId)")
        return safeApiCall { api.subirFoto(propertyId, userId, roleId, file) }
    }

    suspend fun listarFotos(propertyId: Long): ApiResult<List<FotoRemoteDTO>> {
        return safeApiCall { api.listarFotos(propertyId) }
    }

    suspend fun obtenerFoto(fotoId: Long): ApiResult<FotoRemoteDTO> {
        return safeApiCall { api.obtenerFoto(fotoId) }
    }

    suspend fun eliminarFoto(
        userId: Long,
        roleId: Int,
        fotoId: Long
    ): ApiResult<Unit> {
        Log.d(TAG, "Eliminando foto $fotoId (caller=$userId)")
        return try {
            val response = api.eliminarFoto(fotoId, userId, roleId)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error("Error ${response.code()}", response.code())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error de conexion")
        }
    }

    suspend fun reordenarFotos(
        userId: Long,
        roleId: Int,
        propertyId: Long,
        fotosIds: List<Long>
    ): ApiResult<Unit> {
        return try {
            val response = api.reordenarFotos(propertyId, userId, roleId, fotosIds)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error("Error ${response.code()}", response.code())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error de conexion")
        }
    }

    // ==================== TIPOS ====================

    suspend fun crearTipo(userId: Long, roleId: Int, tipo: TipoRemoteDTO): ApiResult<TipoRemoteDTO> =
        safeApiCall { api.crearTipo(userId, roleId, tipo) }

    suspend fun listarTipos(): ApiResult<List<TipoRemoteDTO>> =
        safeApiCall { api.listarTipos() }

    suspend fun obtenerTipoPorId(id: Long): ApiResult<TipoRemoteDTO> =
        safeApiCall { api.obtenerTipoPorId(id) }

    suspend fun actualizarTipo(userId: Long, roleId: Int, id: Long, tipo: TipoRemoteDTO): ApiResult<TipoRemoteDTO> =
        safeApiCall { api.actualizarTipo(id, userId, roleId, tipo) }

    suspend fun eliminarTipo(userId: Long, roleId: Int, id: Long): ApiResult<Unit> {
        return try {
            val response = api.eliminarTipo(id, userId, roleId)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error("Error ${response.code()}", response.code())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error de conexion")
        }
    }

    // ==================== COMUNAS ====================

    suspend fun crearComuna(userId: Long, roleId: Int, comuna: ComunaRemoteDTO): ApiResult<ComunaRemoteDTO> =
        safeApiCall { api.crearComuna(userId, roleId, comuna) }

    suspend fun listarComunas(): ApiResult<List<ComunaRemoteDTO>> =
        safeApiCall { api.listarComunas() }

    suspend fun obtenerComunaPorId(id: Long): ApiResult<ComunaRemoteDTO> =
        safeApiCall { api.obtenerComunaPorId(id) }

    suspend fun obtenerComunasPorRegion(regionId: Long): ApiResult<List<ComunaRemoteDTO>> =
        safeApiCall { api.obtenerComunasPorRegion(regionId) }

    suspend fun actualizarComuna(userId: Long, roleId: Int, id: Long, comuna: ComunaRemoteDTO): ApiResult<ComunaRemoteDTO> =
        safeApiCall { api.actualizarComuna(id, userId, roleId, comuna) }

    suspend fun eliminarComuna(userId: Long, roleId: Int, id: Long): ApiResult<Unit> {
        return try {
            val response = api.eliminarComuna(id, userId, roleId)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error("Error ${response.code()}", response.code())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error de conexion")
        }
    }

    // ==================== REGIONES ====================

    suspend fun crearRegion(userId: Long, roleId: Int, region: RegionRemoteDTO): ApiResult<RegionRemoteDTO> =
        safeApiCall { api.crearRegion(userId, roleId, region) }

    suspend fun listarRegiones(): ApiResult<List<RegionRemoteDTO>> =
        safeApiCall { api.listarRegiones() }

    suspend fun obtenerRegionPorId(id: Long): ApiResult<RegionRemoteDTO> =
        safeApiCall { api.obtenerRegionPorId(id) }

    suspend fun actualizarRegion(userId: Long, roleId: Int, id: Long, region: RegionRemoteDTO): ApiResult<RegionRemoteDTO> =
        safeApiCall { api.actualizarRegion(id, userId, roleId, region) }

    suspend fun eliminarRegion(userId: Long, roleId: Int, id: Long): ApiResult<Unit> {
        return try {
            val response = api.eliminarRegion(id, userId, roleId)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error("Error ${response.code()}", response.code())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error de conexion")
        }
    }

    // ==================== CATEGORIAS ====================

    suspend fun crearCategoria(userId: Long, roleId: Int, categoria: CategoriaRemoteDTO): ApiResult<CategoriaRemoteDTO> =
        safeApiCall { api.crearCategoria(userId, roleId, categoria) }

    suspend fun listarCategorias(): ApiResult<List<CategoriaRemoteDTO>> =
        safeApiCall { api.listarCategorias() }

    suspend fun obtenerCategoriaPorId(id: Long): ApiResult<CategoriaRemoteDTO> =
        safeApiCall { api.obtenerCategoriaPorId(id) }

    suspend fun actualizarCategoria(userId: Long, roleId: Int, id: Long, categoria: CategoriaRemoteDTO): ApiResult<CategoriaRemoteDTO> =
        safeApiCall { api.actualizarCategoria(id, userId, roleId, categoria) }

    suspend fun eliminarCategoria(userId: Long, roleId: Int, id: Long): ApiResult<Unit> {
        return try {
            val response = api.eliminarCategoria(id, userId, roleId)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error("Error ${response.code()}", response.code())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error de conexion")
        }
    }
}