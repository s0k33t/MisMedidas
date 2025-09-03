package com.persianesricart.mismedidas.data.dao

import androidx.room.*
import com.persianesricart.mismedidas.data.entities.Croquis

@Dao
interface CroquisDao {

    @Query("SELECT * FROM croquis WHERE medidaId = :medidaId ORDER BY createdAt ASC")
    //suspend fun getByMedida(medidaId: Int): List<Croquis>
    fun observeByMedida(medidaId: Int): kotlinx.coroutines.flow.Flow<List<Croquis>>

    @Query("SELECT * FROM croquis WHERE medidaId = :medidaId ORDER BY createdAt ASC")
    suspend fun getByMedida(medidaId: Int): List<Croquis>

    @Query("SELECT * FROM croquis WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Croquis?

    @Insert
    suspend fun insert(c: Croquis): Long

    @Update
    suspend fun update(c: Croquis)

    @Delete
    suspend fun delete(c: Croquis)

    @Query("DELETE FROM croquis WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM croquis WHERE medidaId = :medidaId")
    suspend fun deleteByMedida(medidaId: Int)
}
