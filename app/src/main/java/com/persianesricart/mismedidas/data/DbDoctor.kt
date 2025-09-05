package com.persianesricart.mismedidas.data

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object DbDoctor {

    private const val TAG = "DbDoctor"

    data class RepairReport(
        val ok: Boolean,
        val message: String,
        val details: String
    )

    fun importNotasWithRepair(context: Context, sourceUri: Uri): RepairReport {
        val dbName = "mis_medidas.db"
        val liveDb = context.getDatabasePath(dbName)
        val liveDir = liveDb.parentFile!!

        // 1) Copia a un archivo temporal primero (no toques aún la DB viva)
        val tmpFile = File(liveDir, "import_tmp_${System.currentTimeMillis()}.db")
        runCatching {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(tmpFile).use { out -> input.copyTo(out) }
            }
        }.onFailure {
            return RepairReport(false, "No se pudo copiar el archivo importado", it.message ?: "")
        }

        // 2) Repara lo necesario en el temporal
        val repair = runCatching { patchNotasDbFile(tmpFile) }.fold(
            onSuccess = { it },
            onFailure = { e ->
                return RepairReport(false, "Error parcheando la BD importada", e.message ?: "")
            }
        )

        // 3) Valida con un par de consultas mínimas
        val valid = runCatching { validateNotasDb(tmpFile) }.getOrElse { false }
        if (!valid) {
            tmpFile.delete()
            return RepairReport(false, "La BD importada no pasó la validación", repair)
        }

        // 4) Sustituye la DB viva de forma segura
        runCatching {
            AppDatabase.getInstance(context).close()
            runCatching { File(liveDir, "$dbName-wal").delete() }
            runCatching { File(liveDir, "$dbName-shm").delete() }

            // Mueve la actual a backup por si acaso
            val backup = File(liveDir, "backup_before_import_${System.currentTimeMillis()}.db")
            if (liveDb.exists()) liveDb.renameTo(backup)

            // Mueve el temporal a la ruta final
            tmpFile.renameTo(liveDb)
            // Borra WAL/SHM de la nueva base por si el origen venía en modo WAL
            runCatching { File(liveDir, "${liveDb.name}-wal").delete() }
            runCatching { File(liveDir, "${liveDb.name}-shm").delete() }

            // Limpieza extra
            runCatching { File(liveDir, "$dbName-wal").delete() }
            runCatching { File(liveDir, "$dbName-shm").delete() }
        }.onFailure { e ->
            return RepairReport(false, "No se pudo reemplazar la BD activa", e.message ?: "")
        }

        return RepairReport(true, "Importación y reparación completadas", repair)
    }

    // ---------- Internals ----------

    private fun patchNotasDbFile(dbFile: File): String {
        SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
            db.execSQL("PRAGMA foreign_keys=OFF;")

            val logLines = mutableListOf<String>()

            fun tableExists(name: String): Boolean {
                db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?;", arrayOf(name)).use {
                    return it.moveToFirst()
                }
            }

            fun columnExists(table: String, column: String): Boolean {
                db.rawQuery("PRAGMA table_info($table);", null).use { c ->
                    val idxName = c.getColumnIndex("name")
                    while (c.moveToNext()) {
                        val col = c.getString(idxName)
                        if (col == column) return true
                    }
                    return false
                }
            }

            fun ensureTableCroquis() {
                if (!tableExists("Croquis")) {
                    logLines += "Create table Croquis"
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS Croquis (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            medidaId INTEGER NOT NULL,
                            nombre TEXT NOT NULL,
                            filePath TEXT NOT NULL,
                            createdAt INTEGER NOT NULL,
                            updatedAt INTEGER NOT NULL,
                            FOREIGN KEY(medidaId) REFERENCES Medida(id) ON DELETE CASCADE
                        )
                    """.trimIndent())
                } else {
                    logLines += "Table Croquis OK"
                }
            }

            fun ensureNotaColumns() {
                if (!columnExists("Nota", "email")) {
                    logLines += "Add Nota.email"
                    runCatching { db.execSQL("ALTER TABLE Nota ADD COLUMN email TEXT") }
                }
                if (!columnExists("Nota", "referencia")) {
                    logLines += "Add Nota.referencia"
                    runCatching { db.execSQL("ALTER TABLE Nota ADD COLUMN referencia TEXT") }
                }
            }

            fun ensureMedidaColumns() {
                if (!columnExists("Medida", "luz")) {
                    logLines += "Add Medida.luz"
                    runCatching { db.execSQL("ALTER TABLE Medida ADD COLUMN luz INTEGER NOT NULL DEFAULT 0") }
                }
                if (!columnExists("Medida", "cargoAncho")) {
                    logLines += "Add Medida.cargoAncho"
                    runCatching { db.execSQL("ALTER TABLE Medida ADD COLUMN cargoAncho TEXT") }
                }
                if (!columnExists("Medida", "cargoAlto")) {
                    logLines += "Add Medida.cargoAlto"
                    runCatching { db.execSQL("ALTER TABLE Medida ADD COLUMN cargoAlto TEXT") }
                }
                if (!columnExists("Medida", "motor")) {
                    logLines += "Add Medida.motor"
                    runCatching { db.execSQL("ALTER TABLE Medida ADD COLUMN motor TEXT") }
                }
                if (!columnExists("Medida", "acabado")) {
                    logLines += "Add Medida.acabado"
                    runCatching { db.execSQL("ALTER TABLE Medida ADD COLUMN acabado TEXT") }
                }
                if (!columnExists("Medida", "uuid")) {
                    logLines += "Add Medida.uuid"
                    runCatching { db.execSQL("ALTER TABLE Medida ADD COLUMN uuid TEXT") }
                }
                // índice (si ya existía, no pasa nada)
                logLines += "Ensure index_Medida_uuid"
                runCatching { db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_Medida_uuid ON Medida(uuid)") }
            }

            fun sanitizeNotNulls() {
                // Nota: asumo que estas columnas son NON-NULL en tus entidades
                // Ajusta si hiciera falta.
                logLines += "Sanitize Nota non-nulls"
                runCatching { db.execSQL("UPDATE Nota SET cliente    = COALESCE(cliente, '')") }
                runCatching { db.execSQL("UPDATE Nota SET direccion  = COALESCE(direccion, '')") }
                runCatching { db.execSQL("UPDATE Nota SET poblacion  = COALESCE(poblacion, '')") }
                runCatching { db.execSQL("UPDATE Nota SET telefono   = COALESCE(telefono, '')") }
                runCatching { db.execSQL("UPDATE Nota SET movil      = COALESCE(movil, '')") }
                runCatching { db.execSQL("UPDATE Nota SET fecha      = COALESCE(fecha, '')") }
                runCatching { db.execSQL("UPDATE Nota SET email      = COALESCE(email, '')") }
                runCatching { db.execSQL("UPDATE Nota SET referencia = COALESCE(referencia, '')") }

                logLines += "Sanitize Medida non-nulls"
                runCatching { db.execSQL("UPDATE Medida SET ud    = COALESCE(ud, '')") }
                runCatching { db.execSQL("UPDATE Medida SET ancho = COALESCE(ancho, '')") }
                runCatching { db.execSQL("UPDATE Medida SET alto  = COALESCE(alto, '')") }
                runCatching { db.execSQL("UPDATE Medida SET tipo  = COALESCE(tipo, '')") }
                runCatching { db.execSQL("UPDATE Medida SET modelo= COALESCE(modelo, '')") }
                runCatching { db.execSQL("UPDATE Medida SET color = COALESCE(color, '')") }
                // luz debería ser INTEGER NOT NULL
                runCatching { db.execSQL("UPDATE Medida SET luz = COALESCE(luz, 0)") }
                // acabado y motor son opcionales → no tocamos (si son NULL las entidades ya lo contemplan)
            }

            // PRAGMA user_version actual (diagnóstico)
            val currentVersion = db.rawQuery("PRAGMA user_version;", null).use { c ->
                c.moveToFirst(); c.getInt(0)
            }
            logLines += "user_version antes: $currentVersion"

            ensureTableCroquis()
            ensureNotaColumns()
            ensureMedidaColumns()
            sanitizeNotNulls()

            // Fija versión esperada por Room
            db.execSQL("PRAGMA user_version=3;")
            logLines += "user_version después: 3"

            val report = logLines.joinToString("\n")
            Log.i(TAG, "Repair report:\n$report")
            return report
        }
    }

    private fun validateNotasDb(dbFile: File): Boolean {
        SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            // ¿Existen tablas clave?
            if (!tableExists(db, "Nota")) return false
            if (!tableExists(db, "Medida")) return false
            if (!tableExists(db, "Croquis")) return false

            // ¿Columnas mínimas?
            if (!columnExists(db, "Medida", "uuid")) return false
            if (!columnExists(db, "Medida", "luz")) return false

            // SELECTs simples
            runCatching { db.rawQuery("SELECT id, cliente, fecha FROM Nota LIMIT 1;", null).use { } }.onFailure { return false }
            runCatching { db.rawQuery("SELECT id, notaId, ud, ancho, alto FROM Medida LIMIT 1;", null).use { } }.onFailure { return false }

            return true
        }
    }

    private fun tableExists(db: SQLiteDatabase, name: String): Boolean {
        db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?;", arrayOf(name)).use {
            return it.moveToFirst()
        }
    }

    private fun columnExists(db: SQLiteDatabase, table: String, column: String): Boolean {
        db.rawQuery("PRAGMA table_info($table);", null).use { c ->
            val idxName = c.getColumnIndex("name")
            while (c.moveToNext()) {
                val col = c.getString(idxName)
                if (col == column) return true
            }
            return false
        }
    }
}