// ColorDao.kt
package com.persianesricart.mismedidas.data.ajustes.dao

import androidx.room.*
import com.persianesricart.mismedidas.data.ajustes.entities.Color

@Dao
interface ColorDao {
    @Query("SELECT * FROM Color WHERE modeloId = :modeloId")
    suspend fun getByModelo(modeloId: Int): List<Color>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(color: Color): Long

    @Delete
    suspend fun delete(color: Color)
}