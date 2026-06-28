package com.leaseflow.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leaseflow.app.data.local.dao.CatalogDao
import com.leaseflow.app.data.local.dao.PropiedadDao
import com.leaseflow.app.data.local.entities.PropiedadEntity
import com.leaseflow.app.data.local.storage.UserPreferences
import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.remote.dto.PropertyRemoteDTO
import com.leaseflow.app.data.repository.ApplicationRemoteRepository
import com.leaseflow.app.data.repository.PropertyRemoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class PropiedadConInfo(
    val propiedad: PropiedadEntity,
    val nombreComuna: String?,
    val nombreTipo: String?,
    val propiedadRemota: PropertyRemoteDTO? = null,
    val estadoArriendo: String? = null
)

class MisPropiedadesViewModel(
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao,
    private val propertyRepository: PropertyRemoteRepository,
    private val applicationRepository: ApplicationRemoteRepository,
    private val userPreferences: Flow<UserPreferences>
) : ViewModel() {

    companion object {
        private const val TAG = "MisPropiedadesVM"
    }

    private val _propiedades = MutableStateFlow<List<PropiedadConInfo>>(emptyList())
    val propiedades: StateFlow<List<PropiedadConInfo>> = _propiedades.asStateFlow()

    private val _propiedadesRemotas = MutableStateFlow<List<PropertyRemoteDTO>>(emptyList())
    val propiedadesRemotas: StateFlow<List<PropertyRemoteDTO>> = _propiedadesRemotas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    private val _successMsg = MutableStateFlow<String?>(null)
    val successMsg: StateFlow<String?> = _successMsg.asStateFlow()

    // listarPropiedadesPorUsuario es endpoint protegido
    fun cargarPropiedadesPropietario(propietarioId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null
            val prefs = userPreferences.first()
            val userId = prefs.userId
            val roleId = prefs.userRole

            try {
                Log.d(TAG, "Cargando propiedades del propietario: $propietarioId")

                when (val result = propertyRepository.listarPropiedadesPorUsuario(
                    userId = userId,
                    roleId = roleId,
                    usuarioId = propietarioId,
                    includeDetails = true
                )) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Propiedades del propietario: ${result.data.size}")
                        _propiedadesRemotas.value = result.data

                        val estadoPorPropiedadId = mutableMapOf<Long, String?>()
                        result.data.forEach { dto ->
                            val propId = dto.id
                            if (propId != null) {
                                when (val solResult = applicationRepository.obtenerSolicitudesPorPropiedad(propId)) {
                                    is ApiResult.Success -> {
                                        val arrendada = solResult.data.any { s ->
                                            s.estado?.uppercase() == "ACEPTADA" || s.estado?.uppercase() == "APROBADA"
                                        }
                                        estadoPorPropiedadId[propId] = if (arrendada) "Arrendada" else "Disponible"
                                    }
                                    else -> estadoPorPropiedadId[propId] = null
                                }
                            }
                        }

                        _propiedades.value = result.data.map { dto ->
                            PropiedadConInfo(
                                propiedad = mapRemoteToLocal(dto),
                                nombreComuna = dto.comuna?.nombre,
                                nombreTipo = dto.tipo?.nombre,
                                propiedadRemota = dto,
                                estadoArriendo = dto.id?.let { estadoPorPropiedadId[it] }
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error al cargar propiedades: ${result.message}")
                        _errorMsg.value = result.message
                        cargarPropiedadesLocales(propietarioId)
                    }
                    is ApiResult.Loading -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepcion: ${e.message}", e)
                _errorMsg.value = "Error: ${e.message}"
                cargarPropiedadesLocales(propietarioId)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun cargarPropiedadesLocales(propietarioId: Long) {
        Log.d(TAG, "Cargando propiedades locales del propietario: $propietarioId")
        val propiedadesLocales = propiedadDao.getPropiedadesByPropietario(propietarioId)
        _propiedades.value = propiedadesLocales.map { propiedad ->
            PropiedadConInfo(
                propiedad = propiedad,
                nombreComuna = catalogDao.getComunaById(propiedad.comuna_id)?.nombre,
                nombreTipo = catalogDao.getTipoById(propiedad.tipo_id)?.nombre
            )
        }
    }

    fun eliminarPropiedad(propiedadId: Long, propietarioId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null
            val prefs = userPreferences.first()
            val userId = prefs.userId
            val roleId = prefs.userRole

            try {
                Log.d(TAG, "Eliminando propiedad: $propiedadId")
                when (val result = propertyRepository.eliminarPropiedad(userId, roleId, propiedadId)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Propiedad eliminada exitosamente")
                        _successMsg.value = "Propiedad eliminada"
                        cargarPropiedadesPropietario(propietarioId)
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error al eliminar: ${result.message}")
                        _errorMsg.value = result.message
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepcion: ${e.message}", e)
                _errorMsg.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun limpiarMensajes() {
        _errorMsg.value = null
        _successMsg.value = null
    }

    private fun mapRemoteToLocal(dto: PropertyRemoteDTO): PropiedadEntity {
        return PropiedadEntity(
            id = dto.id ?: 0L,
            codigo = dto.codigo,
            titulo = dto.titulo,
            precio_mensual = dto.precioMensual.toInt(),
            divisa = dto.divisa,
            m2 = dto.m2,
            n_habit = dto.nHabit,
            n_banos = dto.nBanos,
            pet_friendly = dto.petFriendly,
            direccion = dto.direccion,
            fcreacion = System.currentTimeMillis(),
            estado_id = 1L,
            tipo_id = dto.tipoId,
            comuna_id = dto.comunaId,
            propietario_id = dto.propietarioId?.takeIf { it > 0 }
        )
    }
}