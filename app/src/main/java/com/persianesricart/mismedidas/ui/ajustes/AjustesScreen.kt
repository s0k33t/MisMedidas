// AjustesScreen.kt
package com.persianesricart.mismedidas.ui.ajustes

//import androidx.compose.foundation.layout.Row
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.persianesricart.mismedidas.viewmodel.ajustes.AjustesViewModel
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.navigation.NavController
import com.persianesricart.mismedidas.ui.ajustes.AjustesAcabadoSection
import com.persianesricart.mismedidas.ui.ajustes.AjustesColorSection
import com.persianesricart.mismedidas.ui.ajustes.AjustesModeloSection
import com.persianesricart.mismedidas.ui.ajustes.AjustesTipoSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    navController: NavController,
    ajustesViewModel: AjustesViewModel,
    exportAjustesLauncher: ActivityResultLauncher<Intent>,
    importAjustesLauncher: ActivityResultLauncher<Intent>
) {
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Tipos", "Modelos", "Acabados", "Colores")
    val context = LocalContext.current


    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Atrás"
                        )
                    }
                },
                title = { Text("Ajustes") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(innerPadding)
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
}