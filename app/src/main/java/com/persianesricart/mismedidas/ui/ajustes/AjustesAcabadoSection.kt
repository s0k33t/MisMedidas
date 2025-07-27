/*
package com.persianesricart.mismedidas.ui.ajustes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.persianesricart.mismedidas.data.ajustes.entities.Acabado
import com.persianesricart.mismedidas.data.ajustes.entities.Modelo
import com.persianesricart.mismedidas.data.ajustes.entities.Tipo
import com.persianesricart.mismedidas.viewmodel.ajustes.AjustesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesAcabadoSection(viewModel: AjustesViewModel) {
    val focusManager = LocalFocusManager.current

    // 1) Dropdown de Tipos
    val tipos by viewModel.tipos.collectAsState(initial = emptyList())
    var selTipo by remember { mutableStateOf<Tipo?>(null) }
    var expTipo by remember { mutableStateOf(false) }

    // 2) Dropdown de Modelos (dependiente del tipo)
    val modelos by viewModel.modelos.collectAsState(initial = emptyList())
    var selModelo by remember { mutableStateOf<Modelo?>(null) }
    var expModelo by remember { mutableStateOf(false) }

    // 3) Lista de Acabados para el modelo seleccionado
    //    en el ViewModel es un snapshotStateListOf<Acabado>
    val acabados: List<Acabado> = viewModel.acabados

    // 4) campo para nuevo Acabado
    var nuevoAcabado by remember { mutableStateOf("") }

    // Carga inicial de Tipos si es necesario (opcional si ya se hizo en init)
    LaunchedEffect(Unit) {
        viewModel.loadTipos()
    }

    // Cuando cambie Tipo, recarga Modelos
    LaunchedEffect(selTipo) {
        selTipo?.let { viewModel.loadModelos(it.id) }
        // limpia selección de Modelo y Acabados
        selModelo = null
        viewModel.clearAcabados()
    }

    // Cuando cambie Modelo, recarga Acabados
    LaunchedEffect(selModelo) {
        selModelo?.let { viewModel.loadAcabados(it.id) }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        // — Dropdown de Tipos —
        Text("Selecciona Tipo", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expTipo,
            onExpandedChange = { expTipo = !expTipo }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = selTipo?.nombre ?: "",
                onValueChange = {},
                label = { Text("Tipo") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expTipo) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expTipo,
                onDismissRequest = { expTipo = false }
            ) {
                tipos.forEach { tipo ->
                    DropdownMenuItem(
                        text = { Text(tipo.nombre) },
                        onClick = {
                            selTipo = tipo
                            expTipo = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // — Dropdown de Modelos —
        selTipo?.let {
            Text("Selecciona Modelo", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = expModelo,
                onExpandedChange = { expModelo = !expModelo }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = selModelo?.nombre ?: "",
                    onValueChange = {},
                    label = { Text("Modelo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expModelo) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expModelo,
                    onDismissRequest = { expModelo = false }
                ) {
                    modelos.forEach { modelo ->
                        DropdownMenuItem(
                            text = { Text(modelo.nombre) },
                            onClick = {
                                selModelo = modelo
                                expModelo = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // — Lista de Acabados —
        selModelo?.let { modelo ->
            Text("Acabados de “${modelo.nombre}”", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            acabados.forEachIndexed { idx, acabado ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(acabado.nombre, modifier = Modifier.weight(1f))

                    Row {
                        IconButton(
                            enabled = idx > 0,
                            onClick = { viewModel.moverAcabadoArriba(acabado) }
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Subir acabado")
                        }
                        IconButton(
                            enabled = idx < acabados.lastIndex,
                            onClick = { viewModel.moverAcabadoAbajo(acabado) }
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Bajar acabado")
                        }
                        IconButton(onClick = { viewModel.eliminarAcabado(acabado) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar acabado")
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // — Campo Nuevo Acabado (Enter = Añadir) —
            OutlinedTextField(
                value = nuevoAcabado,
                onValueChange = { nuevoAcabado = it },
                label = { Text("Nuevo acabado") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (nuevoAcabado.isNotBlank()) {
                        viewModel.insertarAcabado(nuevoAcabado, modelo.id)
                        nuevoAcabado = ""
                    }
                    focusManager.clearFocus()
                })
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (nuevoAcabado.isNotBlank()) {
                        viewModel.insertarAcabado(nuevoAcabado, modelo.id)
                        nuevoAcabado = ""
                    }
                    focusManager.clearFocus()
                },
                enabled = nuevoAcabado.isNotBlank(),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Añadir acabado")
            }
        }
    }
}

*/


package com.persianesricart.mismedidas.ui.ajustes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

import com.persianesricart.mismedidas.data.ajustes.entities.Modelo
import com.persianesricart.mismedidas.viewmodel.ajustes.AjustesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesAcabadoSection(viewModel: AjustesViewModel) {
    // Dropdown de Modelos (todos)
    val modelos by viewModel.modelos.collectAsState(initial = emptyList())
    var selectedModelo by remember { mutableStateOf<Modelo?>(null) }
    var expandedModelo by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Lista de acabados filtrados
    val acabados by viewModel.acabados.collectAsState(initial = emptyList())
    var nuevoAcabado by remember { mutableStateOf("") }

    //LaunchedEffect(Unit) {
    //    viewModel.loadModelosGeneral()
    //}
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
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expandedModelo)
                },
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
                            viewModel.loadAcabados(modelo.id)
                            expandedModelo = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Solo si hay Modelo seleccionado
        selectedModelo?.let { modelo ->
            // Lista de acabados
            acabados.forEachIndexed {idx, acabado ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(acabado.nombre)
                    Row {
                        // ▲ Arriba
                        IconButton(
                            enabled = idx > 0,
                            onClick = { viewModel.moverAcabadoArriba(acabado) }
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Subir")
                        }
                        // ▼ Abajo
                        IconButton(
                            enabled = idx < acabados.lastIndex,
                            onClick = { viewModel.moverAcabadoAbajo(acabado) }
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Bajar")
                        }
                        IconButton(onClick = { viewModel.eliminarAcabado(acabado) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar acabado")
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Añadir acabado
            OutlinedTextField(
                value = nuevoAcabado,
                onValueChange = { nuevoAcabado = it },
                label = { Text("Nuevo acabado") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (nuevoAcabado.isNotBlank()) {
                        viewModel.insertarAcabado(nuevoAcabado, modelo.id)
                        nuevoAcabado = ""
                    }
                    focusManager.clearFocus()
                })
            )
            Button(
                onClick = {
                    if (nuevoAcabado.isNotBlank()) {
                        viewModel.insertarAcabado(nuevoAcabado, modelo.id)
                        nuevoAcabado = ""
                    }
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
            ) {
                Text("Añadir Acabado")
            }
        }
    }
}
