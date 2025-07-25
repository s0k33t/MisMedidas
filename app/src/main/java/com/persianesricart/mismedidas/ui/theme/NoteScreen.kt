package com.persianesricart.mismedidas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import com.persianesricart.mismedidas.data.entities.Medida
import com.persianesricart.mismedidas.viewmodel.NoteViewModel
import com.persianesricart.mismedidas.viewmodel.ajustes.AjustesViewModel
import com.persianesricart.mismedidas.data.ajustes.entities.Tipo



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    navController: NavController,
    viewModel: NoteViewModel,
    ajustesViewModel: AjustesViewModel
) {
    val focusManager = LocalFocusManager.current
    val clienteFocus = remember { FocusRequester() }
    val referenciaFocus = remember { FocusRequester() }
    val direccionFocus = remember { FocusRequester() }
    val poblacionFocus = remember { FocusRequester() }
    val telefonoFocus = remember { FocusRequester() }
    val movilFocus = remember { FocusRequester() }
    val emailFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        ajustesViewModel.loadTipos()
    }
    val tipos by ajustesViewModel.tipos.collectAsState(initial = emptyList())

    Scaffold(
        floatingActionButton = {
            val ultima = viewModel.medidas.lastOrNull()
            Row(modifier = Modifier.padding(16.dp)) {
                FloatingActionButton(onClick = {
                    val ultima = viewModel.medidas.lastOrNull()

                    val nueva = Medida(
                        notaId = 0,
                        ud = "",
                        ancho = "",
                        alto = "",
                        tipo = ultima?.tipo ?: "",
                        modelo = ultima?.modelo ?: "",
                        acabado = ultima?.acabado,
                        color = ultima?.color ?: "",
                        motor = ultima?.motor,
                        comentario = "",
                        luz = ultima?.luz ?: false,
                        cargoAncho = ultima?.cargoAncho,
                        cargoAlto = ultima?.cargoAlto
                    )

                    viewModel.addMedida(nueva)
                    viewModel.setUltimoMedidaId(nueva.id)
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Medida")
                }
                Spacer(modifier = Modifier.width(16.dp))
                FloatingActionButton(onClick = {
                    if (viewModel.cliente.isNotBlank()) {
                        viewModel.saveNota {
                            navController.navigate("main?reload=true") {
                                popUpTo("main") { inclusive = true }
                            }
                        }
                    }
                }) {
                    Icon(Icons.Default.Save, contentDescription = "Guardar Nota")
                }
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier
            .padding(padding)
            .padding(16.dp)) {
            item {
                Text("Nueva Nota", style = MaterialTheme.typography.headlineMedium)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = viewModel.cliente,
                        onValueChange = { viewModel.onClienteChanged(it) },
                        label = { Text("Cliente") },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(clienteFocus),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { referenciaFocus.requestFocus() })
                    )
                    OutlinedTextField(
                        value = viewModel.referencia,
                        onValueChange = { viewModel.onReferenciaChanged(it) },
                        label = { Text("Referencia") },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(referenciaFocus),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { direccionFocus.requestFocus() })
                    )
                }
                OutlinedTextField(
                    value = viewModel.direccion,
                    onValueChange = { viewModel.onDireccionChanged(it) },
                    label = { Text("Dirección") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(direccionFocus),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { poblacionFocus.requestFocus() })
                )
                OutlinedTextField(
                    value = viewModel.poblacion,
                    onValueChange = { viewModel.onPoblacionChanged(it) },
                    label = { Text("Población") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(poblacionFocus),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { telefonoFocus.requestFocus() })
                )
                OutlinedTextField(
                    value = viewModel.telefono,
                    onValueChange = { viewModel.onTelefonoChanged(it) },
                    label = { Text("Teléfono") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(telefonoFocus),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { movilFocus.requestFocus() })
                )
                OutlinedTextField(
                    value = viewModel.movil,
                    onValueChange = { viewModel.onMovilChanged(it) },
                    label = { Text("Móvil") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(movilFocus),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { emailFocus.requestFocus() })
                )
                OutlinedTextField(
                    value = viewModel.email,
                    onValueChange = { viewModel.onEmailChanged(it) },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(emailFocus),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
                OutlinedTextField(
                    value = viewModel.fecha,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            items(viewModel.medidas) { medida ->

                MedidaItem(
                    medida = medida,
                    ultimoMedidaId = viewModel.ultimoMedidaId,
                    ajustesViewModel = ajustesViewModel,
                    tipos = tipos,
                    onUpdate = { nueva ->
                        val idx = viewModel.medidas.indexOf(medida)
                        if (idx != -1) viewModel.updateMedida(idx, nueva)
                    },
                    onDelete = {
                        val idx = viewModel.medidas.indexOf(medida)
                        if (idx != -1) viewModel.removeMedida(idx)
                    }
                )
            }
        }
    }
}
