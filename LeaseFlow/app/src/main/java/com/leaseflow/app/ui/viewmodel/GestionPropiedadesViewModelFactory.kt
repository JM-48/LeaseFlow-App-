package com.leaseflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.leaseflow.app.data.local.storage.UserPreferences
import com.leaseflow.app.data.repository.PropertyRemoteRepository
import kotlinx.coroutines.flow.Flow

class GestionPropiedadesViewModelFactory(
    private val propertyRepository: PropertyRemoteRepository,
    private val userPreferences: Flow<UserPreferences>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GestionPropiedadesViewModel::class.java)) {
            return GestionPropiedadesViewModel(propertyRepository, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}