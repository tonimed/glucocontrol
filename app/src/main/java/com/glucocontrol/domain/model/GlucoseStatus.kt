package com.glucocontrol.domain.model

/**
 * Clasificación clínica de una lectura de glucosa respecto al rango objetivo del usuario.
 * Se calcula siempre a partir de [GlucoseRange] para garantizar coherencia visual con Historial.
 */
enum class GlucoseStatus { LOW, NORMAL, HIGH }

fun GlucoseReading.glucoseStatus(range: GlucoseRange): GlucoseStatus = when {
    valueMgDl < range.minTarget -> GlucoseStatus.LOW
    valueMgDl > range.maxTarget -> GlucoseStatus.HIGH
    else -> GlucoseStatus.NORMAL
}
