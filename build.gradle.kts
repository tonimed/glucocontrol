// Archivo raíz de build. La configuración específica de cada módulo
// vive en su propio build.gradle.kts (ej: app/build.gradle.kts).
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.hilt.android) apply false
}
