package com.glucocontrol.domain.usecase.reading

import com.glucocontrol.domain.repository.GlucoseRepository
import javax.inject.Inject

/**
 * Elimina permanentemente una lectura de glucosa identificada por su id.
 *
 * La operación es irreversible. La UI debe solicitar confirmación antes de invocar
 * este caso de uso (responsabilidad de la capa de presentación).
 */
class DeleteReadingUseCase
@Inject
constructor(private val repository: GlucoseRepository) {
    suspend operator fun invoke(id: Long) {
        require(id > 0) { "El id proporcionado no es válido: $id" }
        repository.deleteReading(id)
    }
}
