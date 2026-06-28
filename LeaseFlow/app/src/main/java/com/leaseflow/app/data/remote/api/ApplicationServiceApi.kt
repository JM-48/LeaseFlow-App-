package com.leaseflow.app.data.remote.api

import com.leaseflow.app.data.remote.dto.RegistroArriendoDTO
import com.leaseflow.app.data.remote.dto.SolicitudArriendoDTO
import retrofit2.Response
import retrofit2.http.*

/**
 * API para comunicacion con Application Service (Puerto 8084)
 *
 * Capas de seguridad:
 *   Capa 1 (X-App-Client): interceptor global OkHttp en RetrofitClient.
 *   Capa 2 (X-Usuario-Id / X-Rol-Id): requerida en todos los endpoints
 *   excepto POST /api/solicitudes (la creacion de solicitud no lleva headers
 *   de identidad en el controller — el backend los obtiene del body).
 *
 * Nota: crearSolicitud y crearRegistro no llevan @Header de identidad porque
 * el backend los lee del body (usuarioId en el DTO), no del header.
 */
interface ApplicationServiceApi {

    // ==================== SOLICITUDES ====================

    /**
     * Crear nueva solicitud de arriendo
     * POST /api/solicitudes
     * El backend obtiene usuarioId del body, no exige headers de identidad aqui.
     */
    @POST("api/solicitudes")
    suspend fun crearSolicitud(
        @Body solicitud: SolicitudArriendoDTO
    ): Response<SolicitudArriendoDTO>

    /**
     * Listar todas las solicitudes (protegido)
     * GET /api/solicitudes
     */
    @GET("api/solicitudes")
    suspend fun listarTodasSolicitudes(
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<SolicitudArriendoDTO>>

    /**
     * Obtener solicitud por ID (protegido)
     * GET /api/solicitudes/{id}
     */
    @GET("api/solicitudes/{id}")
    suspend fun obtenerSolicitudPorId(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Query("includeDetails") includeDetails: Boolean = true
    ): Response<SolicitudArriendoDTO>

    /**
     * Obtener solicitudes por usuario (protegido)
     * GET /api/solicitudes/usuario/{usuarioId}
     */
    @GET("api/solicitudes/usuario/{usuarioId}")
    suspend fun obtenerSolicitudesPorUsuario(
        @Path("usuarioId") usuarioId: Long,
        @Header("X-Usuario-Id") headerUsuarioId: Long,
        @Header("X-Rol-Id") rolId: Int
    ): Response<List<SolicitudArriendoDTO>>

    /**
     * Obtener solicitudes por propiedad (protegido)
     * GET /api/solicitudes/propiedad/{propiedadId}
     */
    @GET("api/solicitudes/propiedad/{propiedadId}")
    suspend fun obtenerSolicitudesPorPropiedad(
        @Path("propiedadId") propiedadId: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int
    ): Response<List<SolicitudArriendoDTO>>

    /**
     * Actualizar estado de solicitud (protegido)
     * PATCH /api/solicitudes/{id}/estado
     */
    @PATCH("api/solicitudes/{id}/estado")
    suspend fun actualizarEstadoSolicitud(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Query("estado") estado: String
    ): Response<SolicitudArriendoDTO>

    @DELETE("api/solicitudes/{id}")
    suspend fun eliminarSolicitud(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int
    ): Response<Void>

    // ==================== REGISTROS ====================

    /**
     * Crear nuevo registro de arriendo
     * POST /api/registros
     * No lleva headers de identidad — el controller no los exige para POST.
     */
    @POST("api/registros")
    suspend fun crearRegistro(
        @Body registro: RegistroArriendoDTO
    ): Response<RegistroArriendoDTO>

    /**
     * Listar todos los registros (protegido)
     * GET /api/registros
     */
    @GET("api/registros")
    suspend fun listarTodosRegistros(
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<RegistroArriendoDTO>>

    /**
     * Obtener registro por ID (protegido)
     * GET /api/registros/{id}
     */
    @GET("api/registros/{id}")
    suspend fun obtenerRegistroPorId(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Query("includeDetails") includeDetails: Boolean = true
    ): Response<RegistroArriendoDTO>

    /**
     * Obtener registros por solicitud (protegido)
     * GET /api/registros/solicitud/{solicitudId}
     */
    @GET("api/registros/solicitud/{solicitudId}")
    suspend fun obtenerRegistrosPorSolicitud(
        @Path("solicitudId") solicitudId: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int
    ): Response<List<RegistroArriendoDTO>>

    /**
     * Finalizar registro (protegido)
     * PATCH /api/registros/{id}/finalizar
     */
    @PATCH("api/registros/{id}/finalizar")
    suspend fun finalizarRegistro(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int
    ): Response<RegistroArriendoDTO>
}