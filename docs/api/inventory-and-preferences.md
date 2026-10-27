# Inventory, accessibility and preferences

`inventory` manages the device's view of medication stock. `preferences` maps accessibility and notification settings between Android presentation, DataStore and the user-scoped backend contract.

## Inventory endpoints

| Action | Method and path | Effect |
| --- | --- | --- |
| Initialize stock | `POST /api/v1/inventories` | Creates inventory for a medication |
| Read stock | `GET /api/v1/inventories/{medicationId}` | Returns remaining quantity, threshold and batches |
| Replenish | `POST /api/v1/inventories/{medicationId}/replenishments` | Records a batch and returns updated inventory |

Initial registration sends `medicationId`, `initialQuantity` and `replenishmentThreshold`. Replenishment sends a positive `quantity` and an optional `lot`. The server provides `remainingStock`, `lowStock`, available batch metadata and, where calculable, `daysRemaining`.

Quantity and threshold value objects validate user input in the application layer. Retrofit results are converted into typed domain errors; the UI uses localized messages rather than displaying untrusted remote text.

The client does not recompute stock consumption from its own dose widgets. Intake and inventory consistency are backend responsibilities.

## Accessibility endpoints

| Action | Method and path |
| --- | --- |
| Read preferences | `GET /api/v1/users/{userId}/preferences` |
| Set text size | `PUT /api/v1/users/{userId}/preferences/text-size` |
| Set high contrast | `PUT /api/v1/users/{userId}/preferences/contrast` |
| Set dark theme | `PUT /api/v1/users/{userId}/preferences/dark-theme` |
| Set reduced motion | `PUT /api/v1/users/{userId}/preferences/reduced-motion` |
| Set reading assistance | `PUT /api/v1/users/{userId}/preferences/reading-assistance` |
| Set voice confirmation | `PUT /api/v1/users/{userId}/preferences/voice-confirmation` |

`textSize` uses the defined values `SMALL`, `MEDIUM`, `LARGE` and `EXTRA_LARGE`. Toggle operations send `{ "enabled": true }` or `false`. The backend response is mapped to the application's preference model.

Owner-scoped DataStore retains the device representation and the fields selected for synchronization. The preference adapter separates a local user action from the server acknowledgement.

## Notification preferences

| Action | Method and path |
| --- | --- |
| Update channels and quiet hours | `PUT /api/v1/users/{userId}/notification-preferences` |
| Update reminder policy | `PUT /api/v1/users/{userId}/notification-preferences/policy` |

Notification preference resources carry quiet hours and channel selections; the policy resource carries configured reminder/escalation values and contacts. Configuration storage and actual notification delivery are distinct operations.

## Localization

English and Spanish resources map stable API error codes and validation results to UI labels. HTTP identity and authorization rules remain unchanged when the displayed language changes.

See [Push and voice integration](notifications-and-voice.md), [Local persistence](../architecture/offline-sync.md).
