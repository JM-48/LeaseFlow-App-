package com.leaseflow.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.leaseflow.app.data.remote.dto.MensajeContactoDTO
import com.leaseflow.app.ui.viewmodel.ContactViewModel

/**
 * Pantalla de Contacto
 * Permite a usuarios enviar mensajes de contacto
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    contactViewModel: ContactViewModel,
    usuarioId: Long? = null,
    onBack: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var asunto by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var numeroTelefono by remember { mutableStateOf("") }

    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorEmail by remember { mutableStateOf<String?>(null) }
    var errorAsunto by remember { mutableStateOf<String?>(null) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }

    val isLoading by contactViewModel.isLoading.collectAsState()
    val errorMessage by contactViewModel.errorMessage.collectAsState()
    val successMessage by contactViewModel.successMessage.collectAsState()

    val scrollState = rememberScrollState()

    // Limpiar formulario después de éxito
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            nombre = ""
            email = ""
            asunto = ""
            mensaje = ""
            numeroTelefono = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contáctanos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "¿Tienes alguna consulta?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Escríbenos y te responderemos pronto",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Mensajes de error/éxito
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = error, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            successMessage?.let { success ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Éxito",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = success, color = MaterialTheme.colorScheme.primaryContainer)
                    }
                }
            }

            // Formulario
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Nombre
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            errorNombre = if (it.isBlank()) "El nombre es obligatorio" else null
                        },
                        label = { Text("Nombre completo *") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorNombre != null,
                        supportingText = errorNombre?.let { { Text(it) } }
                    )

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            val (isValid, error) = contactViewModel.validarEmail(it)
                            errorEmail = error
                        },
                        label = { Text("Email *") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorEmail != null,
                        supportingText = errorEmail?.let { { Text(it) } }
                    )

                    // Teléfono (opcional)
                    OutlinedTextField(
                        value = numeroTelefono,
                        onValueChange = { numeroTelefono = it },
                        label = { Text("Teléfono (opcional)") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Asunto
                    OutlinedTextField(
                        value = asunto,
                        onValueChange = {
                            asunto = it
                            val (isValid, error) = contactViewModel.validarAsunto(it)
                            errorAsunto = error
                        },
                        label = { Text("Asunto *") },
                        leadingIcon = {
                            Icon(Icons.Default.Info, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorAsunto != null,
                        supportingText = errorAsunto?.let { { Text(it) } }
                    )

                    // Mensaje
                    OutlinedTextField(
                        value = mensaje,
                        onValueChange = {
                            mensaje = it
                            val (isValid, error) = contactViewModel.validarMensaje(it)
                            errorMensaje = error
                        },
                        label = { Text("Mensaje *") },
                        placeholder = { Text("Escribe tu consulta aquí...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        maxLines = 10,
                        isError = errorMensaje != null,
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = errorMensaje ?: "",
                                    color = if (errorMensaje != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${mensaje.length}/5000",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )

                    Text(
                        text = "* Campos obligatorios",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Botón enviar
            Button(
                onClick = {
                    // Validar todo
                    var hasErrors = false

                    if (nombre.isBlank()) {
                        errorNombre = "El nombre es obligatorio"
                        hasErrors = true
                    }

                    val (emailValid, emailError) = contactViewModel.validarEmail(email)
                    if (!emailValid) {
                        errorEmail = emailError
                        hasErrors = true
                    }

                    val (asuntoValid, asuntoError) = contactViewModel.validarAsunto(asunto)
                    if (!asuntoValid) {
                        errorAsunto = asuntoError
                        hasErrors = true
                    }

                    val (mensajeValid, mensajeError) = contactViewModel.validarMensaje(mensaje)
                    if (!mensajeValid) {
                        errorMensaje = mensajeError
                        hasErrors = true
                    }

                    if (!hasErrors) {
                        contactViewModel.crearMensaje(
                            nombre = nombre,
                            email = email,
                            asunto = asunto,
                            mensaje = mensaje,
                            numeroTelefono = numeroTelefono.ifBlank { null },
                            usuarioId = usuarioId
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar Mensaje")
                }
            }

            // Info adicional
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Otros medios de contacto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("support@leaseflow.com", style = MaterialTheme.typography.bodyMedium)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("+56 9 1234 5678", style = MaterialTheme.typography.bodyMedium)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lun - Vie: 9:00 - 18:00", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionContactoScreen(
    contactViewModel: ContactViewModel,
    adminId: Long,
    onBack: () -> Unit
) {
    val mensajes by contactViewModel.mensajes.collectAsState()
    val estadisticas by contactViewModel.estadisticas.collectAsState()
    val isLoading by contactViewModel.isLoading.collectAsState()
    val errorMessage by contactViewModel.errorMessage.collectAsState()
    val successMessage by contactViewModel.successMessage.collectAsState()

    var busqueda by remember { mutableStateOf("") }
    var filtroSeleccionado by remember { mutableStateOf("TODOS") }

    var detalleVisible by remember { mutableStateOf(false) }
    var mensajeSeleccionado by remember { mutableStateOf<MensajeContactoDTO?>(null) }
    var respuesta by remember { mutableStateOf("") }
    var nuevoEstado by remember { mutableStateOf("EN_PROCESO") }
    var confirmarEliminar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        contactViewModel.cargarTodosMensajes()
        contactViewModel.cargarEstadisticas()
    }

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            contactViewModel.cargarEstadisticas()
            contactViewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Contacto") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        filtroSeleccionado = "TODOS"
                        contactViewModel.cargarTodosMensajes()
                        contactViewModel.cargarEstadisticas()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text("Buscar") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    Row {
                        if (busqueda.isNotBlank()) {
                            IconButton(onClick = { busqueda = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                        IconButton(onClick = {
                            if (busqueda.isBlank()) {
                                contactViewModel.cargarTodosMensajes()
                            } else {
                                contactViewModel.buscarMensajes(busqueda.trim())
                            }
                        }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Buscar")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filtroSeleccionado == "TODOS",
                    onClick = {
                        filtroSeleccionado = "TODOS"
                        contactViewModel.cargarTodosMensajes()
                    },
                    label = { Text("Todos") }
                )
                FilterChip(
                    selected = filtroSeleccionado == "PENDIENTE",
                    onClick = {
                        filtroSeleccionado = "PENDIENTE"
                        contactViewModel.cargarMensajesPorEstado("PENDIENTE")
                    },
                    label = { Text("Pendientes") }
                )
                FilterChip(
                    selected = filtroSeleccionado == "EN_PROCESO",
                    onClick = {
                        filtroSeleccionado = "EN_PROCESO"
                        contactViewModel.cargarMensajesPorEstado("EN_PROCESO")
                    },
                    label = { Text("En proceso") }
                )
                FilterChip(
                    selected = filtroSeleccionado == "RESUELTO",
                    onClick = {
                        filtroSeleccionado = "RESUELTO"
                        contactViewModel.cargarMensajesPorEstado("RESUELTO")
                    },
                    label = { Text("Resueltos") }
                )
            }

            if (estadisticas.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total: ${estadisticas["total"] ?: 0}")
                        Text("Pend.: ${estadisticas["pendientes"] ?: 0}")
                        Text("Proc.: ${estadisticas["enProceso"] ?: 0}")
                        Text("Res.: ${estadisticas["resueltos"] ?: 0}")
                    }
                }
            }

            errorMessage?.let { error ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(error, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            when {
                isLoading && mensajes.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                mensajes.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay mensajes")
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(mensajes, key = { it.id ?: 0L }) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    mensajeSeleccionado = item
                                    respuesta = item.respuesta.orEmpty()
                                    nuevoEstado = item.estado ?: "EN_PROCESO"
                                    detalleVisible = true
                                }
                            ) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.asunto,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(item.estado ?: "PENDIENTE") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = when (item.estado) {
                                                        "RESUELTO" -> Icons.Default.CheckCircle
                                                        "EN_PROCESO" -> Icons.Default.Schedule
                                                        else -> Icons.Default.Pending
                                                    },
                                                    contentDescription = null
                                                )
                                            }
                                        )
                                    }

                                    Text(
                                        text = item.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = item.mensaje,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (detalleVisible && mensajeSeleccionado != null) {
            val item = mensajeSeleccionado!!
            AlertDialog(
                onDismissRequest = {
                    detalleVisible = false
                    mensajeSeleccionado = null
                    respuesta = ""
                    nuevoEstado = "EN_PROCESO"
                    confirmarEliminar = false
                },
                title = { Text("Mensaje #${item.id ?: 0}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Nombre: ${item.nombre}")
                        Text("Email: ${item.email}")
                        item.numeroTelefono?.takeIf { it.isNotBlank() }?.let { tel -> Text("Teléfono: $tel") }
                        Text("Asunto: ${item.asunto}", fontWeight = FontWeight.Bold)
                        Text(item.mensaje)

                        Spacer(Modifier.height(6.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = nuevoEstado == "PENDIENTE",
                                onClick = { nuevoEstado = "PENDIENTE" },
                                label = { Text("Pendiente") }
                            )
                            FilterChip(
                                selected = nuevoEstado == "EN_PROCESO",
                                onClick = { nuevoEstado = "EN_PROCESO" },
                                label = { Text("En proceso") }
                            )
                            FilterChip(
                                selected = nuevoEstado == "RESUELTO",
                                onClick = { nuevoEstado = "RESUELTO" },
                                label = { Text("Resuelto") }
                            )
                        }

                        OutlinedTextField(
                            value = respuesta,
                            onValueChange = { respuesta = it },
                            label = { Text("Respuesta") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp)
                        )
                    }
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            enabled = !isLoading,
                            onClick = {
                                contactViewModel.actualizarEstado(item.id ?: return@TextButton, nuevoEstado)
                            }
                        ) {
                            Text("Actualizar estado")
                        }
                        Button(
                            enabled = !isLoading && respuesta.isNotBlank(),
                            onClick = {
                                contactViewModel.responderMensaje(
                                    mensajeId = item.id ?: return@Button,
                                    respuesta = respuesta.trim(),
                                    respondidoPor = adminId,
                                    nuevoEstado = nuevoEstado
                                )
                            }
                        ) {
                            Text("Responder")
                        }
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            enabled = !isLoading,
                            onClick = {
                                confirmarEliminar = true
                            }
                        ) {
                            Text("Eliminar")
                        }
                        TextButton(onClick = {
                            detalleVisible = false
                            mensajeSeleccionado = null
                            respuesta = ""
                            nuevoEstado = "EN_PROCESO"
                            confirmarEliminar = false
                        }) {
                            Text("Cerrar")
                        }
                    }
                }
            )
        }

        if (confirmarEliminar && mensajeSeleccionado != null) {
            val item = mensajeSeleccionado!!
            AlertDialog(
                onDismissRequest = { confirmarEliminar = false },
                title = { Text("Eliminar mensaje") },
                text = { Text("¿Eliminar el mensaje #${item.id ?: 0}?") },
                confirmButton = {
                    Button(
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            contactViewModel.eliminarMensaje(item.id ?: return@Button, adminId)
                            confirmarEliminar = false
                            detalleVisible = false
                            mensajeSeleccionado = null
                            respuesta = ""
                            nuevoEstado = "EN_PROCESO"
                        }
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmarEliminar = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
