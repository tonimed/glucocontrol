package com.glucocontrol.domain.usecase.query

import com.glucocontrol.domain.model.GlucoseReading
import com.glucocontrol.domain.repository.GlucoseRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * Devuelve en tiempo real todas las lecturas de un día concreto.
 *
 * Usado por la pantalla principal (HomeScreen) para mostrar el resumen diario.
 * Las lecturas llegan ordenadas por hora ascendente, con las que no tienen hora al final.
 */
class GetDailyReadingsUseCase
@Inject
constructor(private val repository: GlucoseRepository) {
    operator fun invoke(date: LocalDate): Flow<List<GlucoseReading>> = repository.getReadingsByDate(date)
}
