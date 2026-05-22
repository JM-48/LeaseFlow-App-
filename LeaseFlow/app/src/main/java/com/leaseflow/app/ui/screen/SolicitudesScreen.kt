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

enum class SolicitudesMode { ARRENDATARIO, PROPIETARIO, ADMIN_GLOBAL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudesScreen(
    userPreferences: UserPreferences,
    viewModelFactory: SolicitudesViewModelFactory,
    mode: SolicitudesMode,
    onNavigateToDetalle: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: SolicitudesViewModel = viewModel(factory = viewModelFactory)

    val solicitudes by viewModel.solicitudes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMsg by viewModel.errorMsg.collectAsState()
    val successMsg by viewModel.successMsg.collectAsState()

    // Resuelve la sesión del usuario guardada en DataStore / SharedPreferences
    val userId by userPreferences.userId.collectAsState(initial = null)
    val snackbarHostState = remember { SnackbarHostState() }

    // Función lambda reutilizable para refrescar de acuerdo al modo activo
    val ejecutarCarga = {
        userId?.let { id ->
            when (mode) {
                SolicitudesMode.ARRENDATARIO -> viewModel.cargarSolicitudesArrendatario(id)
                SolicitudesMode.PROPIETARIO -> viewModel.cargarSolicitudesPropietario(id)
                SolicitudesMode.ADMIN_GLOBAL -> viewModel.cargarTodasSolicitudes()
            }
        }
    }

    // Carga inicial al montar la pantalla o cambiar de pestaña
    LaunchedEffect(userId, mode) {
        ejecutarCarga()
    }

    // Escucha notificaciones de éxito (Mensajes de confirmación)
    LaunchedEffect(successMsg) {
        successMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSuccess()
        }
    }

    val tituloBarra = when (mode) {
        SolicitudesMode.ARRENDATARIO -> "Mis Solicitudes Enviadas"
        SolicitudesMode.PROPIETARIO -> "Solicitudes Recibidas"
        SolicitudesMode.ADMIN_GLOBAL -> "Gestor Global de Solicitudes"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tituloBarra) },
                actions = {
                    IconButton(onClick = { ejecutarCarga() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading && solicitudes.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Sincronizando con LeaseFlow...", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                errorMsg != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Error de comunicación", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                        Text(errorMsg ?: "Ocurrió un problema inesperado", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            viewModel.clearError()
                            ejecutarCarga()
                        }) {
                            Text("Reintentar Conexión")
                        }
                    }
                }

                solicitudes.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No se registran solicitudes para mostrar en esta sección.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = solicitudes, key = { it.solicitud.id }) { item ->
                            SolicitudCard(
                                solicitudConDatos = item,
                                onClick = { onNavigateToDetalle(item.solicitud.id) },
                                mostrarSolicitante = (mode != SolicitudesMode.ARRENDATARIO),
                                onActualizarEstado = if (mode != SolicitudesMode.ARRENDATARIO) {
                                    { nuevoEstado ->
                                        if (nuevoEstado == "ACEPTADA") {
                                            viewModel.aprobarSolicitud(item.solicitud.id)
                                        } else {
                                            viewModel.rechazarSolicitud(item.solicitud.id)
                                        }
                                    }
                                } else null,
                                onCancelarSolicitud = if (mode == SolicitudesMode.ARRENDATARIO) {
                                    { userId?.let { uid -> viewModel.cancelarSolicitud(item.solicitud.id, uid) } }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    }
}