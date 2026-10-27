package com.vitahealth.tata.identity.infrastructure.remote

import com.google.gson.Gson
import com.google.gson.stream.MalformedJsonException
import com.vitahealth.tata.identity.application.ports.SessionAccessRepository
import com.vitahealth.tata.identity.application.readmodels.SessionSubject
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import retrofit2.Response
/** Exchanges credentials and persists a session only after validating its authenticated subject. */
class RemoteSessionAccessRepository(
    private val api: SessionApiService,
    private val sessions: SessionStore,
    private val authenticatedApiFor: (StoredSession) -> SessionApiService = { api },
) : SessionAccessRepository {
    /** Authenticates the caregiver and stores the validated session credentials. */
    override suspend fun signIn(email: String, password: String): AppResult<SessionSubject> =
        authenticate("CAREGIVER") { api.signIn(SignInRequest(email.trim(), password)) }

    /** Exchanges signed provider evidence; persists only the returned Tata session. */
    override suspend fun signInSocial(firebaseIdToken: String): AppResult<SessionSubject> {
        if (firebaseIdToken.isBlank() || firebaseIdToken.length > 16384) {
            return AppResult.Failure("Invalid social credential", code = "INVALID_SOCIAL_CREDENTIAL")
        }
        return authenticate("CAREGIVER") {
            api.signInSocial(SocialSessionRequest(firebaseIdToken))
        }
    }

    /** Authorizes provider binding with the original caregiver session, never an anonymous call. */
    override suspend fun linkSocial(firebaseIdToken: String): AppResult<Unit> = request {
        if (firebaseIdToken.isBlank() || firebaseIdToken.length > 16384) {
            return@request AppResult.Failure(
                "Invalid social credential", code = "INVALID_SOCIAL_CREDENTIAL")
        }
        val captured = sessions.session()
            ?: return@request AppResult.Failure(
                "Tata session required", code = "AUTHENTICATION_REQUIRED")
        if (!captured.expiresAt.isAfter(Instant.now()) || captured.owner?.role != "CAREGIVER") {
            return@request AppResult.Failure(
                "Caregiver session required", code = "AUTHENTICATION_REQUIRED")
        }
        val response = authenticatedApiFor(captured).linkSocial(
            SocialSessionRequest(firebaseIdToken))
        currentCoroutineContext().ensureActive()
        if (!sessions.isCurrent(captured)) return@request sessionChanged()
        if (response.isSuccessful) AppResult.Success(Unit) else failure(response)
    }

    /** Authenticates the older adult using the device's remembered profile identifier. */
    override suspend fun signInWithPin(
        olderAdultId: String,
        pin: String,
    ): AppResult<SessionSubject> =
        authenticate("OLDER_ADULT", olderAdultId) {
            api.signInWithPin(PinAccessRequest(olderAdultId, pin))
        }

    /** Registers a PIN using the authenticated consent/setup session. */
    override suspend fun registerPin(olderAdultId: String, pin: String): AppResult<Unit> = request {
        val captured =
            sessions.session()
                ?: return@request AppResult.Failure(
                    "Session required",
                    code = "AUTHENTICATION_REQUIRED",
                )
        if (!captured.expiresAt.isAfter(Instant.now()))
            return@request AppResult.Failure("Session required", code = "AUTHENTICATION_REQUIRED")
        if (
            captured.owner?.subjectId != olderAdultId ||
                captured.owner.role !in setOf("OLDER_ADULT", "LINK_SETUP")
        ) {
            return@request AppResult.Failure(
                "Adult session required",
                code = "OLDER_ADULT_SESSION_REQUIRED",
            )
        }
        val response =
            authenticatedApiFor(captured).registerPin(PinAccessRequest(olderAdultId, pin))
        currentCoroutineContext().ensureActive()
        if (!sessions.isCurrent(captured)) return@request sessionChanged()
        if (response.isSuccessful) AppResult.Success(Unit) else failure(response)
    }

    /** Restores an active application subject, keeping consent-only sessions restricted. */
    override suspend fun current(): AppResult<SessionSubject> = request {
        val captured = sessions.session()
        if (captured == null || !captured.expiresAt.isAfter(Instant.now())) {
            return@request AppResult.Failure("Session required", code = "AUTHENTICATION_REQUIRED")
        }

        val expectedToken = captured.accessToken
        val response = authenticatedApiFor(captured).current()
        currentCoroutineContext().ensureActive()
        if (!response.isSuccessful) {
            if (response.code() == 401) sessions.clearIfCurrent(expectedToken)
            return@request failure(response)
        }

        val body = response.body() ?: return@request invalidResponse()
        when {
            body.role == "LINK_SETUP" ->
                AppResult.Failure("Consent required", code = "CONSENT_REQUIRED")
            body.subjectId.isBlank() || body.role !in setOf("CAREGIVER", "OLDER_ADULT") ->
                invalidResponse()
            else -> {
                val owner = SessionOwner(body.subjectId, body.role)
                if (!sessions.bindOwner(expectedToken, owner)) invalidResponse()
                else AppResult.Success(SessionSubject(body.subjectId, body.role))
            }
        }
    }

    /** Clears local credentials even when remote session revocation fails. */
    override suspend fun signOut(): AppResult<Unit> = request {
        val captured = sessions.session() ?: return@request AppResult.Success(Unit)
        val expectedToken = captured.accessToken
        val response =
            try {
                authenticatedApiFor(captured).signOut()
            } finally {
                sessions.clearIfCurrent(expectedToken)
            }
        if (response.isSuccessful || response.code() == 401) {
            AppResult.Success(Unit)
        } else {
            failure(response)
        }
    }

    private suspend fun authenticate(
        role: String,
        expectedSubjectId: String? = null,
        call: suspend () -> Response<SessionAccessResponse>,
    ): AppResult<SessionSubject> = request {
        val expectedSession = sessions.session()
        val response = call()
        currentCoroutineContext().ensureActive()
        if (!sessions.isCurrent(expectedSession)) return@request sessionChanged()
        if (!response.isSuccessful) return@request failure(response)

        val body = response.body() ?: return@request invalidResponse()
        val subjectId = if (role == "CAREGIVER") body.accountId else body.olderAdultId
        if (
            subjectId.isNullOrBlank() ||
                body.accessToken.isBlank() ||
                (expectedSubjectId != null && subjectId != expectedSubjectId)
        ) {
            return@request invalidResponse()
        }

        // Validate every response field before replacing a previously valid session.
        val expiry = Instant.parse(body.expiresAt)
        require(expiry.isAfter(Instant.now()))
        val subject = SessionSubject(subjectId, role)
        if (
            !sessions.replaceIfCurrent(
                expectedSession,
                StoredSession(body.accessToken, expiry, SessionOwner(subjectId, role)),
            )
        ) {
            return@request sessionChanged()
        }
        AppResult.Success(subject)
    }

    private fun invalidResponse() = AppResult.Failure("Invalid session", code = "INVALID_RESPONSE")

    private fun sessionChanged() = AppResult.Failure("Session changed", code = "SESSION_CHANGED")

    private fun failure(response: Response<*>): AppResult.Failure {
        val code =
            runCatching {
                    response.errorBody()?.charStream()?.use { reader ->
                        Gson().fromJson(reader, ErrorEnvelope::class.java)?.code
                    }
                }
                .getOrNull()
        val fallback = if (response.code() == 401) "INVALID_CREDENTIALS" else "REQUEST_FAILED"
        return AppResult.Failure("Access failed", code = code ?: fallback)
    }

    /** Optional stable error code returned by the session endpoints. */
    private data class ErrorEnvelope(val code: String?)

    private suspend fun <T> request(call: suspend () -> AppResult<T>): AppResult<T> =
        try {
            call()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: MalformedJsonException) {
            AppResult.Failure("Invalid response", code = "INVALID_RESPONSE")
        } catch (exception: IOException) {
            AppResult.Failure(
                "Network unavailable",
                cause = exception,
                code = "NETWORK_UNAVAILABLE",
            )
        } catch (exception: RuntimeException) {
            AppResult.Failure("Invalid response", cause = exception, code = "INVALID_RESPONSE")
        }
}
