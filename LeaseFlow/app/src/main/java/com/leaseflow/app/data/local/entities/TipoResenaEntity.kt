package com.leaseflow.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad para la tabla TIPO_RESENA
 * Tipos de reseña (a propiedad, a propietario, a inquilino)
 */
@Entity(tableName = "tipo_resena")
data class TipoResenaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombre: String                // ej: "Reseña Propiedad", "Reseña Propietario", "Reseña Inquilino"
)
