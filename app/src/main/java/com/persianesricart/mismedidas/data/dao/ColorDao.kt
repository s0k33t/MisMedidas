// ColorDao.kt
package com.persianesricart.mismedidas.data.ajustes.dao

import androidx.room.*
import com.persianesricart.mismedidas.data.ajustes.entities.Color

@Dao
interface ColorDao {
    @Query("SELECT * FROM Color WHERE modeloId = :modeloId ORDER BY orden")
    suspend fun getAllByModelo(modeloId: Int): List<Color>

    @Query("SELECT MAX(orden) FROM Color WHERE modeloId = :modId")
    suspend fun getMaxOrden(modId: Int): Int?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(color: Color): Long

    @Update
    suspend fun update(vararg colors: Color)

    @Delete
    suspend fun delete(color: Color)
}