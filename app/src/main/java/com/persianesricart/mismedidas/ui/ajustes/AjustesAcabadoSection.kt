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
import com.persianesricart.mismedidas.viewmodel.ajustes.AjustesViewModel

@Composable
fun AjustesAcabadoSection(viewModel: AjustesViewModel) {
    val acabados by viewModel.acabados.collectAsState(initial = emptyList())
    var nuevoAcabado by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
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

        OutlinedTextField(
            value = nuevoAcabado,
            onValueChange = { nuevoAcabado = it },
            label = { Text("Nuevo acabado") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                if (nuevoAcabado.isNotBlank()) {
                    viewModel.insertarAcabado(nuevoAcabado)
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
