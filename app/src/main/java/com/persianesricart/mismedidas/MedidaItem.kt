package com.persianesricart.mismedidas.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.text.input.KeyboardCapitalization
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MedidaItem(
    medida: Medida,
    ultimoMedidaId: Int,
    onUpdate: (Medida) -> Unit,
    onDelete: () -> Unit
) {
    val tipos = listOf("Persiana", "Mini", "Seguridad", "Mosquitera", "Toldo", "Apilable", "Cortina")
    val modelos = mapOf(
        "Persiana" to listOf("RR-45", "LC-45", "RC-39"),
        "Mini" to listOf("Alulux", "Compacto", "ThermoBox"),
        "Seguridad" to listOf("RE1000-C", "VisionRoll"),
        "Mosquitera" to listOf("Enrollable", "Plisada", "Minima", "Zip", "Fija"),
        "Toldo" to listOf("Brazo invisible", "Punto recto", "Vertical Cable", "Brazo balcon", "Corredero", "Monoblock"),
        "Apilable" to listOf("GradStore", "Lamisol", "Metalunic", "Grinotex"),
        "Cortina" to listOf("Premium", "Premium Plus", "ZBox", "FitBox")
    )
    val acabados = mapOf(
        "Persiana" to listOf("Tejido", "T/G", "CTA", "CTA s/guias"),
        "Seguridad" to listOf("Tejido", "T/G")
    )
    val colores = listOf("Blanco", "Natural", "Marfil", "7016", "7022", "Grafito", "6009", "6005")
    val motores = listOf("Mecánico", "Radio")

    val udFocus = remember { FocusRequester() }
    val anchoFocus = remember { FocusRequester() }
    val altoFocus = remember { FocusRequester() }
    val cargoAnchoFocus = remember { FocusRequester() }
    val cargoAltoFocus = remember { FocusRequester() }
    //val focusRequester = remember { FocusRequester() }


    LaunchedEffect(Unit) {
        if (medida.id == ultimoMedidaId) {
            udFocus.requestFocus()
        }
    }

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically) {
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
                            scope.launch {
                                bringIntoViewRequester.bringIntoView()
                            }
                        }
                    },

                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { anchoFocus.requestFocus() })
            )
            Spacer(modifier = Modifier.width(8.dp))
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
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = medida.alto,
                onValueChange = { onUpdate(medida.copy(alto = it)) },
                label = { Text("Alto") },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(altoFocus),
                keyboardOptions = KeyboardOptions(imeAction = if (medida.luz) ImeAction.Next else ImeAction.Done),
                keyboardActions = KeyboardActions(onNext = {
                    if (medida.luz) cargoAnchoFocus.requestFocus() else focusManager.clearFocus()
                })
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 4.dp)
            ) {
                // Aquí usamos 'this@Box' como contexto explícito
                IconButton(
                    onClick = { onDelete() },
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar medida")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
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
        Row(modifier = Modifier.fillMaxWidth()) {
            DropdownSelector(
                label = "Tipo",
                opciones = tipos,
                seleccionActual = medida.tipo,
                onSeleccion = {
                    onUpdate(medida.copy(tipo = it, modelo = "", acabado = null))
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            DropdownSelector(
                label = "Modelo",
                opciones = modelos[medida.tipo] ?: emptyList(),
                seleccionActual = medida.modelo,
                onSeleccion = { onUpdate(medida.copy(modelo = it)) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            DropdownSelector(
                label = "Acabado",
                opciones = acabados[medida.tipo] ?: emptyList(),
                seleccionActual = medida.acabado ?: "",
                onSeleccion = { onUpdate(medida.copy(acabado = it)) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            DropdownSelector(
                label = "Color",
                opciones = colores,
                seleccionActual = medida.color,
                onSeleccion = { onUpdate(medida.copy(color = it)) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
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
    }
}
