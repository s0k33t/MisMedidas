package com.persianesricart.mismedidas.data.dao

import androidx.room.*
import com.persianesricart.mismedidas.data.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NotaDao {

    @Transaction
    @Query("SELECT * FROM nota ORDER BY fecha DESC")
    suspend fun getTodasLasNotas(): List<Nota>

    @Query("SELECT * FROM nota WHERE id = :id")
    suspend fun getNotaWithMedidas(id: Int): NotaConMedidas?

    @Insert
    suspend fun insertNota(nota: Nota): Long

    @Update
    suspend fun updateNota(nota: Nota)

    @Insert
    suspend fun insertMedida(medida: Medida): Long

    @Query("DELETE FROM medida WHERE notaId = :notaId")
    suspend fun deleteMedidasByNotaId(notaId: Int)

    @Query("DELETE FROM nota WHERE id = :notaId")
    suspend fun deleteNotaById(notaId: Int)

    @Query("DELETE FROM medida WHERE id IN (:ids)")
    suspend fun deleteMedidasByIds(ids: List<Int>)

    @Delete
    suspend fun deleteMedida(medida: Medida)

    @Query("SELECT * FROM medida WHERE notaId = :notaId")
    suspend fun getMedidasByNota(notaId: Int): List<Medida>

    @Update
    suspend fun updateMedida(medida: Medida)


}