package com.glucocontrol.data.repository

import com.glucocontrol.domain.model.User
import com.glucocontrol.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import javax.inject.Inject

/**
 * Implementación de [AuthRepository] usando Supabase Auth (gotrue-kt 2.x).
 *
 * En SDK 2.x la clase se llama [Auth] y la extension property es [auth] en
 * lugar de [GoTrue]/[gotrue] de la versión 1.x — el artefacto sigue siendo gotrue-kt.
 *
 * Todos los métodos son suspend porque implican operaciones de red.
 * El cliente Supabase almacena la sesión de forma persistente; en el próximo
 * arranque de la app, [currentUserOrNull] devuelve el usuario sin petición de red.
 */
class AuthRepositoryImpl @Inject constructor(private val supabase: SupabaseClient) : AuthRepository {

    override suspend fun signInWithEmail(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signInWithGoogle(idToken: String) {
        // IDToken intercambia el token nativo de Google por una sesión de Supabase
        supabase.auth.signInWith(IDToken) {
            this.idToken = idToken
            provider = Google
        }
    }

    override suspend fun signOut() {
        supabase.auth.signOut()
    }

    override suspend fun getCurrentUser(): User? {
        val userInfo = supabase.auth.currentUserOrNull() ?: return null
        return User(
            id = userInfo.id,
            email = userInfo.email ?: "",
            displayName = userInfo.userMetadata?.get("full_name")?.toString()?.trim('"'),
        )
    }
}
