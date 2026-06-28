package com.leaseflow.app.data.repository

import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.remote.RetrofitClient
import com.leaseflow.app.data.remote.dto.MensajeContactoDTO
import com.leaseflow.app.data.remote.dto.RespuestaMensajeDTO
import com.leaseflow.app.data.remote.safeApiCall

/**
 * Repositorio para comunicacion con Contact Service (Puerto 8085).
 *
 * CAMBIO: Todos los metodos protegidos reciben (userId: Long, roleId: Int)
 * y los propagan como headers X-Usuario-Id / X-Rol-Id.
 * crearMensaje es publico — no requiere headers de identidad.
 */
class ContactRemoteRepository {

    private val api = RetrofitClient.contactServiceApi

    // ==================== MENSAJES ====================

    /**
     * Crear mensaje de contacto — publico, sin headers de identidad.
     */
    suspend fun crearMensaje(
        nombre: String,
        email: String,
        asunto: String,
        mensaje: String,
        numeroTelefono: String? = null,
        usuarioId: Long? = null
    ): ApiResult<MensajeContactoDTO> {
        val mensajeDTO = MensajeContactoDTO(
            nombre = nombre,
            email = email,
            asunto = asunto,
            mensaje = mensaje,
            numeroTelefono = numeroTelefono,
            usuarioId = usuarioId
        )
        return safeApiCall { api.crearMensaje(mensajeDTO) }
    }

    suspend fun listarTodosMensajes(
        userId: Long,
        roleId: Int,
        includeDetails: Boolean = false
    ): ApiResult<List<MensajeContactoDTO>> {
        return safeApiCall { api.listarTodosMensajes(userId, roleId, includeDetails) }
    }

    suspend fun obtenerMensajePorId(
        userId: Long,
        roleId: Int,
        mensajeId: Long,
        includeDetails: Boolean = true
    ): ApiResult<MensajeContactoDTO> {
        return safeApiCall { api.obtenerMensajePorId(mensajeId, userId, roleId, includeDetails) }
    }

    suspend fun listarMensajesPorEmail(
        userId: Long,
        roleId: Int,
        email: String
    ): ApiResult<List<MensajeContactoDTO>> {
        return safeApiCall { api.listarMensajesPorEmail(email, userId, roleId) }
    }

    suspend fun listarMensajesPorUsuario(
        userId: Long,
        roleId: Int,
        targetUsuarioId: Long
    ): ApiResult<List<MensajeContactoDTO>> {
        return safeApiCall { api.listarMensajesPorUsuario(targetUsuarioId, userId, roleId) }
    }

    suspend fun listarMensajesPorEstado(
        userId: Long,
        roleId: Int,
        estado: String
    ): ApiResult<List<MensajeContactoDTO>> {
        return safeApiCall { api.listarMensajesPorEstado(estado, userId, roleId) }
    }

    suspend fun listarMensajesSinResponder(
        userId: Long,
        roleId: Int
    ): ApiResult<List<MensajeContactoDTO>> {
        return safeApiCall { api.listarMensajesSinResponder(userId, roleId) }
    }

    suspend fun buscarMensajes(
        userId: Long,
        roleId: Int,
        keyword: String
    ): ApiResult<List<MensajeContactoDTO>> {
        return safeApiCall { api.buscarMensajesPorPalabraClave(keyword, userId, roleId) }
    }

    suspend fun actualizarEstado(
        userId: Long,
        roleId: Int,
        mensajeId: Long,
        nuevoEstado: String
    ): ApiResult<MensajeContactoDTO> {
        return safeApiCall { api.actualizarEstadoMensaje(mensajeId, userId, roleId, nuevoEstado) }
    }

    suspend fun responderMensaje(
        userId: Long,
        roleId: Int,
        mensajeId: Long,
        respuesta: String,
        respondidoPor: Long,
        nuevoEstado: String? = null
    ): ApiResult<MensajeContactoDTO> {
        val respuestaDTO = RespuestaMensajeDTO(
            respuesta = respuesta,
            respondidoPor = respondidoPor,
            nuevoEstado = nuevoEstado
        )
        return safeApiCall { api.responderMensaje(mensajeId, userId, roleId, respuestaDTO) }
    }

    suspend fun eliminarMensaje(
        userId: Long,
        roleId: Int,
        mensajeId: Long,
        adminId: Long
    ): ApiResult<Void> {
        return safeApiCall { api.eliminarMensaje(mensajeId, userId, roleId, adminId) }
    }

    suspend fun obtenerEstadisticas(
        userId: Long,
        roleId: Int
    ): ApiResult<Map<String, Long>> {
        return safeApiCall { api.obtenerEstadisticas(userId, roleId) }
    }

    // ==================== HELPERS ====================

    suspend fun puedeEnviarMensaje(userId: Long, roleId: Int, targetUsuarioId: Long): Boolean {
        return when (val result = listarMensajesPorUsuario(userId, roleId, targetUsuarioId)) {
            is ApiResult.Success -> result.data.count { it.estado == "PENDIENTE" } < 5
            else -> true
        }
    }

    suspend fun contarMensajesPendientes(userId: Long, roleId: Int, targetUsuarioId: Long): Int {
        return when (val result = listarMensajesPorUsuario(userId, roleId, targetUsuarioId)) {
            is ApiResult.Success -> result.data.count { it.estado == "PENDIENTE" }
            else -> 0
        }
    }

    suspend fun obtenerMensajesPendientesAdmin(userId: Long, roleId: Int): ApiResult<List<MensajeContactoDTO>> {
        return listarMensajesSinResponder(userId, roleId)
    }
}