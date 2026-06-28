package com.leaseflow.app.data.repository

import android.util.Log
import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.remote.RetrofitClient
import com.leaseflow.app.data.remote.dto.LoginRemoteDTO
import com.leaseflow.app.data.remote.dto.LoginResponseRemoteDTO
import com.leaseflow.app.data.remote.dto.UsuarioRemoteDTO
import com.leaseflow.app.data.remote.dto.UsuarioUpdateRemoteDTO
import com.leaseflow.app.data.remote.safeApiCall
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repositorio con logging detallado para User Service.
 *
 * CAMBIO: Todos los metodos que llaman a endpoints protegidos reciben
 * (userId: Long, roleId: Int) y los propagan como headers X-Usuario-Id / X-Rol-Id.
 * Login y registrarUsuario son publicos — no los necesitan.
 */
class UserRemoteRepository {

    private val api = RetrofitClient.userServiceApi

    companion object {
        private const val TAG = "UserRemoteRepository"
    }

    // ==================== AUTENTICACION (publicos) ====================

    suspend fun registrarUsuario(
        pnombre: String,
        snombre: String,
        papellido: String,
        fnacimiento: String,
        email: String,
        rut: String,
        ntelefono: String,
        clave: String,
        rolId: Long? = null
    ): ApiResult<UsuarioRemoteDTO> {
        Log.d(TAG, "Registrando nuevo usuario: $email")

        val usuarioDTO = UsuarioRemoteDTO(
            pnombre = pnombre,
            snombre = snombre,
            papellido = papellido,
            fnacimiento = fnacimiento,
            email = email,
            rut = rut,
            ntelefono = ntelefono,
            clave = clave,
            rolId = rolId
        )

        return when (val result = safeApiCall { api.registrarUsuario(usuarioDTO) }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Usuario registrado: ID=${result.data.id}")
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error al registrar: ${result.message}")
                ApiResult.Error(parseBackendError(result.message, result.code), result.code)
            }
            else -> result
        }
    }

    suspend fun login(email: String, password: String): ApiResult<LoginResponseRemoteDTO> {
        Log.d(TAG, "Login: $email")

        return when (val result = safeApiCall { api.login(LoginRemoteDTO(email, password)) }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Login exitoso: ID=${result.data.usuario.id}")
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error login: ${result.message}")
                ApiResult.Error(parseBackendError(result.message, result.code), result.code)
            }
            else -> result
        }
    }

    // ==================== CONSULTAS (protegidos) ====================

    suspend fun obtenerUsuarioPorId(
        userId: Long,
        roleId: Int,
        targetUserId: Long,
        includeDetails: Boolean = true
    ): ApiResult<UsuarioRemoteDTO> {
        Log.d(TAG, "Obteniendo usuario $targetUserId (caller=$userId)")

        return when (val result = safeApiCall {
            api.obtenerUsuarioPorId(targetUserId, userId, roleId, includeDetails)
        }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Usuario obtenido: ${result.data.email}")
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error: ${result.message}")
                result
            }
            else -> result
        }
    }

    suspend fun obtenerUsuarioPorEmail(
        userId: Long,
        roleId: Int,
        email: String,
        includeDetails: Boolean = true
    ): ApiResult<UsuarioRemoteDTO> {
        Log.d(TAG, "Obteniendo usuario por email: $email")
        return safeApiCall { api.obtenerUsuarioPorEmail(email, userId, roleId, includeDetails) }
    }

    suspend fun obtenerTodosUsuarios(
        userId: Long,
        roleId: Int,
        includeDetails: Boolean = false
    ): ApiResult<List<UsuarioRemoteDTO>> {
        Log.d(TAG, "Obteniendo todos los usuarios (caller=$userId)")

        return when (val result = safeApiCall {
            api.obtenerTodosUsuarios(userId, roleId, includeDetails)
        }) {
            is ApiResult.Success -> {
                Log.d(TAG, "${result.data.size} usuarios obtenidos")
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error: ${result.message}")
                result
            }
            else -> result
        }
    }

    suspend fun obtenerUsuariosPorRol(
        userId: Long,
        roleId: Int,
        targetRolId: Long,
        includeDetails: Boolean = false
    ): ApiResult<List<UsuarioRemoteDTO>> {
        Log.d(TAG, "Obteniendo usuarios con rol $targetRolId")
        return safeApiCall { api.obtenerUsuariosPorRol(targetRolId, userId, roleId, includeDetails) }
    }

    suspend fun existeUsuario(
        userId: Long,
        roleId: Int,
        targetUserId: Long
    ): ApiResult<Boolean> {
        Log.d(TAG, "Verificando existencia de usuario $targetUserId")
        return safeApiCall { api.existeUsuario(targetUserId, userId, roleId) }
    }

    // ==================== ACTUALIZACION (protegidos) ====================

    suspend fun actualizarUsuario(
        userId: Long,
        roleId: Int,
        targetUserId: Long,
        updateDTO: UsuarioUpdateRemoteDTO
    ): ApiResult<UsuarioRemoteDTO> {
        Log.d(TAG, "Actualizando usuario $targetUserId (caller=$userId)")

        return when (val result = safeApiCall {
            api.actualizarUsuario(targetUserId, userId, roleId, updateDTO)
        }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Usuario actualizado")
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error: ${result.message}")
                ApiResult.Error(parseBackendError(result.message, result.code), result.code)
            }
            else -> result
        }
    }

    suspend fun actualizarUsuarioDesdeRemoteDTO(
        userId: Long,
        roleId: Int,
        targetUserId: Long,
        usuarioDTO: UsuarioRemoteDTO
    ): ApiResult<UsuarioRemoteDTO> {
        val updateDTO = UsuarioUpdateRemoteDTO(
            pnombre = usuarioDTO.pnombre,
            snombre = usuarioDTO.snombre,
            papellido = usuarioDTO.papellido,
            email = usuarioDTO.email,
            ntelefono = usuarioDTO.ntelefono,
            rolId = usuarioDTO.rolId,
            estadoId = usuarioDTO.estadoId
        )
        return actualizarUsuario(userId, roleId, targetUserId, updateDTO)
    }

    suspend fun cambiarRol(
        userId: Long,
        roleId: Int,
        targetUserId: Long,
        nuevoRolId: Long
    ): ApiResult<UsuarioRemoteDTO> {
        Log.d(TAG, "Cambiando rol de usuario $targetUserId a $nuevoRolId")

        return when (val result = safeApiCall {
            api.cambiarRol(targetUserId, userId, roleId, nuevoRolId)
        }) {
            is ApiResult.Success -> { Log.d(TAG, "Rol cambiado"); result }
            is ApiResult.Error -> { Log.e(TAG, "Error: ${result.message}"); result }
            else -> result
        }
    }

    suspend fun cambiarEstado(
        userId: Long,
        roleId: Int,
        targetUserId: Long,
        nuevoEstadoId: Long
    ): ApiResult<UsuarioRemoteDTO> {
        Log.d(TAG, "Cambiando estado de usuario $targetUserId a $nuevoEstadoId")

        return when (val result = safeApiCall {
            api.cambiarEstado(targetUserId, userId, roleId, nuevoEstadoId)
        }) {
            is ApiResult.Success -> { Log.d(TAG, "Estado cambiado"); result }
            is ApiResult.Error -> { Log.e(TAG, "Error: ${result.message}"); result }
            else -> result
        }
    }

    suspend fun agregarPuntos(
        userId: Long,
        roleId: Int,
        targetUserId: Long,
        puntos: Int
    ): ApiResult<UsuarioRemoteDTO> {
        Log.d(TAG, "Agregando $puntos puntos al usuario $targetUserId")

        return when (val result = safeApiCall {
            api.agregarPuntos(targetUserId, userId, roleId, puntos)
        }) {
            is ApiResult.Success -> { Log.d(TAG, "Puntos agregados: ${result.data.puntos}"); result }
            is ApiResult.Error -> { Log.e(TAG, "Error: ${result.message}"); result }
            else -> result
        }
    }

    // ==================== HELPERS ====================

    private fun parseBackendError(rawMessage: String, code: Int?): String {
        return when (code) {
            400 -> when {
                rawMessage.contains("email", ignoreCase = true) &&
                        rawMessage.contains("registrado", ignoreCase = true) ->
                    "Este correo ya esta registrado. Intenta con otro."
                rawMessage.contains("RUT", ignoreCase = true) &&
                        rawMessage.contains("registrado", ignoreCase = true) ->
                    "Este RUT ya esta registrado en el sistema."
                rawMessage.contains("18", ignoreCase = true) ||
                        rawMessage.contains("edad", ignoreCase = true) ->
                    "Debes ser mayor de 18 anos para registrarte."
                rawMessage.contains("formato", ignoreCase = true) &&
                        rawMessage.contains("RUT", ignoreCase = true) ->
                    "Formato de RUT invalido. Usa el formato: 12345678-9"
                rawMessage.contains("contrasena", ignoreCase = true) &&
                        rawMessage.contains("8", ignoreCase = true) ->
                    "La contrasena debe tener al menos 8 caracteres."
                rawMessage.contains("obligatorio", ignoreCase = true) ->
                    "Todos los campos son obligatorios."
                else -> rawMessage
            }
            401 -> when {
                rawMessage.contains("incorrectos", ignoreCase = true) ->
                    "Email o contrasena incorrectos. Verifica tus datos."
                rawMessage.contains("inactiva", ignoreCase = true) ->
                    "Tu cuenta esta inactiva. Contacta al administrador."
                rawMessage.contains("suspendida", ignoreCase = true) ->
                    "Tu cuenta ha sido suspendida. Contacta a soporte."
                else -> "No se pudo autenticar. Verifica tus credenciales."
            }
            404 -> "Usuario no encontrado en el sistema."
            500 -> "Error interno del servidor. Por favor intenta mas tarde."
            503 -> "Servicio no disponible. Intenta nuevamente en unos momentos."
            else -> rawMessage
        }
    }

    fun formatearFecha(timestamp: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))

    fun parsearFecha(fechaString: String): Long {
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fechaString)?.time
                ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}