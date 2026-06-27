package com.glucocontrol.di

import com.glucocontrol.data.repository.AuthRepositoryImpl
import com.glucocontrol.data.repository.GlucoseRepositoryImpl
import com.glucocontrol.domain.repository.AuthRepository
import com.glucocontrol.domain.repository.GlucoseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Enlaza las interfaces de dominio con sus implementaciones concretas.
 *
 * [@Binds] genera menos bytecode que [@Provides] cuando no hay lógica de construcción:
 * Hilt inyecta la implementación directamente gracias a su [@Inject constructor].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGlucoseRepository(impl: GlucoseRepositoryImpl): GlucoseRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
