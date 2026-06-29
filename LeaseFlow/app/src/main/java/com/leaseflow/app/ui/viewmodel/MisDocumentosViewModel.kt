package com.leaseflow.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leaseflow.app.data.local.storage.UserSessionData
import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.remote.dto.DocumentoRemoteDTO
import com.leaseflow.app.data.remote.dto.TipoDocumentoRemoteDTO
import com.leaseflow.app.data.repository.DocumentRemoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MisDocumentosUiState(
    val documentos: List<DocumentoRemoteDTO> = emptyList(),
    val tiposDocumento: List<TipoDocumentoRemoteDTO> = emptyList(),
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

class MisDocumentosViewModel(
    private val documentRepository: DocumentRemoteRepository,
    private val userPreferences: Flow<UserSessionData>
) : ViewModel() {

    companion object {
        private const val TAG = "MisDocumentosVM"
    }

    private val _uiState = MutableStateFlow(MisDocumentosUiState())
    val uiState: StateFlow<MisDocumentosUiState> = _uiState

    fun cargarMisDocumentos(usuarioId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val prefs = userPreferences.first()
                val userId = prefs.userId
                val roleId = prefs.userRole

                when (val result = documentRepository.obtenerDocumentosPorUsuario(userId, roleId, usuarioId, includeDetails = true)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Documentos cargados: ${result.data.size}")
                        _uiState.update {
                            it.copy(
                                documentos = result.data.sortedByDescending { doc -> doc.fechaSubido },
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error al cargar documentos: ${result.message}")
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                    is ApiResult.Loading -> {}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun cargarTiposDocumento() {
        viewModelScope.launch {
            when (val result = documentRepository.listarTiposDocumentos()) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(tiposDocumento = result.data) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun resubirDocumentoRechazado(
        documentoRechazadoId: Long,
        usuarioId: Long,
        tipoDocId: Long,
        nombreArchivo: String,
        extension: String = "pdf"
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true) }

            try {
                val prefs = userPreferences.first()
                val userId = prefs.userId
                val roleId = prefs.userRole

                val deleteResult = documentRepository.eliminarDocumento(userId, roleId, documentoRechazadoId)

                when (deleteResult) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Documento rechazado eliminado: $documentoRechazadoId")

                        val nombreDoc = documentRepository.generarNombreDocumento(
                            tipoDocId = tipoDocId,
                            nombreUsuario = "user_$usuarioId",
                            extension = extension
                        )

                        when (val createResult = documentRepository.crearDocumento(
                            userId = userId,
                            roleId = roleId,
                            nombre = nombreDoc,
                            usuarioId = usuarioId,
                            tipoDocId = tipoDocId,
                            estadoId = DocumentRemoteRepository.ESTADO_PENDIENTE
                        )) {
                            is ApiResult.Success -> {
                                Log.d(TAG, "Nuevo documento creado exitosamente")
                                _uiState.update {
                                    it.copy(
                                        isUploading = false,
                                        mensaje = "Documento subido correctamente. Pendiente de revision."
                                    )
                                }
                                cargarMisDocumentos(usuarioId)
                            }
                            is ApiResult.Error -> {
                                Log.e(TAG, "Error al crear nuevo documento: ${createResult.message}")
                                _uiState.update {
                                    it.copy(isUploading = false, error = "Error al subir documento: ${createResult.message}")
                                }
                            }
                            is ApiResult.Loading -> {}
                        }
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error al eliminar documento rechazado: ${deleteResult.message}")
                        _uiState.update {
                            it.copy(isUploading = false, error = "Error al procesar: ${deleteResult.message}")
                        }
                    }
                    is ApiResult.Loading -> {}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUploading = false, error = e.message) }
            }
        }
    }

    fun subirNuevoDocumento(
        usuarioId: Long,
        tipoDocId: Long,
        nombreArchivo: String,
        extension: String = "pdf"
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true) }

            try {
                val prefs = userPreferences.first()
                val userId = prefs.userId
                val roleId = prefs.userRole

                val nombreDoc = documentRepository.generarNombreDocumento(
                    tipoDocId = tipoDocId,
                    nombreUsuario = "user_$usuarioId",
                    extension = extension
                )

                when (val result = documentRepository.crearDocumento(
                    userId = userId,
                    roleId = roleId,
                    nombre = nombreDoc,
                    usuarioId = usuarioId,
                    tipoDocId = tipoDocId,
                    estadoId = DocumentRemoteRepository.ESTADO_PENDIENTE
                )) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Documento subido exitosamente")
                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                mensaje = "Documento subido correctamente. Pendiente de revision."
                            )
                        }
                        cargarMisDocumentos(usuarioId)
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error al subir documento: ${result.message}")
                        _uiState.update {
                            it.copy(isUploading = false, error = "Error al subir documento: ${result.message}")
                        }
                    }
                    is ApiResult.Loading -> {}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUploading = false, error = e.message) }
            }
        }
    }

    fun limpiarMensaje() { _uiState.update { it.copy(mensaje = null) } }
    fun limpiarError() { _uiState.update { it.copy(error = null) } }
}
