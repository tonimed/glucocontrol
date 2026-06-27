package com.glucocontrol.domain.model

/**
 * Modelo de dominio del usuario autenticado.
 * Sin dependencias Android — solo Kotlin puro.
 *
 * @param id          UUID del usuario en Supabase Auth (auth.users.id).
 * @param email       Email con el que se registró o inició sesión.
 * @param displayName Nombre mostrable (disponible con Google OAuth; null si es email/password).
 */
data class User(val id: String, val email: String, val displayName: String? = null)
