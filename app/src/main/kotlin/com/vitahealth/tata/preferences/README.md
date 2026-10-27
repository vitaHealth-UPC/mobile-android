# Accessibility & Preferences — Android

Bounded context dentro del único módulo Gradle `:app`, según la arquitectura
lógica de cuatro capas DDD/CQRS de APMO. No se duplican destinos ni módulos
Gradle para simular una separación que ya proporcionan los paquetes.

## Estructura

```text
preferences/
  domain/
    model/entities/                 UserPreferences (identidad validada)
    model/valueobjects/             AccessibilityPreferences, TextSizeLevel,
                                    NotificationPreferences, NotificationPolicy,
                                    QuietHours, channels, emergency contacts
    repositories/                   NotificationPreferencesRepository
  application/
    commands/                       mutaciones y sincronización de preferencias,
                                    política/canales/contactos + handlers
    queries/                        observar accesibilidad local, obtener
                                    preferencias de notificación + handlers
    ports/                          AccessibilityLocalStore,
                                    UserPreferencesRemote,
                                    UserPreferencesRepository
    readmodels/                     PreferenceUpdate (estado de sincronización)
    sync/                           AccessibilityField (dirty fields)
  infrastructure/
    local/                          DataStoreAccessibilityLocalStore
    remote/                         API, requests, DTOs, response mappers
    security/                       SessionScopedUserPreferencesRepository,
                                    SessionScopedNotificationPreferencesRepository
    sync/                           OfflineFirstUserPreferencesRepository
    di/                             PreferencesDependencies
  presentation/
    navigation/                     destinos y registro del grafo por callbacks
    accessibility/
      AccessibilityRoute.kt
      screens/                      AccessibilityScreen
      viewmodel/                    AccessibilityViewModel, UiState, Message
      ui/                           controles y grupos accesibles
      mappers/                      mapeo puro de errores
    notifications/
      NotificationPreferencesRoute.kt
      screens/                      NotificationPreferencesScreen
      viewmodel/                    ViewModel, UiState y Message
      ui/                           tarjetas de horario, canales y política
      mappers/                      errores y formato de hora
```

## Responsabilidades y decisiones

- **Domain**: la identidad de `UserPreferences` debe ser no vacía, incluso
  al aplicar `copy`. QuietHours valida horas y cruce de medianoche sin permitir
  un intervalo vacío. NotificationPolicy valida secuencia de avisos,
  escalamiento y par de campos de resumen. La autorización real la decide
  el backend; el cliente no inventa aggregates ni domain events adicionales.
- **Application**: `commands/` implementa cambios individuales, canales,
  horarios, política y sincronización pendiente; `queries/` obtiene los
  ajustes persistidos y observa la copia local. Los ViewModels inyectan
  handlers y nunca Retrofit, DataStore ni un repositorio directamente.
- **Infrastructure**: la copia accesible usa DataStore segregado por
  propietario. `security/` captura credenciales y descarta resultados al
  cambiar de cuenta. `sync/` aplica escrituras locales primero, registra
  campos no confirmados y reconcilia con el estado validado del servidor.
  Estos contratos preexistentes se trasladaron sin reinterpretar su
  semántica ni modificar claves persistidas.
- **Presentation**: cada flujo tiene Route, Screen, ViewModel/UiState,
  componentes `ui/` y mapeadores que contienen código real. El Route
  recoge estado y conecta callbacks; las pantallas y sus componentes
  preservan el contenido Compose y los layouts validados. Los mensajes
  y el formato de hora pertenecen a presentación, no al ViewModel/domain.
- **Navigation**: `presentation/navigation/PreferencesNavigationGraph.kt`
  ya era una frontera útil que registra ambos destinos a partir de fábricas
  y callbacks, sin conocer `AppContainer`. Se mantiene, mientras que
  `app/navigation/PreferencesNavigationGraph.kt` conecta el host.

## Verificación y aceptación pendiente

La compilación Kotlin, compilación de fuentes de `androidTest` y pruebas
JVM verifican las fronteras del código. La compilación de instrumentadas
**no** significa ejecución sobre dispositivo.

Quedan pendientes como aceptación funcional: cambio de cuenta durante una
escritura, reconexión y replay de campos dirty, política de contactos contra
backend en producción, horario silencioso bajo cambios de zona horaria,
persistencia de accesibilidad tras cierre del proceso y comparación visual
de variantes no validadas en Figma.

No se alteraron intencionalmente las composiciones visuales ni se realizaron
commits, stage, push o reset de Git.

Plan único: `C:/Users/david/.codex/tmp/android-normalization-file-plan.md`.
