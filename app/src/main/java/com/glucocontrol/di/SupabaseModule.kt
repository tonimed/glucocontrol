package com.glucocontrol.di

import com.glucocontrol.data.remote.buildSupabaseClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

/**
 * Provee el [SupabaseClient] como singleton para toda la app.
 *
 * Un único cliente gestiona la conexión HTTP (Ktor), la sesión de auth (GoTrue)
 * y las queries a la BD (PostgREST). Crear múltiples instancias abriría
 * varias conexiones innecesarias y desincronizaría el estado de sesión.
 */
@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = buildSupabaseClient()
}
