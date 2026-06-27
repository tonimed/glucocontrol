# CLAUDE

## Proyecto

GlucoControl — aplicación Android para registro y seguimiento de glucosa en sangre con sincronización en la nube mediante Supabase.

## Arquitectura

**Clean Architecture + MVVM** con tres capas estrictas:

- `domain/` — Kotlin puro, sin dependencias Android. Modelos, interfaces de repositorio, casos de uso.
- `data/` — Supabase, Hilt. Implementaciones de repositorio, DTOs, cliente HTTP, proveedor Supabase.
- `presentation/` — Jetpack Compose, ViewModels. Pantallas, componentes, tema, navegación.

Reglas de capas:

- `domain/` no importa nada de `data/` ni de Android SDK.
- El mapeo DTO ↔ Domain ocurre únicamente en `data/repository/`.
- Los ViewModels solo llaman casos de uso, nunca al repositorio directamente.

## Stack tecnológico

- **Lenguaje:** Kotlin 2.2.21
- **UI:** Jetpack Compose + Material3 (BOM 2026.05.01)
- **Backend:** Supabase BOM 2.5.4 — `gotrue-kt` (Auth) + `postgrest-kt` (base de datos)
- **HTTP:** Ktor `ktor-client-android` (requerido por el SDK de Supabase)
- **Serialización:** `kotlinx.serialization-json` (DTOs PostgREST)
- **DI:** Hilt 2.56.2
- **Navegación:** Navigation Compose 2.9.8 con `hilt-navigation-compose` 1.3.0
- **Lifecycle:** `lifecycle-viewmodel-compose` + `lifecycle-runtime-compose` 2.10.0
- **Coroutines:** kotlinx.coroutines 1.10.2 — `Flow` para queries, `suspend` para mutaciones
- **Fechas:** `java.time.*` — `LocalDate`/`LocalTime` almacenadas como primitivos (epoch days / seconds of day)
- **WorkManager:** `work-runtime-ktx` 2.10.1 + `hilt-work` 1.2.0
- **Google Sign-In:** `credentials` + `googleid` (pendiente de configurar Web Client ID)
- **Tests unitarios:** JUnit 5.12.2 + MockK + `runTest`

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

# Android App Bundle (para Google Play)
.\gradlew.bat :app:bundleRelease
# Salida: app/build/outputs/bundle/release/app-release.aab

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
- Hilt 2.56.2 no soporta Kotlin 2.3.x ni AGP 9.x (ver `memory/project_versions.md`).
- `@OptIn(ExperimentalCoroutinesApi::class)` debe anotarse a nivel de **clase**, no en bloques `init`.
- Las propiedades `_foo: MutableStateFlow` sin propiedad pública `foo` correspondiente deben nombrarse sin guión bajo (ej. `fooFlow`) para cumplir ktlint `backing-property-naming`.
- DatePicker epoch millis: `date.toEpochDay() * 86_400_000L` para UTC (evita problemas de zona horaria con `java.time`).
- Canvas `Stroke`: el parámetro es `width`, no `strokeWidth` → `Stroke(width = 3f)`.
- Canvas texto nativo: `canvas.nativeCanvas` requiere `import androidx.compose.ui.graphics.nativeCanvas` explícito.
- Colores del canvas de la gráfica: usar `GlucoseLow/Normal/High` de `theme/Color.kt`, nunca literales hex, para garantizar contraste sobre fondo blanco.
- WorkManager + Hilt: `GlucoControlApp` implementa `Configuration.Provider`; el `InitializationProvider` de WorkManager debe desactivarse en el Manifest (`tools:node="remove"`).
- `MutableInteractionSource.interactions.collect` recibe todos los `Interaction` (Focus, Hover, Press…). En campos `readOnly=true` que abren un picker, filtrar siempre por `is PressInteraction.Press`; de lo contrario el picker se dispara con eventos de scroll o pérdida de foco.
- Tema XML: `Theme.Material3.DayNight.NoActionBar` **NO** está disponible en proyectos Compose puro (requiere la librería View-based). Usar `Theme.AppCompat.DayNight.NoActionBar` como padre en `res/values/themes.xml`.
- Adaptive icon (`mipmap-anydpi-v26/ic_launcher.xml`): el `<foreground>` NO debe referenciar `@mipmap/ic_launcher` (referencia circular en API 26+ — el sistema resuelve `anydpi` con máxima prioridad). Usar `@drawable/ic_app_logo` como foreground.
- `AppTopBar` usa `Surface + Box + Column` con `.statusBarsPadding()` para respetar la barra de estado; no usa `CenterAlignedTopAppBar` (restricción de altura fija 64dp incompatible con imagen encima del texto).
- Imagen en `AppTopBar`: `Modifier.size(72.dp)` + `ContentScale.Fit` — tamaño fijo en ambas dimensiones garantiza que el dibujo no se deforme al girar el teléfono.
- `AppTopBar` acepta `showImage: Boolean = true`; en pantallas donde el espacio vertical es limitado (ej. `ReadingDetailScreen`) usar `showImage = false` para ahorrar ~90 dp.
- Orientación portrait bloqueada globalmente mediante `android:screenOrientation="portrait"` en el `<activity>` del Manifest; **no usar** `DisposableEffect` + `ActivityInfo` por pantalla (el `onDispose` restaura `UNSPECIFIED` y puede afectar otras pantallas).
- `navigateTopLevel` en `NavGraph.kt`: cuando el destino es `Screen.Home` usar `popBackStack(Screen.Home.route, inclusive = false)` en lugar de `navigate + popUpTo`; de lo contrario `launchSingleTop` puede producir un no-op si Home ya es el top de la back stack tras el `popUpTo`.

### Restricciones Supabase SDK 2.x (gotrue-kt / postgrest-kt)

- **Artefacto correcto:** `gotrue-kt` en BOM 2.5.4 (no `auth-kt` — no existe).
- **Clase Auth:** en SDK 2.x la clase se llama `Auth` (no `GoTrue`); la extension property es `supabase.auth` (no `supabase.gotrue`). Paquete: `io.github.jan.supabase.gotrue`.
- **`buildSupabaseClient()`:** el wrapper local se llama así para no colisionar con `createSupabaseClient()` del SDK (mismo nombre → error de compilación si se llama igual).
- **`supabaseUrl`:** debe ser `https://<project>.supabase.co` sin sufijo `/rest/v1/` — el SDK construye las rutas internamente.
- **INSERT sin decode:** `postgrest[TABLE].insert(dto)` ejecuta inmediatamente y retorna body vacío (PostgREST sin `Prefer:return=representation`); la propiedad `returning` es privada en 2.x. No llamar `.decodeSingle()` sobre el resultado del insert o lanzará `JsonDecodingException`.
- **`Returning.REPRESENTATION` privada:** no disponible desde la API pública en postgrest-kt 2.x.
- **`credentials-play-services-auth` eliminado:** registraba un proveedor de credenciales a nivel de sistema que mostraba pop-ups de Google en campos de email. Reactivar solo cuando el Web Client ID real esté configurado.
- **`supabase-compose-auth` eliminado:** `ComposeAuth` no está instalado en el cliente Supabase; la librería no hace nada y puede interferir.
- **Google Sign-In:** detectar placeholder con `!BuildConfig.GOOGLE_WEB_CLIENT_ID.startsWith("000000")`; mostrar error informativo si no está configurado en lugar de llamar al Credential Manager.
- **`signUp` y confirmación de email:** tras `signUpWith(Email)`, llamar a `getCurrentUser()` para verificar si se creó sesión. Si Supabase requiere confirmación de email, no habrá sesión → mostrar aviso informativo, no navegar a Home.
- **Errores en inglés:** Supabase devuelve mensajes en inglés (`"email rate limit exceeded"`, `"invalid login credentials"`…). Traducir en `AuthViewModel.translateError()` antes de mostrar al usuario.
- **`refreshTrigger`:** `GlucoseRepositoryImpl` usa `MutableSharedFlow<Unit>(replay = 1)` para invalidar todos los Flows activos tras cualquier mutación. Tras insert/update/delete llamar siempre a `invalidate()`. Sin esto el Dashboard y el Historial no se actualizan hasta que el usuario navega fuera y vuelve.

### Credenciales en keystore.properties

```properties
storeFile=../glucocontrol-release.jks
storePassword=...
keyAlias=...
keyPassword=...
supabaseUrl=https://<project>.supabase.co
supabaseAnonKey=<anon-key>
googleWebClientId=<web-client-id>.apps.googleusercontent.com
```

`keystore.properties` y `*.jks` nunca deben commitearse al repositorio.

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
- `AppBottomBar` es visible en **todas las pantallas excepto `AuthScreen`** — se oculta con `currentBackStackEntryAsState()` comprobando `route != Screen.Auth.route`.
- La app arranca en `Screen.Auth`; tras login exitoso navega a `Screen.Home` con `popUpTo(Auth, inclusive=true)` para que "Atrás" no vuelva al login.
- `AuthViewModel` se crea con scope de Activity (`hiltViewModel(activity)`) para compartir estado de sesión entre `AuthScreen` y `HomeScreen` (logout).
- Navegación tipada con sealed class `Screen`; los argumentos Long se pasan en la ruta (`"add_edit/{readingId}"`).
- `readingId = -1L` → pantalla de nueva lectura; `readingId > 0` → edición.
- Todas las pantallas secundarias (AddEdit, History, Chart, ReadingDetail) pasan `onBack: () -> Unit` a `AppTopBar`; HomeScreen pasa `onBack = null` (sin botón de retroceso).
- `AppTopBar` es el componente compartido de barra superior: imagen `ic_app_logo` (72dp) centrada encima del título, botón ← superpuesto a la izquierda cuando `onBack != null`.
- ViewModels con `@HiltViewModel` + `@Inject constructor` + `SavedStateHandle` para leer args de navegación.
- Estados de UI como `data class *UiState` con `isLoading = true` como valor inicial.
- `flatMapLatest` + `combine` para cancelar suscripciones anteriores al cambiar filtros/fechas.
- Tema: `background = Color.White` en `LightColorScheme`; `surface = Color.White`. El `DarkColorScheme` mantiene su propio fondo oscuro.
- Nombre de la app en todas las vistas: "Control de Glucosa" (`strings.xml` + título del `AppTopBar`).

## Fases implementadas

1. ✅ ~~Base de datos Room~~ → **migrado a Supabase** (PostgREST + Auth)
2. ✅ Capa `domain/usecase`: 9 casos de uso de glucosa + 5 de autenticación + tests unitarios
3. ✅ Gradle build system: `settings.gradle.kts`, `libs.versions.toml`, `app/build.gradle.kts`
4. ✅ Capa `di/`: `AppModule`, `SupabaseModule`, `RepositoryModule`
5. ✅ Capa `presentation/`: dashboard, historial, gráfica Canvas, formulario, navegación responsive
6. ✅ `data/export/`: `CsvExporter` (RFC 4180), `PdfExporter` (Android PdfDocument, A4)
7. ✅ `notification/`: `GlucoseReminderWorker` (HiltWorker), `ReminderScheduler` (WorkManager periódico)
8. ✅ Recursos visuales: icono personalizado (mipmap 5 densidades + adaptive icon), `AppTopBar` con imagen+texto, tema `Theme.GlucoControl`, título "Control de Glucosa"
9. ✅ `presentation/screen/readingdetail/`: `ReadingDetailScreen` (barra de estado coloreada + donut de distribución por periodo), `ReadingDetailViewModel`, `DetailPeriod` (3 días / semana / mes); `GlucoseStatus` en `domain/model/`
10. ✅ **Supabase cloud + Auth:** `data/remote/` (DTOs, cliente), `AuthRepositoryImpl`, `SupabaseModule`, `AuthScreen`/`AuthViewModel`; `refreshTrigger` en repositorio para actualizaciones reactivas sin Room

## Árbol de archivos relevantes

```text
app/src/main/
├── AndroidManifest.xml                 (android:icon, android:roundIcon, android:theme, android:screenOrientation="portrait")
├── res/
│   ├── drawable/
│   │   └── ic_app_logo.png             (192×192 px — usado en AppTopBar y adaptive icon foreground)
│   ├── mipmap-mdpi/   ic_launcher.png + ic_launcher_round.png   (48×48)
│   ├── mipmap-hdpi/                                              (72×72)
│   ├── mipmap-xhdpi/                                             (96×96)
│   ├── mipmap-xxhdpi/                                            (144×144)
│   ├── mipmap-xxxhdpi/                                           (192×192)
│   ├── mipmap-anydpi-v26/
│   │   ├── ic_launcher.xml             (adaptive icon: bg=white, fg=@drawable/ic_app_logo)
│   │   └── ic_launcher_round.xml
│   └── values/
│       ├── strings.xml                 (app_name = "Control de Glucosa")
│       └── themes.xml                  (Theme.GlucoControl → AppCompat.DayNight.NoActionBar)
└── java/com/glucocontrol/
    ├── MainActivity.kt
    ├── GlucoControlApp.kt              (Configuration.Provider para WorkManager+Hilt,
    │                                    canal de notificaciones CHANNEL_ID)
    ├── data/
    │   ├── export/
    │   │   ├── CsvExporter.kt          (RFC 4180, UTF-8)
    │   │   └── PdfExporter.kt          (A4 595×842 pts, paginación automática)
    │   ├── remote/
    │   │   ├── SupabaseClientProvider.kt   (buildSupabaseClient — install(Auth)+install(Postgrest))
    │   │   └── dto/
    │   │       └── GlucoseReadingDto.kt    (GlucoseReadingDto / CreateDto / UpdateDto)
    │   └── repository/
    │       ├── AuthRepositoryImpl.kt       (supabase.auth: signIn/signUp/signOut/getCurrentUser)
    │       └── GlucoseRepositoryImpl.kt    (refreshTrigger + flatMapLatest; insert sin decode)
    ├── di/
    │   ├── AppModule.kt
    │   ├── SupabaseModule.kt               (@Singleton SupabaseClient vía buildSupabaseClient())
    │   └── RepositoryModule.kt
    ├── domain/
    │   ├── model/          (GlucoseReading, GlucoseRange, ReadingTag, GlucoseStatus, User)
    │   ├── repository/     (GlucoseRepository, AuthRepository)
    │   └── usecase/
    │       ├── auth/       (SignInWithEmail, SignUpWithEmail, SignInWithGoogle, SignOut, GetCurrentUser)
    │       ├── query/      (GetDailyReadings, GetWeekly, GetMonthly, GetByDateRange)
    │       └── reading/    (AddReading, UpdateReading, DeleteReading, GetReadingById)
    ├── notification/
    │   ├── GlucoseReminderWorker.kt    (@HiltWorker, POST_NOTIFICATIONS API 33+)
    │   └── ReminderScheduler.kt        (enqueueUniquePeriodicWork, ExistingPeriodicWorkPolicy.UPDATE)
    └── presentation/
        ├── component/
        │   ├── AdaptiveNavigation.kt   (AppBottomBar oculta en Auth, AppNavigationRail)
        │   ├── AppTopBar.kt            (Surface+Box+Column: Image 72dp + Text; showImage=false en ReadingDetail)
        │   └── GlucoseReadingCard.kt   (onClick/onEdit/onDelete opcionales — modo solo lectura en ReadingDetail)
        ├── navigation/
        │   ├── NavGraph.kt             (GlucoApp — showNav oculta nav en Auth; AppNavHost; navigateTopLevel)
        │   └── Screen.kt               (Auth, Home, History, AddEditReading, Chart, ReadingDetail)
        ├── screen/
        │   ├── auth/                   (AuthScreen — tabs login/registro; AuthViewModel — translateError)
        │   ├── addreading/             (AddEditReadingScreen, AddEditReadingViewModel)
        │   ├── chart/                  (ChartScreen, ChartViewModel)
        │   │                            ChartPeriod {WEEKLY, MONTHLY}
        │   │                            ChartPoint(date, avgMgDl) — media diaria
        │   ├── history/                (HistoryScreen, HistoryViewModel)
        │   │                            HistoryPeriod {DAILY, WEEKLY, MONTHLY}
        │   ├── home/                   (HomeScreen — dashboard, HomeViewModel)
        │   └── readingdetail/          (ReadingDetailScreen, ReadingDetailViewModel, DetailPeriod)
        │                                DetailPeriod {THREE_DAYS, WEEKLY, MONTHLY}
        │                                GlucoseStatusBar (Canvas: zonas color + cursor triangular)
        │                                DonutChart (Canvas: arcos coloreados sin texto central)
        └── theme/
            ├── Color.kt                (GlucoseLow/Normal/High, background=White)
            ├── Theme.kt                (GlucoControlTheme)
            └── Type.kt
```
