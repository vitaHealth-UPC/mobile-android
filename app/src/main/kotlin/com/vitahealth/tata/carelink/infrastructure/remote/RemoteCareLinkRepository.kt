package com.vitahealth.tata.carelink.infrastructure.remote

import com.google.gson.stream.MalformedJsonException
import com.vitahealth.tata.carelink.domain.model.entities.CareLink
import com.vitahealth.tata.carelink.domain.model.entities.OlderAdultProfile
import com.vitahealth.tata.carelink.domain.model.valueobjects.CareLinkStatus
import com.vitahealth.tata.carelink.domain.repositories.CareLinkRepository
import com.vitahealth.tata.shared.infrastructure.security.NoSessionStore
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import retrofit2.Response

/** Exchanges linking and consent resources without accepting malformed server state. */
class RemoteCareLinkRepository(
    private val api: CareLinkApiService,
    private val sessions: SessionStore = NoSessionStore,
    private val apiFor: (StoredSession?) -> CareLinkApiService = { api },
) : CareLinkRepository {
    override suspend fun acceptLink(caregiverId: String, code: String): AppResult<CareLink> =
        requestCareLink(sessionRole = "LINK_SETUP", matches = { it.caregiverId == caregiverId }) {
            capturedApi ->
            capturedApi.acceptLink(
                AcceptCareLinkRequest(
                    caregiverId = caregiverId,
                    code = code.trim().uppercase(Locale.ROOT),
                )
            )
        }

    override suspend fun registerConsent(
        careLinkId: String,
        accepted: Boolean,
    ): AppResult<CareLink> =
        requestCareLink(
            sessionRole = "OLDER_ADULT",
            matches = {
                it.id == careLinkId &&
                    it.consentGranted == accepted &&
                    it.status == if (accepted) CareLinkStatus.CONFIRMED else CareLinkStatus.REVOKED
            },
        ) { capturedApi ->
            capturedApi.registerConsent(
                careLinkId = careLinkId,
                request = RegisterConsentRequest(accepted = accepted),
            )
        }

    override suspend fun getOlderAdult(olderAdultId: String): AppResult<OlderAdultProfile> {
        return try {
            val expectedSession = sessions.session()
            val response = apiFor(expectedSession).getOlderAdult(olderAdultId)
            currentCoroutineContext().ensureActive()
            if (!sessions.isCurrent(expectedSession)) return sessionChanged()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                require(body.id == olderAdultId && body.fullName.isNotBlank())
                AppResult.Success(
                    OlderAdultProfile(
                        id = body.id,
                        fullName = body.fullName,
                        birthDate = LocalDate.parse(body.birthDate),
                        emergencyContactName = body.emergencyContactName,
                        emergencyContactRelationship = body.emergencyContactRelationship,
                        emergencyContactPhone = body.emergencyContactPhone,
                    )
                )
            } else {
                val (message, code) =
                    when (response.code()) {
                        404 ->
                            "No encontramos el perfil del adulto mayor." to "OLDER_ADULT_NOT_FOUND"
                        else -> "No pudimos cargar el perfil vinculado." to "REQUEST_FAILED"
                    }
                AppResult.Failure(message = message, code = code)
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: MalformedJsonException) {
            AppResult.Failure("Invalid care-link response", code = "INVALID_RESPONSE")
        } catch (exception: IOException) {
            AppResult.Failure(
                message = "No hay conexión disponible.",
                cause = exception,
                code = "NETWORK_UNAVAILABLE",
            )
        } catch (exception: RuntimeException) {
            AppResult.Failure(
                message = "Invalid care-link response",
                cause = exception,
                code = "INVALID_RESPONSE",
            )
        }
    }

    private suspend fun requestCareLink(
        sessionRole: String,
        matches: (CareLink) -> Boolean,
        call: suspend (CareLinkApiService) -> Response<CareLinkResponse>,
    ): AppResult<CareLink> {
        return try {
            val expectedSession = sessions.session()
            val response = call(apiFor(expectedSession))
            currentCoroutineContext().ensureActive()
            if (!sessions.isCurrent(expectedSession)) return sessionChanged()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                val link = body.toDomain()
                require(matches(link)) { "Care-link response does not match the request" }
                require((body.accessToken == null) == (body.expiresAt == null))
                if (sessionRole == "OLDER_ADULT" && !link.consentGranted) {
                    require(body.accessToken == null) {
                        "Rejected consent cannot establish adult access"
                    }
                }
                if (body.accessToken != null && body.expiresAt != null) {
                    require(body.accessToken.isNotBlank())
                    val expiry = Instant.parse(body.expiresAt)
                    require(expiry.isAfter(Instant.now()))
                    val replacement =
                        StoredSession(
                            body.accessToken,
                            expiry,
                            SessionOwner(link.olderAdultId, sessionRole),
                        )
                    if (!sessions.replaceIfCurrent(expectedSession, replacement))
                        return sessionChanged()
                }
                AppResult.Success(link)
            } else {
                val (message, code) =
                    when (response.code()) {
                        400 -> "El código de vinculación no es válido." to "INVALID_LINKING_CODE"
                        403 ->
                            "La operación requiere una cuenta habilitada y consentimiento válido." to
                                "CONSENT_REQUIRED"
                        404 ->
                            "No encontramos la solicitud de vinculación." to "CARE_LINK_NOT_FOUND"
                        410 ->
                            "El código venció o ya fue utilizado." to "LINKING_CODE_EXPIRED_OR_USED"
                        else -> "No pudimos completar la vinculación." to "REQUEST_FAILED"
                    }
                AppResult.Failure(message = message, code = code)
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: MalformedJsonException) {
            AppResult.Failure("Invalid care-link response", code = "INVALID_RESPONSE")
        } catch (exception: IOException) {
            AppResult.Failure(
                message = "No hay conexión disponible.",
                cause = exception,
                code = "NETWORK_UNAVAILABLE",
            )
        } catch (exception: RuntimeException) {
            AppResult.Failure(
                message = "Invalid care-link response",
                cause = exception,
                code = "INVALID_RESPONSE",
            )
        }
    }

    private fun sessionChanged() =
        AppResult.Failure(
            "La sesión cambió. Abre nuevamente la vinculación.",
            code = "SESSION_CHANGED",
        )
}
