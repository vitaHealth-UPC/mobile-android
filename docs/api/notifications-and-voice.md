# Push and voice adapters

The Android application provides native device interfaces for notifications and spoken dose confirmation. The backend owns authorization and changes to clinical state.

## Push-device registration

| Action | Method and path |
| --- | --- |
| Register or rotate device token | `PUT /api/v1/push-devices/{installationId}` |
| Remove device registration | `DELETE /api/v1/push-devices/{installationId}` |

`PushDeviceApiService` sends the installation's Firebase token for the authenticated session. The backend determines account ownership from the bearer credential, not from a client-supplied owner ID.

`OmissionMessagingService` receives Firebase messages. The notification adapters validate the payload, use the active session and navigate to the associated dose detail or caregiver alerts. The native notification system is separate from the backend's omission and escalation decisions.

The push payload includes a version, delivery identity, related intake and older-adult references, event kind, medication display name and scheduled time. Routing validates the relationship before opening protected content.

## Session handling

The push registration coordinator operates with a captured session and must not reuse the result after a user or account change. Sign-out removes stored credentials and coordinates registration cleanup. Sensitive content is not obtained solely from the notification's display payload.

## Native voice capture

`AndroidVoiceAudioRecorder` obtains microphone data and builds a RIFF/WAV container using `Pcm16WavEncoder`:

| Property | Value |
| --- | --- |
| Sample rate | 16,000 Hz |
| Channels | 1 (mono) |
| Encoding | Signed PCM, 16-bit |
| MIME type | `audio/wav` |
| Multipart part | `audio` |
| Filename | `confirmation.wav` |

`VoiceConfirmationApiService` sends this recording to `POST /api/v1/intakes/{intakeId}/voice-confirmation?language=...`. The server performs speech processing and validates the authorized intake transition.

A successful voice response includes the matching intake, confirmed timestamp and server status. Recognition outcomes such as `NOT_RECOGNIZED` and `PROVIDER_UNAVAILABLE` do not create a local confirmation.

## Provider integration

Firebase and the backend speech/notification providers are configured externally. Their presence does not change the authorization model or the source of truth for intakes.

See [Treatments and intakes](treatments-and-intakes.md) and [Identity and care links](identity-and-care-links.md).
