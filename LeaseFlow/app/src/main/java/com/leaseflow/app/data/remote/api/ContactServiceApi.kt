package com.leaseflow.app.data.remote.api

import com.leaseflow.app.data.remote.dto.MensajeContactoDTO
import com.leaseflow.app.data.remote.dto.RespuestaMensajeDTO
import com.leaseflow.app.data.remote.dto.EstadisticasMensajesDTO
import retrofit2.Response
import retrofit2.http.*

/**
 * API para comunicacion con Contact Service (Puerto 8085)
 *
 * Capas de seguridad:
 *   Capa 1 (X-App-Client): interceptor global OkHttp en RetrofitClient.
 *   Capa 2 (X-Usuario-Id / X-Rol-Id): requerida en endpoints de admin y consultas.
 *
 * Publico (sin headers de identidad):
 *   - crearMensaje: cualquier visitante puede enviar un mensaje de contacto.
 *
 * Protegidos (requieren headers de identidad):
 *   - Todo lo demas (listar, consultar, actualizar, responder, eliminar).
 */
interface ContactServiceApi {

    // ==================== MENSAJES DE CONTACTO ====================

    /**
     * Crear nuevo mensaje — publico, no requiere identidad.
     */
    @POST("api/contacto")
    suspend fun crearMensaje(
        @Body mensaje: MensajeContactoDTO
    ): Response<MensajeContactoDTO>

    @GET("api/contacto")
    suspend fun listarTodosMensajes(
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<MensajeContactoDTO>>

    @GET("api/contacto/{id}")
    suspend fun obtenerMensajePorId(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Query("includeDetails") includeDetails: Boolean = true
    ): Response<MensajeContactoDTO>

    @GET("api/contacto/email/{email}")
    suspend fun listarMensajesPorEmail(
        @Path("email") email: String,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int
    ): Response<List<MensajeContactoDTO>>

    @GET("api/contacto/usuario/{usuarioId}")
    suspend fun listarMensajesPorUsuario(
        @Path("usuarioId") usuarioId: Long,
        @Header("X-Usuario-Id") headerUsuarioId: Long,
        @Header("X-Rol-Id") rolId: Int
    ): Response<List<MensajeContactoDTO>>

    @GET("api/contacto/estado/{estado}")
    suspend fun listarMensajesPorEstado(
        @Path("estado") estado: String,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int
    ): Response<List<MensajeContactoDTO>>

    @GET("api/contacto/sin-responder")
    suspend fun listarMensajesSinResponder(
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int
    ): Response<List<MensajeContactoDTO>>

    @GET("api/contacto/buscar")
    suspend fun buscarMensajesPorPalabraClave(
        @Query("keyword") keyword: String,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int
    ): Response<List<MensajeContactoDTO>>

    @PATCH("api/contacto/{id}/estado")
    suspend fun actualizarEstadoMensaje(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Query("estado") estado: String
    ): Response<MensajeContactoDTO>

    @POST("api/contacto/{id}/responder")
    suspend fun responderMensaje(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Body respuesta: RespuestaMensajeDTO
    ): Response<MensajeContactoDTO>

    @DELETE("api/contacto/{id}")
    suspend fun eliminarMensaje(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Query("adminId") adminId: Long
    ): Response<Void>

    @GET("api/contacto/estadisticas")
    suspend fun obtenerEstadisticas(
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int
    ): Response<Map<String, Long>>
}