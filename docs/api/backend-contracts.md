# Backend contracts

Android consumes the `vitaHealth-UPC/web-services` API but does not mirror backend internals.

Rules:

- Retrofit request/response DTOs stay in each context's `infrastructure/remote` package.
- DTOs are mapped before they cross into Domain or Presentation.
- API paths and payloads must be documented here when the corresponding backend contract is finalized.

## Intake confirmation (TS-04 / US-06 touch)

`POST /api/v1/intakes/{intakeId}/confirmation`, body `{ "channel": "TOUCH" }` (`VOICE` shares the same invariant). Returns the same intake representation as detail. `404` means missing intake; `409` means definitive omission cannot be overwritten. Retries use the original intake ID and do not create duplicate confirmations. Backend decides `CONFIRMED / LATE / OMITTED`. The response includes `alreadyConfirmed`: `false` for the request that records the intake and `true` for an idempotent replay. This request metadata is not a new intake status. Replays preserve the original confirmation channel and timestamp; Android displays the "Toma ya confirmada" result with "Sin duplicados".

Account, OlderAdult, CareLink, Medication, Treatment and Intake IDs and references are UUID `String` values. Numeric internal IDs from other contexts do not change that contract.

Touch and voice use separate endpoints. `POST /api/v1/intakes/{intakeId}/voice-confirmation` sends multipart part `audio` (`audio/mp4`) and query `language` (`es-419`, or `en-US` for EN). Android requests microphone permission and records eight seconds in private cache, then uploads and deletes the temporary recording. Departure or cancellation releases the microphone. The UI shows listening only after capture starts and processing while the server validates the recording.

Voice results are `CONFIRMED`, `ALREADY_CONFIRMED`, `NOT_RECOGNIZED`, `NOT_VALIDATED` or `PROVIDER_UNAVAILABLE`. Success requires the expected intake, a persisted confirmation timestamp and `CONFIRMED`/`LATE` state. Recognition failure leaves the intake unchanged; the screen offers retry and return to touch confirmation. HTTP `409` distinguishes `VOICE_CONFIRMATION_DISABLED` from `INTAKE_NOT_CONFIRMABLE` through the Problem Detail title. Voice recognition requires the configured backend speech provider; the app displays unavailable when that provider is unavailable.

US-23 displays the Figma confirmation composition with the persisted timestamp, actual next intake, adult navigation and late classification notice. A delayed omission event cannot replace an already confirmed intake, including a late one. US-23 uses the same endpoint: a `200` whose intake comes back `LATE` shows "Dose confirmed late" (confirmed within the tolerance period); a `409` shows "Missed dose": the app reads the intake again and keeps the omission, because a later confirmation does not replace it.

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

Quantity/threshold are validated in the application layer with the `Quantity` (> 0) and `ReorderThreshold` (>= 0) value objects before any request, so invalid input (the "Cantidad invÃ¡lida" case) never reaches the network. Those app-born failures use the codes `INVALID_QUANTITY`, `INVALID_THRESHOLD` and `INVALID_MEDICATION_REFERENCE`. `GetInventoryStockQueryHandler` passes `INVENTORY_NOT_FOUND` through unchanged; the ViewModel turns it into the "not initialized" state. Each code (backend and app-born) resolves to a localized string in `app/src/main/res/values/inventory_strings.xml` and `app/src/main/res/values-b+es+419/inventory_strings.xml`; the UI never shows the backend `message` directly.

### Inventory metadata and coverage

Replenishments accept an optional `lot` (trimmed, maximum 200 characters), which is persisted and returned on the batch. The app presents the Figma "Lote / nota" field and the last replenishment metadata.

`daysRemaining` is computed by the backend as remaining stock divided by scheduled daily intake units, rounded down. The current intake model consumes one unit per scheduled intake. A medication without an active scheduled treatment returns null; the app displays "Sin estimaciÃ³n". Medication name and presentation are supplied from the treatment entry point.

## Accessibility preferences (US-35 and following)

`GET /api/v1/users/{userId}/preferences` returns `textSize` (`SMALL | MEDIUM | LARGE | EXTRA_LARGE`), `highContrast`, `reducedMotion`, `readingAssistance`, `voiceConfirmationEnabled`, `quietHours` (`{start, end}` or null) and `notificationChannels` (`PUSH | SMS | EMAIL | CALL`). A missing channel is disabled; an explicit empty list disables every channel. A user who never saved anything gets the defaults. `PUT /api/v1/users/{userId}/preferences/text-size` takes `{ "textSize": "LARGE" }` and returns the same representation. The endpoints require a bearer session authorized for the account identified by `userId`.

The app keeps a copy in DataStore (`tata_accessibility`) and applies it before any network call. A change made without connection stays on the device, is flagged as pending and is sent on the next `sync`. A `400` from the backend rolls the device copy back. The "Large text" switch maps to `LARGE` (on) and `MEDIUM` (off).

`PUT /api/v1/users/{userId}/preferences/contrast` takes `{ "enabled": true }` and returns the same preferences representation (US-36). The offline rule is the same as for the text size.

`PUT /api/v1/users/{userId}/preferences/reduced-motion` (US-37) and `PUT /api/v1/users/{userId}/preferences/voice-confirmation` take `{ "enabled": true }` and return the preferences representation.

`PUT /api/v1/users/{userId}/preferences/reading-assistance` (US-38) takes `{ "enabled": true }` and returns the preferences representation.

`PUT /api/v1/users/{userId}/notification-preferences` (US-39) takes `{ "quietHours": {"start": "22:00", "end": "07:00"} | absent, "channels": [{"type": "PUSH", "enabled": true}, ...] }`, replaces both settings and returns the preferences representation. A `400` means an empty interval or a repeated channel. These two settings have no device copy: they are read and saved online only.

`PUT /api/v1/users/{userId}/notification-preferences/policy` stores `{ "policy": { "reminderMinutes": 10, "pendingAlertMinutes": 20, "omittedAlertMinutes": 120, "escalationMinutes": 180, "dailySummaryTime": "20:00" }, "contacts": [] }`. Delays are minutes from the scheduled intake, increase strictly and stay within one day. Summary time can be null. Contacts require a name and a distinct phone, with at most five recipients. The response and GET include `notificationPolicy` and `emergencyContacts`; changing channels preserves them. Android renders these fields only when the server exposes the policy contract. This stores user configuration; the current omission scheduler still uses its existing configured delays, and summary or emergency-contact delivery is not enabled by storage alone.
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

`PUT /api/v1/older-adults/{olderAdultId}/alerts/{alertId}/status?caregiverId=` with `{ "status": "ATTENDED" | "CLOSED" }` exists for US-31 and is not consumed by US-27.

## Plans and subscription (US-44, US-45)

`GET /api/v1/plans` returns `[{ code, name, monthlyPrice, currency, capabilities[] }]`; today ESSENTIAL (S/ 9.90) and FAMILY (S/ 19.90). `GET /api/v1/accounts/{accountId}/subscription` returns `{ accountId, plan, status, renewsAt }` with status `ACTIVE` and `renewsAt` as an ISO instant. Errors are a problem detail whose `title` is the code: `ACCOUNT_NOT_FOUND` and `PLAN_NOT_FOUND` (404), `ACCOUNT_NOT_ACTIVE` (403). A capability this app does not know is ignored, so the backend can add capabilities without breaking old versions.

`PUT /api/v1/accounts/{accountId}/subscription` takes `{ "planCode": "ESSENTIAL" }` and returns the subscription representation. Choosing the plan the account already has changes nothing. `404 PLAN_NOT_FOUND` for an unknown code and `403 ACCOUNT_NOT_ACTIVE` for an account that is not active.
## Omission push (US-22)

The backend evaluates pending intakes, notification preferences and escalation policy to send reinforced reminders and caregiver alerts.

`PUT /api/v1/push-devices/{installationId}` registers or rotates an installation using `{ "token": String }`. The installation identifier is a canonical UUID; the nonblank token has a maximum length of 4096 characters. Ownership comes from the authenticated bearer, not from fields supplied by Android. A successful response contains `{ installationId, updatedAt }`. `DELETE /api/v1/push-devices/{installationId}` removes the current session's registration and returns `204`.

Android persists an installation UUID independently of account credentials. The process-owned coordinator recovers Firebase's current token when a valid session appears and forwards token rotation from `OmissionMessagingService`. Each request captures its session and rejects obsolete results after a credential change. Before sign-out, authenticated removal has a bounded wait; the account session is then ended through Identity. An unconfigured Firebase build does not register a device.

The direct FCM data payload uses schemaVersion 1 and carries deliveryId, intakeId, olderAdultId, kind, medicationName and scheduledAt. Android validates every required field before displaying localized text. Recipient checks use the current valid session and the remembered caregiver/adult relationship. Delivery retries reuse the notification identity and do not alert again while that notification remains present.

Versioned reminder taps preserve intakeId and open that dose detail. Caregiver taps select an open alert for that exact intake; a missing explicit reference leaves the alert list instead of opening an unrelated alert. Current session access is rechecked before navigation and after alert lookup. Temporary token-fetch or registration failures receive three process-scoped retries after 5, 15 and 60 seconds; authorization and invalid-data failures are not retried.

Clinical push uses authenticated device registration; Android creates no new topic subscriptions. Legacy audiences `user-<olderAdultId>` and `caregivers-of-<olderAdultId>` inferred from persisted targets are retired at process startup through acknowledged Firebase unsubscribe calls, with retry after failures. Targets remain available for navigation and repeated migration after restart. Delivery requires Firebase configuration in Android and a configured backend push provider; the unconfigured backend provider reports failure instead of recording a delivery. HTTP and JVM tests do not demonstrate live FCM delivery or completed retirement on a real installation.

On the home, the second-reminder card derives from the next pending intake and its scheduled time. That presentation does not prove a push was delivered.

### Alert follow-up status (US-31)

`PUT /api/v1/older-adults/{olderAdultId}/alerts/{alertId}/status?caregiverId=` with body `UpdateAlertStatusResource` `{ "status": "ATTENDED" | "CLOSED" }` returns the updated `AlertSummaryResource` (`200`). The schema enum also lists `OPEN`, but the backend only accepts `ATTENDED` and `CLOSED`, so the app never sends `OPEN`. Errors: `400` status not accepted, `404` follow-up or alert not found, `409` the alert cannot move to that status. No `403` is documented; the app treats one like the reads.

The app offers OPEN â†’ ATTENDED ("Mark as attended"), OPEN or ATTENDED â†’ CLOSED ("Close alert", after a confirmation because a closed alert cannot be reopened) and no action on a CLOSED alert. On `409` it reloads the alert detail and shows its current status. A CLOSED alert leaves `openAlerts`; the list refreshes when it is shown again.

## Follow-up notes (US-30)

- List: `GET /api/v1/older-adults/{olderAdultId}/notes?caregiverId=` returns `CaregiverNoteResource[]`, most recent first; `200 []` means no notes. `404` means the older adult has no active follow-up.
- Register: `POST /api/v1/older-adults/{olderAdultId}/notes` with body `CreateCaregiverNoteResource` `{ "familiarId": String, "text": String }` returns the stored note (`201`). This endpoint has no `caregiverId` query: the app sends the signed-in caregiver id as `familiarId`. `400` invalid note, `404` no active follow-up.

`CaregiverNoteResource`: `{ id: int64, text: String, recordedAt: date-time, familiarId: String }`.

`text` has `maxLength` 1000 and `minLength` 0. The app trims the text, refuses blank notes before sending them and allows up to 1000 characters. Notes belong to the follow-up of the older adult: there is no `alertId`, so "Add follow-up note" on an alert stores a general note that does not show which alert it came from. A note written by another caregiver of the same adult appears as "Another caregiver", because the resource carries only `familiarId`, not a name.

## Contact from an alert (US-29)

- Channel: `GET /api/v1/older-adults/{olderAdultId}/contact-channel?caregiverId=` returns `ContactChannelResource` `{ type: "PHONE" | "WHATSAPP", value: String }`. `404` means no follow-up or no contact channel; the app shows the disabled "Contact unavailable" button.
- Name: `GET /api/v1/older-adults/{olderAdultId}` returns `OlderAdultProfileResource`; only `fullName` is read, for "Contact <first name>". It has no `caregiverId` query. If it fails, the button reads "Contact" and contact still works.

PHONE opens the dialer with the number typed (`ACTION_DIAL`, no `CALL_PHONE` permission). WHATSAPP opens `https://wa.me/<digits>`, which WhatsApp or the browser handles. The API returns one channel, so the app never offers a choice between phone and WhatsApp. The family summary "Contact" action uses the same lookup and launcher.

## Older-adult medication catalog

`GET /api/v1/me/medications` returns a list of `{ medication, treatments }`. The owner is derived from the PIN session. Caregiver sessions receive 403; missing sessions receive 401. The client does not send a caregiver ID or owner query parameter.

`MyMedicationsScreen` displays active treatments and history using this catalog. The highlighted next-dose action uses the Intake next-dose contract and its actual intake ID. A missing next dose does not fabricate a time or hide the catalog. Session failures offer PIN access; network failures offer retry.

### Notas personales del adulto

- `GET /api/v1/me/notes`: lista privada, mÃ¡s reciente primero, con `id`, `title`, `text`, `category` y `recordedAt`.
- `POST /api/v1/me/notes`: cuerpo `{title, text, category}`; categorÃ­as `MEDICATION` o `ROUTINE`, tÃ­tulo hasta 100 caracteres y texto hasta 1000. Devuelve 201 y la nota persistida.
- El propietario proviene de la sesiÃ³n PIN. No se acepta selector de propietario. Las sesiones de cuidador y configuraciÃ³n reciben 403.
- Las notas de intervenciÃ³n del cuidador conservan `/api/v1/older-adults/{olderAdultId}/notes`; las notas personales no se incorporan a ese historial.
- Un error de guardado conserva el formulario y sus datos. La lista agrega la respuesta real del servidor y bloquea el doble envÃ­o.

### Omission preserved outcome

After `INTAKE_NOT_CONFIRMABLE`, the detail is read again. An `OMITTED` response preserves the programmed timestamp and renders a disabled confirmation control. The next dose is queried for the same older adult; failure of that query does not replace the omission or invent a next dose. Home navigation remains available through the adult tab bar.

### Recorded dose detail

A recorded dose loads the next intake for its older adult and excludes its own ID. When no next intake is available, the schedule card is labeled as the current dose schedule. Clinical indication and recurrence remain unspecified unless supplied by the contract. ES is the resource fallback; the EN prototype uses localized detail resources.


## Treatment reminder timing

PUT `/api/v1/treatments/{treatmentId}/regimen` distinguishes two values:

- `reminderLeadMinutes`: 0..1440 minutes before the scheduled intake.
- `followUpDelayMinutes`: optional 0..1440 minutes after the scheduled intake before it is reported unconfirmed. Null preserves the global server waiting period for older clients/configurations.

Both values persist and are returned in treatment details. Android sends its post-dose setting as `followUpDelayMinutes` and verifies the server receipt before activation. This extended contract remains local until the accumulated backend changes are published.

Intake queries pending candidates through the evaluation time and applies the owning treatment's follow-up delay through the public Treatment ACL. An absent delay uses the command's global cutoff. Already reported or confirmed outcomes are not replayed. This boundary triggers the unconfirmed event; it does not prove notification delivery.


### Cronología persistida de alertas

`GET /api/v1/older-adults/{olderAdultId}/alerts/{alertId}/timeline?caregiverId={caregiverId}`

Requiere sesión y vínculo con consentimiento vigente. Devuelve una lista
`[{"type":"INTAKE_SCHEDULED","occurredAt":"2026-10-26T12:00:00Z"}, ...]`.
Los tipos disponibles son `INTAKE_SCHEDULED`, `ALERT_GENERATED` y, cuando
existe un cierre persistido, `ALERT_CLOSED`. No se deduce la entrega de un
recordatorio a partir del horario. Android lo consume mediante
`RemoteAlertHistoryReader`. El backend local verifica 401 sin sesión,
403 sin autorización o con consentimiento revocado y 404 para alerta inexistente.
Esta evidencia local no confirma que el endpoint esté publicado en Render.


### Contraste estático de rutas: 26-10-2026

Las 63 combinaciones únicas de método y ruta declaradas por Retrofit tienen
contraparte en los controladores del backend normalizado, incluida cronología.
Esta comparación de código no verifica payloads, llamadas efectivas ni despliegue.
Hay 14 operaciones adicionales del servidor: salud, autorización y detalle de
vínculo, retiro de consentimiento por DELETE, detalle de medicamento, contacto
de emergencia y consultas/consolidaciones analíticas. Su ausencia como declaración
Retrofit requiere revisar consumidores y alternativas existentes antes de decidir
si falta una acción de producto; no justifica crear pantallas automáticamente.
Inventario reproducible local:
`C:/Users/david/.codex/tmp/tata-contract-route-comparison-corrected-20261026.json`.


### Operaciones adicionales: clasificación comprobada

| Operaciones | Clasificación |
| --- | --- |
| GET /health y /actuator/health | Diagnóstico del servicio; no requiere pantalla de negocio. |
| GET care-links/authorization | Autorización explícita; las operaciones protegidas siguen siendo autoridad. |
| GET care-links/{id} | Detalle: Android recibe evidencia de vínculo en aceptación, consentimiento y lista confirmada. |
| DELETE care-links/{id}/consent | Conectado al final del inicio del adulto, con confirmación explícita. El rechazo inicial por POST sigue siendo una operación distinta. |
| GET medications/{id} | Android consulta el catálogo en gestión; comprobar frescura sin añadir consultas redundantes. |
| GET adherence/insights | Alias de adherence/insight ya consumido. |
| GET adherence/weekly, patterns, history, recommendations | Consultas por fechas explícitas; Android usa summary e insight por días y zona. No ofrecen selección arbitraria de fechas. |
| POST adherence/consolidations y GET adherence/consolidations/{id} | Evidencia inmutable; Android consulta proyecciones actuales, sin visor de snapshots. |
| PUT older-adults/{id}/emergency-contact | Conectado desde Persona vinculada > Editar > Editar contacto de emergencia. Usa un comando propio, credencial capturada y comprobación del recibo. Los contactos de política de notificaciones siguen siendo independientes. |

Salvo salud, las rutas incluyen /api/v1; las de Analytics están bajo
older-adults/{olderAdultId}. Priorizar retiro de consentimiento y edición de
contacto. Esta clasificación no certifica que esas acciones estén implementadas.


El formulario de contacto exige nombre, relación y teléfono completos, con límites
160/80/40 equivalentes al servidor. Cancelar no envía cambios. Guardar no modifica
nombre ni nacimiento del adulto y solo cierra tras recibo válido y recarga. Una
respuesta posterior a cambio de sesión se descarta; una denegación elimina la ficha
visible. La verificación contra Render continúa separada de las pruebas locales.


### Retiro de consentimiento: contrato Android

`AuthorizedConsentWithdrawal` resuelve `careLinkId` mediante
`GET /api/v1/sessions/current`, verifica subjectId y rol OLDER_ADULT y después
solicita `DELETE /api/v1/care-links/{careLinkId}/consent` sin cuerpo.
Solo acepta 204 como confirmación. Comprueba sesión vigente antes de escribir y
al recibir el resultado; no borra credenciales ni decide navegación por sí mismo.
Las pruebas HTTP del contrato pasan en `tata-consent-withdrawal-contract-20261026.log`.
La confirmación nativa está conectada en el inicio del adulto. El host coordina
retiro, limpieza del dispositivo y cierre de sesión; los resultados de otra cuenta
se rechazan. Gate local `tata-consent-withdrawal-final-gate-20261026.log`:
844 pruebas JVM / 163 suites, cero fallos y errores, producción y AndroidTest
compilan. Falta verificar persistencia y revocación contra el servidor desplegado.


### Integración HTTP con Spring local: 26-10-2026

La comprobación optativa `:app:verifyLocalBackendContracts` ejecuta los adaptadores
reales de Android contra Spring/JPA y una base H2 nueva, por TCP en localhost.
Verifica contacto persistido, conservación de datos básicos, cronología, retiro
con 204, sesión adulta rechazada con 401 y seguimiento del cuidador rechazado con
403. No utiliza MockWebServer ni respuestas HTTP prefabricadas.

Evidencia: `tata-live-backend-android-contracts-final-20261026.log` en
`C:/Users/david/.codex/tmp`. Procedimiento y alcance:
[Integración local](../testing/local-backend-contracts.md).
Las credenciales pertenecen solo al fixture y se eliminan al cerrar el servidor.
Esta prueba no verifica Render, PostgreSQL ni el recorrido completo desde Compose.


### Identity: opciones visuales y contratos efectivos

| Opción | Implementación actual |
| --- | --- |
| Correo y contraseña | SignInCommandHandler y POST /api/v1/sessions. |
| Crear cuenta | RegisterCaregiverCommandHandler y POST /api/v1/accounts. |
| Verificar correo y reenviar código | POST /api/v1/accounts/verification y /api/v1/email-verification-requests. |
| Cuenta demo | Rellenado explícito del formulario; requiere autenticación del servidor. |
| Google / Facebook | Diálogo informativo; faltan SDK/intercambio de token y contrato de autenticación del backend. |
| Recuperar contraseña | Diálogo de ayuda; falta solicitud y confirmación de restablecimiento en ambos extremos. |

Actualizar Render permite contrastar los contratos implementados. No implementa
por sí mismo los proveedores sociales ni el restablecimiento de contraseña.


### Recuperación de contraseña: contratos implementados

- POST `/api/v1/password-recovery-requests`, cuerpo `{email}`: 202 sin cuerpo
  para correo existente, pendiente o inexistente. Solo cuentas activas generan
  reto y entrega cifrada; reenvíos de la misma cuenta tienen intervalo de 60 s.
- POST `/api/v1/password-recoveries`, cuerpo `{email,code,newPassword}`: 204
  sin credenciales al completar. Evidencia inválida usa 400 con
  `title=INVALID_RECOVERY_CODE`, sin distinguir cuenta inexistente, código usado,
  vencido o bloqueado. El cambio revoca sesiones de la cuenta.

Android dispone de PasswordRecovery y RemotePasswordRecovery, probados con
Retrofit/MockWebServer en `tata-android-recovery-contract-20261026.log`.
El formulario todavía no está conectado. El límite por cuenta no reemplaza
protección de abuso global o por origen en el despliegue. No se envió correo real.
