# Identity, subscriptions and care links

The `identity` and `carelink` contexts expose the registration, authentication, profile and consent journeys through separate Retrofit services.

## Identity and account access

| Operation | Method and path | Android adapter |
| --- | --- | --- |
| Register account | `POST /api/v1/accounts` | `IdentityApiService` |
| Verify account | `POST /api/v1/accounts/verification` | `IdentityApiService` |
| Request verification email | `POST /api/v1/email-verification-requests` | `IdentityApiService` |
| Password sign-in | `POST /api/v1/sessions` | `SessionApiService` |
| Social sign-in | `POST /api/v1/social-sessions` | `SessionApiService` |
| Link social identity | `PUT /api/v1/me/social-identities` | `SessionApiService` |
| Establish PIN credential | `POST /api/v1/pin-credentials` | `SessionApiService` |
| PIN sign-in | `POST /api/v1/pin-sessions` | `SessionApiService` |
| Resolve active session | `GET /api/v1/sessions/current` | `SessionApiService` |
| Sign out | `DELETE /api/v1/sessions` | `SessionApiService` |

Session information is held in the encrypted local session adapter and used by the shared Retrofit client. Access to clinical information follows the role and care relationship authorized by the backend.

Social identity is verified through the configured Firebase providers before exchange for a Tata session. Linking a social identity to an existing Tata account requires that account's authenticated session.

## Password recovery

| Operation | Method and path | Expected successful status |
| --- | --- | --- |
| Request recovery | `POST /api/v1/password-recovery-requests` | `202` |
| Complete recovery | `POST /api/v1/password-recoveries` | `204` |

Recovery completion submits `email`, `code` and `newPassword`. The anonymous recovery client does not automatically establish an authenticated session after a password change. Recovery request bodies redact their contents in diagnostic string output.

## Plans and subscription

| Operation | Method and path |
| --- | --- |
| List available plans | `GET /api/v1/plans` |
| Read account subscription | `GET /api/v1/accounts/{accountId}/subscription` |
| Change account plan | `PUT /api/v1/accounts/{accountId}/subscription` |

`SubscriptionApiService` maps plan and subscription DTOs to domain values. Plan capabilities supplied by the backend are interpreted by the client without treating unknown capabilities as errors.

## Care links and consent

| Operation | Method and path |
| --- | --- |
| List a caregiver's linked profiles | `GET /api/v1/care-links?caregiverId=...` |
| Register an older-adult profile | `POST /api/v1/older-adults` |
| Generate linking code | `POST /api/v1/care-links/linking-codes` |
| Accept linking code | `POST /api/v1/care-links/acceptances` |
| Register consent | `POST /api/v1/care-links/{careLinkId}/consent` |
| Withdraw consent | `DELETE /api/v1/care-links/{careLinkId}/consent` |
| Read an older-adult profile | `GET /api/v1/older-adults/{olderAdultId}` |

The backend owns link validity, caregiver/adult authorization and consent status. Android routes users according to the returned state and does not grant itself access from possession of a linking code alone.

Profile and emergency-contact updates use authorized care-link adapters. The corresponding update route for the emergency contact is `PUT /api/v1/older-adults/{olderAdultId}/emergency-contact`.

See [Contract guidelines](contract-guidelines.md) and [Mobile architecture](../architecture/mobile-architecture.md).
