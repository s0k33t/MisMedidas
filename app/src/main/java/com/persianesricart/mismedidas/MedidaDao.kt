package com.persianesricart.mismedidas.data.dao

import androidx.room.*
import com.persianesricart.mismedidas.data.entities.Medida

@Dao
interface MedidaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedida(medida: Medida)

    @Query("SELECT * FROM medida WHERE notaId = :notaId")
    suspend fun getMedidasByNota(notaId: Int): List<Medida>

}
