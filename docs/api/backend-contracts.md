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

## Accessibility preferences (US-35 and following)

`GET /api/v1/users/{userId}/preferences` returns `textSize` (`SMALL | MEDIUM | LARGE | EXTRA_LARGE`), `highContrast`, `reducedMotion`, `readingAssistance`, `voiceConfirmationEnabled`, `quietHours` (`{start, end}` or null) and `notificationChannels` (`PUSH | SMS | EMAIL`). A user who never saved anything gets the defaults. `PUT /api/v1/users/{userId}/preferences/text-size` takes `{ "textSize": "LARGE" }` and returns the same representation. The endpoints have no authentication yet: `userId` is the account id.

The app keeps a copy in DataStore (`tata_accessibility`) and applies it before any network call. A change made without connection stays on the device, is flagged as pending and is sent on the next `sync`. A `400` from the backend rolls the device copy back. The "Large text" switch maps to `LARGE` (on) and `MEDIUM` (off).

`PUT /api/v1/users/{userId}/preferences/contrast` takes `{ "enabled": true }` and returns the same preferences representation (US-36). The offline rule is the same as for the text size.
