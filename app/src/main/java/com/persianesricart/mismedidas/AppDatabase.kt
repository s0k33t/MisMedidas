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

@Database(
    entities = [Nota::class, Medida::class, Croquis::class],
    version = 2,
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

/*
package com.persianesricart.mismedidas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.persianesricart.mismedidas.data.dao.MedidaDao
import com.persianesricart.mismedidas.data.dao.NotaDao
import com.persianesricart.mismedidas.data.entities.Medida
import com.persianesricart.mismedidas.data.entities.Nota

@Database(entities = [Nota::class, Medida::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notaDao(): NotaDao
    abstract fun medidaDao(): MedidaDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mis_medidas.db"
                ).fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
*/