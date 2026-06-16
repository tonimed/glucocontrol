package com.glucocontrol.domain.usecase.query

import com.glucocontrol.domain.model.GlucoseReading
import com.glucocontrol.domain.repository.GlucoseRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.Month
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * Devuelve en tiempo real todas las lecturas de un mes completo.
 *
 * Calcula automáticamente el primer y último día del mes indicado,
 * incluyendo años bisiestos (febrero tiene 29 días en años bisiestos).
 *
 * Se usa en la vista mensual y para generar gráficas de tendencia de 30/31 días.
 */
class GetMonthlyReadingsUseCase
@Inject
constructor(private val repository: GlucoseRepository) {
    operator fun invoke(year: Int, month: Month): Flow<List<GlucoseReading>> {
        val start = LocalDate.of(year, month, 1)
        val end = start.with(TemporalAdjusters.lastDayOfMonth())
        return repository.getReadingsByDateRange(start, end)
    }
}
