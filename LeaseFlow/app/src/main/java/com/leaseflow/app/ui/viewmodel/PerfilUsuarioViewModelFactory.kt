package com.leaseflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.leaseflow.app.data.local.dao.UsuarioDao
import com.leaseflow.app.data.local.dao.CatalogDao
import com.leaseflow.app.data.local.dao.SolicitudDao

class PerfilUsuarioViewModelFactory(
    private val usuarioDao: UsuarioDao,
    private val catalogDao: CatalogDao,
    private val solicitudDao: SolicitudDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PerfilUsuarioViewModel::class.java)) {
            return PerfilUsuarioViewModel(usuarioDao, catalogDao, solicitudDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
