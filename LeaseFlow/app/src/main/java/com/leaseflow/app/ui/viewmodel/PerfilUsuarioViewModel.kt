package com.leaseflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leaseflow.app.data.local.dao.UsuarioDao
import com.leaseflow.app.data.local.dao.CatalogDao
import com.leaseflow.app.data.local.dao.SolicitudDao
import com.leaseflow.app.data.local.entities.UsuarioEntity
import com.leaseflow.app.data.local.storage.UserPreferences
import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.remote.dto.UsuarioUpdateRemoteDTO
import com.leaseflow.app.data.repository.LeaseFlowUserRepository
import com.leaseflow.app.data.repository.UserRemoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PerfilUsuarioViewModel(
    private val usuarioDao: UsuarioDao,
    private val catalogDao: CatalogDao,
    private val solicitudDao: SolicitudDao,
    private val userRemoteRepository: UserRemoteRepository,
    private val localUserRepository: LeaseFlowUserRepository,
    private val userPreferences: Flow<UserPreferences>
) : ViewModel() {

    private val _usuario = MutableStateFlow<UsuarioEntity?>(null)
    val usuario: StateFlow<UsuarioEntity?> = _usuario

    private val _nombreRol = MutableStateFlow<String?>(null)
    val nombreRol: StateFlow<String?> = _nombreRol

    private val _cantidadSolicitudes = MutableStateFlow(0)
    val cantidadSolicitudes: StateFlow<Int> = _cantidadSolicitudes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg

    fun cargarDatosUsuario(usuarioId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                val localUser = usuarioDao.getById(usuarioId)
                _usuario.value = localUser

                val prefs = userPreferences.first()
                val userId = prefs.userId
                val roleId = prefs.userRole

                when (val remoteResult = userRemoteRepository.obtenerUsuarioPorId(userId, roleId, usuarioId, includeDetails = true)) {
                    is ApiResult.Success -> {
                        localUserRepository.syncUsuarioFromRemote(remoteResult.data)

                        _usuario.value = UsuarioEntity(
                            id = remoteResult.data.id ?: usuarioId,
                            pnombre = remoteResult.data.pnombre ?: "Admin",
                            snombre = remoteResult.data.snombre ?: "Sistema",
                            papellido = remoteResult.data.papellido ?: "LeaseFlowx",
                            fnacimiento = System.currentTimeMillis(),
                            email = remoteResult.data.email ?: "admin@leaseflow.cl",
                            rut = remoteResult.data.rut ?: "19430962-7",
                            ntelefono = remoteResult.data.ntelefono ?: "+56911111112",
                            direccion = null,
                            comuna = null,
                            fotoPerfil = _usuario.value?.fotoPerfil,
                            clave = "",
                            duoc_vip = remoteResult.data.duocVip ?: false,
                            puntos = remoteResult.data.puntos ?: 0,
                            codigo_ref = remoteResult.data.codigoRef ?: "",
                            fcreacion = System.currentTimeMillis(),
                            factualizacion = System.currentTimeMillis(),
                            estado_id = remoteResult.data.estadoId ?: 1L,
                            rol_id = remoteResult.data.rolId
                        )
                    }
                    is ApiResult.Error -> {
                        if (localUser == null) {
                            _errorMsg.value = remoteResult.message
                        }
                    }
                    is ApiResult.Loading -> {}
                }

                _usuario.value?.rol_id?.let { rolId ->
                    val rol = catalogDao.getRolById(rolId)
                    _nombreRol.value = rol?.nombre
                }

            } catch (e: Exception) {
                _errorMsg.value = e.message ?: "Error al cargar perfil"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun actualizarPerfil(
        usuarioId: Long,
        pnombre: String,
        snombre: String,
        papellido: String,
        telefono: String,
        fotoUri: String? = null
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMsg.value = null
            try {
                val prefs = userPreferences.first()
                val userId = prefs.userId
                val roleId = prefs.userRole

                val updateDto = UsuarioUpdateRemoteDTO(
                    pnombre = pnombre,
                    snombre = snombre,
                    papellido = papellido,
                    email = _usuario.value?.email ?: "",
                    ntelefono = telefono,
                    rolId = _usuario.value?.rol_id,
                    estadoId = _usuario.value?.estado_id
                )

                when (val result = userRemoteRepository.actualizarUsuario(userId, roleId, usuarioId, updateDto)) {
                    is ApiResult.Success -> {
                        localUserRepository.syncUsuarioFromRemote(result.data)

                        _usuario.value = _usuario.value?.copy(
                            pnombre = pnombre,
                            snombre = snombre,
                            papellido = papellido,
                            ntelefono = telefono,
                            fotoPerfil = fotoUri ?: _usuario.value?.fotoPerfil
                        )

                        if (fotoUri != null) {
                            _usuario.value?.let { usuarioDao.update(it) }
                        }
                    }
                    is ApiResult.Error -> _errorMsg.value = result.message
                    is ApiResult.Loading -> {}
                }
            } catch (e: Exception) {
                _errorMsg.value = e.message ?: "Error al actualizar perfil"
            } finally {
                _isSaving.value = false
            }
        }
    }
}