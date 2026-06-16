package com.glucocontrol.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.glucocontrol.data.local.dao.GlucoseReadingDao
import com.glucocontrol.data.local.entity.GlucoseReadingEntity

/**
 * Base de datos Room. Actúa como punto de acceso único a SQLite.
 *
 * - [version] = 1: versión inicial del esquema. Incrementar aquí al añadir migraciones.
 * - [exportSchema] = true: genera un JSON del esquema en /schemas/ durante la compilación,
 *   lo que permite auditar cambios de esquema en el historial de git.
 *
 * La instancia se crea exclusivamente desde [DatabaseModule] (Hilt) mediante [create],
 * garantizando que sea un singleton en toda la app.
 */
@Database(
    entities = [GlucoseReadingEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class GlucoDatabase : RoomDatabase() {
    abstract fun glucoseReadingDao(): GlucoseReadingDao

    companion object {
        private const val DATABASE_NAME = "gluco_control.db"

        /**
         * Construye la instancia de la base de datos.
         *
         * [Context.applicationContext] evita memory leaks al desacoplar
         * la base de datos del ciclo de vida de Activity o Fragment.
         *
         * allowMainThreadQueries() NO se habilita: Room obliga a usar
         * coroutines o hilos secundarios para todas las operaciones de I/O.
         */
        fun create(context: Context): GlucoDatabase = Room
            .databaseBuilder(
                context.applicationContext,
                GlucoDatabase::class.java,
                DATABASE_NAME,
            ).build()
    }
}
