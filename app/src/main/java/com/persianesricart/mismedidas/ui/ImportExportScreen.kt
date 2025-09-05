package com.persianesricart.mismedidas.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.persianesricart.mismedidas.viewmodel.MainViewModel
import com.persianesricart.mismedidas.viewmodel.ajustes.AjustesViewModel
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.text.DecimalFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import com.persianesricart.mismedidas.data.AppDatabase
import com.persianesricart.mismedidas.data.entities.Croquis
import java.io.BufferedInputStream
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExportScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    ajustesViewModel: AjustesViewModel,
    exportNotasLauncher: ActivityResultLauncher<Intent>,
    importNotasLauncher: ActivityResultLauncher<Intent>,
    exportAjustesLauncher: ActivityResultLauncher<Intent>,
    importAjustesLauncher: ActivityResultLauncher<Intent>,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()

    // ---- Info Notas/Ajustes (informativo) ----
    val notasDbInfo = remember {
        val f = context.getDatabasePath("mis_medidas.db")
        DbInfo(
            path = f.absolutePath,
            size = fileSizeOrZero(f)
        )
    }
    val ajustesDbInfo = remember {
        val f = context.getDatabasePath("ajustes.db")
        DbInfo(
            path = f.absolutePath,
            size = fileSizeOrZero(f)
        )
    }

    // ---- CROQUIS: stats y launchers SAF ----
    val croquisDir = remember { File(context.filesDir, "croquis") }
    var croquisCount by remember { mutableStateOf(countFilesRecursive(croquisDir)) }
    var croquisBytes by remember { mutableStateOf(folderSize(croquisDir)) }

    // Launchers CROQUIS (SAF)
    val croquisExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.data?.let { uri ->
            scope.launch {
                runCatching {
                    exportCroquisToZip(context, uri)
                    showToast(context, "Croquis exportados")
                }.onFailure {
                    showToast(context, "Error exportando croquis: ${it.message}")
                }
            }
        }
    }

    val croquisImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.data?.let { uri ->
            scope.launch {
                runCatching {
                    val (ok, skipped) = importCroquisFromZip(context, uri)
                    // refresca métricas
                    croquisCount = countFilesRecursive(croquisDir)
                    croquisBytes = folderSize(croquisDir)
                    showToast(context, "Croquis importados: $ok, omitidos: $skipped")
                }.onFailure {
                    showToast(context, "Error importando croquis: ${it.message}")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Importación / Exportación") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(scroll)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ================== NOTAS ==================
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Notas", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Copia/restauración de la base de datos de notas.\n" +
                                "Al importar, la app se reiniciará para recargar datos.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Ruta actual:\n${notasDbInfo.path}\n" +
                                "Tamaño: ${formatSize(notasDbInfo.size)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Divider(Modifier.padding(vertical = 8.dp))

                    // Diagnóstico rápido (Notas)
                    val notasInfo by remember {
                        mutableStateOf(mutableStateOf(""))
                    }
                    LaunchedEffect(Unit) {
                        notasInfo.value = infoNotas(context, mainViewModel)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            notasInfo.value = infoNotas(context, mainViewModel)
                        }) { Text("Refrescar info") }
                        Button(onClick = {
                            mainViewModel.cargarNotas()
                            android.widget.Toast
                                .makeText(context, "Notas recargadas en memoria", android.widget.Toast.LENGTH_SHORT)
                                .show()
                        }) { Text("Cargar notas ahora") }
                    }

                    Divider(Modifier.padding(vertical = 8.dp))
                    Text(notasInfo.value, style = MaterialTheme.typography.bodySmall)

                    Button(
                        onClick = {
                            mainViewModel.exportarBaseDeDatosSAF(context, exportNotasLauncher)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Exportar base de datos de notas") }

                    Button(
                        onClick = {
                            mainViewModel.importarBaseDeDatosSAF(context, importNotasLauncher)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Importar base de datos de notas") }
                }
            }

            // ================== AJUSTES ==================
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Ajustes", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Copia/restauración de la base de datos de ajustes (Tipos, Modelos, Acabados y Colores).\n" +
                                "Al importar, la app se reiniciará para recargar datos.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Ruta actual:\n${ajustesDbInfo.path}\n" +
                                "Tamaño: ${formatSize(ajustesDbInfo.size)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Divider(Modifier.padding(vertical = 8.dp))

                    Button(
                        onClick = {
                            ajustesViewModel.exportarAjustesSAF(context, exportAjustesLauncher)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Exportar base de datos de ajustes") }

                    Button(
                        onClick = {
                            ajustesViewModel.importarAjustesSAF(importAjustesLauncher)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Importar base de datos de ajustes") }
                }
            }

            // ================== CROQUIS (ZIP con croquis.json + /img) ==================
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Croquis", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Exporta/Importa dibujos (PNG) + metadatos.\n" +
                                "El ZIP incluye /img y un fichero croquis.json que enlaza por medidaUuid.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Carpeta local: ${croquisDir.absolutePath}\n" +
                                "Ficheros: $croquisCount    Tamaño: ${formatSize(croquisBytes)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Divider(Modifier.padding(vertical = 8.dp))

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "application/zip"
                                putExtra(Intent.EXTRA_TITLE, "CroquisBackup.zip")
                            }
                            croquisExportLauncher.launch(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Exportar croquis (.zip)") }

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "application/zip"
                            }
                            croquisImportLauncher.launch(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Importar croquis (.zip)") }
                }
            }
        }
    }
}

/* ------------------ Helpers visuales de tamaño ------------------- */
private data class DbInfo(
    val path: String,
    val size: Long,
)

private fun fileSizeOrZero(f: File): Long =
    runCatching { if (f.exists()) f.length() else 0L }.getOrDefault(0L)

private fun folderSize(dir: File): Long =
    if (!dir.exists()) 0L
    else dir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()

private fun countFilesRecursive(dir: File): Int =
    if (!dir.exists()) 0
    else dir.walkTopDown().count { it.isFile }

private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(bytes / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
}

private fun showToast(context: android.content.Context, msg: String) {
    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
}

/* ------------------ CROQUIS export/import (uuid-based) ------------------- */

/*
 * Exporta ZIP con:
 * - croquis.json en raíz, con nombre, fileName, medidaUuid, createdAt, updatedAt
 * - /img/*.png con los ficheros
*/*/
private suspend fun exportCroquisToZip(context: android.content.Context, destUri: Uri) {
    val db = AppDatabase.getInstance(context)
    val croquisDao = db.croquisDao()
    val medidaDao = db.medidaDao()

    val croquisList: List<Croquis> = croquisDao.getAll() // necesitas este getAll() en CroquisDao
    val metas = JSONArray()
    val files = mutableListOf<File>()

    for (c in croquisList) {
        val medida = medidaDao.getById(c.medidaId) ?: continue
        val file = File(c.filePath)
        if (!file.exists()) continue

        val obj = JSONObject().apply {
            put("nombre", c.nombre)
            put("fileName", file.name)
            // Compatibilidad + futuro:
            put("medidaId", c.medidaId)          // legacy
            put("medidaUuid", medida.uuid ?: "") // nuevo (estable). Si es null, lo dejaremos vacío.
            put("createdAt", c.createdAt)
            put("updatedAt", c.updatedAt)
        }
        metas.put(obj)
        files.add(file)
    }

    context.contentResolver.openOutputStream(destUri)?.use { out ->
        ZipOutputStream(BufferedOutputStream(out)).use { zip ->
            // 1) croquis.json
            zip.putNextEntry(ZipEntry("croquis.json"))
            zip.write(metas.toString().toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // 2) imágenes /img
            for (f in files) {
                zip.putNextEntry(ZipEntry("img/${f.name}"))
                f.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }
}

/*
 * Importa ZIP con:
 * - croquis.json en raíz
 * - /img/*.png
 * Reconstruye rutas en filesDir/croquis y repuebla tabla Croquis.
*/*/
private suspend fun importCroquisFromZip(
    context: android.content.Context,
    sourceUri: Uri
): Pair<Int, Int> {
    val db = AppDatabase.getInstance(context)
    val medidaDao = db.medidaDao()
    val croquisDao = db.croquisDao()

    // 0) Preparar carpetas
    val tmpDir = File(context.cacheDir, "croquis_import_tmp").apply { mkdirs() }
    val croquisDir = File(context.filesDir, "croquis")

    // 1) Extraer: leer croquis.json y copiar img/* a tmp
    var metaJson: String? = null
    context.contentResolver.openInputStream(sourceUri)?.use { input ->
        ZipInputStream(BufferedInputStream(input)).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            val buf = ByteArray(8 * 1024)
            while (entry != null) {
                if (!entry.isDirectory) {
                    when {
                        entry.name == "croquis.json" -> {
                            val baos = ByteArrayOutputStream()
                            var n: Int
                            while (zis.read(buf).also { n = it } != -1) {
                                baos.write(buf, 0, n)
                            }
                            metaJson = baos.toString(Charsets.UTF_8.name())
                            zis.closeEntry()
                        }
                        entry.name.startsWith("img/") -> {
                            val fileName = entry.name.substringAfter("img/")
                            val dst = File(tmpDir, fileName)
                            dst.parentFile?.mkdirs()
                            FileOutputStream(dst).use { fos ->
                                var n: Int
                                while (zis.read(buf).also { n = it } != -1) {
                                    fos.write(buf, 0, n)
                                }
                            }
                            zis.closeEntry()
                        }
                        else -> zis.closeEntry()
                    }
                } else {
                    zis.closeEntry()
                }
                entry = zis.nextEntry
            }
        }
    }

    if (metaJson.isNullOrBlank()) {
        tmpDir.deleteRecursively()
        return 0 to 0
    }

    // 2) REEMPLAZAR todo: borrar tabla y limpiar carpeta destino
    croquisDao.deleteAll()
    if (croquisDir.exists()) croquisDir.deleteRecursively()
    croquisDir.mkdirs()

    // 3) Reconstruir desde JSON
    val arr = JSONArray(metaJson)
    var imported = 0
    var skipped = 0
    for (i in 0 until arr.length()) {
        val obj = arr.getJSONObject(i)
        val nombre = obj.optString("nombre")
        val fileName = obj.optString("fileName")
        val medidaUuid = obj.optString("medidaUuid") // ← tu export ya lo genera así
        val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
        val updatedAt = obj.optLong("updatedAt", createdAt)

        // Resolver medida por UUID
        val medida = medidaDao.getByUuid(medidaUuid)
        if (medida == null) {
            skipped++
            continue
        }

        // Mover el PNG desde tmp a destino
        val src = File(tmpDir, fileName)
        if (!src.exists()) {
            skipped++
            continue
        }
        val dst = File(croquisDir, fileName.ifBlank { java.util.UUID.randomUUID().toString() + ".png" })
        src.copyTo(dst, overwrite = true)

        // Insertar croquis
        croquisDao.insert(
            Croquis(
                id = 0,
                medidaId = medida.id,
                nombre = nombre,
                filePath = dst.absolutePath,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        )
        imported++
    }

    // 4) Limpieza tmp
    tmpDir.deleteRecursively()
    return imported to skipped
}

/* ------------------ ZIP helpers (genéricos) ------------------- */
private fun zipDirectory(sourceDir: File, zos: ZipOutputStream, prefixLen: Int) {
    if (!sourceDir.exists()) return
    sourceDir.walkTopDown().forEach { file ->
        val name = file.absolutePath
        val entryName = name.drop(prefixLen).replace('\\', '/')
        if (file.isDirectory) {
            if (entryName.isNotEmpty()) {
                val dirEntry = if (entryName.endsWith("/")) entryName else "$entryName/"
                zos.putNextEntry(ZipEntry(dirEntry))
                zos.closeEntry()
            }
        } else {
            FileInputStream(file).use { fis ->
                BufferedInputStream(fis).use { bis ->
                    val entry = ZipEntry(entryName)
                    zos.putNextEntry(entry)
                    bis.copyTo(zos, 8 * 1024)
                    zos.closeEntry()
                }
            }
        }
    }
}

private fun unzipToDirectory(zis: ZipInputStream, destDir: File) {
    destDir.mkdirs()
    var entry: ZipEntry? = zis.nextEntry
    val buffer = ByteArray(8 * 1024)
    while (entry != null) {
        val outFile = File(destDir, entry.name)
        if (entry.isDirectory) {
            outFile.mkdirs()
        } else {
            outFile.parentFile?.mkdirs()
            FileOutputStream(outFile).use { fos ->
                var count: Int
                while (zis.read(buffer).also { count = it } != -1) {
                    fos.write(buffer, 0, count)
                }
            }
        }
        zis.closeEntry()
        entry = zis.nextEntry
    }
    zis.close()
}

/* ------------------ Diagnóstico Notas ------------------- */
private fun infoNotas(context: android.content.Context, mainVM: MainViewModel): String {
    val f = context.getDatabasePath("mis_medidas.db")
    val size = if (f.exists()) f.length() else 0L
    val mod = if (f.exists()) java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        .format(java.util.Date(f.lastModified())) else "N/A"

    // leer recuento directamente con DAO (sincrónico con runBlocking para diagnóstico)
    val dao = AppDatabase.getInstance(context).notaDao()
    val count = kotlinx.coroutines.runBlocking { dao.getTodasLasNotas().size }

    return "Archivo: ${f.absolutePath}\n" +
            "Tamaño: ${size} bytes\n" +
            "Modificado: $mod\n" +
            "Notas en DB: $count"
}
