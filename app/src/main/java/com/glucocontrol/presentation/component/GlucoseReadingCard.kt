package com.glucocontrol.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.glucocontrol.domain.model.GlucoseRange
import com.glucocontrol.domain.model.GlucoseReading
import com.glucocontrol.presentation.theme.GlucoseHigh
import com.glucocontrol.presentation.theme.GlucoseLow
import com.glucocontrol.presentation.theme.GlucoseNormal
import java.time.format.DateTimeFormatter

@Composable
fun GlucoseReadingCard(
    reading: GlucoseReading,
    glucoseRange: GlucoseRange,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusColor =
        when {
            reading.valueMgDl < glucoseRange.minTarget -> GlucoseLow
            reading.valueMgDl > glucoseRange.maxTarget -> GlucoseHigh
            else -> GlucoseNormal
        }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Valor principal con color de estado
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${reading.valueMgDl}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = statusColor,
                    )
                    Text(
                        text = " mg/dL",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(reading.tag.label, style = MaterialTheme.typography.labelMedium) },
                    )
                    reading.time?.let { time ->
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }
                if (!reading.notes.isNullOrBlank()) {
                    Text(
                        text = reading.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 2.dp, bottom = 4.dp),
                    )
                }
            }

            // Acciones separadas del área táctil del Card
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
