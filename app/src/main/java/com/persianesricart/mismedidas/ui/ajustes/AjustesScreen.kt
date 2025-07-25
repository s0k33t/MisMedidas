// AjustesScreen.kt
package com.persianesricart.mismedidas.ui.ajustes

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.persianesricart.mismedidas.data.ajustes.AjustesDatabase
import com.persianesricart.mismedidas.viewmodel.ajustes.AjustesViewModel
import com.persianesricart.mismedidas.viewmodel.ajustes.AjustesViewModelFactory


import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row


@Composable
fun AjustesScreen(
    navController: NavController,
    ajustesViewModel: AjustesViewModel,
    exportAjustesLauncher: ActivityResultLauncher<Intent>,
    importAjustesLauncher: ActivityResultLauncher<Intent>
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Tipos", "Modelos", "Acabados", "Colores")
    val context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp)) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Copia de seguridad de ajustes", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = {
                //ajustesViewModel.exportarAjustesSAF(this@AjustesScreen, exportAjustesLauncher)
                ajustesViewModel.exportarAjustesSAF(context, exportAjustesLauncher)
            }) {
                Text("Exportar ajustes")
            }
            Button(onClick = {
                ajustesViewModel.importarAjustesSAF(importAjustesLauncher)
            }) {
                Text("Importar ajustes")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        when (selectedTab) {
            0 -> AjustesTipoSection(ajustesViewModel)
            1 -> AjustesModeloSection(ajustesViewModel)
            2 -> AjustesAcabadoSection(ajustesViewModel)
            3 -> AjustesColorSection(ajustesViewModel)
        }
    }
}