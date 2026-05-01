package com.leaseflow.app.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leaseflow.app.data.model.DocumentoRegistro
import com.leaseflow.app.data.model.DocumentosRegistroState
import com.leaseflow.app.data.model.TipoDocumentoRegistro
import com.leaseflow.app.ui.viewmodel.LeaseFlowAuthViewModel

/**
 * Pantalla de registro con documentos y segundo nombre opcional.
 */
@Composable
fun RegisterScreenVm(
    vm: LeaseFlowAuthViewModel,
    onRegisteredNavigateLogin: () -> Unit,
    onGoLogin: () -> Unit
) {
    val state by vm.register.collectAsStateWithLifecycle()
    val documentosState by vm.documentosRegistro.collectAsStateWithLifecycle()

    if (state.success) {
        vm.clearRegisterResult()
        onRegisteredNavigateLogin()
    }

    RegisterScreen(
        pnombre = state.pnombre,
        snombre = state.snombre,
        papellido = state.papellido,
        fechaNacimiento = state.fechaNacimiento,
        email = state.email,
        rut = state.rut,
        telefono = state.telefono,
        pass = state.pass,
        confirm = state.confirm,
        codigoReferido = state.codigoReferido,

        pnombreError = state.pnombreError,
        snombreError = state.snombreError,
        papellidoError = state.papellidoError,
        fechaNacimientoError = state.fechaNacimientoError,
        emailError = state.emailError,
        rutError = state.rutError,
        telefonoError = state.telefonoError,
        passError = state.passError,
        confirmError = state.confirmError,
        codigoReferidoError = state.codigoReferidoError,

        canSubmit = state.canSubmit,
        isSubmitting = state.isSubmitting,
        errorMsg = state.errorMsg,
        isDuocDetected = state.isDuocDetected,

        rolSeleccionado = state.rolSeleccionado ?: "",
        onRolChange = vm::onRolChange,

        // Documentos
        documentosState = documentosState,
        onDocumentoSeleccionado = vm::onDocumentoSeleccionado,
        onDocumentoEliminado = vm::onDocumentoEliminado,

        onPnombreChange = vm::onPnombreChange,
        onSnombreChange = vm::onSnombreChange,
        onPapellidoChange = vm::onPapellidoChange,
        onFechaNacimientoChange = vm::onFechaNacimientoChange,
        onEmailChange = vm::onRegisterEmailChange,
        onRutChange = vm::onRutChange,
        onTelefonoChange = vm::onTelefonoChange,
        onPassChange = vm::onRegisterPassChange,
        onConfirmChange = vm::onConfirmChange,
        onCodigoReferidoChange = vm::onCodigoReferidoChange,

        onSubmit = vm::submitRegister,
        onGoLogin = onGoLogin
    )
}

@Composable
private fun RegisterScreen(
    pnombre: String,
    snombre: String,
    papellido: String,
    fechaNacimiento: TextFieldValue,
    email: String,
    rut: String,
    telefono: String,
    pass: String,
    confirm: String,
    codigoReferido: String,

    pnombreError: String?,
    snombreError: String?,
    papellidoError: String?,
    fechaNacimientoError: String?,
    emailError: String?,
    rutError: String?,
    telefonoError: String?,
    passError: String?,
    confirmError: String?,
    codigoReferidoError: String?,

    canSubmit: Boolean,
    isSubmitting: Boolean,
    errorMsg: String?,
    isDuocDetected: Boolean,

    rolSeleccionado: String,
    onRolChange: (String) -> Unit,

    // Documentos
    documentosState: DocumentosRegistroState,
    onDocumentoSeleccionado: (TipoDocumentoRegistro, Uri, String) -> Unit,
    onDocumentoEliminado: (TipoDocumentoRegistro) -> Unit,

    onPnombreChange: (String) -> Unit,
    onSnombreChange: (String) -> Unit,
    onPapellidoChange: (String) -> Unit,
    onFechaNacimientoChange: (TextFieldValue) -> Unit,
    onEmailChange: (String) -> Unit,
    onRutChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onPassChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onCodigoReferidoChange: (String) -> Unit,

    onSubmit: () -> Unit,
    onGoLogin: () -> Unit
) {
    val bg = MaterialTheme.colorScheme.background
    var showPass by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Estado para el picker de documentos
    var tipoDocumentoActual by remember { mutableStateOf<TipoDocumentoRegistro?>(null) }

    // Launcher para seleccionar archivos
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            tipoDocumentoActual?.let { tipo ->
                val nombreArchivo = "${tipo.name}_${System.currentTimeMillis()}"
                onDocumentoSeleccionado(tipo, selectedUri, nombreArchivo)
            }
        }
        tipoDocumentoActual = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ========== ENCABEZADO ==========
            Text(
                text = "Únete a LeaseFlow",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Encuentra tu hogar ideal de forma simple y segura",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(24.dp))

            // ========== SECCIÓN: DATOS PERSONALES ==========
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Datos Personales",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pnombre,
                        onValueChange = onPnombreChange,
                        label = { Text("Primer Nombre *") },
                        singleLine = true,
                        isError = pnombreError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pnombreError != null) {
                        Text(pnombreError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(8.dp))

                    // ✅ SEGUNDO NOMBRE AHORA ES OPCIONAL
                    OutlinedTextField(
                        value = snombre,
                        onValueChange = onSnombreChange,
                        label = { Text("Segundo Nombre (Opcional)") },
                        singleLine = true,
                        isError = snombreError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (snombreError != null) {
                        Text(snombreError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = papellido,
                        onValueChange = onPapellidoChange,
                        label = { Text("Apellido Paterno *") },
                        singleLine = true,
                        isError = papellidoError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (papellidoError != null) {
                        Text(papellidoError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fechaNacimiento,
                        onValueChange = onFechaNacimientoChange,
                        label = { Text("Fecha de Nacimiento *") },
                        singleLine = true,
                        isError = fechaNacimientoError != null,
                        placeholder = { Text("DD/MM/AAAA") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (fechaNacimientoError != null) {
                        Text(fechaNacimientoError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ========== SECCIÓN: TIPO DE USUARIO ==========
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "¿Cómo usarás LeaseFlow? *",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Botón Arrendatario
                        OutlinedButton(
                            onClick = { onRolChange("Arrendatario") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (rolSeleccionado == "Arrendatario")
                                    MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (rolSeleccionado == "Arrendatario")
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Search, contentDescription = null)
                                Text("Arrendatario", style = MaterialTheme.typography.labelMedium)
                                Text("Busco arriendo", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        // Botón Propietario
                        OutlinedButton(
                            onClick = { onRolChange("Propietario") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (rolSeleccionado == "Propietario")
                                    MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (rolSeleccionado == "Propietario")
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Home, contentDescription = null)
                                Text("Propietario", style = MaterialTheme.typography.labelMedium)
                                Text("Publico propiedades", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    if (rolSeleccionado.isBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Selecciona un tipo de usuario",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ========== SECCIÓN: CONTACTO ==========
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Información de Contacto",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = { Text("Email *") },
                        singleLine = true,
                        isError = emailError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (emailError != null) {
                        Text(emailError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }

                    if (isDuocDetected) {
                        Spacer(Modifier.height(4.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "🎉 ¡Eres DUOC VIP! 20% descuento de por vida en comisión de servicio",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = rut,
                        onValueChange = onRutChange,
                        label = { Text("RUT *") },
                        singleLine = true,
                        isError = rutError != null,
                        placeholder = { Text("12345678-9") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (rutError != null) {
                        Text(rutError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = telefono,
                        onValueChange = onTelefonoChange,
                        label = { Text("Teléfono *") },
                        singleLine = true,
                        isError = telefonoError != null,
                        placeholder = { Text("+56912345678") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (telefonoError != null) {
                        Text(telefonoError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ========== SECCIÓN: DOCUMENTACIÓN ==========
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Documentación",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Sube los documentos requeridos para verificar tu identidad. Los documentos serán revisados por nuestro equipo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))

                    // Documentos Obligatorios
                    Text(
                        "Documentos Obligatorios",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(8.dp))

                    TipoDocumentoRegistro.obligatorios.forEach { tipo ->
                        DocumentoItem(
                            tipo = tipo,
                            documento = documentosState.obtenerDocumento(tipo),
                            onSeleccionar = {
                                tipoDocumentoActual = tipo
                                filePickerLauncher.launch("image/*")
                            },
                            onEliminar = { onDocumentoEliminado(tipo) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    Spacer(Modifier.height(16.dp))

                    // Documentos Opcionales
                    Text(
                        "Documentos Opcionales",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Acelera la aprobación de tus solicitudes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))

                    TipoDocumentoRegistro.opcionales.forEach { tipo ->
                        DocumentoItem(
                            tipo = tipo,
                            documento = documentosState.obtenerDocumento(tipo),
                            onSeleccionar = {
                                tipoDocumentoActual = tipo
                                filePickerLauncher.launch("*/*")
                            },
                            onEliminar = { onDocumentoEliminado(tipo) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    // Resumen de documentos
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (documentosState.todosObligatoriosCargados)
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (documentosState.todosObligatoriosCargados)
                                        Icons.Default.CheckCircle
                                    else Icons.Default.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    if (documentosState.todosObligatoriosCargados)
                                        "${documentosState.cantidadCargados} documento(s)"
                                    else "Falta DNI obligatorio",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ========== SECCIÓN: SEGURIDAD ==========
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Seguridad",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pass,
                        onValueChange = onPassChange,
                        label = { Text("Contraseña *") },
                        singleLine = true,
                        isError = passError != null,
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPass = !showPass }) {
                                Icon(
                                    imageVector = if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showPass) "Ocultar" else "Mostrar"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (passError != null) {
                        Text(passError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirm,
                        onValueChange = onConfirmChange,
                        label = { Text("Confirmar Contraseña *") },
                        singleLine = true,
                        isError = confirmError != null,
                        visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showConfirm = !showConfirm }) {
                                Icon(
                                    imageVector = if (showConfirm) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showConfirm) "Ocultar" else "Mostrar"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (confirmError != null) {
                        Text(confirmError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ========== SECCIÓN: CÓDIGO REFERIDO (OPCIONAL) ==========
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "¿Tienes un código de referido? (Opcional)",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = codigoReferido,
                        onValueChange = onCodigoReferidoChange,
                        label = { Text("Código Referido") },
                        singleLine = true,
                        isError = codigoReferidoError != null,
                        placeholder = { Text("ABC12345") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (codigoReferidoError != null) {
                        Text(codigoReferidoError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Gana LeaseFlowPoints al registrarte con un código",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ========== BOTÓN REGISTRAR ==========
            Button(
                onClick = onSubmit,
                enabled = canSubmit && !isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Creando cuenta...")
                } else {
                    Text("Registrarme en LeaseFlow")
                }
            }

            if (errorMsg != null) {
                Spacer(Modifier.height(8.dp))
                Text(errorMsg, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(onClick = onGoLogin, modifier = Modifier.fillMaxWidth()) {
                Text("Ya tengo cuenta - Iniciar Sesión")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * Componente para mostrar un item de documento.
 */
@Composable
private fun DocumentoItem(
    tipo: TipoDocumentoRegistro,
    documento: DocumentoRegistro?,
    onSeleccionar: () -> Unit,
    onEliminar: () -> Unit
) {
    val estaCargado = documento != null
    val borderColor = when {
        estaCargado -> MaterialTheme.colorScheme.primary
        tipo.esObligatorio -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.outline
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (estaCargado)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (estaCargado) Icons.Default.CheckCircle else Icons.Default.Upload,
                    contentDescription = null,
                    tint = if (estaCargado)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(12.dp))

            // Info del documento
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tipo.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (tipo.esObligatorio) {
                        Text(
                            text = " *",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (estaCargado) {
                    Text(
                        text = documento!!.nombreArchivo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = tipo.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Botón de acción
            if (estaCargado) {
                IconButton(onClick = onEliminar) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                IconButton(onClick = onSeleccionar) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Agregar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
