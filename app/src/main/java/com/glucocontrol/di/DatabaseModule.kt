package com.glucocontrol.di

import android.content.Context
import com.glucocontrol.data.local.dao.GlucoseReadingDao
import com.glucocontrol.data.local.database.GlucoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provee las dependencias de persistencia: la base de datos Room y su DAO.
 *
 * Ambas se declaran [@Singleton] para garantizar una sola instancia de conexión
 * SQLite durante toda la vida de la app, evitando condiciones de carrera y
 * abriendo el archivo de BD una única vez.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GlucoDatabase = GlucoDatabase.create(context)

    // El DAO se extrae de la BD en lugar de construirse directamente,
    // porque Room genera su implementación en tiempo de compilación.
    @Provides
    @Singleton
    fun provideGlucoseReadingDao(database: GlucoDatabase): GlucoseReadingDao = database.glucoseReadingDao()
}
