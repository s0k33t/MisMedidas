// ModeloDao.kt
package com.persianesricart.mismedidas.data.ajustes.dao

import androidx.room.*
import com.persianesricart.mismedidas.data.ajustes.entities.Modelo

@Dao
interface ModeloDao {
    @Query("SELECT * FROM Modelo")
    suspend fun getAll(): List<Modelo>

    @Query("SELECT * FROM Modelo WHERE tipoId = :tipoId")
    suspend fun getByTipo(tipoId: Int): List<Modelo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(modelo: Modelo): Long

    @Delete
    suspend fun delete(modelo: Modelo)
}
