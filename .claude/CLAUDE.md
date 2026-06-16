# CLAUDE

## Proyecto

GlucoControl — aplicación Android para registro y seguimiento de glucosa en sangre.

## Arquitectura

**Clean Architecture + MVVM** con tres capas estrictas:

- `domain/` — Kotlin puro, sin dependencias Android. Modelos, interfaces de repositorio, casos de uso.
- `data/` — Room, Hilt. Implementaciones de repositorio, entidades, DAOs, base de datos.
- `presentation/` — Jetpack Compose, ViewModels. Pantallas, componentes, tema, navegación.

Reglas de capas:

- `domain/` no importa nada de `data/` ni de Android SDK.
- El mapeo Entity ↔ Domain ocurre únicamente en `data/repository/`.
- Los ViewModels solo llaman casos de uso, nunca al repositorio directamente.

## Stack tecnológico

- **Lenguaje:** Kotlin 2.2.21
- **UI:** Jetpack Compose + Material3 (BOM 2026.05.01)
- **BD:** Room 2.8.4 con KSP 2.2.21-2.0.5 (no KAPT)
- **DI:** Hilt 2.56.2
- **Navegación:** Navigation Compose 2.9.8 con `hilt-navigation-compose` 1.3.0
- **Lifecycle:** `lifecycle-viewmodel-compose` + `lifecycle-runtime-compose` 2.10.0
- **Coroutines:** kotlinx.coroutines 1.10.2 — `Flow` para queries, `suspend` para mutaciones
- **Fechas:** `java.time.*` — `LocalDate`/`LocalTime` almacenadas como primitivos (epoch days / seconds of day)
- **WorkManager:** `work-runtime-ktx` 2.10.1 + `hilt-work` 1.2.0
- **Tests unitarios:** JUnit 5.12.2 + MockK + `runTest`
- **Tests instrumentados:** JUnit 4 + AndroidJUnit4 + Room in-memory

## Añadir comentarios

Añadir comentarios que ayuden a entender el código y su lógica:

- Explicar el **por qué**, no el qué (el qué lo dicen los nombres).
- Documentar restricciones no obvias, invariantes sutiles, workarounds.
- Un comentario corto por bloque lógico es suficiente; evitar bloques multilinea.

## Comandos de build y tests

```powershell
# Configurar entorno antes de correr Gradle
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "C:\Users\toni_\AppData\Local\Android\Sdk"

# Unit tests (JVM, sin dispositivo)
.\gradlew.bat :app:testDebugUnitTest

# APK release firmado (keystore: glucocontrol-release.jks / keystore.properties)
.\gradlew.bat :app:assembleRelease
# Salida: app/build/outputs/apk/release/app-release.apk

# Linter
java -jar ktlint-1.8.0.jar --format "app/src/**/*.kt"
java -jar ktlint-1.8.0.jar "app/src/**/*.kt"   # verificación

# Tests instrumentados (requiere emulador/dispositivo)
.\gradlew.bat :app:connectedDebugAndroidTest
```

## Restricciones importantes

- `minSdk = 26` — `java.time.*` está disponible desde API 26; no usar `Calendar` ni `Date`.
- Fechas válidas para lecturas: desde 2020-01-01 hasta hoy (validado en `AddReadingUseCase`).
- Glucosa válida: 1–600 mg/dL.
- SQL compatible con todas las versiones de SQLite en Android: no usar `NULLS LAST`; usar `CASE WHEN IS NULL THEN 1 ELSE 0 END`.
- Room con `exportSchema = true` → los JSON de esquema se generan en `app/schemas/`.
- Hilt 2.56.2 no soporta Kotlin 2.3.x ni AGP 9.x (ver `memory/project_versions.md`).
- `@OptIn(ExperimentalCoroutinesApi::class)` debe anotarse a nivel de **clase**, no en bloques `init`.
- Las propiedades `_foo: MutableStateFlow` sin propiedad pública `foo` correspondiente deben nombrarse sin guión bajo (ej. `fooFlow`) para cumplir ktlint `backing-property-naming`.
- DatePicker epoch millis: `date.toEpochDay() * 86_400_000L` para UTC (evita problemas de zona horaria con `java.time`).
- Canvas `Stroke`: el parámetro es `width`, no `strokeWidth` → `Stroke(width = 3f)`.
- Canvas texto nativo: `canvas.nativeCanvas` requiere `import androidx.compose.ui.graphics.nativeCanvas` explícito.
- Colores del canvas de la gráfica: usar `GlucoseLow/Normal/High` de `theme/Color.kt`, nunca literales hex, para garantizar contraste sobre fondo blanco.
- WorkManager + Hilt: `GlucoControlApp` implementa `Configuration.Provider`; el `InitializationProvider` de WorkManager debe desactivarse en el Manifest (`tools:node="remove"`).

## Convenciones de código

- Trailing comma en listas multilínea (requisito ktlint).
- `enum` con `val label: String` en español para mostrar al usuario.
- Repositorio siempre devuelve `Flow` en queries y `suspend` en mutaciones.
- Los casos de uso son clases con un solo método `invoke` anotado con `operator fun`.
- No usar `@Suppress` para ocultar errores reales; resolver la causa raíz.
- Funciones `@Composable` en PascalCase — permitido por `.editorconfig` (`ktlint_function_naming_ignore_when_annotated_with = Composable`).
- Íconos con soporte RTL: `Icons.AutoMirrored.Filled.*` en lugar de `Icons.Default.KeyboardArrow*` y `Icons.Default.ShowChart`.
- Locale en español: `Locale.forLanguageTag("es")` (no `Locale("es")`).

## Convenciones de presentación

- Responsive: `LocalConfiguration.current.screenWidthDp >= 600` → `NavigationRail` (tablet) vs `BottomNavigationBar` (móvil).
- `AppBottomBar` es visible en **todas** las pantallas (no solo en Home e History) para que el usuario pueda volver al dashboard desde cualquier lugar.
- Navegación tipada con sealed class `Screen`; los argumentos Long se pasan en la ruta (`"add_edit/{readingId}"`).
- `readingId = -1L` → pantalla de nueva lectura; `readingId > 0` → edición.
- Todas las pantallas secundarias (AddEdit, History, Chart) reciben `onBack: () -> Unit` como primer parámetro y muestran `Icons.AutoMirrored.Filled.ArrowBack` en el `TopAppBar` con `navigationIconContentColor = onPrimary`.
- ViewModels con `@HiltViewModel` + `@Inject constructor` + `SavedStateHandle` para leer args de navegación.
- Estados de UI como `data class *UiState` con `isLoading = true` como valor inicial.
- `flatMapLatest` + `combine` para cancelar suscripciones anteriores al cambiar filtros/fechas.
- Tema: `background = Color.White` en `LightColorScheme`; `surface = Color.White`. El `DarkColorScheme` mantiene su propio fondo oscuro.

## Fases implementadas

1. ✅ Base de datos (Room): entidades, DAOs, `GlucoDatabase`
2. ✅ Capa `domain/usecase`: 9 casos de uso + tests unitarios (todos pasan)
3. ✅ Gradle build system: `settings.gradle.kts`, `libs.versions.toml`, `app/build.gradle.kts`
4. ✅ Capa `di/`: `AppModule`, `DatabaseModule`, `RepositoryModule`
5. ✅ Capa `presentation/`: dashboard, historial, gráfica Canvas, formulario, navegación responsive
6. ✅ `data/export/`: `CsvExporter` (RFC 4180), `PdfExporter` (Android PdfDocument, A4)
7. ✅ `notification/`: `GlucoseReminderWorker` (HiltWorker), `ReminderScheduler` (WorkManager periódico)

## Árbol de archivos relevantes

```text
app/src/main/java/com/glucocontrol/
├── MainActivity.kt
├── GlucoControlApplication.kt          (Configuration.Provider para WorkManager+Hilt,
│                                        canal de notificaciones CHANNEL_ID)
├── data/
│   ├── export/
│   │   ├── CsvExporter.kt              (RFC 4180, UTF-8)
│   │   └── PdfExporter.kt              (A4 595×842 pts, paginación automática)
│   ├── local/
│   │   ├── dao/GlucoseReadingDao.kt
│   │   ├── database/GlucoDatabase.kt
│   │   └── entity/GlucoseReadingEntity.kt
│   └── repository/GlucoseRepositoryImpl.kt
├── di/
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   └── RepositoryModule.kt
├── domain/
│   ├── model/          (GlucoseReading, GlucoseRange, ReadingTag)
│   ├── repository/     (GlucoseRepository)
│   └── usecase/
│       ├── query/      (GetDailyReadings, GetWeekly, GetMonthly, GetByDateRange)
│       └── reading/    (AddReading, UpdateReading, DeleteReading, GetReadingById)
├── notification/
│   ├── GlucoseReminderWorker.kt        (@HiltWorker, POST_NOTIFICATIONS API 33+)
│   └── ReminderScheduler.kt            (enqueueUniquePeriodicWork, ExistingPeriodicWorkPolicy.UPDATE)
└── presentation/
    ├── component/
    │   ├── AdaptiveNavigation.kt       (AppBottomBar siempre visible, AppNavigationRail)
    │   └── GlucoseReadingCard.kt
    ├── navigation/
    │   ├── NavGraph.kt                 (GlucoApp, AppNavHost)
    │   └── Screen.kt                   (Home, History, AddEditReading, Chart)
    ├── screen/
    │   ├── addreading/                 (AddEditReadingScreen, AddEditReadingViewModel)
    │   ├── chart/                      (ChartScreen, ChartViewModel)
    │   │                                ChartPeriod {WEEKLY, MONTHLY}
    │   │                                ChartPoint(date, avgMgDl) — media diaria
    │   ├── history/                    (HistoryScreen, HistoryViewModel)
    │   │                                HistoryPeriod {DAILY, WEEKLY, MONTHLY}
    │   └── home/                       (HomeScreen — dashboard, HomeViewModel)
    └── theme/
        ├── Color.kt                    (GlucoseLow/Normal/High, background=White)
        ├── Theme.kt                    (GlucoControlTheme)
        └── Type.kt
```
