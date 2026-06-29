package com.leaseflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.leaseflow.app.data.local.storage.UserSessionData
import com.leaseflow.app.data.repository.ContactRemoteRepository
import kotlinx.coroutines.flow.Flow

class ContactViewModelFactory(
    private val contactRepository: ContactRemoteRepository,
    private val userPreferences: Flow<UserSessionData>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactViewModel::class.java)) {
            return ContactViewModel(contactRepository, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
