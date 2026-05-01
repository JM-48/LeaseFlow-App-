//package com.leaseflow.app.ui.viewmodel
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import com.leaseflow.app.data.local.dao.CatalogDao
//import com.leaseflow.app.data.local.dao.DocumentoDao
//import com.leaseflow.app.data.repository.DocumentRemoteRepository
//
///**
// * Factory para crear DocumentosViewModel
// */
//class DocumentosViewModelFactory(
//    private val documentoDao: DocumentoDao,
//    private val catalogDao: CatalogDao,
//    private val remoteRepository: DocumentRemoteRepository
//) : ViewModelProvider.Factory {
//
//    @Suppress("UNCHECKED_CAST")
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(DocumentosViewModel::class.java)) {
//            return DocumentosViewModel(
//                documentoDao,
//                catalogDao,
//                remoteRepository
//            ) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
//    }
//}
