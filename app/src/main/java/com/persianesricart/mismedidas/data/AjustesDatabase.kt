// AjustesDatabase.kt
package com.persianesricart.mismedidas.data.ajustes

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.persianesricart.mismedidas.data.ajustes.dao.*
import com.persianesricart.mismedidas.data.ajustes.entities.*

@Database(
    entities = [Tipo::class, Modelo::class, Acabado::class, Color::class],
    version = 1,
    exportSchema = false
)
abstract class AjustesDatabase : RoomDatabase() {
    abstract fun tipoDao(): TipoDao
    abstract fun modeloDao(): ModeloDao
    abstract fun acabadoDao(): AcabadoDao
    abstract fun colorDao(): ColorDao

    companion object {
        @Volatile private var INSTANCE: AjustesDatabase? = null

        fun getInstance(context: Context): AjustesDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AjustesDatabase::class.java,
                    "ajustes.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
