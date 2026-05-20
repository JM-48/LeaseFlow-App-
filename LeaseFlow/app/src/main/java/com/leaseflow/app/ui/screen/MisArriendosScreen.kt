package com.leaseflow.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leaseflow.app.data.local.LeaseFlowDatabase
import com.leaseflow.app.data.local.storage.UserPreferences
import com.leaseflow.app.data.remote.ApiResult
import com.leaseflow.app.data.remote.dto.RegistroArriendoDTO
import com.leaseflow.app.data.repository.ApplicationRemoteRepository
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisArriendosScreen(
    userPreferences: UserPreferences,
    onBack: () -> Unit,
    onVerPropiedad: (Long) -> Unit
) {
    val context = LocalContext.current
    val userId by userPreferences.userId.collectAsStateWithLifecycle(initialValue = null)

    val database = remember { LeaseFlowDatabase.getInstance(context) }
    val applicationRepository = remember {
        ApplicationRemoteRepository(
            solicitudDao = database.solicitudDao(),
            catalogDao = database.catalogDao()
        )
    }

    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var registros by remember { mutableStateOf<List<RegistroArriendoDTO>>(emptyList()) }

    val numberFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "CL")) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    suspend fun cargar(uid: Long) {
        isLoading = true
        error = null
        registros = emptyList()
        when (val result = applicationRepository.listarTodosRegistros(includeDetails = true)) {
            is ApiResult.Success -> {
                registros = result.data
                    .filter { it.activo == true && it.solicitud?.usuarioId == uid }
                    .sortedByDescending { it.fechaInicio.time }
            }
            is ApiResult.Error -> error = result.message
            is ApiResult.Loading -> {}
        }
        isLoading = false
    }

    LaunchedEffect(userId) {
        val uid = userId ?: return@LaunchedEffect
        cargar(uid)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Arriendos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val uid = userId ?: return@IconButton
                            scope.launch { cargar(uid) }
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                userId == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Debes iniciar sesion para ver tus arriendos")
                    }
                }
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(error ?: "Error", color = MaterialTheme.colorScheme.error)
                    }
                }
                registros.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("No tienes arriendos vigentes")
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(registros, key = { it.id ?: 0L }) { registro ->
                            val solicitud = registro.solicitud
                            val propiedad = solicitud?.propiedad
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = propiedad?.titulo ?: "Propiedad",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                                        Text(text = propiedad?.codigo ?: "N/A", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "Inicio: ${dateFormat.format(Date(registro.fechaInicio.time))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Monto: ${numberFormat.format(registro.montoMensual)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    TextButton(
                                        onClick = { propiedad?.id?.let(onVerPropiedad) }
                                    ) {
                                        Text("Ver Propiedad")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
