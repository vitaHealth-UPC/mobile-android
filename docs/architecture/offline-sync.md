# Offline persistence and synchronization

Tata Android maintains device-local state for resilience and for applying individual user preferences. The backend remains authoritative for clinical intake outcomes, treatment schedules, authorization and consent.

## Storage ownership

| Store | Implementation | Contents |
| --- | --- | --- |
| Intake cache | Room `IntakeDatabase` | Server dose snapshots and queued confirmation intents |
| Accessibility settings | Preferences DataStore `tata_accessibility` | Text size, contrast, reading assistance, motion, voice and dark theme; synchronization fields |
| Authenticated session | `EncryptedSessionStore` | Credentials and session ownership for authenticated operations |

The Room database file is `tata_intake_cache.db`. Its entities and access layer live in `intake/infrastructure/local/room`.

## Intake Room schema

`IntakeDatabase` is at schema version 2 and exposes `IntakeDao`:

| Table | Primary key | Selected fields |
| --- | --- | --- |
| `cached_intakes` | `(owner_key, id)` | Treatment, medication and adult references; medication name; dose; instructions; scheduled/status/confirmed timestamps |
| `pending_confirmations` | `(owner_key, intake_id)` | Channel, original queue timestamp, optional rejection code |

The index `(owner_key, older_adult_id, scheduled_at)` supports owner-scoped agenda and next-dose retrieval. `IntakeDao.replaceAgenda` replaces an owner's time range atomically. `IntakeDao.acknowledge` writes an accepted server result and removes its corresponding queued confirmation in one transaction.

The schema migration from version 1 to 2 explicitly partitions legacy records as `legacy:unbound` rather than assigning them to the next logged-in account. The data store does not use a destructive migration fallback.

## Offline reads

`OfflineFirstNextDoseRepository` requests the backend first. A successful result updates the cached snapshot. On `NETWORK_UNAVAILABLE` it may return the existing owner-scoped dose; without a cached value it propagates the connection error rather than fabricating an empty schedule.

Agenda and dose details use the same separation between remote resources and local representations. Application read models and Compose state do not expose Room entities.

## Confirmation queue and replay

A touch-confirmation intent is queued only when the corresponding intake exists in the local cache. The queue's composite key keeps the operation unique for the same owner and intake. Queueing is not shown to the user as a server confirmation.

`IntakeSyncWorker` uses WorkManager, a captured authenticated session and the owner-scoped Room store. `ReplayPendingConfirmations` rechecks the session before and after HTTP calls, uses the original intake ID and accepts only a server result with matching ID, `CONFIRMED` or `LATE` status, and a non-null `confirmedAt`.

| Replay result | Local treatment |
| --- | --- |
| Valid server confirmation | Persist the intake result and remove the queued intent atomically |
| Authentication, access or consent rejection | Stop without transferring the operation to another account |
| `INTAKE_NOT_FOUND` or `INTAKE_NOT_CONFIRMABLE` | Store the definitive rejection against the queued intent |
| Transient transport failure | Return a retry outcome to WorkManager |
| Session changed | End the captured replay |

The backend's confirmation endpoint is responsible for idempotency and final status. Android does not create a second clinical event from a repeated local request.

## User preferences

`DataStoreAccessibilityLocalStore` namespaces keys by owner (for example `owner:<ownerKey>/text_size`). Preferences can be applied locally and synchronized through the user-scoped backend preferences API. Pending synchronization is tracked by field rather than by a separate clinical database.

See [Mobile architecture](mobile-architecture.md), [Bounded contexts](bounded-contexts.md) and [Intake API](../api/treatments-and-intakes.md).

## Original confirmation time

`ConfirmDoseCommand.confirmedAt` captures the action instant when the command is created. Room retains it in the existing `pending_confirmations.created_at` millisecond column and reconstructs it during replay; no schema migration is needed. Already queued rows retain their historical queue instant. TOUCH sends this value as ISO-8601 `confirmedAt`; retries reuse it. Voice uses the recognition endpoint without a device-reported timestamp.

The backend validates device time against slot creation and server receipt, classifies the original action, and can reconcile a previously omitted slot while retaining and closing omission history. Acknowledgement remains required before the local operation is considered synchronized. Old server versions must be updated before relying on retrospective omission reconciliation.
