package com.leaseflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.leaseflow.app.data.local.dao.PropiedadDao
import com.leaseflow.app.data.local.dao.CatalogDao
import com.leaseflow.app.data.repository.PropertyRemoteRepository

/**
 * Factory para MisPropiedadesViewModel
 */
class MisPropiedadesViewModelFactory(
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao,
    private val propertyRepository: PropertyRemoteRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MisPropiedadesViewModel::class.java)) {
            return MisPropiedadesViewModel(
                propiedadDao,
                catalogDao,
                propertyRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
