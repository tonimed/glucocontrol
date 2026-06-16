package com.glucocontrol.presentation.screen.addreading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.glucocontrol.domain.model.ReadingTag
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditReadingScreen(onBack: () -> Unit, viewModel: AddEditReadingViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Navegar atrás automáticamente tras guardar con éxito
    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) onBack()
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) "Editar lectura" else "Nueva lectura") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Glucosa ───────────────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.valueMgDl,
                onValueChange = { viewModel.onValueChanged(it) },
                label = { Text("Glucosa (mg/dL)") },
                suffix = { Text("mg/dL") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = uiState.error != null,
                supportingText = uiState.error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // ── Fecha ─────────────────────────────────────────────────────────
            val dateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es"))
            OutlinedTextField(
                value = uiState.date.format(dateFormatter),
                onValueChange = {},
                label = { Text("Fecha") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                // Abrir DatePicker al interactuar con el campo
                interactionSource =
                remember {
                    androidx.compose.foundation.interaction
                        .MutableInteractionSource()
                }.also { src ->
                    LaunchedEffect(src) {
                        src.interactions.collect { showDatePicker = true }
                    }
                },
            )

            // ── Hora (opcional) ───────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "No especificada",
                onValueChange = {},
                label = { Text("Hora (opcional)") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                interactionSource =
                remember {
                    androidx.compose.foundation.interaction
                        .MutableInteractionSource()
                }.also { src ->
                    LaunchedEffect(src) {
                        src.interactions.collect { showTimePicker = true }
                    }
                },
            )
            if (uiState.time != null) {
                TextButton(onClick = { viewModel.onTimeChanged(null) }) {
                    Text("Eliminar hora")
                }
            }

            // ── Etiqueta ──────────────────────────────────────────────────────
            Text("Etiqueta", style = MaterialTheme.typography.labelMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReadingTag.entries.forEach { tag ->
                    FilterChip(
                        selected = uiState.tag == tag,
                        onClick = { viewModel.onTagChanged(tag) },
                        label = { Text(tag.label) },
                    )
                }
            }

            // ── Notas ─────────────────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.onNotesChanged(it) },
                label = { Text("Notas (opcional)") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )

            // ── Guardar ───────────────────────────────────────────────────────
            Button(
                onClick = { viewModel.save() },
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.isSaving) "Guardando…" else "Guardar")
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // ── DatePickerDialog ──────────────────────────────────────────────────────
    if (showDatePicker) {
        val datePickerState =
            rememberDatePickerState(
                // Convertir LocalDate a milisegundos UTC para DatePicker
                initialSelectedDateMillis = uiState.date.toEpochDay() * 86_400_000L,
            )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        viewModel.onDateChanged(LocalDate.ofEpochDay(millis / 86_400_000L))
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ── TimePickerDialog ──────────────────────────────────────────────────────
    if (showTimePicker) {
        val now = uiState.time ?: java.time.LocalTime.now()
        val timePickerState =
            rememberTimePickerState(
                initialHour = now.hour,
                initialMinute = now.minute,
                is24Hour = true,
            )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            androidx.compose.material3.Surface(
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Seleccionar hora",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                    TimePicker(state = timePickerState)
                    Spacer(Modifier.height(16.dp))
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
                        TextButton(onClick = {
                            viewModel.onTimeChanged(
                                java.time.LocalTime.of(timePickerState.hour, timePickerState.minute),
                            )
                            showTimePicker = false
                        }) { Text("Aceptar") }
                    }
                }
            }
        }
    }
}
