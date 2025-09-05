package com.persianesricart.mismedidas.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel

import com.persianesricart.mismedidas.data.AppDatabase
import com.persianesricart.mismedidas.data.entities.Medida
import com.persianesricart.mismedidas.viewmodel.NoteViewModel
import com.persianesricart.mismedidas.viewmodel.CroquisViewModel
import com.persianesricart.mismedidas.viewmodel.CroquisViewModelFactory
import com.persianesricart.mismedidas.viewmodel.ajustes.AjustesViewModel
import com.persianesricart.mismedidas.data.ajustes.entities.Tipo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    navController: NavController,
    viewModel: NoteViewModel,
    ajustesViewModel: AjustesViewModel
) {
    // Focus para cabeceras
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val clienteFocus = remember { FocusRequester() }
    val referenciaFocus = remember { FocusRequester() }
    val direccionFocus = remember { FocusRequester() }
    val poblacionFocus = remember { FocusRequester() }
    val telefonoFocus = remember { FocusRequester() }
    val movilFocus = remember { FocusRequester() }
    val emailFocus = remember { FocusRequester() }

    // Tipos (Ajustes)
    val tipos: List<Tipo> by ajustesViewModel.tipos.collectAsState(initial = emptyList())

    // Croquis VM (local a esta pantalla)
    val context = LocalContext.current
    val croquisDao = remember(context) { AppDatabase.getInstance(context).croquisDao() }
    val croquisFactory = remember(croquisDao) { CroquisViewModelFactory(croquisDao) }
    val croquisVM: CroquisViewModel = viewModel(factory = croquisFactory)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (viewModel.notaId != null && viewModel.notaId != 0) "Editar nota" else "Nueva nota")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            val ultima = viewModel.medidas.lastOrNull()
            Row(modifier = Modifier.padding(16.dp)) {
                FloatingActionButton(onClick = {
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
                        cargoAlto = ultima?.cargoAlto,
                        uuid = java.util.UUID.randomUUID().toString()
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
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            // Cabecera de nota
            item {
                //Text("Nueva Nota", style = MaterialTheme.typography.headlineMedium)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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

            // Lista de medidas
            items(viewModel.medidas) { medida ->
                // Cargar croquis para esta medida
                LaunchedEffect(medida.id) {
                    if (medida.id != 0) {
                        croquisVM.loadCroquis(medida.id)
                    }
                }
                val croquis by croquisVM
                    .croquisFlow(medida.id)
                    .collectAsState(initial = emptyList())

                // MedidaItem ahora recibe también lista de croquis y callbacks
                MedidaItem(
                    medida = medida,
                    ultimoMedidaId = viewModel.ultimoMedidaId,
                    ajustesViewModel = ajustesViewModel,
                    tipos = tipos,
                    onUpdate = { nuevaMedida ->
                        val index = viewModel.medidas.indexOf(medida)
                        if (index != -1) viewModel.updateMedida(index, nuevaMedida)
                    },
                    onDelete = {
                        val index = viewModel.medidas.indexOf(medida)
                        if (index != -1) viewModel.removeMedida(index)
                    },
                    // NUEVO: abrir lienzo de croquis
                    onOpenCroquis = { medidaId, croquisId ->
                        if (medidaId == 0) {
                            Toast.makeText(
                                context,
                                "Guarda la nota antes de añadir croquis",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            if (croquisId == null) {
                                navController.navigate("croquis/$medidaId")
                            } else {
                                navController.navigate("croquis/$medidaId/$croquisId")
                            }
                        }

                    },
                    croquis = croquis,
                    onDeleteCroquis = { c ->
                        croquisVM.deleteCroquis(context, c)
                    }
                )

                // Separador suave entre medidas (si lo deseas)
                Divider(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                )
            }

            // Aire al final para no tapar con FABs
            item { Spacer(modifier = Modifier.height(96.dp)) }
        }
    }
}

/*
package com.persianesricart.mismedidas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
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
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    navController: NavController,
    viewModel: NoteViewModel,
    ajustesViewModel: AjustesViewModel
) {
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
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
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Atras")
                    }
                },
                title = { Text("Nueva Nota", style = MaterialTheme.typography.headlineMedium) }
            )
        },
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

                    scope.launch{
                        val lastIndex = viewModel.medidas.lastIndex
                        if(lastIndex >= 0){
                            listState.animateScrollToItem(lastIndex, scrollOffset = -150)
                        }
                    }
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
            .padding(16.dp),
            state = listState) {
            item {
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
                LaunchedEffect(medida.id) { croquisVM.loadCroquis(medida.id) }
                val croquis by croquisVM.croquisFlow(medida.id).collectAsState(initial = emptyList())

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
                            onOpenCroquis = { medidaId, croquisId ->
                        if (croquisId == null)
                            navController.navigate("croquis/$medidaId")
                        else
                            navController.navigate("croquis/$medidaId/$croquisId")
                    },
                    croquis = croquis,
                    onDeleteCroquis = { c -> croquisVM.deleteCroquis(context, c) }
                )
                //Linea separadora bajo cada medida
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            }


            //Espaciador final para no tapar con FABs
            item {Spacer(modifier = Modifier.height(96.dp))}
        }
    }
}
*/