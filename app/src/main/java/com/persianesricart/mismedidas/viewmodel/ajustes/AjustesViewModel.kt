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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AjustesViewModel constructor(
    private val tipoDao: TipoDao,
    private val modeloDao: ModeloDao,
    private val acabadoDao: AcabadoDao,
    private val colorDao: ColorDao
) : ViewModel() {

    // ------- StateFlows públicos -------
    private val _tipos = MutableStateFlow<List<Tipo>>(emptyList())
    val tipos: StateFlow<List<Tipo>> = _tipos.asStateFlow()

    private val _modelos = MutableStateFlow<List<Modelo>>(emptyList())
    val modelos: StateFlow<List<Modelo>> = _modelos.asStateFlow()

    private val _modelosGeneral = MutableStateFlow<List<Modelo>>(emptyList())
    val modelosGeneral: StateFlow<List<Modelo>> = _modelosGeneral.asStateFlow()

    private val _acabados = MutableStateFlow<List<Acabado>>(emptyList())
    val acabados: StateFlow<List<Acabado>> = _acabados.asStateFlow()

    private val _colores = MutableStateFlow<List<Color>>(emptyList())
    val colores: StateFlow<List<Color>> = _colores.asStateFlow()

    init {
        // Al arrancar, cargo los datos estáticos
        loadTipos()
        loadModelosGeneral()
        loadAcabados()
    }

    // --- Tipos ---
    fun loadTipos() {
        viewModelScope.launch {
            _tipos.value = tipoDao.getAll()
        }
    }

    fun insertarTipo(nombre: String) {
        viewModelScope.launch {
            tipoDao.insert(Tipo(nombre = nombre))
            loadTipos()
        }
    }

    fun eliminarTipo(tipo: Tipo) {
        viewModelScope.launch {
            tipoDao.delete(tipo)
            loadTipos()
        }
    }

    // --- Modelos (filtrado por Tipo) ---
    fun loadModelos(tipoId: Int) {
        viewModelScope.launch {
            _modelos.value = modeloDao.getByTipo(tipoId)
        }
    }

    // Carga **todos** los modelos (para el dropdown de colores)
    fun loadModelosGeneral() {
        viewModelScope.launch {
            _modelosGeneral.value = modeloDao.getAll()
        }
    }

    fun insertarModelo(nombre: String, tipoId: Int) {
        viewModelScope.launch {
            modeloDao.insert(Modelo(nombre = nombre, tipoId = tipoId))
            loadModelos(tipoId)
            loadModelosGeneral()
        }
    }

    fun eliminarModelo(modelo: Modelo) {
        viewModelScope.launch {
            modeloDao.delete(modelo)
            loadModelosGeneral()
        }
    }

    // --- Acabados (no dependen de otro) ---
    fun loadAcabados() {
        viewModelScope.launch {
            _acabados.value = acabadoDao.getAll()
        }
    }

    fun insertarAcabado(nombre: String) {
        viewModelScope.launch {
            acabadoDao.insert(Acabado(nombre = nombre))
            loadAcabados()
        }
    }

    fun eliminarAcabado(acabado: Acabado) {
        viewModelScope.launch {
            acabadoDao.delete(acabado)
            loadAcabados()
        }
    }

    // --- Colores (filtrado por Modelo) ---
    fun loadColores(modeloId: Int) {
        viewModelScope.launch {
            _colores.value = colorDao.getByModelo(modeloId)
        }
    }

    fun insertarColor(nombre: String, modeloId: Int) {
        viewModelScope.launch {
            colorDao.insert(Color(nombre = nombre, modeloId = modeloId))
            loadColores(modeloId)
        }
    }

    fun eliminarColor(color: Color) {
        viewModelScope.launch {
            colorDao.delete(color)
            // Opcional: recargar si quieres
        }
    }

    companion object {
        /** Crea el ViewModel con DAOs obtenidos de AjustesDatabase */
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
}
