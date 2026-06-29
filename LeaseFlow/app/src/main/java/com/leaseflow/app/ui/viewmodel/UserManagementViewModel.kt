package com.leaseflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.leaseflow.app.data.local.storage.UserSessionData
import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.remote.dto.UsuarioDTO
import com.leaseflow.app.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UserManagementViewModel(
    private val repository: UserRepository,
    private val userPreferences: Flow<UserSessionData>
) : ViewModel() {

    private val _users = MutableStateFlow<List<UsuarioDTO>>(emptyList())
    val users: StateFlow<List<UsuarioDTO>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            val prefs = userPreferences.first()
            val userId = prefs.userId
            val roleId = prefs.userRole
            when (val result = repository.getUsers(userId, roleId)) {
                is ApiResult.Success -> {
                    _users.value = result.data
                    _error.value = null
                }
                is ApiResult.Error -> {
                    _error.value = "Error al cargar usuarios: ${result.message}"
                }
                is ApiResult.Loading -> {}
            }
            _isLoading.value = false
        }
    }

    fun updateUser(targetUserId: Long, updatedUser: UsuarioDTO) {
        viewModelScope.launch {
            _isLoading.value = true
            val prefs = userPreferences.first()
            val userId = prefs.userId
            val roleId = prefs.userRole
            when (val result = repository.updateUser(userId, roleId, targetUserId, updatedUser)) {
                is ApiResult.Success -> {
                    _users.value = _users.value.map {
                        if (it.id == targetUserId) result.data else it
                    }
                    _error.value = null
                }
                is ApiResult.Error -> {
                    _error.value = "Error al actualizar el usuario: ${result.message}"
                }
                is ApiResult.Loading -> {}
            }
            _isLoading.value = false
        }
    }

    fun deleteUser(targetUserId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            val prefs = userPreferences.first()
            val userId = prefs.userId
            val roleId = prefs.userRole
            when (val result = repository.deleteUser(userId, roleId, targetUserId)) {
                is ApiResult.Success -> {
                    _users.value = _users.value.filter { it.id != targetUserId }
                    _error.value = null
                }
                is ApiResult.Error -> {
                    _error.value = "Error al eliminar el usuario: ${result.message}"
                }
                is ApiResult.Loading -> {}
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}

class UserManagementViewModelFactory(
    private val repository: UserRepository,
    private val userPreferences: Flow<UserSessionData>
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserManagementViewModel::class.java)) {
            return UserManagementViewModel(repository, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
