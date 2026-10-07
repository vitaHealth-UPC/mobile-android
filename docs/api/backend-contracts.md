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

- `POST /api/v1/inventories` â€” register the initial stock (US-40). Body `{ "medicationId": String, "initialQuantity": Int, "replenishmentThreshold": Int }`. `201` returns the inventory resource. Only one inventory may exist per medication, and initial registration requires an existing active medication.
- `GET /api/v1/inventories/{medicationId}` â€” remaining stock, threshold, low-stock flag and batches (US-41, US-42). `200` returns the inventory resource; `404` means no inventory is registered yet.
- `POST /api/v1/inventories/{medicationId}/replenishments` â€” add a batch and increase stock (US-43). Body `{ "quantity": Int, "lot": String? }`. `201` returns the updated inventory resource.

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

Quantity/threshold are validated in the application layer with the `Quantity` (> 0) and `ReorderThreshold` (>= 0) value objects before any request, so invalid input (the "Cantidad invÃ¡lida" case) never reaches the network. Those app-born failures use the codes `INVALID_QUANTITY`, `INVALID_THRESHOLD` and `INVALID_MEDICATION_REFERENCE`. `GetInventoryStockQueryHandler` passes `INVENTORY_NOT_FOUND` through unchanged; the ViewModel turns it into the "not initialized" state. Each code (backend and app-born) resolves to a localized string in `:inventory` `res/values` and `res/values-b+es+419`; the UI never shows the backend `message` directly.

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
