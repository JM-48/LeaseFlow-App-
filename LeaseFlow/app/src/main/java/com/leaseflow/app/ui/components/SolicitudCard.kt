package com.leaseflow.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.leaseflow.app.ui.viewmodel.SolicitudConDatos
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SolicitudCard(
    solicitudConDatos: SolicitudConDatos,
    onClick: () -> Unit,
    mostrarSolicitante: Boolean = false,
    onActualizarEstado: ((String) -> Unit)? = null,
    onCancelarSolicitud: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

            // Imagen Real desde la URL enriquecida del Property Remote Service
            solicitudConDatos.fotoUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "Foto de la propiedad",
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = solicitudConDatos.tituloPropiedad ?: "Propiedad #${solicitudConDatos.solicitud.propiedad_id}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                EstadoChip(estado = solicitudConDatos.nombreEstado ?: "PENDIENTE")
            }

            // Código e Identificadores
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Código: ${solicitudConDatos.codigoPropiedad ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Fecha: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(solicitudConDatos.solicitud.fsolicitud))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Precio Formateado Chileno/Local
            solicitudConDatos.precioMensual?.let { precio ->
                val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
                Text(
                    text = "${format.format(precio)} / mes",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Bloque de información del Solicitante (Para vistas de Propietario o Administrador Global)
            if (mostrarSolicitante && solicitudConDatos.nombreSolicitante != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(text = "Postulante:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Text(text = solicitudConDatos.nombreSolicitante, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                solicitudConDatos.emailSolicitante?.let { email ->
                    Text(text = email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // BOTONES DE GESTIÓN (Aceptar / Rechazar para Propietarios y Admins)
            if (onActualizarEstado != null && solicitudConDatos.nombreEstado == "PENDIENTE") {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onActualizarEstado("ACEPTADA") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aceptar")
                    }
                    OutlinedButton(
                        onClick = { onActualizarEstado("RECHAZADA") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rechazar")
                    }
                }
            }

            // BOTÓN DE CANCELACIÓN (Exclusivo del Arrendatario en su panel personal)
            if (onCancelarSolicitud != null && solicitudConDatos.nombreEstado == "PENDIENTE") {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                OutlinedButton(
                    onClick = onCancelarSolicitud,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cancelar Solicitud")
                }
            }
        }
    }
}

@Composable
fun EstadoChip(estado: String) {
    val color = when (estado.uppercase()) {
        "PENDIENTE" -> MaterialTheme.colorScheme.tertiary
        "ACEPTADA", "APROBADA" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.error
    }
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = estado,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontWeight = FontWeight.Bold
        )
    }
}