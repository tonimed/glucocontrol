package com.glucocontrol.data.remote.dto

import com.glucocontrol.domain.model.GlucoseReading
import com.glucocontrol.domain.model.ReadingTag
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime

/** DTO para lecturas leídas desde Supabase (incluye id generado por la BD). */
@Serializable
data class GlucoseReadingDto(
    val id: Long,
    @SerialName("user_id") val userId: String,
    @SerialName("value_mg_dl") val valueMgDl: Int,
    @SerialName("date_epoch_day") val dateEpochDay: Long,
    @SerialName("time_seconds_of_day") val timeSecondsOfDay: Int? = null,
    val tag: String = ReadingTag.OTRO.name,
    val notes: String? = null,
) {
    fun toDomain() = GlucoseReading(
        id = id,
        valueMgDl = valueMgDl,
        date = LocalDate.ofEpochDay(dateEpochDay),
        time = timeSecondsOfDay?.let { LocalTime.ofSecondOfDay(it.toLong()) },
        // Enum desconocido cae a OTRO para no romper lecturas antiguas
        tag = runCatching { ReadingTag.valueOf(tag) }.getOrDefault(ReadingTag.OTRO),
        notes = notes,
    )
}

/** DTO para INSERT: sin id (lo genera Supabase con BIGSERIAL). */
@Serializable
data class GlucoseReadingCreateDto(
    @SerialName("user_id") val userId: String,
    @SerialName("value_mg_dl") val valueMgDl: Int,
    @SerialName("date_epoch_day") val dateEpochDay: Long,
    @SerialName("time_seconds_of_day") val timeSecondsOfDay: Int? = null,
    val tag: String,
    val notes: String? = null,
) {
    companion object {
        fun fromDomain(reading: GlucoseReading, userId: String) = GlucoseReadingCreateDto(
            userId = userId,
            valueMgDl = reading.valueMgDl,
            dateEpochDay = reading.date.toEpochDay(),
            timeSecondsOfDay = reading.time?.toSecondOfDay(),
            tag = reading.tag.name,
            notes = reading.notes,
        )
    }
}

/** DTO para UPDATE: incluye id para que PostgREST identifique la fila. */
@Serializable
data class GlucoseReadingUpdateDto(
    val id: Long,
    @SerialName("user_id") val userId: String,
    @SerialName("value_mg_dl") val valueMgDl: Int,
    @SerialName("date_epoch_day") val dateEpochDay: Long,
    @SerialName("time_seconds_of_day") val timeSecondsOfDay: Int? = null,
    val tag: String,
    val notes: String? = null,
) {
    companion object {
        fun fromDomain(reading: GlucoseReading, userId: String) = GlucoseReadingUpdateDto(
            id = reading.id,
            userId = userId,
            valueMgDl = reading.valueMgDl,
            dateEpochDay = reading.date.toEpochDay(),
            timeSecondsOfDay = reading.time?.toSecondOfDay(),
            tag = reading.tag.name,
            notes = reading.notes,
        )
    }
}
