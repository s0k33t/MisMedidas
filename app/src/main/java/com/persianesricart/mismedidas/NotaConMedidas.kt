package com.persianesricart.mismedidas.data.entities

import androidx.room.Embedded
import androidx.room.Relation

data class NotaConMedidas(
    @Embedded val nota: Nota,
    @Relation(
        parentColumn = "id",
        entityColumn = "notaId"
    )
    val medidas: List<Medida>
)
