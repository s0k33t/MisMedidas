package com.persianesricart.mismedidas.data.ajustes.dao

import androidx.room.*
import com.persianesricart.mismedidas.data.ajustes.entities.Acabado
import com.persianesricart.mismedidas.data.ajustes.entities.Modelo
import kotlinx.coroutines.flow.Flow

@Dao
interface ModeloDao {


    /** Carga todos los modelos (para dropdown de Colores y loadModelosGeneral)
    @Query("SELECT * FROM Modelo  WHERE tipoId = :tipoId ORDER BY orden")
    suspend fun getAllByTipo(tipoId: Int): List<Modelo>

    @Query("SELECT MAX(orden) FROM Modelo WHERE tipoId = :tipoId")
    suspend fun getMaxOrden(tipoId: Int): Int?

    /** Carga los modelos filtrados por el Tipo seleccionado */
    @Query("SELECT * FROM Modelo WHERE tipoId = :tipoId")
    suspend fun getByTipo(tipoId: Int): List<Modelo>

    @Update
    suspend fun update(vararg modelo: Modelo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(modelo: Modelo): Long

    @Delete
    suspend fun delete(modelo: Modelo)
    }*/

    /*
@Dao
interface ModeloDao {
*/
    @Query("SELECT * FROM Modelo WHERE tipoId = :tid ORDER BY orden")
    suspend fun getAllByTipo(tid: Int): List<Modelo>


    @Query("SELECT MAX(orden) FROM Modelo WHERE tipoId = :tid")
    suspend fun getMaxOrden(tid: Int): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(modelo: Modelo): Long

    @Update
    suspend fun update(vararg modelos: Modelo)

    @Delete
    suspend fun delete(modelo: Modelo)
}

