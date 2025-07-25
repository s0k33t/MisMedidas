package com.persianesricart.mismedidas.viewmodel.ajustes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.persianesricart.mismedidas.data.ajustes.AjustesDatabase
import com.persianesricart.mismedidas.data.ajustes.dao.AcabadoDao
import com.persianesricart.mismedidas.data.ajustes.dao.ColorDao
import com.persianesricart.mismedidas.data.ajustes.dao.ModeloDao
import com.persianesricart.mismedidas.data.ajustes.dao.TipoDao
import com.persianesricart.mismedidas.data.ajustes.entities.Acabado
import com.persianesricart.mismedidas.data.ajustes.entities.Color
import com.persianesricart.mismedidas.data.ajustes.entities.Modelo
import com.persianesricart.mismedidas.data.ajustes.entities.Tipo
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.content.ContextCompat.startActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class AjustesViewModel(
    private val tipoDao: TipoDao,
    private val modeloDao: ModeloDao,
    private val acabadoDao: AcabadoDao,
    private val colorDao: ColorDao
) : ViewModel() {

    // ── Tipos ─────────────────────────────────────────────────────────
    private val _tipos = MutableStateFlow<List<Tipo>>(emptyList())
    val tipos: StateFlow<List<Tipo>> = _tipos.asStateFlow()

    // ── Modelos ───────────────────────────────────────────────────────
    private val _modelos = MutableStateFlow<List<Modelo>>(emptyList())
    val modelos: StateFlow<List<Modelo>> = _modelos.asStateFlow()

    // Para cargar **todos** los modelos (dropdown de Colores)
    private val _modelosGeneral = MutableStateFlow<List<Modelo>>(emptyList())
    val modelosGeneral: StateFlow<List<Modelo>> = _modelosGeneral.asStateFlow()

    // ── Acabados (filtrados por Modelo) ───────────────────────────────
    private val _acabados = MutableStateFlow<List<Acabado>>(emptyList())
    val acabados: StateFlow<List<Acabado>> = _acabados.asStateFlow()

    // ── Colores (filtrados por Modelo) ────────────────────────────────
    private val _colores = MutableStateFlow<List<Color>>(emptyList())
    val colores: StateFlow<List<Color>> = _colores.asStateFlow()

    init {
        // Cargo solo aquellos flujos que no necesitan parámetro
        loadTipos()
        loadModelosGeneral()
        // No llamamos aquí a loadAcabados() ni loadColores() sin id
    }

    // ===== Métodos para Tipos =====
    fun loadTipos() = viewModelScope.launch {
        _tipos.value = tipoDao.getAll()
    }

    fun insertarTipo(nombre: String) = viewModelScope.launch {
        tipoDao.insert(Tipo(nombre = nombre))
        loadTipos()
    }

    fun eliminarTipo(tipo: Tipo) = viewModelScope.launch {
        tipoDao.delete(tipo)
        loadTipos()
    }

    // ===== Métodos para Modelos =====
    fun loadModelos(tipoId: Int) = viewModelScope.launch {
        _modelos.value = modeloDao.getByTipo(tipoId)
    }

    fun loadModelosGeneral() = viewModelScope.launch {
        _modelosGeneral.value = modeloDao.getAll()
    }

    fun insertarModelo(nombre: String, tipoId: Int) = viewModelScope.launch {
        modeloDao.insert(Modelo(nombre = nombre, tipoId = tipoId))
        loadModelos(tipoId)
        loadModelosGeneral()
    }

    fun eliminarModelo(modelo: Modelo) = viewModelScope.launch {
        modeloDao.delete(modelo)
        // recargo ambos flujos
        loadModelos(modelo.tipoId)
        loadModelosGeneral()
    }

    // ===== Métodos para Acabados =====
    /** Filtra y carga los acabados del modelo dado */
    fun loadAcabados(modeloId: Int) = viewModelScope.launch {
        _acabados.value = acabadoDao.getByModelo(modeloId)
    }

    /** Inserta y recarga los acabados para ese modelo */
    fun insertarAcabado(nombre: String, modeloId: Int) = viewModelScope.launch {
        acabadoDao.insert(Acabado(nombre = nombre, modeloId = modeloId))
        loadAcabados(modeloId)
    }

    /** Elimina y recarga los acabados para el mismo modelo */
    fun eliminarAcabado(acabado: Acabado) = viewModelScope.launch {
        acabadoDao.delete(acabado)
        loadAcabados(acabado.modeloId)
    }

    // ===== Métodos para Colores =====
    /** Filtra y carga los colores del modelo dado */
    fun loadColores(modeloId: Int) = viewModelScope.launch {
        _colores.value = colorDao.getByModelo(modeloId)
    }

    /** Inserta y recarga los colores para ese modelo */
    fun insertarColor(nombre: String, modeloId: Int) = viewModelScope.launch {
        colorDao.insert(Color(nombre = nombre, modeloId = modeloId))
        loadColores(modeloId)
    }

    /** Elimina y recarga los colores para el mismo modelo */
    fun eliminarColor(color: Color) = viewModelScope.launch {
        colorDao.delete(color)
        loadColores(color.modeloId)
    }

    companion object {
        /** Factory helper */
        fun create(context: Context): AjustesViewModel {
            val db = AjustesDatabase.getInstance(context)
            return AjustesViewModel(
                db.tipoDao(),
                db.modeloDao(),
                db.acabadoDao(),
                db.colorDao()
            )
        }
    }

    /** Lanza el SAF para crear un documento donde exportar ajustes.db */
    fun exportarAjustesSAF(context: Context, launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_TITLE, "ajustes.db")
        }
        launcher.launch(intent)
    }

    /** Copia ajustes.db al URI seleccionado por el usuario */
    fun handleExportAjustesResult(context: Context, uri: Uri) {
        try {
            // Cierra la BD antes de copiar
            AjustesDatabase.getInstance(context).close()

            val source = context.getDatabasePath("ajustes.db")
            context.contentResolver.openOutputStream(uri)?.use { out ->
                source.inputStream().use { input -> input.copyTo(out) }
            }
            Toast.makeText(context, "Exportación de ajustes completada", Toast.LENGTH_SHORT).show()
        } catch(e: Exception) {
            Toast.makeText(context, "Error al exportar ajustes: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /** Lanza el SAF para elegir un documento desde el que importar ajustes.db */
    fun importarAjustesSAF(launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        launcher.launch(intent)
    }

    /** Carga desde el URI seleccionado sobre ajustes.db y reinicia la app */
    fun handleImportAjustesResult(context: Context, uri: Uri, restart: ()->Unit) {
        try {
            val dest = context.getDatabasePath("ajustes.db")

            context.contentResolver.openInputStream(uri)?.use { inp ->
                dest.outputStream().use { out -> inp.copyTo(out) }
            }
            val parent = dest.parentFile
            val name = dest.name  // "ajustes.db"
            listOf("-wal", "-shm").forEach { suffix ->
                File(parent, name + suffix).delete()
            }
            Toast.makeText(context, "Importación de ajustes completada", Toast.LENGTH_LONG).show()
            restart()
        } catch(e: Exception) {
            Toast.makeText(context, "Error al importar ajustes: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

}
