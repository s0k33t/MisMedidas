package com.persianesricart.mismedidas.ui

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.persianesricart.mismedidas.viewmodel.MainViewModel
import com.persianesricart.mismedidas.viewmodel.ajustes.AjustesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExportScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    ajustesViewModel: AjustesViewModel,
    exportNotasLauncher: ActivityResultLauncher<Intent>,
    importNotasLauncher: ActivityResultLauncher<Intent>,
    exportAjustesLauncher: ActivityResultLauncher<Intent>,
    importAjustesLauncher: ActivityResultLauncher<Intent>,
) {
    val context = LocalContext.current
    val scroll = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Importación / Exportación") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(scroll)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Notas", style = MaterialTheme.typography.titleMedium)
            Button(onClick = {
                mainViewModel.exportarBaseDeDatosSAF(context, exportNotasLauncher)
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Exportar base de datos de notas")
            }
            Button(onClick = {
                mainViewModel.importarBaseDeDatosSAF(context, importNotasLauncher)
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Importar base de datos de notas")
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Ajustes", style = MaterialTheme.typography.titleMedium)
            Button(onClick = {
                ajustesViewModel.exportarAjustesSAF(context, exportAjustesLauncher)
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Exportar base de datos de ajustes")
            }
            Button(onClick = {
                ajustesViewModel.importarAjustesSAF(importAjustesLauncher)
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Importar base de datos de ajustes")
            }
        }
    }
}
