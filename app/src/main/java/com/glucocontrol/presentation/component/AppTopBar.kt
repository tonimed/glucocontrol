package com.glucocontrol.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.glucocontrol.R

/**
 * Barra superior personalizada: imagen centrada encima del título.
 * Sin restricción de altura fija (el Surface crece con el contenido).
 * [onBack] = null en HomeScreen (sin botón de retroceso).
 * [showImage] = false en pantallas de detalle donde el espacio vertical es limitado.
 * [actions] = slot opcional para iconos en el extremo derecho (ej. cerrar sesión).
 */
@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    showImage: Boolean = true,
    actions: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
        ) {
            // Título centrado: dibujo encima del texto (si showImage) o solo texto
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = if (showImage) 10.dp else 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (showImage) {
                    Image(
                        painter = painterResource(R.drawable.ic_app_logo),
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }

            // Botón de retroceso superpuesto a la izquierda
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 4.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }

            // Acciones opcionales superpuestas a la derecha (ej. cerrar sesión)
            if (actions != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp),
                ) {
                    actions()
                }
            }
        }
    }
}
