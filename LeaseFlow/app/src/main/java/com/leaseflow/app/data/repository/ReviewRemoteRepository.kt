package com.leaseflow.app.data.repository

import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.remote.RetrofitClient
import com.leaseflow.app.data.remote.dto.ResenaDTO
import com.leaseflow.app.data.remote.dto.TipoResenaDTO
import com.leaseflow.app.data.remote.safeApiCall

/**
 * Repositorio para comunicacion con Review Service (Puerto 8086).
 *
 * CAMBIO: Metodos protegidos reciben (userId: Long, roleId: Int) y los
 * propagan como headers X-Usuario-Id / X-Rol-Id.
 * Metodos de solo lectura publica no los necesitan.
 */
class ReviewRemoteRepository {

    private val api = RetrofitClient.reviewServiceApi

    // ==================== RESEÑAS ====================

    suspend fun crearResena(
        userId: Long,
        roleId: Int,
        usuarioId: Long,
        propiedadId: Long?,
        usuarioResenadoId: Long?,
        puntuacion: Int,
        comentario: String?,
        tipoResenaId: Long
    ): ApiResult<ResenaDTO> {
        val resenaDTO = ResenaDTO(
            usuarioId = usuarioId,
            propiedadId = propiedadId,
            usuarioResenadoId = usuarioResenadoId,
            puntuacion = puntuacion,
            comentario = comentario,
            tipoResenaId = tipoResenaId
        )
        return safeApiCall { api.crearResena(userId, roleId, resenaDTO) }
    }

    /**
     * Listar todas las reseñas — publico.
     */
    suspend fun listarTodasResenas(includeDetails: Boolean = false): ApiResult<List<ResenaDTO>> {
        return safeApiCall { api.listarTodasResenas(includeDetails) }
    }

    /**
     * Obtener reseña por ID — publico.
     */
    suspend fun obtenerResenaPorId(resenaId: Long, includeDetails: Boolean = true): ApiResult<ResenaDTO> {
        return safeApiCall { api.obtenerResenaPorId(resenaId, includeDetails) }
    }

    /**
     * Obtener reseñas creadas por un usuario — requiere identidad (lectura propia).
     */
    suspend fun obtenerResenasPorUsuario(
        userId: Long,
        roleId: Int,
        targetUsuarioId: Long,
        includeDetails: Boolean = false
    ): ApiResult<List<ResenaDTO>> {
        return safeApiCall {
            api.obtenerResenasPorUsuario(targetUsuarioId, userId, roleId, includeDetails)
        }
    }

    /**
     * Obtener reseñas de una propiedad — publico.
     */
    suspend fun obtenerResenasPorPropiedad(
        propiedadId: Long,
        includeDetails: Boolean = true
    ): ApiResult<List<ResenaDTO>> {
        return safeApiCall { api.obtenerResenasPorPropiedad(propiedadId, includeDetails) }
    }

    /**
     * Obtener reseñas sobre un usuario — publico.
     */
    suspend fun obtenerResenasSobreUsuario(
        usuarioResenadoId: Long,
        includeDetails: Boolean = true
    ): ApiResult<List<ResenaDTO>> {
        return safeApiCall { api.obtenerResenasSobreUsuario(usuarioResenadoId, includeDetails) }
    }

    /**
     * Calcular promedio propiedad — publico.
     */
    suspend fun calcularPromedioPorPropiedad(propiedadId: Long): ApiResult<Double> {
        return safeApiCall { api.calcularPromedioPorPropiedad(propiedadId) }
    }

    /**
     * Calcular promedio usuario — publico.
     */
    suspend fun calcularPromedioPorUsuario(usuarioResenadoId: Long): ApiResult<Double> {
        return safeApiCall { api.calcularPromedioPorUsuario(usuarioResenadoId) }
    }

    suspend fun actualizarEstadoResena(
        userId: Long,
        roleId: Int,
        resenaId: Long,
        nuevoEstado: String
    ): ApiResult<ResenaDTO> {
        return safeApiCall { api.actualizarEstadoResena(resenaId, userId, roleId, nuevoEstado) }
    }

    suspend fun eliminarResena(
        userId: Long,
        roleId: Int,
        resenaId: Long
    ): ApiResult<Void> {
        return safeApiCall { api.eliminarResena(resenaId, userId, roleId) }
    }

    // ==================== TIPOS DE RESEÑA ====================

    suspend fun listarTiposResena(): ApiResult<List<TipoResenaDTO>> =
        safeApiCall { api.listarTiposResena() }

    suspend fun obtenerTipoResenaPorId(tipoResenaId: Long): ApiResult<TipoResenaDTO> =
        safeApiCall { api.obtenerTipoResenaPorId(tipoResenaId) }

    // ==================== HELPERS ====================

    suspend fun puedeResenarPropiedad(userId: Long, roleId: Int, targetUsuarioId: Long, propiedadId: Long): Boolean {
        return when (val result = obtenerResenasPorUsuario(userId, roleId, targetUsuarioId)) {
            is ApiResult.Success -> !result.data.any { it.propiedadId == propiedadId }
            else -> true
        }
    }

    suspend fun obtenerResenasActivasPorPropiedad(propiedadId: Long): ApiResult<List<ResenaDTO>> {
        return when (val result = obtenerResenasPorPropiedad(propiedadId, true)) {
            is ApiResult.Success -> ApiResult.Success(result.data.filter { it.estado == "ACTIVA" })
            is ApiResult.Error -> result
            else -> ApiResult.Error("Error desconocido")
        }
    }
}