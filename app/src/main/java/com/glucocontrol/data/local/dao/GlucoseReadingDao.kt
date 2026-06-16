package com.glucocontrol.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.glucocontrol.data.local.entity.GlucoseReadingEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para la tabla "glucose_readings".
 *
 * Room valida todas las queries anotadas con [@Query] en tiempo de compilación,
 * lo que garantiza que los errores de SQL se detecten antes de ejecutar la app.
 *
 * Las queries de lectura devuelven [Flow]: la UI se actualiza automáticamente
 * cada vez que los datos subyacentes cambian, sin necesidad de polling manual.
 */
@Dao
interface GlucoseReadingDao {
    /**
     * Todas las lecturas de un día concreto.
     *
     * El CASE WHEN maneja el orden de filas con hora nula de forma compatible
     * con todas las versiones de SQLite presentes en Android (alternativa a NULLS LAST,
     * que solo está disponible a partir de SQLite 3.30.0 / Android 11).
     */
    @Query(
        """
        SELECT * FROM glucose_readings
        WHERE dateEpochDay = :epochDay
        ORDER BY
            CASE WHEN timeSecondOfDay IS NULL THEN 1 ELSE 0 END ASC,
            timeSecondOfDay ASC
        """,
    )
    fun getByDate(epochDay: Long): Flow<List<GlucoseReadingEntity>>

    /**
     * Todas las lecturas dentro de un rango de fechas (ambos extremos incluidos).
     * Usada para las vistas semanal/mensual y la exportación de reportes.
     */
    @Query(
        """
        SELECT * FROM glucose_readings
        WHERE dateEpochDay BETWEEN :fromEpochDay AND :toEpochDay
        ORDER BY
            dateEpochDay ASC,
            CASE WHEN timeSecondOfDay IS NULL THEN 1 ELSE 0 END ASC,
            timeSecondOfDay ASC
        """,
    )
    fun getByDateRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<GlucoseReadingEntity>>

    /**
     * Inserta una lectura nueva.
     * [OnConflictStrategy.REPLACE] permite reutilizar este método también para updates
     * cuando se pasa un id existente (lo que simplifica el repositorio).
     * Devuelve el id asignado por SQLite.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GlucoseReadingEntity): Long

    /**
     * Actualiza todos los campos de una lectura existente.
     * Room identifica la fila por la clave primaria [GlucoseReadingEntity.id].
     */
    @Update
    suspend fun update(entity: GlucoseReadingEntity)

    /**
     * Recupera una lectura concreta por su clave primaria.
     * Devuelve null si el id no existe (más seguro que lanzar excepción).
     */
    @Query("SELECT * FROM glucose_readings WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): GlucoseReadingEntity?

    /**
     * Elimina una lectura por su id.
     * Se usa [@Query] para evitar construir un objeto Entity solo para borrar.
     */
    @Query("DELETE FROM glucose_readings WHERE id = :id")
    suspend fun deleteById(id: Long)
}
