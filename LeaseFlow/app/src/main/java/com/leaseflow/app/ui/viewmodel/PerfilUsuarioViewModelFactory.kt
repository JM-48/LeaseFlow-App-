package com.leaseflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.leaseflow.app.data.local.dao.UsuarioDao
import com.leaseflow.app.data.local.dao.CatalogDao
import com.leaseflow.app.data.local.dao.SolicitudDao
import com.leaseflow.app.data.local.storage.UserSessionData
import com.leaseflow.app.data.repository.LeaseFlowUserRepository
import com.leaseflow.app.data.repository.UserRemoteRepository
import kotlinx.coroutines.flow.Flow

class PerfilUsuarioViewModelFactory(
    private val usuarioDao: UsuarioDao,
    private val catalogDao: CatalogDao,
    private val solicitudDao: SolicitudDao,
    private val userRemoteRepository: UserRemoteRepository,
    private val localUserRepository: LeaseFlowUserRepository,
    private val userPreferences: Flow<UserSessionData>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PerfilUsuarioViewModel::class.java)) {
            return PerfilUsuarioViewModel(
                usuarioDao = usuarioDao,
                catalogDao = catalogDao,
                solicitudDao = solicitudDao,
                userRemoteRepository = userRemoteRepository,
                localUserRepository = localUserRepository,
                userPreferences = userPreferences
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
