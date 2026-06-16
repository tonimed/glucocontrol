# Gluco Control

Aplicación Android para el registro y seguimiento de glucosa en sangre. Permite registrar lecturas con fecha, hora y etiqueta, visualizar el historial por día/semana/mes, consultar gráficas de evolución y exportar los datos en CSV o PDF.

---

## Capturas de pantalla

> *(pendiente de añadir screenshots de la app)*

---

## Características

- **Registro de lecturas** — valor mg/dL, fecha, hora opcional, etiqueta (Ayunas, Postprandial, Antes de dormir…) y notas libres
- **Dashboard** — resumen del día con estado glucémico (bajo/normal/alto) y últimas lecturas
- **Historial** — listado agrupado por día con navegación por día, semana o mes; estadísticas del periodo (media, mín/máx, % en rango)
- **Gráfica** — evolución semanal o mensual con zonas de color por rango glucémico
- **Exportación** — CSV (RFC 4180) y PDF (A4 con paginación automática)
- **Recordatorios** — notificaciones periódicas mediante WorkManager configurables en la app
- **Diseño responsive** — BottomBar en móvil, NavigationRail en tablet (≥ 600 dp)

---

## Arquitectura

**Clean Architecture + MVVM** con tres capas estrictas:

```
domain/        Kotlin puro. Modelos, interfaces de repositorio, casos de uso.
data/          Room + Hilt. Entidades, DAOs, implementaciones de repositorio.
presentation/  Jetpack Compose + ViewModels. Pantallas, componentes, navegación.
```

- `domain/` no importa nada de `data/` ni del SDK de Android.
- El mapeo Entity ↔ Domain ocurre únicamente en `data/repository/`.
- Los ViewModels solo invocan casos de uso, nunca el repositorio directamente.

---

## Stack tecnológico

| Componente | Versión |
|---|---|
| Kotlin | 2.2.21 |
| Android Gradle Plugin | 8.x |
| Jetpack Compose BOM | 2026.05.01 |
| Material3 | (incluido en BOM) |
| Room | 2.8.4 |
| Hilt | 2.56.2 |
| KSP | 2.2.21-2.0.5 |
| Navigation Compose | 2.9.8 |
| WorkManager | 2.10.1 |
| Coroutines | 1.10.2 |
| JUnit 5 | 5.12.2 |
| MockK | latest |

- **Min SDK:** 26 (Android 8.0) — usa `java.time.*` nativo
- **Target SDK:** 36

---

## Estructura del proyecto

```
app/src/main/java/com/glucocontrol/
├── MainActivity.kt
├── GlucoControlApplication.kt        Configuration.Provider para WorkManager + Hilt
├── data/
│   ├── export/
│   │   ├── CsvExporter.kt            RFC 4180, UTF-8
│   │   └── PdfExporter.kt            A4, paginación automática
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
│   ├── model/                        GlucoseReading, GlucoseRange, ReadingTag
│   ├── repository/                   GlucoseRepository (interfaz)
│   └── usecase/
│       ├── query/                    GetDailyReadings, GetWeekly, GetMonthly, GetByDateRange
│       └── reading/                  AddReading, UpdateReading, DeleteReading, GetReadingById
├── notification/
│   ├── GlucoseReminderWorker.kt      @HiltWorker, POST_NOTIFICATIONS API 33+
│   └── ReminderScheduler.kt          enqueueUniquePeriodicWork
└── presentation/
    ├── component/
    │   ├── AdaptiveNavigation.kt     AppBottomBar / AppNavigationRail
    │   └── GlucoseReadingCard.kt
    ├── navigation/
    │   ├── NavGraph.kt               GlucoApp, AppNavHost
    │   └── Screen.kt                 Home, History, AddEditReading, Chart
    ├── screen/
    │   ├── addreading/               AddEditReadingScreen + ViewModel
    │   ├── chart/                    ChartScreen + ViewModel (Canvas)
    │   ├── history/                  HistoryScreen + ViewModel
    │   └── home/                     HomeScreen + ViewModel (dashboard)
    └── theme/
        ├── Color.kt                  GlucoseLow / Normal / High
        ├── Theme.kt
        └── Type.kt
```

---

## Requisitos de entorno

- Android Studio Hedgehog o superior
- JDK 21 (incluido en el JBR de Android Studio)
- Android SDK con API 36 instalado

---

## Configuración inicial

1. Clona el repositorio:
   ```bash
   git clone https://github.com/tonimed/GlucoControl.git
   cd GlucoControl
   ```

2. Copia `local.properties.example` (si existe) o crea `local.properties` con la ruta de tu SDK:
   ```
   sdk.dir=C\:\\Users\\<tu_usuario>\\AppData\\Local\\Android\\Sdk
   ```

3. Para generar una build de release, crea `keystore.properties` en la raíz del proyecto (nunca se sube al repositorio):
   ```
   storeFile=ruta/al/glucocontrol-release.jks
   storePassword=...
   keyAlias=...
   keyPassword=...
   ```

---

## Comandos de build

```powershell
# Configurar entorno (PowerShell)
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "C:\Users\<tu_usuario>\AppData\Local\Android\Sdk"

# Tests unitarios (JVM, sin dispositivo)
.\gradlew.bat :app:testDebugUnitTest

# Tests instrumentados (requiere emulador o dispositivo)
.\gradlew.bat :app:connectedDebugAndroidTest

# APK de release firmado
.\gradlew.bat :app:assembleRelease
# Salida: app/build/outputs/apk/release/app-release.apk

# Android App Bundle (para Google Play)
.\gradlew.bat :app:bundleRelease
# Salida: app/build/outputs/bundle/release/app-release.aab

# Linter
java -jar ktlint-1.8.0.jar --format "app/src/**/*.kt"
java -jar ktlint-1.8.0.jar "app/src/**/*.kt"
```

---

## Tests

Los tests unitarios cubren los 9 casos de uso de la capa `domain/`:

- `AddReadingUseCaseTest` — validación de rango de glucosa y fechas
- `UpdateReadingUseCaseTest`, `DeleteReadingUseCaseTest`, `GetReadingByIdUseCaseTest`
- `GetDailyReadingsUseCaseTest`, `GetWeeklyReadingsUseCaseTest`, `GetMonthlyReadingsUseCaseTest`
- `GetByDateRangeUseCaseTest`, `GetReadingStatsUseCaseTest`

Los tests instrumentados validan el DAO de Room con base de datos in-memory.

---

## Seguridad

- Las credenciales de firma (`keystore.properties`, `*.jks`) están excluidas del repositorio vía `.gitignore`.
- `android:allowBackup="false"` en el Manifest para proteger datos de salud en backups automáticos.
- Los datos se almacenan únicamente en el dispositivo (sin sincronización a servidores externos).

---

## Licencia

Este proyecto es de uso personal y educativo.
