package com.leaseflow.app.data.repository

import android.util.Log
import com.leaseflow.app.data.local.dao.SolicitudDao
import com.leaseflow.app.data.local.dao.CatalogDao
import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.remote.RetrofitClient
import com.leaseflow.app.data.remote.dto.RegistroArriendoDTO
import com.leaseflow.app.data.remote.dto.SolicitudArriendoDTO
import com.leaseflow.app.data.remote.safeApiCall
import java.util.Date

/**
 * Repositorio para comunicacion con Application Service (Puerto 8084)
 *
 * CAMBIO: Todos los metodos que llaman a endpoints protegidos reciben
 * (userId: Long, roleId: Int) y los propagan como headers X-Usuario-Id / X-Rol-Id.
 * crearSolicitudRemota y crearRegistro no los necesitan (el backend los lee del body).
 */
class ApplicationRemoteRepository(
    private val solicitudDao: SolicitudDao,
    private val catalogDao: CatalogDao
) {
    private val api = RetrofitClient.applicationServiceApi

    companion object {
        private const val TAG = "AppRemoteRepository"
    }

    // ==================== SOLICITUDES ====================

    /**
     * Crear solicitud — no requiere headers de identidad (el backend los lee del body).
     */
    suspend fun crearSolicitudRemota(
        usuarioId: Long,
        propiedadId: Long
    ): ApiResult<SolicitudArriendoDTO> {
        Log.d(TAG, "Creando solicitud: usuarioId=$usuarioId, propiedadId=$propiedadId")

        val solicitudDTO = SolicitudArriendoDTO(usuarioId = usuarioId, propiedadId = propiedadId)

        return when (val result = safeApiCall { api.crearSolicitud(solicitudDTO) }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Solicitud creada: id=${result.data.id}")
                result
            }
            is ApiResult.Error -> { Log.e(TAG, "Error: ${result.message}"); result }
            else -> result
        }
    }

    suspend fun obtenerSolicitudesUsuario(
        userId: Long,
        roleId: Int,
        usuarioId: Long
    ): ApiResult<List<SolicitudArriendoDTO>> {
        Log.d(TAG, "Obteniendo solicitudes del usuario $usuarioId (caller=$userId)")
        return safeApiCall { api.obtenerSolicitudesPorUsuario(usuarioId, userId, roleId) }
    }

    suspend fun obtenerSolicitudPorId(
        userId: Long,
        roleId: Int,
        solicitudId: Long
    ): ApiResult<SolicitudArriendoDTO> {
        Log.d(TAG, "Obteniendo solicitud $solicitudId")
        return safeApiCall { api.obtenerSolicitudPorId(solicitudId, userId, roleId, true) }
    }

    suspend fun obtenerSolicitudesPorPropiedad(
        userId: Long,
        roleId: Int,
        propiedadId: Long
    ): ApiResult<List<SolicitudArriendoDTO>> {
        Log.d(TAG, "Obteniendo solicitudes de propiedad $propiedadId")
        return safeApiCall { api.obtenerSolicitudesPorPropiedad(propiedadId, userId, roleId) }
    }

    suspend fun listarTodasSolicitudes(
        userId: Long,
        roleId: Int,
        includeDetails: Boolean = true
    ): ApiResult<List<SolicitudArriendoDTO>> {
        Log.d(TAG, "Listando todas las solicitudes")
        return safeApiCall { api.listarTodasSolicitudes(userId, roleId, includeDetails) }
    }

    suspend fun actualizarEstadoSolicitud(
        userId: Long,
        roleId: Int,
        solicitudId: Long,
        nuevoEstado: String
    ): ApiResult<SolicitudArriendoDTO> {
        Log.d(TAG, "Actualizando estado: solicitudId=$solicitudId, estado=$nuevoEstado")
        return safeApiCall { api.actualizarEstadoSolicitud(solicitudId, userId, roleId, nuevoEstado) }
    }

    suspend fun cancelarSolicitud(
        userId: Long,
        roleId: Int,
        solicitudId: Long
    ): ApiResult<Unit> {
        Log.d(TAG, "Cancelando solicitud $solicitudId")
        val deleteResult = safeApiCall { api.eliminarSolicitud(solicitudId, userId, roleId) }
        return when (deleteResult) {
            is ApiResult.Success -> ApiResult.Success(Unit)
            is ApiResult.Error -> {
                if (deleteResult.code == 405 || deleteResult.code == 404) {
                    when (val patchResult = actualizarEstadoSolicitud(userId, roleId, solicitudId, "CANCELADA")) {
                        is ApiResult.Success -> ApiResult.Success(Unit)
                        is ApiResult.Error -> patchResult
                        is ApiResult.Loading -> ApiResult.Loading
                    }
                } else {
                    deleteResult
                }
            }
            is ApiResult.Loading -> ApiResult.Loading
        }
    }

    // ==================== REGISTROS ====================

    /**
     * Crear registro — no requiere headers de identidad (el backend los lee del body).
     */
    suspend fun crearRegistro(
        solicitudId: Long,
        fechaInicio: Date,
        montoMensual: Double,
        fechaFin: Date? = null
    ): ApiResult<RegistroArriendoDTO> {
        val registroDTO = RegistroArriendoDTO(
            solicitudId = solicitudId,
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            montoMensual = montoMensual
        )
        return safeApiCall { api.crearRegistro(registroDTO) }
    }

    suspend fun obtenerRegistroPorId(
        userId: Long,
        roleId: Int,
        registroId: Long
    ): ApiResult<RegistroArriendoDTO> {
        return safeApiCall { api.obtenerRegistroPorId(registroId, userId, roleId, true) }
    }

    suspend fun obtenerRegistrosPorSolicitud(
        userId: Long,
        roleId: Int,
        solicitudId: Long
    ): ApiResult<List<RegistroArriendoDTO>> {
        return safeApiCall { api.obtenerRegistrosPorSolicitud(solicitudId, userId, roleId) }
    }

    suspend fun finalizarRegistro(
        userId: Long,
        roleId: Int,
        registroId: Long
    ): ApiResult<RegistroArriendoDTO> {
        return safeApiCall { api.finalizarRegistro(registroId, userId, roleId) }
    }

    suspend fun listarTodosRegistros(
        userId: Long,
        roleId: Int,
        includeDetails: Boolean = false
    ): ApiResult<List<RegistroArriendoDTO>> {
        return safeApiCall { api.listarTodosRegistros(userId, roleId, includeDetails) }
    }
}