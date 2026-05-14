package com.leaseflow.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leaseflow.app.ui.theme.LfCyan
import com.leaseflow.app.ui.theme.LfNavy
import com.leaseflow.app.ui.theme.LfPink

/**
 * Chip para mostrar el estado de una solicitud con colores apropiados
 */
@Composable
fun EstadoChip(
    estado: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (estado.uppercase()) {
        "PENDIENTE" -> Pair(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.primary
        )
        "ACEPTADA", "APROBADA", "APROBADO" -> Pair(
            LfCyan.copy(alpha = 0.18f),
            LfNavy
        )
        "RECHAZADA", "RECHAZADO" -> Pair(
            LfPink.copy(alpha = 0.20f),
            LfNavy
        )
        else -> Pair(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    val displayText = when (estado.uppercase()) {
        "ACEPTADA", "APROBADA", "APROBADO" -> "Aceptada"
        "RECHAZADA", "RECHAZADO" -> "Rechazada"
        "PENDIENTE" -> "Pendiente"
        else -> estado
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Text(
            text = displayText,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = textColor
        )
    }
}
