package com.glucocontrol.domain.usecase.reading

import com.glucocontrol.domain.model.GlucoseReading
import com.glucocontrol.domain.repository.GlucoseRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Inserta una nueva lectura de glucosa en la base de datos.
 *
 * Aplica las reglas de negocio definidas en el documento de análisis:
 *   - El valor de glucosa debe estar entre 1 y 600 mg/dL.
 *   - La fecha no puede ser anterior al 1 de enero de 2020 (límite histórico del sistema).
 *   - La fecha no puede ser futura (una lectura no medida aún no tiene valor clínico).
 */
class AddReadingUseCase
@Inject
constructor(private val repository: GlucoseRepository) {
    private val minimumDate = LocalDate.of(2020, 1, 1)

    suspend operator fun invoke(reading: GlucoseReading): Long {
        require(reading.valueMgDl > 0) {
            "El valor de glucosa debe ser mayor que cero"
        }
        require(reading.valueMgDl <= 600) {
            "El valor de glucosa supera el máximo admitido (600 mg/dL)"
        }
        require(!reading.date.isBefore(minimumDate)) {
            "No se admiten registros anteriores al 1 de enero de 2020"
        }
        require(!reading.date.isAfter(LocalDate.now())) {
            "No se pueden registrar lecturas con fecha futura"
        }
        return repository.insertReading(reading)
    }
}
