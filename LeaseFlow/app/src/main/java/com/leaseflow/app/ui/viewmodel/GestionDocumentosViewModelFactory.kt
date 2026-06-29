package com.leaseflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.leaseflow.app.data.local.storage.UserSessionData
import com.leaseflow.app.data.repository.DocumentRemoteRepository
import kotlinx.coroutines.flow.Flow

class GestionDocumentosViewModelFactory(
    private val documentRepository: DocumentRemoteRepository,
    private val userPreferences: Flow<UserSessionData>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GestionDocumentosViewModel::class.java)) {
            return GestionDocumentosViewModel(documentRepository, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
