package com.glucocontrol.domain.model

/**
 * Categorías clínicas que contextualizan el momento de la medición.
 * Se usan para dar relevancia médica al valor numérico de glucosa.
 *
 * [label] es el texto visible en la UI.
 */
enum class ReadingTag(val label: String) {
    AYUNAS("Ayunas"),
    PRE_COMIDA("Pre-comida"),
    POST_COMIDA("Post-comida"),
    ANTES_DORMIR("Antes de dormir"),
    OTRO("Otro"),
}
