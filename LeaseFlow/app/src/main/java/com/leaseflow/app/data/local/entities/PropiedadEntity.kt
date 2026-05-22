package com.leaseflow.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para la tabla PROPIEDAD
 * Inmuebles disponibles para arriendo en LeaseFlow
 */
@Entity(
    tableName = "propiedad",
    // ELIMINAMOS EL BLOQUE 'foreignKeys' POR COMPLETO
    indices = [
        Index(value = ["codigo"], unique = true),
        Index("estado_id"),
        Index("tipo_id"),
        Index("comuna_id"),
        Index("propietario_id")
    ]
)
data class PropiedadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // Identificación
    val codigo: String,
    val titulo: String,

    // Precio
    val precio_mensual: Int,
    val divisa: String = "CLP",

    // Características
    val m2: Double,
    val n_habit: Int,
    val n_banos: Int,
    val pet_friendly: Boolean,
    val direccion: String,

    val descripcion: String? = null,

    // Auditoría
    val fcreacion: Long,

    // Relaciones (Solo los IDs, sin que Room obligue a que existan en otras tablas)
    val estado_id: Long,
    val tipo_id: Long,
    val comuna_id: Long,
    val propietario_id: Long?
)