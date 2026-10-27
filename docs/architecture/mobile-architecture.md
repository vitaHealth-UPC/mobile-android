# Mobile architecture

## 1. Architectural form

Tata Android is a native Kotlin application built as a **single Gradle module**, `:app`, with nine business bounded contexts. These are Kotlin package boundaries rather than independent Gradle modules or separately deployed services. The application communicates with the Tata Spring Boot backend through REST.

The project uses Domain-Driven Design (DDD) to separate ownership and Command Query Responsibility Segregation (CQRS) to distinguish operations that change state from those that retrieve information.

## 2. Source layout

```text
app/src/main/kotlin/com/vitahealth/tata/
  app/                    application shell, dependency composition, navigation
  identity/               identity and subscription
  carelink/               caregiver/adult relationship and consent
  treatment/              medications and treatment management
  intake/                 intake confirmation, agenda and offline sync
  omission/               device messaging and notification routing
  monitoring/             caregiver status, alerts and notes
  analytics/              adherence summaries and insights
  inventory/              medication inventory
  preferences/            accessibility and notification preferences
  shared/                 technical contracts and design system
```

Within each bounded context, the applicable layers are:

| Layer | Responsibility | Typical types |
| --- | --- | --- |
| Domain | Business rules and invariants, without Android dependencies | Aggregates, entities, value objects, repository contracts |
| Application | Use cases and state coordination | Commands, queries, handlers, read models, ports |
| Infrastructure | External interfaces and persistence | Retrofit services, DTO mappers, Room DAOs, DataStore stores, WorkManager adapters |
| Presentation | Native UI and interaction state | Compose screens, ViewModels, routes and UI state |

Only create subpackages with real responsibilities; not every context needs a screen or a local database.

## 3. CQRS and dependency flow

```text
User action
  -> Compose screen / feature route
  -> ViewModel
  -> application command or query handler
  -> repository or service port
  -> infrastructure adapter (HTTP, Room or device)
  -> mapped result / read model
  -> ViewModel UI state
  -> Compose
```

Write commands are validated before invoking the corresponding adapter. Read queries expose read models suitable for presentation without transferring Retrofit DTOs or Room entities into Domain.

The backend is authoritative for account authorization, consent, treatment transitions and clinical intake outcomes. Android does not infer that a dose is confirmed merely because a local operation was queued.

## 4. Application shell and navigation

`app/navigation` owns the root destination model, role-specific navigation graphs and the global navigation host. `app/di/AppContainer` composes the implementation dependencies of the bounded contexts. `app/integration` contains explicit cross-context presentation integrations, such as medication overview and notification navigation.

Feature-specific screens remain within their owning context; callbacks and route contracts connect them to the host. `shared/presentation/designsystem` provides Compose visual primitives without transferring business ownership to `shared`.

`shared/application/session` and the security/networking adapters provide reusable technical support. Session and consent checks remain enforced by the backend, including for notification-initiated navigation.

## 5. Device and backend boundaries

| Concern | Android implementation | Authority |
| --- | --- | --- |
| API communication | Retrofit + mapped request/response models | Spring Boot REST API |
| Session storage | Encrypted local session adapter | Backend session validation |
| Intake cache | Room database owned by Intake Execution | Server intake state |
| Accessibility settings | Owner-scoped DataStore preferences | Server confirmation for remote settings |
| Offline confirmation | Room queue + WorkManager replay | Server confirmation endpoint |
| Voice recording | Mono PCM16 WAV capture | Backend speech validation and intake outcome |
| Push messages | Firebase adapters, device registration and tap navigation | Backend authorization and provider delivery |
| Localization | Android string resources | Stable backend error codes |

The app has no cross-context business database shared with the server. Local persistence stores device state or server snapshots, not an alternative source of clinical truth.

## 6. Integration rules

- A context owns its application commands, query handlers, DTOs and persistence adapters.
- Domain does not import Android, Retrofit, Compose or Room.
- Presentation does not instantiate API clients, DAOs or persistence entities directly.
- Cross-context behavior goes through the application shell or explicit contracts rather than private infrastructure imports.
- The server's OpenAPI document defines HTTP paths and payloads; mobile adapters map and validate their responses.
- Real provider services depend on their configured environments and credentials.

See [Bounded contexts](bounded-contexts.md), [Contract guidelines](../api/contract-guidelines.md) and [Local persistence](offline-sync.md).
