package com.glucocontrol.presentation.screen.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.glucocontrol.BuildConfig
import com.glucocontrol.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

private val AuthBackground = Color(0xFFE3F2FD)

// El botón de Google solo funciona cuando el Web Client ID está configurado.
// Un placeholder contiene dígitos de relleno y no es un ID real de Google Cloud.
private val googleSignInAvailable: Boolean
    get() = BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank() &&
        !BuildConfig.GOOGLE_WEB_CLIENT_ID.startsWith("000000")

/**
 * Pantalla de autenticación: login / registro con email-password y Google Sign-In.
 *
 * Flujo:
 * 1. Al montarse, el [AuthViewModel] comprueba la sesión en caché.
 * 2. Si ya hay sesión activa → [onLoginSuccess] se dispara automáticamente.
 * 3. Si no hay sesión → muestra el formulario.
 * 4. Si el registro requiere confirmación de email, se muestra aviso informativo.
 */
@Composable
fun AuthScreen(onLoginSuccess: () -> Unit, viewModel: AuthViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
            .statusBarsPadding(),
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            else -> {
                AuthContent(
                    uiState = uiState,
                    onSignIn = { email, password -> viewModel.signIn(email, password) },
                    onSignUp = { email, password -> viewModel.signUp(email, password) },
                    onSignOut = { viewModel.signOut() },
                    onGoogleSignIn = {
                        if (googleSignInAvailable) {
                            coroutineScope.launch {
                                handleGoogleSignIn(
                                    context = context,
                                    onToken = { token -> viewModel.signInWithGoogle(token) },
                                    onError = { msg -> viewModel.setError(msg) },
                                )
                            }
                        } else {
                            viewModel.setError("Google Sign-In no está configurado todavía.")
                        }
                    },
                    onClearMessages = { viewModel.clearMessages() },
                )
            }
        }
    }
}

@Composable
private fun AuthContent(
    uiState: AuthUiState,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onSignOut: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onClearMessages: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.height(12.dp))

        Image(
            painter = painterResource(R.drawable.ic_app_logo),
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            contentScale = ContentScale.Fit,
        )
        Text(
            text = "Control de Glucosa",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(Modifier.height(8.dp))

        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = {
                    selectedTab = 0
                    onClearMessages()
                },
                text = { Text("Iniciar sesión") },
            )
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                    onClearMessages()
                },
                text = { Text("Registrarse") },
            )
        }

        Spacer(Modifier.height(4.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Ver contraseña",
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        // Error de operación (login fallido, cuenta duplicada…)
        if (uiState.error != null) {
            Text(
                text = uiState.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }

        // Aviso informativo (confirmación de email pendiente)
        if (uiState.info != null) {
            Text(
                text = uiState.info,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }

        Button(
            onClick = {
                if (selectedTab == 0) onSignIn(email, password) else onSignUp(email, password)
            },
            enabled = email.isNotBlank() && password.isNotBlank() && !uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (selectedTab == 0) "Entrar" else "Crear cuenta")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                "o",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        // El botón de Google se muestra siempre pero solo funciona si está configurado.
        // Si el Web Client ID no está configurado muestra un mensaje de error informativo.
        OutlinedButton(
            onClick = onGoogleSignIn,
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (googleSignInAvailable) "Continuar con Google" else "Google (no configurado aún)")
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(4.dp))

        TextButton(
            onClick = onSignOut,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Text("Cerrar sesión")
        }
    }
}

/**
 * Lanza el flujo de Google Sign-In usando el Credential Manager nativo de Android.
 * Solo se llama cuando [googleSignInAvailable] es true.
 */
private suspend fun handleGoogleSignIn(
    context: android.content.Context,
    onToken: (String) -> Unit,
    onError: (String) -> Unit,
) {
    try {
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        val result = credentialManager.getCredential(context, request)
        val googleCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
        onToken(googleCredential.idToken)
    } catch (e: GetCredentialException) {
        onError("Google Sign-In no disponible: ${e.message}")
    } catch (e: Exception) {
        onError("Error con Google: ${e.message}")
    }
}
