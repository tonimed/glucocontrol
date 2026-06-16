package com.glucocontrol.domain.usecase.reading

import com.glucocontrol.domain.model.GlucoseReading
import com.glucocontrol.domain.repository.GlucoseRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * Devuelve en tiempo real todas las lecturas comprendidas en un rango de fechas.
 *
 * Se usa como base para las consultas de exportación y para filtros personalizados
 * en la pantalla de historial.
 *
 * Devuelve [Flow] para que la UI reaccione automáticamente si los datos cambian
 * mientras la pantalla está activa.
 */
class GetReadingsByDateRangeUseCase
@Inject
constructor(private val repository: GlucoseRepository) {
    operator fun invoke(from: LocalDate, to: LocalDate): Flow<List<GlucoseReading>> {
        require(!from.isAfter(to)) {
            "La fecha de inicio ($from) no puede ser posterior a la fecha de fin ($to)"
        }
        return repository.getReadingsByDateRange(from, to)
    }
}
