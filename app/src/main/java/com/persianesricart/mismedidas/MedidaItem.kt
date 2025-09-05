package com.persianesricart.mismedidas.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import com.persianesricart.mismedidas.data.entities.Medida
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.tooling.preview.Preview
import com.persianesricart.mismedidas.viewmodel.ajustes.AjustesViewModel
import com.persianesricart.mismedidas.data.ajustes.entities.Tipo
import kotlinx.coroutines.launch


import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MedidaItem(
    medida: Medida,
    ultimoMedidaId: Int,
    ajustesViewModel: AjustesViewModel,
    tipos: List<Tipo>, // se mantiene para no romper llamadas existentes
    onUpdate: (Medida) -> Unit,
    onDelete: () -> Unit,
    //onDuplicate: (() -> Unit)? = null // opcional: si no lo pasas, el botón se desactiva
    onOpenCroquis: (Int, Int?) -> Unit,
    croquis: List<com.persianesricart.mismedidas.data.entities.Croquis>,
    onDeleteCroquis: (com.persianesricart.mismedidas.data.entities.Croquis) -> Unit

) {
    // Estados locales “visuales”
    var tipo by rememberSaveable { mutableStateOf(medida.tipo) }
    var modelo by rememberSaveable { mutableStateOf(medida.modelo) }
    var acabado by rememberSaveable { mutableStateOf(medida.acabado ?: "") }
    var color by rememberSaveable { mutableStateOf(medida.color) }

    // Flujos de Ajustes (OJO con el nombre: evitamos colisión con el parámetro 'tipos')
    val tiposState by ajustesViewModel.tipos.collectAsState(initial = emptyList())
    val modelos by ajustesViewModel.modelos.collectAsState(initial = emptyList())
    val acabados by ajustesViewModel.acabados.collectAsState(initial = emptyList())
    val colores by ajustesViewModel.colores.collectAsState(initial = emptyList())

    val motores = listOf("Mecánico", "Radio")

    // Focus & bring into view
    val udFocus = remember { FocusRequester() }
    val anchoFocus = remember { FocusRequester() }
    val altoFocus = remember { FocusRequester() }
    val cargoAnchoFocus = remember { FocusRequester() }
    val cargoAltoFocus = remember { FocusRequester() }

    LaunchedEffect(ultimoMedidaId, medida.id) {
        if (medida.id == ultimoMedidaId) {
            udFocus.requestFocus()
        }
    }
    LaunchedEffect(Unit) {
        if (medida.id == ultimoMedidaId) {
            udFocus.requestFocus()
        }
    }

    LaunchedEffect(medida.luz) {
        if(medida.luz){
            if(!medida.ud.isEmpty() || !medida.ancho.isEmpty() || !medida.alto.isEmpty()){
                cargoAnchoFocus.requestFocus()
            }

        }
    }

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    // ===== Card contenedora de la medida =====
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // ===== Fila Ud, Ancho, Alto + acciones =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = medida.ud,
                    onValueChange = { onUpdate(medida.copy(ud = it)) },
                    label = { Text("Ud") },
                    modifier = Modifier
                        .width(80.dp)
                        .focusRequester(udFocus)
                        .bringIntoViewRequester(bringIntoViewRequester)
                        .onFocusEvent {
                            if (it.isFocused) {
                                scope.launch { bringIntoViewRequester.bringIntoView() }
                            }
                        },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { anchoFocus.requestFocus() })
                )

                OutlinedTextField(
                    value = medida.ancho,
                    onValueChange = { onUpdate(medida.copy(ancho = it)) },
                    label = { Text("Ancho") },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(anchoFocus),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { altoFocus.requestFocus() })
                )

                OutlinedTextField(
                    value = medida.alto,
                    onValueChange = { onUpdate(medida.copy(alto = it)) },
                    label = { Text("Alto") },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(altoFocus),
                    keyboardOptions = KeyboardOptions(
                        imeAction = if (medida.luz) ImeAction.Next else ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        if (medida.luz) cargoAnchoFocus.requestFocus() else focusManager.clearFocus()
                    })
                )

                // Acciones (Duplicar / Borrar)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    /*
                    IconButton(
                        onClick = { onDuplicate?.invoke() },
                        enabled = onDuplicate != null
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Duplicar medida")
                    }*/

                    IconButton(onClick = { onDelete() }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar medida")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ===== Luz + CargoAncho/CargoAlto =====
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = medida.luz,
                        onCheckedChange = { onUpdate(medida.copy(luz = it)) }
                    )
                    Text("Luz")
                }

                if (medida.luz) {
                    OutlinedTextField(
                        value = medida.cargoAncho ?: "",
                        onValueChange = { onUpdate(medida.copy(cargoAncho = it)) },
                        label = { Text("Cargo Ancho") },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(cargoAnchoFocus),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { cargoAltoFocus.requestFocus() })
                    )
                    OutlinedTextField(
                        value = medida.cargoAlto ?: "",
                        onValueChange = { onUpdate(medida.copy(cargoAlto = it)) },
                        label = { Text("Cargo Alto") },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(cargoAltoFocus),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ===== Tipo / Modelo =====
            Row(modifier = Modifier.fillMaxWidth()) {
                DropdownSelector(
                    label = "Tipo",
                    opciones = tiposState.map { it.nombre }, // usamos los del VM
                    seleccionActual = tipo,
                    onSeleccion = { selNombre ->
                        tipo = selNombre
                        // recarga modelos
                        val selTipo = tiposState.first { it.nombre == selNombre }
                        ajustesViewModel.loadModelos(selTipo.id)
                        // limpia descendientes
                        modelo = ""
                        acabado = ""
                        color = ""
                        onUpdate(
                            medida.copy(
                                tipo = tipo,
                                modelo = modelo,
                                acabado = acabado.takeIf { it.isNotBlank() },
                                color = color
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                DropdownSelector(
                    label = "Modelo",
                    opciones = modelos.map { it.nombre },
                    seleccionActual = modelo,
                    onSeleccion = { selNombre ->
                        modelo = selNombre
                        val selModelo = modelos.first { it.nombre == selNombre }
                        ajustesViewModel.loadAcabados(selModelo.id)
                        ajustesViewModel.loadColores(selModelo.id)
                        acabado = ""
                        color = ""
                        onUpdate(
                            medida.copy(
                                tipo = tipo,
                                modelo = modelo,
                                acabado = acabado.takeIf { it.isNotBlank() },
                                color = color
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ===== Acabado / Color =====
            Row(modifier = Modifier.fillMaxWidth()) {
                DropdownSelector(
                    label = "Acabado",
                    opciones = acabados.map { it.nombre },
                    seleccionActual = acabado,
                    onSeleccion = { sel ->
                        acabado = sel
                        onUpdate(medida.copy(acabado = sel))
                    },
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))

                DropdownSelector(
                    label = "Color",
                    opciones = colores.map { it.nombre },
                    seleccionActual = color,
                    onSeleccion = { sel ->
                        color = sel
                        onUpdate(medida.copy(color = sel))
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ===== Motor =====
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = medida.motor != null,
                    onCheckedChange = {
                        val motor = if (it) motores.first() else null
                        onUpdate(medida.copy(motor = motor))
                    }
                )
                Text("Con motor", modifier = Modifier.padding(start = 8.dp))
                Spacer(modifier = Modifier.width(16.dp))
                if (medida.motor != null) {
                    DropdownSelector(
                        label = "Tipo motor",
                        opciones = motores,
                        seleccionActual = medida.motor ?: motores.first(),
                        onSeleccion = { onUpdate(medida.copy(motor = it)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ===== Comentario =====
            OutlinedTextField(
                value = medida.comentario,
                onValueChange = { onUpdate(medida.copy(comentario = it)) },
                label = { Text("Comentario") },
                modifier = Modifier.fillMaxWidth(),
                keyboardActions = KeyboardActions(onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                })
            )

            Spacer(Modifier.height(8.dp))

            Text("Croquis", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))

            if (croquis.isEmpty()) {
                Text("No hay croquis", style = MaterialTheme.typography.bodyMedium)
            } else {
                croquis.forEach { c ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = c.nombre,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                                .clickable { onOpenCroquis(medida.id, c.id) },
                            style = MaterialTheme.typography.bodyLarge
                        )
                        IconButton(onClick = { onDeleteCroquis(c) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar croquis")
                        }
                    }
                    Divider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                    )
                }
            }

            OutlinedButton(
                onClick = { onOpenCroquis(medida.id, null) },
                modifier = Modifier.align(Alignment.End)
            ) { Text("Añadir croquis") }
        }
    }
}

@Preview(showBackground = true, name = "Card de Medida (solo estilo)")
@Composable
private fun Preview_MedidaCard_Style() {
    MaterialTheme {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text("Medida (preview visual)", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Aquí irían Ud, Ancho, Alto…")
                Spacer(Modifier.height(8.dp))
                Divider()
                Spacer(Modifier.height(8.dp))
                Text("Dropdowns: Tipo / Modelo / Acabado / Color…")
            }
        }
    }
}