package com.leaseflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leaseflow.app.data.local.dao.UsuarioDao
import com.leaseflow.app.data.local.dao.CatalogDao
import com.leaseflow.app.data.local.dao.SolicitudDao
import com.leaseflow.app.data.local.entities.UsuarioEntity
import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.remote.dto.UsuarioUpdateRemoteDTO
import com.leaseflow.app.data.repository.LeaseFlowUserRepository
import com.leaseflow.app.data.repository.UserRemoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PerfilUsuarioViewModel(
    private val usuarioDao: UsuarioDao,
    private val catalogDao: CatalogDao,
    private val solicitudDao: SolicitudDao,
    private val userRemoteRepository: UserRemoteRepository,
    private val localUserRepository: LeaseFlowUserRepository
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
                // Carga inicial rápida de Room (datos antiguos)
                val localUser = usuarioDao.getById(usuarioId)
                _usuario.value = localUser

                // Vamos al servidor por la verdad absoluta
                when (val remoteResult = userRemoteRepository.obtenerUsuarioPorId(usuarioId, includeDetails = true)) {
                    is ApiResult.Success -> {
                        // Intentamos sincronizar en segundo plano
                        localUserRepository.syncUsuarioFromRemote(remoteResult.data)

                        // FORZAMOS a la app a mostrar los datos reales del servidor inmediatamente
                        _usuario.value = UsuarioEntity(
                            id = remoteResult.data.id ?: usuarioId,
                            pnombre = remoteResult.data.pnombre ?: "Admin",
                            snombre = remoteResult.data.snombre ?: "Sistema",
                            papellido = remoteResult.data.papellido ?: "LeaseFlowx",
                            fnacimiento = System.currentTimeMillis(),
                            email = remoteResult.data.email ?: "admin@leaseflow.cl",
                            rut = remoteResult.data.rut ?: "19430962-7", // <-- Aquí capturamos el RUT remoto
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
                val updateDto = UsuarioUpdateRemoteDTO(
                    pnombre = pnombre,
                    snombre = snombre,
                    papellido = papellido,
                    email = _usuario.value?.email ?: "",
                    ntelefono = telefono,
                    rolId = _usuario.value?.rol_id,
                    estadoId = _usuario.value?.estado_id
                )

                when (val result = userRemoteRepository.actualizarUsuario(usuarioId, updateDto)) {
                    is ApiResult.Success -> {
                        // 1. Dejamos que se sincronice Room en segundo plano
                        localUserRepository.syncUsuarioFromRemote(result.data)

                        // 2. SOLUCIÓN: Actualizamos el estado de la RAM inmediatamente
                        // con los datos que el usuario escribió y que ya fueron aceptados por el servidor
                        _usuario.value = _usuario.value?.copy(
                            pnombre = pnombre,
                            snombre = snombre,
                            papellido = papellido,
                            ntelefono = telefono,
                            fotoPerfil = fotoUri ?: _usuario.value?.fotoPerfil
                        )

                        // 3. Si se subió una foto local, la aseguramos en la BD
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
