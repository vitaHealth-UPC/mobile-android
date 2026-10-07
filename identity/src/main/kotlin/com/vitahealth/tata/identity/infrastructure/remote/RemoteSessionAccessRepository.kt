package com.vitahealth.tata.identity.infrastructure.remote

import com.google.gson.Gson
import com.vitahealth.tata.identity.application.SessionAccessRepository
import com.vitahealth.tata.identity.application.SessionSubject
import com.vitahealth.tata.shared.application.SessionStore
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.CancellationException
import retrofit2.Response

class RemoteSessionAccessRepository(private val api: SessionApiService, private val sessions: SessionStore) : SessionAccessRepository {
    override suspend fun signIn(email: String, password: String) = authenticate("CAREGIVER") { api.signIn(SignInRequest(email.trim(), password)) }
    override suspend fun signInWithPin(olderAdultId: String, pin: String) = authenticate("OLDER_ADULT") { api.signInWithPin(PinAccessRequest(olderAdultId, pin)) }
    override suspend fun registerPin(olderAdultId: String, pin: String): AppResult<Unit> = request {
        val response = api.registerPin(PinAccessRequest(olderAdultId, pin))
        if (response.isSuccessful) AppResult.Success(Unit) else failure(response)
    }
    override suspend fun current(): AppResult<SessionSubject> = request {
        if (sessions.accessToken() == null) return@request AppResult.Failure("Session required", code = "AUTHENTICATION_REQUIRED")
        val response = api.current()
        val body = response.body()
        if (response.isSuccessful && body != null && body.role == "LINK_SETUP") AppResult.Failure("Consent required", code = "CONSENT_REQUIRED")
        else if (response.isSuccessful && body != null) AppResult.Success(SessionSubject(body.subjectId, body.role))
        else { if (response.code() == 401) sessions.clear(); failure(response) }
    }
    override suspend fun signOut(): AppResult<Unit> = request {
        val response = api.signOut()
        sessions.clear()
        if (response.isSuccessful || response.code() == 401) AppResult.Success(Unit) else failure(response)
    }
    private suspend fun authenticate(role: String, call: suspend () -> Response<SessionAccessResponse>): AppResult<SessionSubject> = request {
        val response = call()
        val body = response.body()
        if (!response.isSuccessful) failure(response)
        else if (body == null || body.accessToken.isBlank() || (body.accountId ?: body.olderAdultId).isNullOrBlank()) AppResult.Failure("Invalid session", code = "INVALID_RESPONSE")
        else {
            java.time.Instant.parse(body.expiresAt)
            sessions.save(body.accessToken, body.expiresAt)
            AppResult.Success(SessionSubject(if (role == "CAREGIVER") requireNotNull(body.accountId) else requireNotNull(body.olderAdultId), role))
        }
    }
    private fun failure(response: Response<*>): AppResult.Failure {
        val code = runCatching { response.errorBody()?.charStream()?.use { Gson().fromJson(it, Error::class.java)?.code } }.getOrNull()
        return AppResult.Failure("Access failed", code = code ?: if(response.code() == 401) "INVALID_CREDENTIALS" else "REQUEST_FAILED")
    }
    private data class Error(val code: String?)
    private suspend fun <T> request(call: suspend () -> AppResult<T>): AppResult<T> = try { call() }
    catch (exception: CancellationException) { throw exception }
    catch (exception: java.io.IOException) { AppResult.Failure("Network unavailable", cause = exception, code = "NETWORK_UNAVAILABLE") }
    catch (exception: RuntimeException) { AppResult.Failure("Invalid response", cause = exception, code = "INVALID_RESPONSE") }
}
