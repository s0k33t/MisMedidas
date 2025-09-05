package com.persianesricart.mismedidas.data.backup

import android.content.Context
import android.net.Uri
import com.persianesricart.mismedidas.data.AppDatabase
import com.persianesricart.mismedidas.data.entities.Croquis
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

suspend fun exportCroquisToZip(context: Context, destUri: Uri) {
    val db = AppDatabase.getInstance(context)
    val croquisDao = db.croquisDao()
    val medidaDao = db.medidaDao()

    val croquisList: List<Croquis> = croquisDao.getAll() // crea getAll() en tu CroquisDao si no existe

    // Construye JSON
    val metaArray = JSONArray()
    val files = mutableListOf<File>()
    for (c in croquisList) {
        val medida = medidaDao.getById(c.medidaId) ?: continue
        val file = File(c.filePath)
        if (!file.exists()) continue

        val obj = JSONObject().apply {
            put("nombre", c.nombre)
            put("fileName", file.name)
            put("medidaUuid", medida.uuid)
            put("createdAt", c.createdAt)
            put("updatedAt", c.updatedAt)
        }
        metaArray.put(obj)
        files.add(file)
    }

    context.contentResolver.openOutputStream(destUri)?.use { out ->
        val zip = ZipOutputStream(BufferedOutputStream(out))

        // 1) croquis.json
        val metaEntry = ZipEntry("croquis.json")
        zip.putNextEntry(metaEntry)
        val metaBytes = metaArray.toString().toByteArray(Charsets.UTF_8)
        zip.write(metaBytes)
        zip.closeEntry()

        // 2) imágenes
        for (f in files) {
            val entry = ZipEntry("img/${f.name}")
            zip.putNextEntry(entry)
            f.inputStream().use { it.copyTo(zip) }
            zip.closeEntry()
        }

        zip.finish()
        zip.close()
    }
}

suspend fun importCroquisFromZip(context: Context, sourceUri: Uri): Pair<Int, Int> {
    val db = AppDatabase.getInstance(context)
    val medidaDao = db.medidaDao()
    val croquisDao = db.croquisDao()

    // Descomprimir en memoria: primero leer croquis.json y mapear entradas
    val tmpDir = File(context.cacheDir, "croquis_import_tmp").apply { mkdirs() }
    var metaJson: String? = null

    context.contentResolver.openInputStream(sourceUri)?.use { input ->
        val zip = ZipInputStream(BufferedInputStream(input))
        var entry: ZipEntry? = zip.nextEntry
        while (entry != null) {
            if (!entry.isDirectory) {
                when (entry.name) {
                    "croquis.json" -> {
                        metaJson = zip.readBytes().toString(Charsets.UTF_8)
                    }
                    else -> {
                        if (entry.name.startsWith("img/")) {
                            val fileName = entry.name.substringAfter("img/")
                            val dst = File(tmpDir, fileName)
                            dst.outputStream().use { zip.copyTo(it) }
                        }
                    }
                }
            }
            zip.closeEntry()
            entry = zip.nextEntry
        }
        zip.close()
    }

    if (metaJson.isNullOrBlank()) return 0 to 0

    val arr = JSONArray(metaJson)
    var imported = 0
    var skipped = 0
    val croquisDir = File(context.filesDir, "croquis").apply { mkdirs() }

    for (i in 0 until arr.length()) {
        val obj = arr.getJSONObject(i)
        val nombre = obj.getString("nombre")
        val fileName = obj.getString("fileName")
        val medidaUuid = obj.getString("medidaUuid")
        val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
        val updatedAt = obj.optLong("updatedAt", createdAt)

        val medida = medidaDao.getByUuid(medidaUuid)
        if (medida == null) {
            skipped++
            continue
        }

        val src = File(tmpDir, fileName)
        if (!src.exists()) {
            skipped++
            continue
        }

        // Copiamos PNG a directorio definitivo
        val dst = File(croquisDir, fileName)
        src.copyTo(dst, overwrite = true)

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

    // limpiar tmp
    tmpDir.deleteRecursively()

    return imported to skipped
}