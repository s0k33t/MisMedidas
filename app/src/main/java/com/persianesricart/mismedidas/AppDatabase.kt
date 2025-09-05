package com.persianesricart.mismedidas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.persianesricart.mismedidas.data.dao.CroquisDao
import com.persianesricart.mismedidas.data.dao.MedidaDao
import com.persianesricart.mismedidas.data.dao.NotaDao
import com.persianesricart.mismedidas.data.entities.Croquis
import com.persianesricart.mismedidas.data.entities.Medida
import com.persianesricart.mismedidas.data.entities.Nota

@Database(
    entities = [Nota::class, Medida::class, Croquis::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notaDao(): NotaDao
    abstract fun medidaDao(): MedidaDao
    abstract fun croquisDao(): CroquisDao

    companion object {

        /**
         * V1 -> V2:
         * - Introducción de la tabla Croquis.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Crea tabla Croquis si no existe (para backups antiguos)
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS Croquis (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        medidaId INTEGER NOT NULL,
                        nombre TEXT NOT NULL,
                        filePath TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                // Índice opcional para acelerar consultas por medidaId
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_Croquis_medidaId ON Croquis(medidaId)
                    """.trimIndent()
                )
            }
        }

        /**
         * V2 -> V3:
         * - Añade columna uuid a Medida y un índice único.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Añadir columna (nullable) para no romper filas existentes
                db.execSQL("ALTER TABLE Medida ADD COLUMN uuid TEXT")
                // Índice único (permite NULL/'' repetidos hasta que rellenemos)
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_Medida_uuid ON Medida(uuid)
                    """.trimIndent()
                )
            }
        }

        val ALL_MIGRATIONS = arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3
        )

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mis_medidas.db"
                )
                    // 🔧 registra TODAS las migraciones necesarias para abrir backups antiguos
                    .addMigrations(*ALL_MIGRATIONS)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}

/*
package com.persianesricart.mismedidas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.persianesricart.mismedidas.data.dao.MedidaDao
import com.persianesricart.mismedidas.data.dao.NotaDao
import com.persianesricart.mismedidas.data.dao.CroquisDao
import com.persianesricart.mismedidas.data.entities.Medida
import com.persianesricart.mismedidas.data.entities.Nota
import com.persianesricart.mismedidas.data.entities.Croquis




import androidx.room.*
import com.persianesricart.mismedidas.data.dao.*
import com.persianesricart.mismedidas.data.entities.*

@Database(
    entities = [Nota::class, Medida::class, Croquis::class],
    version = 3,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun notaDao(): NotaDao
    abstract fun medidaDao(): MedidaDao
    abstract fun croquisDao(): CroquisDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migración de versión 1 a 2: se añade la tabla croquis
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS croquis (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        medidaId INTEGER NOT NULL,
                        nombre TEXT NOT NULL,
                        filePath TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        FOREIGN KEY(medidaId) REFERENCES Medida(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_croquis_medidaId ON croquis(medidaId)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mis_medidas.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    //.fallbackToDestructiveMigration() // ⚠️ Úsalo solo si no te importa perder datos
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

*/