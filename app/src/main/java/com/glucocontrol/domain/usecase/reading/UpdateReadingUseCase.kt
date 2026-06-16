package com.glucocontrol.domain.usecase.reading

import com.glucocontrol.domain.model.GlucoseReading
import com.glucocontrol.domain.repository.GlucoseRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Actualiza todos los campos de una lectura de glucosa existente.
 *
 * Aplica las mismas validaciones de negocio que [AddReadingUseCase] más
 * la comprobación de que el id sea válido (> 0), garantizando que el
 * registro existe antes de intentar modificarlo.
 */
class UpdateReadingUseCase
@Inject
constructor(private val repository: GlucoseRepository) {
    private val minimumDate = LocalDate.of(2020, 1, 1)

    suspend operator fun invoke(reading: GlucoseReading) {
        require(reading.id > 0) {
            "No se puede actualizar un registro sin id válido (id = ${reading.id})"
        }
        require(reading.valueMgDl > 0) {
            "El valor de glucosa debe ser mayor que cero"
        }
        require(reading.valueMgDl <= 600) {
            "El valor de glucosa supera el máximo admitido (600 mg/dL)"
        }
        require(!reading.date.isBefore(minimumDate)) {
            "No se admiten registros anteriores al 1 de enero de 2020"
        }
        repository.updateReading(reading)
    }
}
