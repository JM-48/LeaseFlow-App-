package com.leaseflow.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.leaseflow.app.data.local.storage.UserPreferences
import com.leaseflow.app.ui.components.SolicitudCard
import com.leaseflow.app.ui.viewmodel.SolicitudesViewModel
import com.leaseflow.app.ui.viewmodel.SolicitudesViewModelFactory

/**
 * Pantalla de solicitudes multi-rol
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudesScreen(
    userPreferences: UserPreferences,
    viewModelFactory: SolicitudesViewModelFactory,
    mode: SolicitudesMode = SolicitudesMode.AUTO,
    onNavigateToDetalle: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: SolicitudesViewModel = viewModel(factory = viewModelFactory)

    // Estados del ViewModel
    val solicitudes by viewModel.solicitudes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMsg by viewModel.errorMsg.collectAsState()
    val successMsg by viewModel.successMsg.collectAsState()
    // Eliminamos 'solicitudCreada' porque no existe en el ViewModel, usamos successMsg

    // Datos del usuario
    val userId by userPreferences.userId.collectAsState(initial = null)
    val userRoleId by userPreferences.userRoleId.collectAsState(initial = null)

    // Variables locales
    val currentUserId = userId
    val currentRoleId = userRoleId ?: 3

    val effectiveMode = remember(mode, currentRoleId) {
        when (mode) {
            SolicitudesMode.AUTO -> {
                when (currentRoleId) {
                    1 -> SolicitudesMode.PERSONAL
                    2 -> SolicitudesMode.RECIBIDAS
                    else -> SolicitudesMode.PERSONAL
                }
            }
            else -> mode
        }
    }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // =====================================================================
    // CORRECCIÓN 1: Lógica de carga según el rol (Usando las funciones reales)
    // =====================================================================
    LaunchedEffect(currentUserId, currentRoleId) {
        currentUserId?.let { id ->
            when (effectiveMode) {
                SolicitudesMode.ADMIN_GLOBAL -> viewModel.cargarTodasSolicitudes()
                SolicitudesMode.RECIBIDAS -> viewModel.cargarSolicitudesPropietario(id)
                SolicitudesMode.PERSONAL -> viewModel.cargarSolicitudesArrendatario(id)
                SolicitudesMode.AUTO -> viewModel.cargarSolicitudesArrendatario(id)
            }
        }
    }

    // Mostrar mensaje de éxito
    LaunchedEffect(successMsg) {
        successMsg?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSuccess()
            // Recargar lista después de una acción exitosa
            currentUserId?.let { id ->
                when (effectiveMode) {
                    SolicitudesMode.ADMIN_GLOBAL -> viewModel.cargarTodasSolicitudes()
                    SolicitudesMode.RECIBIDAS -> viewModel.cargarSolicitudesPropietario(id)
                    SolicitudesMode.PERSONAL -> viewModel.cargarSolicitudesArrendatario(id)
                    SolicitudesMode.AUTO -> viewModel.cargarSolicitudesArrendatario(id)
                }
            }
        }
    }

    // Titulo según rol
    val titulo = when (effectiveMode) {
        SolicitudesMode.ADMIN_GLOBAL -> "Gestión de Solicitudes"
        SolicitudesMode.RECIBIDAS -> "Solicitudes Recibidas"
        else -> "Mis Solicitudes"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                actions = {
                    IconButton(
                        onClick = {
                            // CORRECCIÓN: Recargar usando la lógica correcta
                            currentUserId?.let { id ->
                                when (effectiveMode) {
                                    SolicitudesMode.ADMIN_GLOBAL -> viewModel.cargarTodasSolicitudes()
                                    SolicitudesMode.RECIBIDAS -> viewModel.cargarSolicitudesPropietario(id)
                                    SolicitudesMode.PERSONAL -> viewModel.cargarSolicitudesArrendatario(id)
                                    SolicitudesMode.AUTO -> viewModel.cargarSolicitudesArrendatario(id)
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // ESTADO: Cargando
                isLoading && solicitudes.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cargando solicitudes...")
                    }
                }

                // ESTADO: Error
                errorMsg != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Error", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.error)
                        Text(errorMsg ?: "Error desconocido", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.clearError() }) { Text("Reintentar") }
                    }
                }

                // ESTADO: Sin solicitudes
                solicitudes.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Sin solicitudes", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            text = if (currentRoleId == 2) "No has recibido solicitudes" else "No tienes solicitudes enviadas",
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // ESTADO: Lista de solicitudes
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            SolicitudesStatsCard(solicitudes = solicitudes)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(items = solicitudes, key = { it.solicitud.id }) { solicitudConDatos ->

                            // Determinar si puede gestionar (Solo admin o propietario si está PENDIENTE)
                            val puedeGestionar = when (effectiveMode) {
                                SolicitudesMode.ADMIN_GLOBAL -> true
                                SolicitudesMode.RECIBIDAS -> solicitudConDatos.nombreEstado == "PENDIENTE"
                                else -> false
                            }

                            // =====================================================================
                            // CORRECCIÓN 2: Mapear la acción a las funciones reales (aprobar/rechazar)
                            // =====================================================================
                            SolicitudCard(
                                solicitudConDatos = solicitudConDatos,
                                onClick = { onNavigateToDetalle(solicitudConDatos.solicitud.propiedad_id) },
                                mostrarSolicitante = effectiveMode != SolicitudesMode.PERSONAL,
                                onCancelarSolicitud = if (effectiveMode == SolicitudesMode.PERSONAL && solicitudConDatos.nombreEstado == "PENDIENTE") {
                                    {
                                        currentUserId?.let { uid ->
                                            viewModel.cancelarSolicitud(solicitudConDatos.solicitud.id, uid)
                                        }
                                    }
                                } else null,
                                onActualizarEstado = if (puedeGestionar) {
                                    { nuevoEstado ->
                                        if (nuevoEstado == "ACEPTADA" || nuevoEstado == "APROBADA") {
                                            viewModel.aprobarSolicitud(solicitudConDatos.solicitud.id)
                                        } else if (nuevoEstado == "RECHAZADA") {
                                            viewModel.rechazarSolicitud(solicitudConDatos.solicitud.id)
                                        }
                                    }
                                } else null
                            )
                        }
                    }

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

enum class SolicitudesMode {
    AUTO,
    PERSONAL,
    RECIBIDAS,
    ADMIN_GLOBAL
}

@Composable
private fun SolicitudesStatsCard(
    solicitudes: List<com.leaseflow.app.ui.viewmodel.SolicitudConDatos>
) {
    val pendientes = solicitudes.count { it.nombreEstado == "PENDIENTE" }
    val aceptadas = solicitudes.count { it.nombreEstado == "ACEPTADA" || it.nombreEstado == "APROBADA" }
    val rechazadas = solicitudes.count { it.nombreEstado == "RECHAZADA" }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("Pendientes", pendientes, MaterialTheme.colorScheme.tertiary)
            StatItem("Aceptadas", aceptadas, MaterialTheme.colorScheme.primary)
            StatItem("Rechazadas", rechazadas, MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun StatItem(label: String, value: Int, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), style = MaterialTheme.typography.headlineMedium, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}
