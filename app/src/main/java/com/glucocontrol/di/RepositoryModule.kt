package com.glucocontrol.di

import com.glucocontrol.data.repository.GlucoseRepositoryImpl
import com.glucocontrol.domain.repository.GlucoseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Enlaza la interfaz de dominio [GlucoseRepository] con su implementación concreta
 * [GlucoseRepositoryImpl].
 *
 * Se usa [@Binds] en lugar de [@Provides] porque no hay lógica de construcción:
 * Hilt construye [GlucoseRepositoryImpl] automáticamente gracias a su
 * [@Inject constructor]. Esto genera menos bytecode y es la forma idiomática
 * de registrar interfaces con una sola implementación.
 *
 * Debe ser una clase abstracta (no object) para poder declarar métodos abstractos.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindGlucoseRepository(impl: GlucoseRepositoryImpl): GlucoseRepository
}
