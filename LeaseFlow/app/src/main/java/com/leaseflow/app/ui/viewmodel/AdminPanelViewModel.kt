package com.leaseflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.leaseflow.app.data.local.storage.UserSessionData
import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.repository.ApplicationRemoteRepository
import com.leaseflow.app.data.repository.PropertyRemoteRepository
import com.leaseflow.app.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Data class para estadísticas del sistema
 */
data class EstadisticasSistema(
    val totalUsuarios: Int = 0,
    val totalPropiedades: Int = 0,
    val propiedadesActivas: Int = 0,
    val totalSolicitudes: Int = 0
)

/**
 * ViewModel para el Panel de Administración
 */
class AdminPanelViewModel(
    private val userRepository: UserRepository,
    private val propertyRepository: PropertyRemoteRepository,
    private val applicationRepository: ApplicationRemoteRepository,
    private val userPreferences: Flow<UserSessionData>
) : ViewModel() {

    private val _estadisticas = MutableStateFlow(EstadisticasSistema())
    val estadisticas: StateFlow<EstadisticasSistema> = _estadisticas

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Carga las estadísticas generales del sistema
     */
    fun cargarEstadisticas() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val session = userPreferences.first()
                val totalUsuarios = when (val usersResult = userRepository.getUsers(session.userId, session.userRole)) {
                    is ApiResult.Success -> usersResult.data.size
                    else -> 0
                }

                val propiedades = when (val propResult = propertyRepository.listarTodasPropiedades(includeDetails = false)) {
                    is ApiResult.Success -> propResult.data.content
                    else -> emptyList()
                }

                val solicitudes = when (val solResult = applicationRepository.listarTodasSolicitudes(session.userId, session.userRole, includeDetails = false)) {
                    is ApiResult.Success -> solResult.data
                    else -> emptyList()
                }

                val totalPropiedades = propiedades.size
                val totalSolicitudes = solicitudes.size

                val estadosActivos = setOf("ACEPTADA", "APROBADA", "VIGENTE")
                val propiedadesActivas = solicitudes
                    .filter { dto -> dto.estado?.uppercase() in estadosActivos }
                    .map { dto -> dto.propiedadId }
                    .toSet()
                    .size

                _estadisticas.value = EstadisticasSistema(
                    totalUsuarios = totalUsuarios,
                    totalPropiedades = totalPropiedades,
                    propiedadesActivas = propiedadesActivas,
                    totalSolicitudes = totalSolicitudes
                )
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }
}

/**
 * Factory para AdminPanelViewModel
 */
class AdminPanelViewModelFactory(
    private val userRepository: UserRepository,
    private val propertyRepository: PropertyRemoteRepository,
    private val applicationRepository: ApplicationRemoteRepository,
    private val userPreferences: Flow<UserSessionData>
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminPanelViewModel::class.java)) {
            return AdminPanelViewModel(userRepository, propertyRepository, applicationRepository, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
