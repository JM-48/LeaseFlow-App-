package com.leaseflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.leaseflow.app.data.local.dao.SolicitudDao
import com.leaseflow.app.data.local.dao.PropiedadDao
import com.leaseflow.app.data.local.dao.CatalogDao
import com.leaseflow.app.data.local.storage.UserPreferences
import com.leaseflow.app.data.repository.ApplicationRemoteRepository
import com.leaseflow.app.data.repository.PropertyRemoteRepository
import kotlinx.coroutines.flow.Flow

class SolicitudesViewModelFactory(
    private val solicitudDao: SolicitudDao,
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao,
    private val remoteRepository: ApplicationRemoteRepository,
    private val propertyRepository: PropertyRemoteRepository? = null,
    private val userPreferences: Flow<UserPreferences>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SolicitudesViewModel::class.java)) {
            return SolicitudesViewModel(
                solicitudDao = solicitudDao,
                propiedadDao = propiedadDao,
                catalogDao = catalogDao,
                remoteRepository = remoteRepository,
                propertyRepository = propertyRepository,
                userPreferences = userPreferences
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}