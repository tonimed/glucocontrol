import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.serialization) // requerido por kotlinx-serialization en los DTOs
    alias(libs.plugins.hilt.android)
}

// Carga credenciales de firma y configuración de Supabase desde keystore.properties (no versionado)
val keystoreProps = Properties().apply {
    val propsFile = rootProject.file("keystore.properties")
    if (propsFile.exists()) propsFile.inputStream().use { load(it) }
}

android {
    namespace = "com.glucocontrol"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.glucocontrol"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Claves de Supabase y Google inyectadas como BuildConfig — nunca en el código fuente
        buildConfigField("String", "SUPABASE_URL",        "\"${keystoreProps["supabaseUrl"]        ?: ""}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY",   "\"${keystoreProps["supabaseAnonKey"]   ?: ""}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID","\"${keystoreProps["googleWebClientId"] ?: ""}\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProps.getProperty("storeFile") ?: "dummy.jks")
            storePassword = keystoreProps.getProperty("storePassword") ?: ""
            keyAlias = keystoreProps.getProperty("keyAlias") ?: ""
            keyPassword = keystoreProps.getProperty("keyPassword") ?: ""
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true // genera BuildConfig con los campos de Supabase y Google
    }

    testOptions {
        unitTests.all { test ->
            test.useJUnitPlatform()
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

dependencies {
    // ── AndroidX Core ────────────────────────────────────────────────────────
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.activity.ktx)
    implementation(libs.activity.compose)

    // ── Compose (BOM alinea todas las versiones internas de Compose) ──────────
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.icons)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.preview)

    // ── Hilt ─────────────────────────────────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // ── Supabase (BOM alinea postgrest, auth y compose-auth) ─────────────────
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)
    // compose-auth eliminado: ComposeAuth no está instalado en el cliente y causaba
    // interferencia con la autenticación. Se reactivará cuando se configure Google Sign-In.

    // ── Ktor (cliente HTTP requerido por el SDK de Supabase) ──────────────────
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.core)

    // ── Serialización (requerida por PostgREST para los DTOs) ─────────────────
    implementation(libs.kotlinx.serialization.json)

    // ── Google Credential Manager (Google Sign-In nativo en Android) ──────────
    implementation(libs.credentials)
    // credentials-play-services eliminado: registraba un proveedor de credenciales
    // a nivel de sistema que mostraba pop-ups de Google automáticamente.
    // Se reactivará junto con el Google Web Client ID real.
    implementation(libs.googleid)

    // ── WorkManager + notificaciones ────────────────────────────────────────
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // ── Coroutines ───────────────────────────────────────────────────────────
    implementation(libs.kotlinx.coroutines.android)

    // ── Unit tests (JVM — sin dispositivo) ───────────────────────────────────
    testImplementation(libs.junit5.api)
    testImplementation(libs.junit5.params)
    testRuntimeOnly(libs.junit5.engine)
    testRuntimeOnly(libs.junit5.launcher)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)

    // ── Instrumented tests (emulador/dispositivo) ─────────────────────────────
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
