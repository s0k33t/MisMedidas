// AjustesScreen.kt
package com.persianesricart.mismedidas.ui.ajustes

//import android.content.Intent
//import androidx.activity.result.ActivityResultLauncher
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
import com.persianesricart.mismedidas.viewmodel.ajustes.AjustesViewModel


import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.navigation.NavController


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
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Tabs
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
        // Secciones según pestaña
        when (selectedTab) {
            0 -> AjustesTipoSection(ajustesViewModel)
            1 -> AjustesModeloSection(ajustesViewModel)
            2 -> AjustesAcabadoSection(ajustesViewModel)
            3 -> AjustesColorSection(ajustesViewModel)
        }
    }
}
