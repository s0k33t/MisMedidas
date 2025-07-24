package com.persianesricart.mismedidas.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity
data class Medida(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val notaId: Int,
    val ud: String,
    val ancho: String,
    val alto: String,
    val luz: Boolean = false,
    val cargoAncho: String? = null,
    val cargoAlto: String? = null,
    val tipo: String,
    val modelo: String,
    val acabado: String? = null,
    val color: String,
    val motor: String? = "Mecanico",
    val comentario: String
)
