// TipoDao.kt
package com.persianesricart.mismedidas.data.ajustes.dao

import androidx.room.*
import com.persianesricart.mismedidas.data.ajustes.entities.Tipo

@Dao
interface TipoDao {
    @Query("SELECT * FROM Tipo")
    suspend fun getAll(): List<Tipo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tipo: Tipo): Long

    @Delete
    suspend fun delete(tipo: Tipo)
}