package com.persianesricart.mismedidas.ui.ajustes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.persianesricart.mismedidas.data.ajustes.entities.Acabado
import com.persianesricart.mismedidas.data.ajustes.entities.Modelo
import com.persianesricart.mismedidas.viewmodel.ajustes.AjustesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesAcabadoSection(viewModel: AjustesViewModel) {
    // Dropdown de Modelos (todos)
    val modelos by viewModel.modelosGeneral.collectAsState(initial = emptyList())
    var selectedModelo by remember { mutableStateOf<Modelo?>(null) }
    var expandedModelo by remember { mutableStateOf(false) }

    // Lista de acabados filtrados
    val acabados by viewModel.acabados.collectAsState(initial = emptyList())
    var nuevoAcabado by remember { mutableStateOf("") }

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
            acabados.forEach { acabado: Acabado ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(acabado.nombre)
                    IconButton(onClick = { viewModel.eliminarAcabado(acabado) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar acabado")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Añadir acabado
            OutlinedTextField(
                value = nuevoAcabado,
                onValueChange = { nuevoAcabado = it },
                label = { Text("Nuevo acabado") },
                modifier = Modifier.fillMaxWidth()
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
