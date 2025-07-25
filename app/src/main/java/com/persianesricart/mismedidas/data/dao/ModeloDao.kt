package com.persianesricart.mismedidas.data.ajustes.dao

import androidx.room.*
import com.persianesricart.mismedidas.data.ajustes.entities.Modelo

@Dao
interface ModeloDao {

    /** Carga todos los modelos (para dropdown de Colores y loadModelosGeneral) */
    @Query("SELECT * FROM Modelo")
    suspend fun getAll(): List<Modelo>

    /** Carga los modelos filtrados por el Tipo seleccionado */
    @Query("SELECT * FROM Modelo WHERE tipoId = :tipoId")
    suspend fun getByTipo(tipoId: Int): List<Modelo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(modelo: Modelo): Long

    @Delete
    suspend fun delete(modelo: Modelo)
}