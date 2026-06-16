package com.glucocontrol.domain.repository

import com.glucocontrol.domain.model.GlucoseReading
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Contrato que define las operaciones de persistencia disponibles para la capa de dominio.
 *
 * La implementación concreta reside en [GlucoseRepositoryImpl] (capa de datos).
 * Esta separación permite sustituir la fuente de datos (local → nube) sin
 * modificar ningún caso de uso ni la presentación.
 *
 * Las operaciones de lectura devuelven [Flow] para que la UI reaccione
 * automáticamente a cualquier cambio en la base de datos.
 */
interface GlucoseRepository {
    /** Todas las lecturas de un día concreto, ordenadas por hora ascendente. */
    fun getReadingsByDate(date: LocalDate): Flow<List<GlucoseReading>>

    /**
     * Todas las lecturas dentro de un rango de fechas, ambos extremos incluidos.
     * Utilizado por las vistas semanal y mensual, y por la exportación de reportes.
     */
    fun getReadingsByDateRange(from: LocalDate, to: LocalDate): Flow<List<GlucoseReading>>

    /** Inserta una nueva lectura. Devuelve el id asignado por la base de datos. */
    suspend fun insertReading(reading: GlucoseReading): Long

    /** Actualiza todos los campos de una lectura existente, identificada por su id. */
    suspend fun updateReading(reading: GlucoseReading)

    /** Elimina la lectura con el id indicado. */
    suspend fun deleteReading(id: Long)

    // Devuelve null si el id no existe; evita lanzar excepción en lecturas eliminadas.
    suspend fun getReadingById(id: Long): GlucoseReading?
}
