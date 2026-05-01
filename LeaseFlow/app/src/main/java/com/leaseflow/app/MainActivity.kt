package com.leaseflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.leaseflow.app.data.local.LeaseFlowDatabase
import com.leaseflow.app.data.repository.ApplicationRemoteRepository
import com.leaseflow.app.data.repository.DocumentRemoteRepository
import com.leaseflow.app.data.repository.PropertyRemoteRepository
import com.leaseflow.app.data.repository.ReviewRemoteRepository
import com.leaseflow.app.data.repository.UserRemoteRepository
import com.leaseflow.app.data.repository.LeaseFlowUserRepository
import com.leaseflow.app.navigation.AppNavGraph
import com.leaseflow.app.ui.theme.LeaseFlowTheme
import com.leaseflow.app.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LeaseFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val db = LeaseFlowDatabase.getInstance(applicationContext)

                    // ==================== REPOSITORIOS ====================

                    val leaseFlowUserRepository = LeaseFlowUserRepository(
                        usuarioDao = db.usuarioDao(),
                        catalogDao = db.catalogDao()
                    )

                    val userRemoteRepository = UserRemoteRepository()

                    val documentRemoteRepository = DocumentRemoteRepository()

                    val applicationRemoteRepository = ApplicationRemoteRepository(
                        solicitudDao = db.solicitudDao(),
                        catalogDao = db.catalogDao()
                    )

                    val propertyRemoteRepository = PropertyRemoteRepository()

                    val reviewRemoteRepository = ReviewRemoteRepository()

                    // ==================== VIEWMODELS ====================

                    val authViewModel: LeaseFlowAuthViewModel = viewModel(
                        factory = LeaseFlowAuthViewModelFactory(
                            remoteRepository = userRemoteRepository,
                            localRepository = leaseFlowUserRepository,
                            documentRepository = documentRemoteRepository
                        )
                    )

                    val propiedadViewModel: PropiedadViewModel = viewModel(
                        factory = PropiedadViewModelFactory(
                            propiedadDao = db.propiedadDao(),
                            catalogDao = db.catalogDao(),
                            remoteRepository = propertyRemoteRepository
                        )
                    )

                    val propiedadDetalleViewModel: PropiedadDetalleViewModel = viewModel(
                        factory = PropiedadDetalleViewModelFactory(
                            propiedadDao = db.propiedadDao(),
                            catalogDao = db.catalogDao(),
                            propertyRepository = propertyRemoteRepository,
                            applicationRepository = applicationRemoteRepository
                        )
                    )

                    val solicitudesViewModel: SolicitudesViewModel = viewModel(
                        factory = SolicitudesViewModelFactory(
                            solicitudDao = db.solicitudDao(),
                            propiedadDao = db.propiedadDao(),
                            catalogDao = db.catalogDao(),
                            remoteRepository = applicationRemoteRepository,
                            propertyRepository = propertyRemoteRepository
                        )
                    )

                    val perfilViewModel: PerfilUsuarioViewModel = viewModel(
                        factory = PerfilUsuarioViewModelFactory(
                            usuarioDao = db.usuarioDao(),
                            catalogDao = db.catalogDao(),
                            solicitudDao = db.solicitudDao()
                        )
                    )

                    val reviewViewModel: ReviewViewModel = viewModel(
                        factory = ReviewViewModelFactory(reviewRemoteRepository)
                    )

                    // ==================== NAVEGACION ====================

                    AppNavGraph(
                        navController = navController,
                        context = applicationContext,
                        authViewModel = authViewModel,
                        propiedadViewModel = propiedadViewModel,
                        propiedadDetalleViewModel = propiedadDetalleViewModel,
                        solicitudesViewModel = solicitudesViewModel,
                        perfilViewModel = perfilViewModel,
                        reviewViewModel = reviewViewModel
                    )
                }
            }
        }
    }
}
