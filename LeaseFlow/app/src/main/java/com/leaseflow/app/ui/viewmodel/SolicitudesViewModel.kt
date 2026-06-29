package com.leaseflow.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leaseflow.app.data.local.dao.SolicitudDao
import com.leaseflow.app.data.local.dao.PropiedadDao
import com.leaseflow.app.data.local.dao.CatalogDao
import com.leaseflow.app.data.local.entities.SolicitudEntity
import com.leaseflow.app.data.local.storage.UserSessionData
import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.remote.dto.SolicitudArriendoDTO
import com.leaseflow.app.data.repository.ApplicationRemoteRepository
import com.leaseflow.app.data.repository.PropertyRemoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Data class para solicitud con datos enriquecidos
 */
data class SolicitudConDatos(
    val solicitud: SolicitudEntity,
    val tituloPropiedad: String? = null,
    val codigoPropiedad: String? = null,
    val nombreEstado: String? = null,
    val precioMensual: Double? = null,
    val fotoUrl: String? = null,
    val nombreSolicitante: String? = null,
    val emailSolicitante: String? = null,
    val telefonoSolicitante: String? = null,
    val direccionPropiedad: String? = null,
    val solicitudDTO: SolicitudArriendoDTO? = null
)

/**
 * ViewModel para gestion de solicitudes
 */
class SolicitudesViewModel(
    private val solicitudDao: SolicitudDao,
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao,
    private val remoteRepository: ApplicationRemoteRepository,
    private val propertyRepository: PropertyRemoteRepository? = null,
    private val userPreferences: Flow<UserSessionData>
) : ViewModel() {

    companion object {
        private const val TAG = "SolicitudesViewModel"
    }

    private val _solicitudes = MutableStateFlow<List<SolicitudConDatos>>(emptyList())
    val solicitudes: StateFlow<List<SolicitudConDatos>> = _solicitudes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    private val _successMsg = MutableStateFlow<String?>(null)
    val successMsg: StateFlow<String?> = _successMsg.asStateFlow()

    private val _filtroEstado = MutableStateFlow<String?>(null)
    val filtroEstado: StateFlow<String?> = _filtroEstado.asStateFlow()

    private var ultimoModoCargado: (() -> Unit)? = null

    /**
     * Cargar solicitudes del arrendatario (usuario logueado)
     */
    fun cargarSolicitudesArrendatario(usuarioId: Long = 1L) {
        ultimoModoCargado = { cargarSolicitudesArrendatario(usuarioId) }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                val prefs = userPreferences.first()
                val userId = prefs.userId
                val roleId = prefs.userRole

                when (val result = remoteRepository.obtenerSolicitudesUsuario(userId, roleId, usuarioId)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Solicitudes cargadas para arrendatario: ${result.data.size}")

                        val solicitudesMapeadas = result.data.map { dto ->
                            var solicitudEnriquecida = mapearSolicitud(dto)

                            if (solicitudEnriquecida.tituloPropiedad == null && propertyRepository != null) {
                                val propId = solicitudEnriquecida.solicitud.propiedad_id
                                when (val propResult = propertyRepository.obtenerPropiedadPorId(propId)) {
                                    is ApiResult.Success -> {
                                        val propReal = propResult.data
                                        solicitudEnriquecida = solicitudEnriquecida.copy(
                                            tituloPropiedad = propReal.titulo ?: "Propiedad $propId",
                                            codigoPropiedad = propReal.codigo,
                                            direccionPropiedad = propReal.direccion,
                                            precioMensual = propReal.precioMensual,
                                            fotoUrl = propReal.fotos?.firstOrNull()?.url
                                        )
                                    }
                                    else -> Log.w(TAG, "No se pudo obtener detalles de la propiedad $propId")
                                }
                            }
                            solicitudEnriquecida
                        }
                        _solicitudes.value = solicitudesMapeadas
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error remoto: ${result.message}")
                        _errorMsg.value = result.message
                        cargarSolicitudesLocales()
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepcion: ${e.message}", e)
                _errorMsg.value = e.message
                cargarSolicitudesLocales()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cargar solicitudes del propietario
     */
    fun cargarSolicitudesPropietario(propietarioId: Long = 1L) {
        ultimoModoCargado = { cargarSolicitudesPropietario(propietarioId) }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                val prefs = userPreferences.first()
                val userId = prefs.userId
                val roleId = prefs.userRole

                when (val result = remoteRepository.listarTodasSolicitudes(userId, roleId)) {
                    is ApiResult.Success -> {
                        val listaMisSolicitudesRecibidas = mutableListOf<SolicitudConDatos>()

                        for (dto in result.data) {
                            var solicitudEnriquecida = mapearSolicitud(dto)

                            if (propertyRepository != null) {
                                val propId = dto.propiedadId
                                when (val propResult = propertyRepository.obtenerPropiedadPorId(propId)) {
                                    is ApiResult.Success -> {
                                        val propReal = propResult.data
                                        val soyElDueno = (propReal.propietarioId == propietarioId)

                                        if (soyElDueno) {
                                            solicitudEnriquecida = solicitudEnriquecida.copy(
                                                tituloPropiedad = propReal.titulo ?: "Propiedad $propId",
                                                codigoPropiedad = propReal.codigo,
                                                direccionPropiedad = propReal.direccion,
                                                precioMensual = propReal.precioMensual,
                                                fotoUrl = propReal.fotos?.firstOrNull()?.url
                                            )
                                            listaMisSolicitudesRecibidas.add(solicitudEnriquecida)
                                        }
                                    }
                                    else -> Log.w(TAG, "No se pudo obtener detalles de la propiedad $propId")
                                }
                            }
                        }

                        _solicitudes.value = listaMisSolicitudesRecibidas
                    }
                    is ApiResult.Error -> {
                        _errorMsg.value = result.message
                        cargarSolicitudesLocales()
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepcion en Propietario: ${e.message}", e)
                _errorMsg.value = e.message
                cargarSolicitudesLocales()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cargar todas las solicitudes (Admin)
     */
    fun cargarTodasSolicitudes() {
        ultimoModoCargado = { cargarTodasSolicitudes() }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                val prefs = userPreferences.first()
                val userId = prefs.userId
                val roleId = prefs.userRole

                when (val result = remoteRepository.listarTodasSolicitudes(userId, roleId)) {
                    is ApiResult.Success -> {
                        _solicitudes.value = mapearSolicitudes(result.data)
                    }
                    is ApiResult.Error -> {
                        _errorMsg.value = result.message
                        cargarSolicitudesLocales()
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _errorMsg.value = e.message
                cargarSolicitudesLocales()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Crear nueva solicitud
     */
    fun crearSolicitud(usuarioId: Long, propiedadId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                when (val result = remoteRepository.crearSolicitudRemota(usuarioId, propiedadId)) {
                    is ApiResult.Success -> {
                        _successMsg.value = "Solicitud creada exitosamente"
                        cargarSolicitudesArrendatario(usuarioId)
                    }
                    is ApiResult.Error -> {
                        _errorMsg.value = result.message
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _errorMsg.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Aprobar solicitud
     */
    fun aprobarSolicitud(solicitudId: Long) {
        actualizarEstado(solicitudId, "ACEPTADA")
    }

    /**
     * Rechazar solicitud
     */
    fun rechazarSolicitud(solicitudId: Long) {
        actualizarEstado(solicitudId, "RECHAZADA")
    }

    /**
     * Cancelar solicitud por parte del Arrendatario
     */
    fun cancelarSolicitud(solicitudId: Long, usuarioId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null
            try {
                val prefs = userPreferences.first()
                val userId = prefs.userId
                val roleId = prefs.userRole

                when (val result = remoteRepository.cancelarSolicitud(userId, roleId, solicitudId)) {
                    is ApiResult.Success -> {
                        _successMsg.value = "Solicitud cancelada correctamente"
                        cargarSolicitudesArrendatario(usuarioId)
                    }
                    is ApiResult.Error -> {
                        _errorMsg.value = result.message
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _errorMsg.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Seleccionar solicitud por ID para la vista de detalle
     */
    fun seleccionarSolicitud(solicitudId: Long) {
        viewModelScope.launch {
            try {
                val prefs = userPreferences.first()
                val userId = prefs.userId
                val roleId = prefs.userRole

                when (val result = remoteRepository.obtenerSolicitudPorId(userId, roleId, solicitudId)) {
                    is ApiResult.Success -> {
                        val solicitudConDatos = mapearSolicitud(result.data)
                        val listaActual = _solicitudes.value.toMutableList()
                        val index = listaActual.indexOfFirst { it.solicitud.id == solicitudId }
                        if (index >= 0) {
                            listaActual[index] = solicitudConDatos
                        } else {
                            listaActual.add(solicitudConDatos)
                        }
                        _solicitudes.value = listaActual
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error al cargar detalle de solicitud: ${result.message}")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepcion en detalle: ${e.message}")
            }
        }
    }

    /**
     * Actualizar estado dinamico
     */
    private fun actualizarEstado(solicitudId: Long, nuevoEstado: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                val prefs = userPreferences.first()
                val userId = prefs.userId
                val roleId = prefs.userRole

                when (val result = remoteRepository.actualizarEstadoSolicitud(userId, roleId, solicitudId, nuevoEstado)) {
                    is ApiResult.Success -> {
                        _successMsg.value = "Solicitud ${nuevoEstado.lowercase()} con exito"
                        ultimoModoCargado?.invoke() ?: cargarTodasSolicitudes()
                    }
                    is ApiResult.Error -> {
                        _errorMsg.value = result.message
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _errorMsg.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setFiltroEstado(estado: String?) {
        _filtroEstado.value = estado
    }

    fun clearError() { _errorMsg.value = null }
    fun clearSuccess() { _successMsg.value = null }

    private suspend fun cargarSolicitudesLocales() {
        val solicitudesLocales = solicitudDao.getAll().first()
        _solicitudes.value = solicitudesLocales.map { entity ->
            SolicitudConDatos(
                solicitud = entity,
                nombreEstado = mapEstadoIdToNombre(entity.estado_id)
            )
        }
    }

    private fun mapearSolicitudes(dtos: List<SolicitudArriendoDTO>): List<SolicitudConDatos> {
        return dtos.map { dto -> mapearSolicitud(dto) }
    }

    private fun mapearSolicitud(dto: SolicitudArriendoDTO): SolicitudConDatos {
        val nombreUsuario = dto.usuario?.let { u ->
            listOfNotNull(u.pnombre, u.snombre, u.papellido)
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .ifEmpty { "Usuario" }
        }

        val estadoNormalizado = cuandoEstadoSeaPendiente(dto.estado ?: "PENDIENTE")

        return SolicitudConDatos(
            solicitud = SolicitudEntity(
                id = dto.id ?: 0L,
                fsolicitud = dto.fechaSolicitud?.time ?: System.currentTimeMillis(),
                total = 0,
                usuarios_id = dto.usuarioId,
                estado_id = mapEstadoNombreToId(estadoNormalizado),
                propiedad_id = dto.propiedadId
            ),
            tituloPropiedad = dto.propiedad?.titulo,
            codigoPropiedad = dto.propiedad?.codigo,
            nombreEstado = estadoNormalizado,
            precioMensual = dto.propiedad?.precioMensual,
            fotoUrl = dto.propiedad?.fotos?.firstOrNull()?.url,
            nombreSolicitante = nombreUsuario,
            emailSolicitante = dto.usuario?.email,
            telefonoSolicitante = dto.usuario?.ntelefono,
            direccionPropiedad = dto.propiedad?.direccion,
            solicitudDTO = dto
        )
    }

    private fun cuandoEstadoSeaPendiente(estado: String): String {
        return when (estado.uppercase().trim()) {
            "PENDING", "INGRESADA", "CREADA", "PENDIENTE" -> "PENDIENTE"
            "ACEPTADA", "APROBADA", "APPROVED", "ACCEPTED" -> "ACEPTADA"
            "RECHAZADA", "REJECTED", "DECLINED" -> "RECHAZADA"
            else -> estado.uppercase().trim()
        }
    }

    private fun mapEstadoNombreToId(nombre: String): Long {
        return when (nombre.uppercase()) {
            "PENDIENTE" -> 1L
            "ACEPTADA" -> 2L
            "RECHAZADA" -> 3L
            else -> 1L
        }
    }

    private fun mapEstadoIdToNombre(id: Long): String {
        return when (id) {
            1L -> "PENDIENTE"
            2L -> "ACEPTADA"
            3L -> "RECHAZADA"
            else -> "PENDIENTE"
        }
    }
}
