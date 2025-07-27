// AcabadoDao.kt
package com.persianesricart.mismedidas.data.ajustes.dao

import androidx.room.*
import com.persianesricart.mismedidas.data.ajustes.entities.Acabado
import com.persianesricart.mismedidas.data.ajustes.entities.Modelo

@Dao
interface AcabadoDao {
    @Query("SELECT * FROM Acabado WHERE modeloId = :modeloId  ORDER BY orden")
    suspend fun getAllByModelo(modeloId: Int): List<Acabado>

    @Query("SELECT MAX(orden) FROM Acabado WHERE modeloId = :modId")
    suspend fun getMaxOrden(modId: Int): Int?

    @Update
    suspend fun update(vararg acabado: Acabado)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(acabado: Acabado): Long

    @Delete
    suspend fun delete(acabado: Acabado)
}
