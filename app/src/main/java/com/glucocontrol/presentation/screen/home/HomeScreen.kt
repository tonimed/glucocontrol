package com.glucocontrol.presentation.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddReading: () -> Unit,
    onHistory: () -> Unit,
    onChart: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control Glucosa") },
                colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TodaySummaryCard(uiState = uiState)

            Text(
                text = "¿Qué quieres hacer?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Add,
                    title = "Nuevo registro",
                    subtitle = "Añade una lectura",
                    onClick = onAddReading,
                )
                ActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.DateRange,
                    title = "Historial",
                    subtitle = "Por día, semana o mes",
                    onClick = onHistory,
                )
            }

            ActionCard(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.AutoMirrored.Filled.ShowChart,
                title = "Gráfica",
                subtitle = "Visualiza tu evolución semanal o mensual",
                onClick = onChart,
                horizontal = true,
            )
        }
    }
}

@Composable
private fun TodaySummaryCard(uiState: HomeUiState) {
    val fmt = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM yyyy", Locale.forLanguageTag("es"))
    val dateLabel = LocalDate.now().format(fmt).replaceFirstChar { it.uppercase() }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            )
            Spacer(Modifier.height(10.dp))
            when {
                uiState.isLoading ->
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))

                uiState.todayReadings.isEmpty() ->
                    Text(
                        text = "Sin lecturas hoy",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )

                else -> {
                    val avg = uiState.todayReadings.map { it.valueMgDl }.average().roundToInt()
                    val last = uiState.todayReadings.last()
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        SummaryPill("Lecturas", "${uiState.todayReadings.size}")
                        SummaryPill("Media hoy", "$avg mg/dL")
                        SummaryPill("Última", "${last.valueMgDl} mg/dL")
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryPill(label: String, value: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    horizontal: Boolean = false,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
    ) {
        if (horizontal) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }
    }
}
