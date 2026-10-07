# Backend contracts

Android consumes the `vitaHealth-UPC/web-services` API but does not mirror backend internals.

Rules:

- Retrofit request/response DTOs stay in each context's `infrastructure/remote` package.
- DTOs are mapped before they cross into Domain or Presentation.
- API paths and payloads must be documented here when the corresponding backend contract is finalized.

## Intake confirmation (TS-04 / US-06 touch)

`POST /api/v1/intakes/{intakeId}/confirmation`, body `{ "channel": "TOUCH" }` (`VOICE` shares the same invariant). Returns the same intake representation as detail. `404` means missing intake; `409` means definitive omission cannot be overwritten. Retries use the original intake ID and do not create duplicate confirmations. Backend decides `CONFIRMED / LATE / OMITTED`.

Account, OlderAdult, CareLink, Medication, Treatment and Intake IDs and references are UUID `String` values. Numeric internal IDs from other contexts do not change that contract.

Touch confirmation is implemented. Device voice recognition and its permission/error states remain pending; accepting VOICE in REST does not implement speech recognition.

## Daily/weekly agenda (TS-08 / US-24)

`GET /api/v1/older-adults/{olderAdultId}/intakes/agenda?from=<ISO instant>&to=<ISO instant>` returns an array of intake resources, chronologically ordered, including persisted outcomes. `200 []` is an empty agenda. Both bounds are required; start is inclusive and end exclusive. The server rejects invalid or ranges exceeding eight elapsed days with `400`.

`AgendaWeek` computes Monday and the next Monday in the device zone independently, then converts to UTC. Grouping and displayed times also use that zone, including doses whose UTC date differs from their local date. Day/week selection, loading, retry, empty state and navigation into dose detail use real API data; returning from confirmation refreshes the agenda and home.

The stored schedule horizon is finite. A requested later week may legitimately be empty until backend rolling generation exists. Treatment schedule entry currently has a separate UTC interpretation gap; patient-specific schedule zones remain pending. Neither Compose nor the agenda query invents lateness or omission decisions.

## Inventory & replenishment (TS-12 / US-40 to US-43)

Base path `/api/v1/inventories`. Inventory is keyed by `medicationId` (a logical UUID reference to Treatment Management; there is no cross-context foreign key).

### Endpoints

- `POST /api/v1/inventories` — register the initial stock (US-40). Body `{ "medicationId": String, "initialQuantity": Int, "replenishmentThreshold": Int }`. `201` returns the inventory resource. Only one inventory may exist per medication, and initial registration requires an existing active medication.
- `GET /api/v1/inventories/{medicationId}` — remaining stock, threshold, low-stock flag and batches (US-41, US-42). `200` returns the inventory resource; `404` means no inventory is registered yet.
- `POST /api/v1/inventories/{medicationId}/replenishments` — add a batch and increase stock (US-43). Body `{ "quantity": Int, "lot": String? }`. `201` returns the updated inventory resource.

### Response shapes

`InventoryResource`:
`{ id, medicationId, remainingStock: Int, replenishmentThreshold: Int, lowStock: Boolean, batches: Batch[], createdAt: Instant, updatedAt: Instant, daysRemaining: Int?, dailyConsumptionUnits: Int? }`. `lowStock` is `true` when `remainingStock <= replenishmentThreshold`; the app consumes this flag and does not recompute the rule.

`Batch`: `{ id, quantity: Int, registeredAt: Instant, lot: String? }`.

`ErrorResource`: `{ code: String, message: String }`. The `code` is stable and meant for clients; the `message` is localized by `Accept-Language`.

Timestamps are ISO-8601 strings on the wire. The shared Retrofit uses the default Gson, which does not deserialize `java.time.Instant`, so DTO time fields are `String` and parsed in `RemoteInventoryRepository`.

### Backend error codes

`MEDICATION_NOT_FOUND` (`404`, initial registration), `MEDICATION_INACTIVE` (`409`, initial registration), `INVALID_QUANTITY` (`400`), `INVENTORY_NOT_FOUND` (`404`), `INVENTORY_ALREADY_EXISTS` (`409`), `CONCURRENT_UPDATE` (`409`, optimistic-lock conflict on replenishment), `INSUFFICIENT_STOCK` (`409`, only reachable through intake consumption, not these endpoints), `VALIDATION_ERROR` (`400`).

### Mobile mapping

`RemoteInventoryRepository` reads known stable codes from `ErrorResource`; localized presentation uses the code rather than the server message. Missing, malformed or unknown error codes use these endpoint/status fallbacks:

- `getStock`: `404 -> INVENTORY_NOT_FOUND`, other -> `REQUEST_FAILED`.
- `registerInitialInventory`: `400 -> INVALID_QUANTITY`, `404 -> MEDICATION_NOT_FOUND`, `409 -> INVENTORY_ALREADY_EXISTS`, other -> `REQUEST_FAILED`.
- `registerReplenishment`: `400 -> INVALID_QUANTITY`, `404 -> INVENTORY_NOT_FOUND`, `409 -> CONCURRENT_UPDATE`, other -> `REQUEST_FAILED`.
- A thrown I/O exception -> `NETWORK_UNAVAILABLE`; malformed responses or unparseable inventory/batch timestamps -> `INVALID_RESPONSE`. Coroutine cancellation propagates to the caller.

Quantity/threshold are validated in the application layer with the `Quantity` (> 0) and `ReorderThreshold` (>= 0) value objects before any request, so invalid input (the "Cantidad inválida" case) never reaches the network. Those app-born failures use the codes `INVALID_QUANTITY`, `INVALID_THRESHOLD` and `INVALID_MEDICATION_REFERENCE`. `GetInventoryStockQueryHandler` passes `INVENTORY_NOT_FOUND` through unchanged; the ViewModel turns it into the "not initialized" state. Each code (backend and app-born) resolves to a localized string in `:inventory` `res/values` and `res/values-b+es+419`; the UI never shows the backend `message` directly.

### Inventory metadata and coverage

Replenishments accept an optional `lot` (trimmed, maximum 200 characters), which is persisted and returned on the batch. The app presents the Figma "Lote / nota" field and the last replenishment metadata.

`daysRemaining` is computed by the backend as remaining stock divided by scheduled daily intake units, rounded down. The current intake model consumes one unit per scheduled intake. A medication without an active scheduled treatment returns null; the app displays "Sin estimación". Medication name and presentation are supplied from the treatment entry point.

## Accessibility preferences (US-35 and following)

`GET /api/v1/users/{userId}/preferences` returns `textSize` (`SMALL | MEDIUM | LARGE | EXTRA_LARGE`), `highContrast`, `reducedMotion`, `readingAssistance`, `voiceConfirmationEnabled`, `quietHours` (`{start, end}` or null) and `notificationChannels` (`PUSH | SMS | EMAIL`). A user who never saved anything gets the defaults. `PUT /api/v1/users/{userId}/preferences/text-size` takes `{ "textSize": "LARGE" }` and returns the same representation. The endpoints require a bearer session authorized for the account identified by `userId`.

The app keeps a copy in DataStore (`tata_accessibility`) and applies it before any network call. A change made without connection stays on the device, is flagged as pending and is sent on the next `sync`. A `400` from the backend rolls the device copy back. The "Large text" switch maps to `LARGE` (on) and `MEDIUM` (off).

`PUT /api/v1/users/{userId}/preferences/contrast` takes `{ "enabled": true }` and returns the same preferences representation (US-36). The offline rule is the same as for the text size.

`PUT /api/v1/users/{userId}/preferences/reduced-motion` (US-37) and `PUT /api/v1/users/{userId}/preferences/voice-confirmation` take `{ "enabled": true }` and return the preferences representation.

`PUT /api/v1/users/{userId}/preferences/reading-assistance` (US-38) takes `{ "enabled": true }` and returns the preferences representation.

`PUT /api/v1/users/{userId}/notification-preferences` (US-39) takes `{ "quietHours": {"start": "22:00", "end": "07:00"} | absent, "channels": [{"type": "PUSH", "enabled": true}, ...] }`, replaces both settings and returns the preferences representation. A `400` means an empty interval or a repeated channel. These two settings have no device copy: they are read and saved online only.
## Medications of an older adult (US-04)

`GET /api/v1/older-adults/{olderAdultId}/medications?caregiverId=` lists the medications ordered by name, inactive ones included. `PUT /api/v1/medications/{medicationId}` takes `{ caregiverId, name, presentation }`; `POST /api/v1/medications/{medicationId}/deactivation?caregiverId=` deactivates and keeps the history. Both return the medication. `403` means no active care link, `404` an unknown medication, `409` that an inactive medication cannot be edited and `400` a missing field. There is no reactivation endpoint.

## Session access and PIN (US-01)

The login screen calls `POST /api/v1/sessions` with `{email,password}`. The encrypted session store saves the token and expiry before navigation. `GET /api/v1/sessions/current` restores caregiver and older-adult sessions; restricted setup sessions require consent. PIN setup uses authenticated `POST /api/v1/pin-credentials`; PIN access uses `POST /api/v1/pin-sessions`, both with `{olderAdultId,pin}`. The backend enforces `PIN_LOCKED` for 15 minutes. Confirmed consent enters PIN setup and then the adult home. Device metadata remembers the adult identifier and name, never the PIN.

## Caregiver profile entry (US-12)

After caregiver verification/sign-in, the app calls `GET /api/v1/care-links?caregiverId=` and uses `olderAdultId` from confirmed links to open the family summary. New profiles use `POST /api/v1/older-adults`; a temporary code uses `POST /api/v1/care-links/linking-codes`. Partial emergency contacts and future birth dates are rejected before submission. The registered adult is retained if code generation fails, so retry does not duplicate the profile. The caregiver hands the device/code to the adult before acceptance and consent. Account switching revokes the presented session and clears local credentials even without a connection.

## Caregiver alerts (US-27)

Source: the deployed OpenAPI document (`/v3/api-docs`, tags "Alerts" and "Family Monitoring"). There is no alert list endpoint, and the app does not invent one.

- List: `GET /api/v1/older-adults/{olderAdultId}/status?caregiverId=` (US-25). The alerts that still need attention come in `openAlerts`; the app shows only that array. `404` means the older adult has no active follow-up.
- Detail: `GET /api/v1/older-adults/{olderAdultId}/alerts/{alertId}?caregiverId=`. `404` means the follow-up or the alert does not exist.
- Both reads go with the bearer session of the caregiver. The OpenAPI document lists no `403`; the app still treats a `403` as an inactive care link or missing consent, as the recent status already does.

`AlertSummaryResource` (both endpoints): `{ id: int64, intakeId: String, medicationName: String, scheduledAt: date-time, reason: String, status: "OPEN" | "ATTENDED" | "CLOSED", openedAt: date-time, closedAt: date-time | null }`.

The alert `id` is a numeric `int64` (`Long` in Android), unlike the UUID `String` identifiers listed in the intake section; `intakeId` stays a `String`. The status names are the backend ones: the app shows OPEN as Pending / Pendiente, ATTENDED as Attended / Atendida and CLOSED as Closed / Cerrada. There is no "Resolved" status.

### Alert follow-up status (US-31)

`PUT /api/v1/older-adults/{olderAdultId}/alerts/{alertId}/status?caregiverId=` with body `UpdateAlertStatusResource` `{ "status": "ATTENDED" | "CLOSED" }` returns the updated `AlertSummaryResource` (`200`). The schema enum also lists `OPEN`, but the backend only accepts `ATTENDED` and `CLOSED`, so the app never sends `OPEN`. Errors: `400` status not accepted, `404` follow-up or alert not found, `409` the alert cannot move to that status. No `403` is documented; the app treats one like the reads.

The app offers OPEN → ATTENDED ("Mark as attended"), OPEN or ATTENDED → CLOSED ("Close alert", after a confirmation because a closed alert cannot be reopened) and no action on a CLOSED alert. On `409` it reloads the alert detail and shows its current status. A CLOSED alert leaves `openAlerts`; the list refreshes when it is shown again.

## Follow-up notes (US-30)

- List: `GET /api/v1/older-adults/{olderAdultId}/notes?caregiverId=` returns `CaregiverNoteResource[]`, most recent first; `200 []` means no notes. `404` means the older adult has no active follow-up.
- Register: `POST /api/v1/older-adults/{olderAdultId}/notes` with body `CreateCaregiverNoteResource` `{ "familiarId": String, "text": String }` returns the stored note (`201`). This endpoint has no `caregiverId` query: the app sends the signed-in caregiver id as `familiarId`. `400` invalid note, `404` no active follow-up.

`CaregiverNoteResource`: `{ id: int64, text: String, recordedAt: date-time, familiarId: String }`.

`text` has `maxLength` 1000 and `minLength` 0. The app trims the text, refuses blank notes before sending them and allows up to 1000 characters. Notes belong to the follow-up of the older adult: there is no `alertId`, so "Add follow-up note" on an alert stores a general note that does not show which alert it came from. A note written by another caregiver of the same adult appears as "Another caregiver", because the resource carries only `familiarId`, not a name.

## Contact from an alert (US-29)

- Channel: `GET /api/v1/older-adults/{olderAdultId}/contact-channel?caregiverId=` returns `ContactChannelResource` `{ type: "PHONE" | "WHATSAPP", value: String }`. `404` means no follow-up or no contact channel; the app shows the disabled "Contact unavailable" button.
- Name: `GET /api/v1/older-adults/{olderAdultId}` returns `OlderAdultProfileResource`; only `fullName` is read, for "Contact <first name>". It has no `caregiverId` query. If it fails, the button reads "Contact" and contact still works.

PHONE opens the dialer with the number typed (`ACTION_DIAL`, no `CALL_PHONE` permission). WHATSAPP opens `https://wa.me/<digits>`, which WhatsApp or the browser handles. The API returns one channel, so the app never offers a choice between phone and WhatsApp. The family summary "Contact" action uses the same lookup and launcher.
