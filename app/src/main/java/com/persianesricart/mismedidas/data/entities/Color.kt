// Color.kt
package com.persianesricart.mismedidas.data.ajustes.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Color(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val modeloId: Int,
    val orden: Int
)