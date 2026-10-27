# Android REST contract guidelines

The Tata backend (`vitaHealth-UPC/web-services`) owns the API contract. Android consumes the public resources through Retrofit and keeps protocol concerns inside each context's `infrastructure/remote` package.

## Contract boundaries

1. Retrofit services declare HTTP methods, paths, query arguments and request/response DTOs.
2. Repository adapters validate and map DTOs into application read models or domain types.
3. Application command/query handlers enforce client-side input rules and coordinate operations.
4. ViewModels turn typed results into localized UI states. They do not handle raw Retrofit `Response` objects.
5. Server authorization, consent, status transitions and resource ownership are not replaced by client checks.
6. External service adapters use environment configuration rather than hardcoded credentials.

## Authentication and errors

Authenticated endpoints use the active session's bearer credential. PIN-based adult access and caregiver sessions have different authorized operations; the server derives identity from the session and checks care relationships.

| HTTP outcome | Client interpretation |
| --- | --- |
| `200` / `201` | Read or mutation response is mapped and validated |
| `202` / `204` | An accepted request or a successful no-content operation |
| `400` | Invalid request; use stable machine-readable error code where available |
| `401` | Session absent or expired |
| `403` | Access or consent restriction |
| `404` | Unknown resource or unavailable link/record |
| `409` | Conflicting state transition or concurrent change |

Response DTOs and error titles/codes are mapped to defined client failures. Network errors are separate from server-declared business errors; coroutine cancellation is propagated.

## Identifiers and timestamps

Account, older-adult, care-link, medication, treatment and intake identifiers follow the backend's UUID-string contracts. Monitoring alert IDs are numeric (`Long`). ISO-8601 timestamps are parsed at the remote adapter boundary and rendered in the relevant local time zone.

The weekly agenda sends two explicit ISO instants (`from` inclusive and `to` exclusive). Mobile does not infer a confirmed, late or omitted clinical outcome from its own clock.

## API documentation by context

- [Identity and care links](identity-and-care-links.md)
- [Treatments and intakes](treatments-and-intakes.md)
- [Monitoring and analytics](monitoring-and-analytics.md)
- [Inventory and preferences](inventory-and-preferences.md)
- [Push and voice integration](notifications-and-voice.md)

Local storage and queued confirmation behavior are documented under [Database](../architecture/offline-sync.md).
