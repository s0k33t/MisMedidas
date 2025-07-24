package com.persianesricart.mismedidas.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Nota(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cliente: String,
    val referencia: String,
    val direccion: String,
    val poblacion: String,
    val telefono: String,
    val movil: String,
    val email: String,
    val fecha: String
)
