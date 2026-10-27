# Intake & Adherence - Android

The bounded context remains inside the single Gradle `:app` source module.
It implements four logical layers with DDD/CQRS. The backend retains authority
over scheduled-dose lifecycle, late/omitted policy, and clinical permissions.

## Ownership map

```text
intake/
  domain/
    model/valueobjects/             DoseStatus, ConfirmationChannel
  application/
    commands/                       ConfirmDose, ConfirmDoseByVoice + handlers
    queries/                        next dose, detail, weekly agenda,
                                    daily progress + handlers, AgendaWeek
    inputs/                         VoiceRecording
    ports/                          dose, agenda, voice, and recording contracts
    readmodels/                     scheduled doses and progress projections
  infrastructure/
    audio/                          AndroidVoiceAudioRecorder
    remote/                         APIs, request/response DTOs and mappers
    local/                          offline-first adapters, session scoped store
    local/room/                     database, DAO, migration, confirmation queue
    sync/                           WorkManager, pending replay, session recovery
    di/                             IntakeDependencies
  presentation/
    home/
      NextDoseHomeRoute.kt
      screens/                       NextDoseHomeScreen
      viewmodel/                     NextDoseHomeViewModel and state
      ui/                            HomeCards, ReinforcedReminder
      mappers/                       date and remaining-time presentation
    agenda/
      IntakeAgendaRoute.kt
      screens/                       IntakeAgendaScreen
      viewmodel/                     IntakeAgendaViewModel and state
      ui/                            AgendaDoseRow
    detail/
      DoseDetailRoute.kt
      screens/                       DoseDetailScreen, DoseOutcomeScreen
      viewmodel/                     ViewModel, confirmation outcomes and states
      ui/                            dose and result-state components
      mappers/                       schedule and dose-status mapping
    voice/
      VoiceConfirmationRoute.kt
      screens/                       VoiceConfirmationScreen
      viewmodel/                     VoiceConfirmationViewModel, voice phases, state
```

No empty aggregate, domain-events or domain repository folder is created:
the mobile client operates on backend-owned scheduled dose projections,
with pure domain status/channel value objects.

## CQRS, persistence and security

- ConfirmDose and ConfirmDoseByVoice are Commands. The voice command makes
  defensive copies of audio bytes and handlers check content type, length
  and intake identity before submitting to ports.
- GetNextDose, GetDoseDetail, GetDailyDoseProgress and GetIntakeAgenda are
  Queries, separated from Commands. GetIntakeAgendaQueryHandler rejects
  blank adult references and inverted/empty absolute windows. The Agenda
  ViewModel invokes this handler, not the repository directly.
- AgendaWeek models local calendar weeks using a ZoneId and computes
  half-open Instant intervals. Do not replace this with fixed 24-hour days.
- Application ports return read models and command outcomes, not domain
  aggregates. Infrastructure implements those contracts with Retrofit and
  owner-partitioned Room local storage.
- The existing offline-first adapters, Room schema, queue, WorkManager
  recovery and replay logic were preserved. SessionScopedIntakeRepositories
  captures credentials, checks the user/partition and discards stale results.
  Replay retains retriable versus non-retriable failure distinctions.
- Voice permissions and text-to-speech are handled by the Route while the
  ViewModel owns recording commands and cancellation.

## Presentation and navigation

Each flow owns its Route, Screen, ViewModel/state and only the UI/mappers
that are actually implemented. Route collects lifecycle state and
callbacks; stateless Screens render the existing Figma-aligned bodies.
UI helpers and formatters were separated without intentional changes to
visual geometry, strings, images, colors or callback behavior.

The four destination registrations remain owned by
`app/navigation/IntakeNavigationGraph.kt`. The host now injects AppContainer;
individual destination handlers no longer fetch TataApplication from
LocalContext. No duplicate presentation/navigation package is necessary.

## Verification and remaining acceptance

The compile/JVM gate covers structural compatibility, pure handlers,
existing local and remote adapters, synchronization replay and ViewModels.
A successful compile of Android instrumentation-test sources does *not*
mean those tests were run on a physical device or emulator.

Pending functional acceptance includes process death and replay timing,
timezone/clinical-day behavior during offline fallback, caregiver
authorization, live voice/microphone outcomes, server late-versus-omitted
responses and recurrence semantics. Remaining Figma variants need separate
visual checks. Nothing in this architectural pass certifies those areas.

Changes remain local, without a commit or a Git reset. The canonical plan
is `C:/Users/david/.codex/tmp/android-normalization-file-plan.md`.
