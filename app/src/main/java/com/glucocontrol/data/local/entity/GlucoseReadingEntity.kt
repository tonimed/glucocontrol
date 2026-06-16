package com.glucocontrol.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.glucocontrol.domain.model.GlucoseReading
import com.glucocontrol.domain.model.ReadingTag
import java.time.LocalDate
import java.time.LocalTime

/**
 * Representación de una fila en la tabla SQLite "glucose_readings".
 *
 * Room no serializa [LocalDate] ni [LocalTime] de forma nativa, por lo que
 * se almacenan como tipos primitivos:
 *   - [LocalDate]  →  Long  (días desde epoch, via [LocalDate.toEpochDay])
 *   - [LocalTime]  →  Int?  (segundos desde medianoche, via [LocalTime.toSecondOfDay])
 *
 * El mapeo entre esta entidad y el modelo de dominio ocurre en [toDomain] y [fromDomain],
 * de forma que la capa de dominio permanece libre de dependencias de Android o Room.
 */
@Entity(tableName = "glucose_readings")
data class GlucoseReadingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** Fecha almacenada como número de días desde el epoch Unix (1970-01-01). */
    val dateEpochDay: Long,
    /** Segundos transcurridos desde medianoche. Null si el registro histórico no tiene hora. */
    val timeSecondOfDay: Int? = null,
    /** Valor de glucosa en mg/dL. */
    val valueMgDl: Int,
    /**
     * Nombre del enum [ReadingTag] serializado como String.
     * Guardar el nombre (no el ordinal) protege el esquema ante reordenaciones futuras del enum.
     */
    val tag: String,
    /** Notas libres del usuario. Null si no se añadió ninguna. */
    val notes: String? = null,
) {
    /** Convierte la fila de BD al modelo de dominio puro. */
    fun toDomain(): GlucoseReading = GlucoseReading(
        id = id,
        date = LocalDate.ofEpochDay(dateEpochDay),
        time = timeSecondOfDay?.let { LocalTime.ofSecondOfDay(it.toLong()) },
        valueMgDl = valueMgDl,
        tag = ReadingTag.valueOf(tag),
        notes = notes,
    )

    companion object {
        /** Convierte el modelo de dominio a fila de BD lista para persistir. */
        fun fromDomain(reading: GlucoseReading): GlucoseReadingEntity = GlucoseReadingEntity(
            id = reading.id,
            dateEpochDay = reading.date.toEpochDay(),
            timeSecondOfDay = reading.time?.toSecondOfDay(),
            valueMgDl = reading.valueMgDl,
            tag = reading.tag.name,
            notes = reading.notes,
        )
    }
}
