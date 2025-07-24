// AcabadoDao.kt
package com.persianesricart.mismedidas.data.ajustes.dao

import androidx.room.*
import com.persianesricart.mismedidas.data.ajustes.entities.Acabado

@Dao
interface AcabadoDao {
    @Query("SELECT * FROM Acabado")
    suspend fun getAll(): List<Acabado>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(acabado: Acabado): Long

    @Delete
    suspend fun delete(acabado: Acabado)
}