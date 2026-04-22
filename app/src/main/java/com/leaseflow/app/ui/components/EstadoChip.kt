package com.leaseflow.app.ui.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun EstadoChip(estado: String) {
    val containerColor = when (estado.lowercase()) {
        "aprobada", "aprobado" -> MaterialTheme.colorScheme.primaryContainer
        "rechazada", "rechazado" -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    AssistChip(
        onClick = {},
        label = { Text(text = estado) },
        colors = AssistChipDefaults.assistChipColors(containerColor = containerColor),
    )
}
