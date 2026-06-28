package com.leaseflow.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leaseflow.app.data.local.storage.UserPreferences
import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.remote.dto.DocumentoRemoteDTO
import com.leaseflow.app.data.repository.DocumentRemoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FiltroEstadoDocumento(val displayName: String, val estadoId: Long?) {
    TODOS("Todos", null),
    PENDIENTE("Pendientes", 1L),
    EN_REVISION("En Revision", 4L),
    ACEPTADO("Aprobados", 2L),
    RECHAZADO("Rechazados", 3L)
}

data class GestionDocumentosUiState(
    val documentos: List<DocumentoRemoteDTO> = emptyList(),
    val filtroEstado: FiltroEstadoDocumento = FiltroEstadoDocumento.PENDIENTE,
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
) {
    val documentosFiltrados: List<DocumentoRemoteDTO>
        get() = when (filtroEstado) {
            FiltroEstadoDocumento.TODOS -> documentos
            else -> documentos.filter { it.estadoId == filtroEstado.estadoId }
        }

    val contadores: Map<FiltroEstadoDocumento, Int>
        get() = mapOf(
            FiltroEstadoDocumento.TODOS to documentos.size,
            FiltroEstadoDocumento.PENDIENTE to documentos.count { it.estadoId == 1L },
            FiltroEstadoDocumento.EN_REVISION to documentos.count { it.estadoId == 4L },
            FiltroEstadoDocumento.ACEPTADO to documentos.count { it.estadoId == 2L },
            FiltroEstadoDocumento.RECHAZADO to documentos.count { it.estadoId == 3L }
        )
}

class GestionDocumentosViewModel(
    private val documentRepository: DocumentRemoteRepository,
    private val userPreferences: Flow<UserPreferences>
) : ViewModel() {

    companion object {
        private const val TAG = "GestionDocumentosVM"
    }

    private val _uiState = MutableStateFlow(GestionDocumentosUiState())
    val uiState: StateFlow<GestionDocumentosUiState> = _uiState

    fun cargarDocumentos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val prefs = userPreferences.first()
            val userId = prefs.userId
            val roleId = prefs.userRole

            when (val result = documentRepository.listarTodosDocumentos(userId, roleId, includeDetails = true)) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Documentos cargados: ${result.data.size}")
                    _uiState.update {
                        it.copy(
                            documentos = result.data.sortedByDescending { doc -> doc.fechaSubido },
                            isLoading = false
                        )
                    }
                }
                is ApiResult.Error -> {
                    Log.e(TAG, "Error al cargar documentos: ${result.message}")
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun cambiarFiltro(nuevoFiltro: FiltroEstadoDocumento) {
        _uiState.update { it.copy(filtroEstado = nuevoFiltro) }
    }

    fun aprobarDocumento(documentoId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }

            val prefs = userPreferences.first()
            val userId = prefs.userId
            val roleId = prefs.userRole

            when (val result = documentRepository.actualizarEstadoDocumento(
                userId, roleId, documentoId, DocumentRemoteRepository.ESTADO_ACEPTADO
            )) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Documento $documentoId aprobado")
                    _uiState.update { it.copy(isProcessing = false, mensaje = "Documento aprobado correctamente") }
                    cargarDocumentos()
                }
                is ApiResult.Error -> {
                    Log.e(TAG, "Error al aprobar: ${result.message}")
                    _uiState.update { it.copy(isProcessing = false, error = "Error al aprobar: ${result.message}") }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun rechazarDocumento(documentoId: Long, motivo: String, adminId: Long? = null) {
        if (motivo.isBlank()) {
            _uiState.update { it.copy(error = "El motivo de rechazo es obligatorio") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }

            val prefs = userPreferences.first()
            val userId = prefs.userId
            val roleId = prefs.userRole

            when (val result = documentRepository.actualizarEstadoConObservaciones(
                userId, roleId, documentoId, DocumentRemoteRepository.ESTADO_RECHAZADO, motivo, adminId
            )) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Documento $documentoId rechazado con motivo: $motivo")
                    _uiState.update { it.copy(isProcessing = false, mensaje = "Documento rechazado") }
                    cargarDocumentos()
                }
                is ApiResult.Error -> {
                    Log.e(TAG, "Error al rechazar: ${result.message}")
                    _uiState.update { it.copy(isProcessing = false, error = "Error al rechazar: ${result.message}") }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun marcarEnRevision(documentoId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }

            val prefs = userPreferences.first()
            val userId = prefs.userId
            val roleId = prefs.userRole

            when (val result = documentRepository.actualizarEstadoDocumento(
                userId, roleId, documentoId, DocumentRemoteRepository.ESTADO_EN_REVISION
            )) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Documento $documentoId marcado en revision")
                    _uiState.update { it.copy(isProcessing = false, mensaje = "Documento marcado en revision") }
                    cargarDocumentos()
                }
                is ApiResult.Error -> {
                    Log.e(TAG, "Error: ${result.message}")
                    _uiState.update { it.copy(isProcessing = false, error = "Error: ${result.message}") }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun eliminarDocumento(documentoId: Long, motivo: String) {
        if (motivo.isBlank()) {
            _uiState.update { it.copy(error = "El motivo de eliminacion es obligatorio") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }

            val prefs = userPreferences.first()
            val userId = prefs.userId
            val roleId = prefs.userRole

            when (val result = documentRepository.eliminarDocumento(userId, roleId, documentoId)) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Documento $documentoId eliminado. Motivo: $motivo")
                    _uiState.update { it.copy(isProcessing = false, mensaje = "Documento eliminado") }
                    cargarDocumentos()
                }
                is ApiResult.Error -> {
                    Log.e(TAG, "Error al eliminar: ${result.message}")
                    _uiState.update { it.copy(isProcessing = false, error = "Error al eliminar: ${result.message}") }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun limpiarMensaje() { _uiState.update { it.copy(mensaje = null) } }
    fun limpiarError() { _uiState.update { it.copy(error = null) } }
}