package com.persianesricart.mismedidas.ui.ajustes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.persianesricart.mismedidas.data.ajustes.entities.Modelo
import com.persianesricart.mismedidas.data.ajustes.entities.Tipo
import com.persianesricart.mismedidas.viewmodel.ajustes.AjustesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesModeloSection(viewModel: AjustesViewModel) {
    val tipos by viewModel.tipos.collectAsState(initial = emptyList())
    var selectedTipo by remember { mutableStateOf<Tipo?>(null) }
    var expandedTipo by remember { mutableStateOf(false) }
    val modelos by viewModel.modelos.collectAsState(initial = emptyList())
    var nuevoModelo by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Selecciona Tipo", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))

        ExposedDropdownMenuBox(
            expanded = expandedTipo,
            onExpandedChange = { expandedTipo = !expandedTipo }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = selectedTipo?.nombre ?: "",
                onValueChange = {},
                label = { Text("Tipo") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedTipo) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedTipo,
                onDismissRequest = { expandedTipo = false }
            ) {
                tipos.forEach { tipo ->
                    DropdownMenuItem(
                        text = { Text(tipo.nombre) },
                        onClick = {
                            selectedTipo = tipo
                            viewModel.loadModelos(tipo.id)
                            expandedTipo = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        selectedTipo?.let { tipo ->
            modelos.forEach { modelo: Modelo ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(modelo.nombre)
                    IconButton(onClick = { viewModel.eliminarModelo(modelo) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar modelo")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = nuevoModelo,
                onValueChange = { nuevoModelo = it },
                label = { Text("Nuevo modelo") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (nuevoModelo.isNotBlank()) {
                        viewModel.insertarModelo(nuevoModelo, tipo.id)
                        nuevoModelo = ""
                        viewModel.loadModelos(tipo.id)
                    }
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
            ) {
                Text("Añadir Modelo")
            }
        }
    }
}
