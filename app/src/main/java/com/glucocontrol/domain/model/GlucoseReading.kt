package com.glucocontrol.domain.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * Modelo de dominio que representa una medición de glucosa en sangre.
 *
 * Es la única fuente de verdad para la lógica de negocio.
 * La capa de datos transforma esta clase hacia/desde [GlucoseReadingEntity].
 * Este modelo no contiene ninguna dependencia de Android.
 *
 * @param id        Identificador único. Valor 0 indica que el registro aún no existe en BD.
 * @param date      Fecha de la medición.
 * @param time      Hora de la medición. Nullable para registros históricos donde no se conoce la hora.
 * @param valueMgDl Valor de glucosa en mg/dL.
 * @param tag       Etiqueta de contexto clínico (ayunas, post-comida, etc.).
 * @param notes     Texto libre para anotar anomalías (estrés, ejercicio, tipo de comida). Nullable.
 */
data class GlucoseReading(
    val id: Long = 0,
    val date: LocalDate,
    val time: LocalTime? = null,
    val valueMgDl: Int,
    val tag: ReadingTag = ReadingTag.OTRO,
    val notes: String? = null,
)
