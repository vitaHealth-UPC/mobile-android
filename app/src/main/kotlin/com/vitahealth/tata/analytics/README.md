# Adherence Analytics — Android

Bounded context de **solo lectura**, contenido en el mismo módulo Gradle `:app`.
Conserva las cuatro capas lógicas DDD/CQRS de APMO, sin fabricar comandos,
agregados de dominio ni eventos que la aplicación no utiliza.

## Estructura

```text
analytics/
  domain/
    model/valueobjects/              AdherencePeriod, AdherencePatternType,
                                     AdherenceTimeBand, AdherenceRecommendation,
                                     IntakeOutcomeStatus
  application/
    queries/                         GetAdherenceSummaryQuery + Handler,
                                     GetAdherenceRecommendationsQuery + Handler
    ports/                           AdherenceSummaryRepository,
                                     AdherenceRecommendationsRepository
    readmodels/                      summary, trend, recent intake, detected
                                     pattern and recommendation projections
  infrastructure/
    remote/                          Retrofit API, DTOs, response validation,
                                     mappers and both repository adapters
    security/                        SessionScopedAnalyticsRepositories
    di/                              AnalyticsDependencies
  presentation/
    navigation/                      history and recommendations destinations;
                                     internal navigation by host callbacks
    history/
      AdherenceHistoryRoute.kt
      screens/                       AdherenceHistoryScreen, existing previews
      viewmodel/                     AdherenceHistoryViewModel and UiState
      ui/                            HistoryLayout, HistoryMetrics, HistoryTrend,
                                     HistoryFeedback and original color tokens;
                                     summary, pattern, trend, recent intake Ui
      mappers/                       history and pattern presentation mappers
    recommendations/
      AdherenceRecommendationsRoute.kt
      screens/                       AdherenceRecommendationsScreen, previews
      viewmodel/                     AdherenceRecommendationsViewModel and UiState
      ui/                            existing cards, heatmap and UI models
      mappers/                       recommendations and time-band labels
    shared/ui/                       AnalyticsEmptyStateCard, reused by both
```

## Responsabilidades

- **Domain**: los resultados de las tomas, periodos, categorías de patrones,
  franjas horarias y acciones admitidas se representan como valores tipados.
  Las proyecciones de métricas no son agregados de escritura. Los cálculos
  clínicos y las recomendaciones definitivas pertenecen al backend.
- **Application / CQRS**: hay dos Queries y sus dos Handlers, sin
  `application/commands` vacío. Rechazan un adulto mayor no identificado y
  periodos fuera de los 1–31 días admitidos antes de tocar los repositorios.
  El resultado nulo significa evidencia insuficiente, no registros inventados.
- **Ports y ReadModels**: ambos repositorios de consulta exponen
  proyecciones de aplicación. No se importan DTOs Retrofit, Android ni
  componentes de UI en esa capa.
- **Infrastructure**: los DTOs remotos se validan y mapean antes de ofrecer
  un read model. `SessionScopedAnalyticsRepositories` captura el propietario
  de la pantalla y las credenciales por petición, comprueba el adulto asociado,
  la vigencia de sesión y descarta respuestas de una sesión sustituida.
- **Presentation**: el Route obtiene estado y callbacks del ViewModel; las
  Screens retienen la composición visual aprobada. Los componentes reutilizables,
  valores gráficos y mapeadores de etiquetas viven en sus flujos. La UI del
  historial se dividió por layout, métricas, gráfico y feedback para mantener
  legibilidad sin cambiar el comportamiento ni las dimensiones.
- **Navigation**: `presentation/navigation/AnalyticsNavigationGraph.kt`
  registra dos destinos y coordina historial → recomendaciones usando
  fábricas y callbacks suministrados por el host. No necesita un segundo
  NavHost ni depende de `TataApplication` o `AppContainer`.

## Pruebas y alcance pendiente

La suite de JVM valida periodos admitidos, identidad del adulto,
mapeo de proyecciones, límites remotos y HTTP, cancelación de cargas,
aislamiento de sesiones y estado de ViewModel. Las pruebas de arquitectura
protegen que Analytics sea de solo lectura y que cada flujo tenga límites
de presentación completos.

La compilación de `androidTest` no ejecuta instrumentación en emulador.
Quedan por aceptar los resultados del backend real, la navegación y estilos
en dispositivo, la accesibilidad de la gráfica, localización y cambio de
zona horaria, sesiones de cuidadores y variantes Figma sin revisar.

Todos los cambios siguen locales en `fix/android-architecture`; no se
realizaron commit, stage, push ni reset. El plan único reside en
`C:/Users/david/.codex/tmp/android-normalization-file-plan.md`.
