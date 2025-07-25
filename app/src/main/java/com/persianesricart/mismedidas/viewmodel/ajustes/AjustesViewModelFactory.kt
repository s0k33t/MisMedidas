package com.persianesricart.mismedidas.viewmodel.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.persianesricart.mismedidas.data.ajustes.dao.*

class AjustesViewModelFactory(
    private val tipoDao: TipoDao,
    private val modeloDao: ModeloDao,
    private val acabadoDao: AcabadoDao,
    private val colorDao: ColorDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AjustesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AjustesViewModel(
                tipoDao,
                modeloDao,
                acabadoDao,
                colorDao
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
