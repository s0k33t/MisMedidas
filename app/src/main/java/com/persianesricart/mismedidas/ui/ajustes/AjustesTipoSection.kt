package com.persianesricart.mismedidas.ui.ajustes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
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
import com.persianesricart.mismedidas.data.ajustes.entities.Tipo
import com.persianesricart.mismedidas.viewmodel.ajustes.AjustesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesTipoSection(viewModel: AjustesViewModel) {
    val tipos by viewModel.tipos.collectAsState(initial = emptyList())
    var nuevoNombre by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxWidth()) {
        tipos.forEachIndexed { idx, tipo ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(tipo.nombre)
                Row {
                    // ▲ Arriba
                    IconButton(
                        enabled = idx > 0,
                        onClick = { viewModel.moverTipoArriba(tipo) }
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Subir")
                    }
                    // ▼ Abajo
                    IconButton(
                        enabled = idx < tipos.lastIndex,
                        onClick = { viewModel.moverTipoAbajo(tipo) }
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Bajar")
                    }
                    IconButton(onClick = { viewModel.eliminarTipo(tipo) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar tipo")
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = nuevoNombre,
            onValueChange = { nuevoNombre = it },
            label = { Text("Nuevo tipo") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (nuevoNombre.isNotBlank()) {
                    viewModel.insertarTipo(nuevoNombre)
                    nuevoNombre = ""
                }
                focusManager.clearFocus()
            })
        )
        Button(
            onClick = {
                if (nuevoNombre.isNotBlank()) {
                    viewModel.insertarTipo(nuevoNombre)
                    nuevoNombre = ""
                }
            },
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 8.dp)
        ) {
            Text("Añadir")
        }
    }
}
