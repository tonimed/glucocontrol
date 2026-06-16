package com.glucocontrol.domain.usecase.reading

import com.glucocontrol.domain.model.GlucoseReading
import com.glucocontrol.domain.repository.GlucoseRepository
import javax.inject.Inject

/**
 * Recupera una lectura concreta por su id primario.
 *
 * Devuelve null si el id no existe en lugar de lanzar excepción, lo que
 * permite a la UI de edición manejar el caso de lectura eliminada externamente.
 */
class GetReadingByIdUseCase
@Inject
constructor(private val repository: GlucoseRepository) {
    suspend operator fun invoke(id: Long): GlucoseReading? {
        require(id > 0) { "El id debe ser mayor que cero" }
        return repository.getReadingById(id)
    }
}
