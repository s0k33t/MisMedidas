package com.persianesricart.mismedidas.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.persianesricart.mismedidas.data.dao.NotaDao
import com.persianesricart.mismedidas.data.entities.Medida
import com.persianesricart.mismedidas.data.entities.Nota
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class   NoteViewModel(private val dao: NotaDao) : ViewModel() {

    private val _notaId = mutableStateOf(0)
    val notaId: Int get() = _notaId.value

    private val _cliente = mutableStateOf("")
    val cliente: String get() = _cliente.value

    private val _referencia = mutableStateOf("")
    val referencia: String get() = _referencia.value

    private val _direccion = mutableStateOf("")
    val direccion: String get() = _direccion.value

    private val _poblacion = mutableStateOf("")
    val poblacion: String get() = _poblacion.value

    private val _telefono = mutableStateOf("")
    val telefono: String get() = _telefono.value

    private val _movil = mutableStateOf("")
    val movil: String get() = _movil.value

    private val _email = mutableStateOf("")
    val email: String get() = _email.value

    private val _fecha = mutableStateOf(
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    )
    val fecha: String get() = _fecha.value

    private val _medidas = mutableStateListOf<Medida>()
    val medidas: List<Medida> get() = _medidas

    private val _ultimoMedidaId = mutableStateOf(-1)
    val ultimoMedidaId: Int get() = _ultimoMedidaId.value

    fun onClienteChanged(value: String) { _cliente.value = value }
    fun onReferenciaChanged(value: String) { _referencia.value = value }
    fun onDireccionChanged(value: String) { _direccion.value = value }
    fun onPoblacionChanged(value: String) { _poblacion.value = value }
    fun onTelefonoChanged(value: String) { _telefono.value = value }
    fun onMovilChanged(value: String) { _movil.value = value }
    fun onEmailChanged(value: String) { _email.value = value }
    fun setUltimoMedidaId(id: Int) { _ultimoMedidaId.value = id }

    fun addMedida(medida: Medida) {
        _medidas.add(medida)
    }

    fun updateMedida(index: Int, medida: Medida) {
        if (index in _medidas.indices) {
            _medidas[index] = medida
        }
    }

    fun removeMedida(index: Int) {
        if (index in _medidas.indices) {
            _medidas.removeAt(index)
        }
    }

    fun loadNota(id: Int) {
        viewModelScope.launch {
            val notaConMedidas = dao.getNotaWithMedidas(id)
            notaConMedidas?.let {
                _notaId.value = it.nota.id
                _cliente.value = it.nota.cliente
                _referencia.value = it.nota.referencia
                _direccion.value = it.nota.direccion
                _poblacion.value = it.nota.poblacion
                _telefono.value = it.nota.telefono
                _movil.value = it.nota.movil
                _email.value = it.nota.email
                _fecha.value = it.nota.fecha
                _medidas.clear()
                _medidas.addAll(it.medidas)
            }
        }
    }

    fun saveNota(onSaved: () -> Unit) {
        viewModelScope.launch {
            val nota = Nota(
                id = if (notaId != 0) notaId else 0,
                cliente = cliente,
                referencia = referencia,
                direccion = direccion,
                poblacion = poblacion,
                telefono = telefono,
                movil = movil,
                email = email,
                fecha = fecha
            )

            val notaIdGuardado = if (notaId == 0) {
                dao.insertNota(nota).toInt()
            } else {
                dao.updateNota(nota)
                notaId
            }

            dao.deleteMedidasByNotaId(notaIdGuardado)
            _medidas.forEach {
                dao.insertMedida(it.copy(notaId = notaIdGuardado))
            }

            _notaId.value = notaIdGuardado
            onSaved()
        }
    }
}
