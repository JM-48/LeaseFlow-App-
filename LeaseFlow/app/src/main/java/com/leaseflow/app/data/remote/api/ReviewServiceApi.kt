package com.leaseflow.app.data.remote.api

import com.leaseflow.app.data.remote.dto.ResenaDTO
import com.leaseflow.app.data.remote.dto.TipoResenaDTO
import retrofit2.Response
import retrofit2.http.*

/**
 * API para comunicacion con Review Service (Puerto 8086)
 *
 * Capas de seguridad:
 *   Capa 1 (X-App-Client): interceptor global OkHttp en RetrofitClient.
 *   Capa 2 (X-Usuario-Id / X-Rol-Id): requerida en endpoints de escritura y admin.
 *
 * Publicos (sin headers de identidad):
 *   - listarTodasResenas, obtenerResenaPorId
 *   - obtenerResenasPorPropiedad, obtenerResenasSobreUsuario
 *   - calcularPromedioPorPropiedad, calcularPromedioPorUsuario
 *   - listarTiposResena, obtenerTipoResenaPorId
 *
 * Protegidos (requieren headers de identidad):
 *   - crearResena, obtenerResenasPorUsuario (lectura propia)
 *   - actualizarEstadoResena, eliminarResena (admin)
 *   - crearTipoResena, actualizarTipoResena, eliminarTipoResena (admin)
 */
interface ReviewServiceApi {

    // ==================== RESEÑAS ====================

    @POST("api/reviews")
    suspend fun crearResena(
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Body resena: ResenaDTO
    ): Response<ResenaDTO>

    @GET("api/reviews")
    suspend fun listarTodasResenas(
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<ResenaDTO>>

    @GET("api/reviews/{id}")
    suspend fun obtenerResenaPorId(
        @Path("id") id: Long,
        @Query("includeDetails") includeDetails: Boolean = true
    ): Response<ResenaDTO>

    @GET("api/reviews/usuario/{usuarioId}")
    suspend fun obtenerResenasPorUsuario(
        @Path("usuarioId") usuarioId: Long,
        @Header("X-Usuario-Id") headerUsuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<ResenaDTO>>

    @GET("api/reviews/propiedad/{propiedadId}")
    suspend fun obtenerResenasPorPropiedad(
        @Path("propiedadId") propiedadId: Long,
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<ResenaDTO>>

    @GET("api/reviews/usuario-resenado/{usuarioResenadoId}")
    suspend fun obtenerResenasSobreUsuario(
        @Path("usuarioResenadoId") usuarioResenadoId: Long,
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<ResenaDTO>>

    @GET("api/reviews/propiedad/{propiedadId}/promedio")
    suspend fun calcularPromedioPorPropiedad(
        @Path("propiedadId") propiedadId: Long
    ): Response<Double>

    @GET("api/reviews/usuario-resenado/{usuarioResenadoId}/promedio")
    suspend fun calcularPromedioPorUsuario(
        @Path("usuarioResenadoId") usuarioResenadoId: Long
    ): Response<Double>

    @PATCH("api/reviews/{id}/estado")
    suspend fun actualizarEstadoResena(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Query("estado") estado: String
    ): Response<ResenaDTO>

    @DELETE("api/reviews/{id}")
    suspend fun eliminarResena(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int
    ): Response<Void>

    // ==================== TIPOS DE RESEÑA ====================

    @GET("api/tipo-resenas")
    suspend fun listarTiposResena(): Response<List<TipoResenaDTO>>

    @GET("api/tipo-resenas/{id}")
    suspend fun obtenerTipoResenaPorId(
        @Path("id") id: Long
    ): Response<TipoResenaDTO>

    @POST("api/tipo-resenas")
    suspend fun crearTipoResena(
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Body tipoResena: TipoResenaDTO
    ): Response<TipoResenaDTO>

    @PUT("api/tipo-resenas/{id}")
    suspend fun actualizarTipoResena(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Body tipoResena: TipoResenaDTO
    ): Response<TipoResenaDTO>

    @DELETE("api/tipo-resenas/{id}")
    suspend fun eliminarTipoResena(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int
    ): Response<Void>
}