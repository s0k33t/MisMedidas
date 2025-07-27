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
import androidx.compose.runtime.mutableStateListOf
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.content.ContextCompat.startActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    private val _modelos =  MutableStateFlow<List<Modelo>>(emptyList())
    val modelos: StateFlow<List<Modelo>>  = _modelos.asStateFlow()

    // Para cargar **todos** los modelos (dropdown de Colores)
    private val _modelosGeneral = MutableStateFlow<List<Modelo>>(emptyList())
    val modelosGeneral: StateFlow<List<Modelo>> = _modelosGeneral.asStateFlow()

    // ── Acabados (filtrados por Modelo) ───────────────────────────────
    private val _acabados = MutableStateFlow<List<Acabado>>(emptyList())
    val acabados: StateFlow<List<Acabado>> = _acabados.asStateFlow()

    private val _acabadosGeneral = MutableStateFlow<List<Modelo>>(emptyList())
    val acabadosGeneral: StateFlow<List<Modelo>> = _modelosGeneral.asStateFlow()

    // ── Colores (filtrados por Modelo) ────────────────────────────────
    private val _colores = MutableStateFlow<List<Color>>(emptyList())
    val colores: StateFlow<List<Color>> = _colores.asStateFlow()

    init {
        // Cargo solo aquellos flujos que no necesitan parámetro
        loadTipos()
        //loadModelosGeneral()
        // No llamamos aquí a loadAcabados() ni loadColores() sin id
    }

    // ===== Métodos para Tipos =====
    fun loadTipos() = viewModelScope.launch {
        _tipos.value = tipoDao.getAll()
    }

    fun insertarTipo(nombre: String) = viewModelScope.launch {
        val maxOrden = tipoDao.getMaxOrden() ?: 0
        tipoDao.insert(Tipo(nombre = nombre, orden = maxOrden + 1))
        loadTipos()
    }

    fun eliminarTipo(tipo: Tipo) = viewModelScope.launch {
        tipoDao.delete(tipo)
        loadTipos()
    }

    // ===== Métodos para Modelos =====
    fun loadModelos(tipoId: Int) = viewModelScope.launch {
        _modelos.value = modeloDao.getAllByTipo(tipoId)

    }

    fun loadModelosGeneral(tipoId: Int) = viewModelScope.launch {
        _modelosGeneral.value = modeloDao.getAllByTipo(tipoId)
    }

    //fun insertarModelo(nombre: String, tipoId: Int) = viewModelScope.launch {
    //    val maxOrden = modeloDao.getMaxOrden(tipoId) ?: 0
    //    modeloDao.insert(Modelo(nombre = nombre, tipoId=tipoId, orden = maxOrden + 1))
    //    loadModelos(tipoId)
    //    loadModelosGeneral(tipoId)
    // }

    fun insertarModelo(nombre: String, tipoId: Int) = viewModelScope.launch {
        val max = modeloDao.getMaxOrden(tipoId) ?: 0
        modeloDao.insert(
            Modelo(
                nombre = nombre,
                tipoId = tipoId,
                orden = max + 1            // ← asignamos orden
            )
        )
        loadModelos(tipoId)
    }
    fun eliminarModelo(modelo: Modelo) = viewModelScope.launch {
        modeloDao.delete(modelo)
        loadModelos(modelo.tipoId)
    }

    //fun eliminarModelo(modelo: Modelo, tipoId: Int) = viewModelScope.launch {
    //    modeloDao.delete(modelo)
    //    // recargo ambos flujos
    //    loadModelos(modelo.tipoId)
    //    loadModelosGeneral(tipoId)
    //}

    // ===== Métodos para Acabados =====
    /** Filtra y carga los acabados del modelo dado */
    fun loadAcabados(modeloId: Int) = viewModelScope.launch {
       // _acabados.value = acabadoDao.getAllByModelo(modeloId)
        val list = acabadoDao.getAllByModelo(modeloId)
        _acabados.value = list
    }

    /** Inserta y recarga los acabados para ese modelo */
    fun insertarAcabado(nombre: String, modeloId: Int, ) = viewModelScope.launch {
        val maxOrden = acabadoDao.getMaxOrden(modeloId) ?: 0
        acabadoDao.insert(Acabado(nombre = nombre, modeloId = modeloId, orden = maxOrden + 1))
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
        val list = colorDao.getAllByModelo(modeloId)
        _colores.value = list
        //_colores.value = colorDao.getAllByModelo(modeloId)
    }

    /** Inserta y recarga los colores para ese modelo */
    fun insertarColor(nombre: String, modeloId: Int) = viewModelScope.launch {
        val maxOrden = colorDao.getMaxOrden(modeloId) ?: 0
        colorDao.insert(Color(nombre = nombre, modeloId = modeloId, orden = maxOrden + 1))
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


    fun moverTipoArriba(tipo: Tipo) = viewModelScope.launch {
        val all = tipoDao.getAll().toMutableList()
        val idx = all.indexOfFirst { it.id == tipo.id }
        if (idx > 0) {
            val otro = all[idx - 1]
            // intercambiamos orden
            tipoDao.update(
                tipo.copy(orden = otro.orden),
                otro.copy(orden = tipo.orden)
            )
            loadTipos()
        }
    }

    /** Intercambia con el siguiente */
    fun moverTipoAbajo(tipo: Tipo) = viewModelScope.launch {
        val all = tipoDao.getAll().toMutableList()
        val idx = all.indexOfFirst { it.id == tipo.id }
        if (idx >= 0 && idx < all.lastIndex) {
            val otro = all[idx + 1]
            tipoDao.update(
                tipo.copy(orden = otro.orden),
                otro.copy(orden = tipo.orden)
            )
            loadTipos()
        }
    }

    fun moverColorArriba(color: Color) = viewModelScope.launch {
        val list = colorDao.getAllByModelo(color.modeloId).toMutableList()
        val idx = list.indexOfFirst { it.id == color.id }
        if (idx > 0) {
            val other = list[idx - 1]
            colorDao.update(
                color.copy(orden = other.orden),
                other.copy(orden = color.orden)
            )
            loadColores(color.modeloId)
        }
    }

    fun moverColorAbajo(color: Color) = viewModelScope.launch {
        val list = colorDao.getAllByModelo(color.modeloId).toMutableList()
        val idx = list.indexOfFirst { it.id == color.id }
        if (idx >= 0 && idx < list.lastIndex) {
            val other = list[idx + 1]
            colorDao.update(
                color.copy(orden = other.orden),
                other.copy(orden = color.orden)
            )
            loadColores(color.modeloId)
        }
    }

    fun moverAcabadoArriba(acabado: Acabado) = viewModelScope.launch {
        val list = acabadoDao.getAllByModelo(acabado.modeloId).toMutableList()
        val idx = list.indexOfFirst { it.id == acabado.id }
        if (idx > 0) {
            val other = list[idx - 1]
            acabadoDao.update(
                acabado.copy(orden = other.orden),
                other.copy(orden = acabado.orden)
            )
            loadAcabados(acabado.modeloId)
        }
    }

    /** Intercambia con el siguiente */
    fun moverAcabadoAbajo(acabado: Acabado) = viewModelScope.launch {
        val all = acabadoDao.getAllByModelo(acabado.modeloId).toMutableList()
        val idx = all.indexOfFirst { it.id == acabado.id }
        if (idx >= 0 && idx < all.lastIndex) {
            val otro = all[idx + 1]
            acabadoDao.update(
                acabado.copy(orden = otro.orden),
                otro.copy(orden = acabado.orden)
            )
            loadAcabados(acabado.modeloId)
        }
    }

    fun moverModeloArriba(modelo: Modelo) = viewModelScope.launch {
        val lista = modeloDao.getAllByTipo(modelo.tipoId).toMutableList()
        val idx = lista.indexOfFirst { it.id == modelo.id }
        if (idx > 0) {
            val otro = lista[idx - 1]
            modeloDao.update(
                modelo.copy(orden = otro.orden),
                otro.copy(orden = modelo.orden)
            )
            loadModelos(modelo.tipoId)
        }
    }

    fun moverModeloAbajo(modelo: Modelo) = viewModelScope.launch {
        val lista = modeloDao.getAllByTipo(modelo.tipoId).toMutableList()
        val idx = lista.indexOfFirst { it.id == modelo.id }
        if (idx >= 0 && idx < lista.lastIndex) {
            val otro = lista[idx + 1]
            modeloDao.update(
                modelo.copy(orden = otro.orden),
                otro.copy(orden = modelo.orden)
            )
            loadModelos(modelo.tipoId)
        }
    }
}
