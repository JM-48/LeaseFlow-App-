package com.leaseflow.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leaseflow.app.data.local.storage.UserSessionData
import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.remote.dto.*
import com.leaseflow.app.data.repository.PropertyRemoteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class AgregarPropiedadViewModel(
    private val propertyRepository: PropertyRemoteRepository,
    private val userPreferences: Flow<UserSessionData>
) : ViewModel() {

    companion object {
        private const val TAG = "AgregarPropiedadVM"
    }

    private val _tipos = MutableStateFlow<List<TipoRemoteDTO>>(emptyList())
    val tipos: StateFlow<List<TipoRemoteDTO>> = _tipos.asStateFlow()

    private val _regiones = MutableStateFlow<List<RegionRemoteDTO>>(emptyList())
    val regiones: StateFlow<List<RegionRemoteDTO>> = _regiones.asStateFlow()

    private val _comunas = MutableStateFlow<List<ComunaRemoteDTO>>(emptyList())
    val comunas: StateFlow<List<ComunaRemoteDTO>> = _comunas.asStateFlow()

    private val _comunasFiltradas = MutableStateFlow<List<ComunaRemoteDTO>>(emptyList())
    val comunasFiltradas: StateFlow<List<ComunaRemoteDTO>> = _comunasFiltradas.asStateFlow()

    private val _categorias = MutableStateFlow<List<CategoriaRemoteDTO>>(emptyList())
    val categorias: StateFlow<List<CategoriaRemoteDTO>> = _categorias.asStateFlow()

    private val _propiedadCreada = MutableStateFlow<PropertyRemoteDTO?>(null)
    val propiedadCreada: StateFlow<PropertyRemoteDTO?> = _propiedadCreada.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    private val _successMsg = MutableStateFlow<String?>(null)
    val successMsg: StateFlow<String?> = _successMsg.asStateFlow()

    private val _fotosSubidas = MutableStateFlow<List<FotoRemoteDTO>>(emptyList())
    val fotosSubidas: StateFlow<List<FotoRemoteDTO>> = _fotosSubidas.asStateFlow()

    private val _fotoSubiendo = MutableStateFlow(false)
    val fotoSubiendo: StateFlow<Boolean> = _fotoSubiendo.asStateFlow()

    init {
        cargarCatalogos()
    }

    // Catalogos; lecturas publicas, sin headers de identidad
    fun cargarCatalogos() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null
            try {
                Log.d(TAG, "Cargando catalogos...")
                val jobs = mutableListOf<Job>()

                jobs.add(launch {
                    when (val result = propertyRepository.listarTipos()) {
                        is ApiResult.Success -> { _tipos.value = result.data; Log.d(TAG, "Tipos: ${result.data.size}") }
                        is ApiResult.Error -> Log.e(TAG, "Error tipos: ${result.message}")
                        else -> {}
                    }
                })

                jobs.add(launch {
                    when (val result = propertyRepository.listarRegiones()) {
                        is ApiResult.Success -> { _regiones.value = result.data; Log.d(TAG, "Regiones: ${result.data.size}") }
                        is ApiResult.Error -> Log.e(TAG, "Error regiones: ${result.message}")
                        else -> {}
                    }
                })

                jobs.add(launch {
                    when (val result = propertyRepository.listarComunas()) {
                        is ApiResult.Success -> { _comunas.value = result.data; Log.d(TAG, "Comunas: ${result.data.size}") }
                        is ApiResult.Error -> Log.e(TAG, "Error comunas: ${result.message}")
                        else -> {}
                    }
                })

                jobs.add(launch {
                    when (val result = propertyRepository.listarCategorias()) {
                        is ApiResult.Success -> { _categorias.value = result.data; Log.d(TAG, "Categorias: ${result.data.size}") }
                        is ApiResult.Error -> Log.e(TAG, "Error categorias: ${result.message}")
                        else -> {}
                    }
                })

                jobs.joinAll()
            } catch (e: Exception) {
                Log.e(TAG, "Excepcion catalogos: ${e.message}", e)
                _errorMsg.value = "Error al cargar datos: ${e.message}"
            } finally {
                _isLoading.value = false
                Log.d(TAG, "Carga de catalogos finalizada.")
            }
        }
    }

    fun cargarComunasPorRegion(regionId: Long) {
        viewModelScope.launch {
            Log.d(TAG, "Filtrando comunas por region: $regionId")
            _comunasFiltradas.value = _comunas.value.filter { it.regionId == regionId }
            Log.d(TAG, "Comunas filtradas: ${_comunasFiltradas.value.size}")
            if (_comunasFiltradas.value.isEmpty()) {
                _errorMsg.value = "No se encontraron comunas para esta region."
            } else {
                _errorMsg.value = null
            }
        }
    }

    // Escritura â€” requiere headers
    fun crearPropiedad(
        titulo: String,
        precioMensual: Double,
        m2: Double,
        nHabit: Int,
        nBanos: Int,
        petFriendly: Boolean,
        direccion: String,
        tipoId: Long,
        comunaId: Long,
        propietarioId: Long?
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMsg.value = null
            val prefs = userPreferences.first()
            val userId = prefs.userId
            val roleId = prefs.userRole

            try {
                val codigoGenerado = generarCodigo(tipoId)
                val propiedad = PropertyRemoteDTO(
                    codigo = codigoGenerado,
                    titulo = titulo,
                    precioMensual = precioMensual,
                    divisa = "CLP",
                    m2 = m2,
                    nHabit = nHabit,
                    nBanos = nBanos,
                    petFriendly = petFriendly,
                    direccion = direccion,
                    tipoId = tipoId,
                    comunaId = comunaId,
                    propietarioId = propietarioId
                )
                Log.d(TAG, "Creando propiedad: codigo=$codigoGenerado, titulo=$titulo")

                when (val result = propertyRepository.crearPropiedad(
                    userId = userId,
                    roleId = roleId,
                    propiedad = propiedad
                )) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Propiedad creada: id=${result.data.id}")
                        _propiedadCreada.value = result.data
                        _successMsg.value = "Propiedad creada exitosamente"
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error al crear propiedad: ${result.message}")
                        _errorMsg.value = result.message
                    }
                    is ApiResult.Loading -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepcion al crear propiedad: ${e.message}", e)
                _errorMsg.value = "Error: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    private fun generarCodigo(tipoId: Long): String {
        val tipoNombre = _tipos.value.firstOrNull { it.id == tipoId }?.nombre?.trim().orEmpty()
        val normalized = tipoNombre.uppercase()
        val prefix = when {
            "DEPART" in normalized -> "DP"
            "CASA" in normalized -> "CA"
            "OFIC" in normalized -> "OF"
            "BODEG" in normalized -> "BD"
            "TERREN" in normalized -> "TR"
            "PARCEL" in normalized -> "PA"
            normalized.length >= 2 && normalized.take(2).all { it.isLetter() } -> normalized.take(2)
            else -> "PR"
        }
        val digits = (1000..9999).random()
        return "$prefix-$digits"
    }

    fun subirFoto(propiedadId: Long, file: File) {
        viewModelScope.launch {
            _fotoSubiendo.value = true
            val prefs = userPreferences.first()
            val userId = prefs.userId
            val roleId = prefs.userRole

            try {
                Log.d(TAG, "Subiendo foto: propiedad=$propiedadId, archivo=${file.name}")
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", file.name, requestBody)

                when (val result = propertyRepository.subirFoto(userId, roleId, propiedadId, filePart)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Foto subida: id=${result.data.id}")
                        _fotosSubidas.value = _fotosSubidas.value + result.data
                        _successMsg.value = "Foto subida"
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error al subir foto: ${result.message}")
                        _errorMsg.value = result.message
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepcion al subir foto: ${e.message}", e)
                _errorMsg.value = "Error al subir foto: ${e.message}"
            } finally {
                _fotoSubiendo.value = false
            }
        }
    }

    fun eliminarFoto(fotoId: Long) {
        viewModelScope.launch {
            val prefs = userPreferences.first()
            val userId = prefs.userId
            val roleId = prefs.userRole

            try {
                Log.d(TAG, "Eliminando foto: id=$fotoId")
                when (val result = propertyRepository.eliminarFoto(userId, roleId, fotoId)) {
                    is ApiResult.Success -> {
                        _fotosSubidas.value = _fotosSubidas.value.filter { it.id != fotoId }
                        _successMsg.value = "Foto eliminada"
                    }
                    is ApiResult.Error -> _errorMsg.value = result.message
                    else -> {}
                }
            } catch (e: Exception) {
                _errorMsg.value = "Error: ${e.message}"
            }
        }
    }

    fun limpiarMensajes() {
        _errorMsg.value = null
        _successMsg.value = null
    }

    fun resetearEstado() {
        _propiedadCreada.value = null
        _fotosSubidas.value = emptyList()
        _errorMsg.value = null
        _successMsg.value = null
    }
}
