package com.glucocontrol.presentation.screen.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.glucocontrol.domain.model.GlucoseRange
import com.glucocontrol.presentation.theme.GlucoseHigh
import com.glucocontrol.presentation.theme.GlucoseLow
import com.glucocontrol.presentation.theme.GlucoseNormal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(onBack: () -> Unit, viewModel: ChartViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gráfica") },
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
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(12.dp))

            ChartPeriodSelector(
                current = uiState.period,
                onSelect = { viewModel.setPeriod(it) },
            )

            Spacer(Modifier.height(8.dp))

            ChartPeriodNavigator(
                period = uiState.period,
                referenceDate = uiState.referenceDate,
                onPrevious = { viewModel.previousPeriod() },
                onNext = { viewModel.nextPeriod() },
            )

            Spacer(Modifier.height(12.dp))

            when {
                uiState.isLoading ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }

                uiState.points.isEmpty() ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Sin lecturas en este periodo",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }

                else -> {
                    ChartStatsStrip(points = uiState.points, glucoseRange = uiState.glucoseRange)
                    Spacer(Modifier.height(16.dp))
                    GlucoseLineChart(
                        points = uiState.points,
                        glucoseRange = uiState.glucoseRange,
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartPeriodSelector(current: ChartPeriod, onSelect: (ChartPeriod) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = current == ChartPeriod.WEEKLY,
            onClick = { onSelect(ChartPeriod.WEEKLY) },
            label = { Text("Semana") },
        )
        FilterChip(
            selected = current == ChartPeriod.MONTHLY,
            onClick = { onSelect(ChartPeriod.MONTHLY) },
            label = { Text("Mes") },
        )
    }
}

@Composable
private fun ChartPeriodNavigator(
    period: ChartPeriod,
    referenceDate: LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val label =
        when (period) {
            ChartPeriod.WEEKLY -> {
                val start =
                    referenceDate.with(
                        java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY),
                    )
                val end = start.plusDays(6)
                val fmt = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es"))
                "${start.format(fmt)} – ${end.format(fmt)} ${start.year}"
            }

            ChartPeriod.MONTHLY -> {
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
        Text(label, style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Periodo siguiente")
        }
    }
}

@Composable
private fun ChartStatsStrip(points: List<ChartPoint>, glucoseRange: GlucoseRange) {
    val values = points.map { it.avgMgDl }
    val avg = values.average().roundToInt()
    val min = values.min().roundToInt()
    val max = values.max().roundToInt()
    val inRange = values.count { it >= glucoseRange.minTarget && it <= glucoseRange.maxTarget }
    val pct = (inRange * 100.0 / values.size).roundToInt()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatItem("Media", "$avg mg/dL")
            StatItem("Mín", "$min mg/dL")
            StatItem("Máx", "$max mg/dL")
            StatItem("En rango", "$pct%")
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun GlucoseLineChart(points: List<ChartPoint>, glucoseRange: GlucoseRange, modifier: Modifier = Modifier) {
    // Reutilizar los colores semánticos del tema para garantizar contraste sobre fondo blanco
    val colorLow = GlucoseLow // naranja oscuro #E65100 — contraste 3.3:1
    val colorNormal = GlucoseNormal // verde oscuro   #2E7D32 — contraste 5.1:1
    val colorHigh = GlucoseHigh // rojo oscuro    #C62828 — contraste 5.4:1
    val colorLine = Color(0xFF1565C0)
    val colorAxis = Color(0xFF616161) // gris oscuro — contraste 5.9:1 sobre blanco

    // Rango Y fijo con margen para no saturar
    val yMin = 40f
    val yMax = 350f

    val xLabelFmt = DateTimeFormatter.ofPattern("d/M")
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))

    Canvas(modifier = modifier) {
        val leftMargin = 52f
        val rightMargin = 16f
        val topMargin = 16f
        val bottomMargin = 36f

        val chartW = size.width - leftMargin - rightMargin
        val chartH = size.height - topMargin - bottomMargin

        // Mapea índice → pixel X, valor glucosa → pixel Y (clamped)
        fun xOf(index: Int): Float = leftMargin + index.toFloat() / (points.size - 1).coerceAtLeast(1) * chartW

        fun yOf(value: Float): Float = topMargin + chartH * (1f - ((value - yMin) / (yMax - yMin)).coerceIn(0f, 1f))

        val yLow = yOf(glucoseRange.minTarget.toFloat())
        val yHigh = yOf(glucoseRange.maxTarget.toFloat())

        // Zonas de color de fondo (baja / normal / alta)
        drawRect(
            color = colorLow,
            topLeft = Offset(leftMargin, yLow),
            size = Size(chartW, topMargin + chartH - yLow),
            alpha = 0.07f,
        )
        drawRect(
            color = colorNormal,
            topLeft = Offset(leftMargin, yHigh),
            size = Size(chartW, yLow - yHigh),
            alpha = 0.07f,
        )
        drawRect(
            color = colorHigh,
            topLeft = Offset(leftMargin, topMargin),
            size = Size(chartW, yHigh - topMargin),
            alpha = 0.07f,
        )

        // Líneas de referencia punteadas en los umbrales objetivo
        drawLine(
            color = colorNormal,
            start = Offset(leftMargin, yLow),
            end = Offset(leftMargin + chartW, yLow),
            strokeWidth = 1.5f,
            pathEffect = dashEffect,
        )
        drawLine(
            color = colorHigh,
            start = Offset(leftMargin, yHigh),
            end = Offset(leftMargin + chartW, yHigh),
            strokeWidth = 1.5f,
            pathEffect = dashEffect,
        )

        // Cuadrícula horizontal
        val ySteps = listOf(80, 120, 180, 250, 300)
        ySteps.forEach { step ->
            val y = yOf(step.toFloat())
            if (y > topMargin && y < topMargin + chartH) {
                drawLine(
                    color = colorAxis.copy(alpha = 0.18f),
                    start = Offset(leftMargin, y),
                    end = Offset(leftMargin + chartW, y),
                    strokeWidth = 1f,
                )
            }
        }

        // Polilínea de medias diarias
        if (points.size >= 2) {
            val linePath = Path()
            points.forEachIndexed { i, p ->
                val x = xOf(i)
                val y = yOf(p.avgMgDl)
                if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
            }
            drawPath(linePath, color = colorLine, style = Stroke(width = 3f))
        }

        // Puntos de datos coloreados por rango
        points.forEachIndexed { i, p ->
            val x = xOf(i)
            val y = yOf(p.avgMgDl)
            val dotColor = when {
                p.avgMgDl < glucoseRange.minTarget.toFloat() -> colorLow
                p.avgMgDl > glucoseRange.maxTarget.toFloat() -> colorHigh
                else -> colorNormal
            }
            drawCircle(color = dotColor, radius = 8f, center = Offset(x, y))
            drawCircle(color = Color.White, radius = 4f, center = Offset(x, y))
        }

        // Ejes
        drawLine(
            color = colorAxis.copy(alpha = 0.4f),
            start = Offset(leftMargin, topMargin),
            end = Offset(
                leftMargin,
                topMargin + chartH,
            ),
            strokeWidth = 1.5f,
        )
        drawLine(
            color = colorAxis.copy(alpha = 0.4f),
            start = Offset(leftMargin, topMargin + chartH),
            end = Offset(leftMargin + chartW, topMargin + chartH),
            strokeWidth = 1.5f,
        )

        // Etiquetas de texto (require nativeCanvas para drawText)
        drawIntoCanvas { canvas ->
            val yPaint =
                android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(180, 117, 117, 117)
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.RIGHT
                    isAntiAlias = true
                }
            ySteps.forEach { step ->
                val y = yOf(step.toFloat())
                if (y > topMargin && y < topMargin + chartH) {
                    canvas.nativeCanvas.drawText("$step", leftMargin - 6f, y + 9f, yPaint)
                }
            }

            val xPaint =
                android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(180, 117, 117, 117)
                    textSize = 26f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
            // Reduce densidad de etiquetas para periodos largos
            val labelStep = if (points.size > 14) {
                3
            } else if (points.size > 7) {
                2
            } else {
                1
            }
            points.forEachIndexed { i, p ->
                if (i % labelStep == 0) {
                    canvas.nativeCanvas.drawText(
                        p.date.format(xLabelFmt),
                        xOf(i),
                        topMargin + chartH + 28f,
                        xPaint,
                    )
                }
            }
        }
    }
}
