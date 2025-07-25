// AjustesScreen.kt
package com.persianesricart.mismedidas.ui.ajustes

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

@Composable
fun AjustesScreen(
    navController: NavController,
    ajustesViewModel: AjustesViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Tipos", "Modelos", "Acabados", "Colores")

    val context = LocalContext.current
    val db = AjustesDatabase.getInstance(context)
    val factory = AjustesViewModelFactory(
        db.tipoDao(),
        db.modeloDao(),
        db.acabadoDao(),
        db.colorDao()
    )
    val ajustesViewModel: AjustesViewModel = viewModel(factory = factory)

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

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> AjustesTipoSection(ajustesViewModel)
            1 -> AjustesModeloSection(ajustesViewModel)
            2 -> AjustesAcabadoSection(ajustesViewModel)
            3 -> AjustesColorSection(ajustesViewModel)
        }
    }
}
