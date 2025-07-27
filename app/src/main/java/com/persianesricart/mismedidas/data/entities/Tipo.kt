// Tipo.kt
package com.persianesricart.mismedidas.data.ajustes.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Tipo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val orden: Int
)