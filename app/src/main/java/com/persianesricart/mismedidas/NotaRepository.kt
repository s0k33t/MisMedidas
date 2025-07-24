package com.persianesricart.mismedidas.data.repository

import com.persianesricart.mismedidas.data.dao.MedidaDao
import com.persianesricart.mismedidas.data.dao.NotaDao
import com.persianesricart.mismedidas.data.entities.Medida
import com.persianesricart.mismedidas.data.entities.Nota
import kotlinx.coroutines.flow.Flow

class NotaRepository(private val notaDao: NotaDao, private val medidaDao: MedidaDao) {

    //fun getAllNotas(): Flow<List<Nota>> = notaDao.getAllNotas()

    suspend fun insertNotaWithMedidas(nota: Nota, medidas: List<Medida>) {
        val notaId = notaDao.insertNota(nota)
        medidas.forEach {
            medidaDao.insertMedida(it.copy(notaId = notaId.toInt()))
        }
    }
    suspend fun getAllNotas(): List<Nota> {
        return notaDao.getTodasLasNotas()
    }
}
