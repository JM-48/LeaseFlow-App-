package com.leaseflow.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.leaseflow.app.data.local.LeaseFlowDatabase
import com.leaseflow.app.data.local.storage.UserPreferences
import com.leaseflow.app.data.remote.RetrofitClient
import com.leaseflow.app.data.repository.ApplicationRemoteRepository
import com.leaseflow.app.data.repository.PropertyRemoteRepository
import com.leaseflow.app.data.repository.UserRepository
import com.leaseflow.app.ui.viewmodel.AdminPanelViewModel
import com.leaseflow.app.ui.viewmodel.AdminPanelViewModelFactory
import com.leaseflow.app.ui.viewmodel.UserManagementViewModel
import com.leaseflow.app.ui.viewmodel.UserManagementViewModelFactory

/**
 * Panel de Administración - Dashboard para ADMIN
 * ✅ CORREGIDO: Llama correctamente a UserManagementScreen y corrige todos los warnings e imports.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onBack: () -> Unit,
    onGestionPropiedades: () -> Unit,
    onGestionDocumentos: () -> Unit,
    onGestionContacto: () -> Unit
) {
    val context = LocalContext.current
    val db = LeaseFlowDatabase.getInstance(context)
    val userPrefsFlow = remember { UserPreferences(context).data }
    val prefs = context.getSharedPreferences("LeaseFlowPrefs", 0)
    val currentRol = prefs.getLong("currentUserRolId", -1L)
    val isAdmin = currentRol == 1L

    val userRepository = remember { UserRepository(RetrofitClient.userServiceApi) }
    val propertyRepository = remember { PropertyRemoteRepository() }
    val applicationRepository = remember {
        ApplicationRemoteRepository(
            solicitudDao = db.solicitudDao(),
            catalogDao = db.catalogDao()
        )
    }

    val vm: AdminPanelViewModel = viewModel(
        factory = AdminPanelViewModelFactory(
            userRepository,
            propertyRepository,
            applicationRepository,
            userPrefsFlow
        )
    )

    val stats by vm.estadisticas.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()

    var mostrarUserManagement by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.cargarEstadisticas()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (mostrarUserManagement) "Gestión de Usuarios" else "Panel de Administración") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (mostrarUserManagement) {
                            mostrarUserManagement = false
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (mostrarUserManagement) {
                // Inyectar el ViewModel para la gestión de usuarios
                val userRepository = UserRepository(RetrofitClient.userServiceApi)
                val userManagementViewModel: UserManagementViewModel = viewModel(
                    factory = UserManagementViewModelFactory(userRepository, userPrefsFlow)
                )

                val users by userManagementViewModel.users.collectAsStateWithLifecycle()
                val userIsLoading by userManagementViewModel.isLoading.collectAsStateWithLifecycle()
                val userError by userManagementViewModel.error.collectAsStateWithLifecycle()

                UserManagementScreen(
                    users = users,
                    isLoading = userIsLoading,
                    error = userError,
                    onBack = { mostrarUserManagement = false },
                    onUpdateUser = { user -> userManagementViewModel.updateUser(user.id!!, user) },
                    onDeleteUser = { user -> userManagementViewModel.deleteUser(user.id!!) },
                    onRetry = { userManagementViewModel.loadUsers() }
                )
            } else {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Header
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.AdminPanelSettings,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(
                                        "Panel de Administración",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Gestión completa del sistema LeaseFlow",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Estadísticas
                        Text(
                            "Estadísticas Generales",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = "Usuarios",
                                value = "${stats.totalUsuarios}",
                                icon = Icons.Filled.People,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Propiedades",
                                value = "${stats.totalPropiedades}",
                                icon = Icons.Filled.Business,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = "Solicitudes",
                                value = "${stats.totalSolicitudes}",
                                icon = Icons.AutoMirrored.Filled.Assignment,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Activas",
                                value = "${stats.propiedadesActivas}",
                                icon = Icons.Filled.CheckCircle,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        // Acciones Rápidas
                        Text(
                            "Acciones Rápidas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(12.dp))

                        // --- Solo el admin ve estas acciones ---
                        if (isAdmin) {
                            ActionCard(
                                title = "Gestión de Usuarios",
                                description = "Administrar usuarios, roles y permisos",
                                icon = Icons.Filled.People,
                                onClick = { mostrarUserManagement = true }
                            )

                            Spacer(Modifier.height(12.dp))

                            ActionCard(
                                title = "Gestión de Propiedades",
                                description = "Ver y administrar todas las propiedades publicadas",
                                icon = Icons.Filled.Business,
                                onClick = onGestionPropiedades
                            )

                            Spacer(Modifier.height(12.dp))

                            ActionCard(
                                title = "Gestión de Documentos",
                                description = "Aprobar o rechazar documentación cargada",
                                icon = Icons.Filled.Description,
                                onClick = onGestionDocumentos
                            )

                            Spacer(Modifier.height(12.dp))

                            ActionCard(
                                title = "Gestión de Contacto",
                                description = "Ver y responder mensajes de Contáctanos",
                                icon = Icons.Filled.ContactMail,
                                onClick = onGestionContacto
                            )
                        }

                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.secondaryContainer
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
