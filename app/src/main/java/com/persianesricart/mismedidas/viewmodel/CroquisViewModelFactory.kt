package com.persianesricart.mismedidas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.persianesricart.mismedidas.data.dao.CroquisDao

class CroquisViewModelFactory(private val dao: CroquisDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CroquisViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CroquisViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
