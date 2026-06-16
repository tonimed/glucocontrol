package com.glucocontrol.domain.model

/**
 * Define el rango objetivo de glucosa configurado por el usuario.
 *
 * Se usa para:
 *   - Pintar franjas de color en las gráficas (zonas de normo/hiper/hipoglucemia).
 *   - Clasificar visualmente cada lectura al renderizar las tarjetas.
 *
 * Valores expresados en mg/dL. Los valores por defecto reflejan el rango
 * estándar recomendado para personas sin diabetes (70–140 mg/dL).
 */
data class GlucoseRange(val minTarget: Int = 70, val maxTarget: Int = 140)
