# Family monitoring and adherence analytics

`monitoring` composes caregiver-facing status, alerts, history, contacts and notes from protected backend resources. `analytics` consumes the backend's adherence summaries and insights.

## Family status and alert resources

| Action | Method and path |
| --- | --- |
| Caregiver status | `GET /api/v1/older-adults/{olderAdultId}/status?caregiverId=...` |
| Alert detail | `GET /api/v1/older-adults/{olderAdultId}/alerts/{alertId}?caregiverId=...` |
| Change alert status | `PUT /api/v1/older-adults/{olderAdultId}/alerts/{alertId}/status?caregiverId=...` |
| Alert timeline | `GET /api/v1/older-adults/{olderAdultId}/alerts/{alertId}/timeline?caregiverId=...` |
| Intake history | `GET /api/v1/older-adults/{olderAdultId}/intakes?caregiverId=...&days=...` |
| Contact channel | `GET /api/v1/older-adults/{olderAdultId}/contact-channel?caregiverId=...` |

The caregiver status response provides `openAlerts` for the list. An alert ID is represented as `Long`, while its corresponding intake ID is a UUID string. Alert detail and timeline retain the backend's recorded state and chronology.

Status update requests use the permitted backend transitions, with the client refreshing the detail on a conflict. Contact-channel responses identify a phone or WhatsApp destination; the device opens its dialer or HTTPS handler rather than placing a phone call directly.

## Notes

| Action | Method and path |
| --- | --- |
| List caregiver notes | `GET /api/v1/older-adults/{olderAdultId}/notes?caregiverId=...` |
| Create caregiver note | `POST /api/v1/older-adults/{olderAdultId}/notes` |
| List private adult notes | `GET /api/v1/me/notes` |
| Create private adult note | `POST /api/v1/me/notes` |

Caregiver notes and adult-owned personal notes are separate resources. The server establishes the note owner through the session and authorized relationship.

## Adherence analytics

| Action | Method and path |
| --- | --- |
| Summary for a period | `GET /api/v1/older-adults/{olderAdultId}/adherence/summary` |
| Specific treatment summary | `GET /api/v1/older-adults/{olderAdultId}/adherence/treatments/{treatmentId}/summary` |
| Insights | `GET /api/v1/older-adults/{olderAdultId}/adherence/insight` |

The analytics client submits the period (`days`) and IANA calendar zone (`zone`). An optional `treatmentId` scopes a summary. Counts, percentages and insights come from the backend; the mobile application displays them without synthesizing missing clinical data.

## Security boundary

Protected caregiver resources require an authenticated caregiver with a valid care link and consent. A server refusal is rendered as an access or state error, not interpreted as an empty successful summary.

See [Identity and care links](identity-and-care-links.md) and [Contract guidelines](contract-guidelines.md).
