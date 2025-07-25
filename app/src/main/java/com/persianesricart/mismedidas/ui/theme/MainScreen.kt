package com.persianesricart.mismedidas.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.persianesricart.mismedidas.viewmodel.MainViewModel
import com.persianesricart.mismedidas.data.entities.Nota
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController,
               viewModel: MainViewModel,
               exportLauncher: ActivityResultLauncher<Intent>,
               importLauncher: ActivityResultLauncher<Intent>) {

    val todasLasNotas = viewModel.notas
    var filtro by remember { mutableStateOf("") }
    val notasFiltradas = todasLasNotas
        .filter { it.cliente.contains(filtro, ignoreCase = true) }
        .sortedByDescending { parseFecha(it.fecha) }

    var notaParaEliminar by remember { mutableStateOf<Nota?>(null) }
    val context = LocalContext.current
    var compartirNota by remember { mutableStateOf<Nota?>(null) }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Medidas") },
                actions = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menú")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Exportar base de datos") },
                            onClick = {
                                expanded = false
                                viewModel.exportarBaseDeDatosSAF(context, exportLauncher)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Importar base de datos") },
                            onClick = {
                                expanded = false
                                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                    type = "*/*"
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                }
                                importLauncher.launch(intent)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Info base de datos") },
                            onClick = {
                                expanded = false
                                viewModel.mostrarRutaBaseDeDatos(context)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Ajustes") },
                            onClick = {
                                expanded = false
                                navController.navigate("settings")
                            }
                        )


                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("note")
                println("Botón pulsado")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Nota")
            }
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)) {

            Text("Mis Medidas", style = MaterialTheme.typography.headlineMedium)
            OutlinedTextField(
                value = filtro,
                onValueChange = { filtro = it },
                label = { Text("Buscar por cliente") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                //items(notas) { nota ->
                items(notasFiltradas) { nota ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                // abrimos la nota con su ID
                                navController.navigate("note/${nota.id}")
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { navController.navigate("note/${nota.id}") }
                            ) {
                                Text("Cliente: ${nota.cliente}", style = MaterialTheme.typography.titleMedium)
                                Text("Fecha: ${nota.fecha}")
                            }

                            Row {
                                IconButton(onClick = { compartirNota = nota }) {
                                    Icon(Icons.Default.Share , contentDescription = "Compartir Nota")
                                }
                                IconButton(onClick = { notaParaEliminar = nota }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar Nota")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Diálogo de confirmación
        notaParaEliminar?.let { nota ->
            AlertDialog(
                onDismissRequest = { notaParaEliminar = null },
                title = { Text("¿Eliminar nota?") },
                text = { Text("¿Seguro que quieres eliminar esta nota y sus medidas?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.eliminarNota(nota.id)
                        notaParaEliminar = null
                    }) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { notaParaEliminar = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
        compartirNota?.let { nota ->
            LaunchedEffect(nota) {
                viewModel.compartirNota(context, nota)
                compartirNota = null
            }
        }
    }
}

private fun parseFecha(fecha: String): Date {
    return try {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fecha) ?: Date(0)
    } catch (e: Exception) {
        Date(0)
    }
}


