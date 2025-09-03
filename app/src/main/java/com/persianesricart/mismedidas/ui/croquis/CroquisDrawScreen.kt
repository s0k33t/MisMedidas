package com.persianesricart.mismedidas.ui.croquis

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.graphics.drawscope.drawImage
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.persianesricart.mismedidas.viewmodel.CroquisViewModel
import kotlinx.coroutines.launch

private data class StrokePath(
    val points: List<Offset>,
    val color: Color,
    val width: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CroquisDrawScreen(
    navController: NavController,
    medidaId: Int,
    croquisId: Int?,                   // null = nuevo, != null = editar (sobrescribe al guardar)
    croquisVM: CroquisViewModel,
    croquisNombreInicial: String = "Croquis"
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estado del lienzo
    var nombre by remember { mutableStateOf(croquisNombreInicial) }
    var paths by remember { mutableStateOf(listOf<StrokePath>()) }
    var current by remember { mutableStateOf(listOf<Offset>()) }
    var strokeWidth by remember { mutableStateOf(6f) }
    var strokeColor by remember { mutableStateOf(Color.Black) }

    // Fondo si estamos editando
    var background by remember { mutableStateOf<ImageBitmap?>(null) }

    // Cargar croquis existente como fondo (edición)
    LaunchedEffect(croquisId) {
        if (croquisId != null) {
            val c = croquisVM.getById(croquisId)
            if (c != null) {
                nombre = c.nombre
                runCatching {
                    val bmp = BitmapFactory.decodeFile(c.filePath)
                    if (bmp != null) background = bmp.asImageBitmap()
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
                title = {
                    Text(if (croquisId == null) "Nuevo croquis" else "Editar croquis")
                },
                actions = {
                    IconButton(onClick = {
                        // Limpiar trazos (mantiene el fondo si existe)
                        paths = emptyList()
                        current = emptyList()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Limpiar")
                    }

                    IconButton(onClick = {
                        scope.launch {
                            val bitmap = renderToBitmapWithBackground(
                                background = background,
                                paths = paths,
                                current = current
                            )
                            if (croquisId == null) {
                                // Crear nuevo
                                croquisVM.saveNewCroquis(
                                    context = context,
                                    medidaId = medidaId,
                                    nombre = nombreAuto(paths, nombre),
                                    bitmap = bitmap
                                )
                            } else {
                                // Sobrescribir existente
                                croquisVM.overwriteCroquis(
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
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del croquis") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    current = listOf(offset)
                                },
                                onDrag = { change, _ ->
                                    current = current + change.position
                                },
                                onDragEnd = {
                                    if (current.isNotEmpty()) {
                                        paths = paths + StrokePath(current, strokeColor, strokeWidth)
                                        current = emptyList()
                                    }
                                }
                            )
                        }
                ) {
                    // 1) Fondo si existe (escalado a todo el lienzo)
                    background?.let { bg ->
                        drawImage(
                            image = bg,
                            dstSize = IntSize(size.width.toInt(), size.height.toInt())
                        )
                    } ?: run {
                        // Si no hay fondo, blanco plano (Canvas ya tiene surface, pero aseguramos)
                        drawRect(Color.White)
                    }

                    // 2) Trazos persistentes
                    paths.forEach { sp ->
                        drawPath(
                            path = sp.points.toPath(),
                            color = sp.color,
                            style = Stroke(width = sp.width)
                        )
                    }
                    // 3) Trazo en curso
                    if (current.isNotEmpty()) {
                        drawPath(
                            path = current.toPath(),
                            color = strokeColor,
                            style = Stroke(width = strokeWidth)
                        )
                    }
                }
            }
        }
    }
}

private fun List<Offset>.toPath(): Path {
    val p = Path()
    if (isNotEmpty()) {
        p.moveTo(first().x, first().y)
        for (i in 1 until size) {
            p.lineTo(this[i].x, this[i].y)
        }
    }
    return p
}

/**
 * Renderiza a bitmap, aplicando fondo (si lo hay) escalado a tamaño base.
 * Tamaño base: si hay fondo, se usa su tamaño; si no, 1080x1920.
 */
private fun renderToBitmapWithBackground(
    background: ImageBitmap?,
    paths: List<StrokePath>,
    current: List<Offset>
): Bitmap {
    val width = background?.width ?: 1080
    val height = background?.height ?: 1920
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)

    if (background != null) {
        val bgBmp = background.asAndroidBitmap()
        // Pintamos el fondo escalándolo a la superficie de salida (width x height)
        val src = android.graphics.Rect(0, 0, bgBmp.width, bgBmp.height)
        val dst = android.graphics.Rect(0, 0, width, height)
        canvas.drawBitmap(bgBmp, src, dst, null)
    } else {
        canvas.drawColor(android.graphics.Color.WHITE)
    }

    val paint = android.graphics.Paint().apply {
        style = android.graphics.Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
    }

    // Trazos ya cerrados
    for (sp in paths) {
        paint.color = sp.color.toArgb()
        paint.strokeWidth = sp.width
        val pts = sp.points
        for (i in 1 until pts.size) {
            canvas.drawLine(pts[i - 1].x, pts[i - 1].y, pts[i].x, pts[i].y, paint)
        }
    }

    // Trazo en curso (por si el usuario guarda mientras dibuja)
    paint.color = android.graphics.Color.BLACK
    paint.strokeWidth = 6f
    for (i in 1 until current.size) {
        canvas.drawLine(current[i - 1].x, current[i - 1].y, current[i].x, current[i].y, paint)
    }

    return bitmap
}

private fun nombreAuto(paths: List<StrokePath>, base: String): String {
    return if (base.isBlank()) "Croquis ${kotlin.math.max(1, paths.size)}" else base
}

/*

package com.persianesricart.mismedidas.ui.croquis

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.persianesricart.mismedidas.viewmodel.CroquisViewModel
import kotlinx.coroutines.launch

private data class StrokePath(val points: List<Offset>, val color: Color, val width: Float)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CroquisDrawScreen(
    navController: NavController,
    medidaId: Int,
    croquisId: Int?,
    croquisVM: CroquisViewModel,
    croquisNombreInicial: String = "Croquis"
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf(croquisNombreInicial) }
    var paths by remember { mutableStateOf(listOf<StrokePath>()) }
    var current by remember { mutableStateOf(listOf<Offset>()) }
    var background by remember { mutableStateOf<ImageBitmap?>(null)}
    var strokeWidth by remember { mutableStateOf(6f) }
    var strokeColor by remember { mutableStateOf(Color.Black) }

    LaunchedEffect(croquisId) {
        if (croquisId != null) {
            val c = croquisVM.daoGetById(croquisId) // añade un wrapper suspend en VM si lo prefieres
            if (c != null) {
                nombre = c.nombre
                val bmp = android.graphics.BitmapFactory.decodeFile(c.filePath)
                background = bmp.asImageBitmap()
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
                        // limpiar
                        paths = emptyList()
                        current = emptyList()
                    }) { Icon(Icons.Default.Delete, contentDescription = "Limpiar") }

                    IconButton(onClick = {
                        scope.launch {
                            // convertir a bitmap y guardar
                            val bitmap = renderToBitmap(paths, current, background)
                            if (croquisId == null) {
                                croquisVM.saveNewCroquis(context, medidaId, nombreAuto(paths, nombre), bitmap)
                            } else {
                                // update: buscamos croquis por id y sobrescribimos
                                // (lo sencillo: volvemos a Room a por el croquis y sobrescribimos su file)
                                // para no alargar: guardamos como si fuera nuevo y luego podrías borrar el antiguo
                                //croquisVM.saveNewCroquis(context, medidaId, nombreAuto(paths, nombre), bitmap)
                                croquisVM.overwriteCroquis(context, croquisId, bitmap)

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
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del croquis") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )

            // Área de dibujo
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    current = listOf(offset)
                                },
                                onDrag = { change, _ ->
                                    current = current + change.position
                                },
                                onDragEnd = {
                                    if (current.isNotEmpty()) {
                                        paths = paths + StrokePath(current, strokeColor, strokeWidth)
                                        current = emptyList()
                                    }
                                }
                            )
                        }
                ) {
                    background?.let { img -> drawImage(img)

                    // dibujar trazos anteriores
                    paths.forEach { sp ->
                        drawPath(
                            path = sp.points.toPath(),
                            color = sp.color,
                            style = Stroke(width = sp.width)
                        )
                    }
                    // dibujar trazo actual
                    if (current.isNotEmpty()) {
                        drawPath(
                            path = current.toPath(),
                            color = strokeColor,
                            style = Stroke(width = strokeWidth)
                        )
                    }
                }
            }
        }
    }
}


private fun List<Offset>.toPath(): Path {
    val p = Path()
    if (isNotEmpty()) {
        p.moveTo(first().x, first().y)
        for (i in 1 until size) {
            p.lineTo(this[i].x, this[i].y)
        }
    }
    return p
}

private fun renderToBitmap(paths: List<StrokePath>, current: List<Offset>): Bitmap {
    // tamaño fijo razonable; si quieres real size, mide el Canvas con onSizeChanged
    val width = 1080
    val height = 1920
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    val paint = android.graphics.Paint().apply {
        style = android.graphics.Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
    }
    // fondo blanco
    canvas.drawColor(android.graphics.Color.WHITE)
    // dibuja cada trazo
    for (sp in paths) {
        paint.color = sp.color.toArgb()
        paint.strokeWidth = sp.width
        val pts = sp.points
        for (i in 1 until pts.size) {
            canvas.drawLine(pts[i-1].x, pts[i-1].y, pts[i].x, pts[i].y, paint)
        }
    }
    // trazo actual (si existe)
    paint.color = android.graphics.Color.BLACK
    paint.strokeWidth = 6f
    for (i in 1 until current.size) {
        canvas.drawLine(current[i-1].x, current[i-1].y, current[i].x, current[i].y, paint)
    }
    return bitmap
}

private fun nombreAuto(paths: List<StrokePath>, base: String): String {
    // si el usuario no escribe, generamos algo tipo "Croquis 1"
    return if (base.isBlank()) {
        "Croquis ${kotlin.math.max(1, paths.size)}"
    } else base
}
*/