// TipoDao.kt
package com.persianesricart.mismedidas.data.ajustes.dao

import androidx.room.*
import com.persianesricart.mismedidas.data.ajustes.entities.Tipo

@Dao
interface TipoDao {
    @Query("SELECT * FROM Tipo ORDER BY orden")
    suspend fun getAll(): List<Tipo>

    @Query("SELECT MAX(orden) FROM Tipo")
    suspend fun getMaxOrden(): Int?

    @Update
    suspend fun update(vararg tipos: Tipo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tipo: Tipo): Long

    @Delete
    suspend fun delete(tipo: Tipo)
    //abstract fun getMaxOrden(): Int?
}