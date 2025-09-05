package com.persianesricart.mismedidas.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.persianesricart.mismedidas.data.AppDatabase
import com.persianesricart.mismedidas.data.dao.CroquisDao
import com.persianesricart.mismedidas.data.entities.Croquis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class CroquisViewModel(private val dao: CroquisDao) : ViewModel() {

    private val flowsByMedida = mutableMapOf<Int, MutableStateFlow<List<Croquis>>>()

    suspend fun getById(id: Int): Croquis? {
        return dao.getById(id)
    }
    /*
    fun croquisFlow(medidaId: Int): StateFlow<List<Croquis>> {
        return flowsByMedida.getOrPut(medidaId) { MutableStateFlow(emptyList()) }
    }
    */
    fun croquisFlow(medidaId: Int) = dao.observeByMedida(medidaId)

    fun loadCroquis(medidaId: Int) {
        viewModelScope.launch {
            val list = dao.getByMedida(medidaId)
            flowsByMedida.getOrPut(medidaId) { MutableStateFlow(emptyList()) }.value = list
        }
    }

    /*
    fun saveNewCroquis(context: Context, medidaId: Int, nombre: String, bitmap: Bitmap) {
        viewModelScope.launch {
            val path = saveBitmapToInternal(context, bitmap)
            val id = dao.insert(Croquis(medidaId = medidaId, nombre = nombre, filePath = path)).toInt()
            loadCroquis(medidaId)
        }
    }
    */
    suspend fun saveNewCroquis(context: Context, medidaId: Int, nombre: String, bitmap: android.graphics.Bitmap) {
        val dir = java.io.File(context.filesDir, "croquis")
        if (!dir.exists()) dir.mkdirs()
        val file = java.io.File(dir, "${java.util.UUID.randomUUID()}.png")
        java.io.FileOutputStream(file).use { out ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
        }
        val now = System.currentTimeMillis()
        dao.insert(
            Croquis(
                id = 0,
                medidaId = medidaId,
                nombre = nombre.ifBlank { "Croquis" },
                filePath = file.absolutePath,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun overwriteCroquis(context: Context, croquisId: Int, bitmap: android.graphics.Bitmap) {
        val c = dao.getById(croquisId) ?: return
        java.io.FileOutputStream(java.io.File(c.filePath)).use { out ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
        }
        dao.update(c.copy(updatedAt = System.currentTimeMillis()))
    }



    fun updateCroquisBitmap(context: Context, croquis: Croquis, bitmap: Bitmap) {
        viewModelScope.launch {
            // sobrescribe su fichero
            saveBitmapToPath(bitmap, File(croquis.filePath))
            dao.update(croquis.copy(updatedAt = System.currentTimeMillis()))
            loadCroquis(croquis.medidaId)
        }
    }


    //fun deleteCroquis(context: Context, croquis: Croquis) {
    //    viewModelScope.launch {
    //        try { File(croquis.filePath).delete() } catch (_: Exception) {}
    //        dao.deleteById(croquis.id)
    //        loadCroquis(croquis.medidaId)
    //    }
    //}

    fun deleteCroquis(context: Context, c: Croquis){
        viewModelScope.launch {
            try {
                dao.delete(c)
                runCatching { java.io.File(c.filePath).delete() }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "No se pudo borrar el croquis: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun saveBitmapToInternal(context: Context, bitmap: Bitmap): String {
        val dir = File(context.filesDir, "croquis")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "croquis_${System.currentTimeMillis()}.png")
        saveBitmapToPath(bitmap, file)
        return file.absolutePath
    }

    private fun saveBitmapToPath(bitmap: Bitmap, file: File) {
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    fun fileUriForShare(context: Context, filePath: String): Uri {
        val file = File(filePath)
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    companion object {
        fun create(context: Context): CroquisViewModel {
            val dao = AppDatabase.getInstance(context).croquisDao()
            return CroquisViewModel(dao)
        }
    }
}
