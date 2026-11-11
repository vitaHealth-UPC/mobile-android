# Treatment management and intake execution

`treatment` owns medication/treatment presentation and its API adapters. `intake` owns the dose agenda, confirmation flows and synchronized device cache. Both consume authoritative backend resources.

## Medication and treatment endpoints

| Action | Method and path |
| --- | --- |
| Read own medication catalog | `GET /api/v1/me/medications` |
| Register medication | `POST /api/v1/older-adults/{olderAdultId}/medications` |
| List medications | `GET /api/v1/older-adults/{olderAdultId}/medications?caregiverId=...` |
| Update medication | `PUT /api/v1/medications/{medicationId}` |
| Deactivate/reactivate | `POST /api/v1/medications/{medicationId}/deactivation` / `reactivation` |
| Create treatment | `POST /api/v1/older-adults/{olderAdultId}/treatments` |
| List treatments | `GET /api/v1/older-adults/{olderAdultId}/treatments?caregiverId=...` |
| Read treatment detail | `GET /api/v1/treatments/{treatmentId}?caregiverId=...` |
| Configure regimen | `PUT /api/v1/treatments/{treatmentId}/regimen` |
| Activate/pause/resume | `POST /api/v1/treatments/{treatmentId}/activation`, `pause`, `resume` |

The regimen contract distinguishes `reminderLeadMinutes` from `followUpDelayMinutes`. The server stores the configured schedule and determines which intake occurrences apply to the treatment. Client presentation uses the returned treatment state.

## Agenda, next dose and detail

| Action | Method and path |
| --- | --- |
| Weekly/daily agenda | `GET /api/v1/older-adults/{olderAdultId}/intakes/agenda?from=...&to=...` |
| Next dose | `GET /api/v1/older-adults/{olderAdultId}/intakes/next` |
| Intake detail | `GET /api/v1/intakes/{intakeId}` |
| Touch confirmation | `POST /api/v1/intakes/{intakeId}/confirmation` |

The agenda range is half-open: `from` inclusive, `to` exclusive. Local Monday boundaries are computed using the device zone before conversion to UTC. Responses are displayed chronologically and preserve backend statuses such as `PENDING`, `CONFIRMED`, `LATE` and `OMITTED`.

Touch confirmation sends a confirmation request for the same intake ID. A successful replay is idempotent: it is not shown as a second dose. A server response with an existing definitive outcome determines the displayed result.

## Voice confirmation

`POST /api/v1/intakes/{intakeId}/voice-confirmation` accepts a multipart `audio` part and a `language` query. Android records mono 16 kHz PCM16 WAV (`audio/wav`), packages the recording as `confirmation.wav` and sends it through `VoiceConfirmationApiService`.

`RemoteVoiceConfirmationRepository` interprets `CONFIRMED`, `ALREADY_CONFIRMED`, `NOT_RECOGNIZED`, `NOT_VALIDATED` and `PROVIDER_UNAVAILABLE`. A confirmed result must refer to the requested intake and contain a server confirmation timestamp and confirmable status. A recognition error never substitutes a touch confirmation.

## Local consistency

A successful network read refreshes the local intake snapshot. On connection failure, offline-first readers can present an owner-scoped cached dose; an absent cached record is not fabricated as a confirmed empty schedule.

Offline touch commands use the same backend confirmation operation on replay. The acknowledgement stores the returned intake and removes the queued command together. Details are in [Synchronization](../architecture/offline-sync.md).

Related API policies: [Contract guidelines](contract-guidelines.md), [Push and voice](notifications-and-voice.md).
