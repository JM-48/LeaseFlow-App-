package com.leaseflow.app.data.remote.api

import com.leaseflow.app.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * API para comunicacion con Property Service (Puerto 8082)
 *
 * Capas de seguridad:
 *   Capa 1 (X-App-Client): interceptor global OkHttp en RetrofitClient.
 *   Capa 2 (X-Usuario-Id / X-Rol-Id): requerida en endpoints de escritura y admin.
 *
 * Publicos (sin headers de identidad):
 *   - listarTodasPropiedades, obtenerPropiedadPorId, obtenerPropiedadPorCodigo
 *   - buscarPropiedadesConFiltros, existePropiedad, listarFotos, obtenerFoto
 *   - listarTipos, obtenerTipoPorId, listarComunas, obtenerComunaPorId,
 *     obtenerComunasPorRegion, listarRegiones, obtenerRegionPorId,
 *     listarCategorias, obtenerCategoriaPorId
 *
 * Protegidos (requieren headers de identidad):
 *   - crearPropiedad, actualizarPropiedad, eliminarPropiedad
 *   - listarPropiedadesPorUsuario
 *   - subirFoto, eliminarFoto, reordenarFotos
 *   - crearTipo, actualizarTipo, eliminarTipo
 *   - crearComuna, actualizarComuna, eliminarComuna
 *   - crearRegion, actualizarRegion, eliminarRegion
 *   - crearCategoria, actualizarCategoria, eliminarCategoria
 */
interface PropertyServiceApi {

    // ==================== PROPIEDADES ====================

    @POST("api/propiedades")
    suspend fun crearPropiedad(
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Body propiedad: PropertyRemoteDTO
    ): Response<PropertyRemoteDTO>

    @GET("api/propiedades")
    suspend fun listarTodasPropiedades(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100,
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<PageResponse<PropertyRemoteDTO>>

    @GET("api/propiedades/usuario/{usuarioId}")
    suspend fun listarPropiedadesPorUsuario(
        @Path("usuarioId") usuarioId: Long,
        @Header("X-Usuario-Id") headerUsuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<PropertyRemoteDTO>>

    @GET("api/propiedades/{id}")
    suspend fun obtenerPropiedadPorId(
        @Path("id") id: Long,
        @Query("includeDetails") includeDetails: Boolean = true
    ): Response<PropertyRemoteDTO>

    @GET("api/propiedades/codigo/{codigo}")
    suspend fun obtenerPropiedadPorCodigo(
        @Path("codigo") codigo: String,
        @Query("includeDetails") includeDetails: Boolean = true
    ): Response<PropertyRemoteDTO>

    @PUT("api/propiedades/{id}")
    suspend fun actualizarPropiedad(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Body propiedad: PropertyRemoteDTO
    ): Response<PropertyRemoteDTO>

    @DELETE("api/propiedades/{id}")
    suspend fun eliminarPropiedad(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int
    ): Response<Void>

    @GET("api/propiedades/buscar")
    suspend fun buscarPropiedadesConFiltros(
        @Query("tipoId") tipoId: Long? = null,
        @Query("comunaId") comunaId: Long? = null,
        @Query("minPrecio") minPrecio: Double? = null,
        @Query("maxPrecio") maxPrecio: Double? = null,
        @Query("nHabit") nHabit: Int? = null,
        @Query("nBanos") nBanos: Int? = null,
        @Query("petFriendly") petFriendly: Boolean? = null,
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<PropertyRemoteDTO>>

    @GET("api/propiedades/{id}/existe")
    suspend fun existePropiedad(
        @Path("id") id: Long
    ): Response<Boolean>

    // ==================== FOTOS ====================

    @Multipart
    @POST("api/propiedades/{id}/fotos")
    suspend fun subirFoto(
        @Path("id") propertyId: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Part file: MultipartBody.Part
    ): Response<FotoRemoteDTO>

    @GET("api/propiedades/{id}/fotos")
    suspend fun listarFotos(
        @Path("id") propertyId: Long
    ): Response<List<FotoRemoteDTO>>

    @GET("api/fotos/{fotoId}")
    suspend fun obtenerFoto(
        @Path("fotoId") fotoId: Long
    ): Response<FotoRemoteDTO>

    @DELETE("api/fotos/{fotoId}")
    suspend fun eliminarFoto(
        @Path("fotoId") fotoId: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int
    ): Response<Void>

    @PUT("api/propiedades/{id}/fotos/reordenar")
    suspend fun reordenarFotos(
        @Path("id") propertyId: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Body fotosIds: List<Long>
    ): Response<Void>

    // ==================== TIPOS (catalogo) ====================

    @POST("api/tipos")
    suspend fun crearTipo(
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Body tipo: TipoRemoteDTO
    ): Response<TipoRemoteDTO>

    @GET("api/tipos")
    suspend fun listarTipos(): Response<List<TipoRemoteDTO>>

    @GET("api/tipos/{id}")
    suspend fun obtenerTipoPorId(
        @Path("id") id: Long
    ): Response<TipoRemoteDTO>

    @PUT("api/tipos/{id}")
    suspend fun actualizarTipo(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Body tipo: TipoRemoteDTO
    ): Response<TipoRemoteDTO>

    @DELETE("api/tipos/{id}")
    suspend fun eliminarTipo(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int
    ): Response<Void>

    // ==================== COMUNAS (catalogo) ====================

    @POST("api/comunas")
    suspend fun crearComuna(
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Body comuna: ComunaRemoteDTO
    ): Response<ComunaRemoteDTO>

    @GET("api/comunas")
    suspend fun listarComunas(): Response<List<ComunaRemoteDTO>>

    @GET("api/comunas/{id}")
    suspend fun obtenerComunaPorId(
        @Path("id") id: Long
    ): Response<ComunaRemoteDTO>

    @GET("api/comunas/region/{regionId}")
    suspend fun obtenerComunasPorRegion(
        @Path("regionId") regionId: Long
    ): Response<List<ComunaRemoteDTO>>

    @PUT("api/comunas/{id}")
    suspend fun actualizarComuna(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Body comuna: ComunaRemoteDTO
    ): Response<ComunaRemoteDTO>

    @DELETE("api/comunas/{id}")
    suspend fun eliminarComuna(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int
    ): Response<Void>

    // ==================== REGIONES (catalogo) ====================

    @POST("api/regiones")
    suspend fun crearRegion(
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Body region: RegionRemoteDTO
    ): Response<RegionRemoteDTO>

    @GET("api/regiones")
    suspend fun listarRegiones(): Response<List<RegionRemoteDTO>>

    @GET("api/regiones/{id}")
    suspend fun obtenerRegionPorId(
        @Path("id") id: Long
    ): Response<RegionRemoteDTO>

    @PUT("api/regiones/{id}")
    suspend fun actualizarRegion(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Body region: RegionRemoteDTO
    ): Response<RegionRemoteDTO>

    @DELETE("api/regiones/{id}")
    suspend fun eliminarRegion(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int
    ): Response<Void>

    // ==================== CATEGORIAS (catalogo) ====================

    @POST("api/categorias")
    suspend fun crearCategoria(
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Body categoria: CategoriaRemoteDTO
    ): Response<CategoriaRemoteDTO>

    @GET("api/categorias")
    suspend fun listarCategorias(): Response<List<CategoriaRemoteDTO>>

    @GET("api/categorias/{id}")
    suspend fun obtenerCategoriaPorId(
        @Path("id") id: Long
    ): Response<CategoriaRemoteDTO>

    @PUT("api/categorias/{id}")
    suspend fun actualizarCategoria(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int,
        @Body categoria: CategoriaRemoteDTO
    ): Response<CategoriaRemoteDTO>

    @DELETE("api/categorias/{id}")
    suspend fun eliminarCategoria(
        @Path("id") id: Long,
        @Header("X-Usuario-Id") usuarioId: Long,
        @Header("X-Rol-Id") rolId: Int
    ): Response<Void>
}