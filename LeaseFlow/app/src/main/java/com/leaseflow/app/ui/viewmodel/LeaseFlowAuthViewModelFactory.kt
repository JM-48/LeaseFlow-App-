package com.leaseflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.leaseflow.app.data.repository.DocumentRemoteRepository
import com.leaseflow.app.data.repository.LeaseFlowUserRepository
import com.leaseflow.app.data.repository.UserRemoteRepository

/**
 * Factory para crear LeaseFlowAuthViewModel con las dependencias necesarias.
 */
class LeaseFlowAuthViewModelFactory(
    private val remoteRepository: UserRemoteRepository,
    private val localRepository: LeaseFlowUserRepository,
    private val documentRepository: DocumentRemoteRepository = DocumentRemoteRepository()
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeaseFlowAuthViewModel::class.java)) {
            return LeaseFlowAuthViewModel(
                remoteRepository = remoteRepository,
                localRepository = localRepository,
                documentRepository = documentRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
