package com.persianesricart.mismedidas.data.dao

import androidx.room.*
import com.persianesricart.mismedidas.data.entities.Medida



@Dao
interface MedidaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedida(medida: Medida)

    @Query("SELECT * FROM medida WHERE notaId = :notaId")
    suspend fun getMedidasByNota(notaId: Int): List<Medida>

    @Query("SELECT * FROM Medida WHERE uuid IS NULL OR uuid = ''")
    suspend fun getAllWithoutUuid(): List<Medida>

    @Update
    suspend fun update(medida: Medida)

    @Query("SELECT * FROM Medida WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Medida?

    @Query("SELECT * FROM Medida WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): Medida?

    @Query("SELECT uuid FROM Medida WHERE id = :id LIMIT 1")
    suspend fun getUuidById(id: Int): String?

    @Query("SELECT id FROM Medida WHERE uuid = :uuid LIMIT 1")
    suspend fun getIdByUuid(uuid: String): Int?

}
