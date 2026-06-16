package com.glucocontrol.domain.usecase.query

import com.glucocontrol.domain.model.GlucoseReading
import com.glucocontrol.domain.repository.GlucoseRepository
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * Devuelve en tiempo real todas las lecturas de la semana que contiene [dateInWeek].
 *
 * La semana se define de lunes a domingo (estándar ISO 8601).
 * Se usa en la vista semanal y para generar las gráficas de tendencia de 7 días.
 *
 * @param dateInWeek Cualquier día dentro de la semana deseada.
 */
class GetWeeklyReadingsUseCase
@Inject
constructor(private val repository: GlucoseRepository) {
    operator fun invoke(dateInWeek: LocalDate): Flow<List<GlucoseReading>> {
        val weekStart = dateInWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = weekStart.plusDays(6) // domingo
        return repository.getReadingsByDateRange(weekStart, weekEnd)
    }
}
