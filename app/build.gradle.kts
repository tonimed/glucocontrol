import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose) // habilita el compilador de Compose en Kotlin 2.x
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.hilt.android)
}

// Carga credenciales de firma desde keystore.properties (fuera del código fuente)
val keystoreProps = Properties().apply {
    val propsFile = rootProject.file("keystore.properties")
    if (propsFile.exists()) propsFile.inputStream().use { load(it) }
}

android {
    namespace = "com.glucocontrol"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.glucocontrol"
        minSdk = 26 // java.time.* requiere API 26+
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProps.getProperty("storeFile"))
            storePassword = keystoreProps.getProperty("storePassword")
            keyAlias = keystoreProps.getProperty("keyAlias")
            keyPassword = keystoreProps.getProperty("keyPassword")
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
        // Alinear con el JDK 21 del JBR de Android Studio (ver gradle.properties)
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true // activa el procesamiento de @Composable por AGP
    }

    testOptions {
        // Habilita JUnit 5 para los unit tests locales (test/)
        unitTests.all { test ->
            test.useJUnitPlatform()
        }
    }
}

// Kotlin compiler target: equivalente al antiguo kotlinOptions.jvmTarget en Kotlin 2.3+
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

// Room: directorio donde KSP escribe los archivos JSON del esquema de BD.
// Necesario porque GlucoDatabase tiene exportSchema = true.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
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
    implementation(libs.compose.icons)        // Icons.Default.* y Extended
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.preview)

    // ── Hilt ─────────────────────────────────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // ── Room ─────────────────────────────────────────────────────────────────
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler) // genera el código DAO en tiempo de compilación

    // ── WorkManager + notificaciones ────────────────────────────────────────
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler) // genera la factory para @HiltWorker

    // ── Coroutines ───────────────────────────────────────────────────────────
    implementation(libs.kotlinx.coroutines.android)

    // ── Unit tests (JVM — sin dispositivo) ───────────────────────────────────
    testImplementation(libs.junit5.api)
    testImplementation(libs.junit5.params)
    testRuntimeOnly(libs.junit5.engine)
    testRuntimeOnly(libs.junit5.launcher) // alinea versiones Launcher/Engine en classpath
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)

    // ── Instrumented tests (emulador/dispositivo) ─────────────────────────────
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
