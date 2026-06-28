package com.leaseflow.app.data.repository

import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.remote.api.UserServiceApi
import com.leaseflow.app.data.remote.dto.UsuarioDTO
import com.leaseflow.app.data.remote.dto.UsuarioRemoteDTO
import com.leaseflow.app.data.remote.dto.UsuarioUpdateRemoteDTO
import com.leaseflow.app.data.remote.safeApiCall

class UserRepository(private val api: UserServiceApi) {

    private fun UsuarioRemoteDTO.toUsuarioDTO(): UsuarioDTO {
        return UsuarioDTO(
            id = this.id,
            pnombre = this.pnombre,
            snombre = this.snombre,
            papellido = this.papellido,
            email = this.email,
            ntelefono = this.ntelefono,
            rolId = this.rolId?.toInt(),
            estadoId = this.estadoId?.toInt(),
            rol = this.rol?.let { UsuarioDTO.RolInfo(it.id.toInt(), it.nombre) },
            estado = this.estado?.let { UsuarioDTO.EstadoInfo(it.id.toInt(), it.nombre) },
            duocVip = this.duocVip
        )
    }

    private fun UsuarioDTO.toUsuarioUpdateRemoteDTO(): UsuarioUpdateRemoteDTO {
        return UsuarioUpdateRemoteDTO(
            pnombre = this.pnombre ?: "",
            snombre = this.snombre ?: "",
            papellido = this.papellido ?: "",
            email = this.email ?: "",
            ntelefono = this.ntelefono ?: "",
            rolId = this.rolId?.toLong(),
            estadoId = this.estadoId?.toLong() ?: this.estado?.id?.toLong()
        )
    }

    suspend fun getUsers(userId: Long, roleId: Int): ApiResult<List<UsuarioDTO>> {
        return when (val result = safeApiCall {
            api.obtenerTodosUsuarios(
                usuarioId = userId.toString(),
                rolId = roleId.toString(),
                includeDetails = true
            )
        }) {
            is ApiResult.Success -> ApiResult.Success(result.data.map { it.toUsuarioDTO() })
            is ApiResult.Error -> result
            else -> ApiResult.Loading
        }
    }

    suspend fun getUserById(userId: Long, roleId: Int, targetUserId: Long): ApiResult<UsuarioDTO> {
        return when (val result = safeApiCall {
            api.obtenerUsuarioPorId(
                id = targetUserId,
                usuarioId = userId.toString(),
                rolId = roleId.toString(),
                includeDetails = true
            )
        }) {
            is ApiResult.Success -> ApiResult.Success(result.data.toUsuarioDTO())
            is ApiResult.Error -> result
            else -> ApiResult.Loading
        }
    }

    suspend fun updateUser(userId: Long, roleId: Int, targetUserId: Long, user: UsuarioDTO): ApiResult<UsuarioDTO> {
        val updateDTO = user.toUsuarioUpdateRemoteDTO()
        return when (val result = safeApiCall {
            api.actualizarUsuario(
                id = targetUserId,
                usuario = updateDTO,
                usuarioId = userId.toString(),
                rolId = roleId.toString()
            )
        }) {
            is ApiResult.Success -> ApiResult.Success(result.data.toUsuarioDTO())
            is ApiResult.Error -> result
            else -> ApiResult.Loading
        }
    }

    suspend fun deleteUser(userId: Long, roleId: Int, targetUserId: Long): ApiResult<Unit> {
        return try {
            val response = api.eliminarUsuario(
                id = targetUserId,
                usuarioId = userId.toString(),
                rolId = roleId.toString()
            )
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                val fallback = safeApiCall {
                    api.cambiarEstado(
                        id = targetUserId,
                        estadoId = 2,
                        usuarioId = userId.toString(),
                        rolId = roleId.toString()
                    )
                }
                when (fallback) {
                    is ApiResult.Success -> ApiResult.Success(Unit)
                    is ApiResult.Error -> fallback
                    is ApiResult.Loading -> ApiResult.Loading
                }
            }
        } catch (e: Exception) {
            when (val fallback = safeApiCall {
                api.cambiarEstado(
                    id = targetUserId,
                    estadoId = 2,
                    usuarioId = userId.toString(),
                    rolId = roleId.toString()
                )
            }) {
                is ApiResult.Success -> ApiResult.Success(Unit)
                is ApiResult.Error -> fallback
                is ApiResult.Loading -> ApiResult.Error(e.message ?: "Error de conexion")
            }
        }
    }
}