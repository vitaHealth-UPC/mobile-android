# Monitoring & Clinical Tracking — Android

Monitoring is a bounded context **inside the existing Gradle `:app` module**.
The course's four DDD/CQRS layers stay logically separated; the Android client
does not invent backend-owned clinical aggregate lifecycle or domain events.

## Structure

```text
monitoring/
  domain/
    model/entities/                 CaregiverAlert, FollowUpNote, PersonalNote
    model/valueobjects/             AdherenceCounts, AlertStatus, ContactChannel
    repositories/                   alerts, follow-up, personal notes, contact
  application/
    commands/                       alert status, caregiver follow-up note,
                                    personal note creation + handlers
    queries/                        alerts, contact, follow-up, family summary,
                                    family history, personal notes + handlers
    ports/                          FamilyMonitoringRepository, ContactOptionReader
    readmodels/                     family summary, history, inventory, contact
  infrastructure/
    remote/                         Retrofit, DTOs, response mapping and adapters
    security/                       caregiver/adult session ownership adapters
    di/                             MonitoringDependencies
  presentation/
    summary/
      FamilySummaryRoute.kt
      screens/                      FamilySummaryScreen
      viewmodel/                    FamilySummaryViewModel, state, dialog
      ui/                           summary layout components
      mappers/                      history label/date formatting
    alerts/
      AlertsRoute.kt
      AlertDetailRoute.kt
      screens/                      AlertsScreen, AlertDetailScreen, preview fixtures
      viewmodel/                    alert state, ViewModels, filters
      ui/                           list/detail cards, chips, follow-up actions
    notes/
      NotesRoute.kt
      screens/                      NotesScreen
      viewmodel/                    NotesViewModel, NoteComposer
      ui/                           composer dialog and note cards
    personalnotes/
      PersonalNotesRoute.kt
      screens/                      PersonalNotesScreen
      viewmodel/                    PersonalNotesViewModel and state
      ui/                           note cards, filters and composer
    contact/
      ui/                           shared contact controls
      mappers/                      Android phone/WhatsApp intents
```

## Decision boundaries

- **Domain**: alert, caregiver note and personal note entities have meaningful
  identity. CaregiverAlert validates status and timestamps. AdherenceCounts
  rejects inconsistent totals, and AlertStatus.canMoveTo defines actions the
  UI may offer. Consent, write authorization, state transitions and conflict
  resolution remain authoritative on the backend.
- **Application**: typed Commands and Queries dispatch through dedicated
  handlers. Invalid caregiver/adult references and invalid personal note text
  are rejected before calling repositories; read models remain separate from
  domain entities.
- **Repositories and ports**: AlertsRepository, ContactRepository and note
  repositories return domain objects. FamilyMonitoringRepository and
  ContactOptionReader return projections, so they belong in application ports.
- **Infrastructure**: Remote adapters map HTTP DTOs; security adapters bind
  actions to the initiating caregiver/adult session and discard results after
  a credential change. All factories are composed in MonitoringDependencies.
- **Presentation**: family summary and personal note ViewModels use application
  handlers instead of repository implementations/ports. Screen Composables
  retain their existing Figma-aligned bodies; reusable components, preview
  fixtures, UI mapping and contact intents have been separated.
- **Navigation**: AppContainer is supplied to the five destination
  registrations by TataNavHost in app/navigation. No duplicate NavHost or
  redundant presentation/navigation directory was created.

## Checks and pending acceptance

The Gradle gate covers app compilation, Kotlin AndroidTest source compilation,
unit tests and architecture tests. Compiling AndroidTest does *not* mean running
instrumentation on a connected device.

Pending integration/device and visual checks: live caregiver permissions,
consent changes, alert conflicts, stale responses, push navigation, phone and
WhatsApp intents, timezone labels, accessibility, and still-unverified Figma
variants. Do not present those as completed by the architecture tests.

No Git commit, stage, push or reset was performed. Active canonical plan:
`C:/Users/david/.codex/tmp/android-normalization-file-plan.md`.
