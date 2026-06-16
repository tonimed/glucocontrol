package com.glucocontrol.presentation.screen.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.glucocontrol.domain.model.GlucoseReading
import com.glucocontrol.presentation.component.GlucoseReadingCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onBack: () -> Unit, onEditReading: (Long) -> Unit, viewModel: HistoryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver al dashboard")
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
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(12.dp))

            // Selector de periodo: Semana / Mes
            PeriodSelector(
                current = uiState.period,
                onSelect = { viewModel.setPeriod(it) },
            )

            Spacer(Modifier.height(8.dp))

            // Navegador de periodo
            PeriodNavigator(
                period = uiState.period,
                referenceDate = uiState.referenceDate,
                onPrevious = { viewModel.previousPeriod() },
                onNext = { viewModel.nextPeriod() },
            )

            Spacer(Modifier.height(12.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            if (uiState.groupedReadings.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Sin lecturas en este periodo",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            } else {
                // Estadística global del periodo
                val allReadings = uiState.groupedReadings.values.flatten()
                PeriodStatsBar(readings = allReadings, glucoseRange = uiState.glucoseRange)

                Spacer(Modifier.height(12.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    uiState.groupedReadings.forEach { (date, readings) ->
                        item(key = date.toEpochDay()) {
                            DateGroupHeader(date = date, readings = readings)
                        }
                        items(readings, key = { it.id }) { reading ->
                            GlucoseReadingCard(
                                reading = reading,
                                glucoseRange = uiState.glucoseRange,
                                onEdit = { onEditReading(reading.id) },
                                onDelete = { viewModel.deleteReading(reading.id) },
                            )
                        }
                        item { Spacer(Modifier.height(4.dp)) }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun PeriodSelector(current: HistoryPeriod, onSelect: (HistoryPeriod) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = current == HistoryPeriod.DAILY,
            onClick = { onSelect(HistoryPeriod.DAILY) },
            label = { Text("Día") },
        )
        FilterChip(
            selected = current == HistoryPeriod.WEEKLY,
            onClick = { onSelect(HistoryPeriod.WEEKLY) },
            label = { Text("Semana") },
        )
        FilterChip(
            selected = current == HistoryPeriod.MONTHLY,
            onClick = { onSelect(HistoryPeriod.MONTHLY) },
            label = { Text("Mes") },
        )
    }
}

@Composable
private fun PeriodNavigator(
    period: HistoryPeriod,
    referenceDate: LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val label =
        when (period) {
            HistoryPeriod.DAILY -> {
                val fmt = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale.forLanguageTag("es"))
                if (referenceDate ==
                    LocalDate.now()
                ) {
                    "Hoy"
                } else {
                    referenceDate.format(fmt).replaceFirstChar { it.uppercase() }
                }
            }

            HistoryPeriod.WEEKLY -> {
                val start =
                    referenceDate.with(
                        java.time.temporal.TemporalAdjusters
                            .previousOrSame(java.time.DayOfWeek.MONDAY),
                    )
                val end = start.plusDays(6)
                val fmt = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es"))
                "${start.format(fmt)} – ${end.format(fmt)} ${start.year}"
            }

            HistoryPeriod.MONTHLY -> {
                val fmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es"))
                referenceDate.format(fmt).replaceFirstChar { it.uppercase() }
            }
        }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Periodo anterior")
        }
        Text(text = label, style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Periodo siguiente")
        }
    }
}

@Composable
private fun PeriodStatsBar(readings: List<GlucoseReading>, glucoseRange: com.glucocontrol.domain.model.GlucoseRange) {
    val values = readings.map { it.valueMgDl }
    val average = values.average().roundToInt()
    val inRange = values.count { it in glucoseRange.minTarget..glucoseRange.maxTarget }
    val pctRange = (inRange * 100.0 / values.size).roundToInt()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        StatsChip("Lecturas", "${readings.size}")
        StatsChip("Media", "$average")
        StatsChip("En rango", "$pctRange%")
        StatsChip("Mín/Máx", "${values.min()}/${values.max()}")
    }
}

@Composable
private fun StatsChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun DateGroupHeader(date: LocalDate, readings: List<GlucoseReading>) {
    val formatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale.forLanguageTag("es"))
    val label = if (date == LocalDate.now()) "Hoy" else date.format(formatter).replaceFirstChar { it.uppercase() }
    val avg = readings.map { it.valueMgDl }.average().roundToInt()

    Column {
        HorizontalDivider()
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Media $avg mg/dL",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}
