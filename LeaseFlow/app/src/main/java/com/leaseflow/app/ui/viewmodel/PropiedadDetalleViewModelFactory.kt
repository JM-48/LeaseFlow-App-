package com.leaseflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.leaseflow.app.data.local.dao.PropiedadDao
import com.leaseflow.app.data.local.dao.CatalogDao
import com.leaseflow.app.data.repository.PropertyRemoteRepository
import com.leaseflow.app.data.repository.ApplicationRemoteRepository

/**
 * Factory para PropiedadDetalleViewModel
 */
class PropiedadDetalleViewModelFactory(
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao,
    private val propertyRepository: PropertyRemoteRepository,
    private val applicationRepository: ApplicationRemoteRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PropiedadDetalleViewModel::class.java)) {
            return PropiedadDetalleViewModel(
                propiedadDao,
                catalogDao,
                propertyRepository,
                applicationRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
