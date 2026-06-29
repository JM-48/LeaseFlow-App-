package com.leaseflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.leaseflow.app.data.local.storage.UserSessionData
import com.leaseflow.app.data.repository.ReviewRemoteRepository
import kotlinx.coroutines.flow.Flow

class ReviewViewModelFactory(
    private val reviewRepository: ReviewRemoteRepository,
    private val userPreferences: Flow<UserSessionData>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewViewModel::class.java)) {
            return ReviewViewModel(reviewRepository, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
