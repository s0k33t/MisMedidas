package com.persianesricart.mismedidas.ui.ajustes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.unit.dp
import com.persianesricart.mismedidas.data.ajustes.entities.Color
import com.persianesricart.mismedidas.data.ajustes.entities.Modelo
import com.persianesricart.mismedidas.viewmodel.ajustes.AjustesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesColorSection(viewModel: AjustesViewModel) {
    val modelos by viewModel.modelos.collectAsState(initial = emptyList())
    var selectedModelo by remember { mutableStateOf<Modelo?>(null) }
    var expandedModelo by remember { mutableStateOf(false) }

    val colores by viewModel.colores.collectAsState(initial = emptyList())
    var nuevoColor by remember { mutableStateOf("") }

    // Para controlar el teclado
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Selecciona Modelo", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))

        ExposedDropdownMenuBox(
            expanded = expandedModelo,
            onExpandedChange = { expandedModelo = !expandedModelo }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = selectedModelo?.nombre ?: "",
                onValueChange = {},
                label = { Text("Modelo") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedModelo) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedModelo,
                onDismissRequest = { expandedModelo = false }
            ) {
                modelos.forEach { modelo ->
                    DropdownMenuItem(
                        text = { Text(modelo.nombre) },
                        onClick = {
                            selectedModelo = modelo
                            viewModel.loadColores(modelo.id)
                            expandedModelo = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        selectedModelo?.let { modelo ->
            // Lista de colores existentes
            colores.forEachIndexed { idx, color ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(color.nombre)
                    Row {
                        // ▲ Arriba
                        IconButton(
                            enabled = idx > 0,
                            onClick = { viewModel.moverColorArriba(color) }
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Subir")
                        }
                        // ▼ Abajo
                        IconButton(
                            enabled = idx < colores.lastIndex,
                            onClick = { viewModel.moverColorAbajo(color) }
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Bajar")
                        }
                        IconButton(onClick = { viewModel.eliminarColor(color) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar color")
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Campo para nuevo color con Enter = Añadir
            OutlinedTextField(
                value = nuevoColor,
                onValueChange = { nuevoColor = it },
                label = { Text("Nuevo color") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (nuevoColor.isNotBlank()) {
                            viewModel.insertarColor(nuevoColor, modelo.id)
                            nuevoColor = ""
                            viewModel.loadColores(modelo.id)
                        }
                        focusManager.clearFocus()
                    }
                )
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (nuevoColor.isNotBlank()) {
                        viewModel.insertarColor(nuevoColor, modelo.id)
                        nuevoColor = ""
                        viewModel.loadColores(modelo.id)
                    }
                    focusManager.clearFocus()
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
            ) {
                Text("Añadir color")
            }
        }
    }
}

/*package com.persianesricart.mismedidas.ui.ajustes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.persianesricart.mismedidas.data.ajustes.entities.Color
import com.persianesricart.mismedidas.data.ajustes.entities.Modelo
import com.persianesricart.mismedidas.viewmodel.ajustes.AjustesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesColorSection(viewModel: AjustesViewModel) {
    val modelos by viewModel.modelosGeneral.collectAsState(initial = emptyList())
    var selectedModelo by remember { mutableStateOf<Modelo?>(null) }
    var expandedModelo by remember { mutableStateOf(false) }
    val colores by viewModel.colores.collectAsState(initial = emptyList())
    var nuevoColor by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Selecciona Modelo", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))

        ExposedDropdownMenuBox(
            expanded = expandedModelo,
            onExpandedChange = { expandedModelo = !expandedModelo }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = selectedModelo?.nombre ?: "",
                onValueChange = {},
                label = { Text("Modelo") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedModelo) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedModelo,
                onDismissRequest = { expandedModelo = false }
            ) {
                modelos.forEach { modelo ->
                    DropdownMenuItem(
                        text = { Text(modelo.nombre) },
                        onClick = {
                            selectedModelo = modelo
                            viewModel.loadColores(modelo.id)
                            expandedModelo = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        selectedModelo?.let { modelo ->
            colores.forEach { color: Color ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(color.nombre)
                    IconButton(onClick = { viewModel.eliminarColor(color) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar color")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = nuevoColor,
                onValueChange = { nuevoColor = it },
                label = { Text("Nuevo color") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (nuevoColor.isNotBlank()) {
                        viewModel.insertarColor(nuevoColor, modelo.id)
                        nuevoColor = ""
                        viewModel.loadColores(modelo.id)
                    }
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
            ) {
                Text("Añadir Color")
            }
        }
    }
}
*/