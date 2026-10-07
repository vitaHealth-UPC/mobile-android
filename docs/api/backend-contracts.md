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

- `POST /api/v1/inventories` — register the initial stock (US-40). Body `{ "medicationId": String, "initialQuantity": Int, "replenishmentThreshold": Int }`. `201` returns the inventory resource. Only one inventory may exist per medication.
- `GET /api/v1/inventories/{medicationId}` — remaining stock, threshold, low-stock flag and batches (US-41, US-42). `200` returns the inventory resource; `404` means no inventory is registered yet.
- `POST /api/v1/inventories/{medicationId}/replenishments` — add a batch and increase stock (US-43). Body `{ "quantity": Int }`. `201` returns the updated inventory resource.

### Response shapes

`InventoryResource`:
`{ id, medicationId, remainingStock: Int, replenishmentThreshold: Int, lowStock: Boolean, batches: Batch[], createdAt: Instant, updatedAt: Instant }`. `lowStock` is `true` when `remainingStock <= replenishmentThreshold`; the app consumes this flag and does not recompute the rule.

`Batch`: `{ id, quantity: Int, registeredAt: Instant }`.

`ErrorResource`: `{ code: String, message: String }`. The `code` is stable and meant for clients; the `message` is localized by `Accept-Language`.

Timestamps are ISO-8601 strings on the wire. The shared Retrofit uses the default Gson, which does not deserialize `java.time.Instant`, so DTO time fields are `String` and parsed in `RemoteInventoryRepository`.

### Backend error codes

`INVALID_QUANTITY` (`400`), `INVENTORY_NOT_FOUND` (`404`), `INVENTORY_ALREADY_EXISTS` (`409`), `CONCURRENT_UPDATE` (`409`, optimistic-lock conflict on replenishment), `INSUFFICIENT_STOCK` (`409`, only reachable through intake consumption, not these endpoints), `VALIDATION_ERROR` (`400`).

### Mobile mapping

`RemoteInventoryRepository` maps per `(endpoint, HTTP status)` following the contract above, so each stable `AppResult.Failure.code` is derived without parsing the error body:

- `getStock`: `404 -> INVENTORY_NOT_FOUND`, other -> `REQUEST_FAILED`.
- `registerInitialInventory`: `400 -> INVALID_QUANTITY`, `409 -> INVENTORY_ALREADY_EXISTS`, other -> `REQUEST_FAILED`.
- `registerReplenishment`: `400 -> INVALID_QUANTITY`, `404 -> INVENTORY_NOT_FOUND`, `409 -> CONCURRENT_UPDATE`, other -> `REQUEST_FAILED`.
- A thrown I/O exception -> `NETWORK_UNAVAILABLE`; unparseable timestamps -> `INVALID_RESPONSE`.

Quantity/threshold are validated in the application layer with the `Quantity` (> 0) and `ReorderThreshold` (>= 0) value objects before any request, so invalid input (the "Cantidad inválida" case) never reaches the network. Those app-born failures use the codes `INVALID_QUANTITY`, `INVALID_THRESHOLD` and `INVALID_MEDICATION_REFERENCE`. `GetInventoryStockQueryHandler` passes `INVENTORY_NOT_FOUND` through unchanged; the ViewModel turns it into the "not initialized" state. Each code (backend and app-born) resolves to a localized string in `:inventory` `res/values` and `res/values-b+es+419`; the UI never shows the backend `message` directly.

### Pending backend contract

- **`lot`** on batches. The US-43 ticket describes "quantity, lot and timestamp" and the mockup shows a "Lote / nota" field, but `Batch` currently exposes only quantity and timestamp. The field is intentionally omitted from the app until the backend adds it.
- **`daysRemaining`** ("≈ X días de tratamiento"). Not returned by `GET`. It is modeled as an optional `InventoryStockReadModel.daysRemaining: Int? = null`; the UI shows "Sin estimación". The estimate needs a consumption rate (doses/day, derivable from the Treatment regimen) and belongs in the backend read model, not in the app.
- **Medication unit** ("comprimidos") and display name. Inventory only knows `medicationId`; it does not return the medication name or presentation. The app passes them in by composition (`:app` from `TreatmentDetail`). The unit is not available at that entry point yet, so the screen receives a blank unit and renders counts without a unit word ("5 restantes"). A future source is `GET /api/v1/medications/{id}` (returns `name` + `presentation`).
