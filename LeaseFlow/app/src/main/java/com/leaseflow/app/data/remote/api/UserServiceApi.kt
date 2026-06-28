package com.leaseflow.app.data.remote.api

import com.leaseflow.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API para comunicacion con User Service (Puerto 8081)
 *
 * Capas de seguridad:
 *   Capa 1 (X-App-Client): inyectada globalmente por el interceptor OkHttp en RetrofitClient.
 *   Capa 2 (X-Usuario-Id / X-Rol-Id): requerida en endpoints protegidos (todo salvo login/registro
 *   y catálogos de solo lectura como roles y estados).
 */
interface UserServiceApi {

    // ==================== AUTENTICACION (publicos — sin headers de identidad) ====================

    @POST("api/usuarios")
    suspend fun registrarUsuario(
        @Body usuario: UsuarioRemoteDTO
    ): Response<UsuarioRemoteDTO>

    @POST("api/usuarios/login")
    suspend fun login(
        @Body loginDTO: LoginRemoteDTO
    ): Response<LoginResponseRemoteDTO>

    // ==================== USUARIOS (protegidos) ====================

    @GET("api/usuarios")
    suspend fun obtenerTodosUsuarios(
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<UsuarioRemoteDTO>>

    @GET("api/usuarios/{id}")
    suspend fun obtenerUsuarioPorId(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Query("includeDetails") includeDetails: Boolean = true
    ): Response<UsuarioRemoteDTO>

    @GET("api/usuarios/email/{email}")
    suspend fun obtenerUsuarioPorEmail(
        @Path("email") email: String,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Query("includeDetails") includeDetails: Boolean = true
    ): Response<UsuarioRemoteDTO>

    @GET("api/usuarios/rol/{rolId}")
    suspend fun obtenerUsuariosPorRol(
        @Path("rolId") rolId: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") headerRolId: Int,
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<UsuarioRemoteDTO>>

    @GET("api/usuarios/vip")
    suspend fun obtenerUsuariosVIP(
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<UsuarioRemoteDTO>>

    @PUT("api/usuarios/{id}")
    suspend fun actualizarUsuario(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Body usuario: UsuarioUpdateRemoteDTO
    ): Response<UsuarioRemoteDTO>

    @PATCH("api/usuarios/{id}/rol")
    suspend fun cambiarRol(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Query("rolId") nuevoRolId: Long
    ): Response<UsuarioRemoteDTO>

    @PATCH("api/usuarios/{id}/estado")
    suspend fun cambiarEstado(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Query("estadoId") estadoId: Long
    ): Response<UsuarioRemoteDTO>

    @DELETE("api/usuarios/{id}")
    suspend fun eliminarUsuario(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int
    ): Response<Void>

    @PATCH("api/usuarios/{id}/puntos")
    suspend fun agregarPuntos(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Query("puntos") puntos: Int
    ): Response<UsuarioRemoteDTO>

    @GET("api/usuarios/{id}/exists")
    suspend fun existeUsuario(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int
    ): Response<Boolean>

    // ==================== ROLES (publicos — catalogo de solo lectura) ====================

    @GET("api/roles")
    suspend fun obtenerTodosRoles(): Response<List<RolRemoteDTO>>

    @GET("api/roles/{id}")
    suspend fun obtenerRolPorId(
        @Path("id") id: Long
    ): Response<RolRemoteDTO>

    @GET("api/roles/nombre/{nombre}")
    suspend fun obtenerRolPorNombre(
        @Path("nombre") nombre: String
    ): Response<RolRemoteDTO>

    // ==================== ESTADOS (publicos — catalogo de solo lectura) ====================

    @GET("api/estados")
    suspend fun obtenerTodosEstados(): Response<List<EstadoRemoteDTO>>

    @GET("api/estados/{id}")
    suspend fun obtenerEstadoPorId(
        @Path("id") id: Long
    ): Response<EstadoRemoteDTO>

    @GET("api/estados/nombre/{nombre}")
    suspend fun obtenerEstadoPorNombre(
        @Path("nombre") nombre: String
    ): Response<EstadoRemoteDTO>
}