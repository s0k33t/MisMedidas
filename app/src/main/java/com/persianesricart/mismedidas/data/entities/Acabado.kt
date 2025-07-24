// Acabado.kt
package com.persianesricart.mismedidas.data.ajustes.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Acabado(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String
)

