package com.glucocontrol.presentation.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glucocontrol.domain.usecase.auth.GetCurrentUserUseCase
import com.glucocontrol.domain.usecase.auth.SignInWithEmailUseCase
import com.glucocontrol.domain.usecase.auth.SignInWithGoogleUseCase
import com.glucocontrol.domain.usecase.auth.SignOutUseCase
import com.glucocontrol.domain.usecase.auth.SignUpWithEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    // Mensaje informativo distinto de error (ej. "revisa tu email")
    val info: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInWithEmailUseCase: SignInWithEmailUseCase,
    private val signUpWithEmailUseCase: SignUpWithEmailUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            val user = runCatching { getCurrentUserUseCase() }.getOrNull()
            _uiState.update { it.copy(isLoading = false, isAuthenticated = user != null) }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, info = null) }
            runCatching { signInWithEmailUseCase(email, password) }
                .onSuccess { _uiState.update { it.copy(isLoading = false, isAuthenticated = true) } }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = translateError(e, context = "login")) }
                }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, info = null) }
            runCatching { signUpWithEmailUseCase(email, password) }
                .onSuccess {
                    // Supabase puede requerir confirmación de email antes de crear sesión.
                    // Comprobamos si la sesión quedó activa inmediatamente.
                    val user = runCatching { getCurrentUserUseCase() }.getOrNull()
                    if (user != null) {
                        _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                    } else {
                        // Confirmación de email pendiente — no navegar, mostrar aviso
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                info = "Cuenta creada. Revisa tu correo y confirma la cuenta antes de iniciar sesión.",
                            )
                        }
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = translateError(e, context = "registro")) }
                }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, info = null) }
            runCatching { signInWithGoogleUseCase(idToken) }
                .onSuccess { _uiState.update { it.copy(isLoading = false, isAuthenticated = true) } }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error con Google") }
                }
        }
    }

    /** Traduce errores de Supabase (en inglés) a mensajes en español para el usuario. */
    private fun translateError(e: Throwable, context: String): String {
        val raw = e.message?.lowercase() ?: return "Error en $context. Inténtalo de nuevo."
        return when {
            "email rate limit exceeded" in raw || "rate limit" in raw ->
                "Demasiados intentos. Espera unos minutos antes de volver a intentarlo."

            "invalid login credentials" in raw || "invalid credentials" in raw ->
                "Correo o contraseña incorrectos."

            "email not confirmed" in raw ->
                "Debes confirmar tu correo antes de iniciar sesión. Revisa tu bandeja de entrada."

            "user already registered" in raw || "already registered" in raw ->
                "Ya existe una cuenta con ese correo. Usa 'Iniciar sesión'."

            "password should be at least" in raw ->
                "La contraseña debe tener al menos 6 caracteres."

            "unable to validate email address" in raw || "invalid email" in raw ->
                "El formato del correo no es válido."

            "network" in raw || "connect" in raw || "timeout" in raw ->
                "Error de conexión. Comprueba tu internet e inténtalo de nuevo."

            else -> e.message ?: "Error en $context. Inténtalo de nuevo."
        }
    }

    fun signOut() {
        viewModelScope.launch {
            runCatching { signOutUseCase() }
                .onSuccess { _uiState.update { it.copy(isAuthenticated = false, error = null, info = null) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(error = null, info = null) }

    fun setError(message: String) = _uiState.update { it.copy(error = message, isLoading = false) }
}
