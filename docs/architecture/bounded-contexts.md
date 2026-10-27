# Bounded context ownership

The Android packages correspond to the nine canonical Tata bounded contexts used by the backend and academic architecture.

| Business context | Android package | Mobile responsibility | Backend collaboration |
| --- | --- | --- | --- |
| Identity & Subscription | `identity` | Registration, verification, password/PIN sign-in, recovery, subscription and social sign-in | Accounts, sessions, plans and linked identities |
| Care Link | `carelink` | Adult profile registration, linking codes, acceptance and consent | Verified care relationships and authorized profile access |
| Treatment Management | `treatment` | Medication catalog, treatment creation, regimen and lifecycle screens | Medication and treatment resources |
| Intake Execution | `intake` | Agenda, next dose, dose detail, touch/voice confirmation and offline synchronization | Persisted intake state, confirmation and schedules |
| Omission & Escalation | `omission` | Device token registration, notification reception and tap routing | Push-device authorization and omission alerts |
| Family Monitoring | `monitoring` | Caregiver summary, alert detail, status, contacts, follow-up and personal notes | Family status, alerts, notes and contact channels |
| Adherence Analytics | `analytics` | Adherence summaries, history, insights and recommendations | Backend calculated adherence metrics |
| Inventory & Replenishment | `inventory` | Register stock, read quantity and register replenishment | Inventory balances, batches and thresholds |
| Accessibility & Preferences | `preferences` | Text size, contrast, reading assistance, motion, dark theme and notification settings | User-scoped preferences and notification policies |

## Ownership and collaboration

Each context keeps the four DDD layers applicable to its features. A business concept has one owner; other contexts consume its public application contract or the corresponding backend resource.

The `app` package hosts application composition and navigation. It can coordinate the treatment/intake overview or link notification taps to monitoring screens without moving those contexts' business logic into the host.

The `shared` package contains reusable technical types, such as common results, session abstractions and design-system components. It does **not** contain shared medication, consent or intake aggregates.

## Representative package structure

```text
intake/
  domain/                  dose-related concepts
  application/             commands, queries, handlers, read models, ports
  infrastructure/
    remote/                Retrofit DTOs and mappers
    local/room/            Room entities, DAO and database
    sync/                  WorkManager synchronization
    audio/                 native audio recording
  presentation/            Compose screens and UI state
```

The physical layout varies by context: Analytics is read-oriented, Omission manages push and routing infrastructure, and Inventory uses remote stock contracts. Empty DDD subfolders are not created for the sake of symmetry.

## Identifiers and data ownership

Account, older-adult, care-link, medication, treatment and intake references travel as backend-defined identifiers. Alert identifiers are handled as `Long` where defined in the monitoring responses. Android does not make foreign-key assumptions across different backend contexts.

The authoritative treatment schedule, dose status, stock quantity, consent and caregiver alert transitions remain on the backend. The local intake cache and preference store are scoped to the authenticated owner; their storage rules are described in [Local persistence](offline-sync.md).

## Related documentation

- [Mobile architecture](mobile-architecture.md)
- [Backend contracts](../api/contract-guidelines.md)
- [Figma frame map](figma-frame-map.md)
- [User Stories](user-stories.md)
