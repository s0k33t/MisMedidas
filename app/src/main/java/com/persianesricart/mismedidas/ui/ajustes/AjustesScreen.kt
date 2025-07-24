// AjustesScreen.kt
package com.persianesricart.mismedidas.ui.ajustes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.persianesricart.mismedidas.viewmodel.ajustes.AjustesViewModel

@Composable
fun AjustesScreen(
    navController: NavController,
    ajustesViewModel: AjustesViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Tipos", "Modelos", "Acabados", "Colores")

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
