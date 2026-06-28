package com.leaseflow.app.data.repository

import android.util.Log
import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.remote.RetrofitClient
import com.leaseflow.app.data.remote.dto.ActualizarEstadoRequest
import com.leaseflow.app.data.remote.dto.DocumentoRemoteDTO
import com.leaseflow.app.data.remote.dto.EstadoDocumentoDTO
import com.leaseflow.app.data.remote.dto.TipoDocumentoRemoteDTO
import com.leaseflow.app.data.remote.safeApiCall

/**
 * Repositorio para comunicacion con Document Service (Puerto 8083).
 *
 * CAMBIO: Todos los metodos protegidos reciben (userId: Long, roleId: Int)
 * y los propagan como headers X-Usuario-Id / X-Rol-Id.
 * listarEstados y listarTiposDocumentos son publicos — no los necesitan.
 */
class DocumentRemoteRepository {

    private val api = RetrofitClient.documentServiceApi

    companion object {
        private const val TAG = "DocumentRemoteRepo"

        const val ESTADO_PENDIENTE = 1L
        const val ESTADO_ACEPTADO = 2L
        const val ESTADO_RECHAZADO = 3L
        const val ESTADO_EN_REVISION = 4L

        const val TIPO_DNI = 1L
        const val TIPO_PASAPORTE = 2L
        const val TIPO_LIQUIDACION_SUELDO = 3L
        const val TIPO_CERTIFICADO_ANTECEDENTES = 4L
        const val TIPO_CERTIFICADO_AFP = 5L
        const val TIPO_CONTRATO_TRABAJO = 6L
    }

    // ==================== DOCUMENTOS ====================

    suspend fun crearDocumento(
        userId: Long,
        roleId: Int,
        nombre: String,
        usuarioId: Long,
        tipoDocId: Long,
        estadoId: Long = ESTADO_PENDIENTE
    ): ApiResult<DocumentoRemoteDTO> {
        Log.d(TAG, "Creando documento: $nombre para usuario $usuarioId")

        val documentoDTO = DocumentoRemoteDTO(
            nombre = nombre,
            usuarioId = usuarioId,
            estadoId = estadoId,
            tipoDocId = tipoDocId
        )

        return safeApiCall { api.crearDocumento(userId, roleId, documentoDTO) }
    }

    suspend fun subirMultiplesDocumentos(
        userId: Long,
        roleId: Int,
        usuarioId: Long,
        documentos: List<Pair<Long, String>>
    ): List<ApiResult<DocumentoRemoteDTO>> {
        Log.d(TAG, "Subiendo ${documentos.size} documentos para usuario $usuarioId")

        return documentos.map { (tipoDocId, nombreArchivo) ->
            crearDocumento(
                userId = userId,
                roleId = roleId,
                nombre = nombreArchivo,
                usuarioId = usuarioId,
                tipoDocId = tipoDocId,
                estadoId = ESTADO_PENDIENTE
            )
        }
    }

    suspend fun listarTodosDocumentos(
        userId: Long,
        roleId: Int,
        includeDetails: Boolean = false
    ): ApiResult<List<DocumentoRemoteDTO>> {
        Log.d(TAG, "Listando todos los documentos")
        return safeApiCall { api.listarTodosDocumentos(userId, roleId, includeDetails) }
    }

    suspend fun obtenerDocumentoPorId(
        userId: Long,
        roleId: Int,
        id: Long,
        includeDetails: Boolean = true
    ): ApiResult<DocumentoRemoteDTO> {
        Log.d(TAG, "Obteniendo documento $id")
        return safeApiCall { api.obtenerDocumentoPorId(id, userId, roleId, includeDetails) }
    }

    suspend fun obtenerDocumentosPorUsuario(
        userId: Long,
        roleId: Int,
        targetUsuarioId: Long,
        includeDetails: Boolean = true
    ): ApiResult<List<DocumentoRemoteDTO>> {
        Log.d(TAG, "Obteniendo documentos del usuario $targetUsuarioId (caller=$userId)")
        return safeApiCall {
            api.obtenerDocumentosPorUsuario(targetUsuarioId, userId, roleId, includeDetails)
        }
    }

    suspend fun verificarDocumentosAprobados(
        userId: Long,
        roleId: Int,
        targetUsuarioId: Long
    ): ApiResult<Boolean> {
        Log.d(TAG, "Verificando documentos aprobados para usuario $targetUsuarioId")
        return safeApiCall {
            api.verificarDocumentosAprobados(targetUsuarioId, userId, roleId)
        }
    }

    suspend fun actualizarEstadoDocumento(
        userId: Long,
        roleId: Int,
        documentoId: Long,
        nuevoEstadoId: Long
    ): ApiResult<DocumentoRemoteDTO> {
        Log.d(TAG, "Actualizando estado de documento $documentoId a $nuevoEstadoId")
        return safeApiCall {
            api.actualizarEstadoDocumento(documentoId, nuevoEstadoId, userId, roleId)
        }
    }

    suspend fun actualizarEstadoConObservaciones(
        userId: Long,
        roleId: Int,
        documentoId: Long,
        nuevoEstadoId: Long,
        observaciones: String?,
        revisadoPor: Long? = null
    ): ApiResult<DocumentoRemoteDTO> {
        Log.d(TAG, "Actualizando estado documento $documentoId con observaciones")

        val request = ActualizarEstadoRequest(
            estadoId = nuevoEstadoId,
            observaciones = observaciones,
            revisadoPor = revisadoPor
        )

        return safeApiCall { api.actualizarEstadoConObservaciones(documentoId, userId, roleId, request) }
    }

    suspend fun eliminarDocumento(
        userId: Long,
        roleId: Int,
        id: Long
    ): ApiResult<Unit> {
        Log.d(TAG, "Eliminando documento $id (caller=$userId)")
        return try {
            val response = api.eliminarDocumento(id, userId, roleId)
            if (response.isSuccessful) {
                Log.d(TAG, "Documento $id eliminado")
                ApiResult.Success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error ${response.code()}"
                Log.e(TAG, "Error al eliminar: $errorMsg")
                ApiResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepcion al eliminar documento: ${e.message}")
            ApiResult.Error(e.message ?: "Error de conexion")
        }
    }

    // ==================== ESTADOS (publico) ====================

    suspend fun listarEstados(): ApiResult<List<EstadoDocumentoDTO>> =
        safeApiCall { api.listarEstados() }

    suspend fun obtenerEstadoPorId(id: Long): ApiResult<EstadoDocumentoDTO> =
        safeApiCall { api.obtenerEstadoPorId(id) }

    // ==================== TIPOS DE DOCUMENTOS (publico) ====================

    suspend fun listarTiposDocumentos(): ApiResult<List<TipoDocumentoRemoteDTO>> =
        safeApiCall { api.listarTiposDocumentos() }

    suspend fun obtenerTipoDocumentoPorId(id: Long): ApiResult<TipoDocumentoRemoteDTO> =
        safeApiCall { api.obtenerTipoDocumentoPorId(id) }

    // ==================== HELPERS ====================

    fun generarNombreDocumento(tipoDocId: Long, nombreUsuario: String, extension: String = "pdf"): String {
        val tipoNombre = when (tipoDocId) {
            TIPO_DNI -> "DNI"
            TIPO_PASAPORTE -> "PASAPORTE"
            TIPO_LIQUIDACION_SUELDO -> "LIQUIDACION"
            TIPO_CERTIFICADO_ANTECEDENTES -> "ANTECEDENTES"
            TIPO_CERTIFICADO_AFP -> "AFP"
            TIPO_CONTRATO_TRABAJO -> "CONTRATO"
            else -> "DOC"
        }
        val nombreLimpio = nombreUsuario.replace(" ", "_").replace(Regex("[^A-Za-z0-9_]"), "").take(20)
        return "${tipoNombre}_${nombreLimpio}_${System.currentTimeMillis()}.$extension"
    }

    fun getNombreTipoDocumento(tipoDocId: Long): String = when (tipoDocId) {
        TIPO_DNI -> "Cedula de Identidad"
        TIPO_PASAPORTE -> "Pasaporte"
        TIPO_LIQUIDACION_SUELDO -> "Liquidacion de Sueldo"
        TIPO_CERTIFICADO_ANTECEDENTES -> "Certificado de Antecedentes"
        TIPO_CERTIFICADO_AFP -> "Certificado AFP"
        TIPO_CONTRATO_TRABAJO -> "Contrato de Trabajo"
        else -> "Documento"
    }

    fun getNombreEstado(estadoId: Long): String = when (estadoId) {
        ESTADO_PENDIENTE -> "Pendiente"
        ESTADO_ACEPTADO -> "Aceptado"
        ESTADO_RECHAZADO -> "Rechazado"
        ESTADO_EN_REVISION -> "En Revision"
        else -> "Desconocido"
    }
}