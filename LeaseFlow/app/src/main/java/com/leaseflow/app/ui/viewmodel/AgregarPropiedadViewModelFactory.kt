package com.leaseflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.leaseflow.app.data.local.storage.UserSessionData
import com.leaseflow.app.data.repository.PropertyRemoteRepository
import kotlinx.coroutines.flow.Flow

class AgregarPropiedadViewModelFactory(
    private val propertyRepository: PropertyRemoteRepository,
    private val userPreferences: Flow<UserSessionData>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgregarPropiedadViewModel::class.java)) {
            return AgregarPropiedadViewModel(propertyRepository, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
