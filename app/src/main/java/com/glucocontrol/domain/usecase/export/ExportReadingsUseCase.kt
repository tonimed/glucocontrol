package com.glucocontrol.domain.usecase.export

import com.glucocontrol.domain.model.GlucoseReading
import com.glucocontrol.domain.repository.GlucoseRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

/**
 * Recopila las lecturas de un rango de fechas para su exportación.
 *
 * Esta clase solo obtiene y valida los datos; la generación del archivo
 * (CSV o PDF) se delega a los exportadores de la capa de datos
 * (CsvExporter / PdfExporter), que el ViewModel invoca con la lista resultante.
 *
 * Devuelve una [List] en lugar de un [Flow] porque la exportación es una acción
 * imperativa puntual, no una suscripción reactiva continua.
 */
class ExportReadingsUseCase
@Inject
constructor(private val repository: GlucoseRepository) {
    suspend operator fun invoke(from: LocalDate, to: LocalDate): List<GlucoseReading> {
        require(!from.isAfter(to)) {
            "La fecha de inicio ($from) no puede ser posterior a la fecha de fin ($to)"
        }
        return repository.getReadingsByDateRange(from, to).first()
    }
}
