package com.leaseflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.leaseflow.app.data.local.dao.CatalogDao
import com.leaseflow.app.data.local.dao.PropiedadDao
import com.leaseflow.app.data.local.storage.UserPreferences
import com.leaseflow.app.data.repository.ApplicationRemoteRepository
import com.leaseflow.app.data.repository.PropertyRemoteRepository
import kotlinx.coroutines.flow.Flow

class MisPropiedadesViewModelFactory(
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao,
    private val propertyRepository: PropertyRemoteRepository,
    private val applicationRepository: ApplicationRemoteRepository,
    private val userPreferences: Flow<UserPreferences>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MisPropiedadesViewModel::class.java)) {
            return MisPropiedadesViewModel(
                propiedadDao,
                catalogDao,
                propertyRepository,
                applicationRepository,
                userPreferences
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}