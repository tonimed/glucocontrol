package com.glucocontrol.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Named
import javax.inject.Singleton

/**
 * Provee dependencias de nivel de aplicación no ligadas a un subsistema concreto.
 *
 * El dispatcher de IO se inyecta con [@Named] en lugar de un tipo directo para
 * permitir sustituirlo por [UnconfinedTestDispatcher] en tests instrumentados,
 * desacoplando la lógica asíncrona del scheduler real.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    @Named("io")
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
