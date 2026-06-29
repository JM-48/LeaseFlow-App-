package com.leaseflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leaseflow.app.data.local.storage.UserSessionData
import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.remote.dto.MensajeContactoDTO
import com.leaseflow.app.data.repository.ContactRemoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactViewModel(
    private val contactRepository: ContactRemoteRepository,
    private val userPreferences: Flow<UserSessionData>
) : ViewModel() {

    private val _mensajes = MutableStateFlow<List<MensajeContactoDTO>>(emptyList())
    val mensajes: StateFlow<List<MensajeContactoDTO>> = _mensajes

    private val _mensajeSeleccionado = MutableStateFlow<MensajeContactoDTO?>(null)
    val mensajeSeleccionado: StateFlow<MensajeContactoDTO?> = _mensajeSeleccionado

    private val _estadisticas = MutableStateFlow<Map<String, Long>>(emptyMap())
    val estadisticas: StateFlow<Map<String, Long>> = _estadisticas

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    // crearMensaje es publico y no necesita userId/roleId
    fun crearMensaje(
        nombre: String,
        email: String,
        asunto: String,
        mensaje: String,
        numeroTelefono: String? = null,
        usuarioId: Long? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = contactRepository.crearMensaje(
                nombre = nombre,
                email = email,
                asunto = asunto,
                mensaje = mensaje,
                numeroTelefono = numeroTelefono,
                usuarioId = usuarioId
            )) {
                is ApiResult.Success -> {
                    _successMessage.value = "Mensaje enviado exitosamente. Le responderemos pronto."
                    usuarioId?.let { cargarMensajesPorUsuario(it) }
                }
                is ApiResult.Error -> _errorMessage.value = result.message
                else -> _errorMessage.value = "Error al enviar mensaje"
            }

            _isLoading.value = false
        }
    }

    fun cargarTodosMensajes() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val prefs = userPreferences.first()
            val userId = prefs.userId
            val roleId = prefs.userRole

            when (val result = contactRepository.listarTodosMensajes(userId, roleId, includeDetails = true)) {
                is ApiResult.Success -> _mensajes.value = result.data
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    _mensajes.value = emptyList()
                }
                else -> _errorMessage.value = "Error al cargar mensajes"
            }

            _isLoading.value = false
        }
    }

    fun cargarMensajesPorUsuario(usuarioId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val prefs = userPreferences.first()
            val userId = prefs.userId
            val roleId = prefs.userRole

            when (val result = contactRepository.listarMensajesPorUsuario(userId, roleId, usuarioId)) {
                is ApiResult.Success -> _mensajes.value = result.data
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    _mensajes.value = emptyList()
                }
                else -> _errorMessage.value = "Error al cargar mensajes"
            }

            _isLoading.value = false
        }
    }

    fun cargarMensajesPorEmail(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val prefs = userPreferences.first()
            val userId = prefs.userId
            val roleId = prefs.userRole

            when (val result = contactRepository.listarMensajesPorEmail(userId, roleId, email)) {
                is ApiResult.Success -> _mensajes.value = result.data
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    _mensajes.value = emptyList()
                }
                else -> _errorMessage.value = "Error al cargar mensajes"
            }

            _isLoading.value = false
        }
    }

    fun cargarMensajesPorEstado(estado: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val prefs = userPreferences.first()
            val userId = prefs.userId
            val roleId = prefs.userRole

            when (val result = contactRepository.listarMensajesPorEstado(userId, roleId, estado)) {
                is ApiResult.Success -> _mensajes.value = result.data
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    _mensajes.value = emptyList()
                }
                else -> _errorMessage.value = "Error al cargar mensajes"
            }

            _isLoading.value = false
        }
    }

    fun cargarMensajesSinResponder() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val prefs = userPreferences.first()
            val userId = prefs.userId
            val roleId = prefs.userRole

            when (val result = contactRepository.listarMensajesSinResponder(userId, roleId)) {
                is ApiResult.Success -> _mensajes.value = result.data
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    _mensajes.value = emptyList()
                }
                else -> _errorMessage.value = "Error al cargar mensajes"
            }

            _isLoading.value = false
        }
    }

    fun buscarMensajes(keyword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val prefs = userPreferences.first()
            val userId = prefs.userId
            val roleId = prefs.userRole

            when (val result = contactRepository.buscarMensajes(userId, roleId, keyword)) {
                is ApiResult.Success -> _mensajes.value = result.data
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    _mensajes.value = emptyList()
                }
                else -> _errorMessage.value = "Error al buscar mensajes"
            }

            _isLoading.value = false
        }
    }

    fun cargarMensajePorId(mensajeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val prefs = userPreferences.first()
            val userId = prefs.userId
            val roleId = prefs.userRole

            when (val result = contactRepository.obtenerMensajePorId(userId, roleId, mensajeId, true)) {
                is ApiResult.Success -> _mensajeSeleccionado.value = result.data
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    _mensajeSeleccionado.value = null
                }
                else -> _errorMessage.value = "Error al cargar mensaje"
            }

            _isLoading.value = false
        }
    }

    fun actualizarEstado(mensajeId: Long, nuevoEstado: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val prefs = userPreferences.first()
            val userId = prefs.userId
            val roleId = prefs.userRole

            when (val result = contactRepository.actualizarEstado(userId, roleId, mensajeId, nuevoEstado)) {
                is ApiResult.Success -> {
                    _successMessage.value = "Estado actualizado a $nuevoEstado"
                    _mensajes.update { mensajes -> mensajes.map { if (it.id == mensajeId) result.data else it } }
                    if (_mensajeSeleccionado.value?.id == mensajeId) _mensajeSeleccionado.value = result.data
                }
                is ApiResult.Error -> _errorMessage.value = result.message
                else -> _errorMessage.value = "Error al actualizar estado"
            }

            _isLoading.value = false
        }
    }

    fun responderMensaje(
        mensajeId: Long,
        respuesta: String,
        respondidoPor: Long,
        nuevoEstado: String? = "RESUELTO"
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val prefs = userPreferences.first()
            val userId = prefs.userId
            val roleId = prefs.userRole

            when (val result = contactRepository.responderMensaje(
                userId, roleId, mensajeId, respuesta, respondidoPor, nuevoEstado
            )) {
                is ApiResult.Success -> {
                    _successMessage.value = "Respuesta enviada exitosamente"
                    _mensajes.update { mensajes -> mensajes.map { if (it.id == mensajeId) result.data else it } }
                    _mensajeSeleccionado.value = result.data
                }
                is ApiResult.Error -> _errorMessage.value = result.message
                else -> _errorMessage.value = "Error al enviar respuesta"
            }

            _isLoading.value = false
        }
    }

    fun eliminarMensaje(mensajeId: Long, adminId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val prefs = userPreferences.first()
            val userId = prefs.userId
            val roleId = prefs.userRole

            when (val result = contactRepository.eliminarMensaje(userId, roleId, mensajeId, adminId)) {
                is ApiResult.Success -> {
                    _successMessage.value = "Mensaje eliminado exitosamente"
                    _mensajes.update { it.filter { m -> m.id != mensajeId } }
                    if (_mensajeSeleccionado.value?.id == mensajeId) _mensajeSeleccionado.value = null
                }
                is ApiResult.Error -> _errorMessage.value = result.message
                else -> _errorMessage.value = "Error al eliminar mensaje"
            }

            _isLoading.value = false
        }
    }

    fun cargarEstadisticas() {
        viewModelScope.launch {
            val prefs = userPreferences.first()
            val userId = prefs.userId
            val roleId = prefs.userRole

            when (val result = contactRepository.obtenerEstadisticas(userId, roleId)) {
                is ApiResult.Success -> _estadisticas.value = result.data
                else -> _estadisticas.value = emptyMap()
            }
        }
    }

    fun validarEmail(email: String): Pair<Boolean, String?> {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return when {
            email.isBlank() -> false to "El email es obligatorio"
            !email.matches(emailRegex) -> false to "El email no es valido"
            else -> true to null
        }
    }

    fun validarMensaje(mensaje: String): Pair<Boolean, String?> {
        return when {
            mensaje.isBlank() -> false to "El mensaje es obligatorio"
            mensaje.length < 10 -> false to "El mensaje debe tener al menos 10 caracteres"
            mensaje.length > 5000 -> false to "El mensaje no puede exceder 5000 caracteres"
            else -> true to null
        }
    }

    fun validarAsunto(asunto: String): Pair<Boolean, String?> {
        return when {
            asunto.isBlank() -> false to "El asunto es obligatorio"
            asunto.length > 200 -> false to "El asunto no puede exceder 200 caracteres"
            else -> true to null
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun getMensajesPendientes(): Int = _mensajes.value.count { it.estado == "PENDIENTE" }
    fun getMensajesSinResponder(): Int = _mensajes.value.count { it.respuesta == null }
    fun limpiarMensajeSeleccionado() { _mensajeSeleccionado.value = null }
}
