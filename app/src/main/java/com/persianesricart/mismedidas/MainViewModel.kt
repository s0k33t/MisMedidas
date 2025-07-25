/*
package com.persianesricart.mismedidas.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.persianesricart.mismedidas.data.dao.NotaDao
import com.persianesricart.mismedidas.data.entities.Nota
import kotlinx.coroutines.launch

class MainViewModel(private val dao: NotaDao) : ViewModel() {

    private val _notas = mutableStateListOf<Nota>()
    val notas: SnapshotStateList<Nota> get() = _notas


    fun cargarNotas() {

        viewModelScope.launch {
            _notas.clear()
            _notas.addAll(dao.getTodasLasNotas())
        }
    }

    fun eliminarNota(notaId: Int) {
        viewModelScope.launch {
            dao.deleteMedidasByNotaId(notaId)
            dao.deleteNotaById(notaId)
            cargarNotas()
        }
    }

    fun compartirNota(context: Context, nota: Nota) {
        viewModelScope.launch {
            val pdfFile = generatePdfFile(context, nota)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Nota de ${nota.cliente}")
                putExtra(Intent.EXTRA_TEXT, "Adjunto PDF con las medidas de ${nota.cliente}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Enviar nota por correo"))
        }
    }
}
*/

package com.persianesricart.mismedidas.viewmodel

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.persianesricart.mismedidas.data.AppDatabase
import com.persianesricart.mismedidas.data.dao.NotaDao
import com.persianesricart.mismedidas.data.entities.Nota
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainViewModel(private val dao: NotaDao) : ViewModel() {

    private val _notas = mutableStateListOf<Nota>()
    val notas: SnapshotStateList<Nota> get() = _notas

    fun cargarNotas() {
        viewModelScope.launch {
            _notas.clear()
            _notas.addAll(dao.getTodasLasNotas())
        }
    }

    fun eliminarNota(notaId: Int) {
        viewModelScope.launch {
            dao.deleteMedidasByNotaId(notaId)
            dao.deleteNotaById(notaId)
            cargarNotas()
        }
    }

    fun compartirNota(context: Context, nota: Nota) {
        viewModelScope.launch {
            val pdfFile = generatePdfFile(context, nota)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Nota de ${nota.cliente}")
                putExtra(Intent.EXTRA_TEXT, "Adjunto PDF con las medidas de ${nota.cliente}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Enviar nota por correo"))

            //delay(10000)
            //pdfFile.delete()
        }
    }


    private suspend fun generatePdfFile(context: Context, nota: Nota): File {
        val medidas = dao.getMedidasByNota(nota.id)

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint().apply { textSize = 14f }

        var y = 40

        // Encabezado
        canvas.drawText("Cliente: ${nota.cliente}  - Ref: ${nota.referencia}", 40f, y.toFloat(), paint)
        y += 20
        canvas.drawText("Dirección: ${nota.direccion}", 40f, y.toFloat(), paint)
        y += 20
        canvas.drawText("Población: ${nota.poblacion}", 40f, y.toFloat(), paint)
        y += 20
        canvas.drawText("Teléfono: ${nota.telefono} - Móvil: ${nota.movil}", 40f, y.toFloat(), paint)
        y += 20
        canvas.drawText("em@il: ${nota.email}", 40f, y.toFloat(), paint)
        y += 20
        canvas.drawText("Fecha: ${nota.fecha}", 40f, y.toFloat(), paint)
        y += 30
        canvas.drawText("Medidas:", 40f, y.toFloat(), paint)
        y += 20

        medidas.forEachIndexed { index, medida ->
            if (y > 800) {
                pdfDocument.finishPage(page)
                val newPage = pdfDocument.startPage(pageInfo)
                y = 60
            }
            canvas.drawText(
                "${medida.ud} - ${medida.ancho}x${medida.alto} - ${medida.tipo} en ${medida.modelo} ${medida.acabado} ${medida.color}",
                40f, y.toFloat(), paint
            )

            if (medida.luz) {
                y += 20
                canvas.drawText("Medidas Luz: Ancho = ${medida.cargoAncho} Alto = ${medida.cargoAlto}", 60f, y.toFloat(), paint)
            }

            if (!medida.motor.isNullOrBlank()) {
                y += 20
                canvas.drawText("Motor: ${medida.motor}", 60f, y.toFloat(), paint)
            }

            if (!medida.comentario.isNullOrBlank()) {
                y += 20
                canvas.drawText("   Comentario: ${medida.comentario}", 50f, y.toFloat(), paint)
            }
            y += 10
            canvas.drawText("---------------------------------------------------------------------------------------------------------",40f, y.toFloat(), paint)
            y += 20
        }

        pdfDocument.finishPage(page)


        val file = File(context.cacheDir, "nota_${nota.id}.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()


        return file
    }

    fun exportarBaseDeDatosSAF(context: Context, launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_TITLE, "MisMedidasBackup.db")
        }
        launcher.launch(intent)
    }

    fun importarBaseDeDatosSAF(context: Context, launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        launcher.launch(intent)
    }

    fun handleExportResult(context: Context, uri: Uri) {
        try {
            // Forzar cierre de la base de datos antes de copiarla
            AppDatabase.getInstance(context).close()

            val input = context.getDatabasePath("mis_medidas.db")
            context.contentResolver.openOutputStream(uri)?.use { output ->
                input.inputStream().copyTo(output)
            }
            Toast.makeText(context, "Exportación completada", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun handleImportResult(context: Context, uri: Uri) {
        try {
            val output = context.getDatabasePath("mis_medidas.db")
            context.contentResolver.openInputStream(uri)?.use { input ->
                output.outputStream().use { fileOut ->
                    input.copyTo(fileOut)
                }
            }
            Toast.makeText(context, "Importación completada", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error al importar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun mostrarRutaBaseDeDatos(context: Context) {
        var dbFile = context.getDatabasePath("mis_medidas.db")
        var path = dbFile.absolutePath
        var sizeKb = if (dbFile.exists()) dbFile.length() / 1024 else 0

        Toast.makeText(
            context,
            "Ruta DB:\n$path\nTamaño: $sizeKb KB",
            Toast.LENGTH_LONG
        ).show()
        println("Ruta DB:\n$path\nTamaño: $sizeKb KB")

        dbFile = context.getDatabasePath("ajustes.db")
        path = dbFile.absolutePath
        sizeKb = if (dbFile.exists()) dbFile.length() / 1024 else 0

        Toast.makeText(
            context,
            "Ruta Ajustes: \n$path\nTamaño: $sizeKb KB",
            Toast.LENGTH_LONG
        ).show()
        println("Ruta DB:\n$path\nTamaño: $sizeKb KB")

    }

}
