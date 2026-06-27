package com.glucocontrol.data.remote

import com.glucocontrol.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Crea e inicializa el cliente de Supabase.
 *
 * URL y Anon Key se inyectan en tiempo de compilación desde BuildConfig,
 * cuyo valor proviene de keystore.properties (no versionado en git).
 *
 * Plugins instalados:
 * - [Auth]      → autenticación (email/password); clase renombrada de GoTrue en SDK 2.x
 * - [Postgrest] → queries a la tabla glucose_readings
 *
 * ComposeAuth (Google Sign-In) se añadirá cuando se configure el Web Client ID.
 */
fun buildSupabaseClient(): SupabaseClient = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
) {
    install(Auth)
    install(Postgrest)
}
