package com.leaseflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.leaseflow.app.data.repository.DocumentRemoteRepository

/**
 * Factory para crear GestionDocumentosViewModel.
 */
class GestionDocumentosViewModelFactory(
    private val documentRepository: DocumentRemoteRepository = DocumentRemoteRepository()
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GestionDocumentosViewModel::class.java)) {
            return GestionDocumentosViewModel(documentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
