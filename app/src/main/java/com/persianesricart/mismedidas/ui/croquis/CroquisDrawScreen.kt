package com.persianesricart.mismedidas.ui.croquis

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.persianesricart.mismedidas.viewmodel.CroquisViewModel
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.max
import androidx.compose.material.icons.filled.MoreVert
//import androidx.compose.ui.input.pointer.awaitEachGesture
//import androidx.compose.ui.input.pointer.awaitFirstDown
//import androidx.compose.ui.input.pointer.drag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
//import androidx.compose.ui.graphics.toAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import kotlin.math.hypot
import kotlin.math.max

private data class StrokePath(
    val points: List<Offset>,
    val color: Color,
    val width: Float,
    val isEraser: Boolean,
    val isDot: Boolean = false,
    val dotRadius: Float = 4f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CroquisDrawScreen(
    navController: NavController,
    medidaId: Int,
    croquisId: Int?,            // null = nuevo, != null = editar
    viewModel: CroquisViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Estado dibujo
    var nombre by remember { mutableStateOf("Croquis") }
    var paths by remember { mutableStateOf(listOf<StrokePath>()) }
    var redoStack by remember { mutableStateOf(listOf<StrokePath>()) }
    var current by remember { mutableStateOf(listOf<Offset>()) }
    var strokeWidth by remember { mutableStateOf(6f) }
    var eraserWidth by remember { mutableStateOf(20f) }
    var eraserOn by remember { mutableStateOf(false) }

    // Paleta / color
    val palette = listOf(
        Color.Black, Color.DarkGray, Color.Gray,
        Color(0xFFEF5350), Color(0xFFAB47BC), Color(0xFF42A5F5),
        Color(0xFF26A69A), Color(0xFFFFCA28), Color(0xFFFF8A65)
    )
    var selectedColor by remember { mutableStateOf(Color.Black) }

    // Fondo si editamos
    var background by remember { mutableStateOf<ImageBitmap?>(null) }

    // Medida del canvas para exportar 1:1
    var canvasSize by remember { mutableStateOf(IntSize(0, 0)) }

    // Cargar croquis existente como fondo
    LaunchedEffect(croquisId) {
        if (croquisId != null) {
            val c = viewModel.getById(croquisId)
            if (c != null) {
                nombre = c.nombre
                runCatching { BitmapFactory.decodeFile(c.filePath)?.let { background = it.asImageBitmap() } }
            }
        }
    }

    // Menú Ajustes (paleta + grosor)
    var menuOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                title = { Text(if (croquisId == null) "Nuevo croquis" else "Editar croquis") },
                actions = {
                    // Limpiar todos los trazos
                    IconButton(onClick = {
                        paths = emptyList()
                        redoStack = emptyList()
                        current = emptyList()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Limpiar")
                    }

                    // Deshacer último trazo
                    IconButton(
                        onClick = {
                            if (paths.isNotEmpty()) {
                                val last = paths.last()
                                paths = paths.dropLast(1)
                                redoStack = redoStack + last
                            }
                        },
                        enabled = paths.isNotEmpty()
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.ArrowBack),
                            contentDescription = "Deshacer último trazo"
                        )
                    }

                    // Guardar croquis
                    IconButton(onClick = {
                        scope.launch {
                            val bitmap = renderToBitmapWithBackground(
                                background = background,
                                paths = paths,
                                current = current,
                                outSize = if (canvasSize.width > 0) canvasSize else IntSize(1080, 1920)
                            )
                            if (croquisId == null) {
                                viewModel.saveNewCroquis(
                                    context = context,
                                    medidaId = medidaId,
                                    nombre = if (nombre.isBlank()) "Croquis ${max(1, paths.size)}" else nombre,
                                    bitmap = bitmap
                                )
                            } else {
                                viewModel.overwriteCroquis(
                                    context = context,
                                    croquisId = croquisId,
                                    bitmap = bitmap
                                )
                            }
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar")
                    }
                    // ===== Menú de herramientas (Lápiz/Borrador, color, grosores) =====
                    var toolsExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { toolsExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Herramientas")
                    }
                    DropdownMenu(
                        expanded = toolsExpanded,
                        onDismissRequest = { toolsExpanded = false }
                    ) {
                        // Contenedor libre (no hace falta usar solo DropdownMenuItem)
                        Column(
                            modifier = Modifier
                                .widthIn(min = 260.dp, max = 320.dp)
                                .padding(12.dp)
                        ) {
                            Text("Herramientas de dibujo", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(8.dp))

                            // Toggle Lápiz / Borrador
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AssistChip(
                                    onClick = { eraserOn = false },
                                    label = { Text("Lápiz") },
                                    leadingIcon = { Icon(Icons.Default.Create, contentDescription = null) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (!eraserOn)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                                Spacer(Modifier.width(8.dp))
                                AssistChip(
                                    onClick = { eraserOn = true },
                                    label = { Text("Borrador") },
                                    leadingIcon = { Icon(Icons.Default.AutoFixHigh, contentDescription = null) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (eraserOn)
                                            MaterialTheme.colorScheme.secondaryContainer
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }

                            // Paleta (solo lápiz)
                            if (!eraserOn) {
                                Spacer(Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    palette.forEach { color ->
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .background(color, CircleShape)
                                                .border(
                                                    width = if (selectedColor == color) 3.dp else 1.dp,
                                                    color = if (selectedColor == color)
                                                        MaterialTheme.colorScheme.primary
                                                    else Color.LightGray,
                                                    shape = CircleShape
                                                )
                                                .clickable { selectedColor = color }
                                        )
                                    }
                                }
                            }

                            // Grosor lápiz
                            Spacer(Modifier.height(12.dp))
                            Text("Grosor lápiz", style = MaterialTheme.typography.labelLarge)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Slider(
                                    value = strokeWidth,
                                    onValueChange = { strokeWidth = it },
                                    valueRange = 1f..50f,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("${strokeWidth.toInt()} px")
                            }

                            // Grosor borrador
                            Spacer(Modifier.height(12.dp))
                            Text("Grosor borrador", style = MaterialTheme.typography.labelLarge)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Slider(
                                    value = eraserWidth,
                                    onValueChange = { eraserWidth = it },
                                    valueRange = 1f..80f, // borrador más ancho si quieres
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("${eraserWidth.toInt()} px")
                            }
                        }
                    }
                }
            )
        }

    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            // Nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del croquis") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
            // Lienzo
            Box(Modifier.fillMaxSize()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(eraserOn, selectedColor, strokeWidth, eraserWidth) {
                            // === GESTOS SIN SLOP ===
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                var downPoint = down.position
                                val startTime = android.os.SystemClock.uptimeMillis()
                                var pathLen = 0f

                                // Grosor activo según modo
                                val activeWidth = if (eraserOn) eraserWidth else strokeWidth

                                // Semilla dp-aware
                                val epsPx = with(density) { 0.75f.dp.toPx() }
                                current = listOf(downPoint, downPoint + Offset(epsPx, epsPx))

                                // (opcional) punto inmediato para “i”
                                run {
                                    val ink = if (eraserOn) Color.Transparent else selectedColor
                                    val dot = StrokePath(
                                        points = listOf(downPoint),
                                        color = ink,
                                        width = activeWidth,
                                        isEraser = eraserOn,
                                        isDot = true,
                                        dotRadius = max(2f, activeWidth / 2f)
                                    )
                                    paths = paths + dot
                                }

                                var last = downPoint
                                drag(down.id) { change ->
                                    val now = change.position
                                    val dx = now.x - last.x
                                    val dy = now.y - last.y
                                    pathLen += hypot(dx, dy)

                                    // Interpola si hay salto grande
                                    val dist = hypot(dx, dy)
                                    if (dist > 6f) {
                                        val steps = (dist / 6f).toInt().coerceAtMost(3)
                                        repeat(steps) { i ->
                                            val t = (i + 1f) / (steps + 1f)
                                            current = current + Offset(last.x + dx * t, last.y + dy * t)
                                        }
                                    }

                                    current = current + now
                                    last = now
                                    change.consume()
                                }

                                // Fin: decide tap vs stroke
                                val duration = android.os.SystemClock.uptimeMillis() - startTime
                                val isTinyPath = pathLen < 6f
                                val isQuick = duration < 180L
                                val isFewPoints = current.size <= 2
                                val isTap = isTinyPath && isQuick && isFewPoints

                                val ink = if (eraserOn) Color.Transparent else selectedColor
                                if (!isTap && current.isNotEmpty()) {
                                    paths = paths + StrokePath(
                                        points = current,
                                        color = ink,
                                        width = activeWidth, // ← grosor correcto según modo
                                        isEraser = eraserOn
                                    )
                                }
                                current = emptyList()
                            }
                        }
                ) {
                    // Registrar tamaño de canvas
                    canvasSize = IntSize(size.width.toInt(), size.height.toInt())

                    // Capa condicional para CLEAR
                    val needLayer = eraserOn || paths.any { it.isEraser }
                    val layerRect = androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height)
                    val paint = androidx.compose.ui.graphics.Paint()
                    val canvas = drawContext.canvas
                    if (needLayer) canvas.saveLayer(layerRect, paint)

                    // Fondo
                    background?.let { bg ->
                        drawImage(
                            image = bg,
                            dstSize = IntSize(size.width.toInt(), size.height.toInt())
                        )
                    } ?: run { drawRect(Color.Transparent) }

                    // Persistentes
                    paths.forEach { sp ->
                        val blend = if (sp.isEraser) BlendMode.Clear else BlendMode.SrcOver
                        val ink = if (sp.isEraser) Color.Transparent else sp.color
                        val pts = sp.points
                        when {
                            sp.isDot && pts.isNotEmpty() -> drawCircle(
                                color = ink, radius = sp.dotRadius, center = pts.first(), blendMode = blend
                            )
                            pts.size <= 1 -> if (pts.isNotEmpty()) drawCircle(
                                color = ink, radius = max(2f, sp.width / 2f), center = pts.first(), blendMode = blend
                            )
                            pts.size == 2 -> drawLine(
                                color = ink, start = pts[0], end = pts[1],
                                strokeWidth = sp.width, // ← usa el ancho guardado
                                blendMode = blend
                            )
                            else -> drawPath(
                                path = buildSmoothPath(pts),
                                color = ink,
                                style = Stroke(width = sp.width), // ← usa el ancho guardado
                                blendMode = blend
                            )
                        }
                    }

                    // En curso
                    if (current.isNotEmpty()) {
                        val blend = if (eraserOn) BlendMode.Clear else BlendMode.SrcOver
                        val ink = if (eraserOn) Color.Transparent else selectedColor
                        val activeWidth = if (eraserOn) eraserWidth else strokeWidth // ← ancho en curso
                        when (current.size) {
                            1 -> drawCircle(
                                color = ink,
                                radius = max(2f, activeWidth / 2f),
                                center = current.first(),
                                blendMode = blend
                            )
                            2 -> drawLine(
                                color = ink,
                                start = current[0],
                                end = current[1],
                                strokeWidth = activeWidth,
                                blendMode = blend
                            )
                            else -> drawPath(
                                path = buildSmoothPath(current),
                                color = ink,
                                style = Stroke(width = activeWidth),
                                blendMode = blend
                            )
                        }
                    }

                    if (needLayer) canvas.restore()
                }
            }

        }
    }
}
/*
fun CroquisDrawScreen(
    navController: NavController,
    medidaId: Int,
    croquisId: Int?,                   // null = nuevo, != null = editar (sobrescribe al guardar)
    viewModel: CroquisViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Estado del lienzo
    var nombre by remember { mutableStateOf("Croquis") }
    var paths by remember { mutableStateOf(listOf<StrokePath>()) }
    var redoStack by remember { mutableStateOf(listOf<StrokePath>()) }
    var current by remember { mutableStateOf(listOf<Offset>()) }
    var strokeWidth by remember { mutableStateOf(6f) }
    var eraserOn by remember { mutableStateOf(false) }

    // Paleta de colores
    val palette = listOf(
        Color.Black, Color.DarkGray, Color.Gray,
        Color(0xFFEF5350), // rojo
        Color(0xFFAB47BC), // morado
        Color(0xFF42A5F5), // azul
        Color(0xFF26A69A), // verde
        Color(0xFFFFCA28), // ámbar
        Color(0xFFFF8A65)  // naranja
    )
    var selectedColor by remember { mutableStateOf(Color.Black) }

    // Fondo si estamos editando
    var background by remember { mutableStateOf<ImageBitmap?>(null) }

    // Cargar croquis existente como fondo (edición)
    LaunchedEffect(croquisId) {
        if (croquisId != null) {
            val c = viewModel.getById(croquisId)
            if (c != null) {
                nombre = c.nombre
                runCatching {
                    BitmapFactory.decodeFile(c.filePath)?.let { bmp ->
                        background = bmp.asImageBitmap()
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                title = { Text(if (croquisId == null) "Nuevo croquis" else "Editar croquis") },
                actions = {
                    IconButton(onClick = {
                        // Limpiar trazos (mantiene el fondo si existe)
                        paths = emptyList()
                        redoStack = emptyList()
                        current = emptyList()
                    }) { Icon(Icons.Default.Delete, contentDescription = "Limpiar") }

                    IconButton(onClick = {
                        scope.launch {
                            val bitmap = renderToBitmapWithBackground(
                                background = background,
                                paths = paths,
                                current = current
                            )
                            if (croquisId == null) {
                                viewModel.saveNewCroquis(
                                    context = context,
                                    medidaId = medidaId,
                                    nombre = if (nombre.isBlank()) "Croquis ${max(1, paths.size)}" else nombre,
                                    bitmap = bitmap
                                )
                            } else {
                                viewModel.overwriteCroquis(
                                    context = context,
                                    croquisId = croquisId,
                                    bitmap = bitmap
                                )
                            }
                            navController.popBackStack()
                        }
                    }) { Icon(Icons.Default.Save, contentDescription = "Guardar") }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            // Nombre del croquis
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del croquis") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )

            // Controles superiores (paleta, borrador, grosor, deshacer/rehacer)
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Paleta + Borrador
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    palette.forEach { color ->
                        val selected = color == selectedColor && !eraserOn
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(color, CircleShape)
                                .then(
                                    if (selected) Modifier.border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    ) else Modifier.border(
                                        width = 1.dp,
                                        color = Color.LightGray,
                                        shape = CircleShape
                                    )
                                )
                                .padding(0.dp)
                                .noRippleClickable {
                                    eraserOn = false
                                    selectedColor = color
                                }
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    AssistChip(
                        onClick = { eraserOn = !eraserOn },
                        label = { Text(if (eraserOn) "Borrador (ON)" else "Borrador") },
                        leadingIcon = {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (eraserOn)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Grosor + Deshacer/Rehacer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Grosor", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(12.dp))
                    Slider(
                        value = strokeWidth,
                        onValueChange = { strokeWidth = it },
                        valueRange = 1f..30f,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("${strokeWidth.toInt()} px")
                }

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        enabled = paths.isNotEmpty(),
                        onClick = {
                            if (paths.isNotEmpty()) {
                                val last = paths.last()
                                paths = paths.dropLast(1)
                                redoStack = redoStack + last
                            }
                        }
                    ) { Text("Deshacer") }

                    OutlinedButton(
                        enabled = redoStack.isNotEmpty(),
                        onClick = {
                            if (redoStack.isNotEmpty()) {
                                val last = redoStack.last()
                                redoStack = redoStack.dropLast(1)
                                paths = paths + last
                            }
                        }
                    ) { Text("Rehacer") }
                }
            }

            // Lienzo de dibujo
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    //.background(MaterialTheme.colorScheme.surface)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(eraserOn, selectedColor, strokeWidth) {
                            // Gestos sin "slop": capturamos desde el primer DOWN
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                var downPoint = down.position
                                val startTime = android.os.SystemClock.uptimeMillis()
                                var pathLen = 0f

                                // Semilla dp-aware para que se vea el primer toque
                                val eps = 0.75f * density // ~0.75dp en px
                                current = listOf(downPoint, downPoint + Offset(eps, eps))

                                // (opcional) punto inmediato para puntear la "i"
                                // Si no lo quieres, comenta este bloque
                                run {
                                    val ink = if (eraserOn) Color.Transparent else selectedColor
                                    val dot = StrokePath(
                                        points = listOf(downPoint),
                                        color = ink,
                                        width = strokeWidth,
                                        isEraser = eraserOn,
                                        isDot = true,
                                        dotRadius = max(2f, strokeWidth / 2f)
                                    )
                                    paths = paths + dot
                                }

                                // Arranca con la posición actual
                                var last = downPoint

                                // Arrastrar sin slop
                                drag(down.id) { change ->
                                    val now = change.position
                                    // acumula longitud para distinguir TAP vs trazo
                                    pathLen += hypot(now.x - last.x, now.y - last.y)

                                    // interpolación opcional si el salto es grande (mejora precisión visual)
                                    val dx = now.x - last.x
                                    val dy = now.y - last.y
                                    val dist = hypot(dx, dy)
                                    if (dist > 6f) {
                                        // mete 1-2 puntos intermedios
                                        val steps = (dist / 6f).toInt().coerceAtMost(3)
                                        repeat(steps) { i ->
                                            val t = (i + 1f) / (steps + 1f)
                                            current = current + Offset(last.x + dx * t, last.y + dy * t)
                                        }
                                    }

                                    current = current + now
                                    last = now
                                    change.consume()
                                }

                                // Fin del gesto: decide punto o trazo
                                val duration = android.os.SystemClock.uptimeMillis() - startTime
                                val isTinyPath = pathLen < 6f
                                val isQuick = duration < 180L
                                val isFewPoints = current.size <= 2
                                val isTap = isTinyPath && isQuick && isFewPoints

                                val ink = if (eraserOn) Color.Transparent else selectedColor
                                if (isTap) {
                                    // ya añadimos el punto al principio, así que no hacemos nada más
                                } else if (current.isNotEmpty()) {
                                    paths = paths + StrokePath(
                                        points = current,
                                        color = ink,
                                        width = strokeWidth,
                                        isEraser = eraserOn
                                    )
                                }

                                current = emptyList()
                            }
                        }
                ) {
                    val layerRect = androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height)
                    val paint = androidx.compose.ui.graphics.Paint()
                    val canvas = drawContext.canvas
                    canvas.saveLayer(layerRect, paint)

                    // 1) Fondo (transparente si no hay imagen)
                    background?.let { bg ->
                        drawImage(
                            image = bg,
                            dstSize = IntSize(size.width.toInt(), size.height.toInt())
                        )
                    } ?: run {
                        drawRect(Color.Transparent)
                    }

                    // 2) Trazos persistentes
                    paths.forEach { sp ->
                        val blend = if (sp.isEraser) BlendMode.Clear else BlendMode.SrcOver
                        val ink   = if (sp.isEraser) Color.Transparent else sp.color

                        if (sp.isDot && sp.points.isNotEmpty()) {
                            drawCircle(
                                color = ink,
                                radius = sp.dotRadius,
                                center = sp.points.first(),
                                blendMode = blend
                            )
                        } else {
                            val pts = sp.points
                            when (pts.size) {
                                0 -> Unit
                                1 -> drawCircle(
                                    color = ink,
                                    radius = max(2f, sp.width / 2f),
                                    center = pts.first(),
                                    blendMode = blend
                                )
                                2 -> drawLine(
                                    color = ink,
                                    start = pts[0],
                                    end = pts[1],
                                    strokeWidth = sp.width,
                                    blendMode = blend
                                )
                                else -> drawPath(
                                    path = buildSmoothPath(pts),
                                    color = ink,
                                    style = Stroke(width = sp.width),
                                    blendMode = blend
                                )
                            }
                        }
                    }

                    // 3) Trazo en curso
                    if (current.isNotEmpty()) {
                        val blend = if (eraserOn) BlendMode.Clear else BlendMode.SrcOver
                        val ink   = if (eraserOn) Color.Transparent else selectedColor

                        when (current.size) {
                            1 -> drawCircle(
                                color = ink,
                                radius = max(2f, strokeWidth / 2f),
                                center = current.first(),
                                blendMode = blend
                            )
                            2 -> drawLine(
                                color = ink,
                                start = current[0],
                                end = current[1],
                                strokeWidth = strokeWidth,
                                blendMode = blend
                            )
                            else -> drawPath(
                                path = buildSmoothPath(current),
                                color = ink,
                                style = Stroke(width = strokeWidth),
                                blendMode = blend
                            )
                        }
                    }

                    // ✅ aplica la capa (CLEAR surte efecto)
                    canvas.restore()
                }
            }
        }
    }
}
*/

/** Modifier helper para click sin ripple (para los swatches de color) */
@Composable
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier =
    composed {
        this.then(
            Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
        )
    }

private fun distance(a: Offset?, b: Offset?): Float {
    if (a == null || b == null) return Float.MAX_VALUE
    return hypot(a.x - b.x, a.y - b.y)
}

/** Path suavizado con Bezier cuadráticas entre puntos medios. */
private fun buildSmoothPath(points: List<Offset>): Path {
    val p = Path()
    val n = points.size
    if (n == 0) return p
    if (n == 1) {
        p.moveTo(points[0].x, points[0].y); return p
    }
    if (n == 2) {
        p.moveTo(points[0].x, points[0].y); p.lineTo(points[1].x, points[1].y); return p
    }

    p.moveTo(points[0].x, points[0].y)
    p.lineTo(points[1].x, points[1].y)
    for (i in 1 until n - 1) {
        val c = points[i]
        val n1 = points[i + 1]
        val midX = (c.x + n1.x) / 2f
        val midY = (c.y + n1.y) / 2f
        p.quadraticBezierTo(c.x, c.y, midX, midY)
    }
    return p
}

/**
 * Renderiza a bitmap con fondo opcional y soportando borrador transparente.
 * Si quieres fondo blanco cuando no hay imagen, cambia drawColor a WHITE.
 */
private fun renderToBitmapWithBackground(
    background: ImageBitmap?,
    paths: List<StrokePath>,
    current: List<Offset>,
    outSize: IntSize
): Bitmap {
    val width = outSize.width.coerceAtLeast(1)
    val height = outSize.height.coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)


    if (background != null) {
        val bgBmp = background.asAndroidBitmap()
        val src = android.graphics.Rect(0, 0, bgBmp.width, bgBmp.height)
        val dst = android.graphics.Rect(0, 0, width, height)
        canvas.drawBitmap(bgBmp, src, dst, null)
    } else {
        // Fondo transparente para respetar CLEAR
        canvas.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    }

    // Pinturas para stroke y fill (con CLEAR en modo borrador)
    val strokePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        style = AndroidPaint.Style.STROKE
        strokeCap = AndroidPaint.Cap.ROUND
        strokeJoin = AndroidPaint.Join.ROUND
    }
    val fillPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        style = AndroidPaint.Style.FILL
    }

    fun drawDot(center: Offset, radius: Float, color: Color, isEraser: Boolean) {
        if (isEraser) {
            fillPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        } else {
            fillPaint.xfermode = null
            fillPaint.color = color.toArgb()
        }
        canvas.drawCircle(center.x, center.y, radius, fillPaint)
    }

    fun drawSmooth(points: List<Offset>, width: Float, color: Color, isEraser: Boolean) {
        strokePaint.strokeWidth = width
        if (isEraser) {
            strokePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        } else {
            strokePaint.xfermode = null
            strokePaint.color = color.toArgb()
        }

        if (points.size < 3) {
            // líneas simples
            for (i in 1 until points.size) {
                val a = points[i - 1]
                val b = points[i]
                canvas.drawLine(a.x, a.y, b.x, b.y, strokePaint)
            }
            return
        }

        for (i in 1 until points.size - 1) {
            val prev = points[i - 1]
            val curr = points[i]
            val next = points[i + 1]
            val mid1x = (prev.x + curr.x) / 2f
            val mid1y = (prev.y + curr.y) / 2f
            val mid2x = (curr.x + next.x) / 2f
            val mid2y = (curr.y + next.y) / 2f

            val path = AndroidPath().apply {
                moveTo(mid1x, mid1y)
                quadTo(curr.x, curr.y, mid2x, mid2y)
            }
            canvas.drawPath(path, strokePaint)
        }
    }

    // Trazos persistentes
    paths.forEach { sp ->
        if (sp.isDot && sp.points.isNotEmpty()) {
            drawDot(sp.points.first(), sp.dotRadius, sp.color, sp.isEraser)
        } else {
            val pts = sp.points
            if (pts.size == 1) {
                drawDot(pts.first(), max(2f, sp.width / 2f), sp.color, sp.isEraser)
            } else {
                drawSmooth(pts, sp.width, sp.color, sp.isEraser)
            }
        }
    }

    // (Opcional) Trazo en curso no se suele incluir en export
    return bitmap
}