package com.leaseflow.app.data.remote.api

import com.leaseflow.app.data.remote.dto.ActualizarEstadoRequest
import com.leaseflow.app.data.remote.dto.DocumentoRemoteDTO
import com.leaseflow.app.data.remote.dto.EstadoDocumentoDTO
import com.leaseflow.app.data.remote.dto.TipoDocumentoRemoteDTO
import retrofit2.Response
import retrofit2.http.*

/**
 * API para comunicacion con Document Service (Puerto 8083)
 *
 * Capas de seguridad:
 *   Capa 1 (X-App-Client): interceptor global OkHttp en RetrofitClient.
 *   Capa 2 (X-Usuario-Id / X-Rol-Id): requerida en todos los endpoints
 *   excepto catalogos de solo lectura (listarEstados, listarTiposDocumentos y sus GET por ID).
 */
interface DocumentServiceApi {

    // ==================== DOCUMENTOS ====================

    @POST("api/documentos")
    suspend fun crearDocumento(
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Body documento: DocumentoRemoteDTO
    ): Response<DocumentoRemoteDTO>

    @GET("api/documentos")
    suspend fun listarTodosDocumentos(
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<DocumentoRemoteDTO>>

    @GET("api/documentos/{id}")
    suspend fun obtenerDocumentoPorId(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Query("includeDetails") includeDetails: Boolean = true
    ): Response<DocumentoRemoteDTO>

    @GET("api/documentos/usuario/{usuarioId}")
    suspend fun obtenerDocumentosPorUsuario(
        @Path("usuarioId") usuarioId: Long,
        @Header("X-Usuario-Id") headerUsuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Query("includeDetails") includeDetails: Boolean = true
    ): Response<List<DocumentoRemoteDTO>>

    @GET("api/documentos/usuario/{usuarioId}/verificar-aprobados")
    suspend fun verificarDocumentosAprobados(
        @Path("usuarioId") usuarioId: Long,
        @Header("X-Usuario-Id") headerUsuarioId: Long,
        @Header("X-Rol-Id") rolId: Int
    ): Response<Boolean>

    @PATCH("api/documentos/{id}/estado/{estadoId}")
    suspend fun actualizarEstadoDocumento(
        @Path("id") id: Long,
        @Path("estadoId") estadoId: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int
    ): Response<DocumentoRemoteDTO>

    @PATCH("api/documentos/{id}/estado")
    suspend fun actualizarEstadoConObservaciones(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Body request: ActualizarEstadoRequest
    ): Response<DocumentoRemoteDTO>

    @DELETE("api/documentos/{id}")
    suspend fun eliminarDocumento(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int
    ): Response<Void>

    // ==================== ESTADOS (catalogo — publico) ====================

    @GET("api/estados")
    suspend fun listarEstados(): Response<List<EstadoDocumentoDTO>>

    @GET("api/estados/{id}")
    suspend fun obtenerEstadoPorId(
        @Path("id") id: Long
    ): Response<EstadoDocumentoDTO>

    @POST("api/estados")
    suspend fun crearEstado(
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Body estado: EstadoDocumentoDTO
    ): Response<EstadoDocumentoDTO>

    // ==================== TIPOS DE DOCUMENTOS (catalogo — publico) ====================

    @GET("api/tipos-documentos")
    suspend fun listarTiposDocumentos(): Response<List<TipoDocumentoRemoteDTO>>

    @GET("api/tipos-documentos/{id}")
    suspend fun obtenerTipoDocumentoPorId(
        @Path("id") id: Long
    ): Response<TipoDocumentoRemoteDTO>

    @POST("api/tipos-documentos")
    suspend fun crearTipoDocumento(
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Body tipoDoc: TipoDocumentoRemoteDTO
    ): Response<TipoDocumentoRemoteDTO>
}