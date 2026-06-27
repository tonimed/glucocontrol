package com.glucocontrol.presentation.screen.readingdetail

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.glucocontrol.domain.model.GlucoseRange
import com.glucocontrol.domain.model.GlucoseReading
import com.glucocontrol.presentation.component.AppTopBar
import com.glucocontrol.presentation.component.GlucoseReadingCard
import com.glucocontrol.presentation.theme.GlucoseHigh
import com.glucocontrol.presentation.theme.GlucoseLow
import com.glucocontrol.presentation.theme.GlucoseNormal

@Composable
fun ReadingDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: ReadingDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // showImage = false: barra compacta para que todo el contenido quepa sin scroll
    Scaffold(
        topBar = { AppTopBar(title = "Detalle", onBack = onBack, showImage = false) },
    ) { padding ->
        // Local val para que Kotlin pueda smart-cast GlucoseReading? → GlucoseReading
        val reading = uiState.reading
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            reading == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Lectura no encontrada",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        // verticalScroll como red de seguridad en pantallas muy pequeñas
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Tarjeta de lectura en modo solo lectura (igual a Historial, sin botones)
                    GlucoseReadingCard(
                        reading = reading,
                        glucoseRange = uiState.glucoseRange,
                    )

                    // Barra horizontal de estado: sin título para ahorrar espacio vertical
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            GlucoseStatusBar(
                                valueMgDl = reading.valueMgDl,
                                glucoseRange = uiState.glucoseRange,
                            )
                        }
                    }

                    // Gráfico circular con selector de periodo
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = "Lecturas del periodo",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                DetailPeriod.entries.forEach { period ->
                                    FilterChip(
                                        selected = uiState.period == period,
                                        onClick = { viewModel.setPeriod(period) },
                                        label = { Text(period.label) },
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            if (uiState.periodReadings.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "Sin lecturas en este periodo",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    )
                                }
                            } else {
                                GlucoseDonutSection(
                                    readings = uiState.periodReadings,
                                    glucoseRange = uiState.glucoseRange,
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { onEdit(reading.id) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Editar lectura")
                    }
                }
            }
        }
    }
}

/**
 * Barra horizontal dividida en zonas bajo/normal/alto según [glucoseRange].
 * El cursor triangular indica la posición del [valueMgDl] en la escala 40–350 mg/dL.
 */
@Composable
private fun GlucoseStatusBar(valueMgDl: Int, glucoseRange: GlucoseRange) {
    val scaleMin = 40f
    val scaleMax = 350f
    val scaleRange = scaleMax - scaleMin

    val lowFraction = ((glucoseRange.minTarget - scaleMin) / scaleRange).coerceIn(0f, 1f)
    val normalFraction = ((glucoseRange.maxTarget - glucoseRange.minTarget) / scaleRange)
        .coerceIn(0f, 1f - lowFraction)
    val highFraction = (1f - lowFraction - normalFraction).coerceAtLeast(0f)

    // Clampear el valor al rango visual para no dibujar el cursor fuera del Canvas
    val cursorFraction = ((valueMgDl - scaleMin) / scaleRange).coerceIn(0f, 1f)

    Canvas(modifier = Modifier.fillMaxWidth().height(48.dp)) {
        val barH = size.height * 0.5f

        if (lowFraction > 0f) {
            drawRect(
                color = GlucoseLow,
                topLeft = Offset(0f, 0f),
                size = Size(size.width * lowFraction, barH),
            )
        }
        if (normalFraction > 0f) {
            drawRect(
                color = GlucoseNormal,
                topLeft = Offset(size.width * lowFraction, 0f),
                size = Size(size.width * normalFraction, barH),
            )
        }
        if (highFraction > 0f) {
            drawRect(
                color = GlucoseHigh,
                topLeft = Offset(size.width * (lowFraction + normalFraction), 0f),
                size = Size(size.width * highFraction, barH),
            )
        }

        // Cursor: triángulo apuntando hacia arriba desde debajo de la barra
        val cursorX = size.width * cursorFraction
        val triTop = barH + 4.dp.toPx()
        val triHalfW = 8.dp.toPx()
        val triH = 12.dp.toPx()
        val triPath = Path().apply {
            moveTo(cursorX - triHalfW, triTop + triH)
            lineTo(cursorX + triHalfW, triTop + triH)
            lineTo(cursorX, triTop)
            close()
        }
        drawPath(triPath, color = Color(0xFF212121))
    }

    // Etiquetas de rango bajo cada zona
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "< ${glucoseRange.minTarget}",
            style = MaterialTheme.typography.labelSmall,
            color = GlucoseLow,
        )
        Text(
            text = "${glucoseRange.minTarget}–${glucoseRange.maxTarget} mg/dL",
            style = MaterialTheme.typography.labelSmall,
            color = GlucoseNormal,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "> ${glucoseRange.maxTarget}",
            style = MaterialTheme.typography.labelSmall,
            color = GlucoseHigh,
        )
    }
}

/** Gráfico donut + leyenda de distribución de lecturas en el periodo. */
@Composable
private fun GlucoseDonutSection(readings: List<GlucoseReading>, glucoseRange: GlucoseRange) {
    val countLow = readings.count { it.valueMgDl < glucoseRange.minTarget }
    val countNormal = readings.count { it.valueMgDl in glucoseRange.minTarget..glucoseRange.maxTarget }
    val countHigh = readings.count { it.valueMgDl > glucoseRange.maxTarget }
    val total = readings.size

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        // Donut reducido a 112dp para que todo encaje sin scroll en pantallas estándar
        DonutChart(
            countLow = countLow,
            countNormal = countNormal,
            countHigh = countHigh,
            total = total,
            modifier = Modifier.size(112.dp),
        )

        // Leyenda lateral
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DonutLegendItem(color = GlucoseLow, label = "Bajo", count = countLow, total = total)
            DonutLegendItem(color = GlucoseNormal, label = "Normal", count = countNormal, total = total)
            DonutLegendItem(color = GlucoseHigh, label = "Alto", count = countHigh, total = total)
            Text(
                text = "$total lecturas",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
    }
}

/** Dibuja los arcos del donut coloreados por zona. */
@Composable
private fun DonutChart(countLow: Int, countNormal: Int, countHigh: Int, total: Int, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (total == 0) return@Canvas

        // strokeW proporcional al tamaño reducido (112dp) para mantener el hueco interior visible
        val strokeW = 30.dp.toPx()
        val diameter = size.minDimension - strokeW
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)

        val sweepLow = countLow * 360f / total
        val sweepNormal = countNormal * 360f / total
        // Último arco usa el resto para evitar huecos por redondeo de float
        val sweepHigh = 360f - sweepLow - sweepNormal

        var startAngle = -90f // 12 en punto

        if (countLow > 0) {
            drawArc(
                color = GlucoseLow,
                startAngle = startAngle,
                sweepAngle = sweepLow,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeW),
            )
            startAngle += sweepLow
        }
        if (countNormal > 0) {
            drawArc(
                color = GlucoseNormal,
                startAngle = startAngle,
                sweepAngle = sweepNormal,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeW),
            )
            startAngle += sweepNormal
        }
        // Guardia de 0.5f para evitar artefactos de un arco invisible por redondeo
        if (countHigh > 0 && sweepHigh > 0.5f) {
            drawArc(
                color = GlucoseHigh,
                startAngle = startAngle,
                sweepAngle = sweepHigh,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeW),
            )
        }
    }
}

@Composable
private fun DonutLegendItem(color: Color, label: String, count: Int, total: Int) {
    val pct = if (total > 0) (count * 100 / total) else 0
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color = color, shape = CircleShape),
        )
        Column {
            Text(
                text = "$label  $pct%",
                style = MaterialTheme.typography.labelMedium,
                color = color,
            )
            Text(
                text = "$count lecturas",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
    }
}
