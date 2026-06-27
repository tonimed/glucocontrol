package com.glucocontrol.domain.repository

import com.glucocontrol.domain.model.User

/**
 * Contrato de autenticación para la capa de dominio.
 * La implementación concreta reside en [AuthRepositoryImpl] (capa de datos).
 */
interface AuthRepository {
    /** Inicia sesión con email y contraseña. Lanza excepción si las credenciales son incorrectas. */
    suspend fun signInWithEmail(email: String, password: String)

    /** Crea una cuenta nueva con email y contraseña. */
    suspend fun signUpWithEmail(email: String, password: String)

    /**
     * Inicia sesión con un ID token de Google obtenido vía Credential Manager.
     * Supabase intercambia este token por una sesión usando el proveedor Google OAuth.
     */
    suspend fun signInWithGoogle(idToken: String)

    /** Cierra la sesión activa y limpia los tokens locales. */
    suspend fun signOut()

    /** Devuelve el usuario autenticado actualmente, o null si no hay sesión. */
    suspend fun getCurrentUser(): User?
}
