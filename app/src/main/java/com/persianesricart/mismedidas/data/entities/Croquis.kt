package com.persianesricart.mismedidas.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "croquis",
    foreignKeys = [
        ForeignKey(
            entity = Medida::class,
            parentColumns = ["id"],
            childColumns = ["medidaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medidaId")]
)
data class Croquis(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medidaId: Int,
    val nombre: String,
    val filePath: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
