package com.leaseflow.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leaseflow.app.data.local.storage.UserPreferences
import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.remote.dto.PropertyRemoteDTO
import com.leaseflow.app.data.repository.PropertyRemoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GestionPropiedadesViewModel(
    private val propertyRepository: PropertyRemoteRepository,
    private val userPreferences: Flow<UserPreferences>
) : ViewModel() {

    companion object {
        private const val TAG = "GestionPropiedadesVM"
    }

    private val _propiedades = MutableStateFlow<List<PropertyRemoteDTO>>(emptyList())
    val propiedades: StateFlow<List<PropertyRemoteDTO>> = _propiedades.asStateFlow()

    private val _propiedadesFiltradas = MutableStateFlow<List<PropertyRemoteDTO>>(emptyList())
    val propiedadesFiltradas: StateFlow<List<PropertyRemoteDTO>> = _propiedadesFiltradas.asStateFlow()

    private val _propiedadSeleccionada = MutableStateFlow<PropertyRemoteDTO?>(null)
    val propiedadSeleccionada: StateFlow<PropertyRemoteDTO?> = _propiedadSeleccionada.asStateFlow()

    private val _filtroEstado = MutableStateFlow<String?>(null)
    val filtroEstado: StateFlow<String?> = _filtroEstado.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    private val _successMsg = MutableStateFlow<String?>(null)
    val successMsg: StateFlow<String?> = _successMsg.asStateFlow()

    init {
        cargarPropiedades()
    }

    // Lectura pública — no necesita headers de identidad
    fun cargarPropiedades() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null
            try {
                Log.d(TAG, "Cargando todas las propiedades...")
                when (val result = propertyRepository.listarTodasPropiedades(includeDetails = true)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Propiedades cargadas: ${result.data.size}")
                        _propiedades.value = result.data
                        aplicarFiltro()
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error al cargar propiedades: ${result.message}")
                        _errorMsg.value = result.message
                    }
                    is ApiResult.Loading -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepcion: ${e.message}", e)
                _errorMsg.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setFiltroEstado(estado: String?) {
        _filtroEstado.value = estado
        aplicarFiltro()
    }

    private fun aplicarFiltro() {
        val filtro = _filtroEstado.value
        _propiedadesFiltradas.value = if (filtro.isNullOrEmpty()) _propiedades.value else _propiedades.value
    }

    // Lectura pública
    fun seleccionarPropiedad(propiedadId: Long) {
        viewModelScope.launch {
            when (val result = propertyRepository.obtenerPropiedadPorId(propiedadId, includeDetails = true)) {
                is ApiResult.Success -> _propiedadSeleccionada.value = result.data
                is ApiResult.Error -> _errorMsg.value = result.message
                else -> {}
            }
        }
    }

    fun limpiarSeleccion() {
        _propiedadSeleccionada.value = null
    }

    fun aprobarPropiedad(propiedadId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Aprobando propiedad: $propiedadId")
                _successMsg.value = "Propiedad aprobada"
                cargarPropiedades()
            } catch (e: Exception) {
                _errorMsg.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun rechazarPropiedad(propiedadId: Long, motivo: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Rechazando propiedad: $propiedadId con motivo: $motivo")
                _successMsg.value = "Propiedad rechazada"
                cargarPropiedades()
            } catch (e: Exception) {
                _errorMsg.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun eliminarPropiedad(propiedadId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Eliminando propiedad: $propiedadId")
                val prefs = userPreferences.first()
                val userId = prefs.userId
                val roleId = prefs.userRole

                when (val result = propertyRepository.eliminarPropiedad(userId, roleId, propiedadId)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Propiedad eliminada exitosamente")
                        _successMsg.value = "Propiedad eliminada"
                        cargarPropiedades()
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error al eliminar: ${result.message}")
                        _errorMsg.value = result.message
                    }
                    else -> {}
                }
            } catch (e: Exception) {
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
}