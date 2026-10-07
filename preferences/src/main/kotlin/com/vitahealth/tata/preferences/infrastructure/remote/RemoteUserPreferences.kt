package com.vitahealth.tata.preferences.infrastructure.remote

import com.vitahealth.tata.preferences.application.UserPreferencesRemote
import com.vitahealth.tata.preferences.domain.model.TextSizeLevel
import com.vitahealth.tata.preferences.domain.model.UserPreferences
import com.vitahealth.tata.shared.common.result.AppResult
import retrofit2.Response

class RemoteUserPreferences(
    private val api: PreferencesApiService,
) : UserPreferencesRemote {
    override suspend fun get(userId: String): AppResult<UserPreferences> =
        request(userId) { api.getPreferences(userId) }

    override suspend fun updateTextSize(userId: String, textSize: TextSizeLevel): AppResult<UserPreferences> =
        request(userId) { api.updateTextSize(userId, UpdateTextSizeRequest(textSize.name)) }

    private suspend fun request(
        userId: String,
        call: suspend () -> Response<UserPreferencesResponse>,
    ): AppResult<UserPreferences> =
        try {
            val response = call()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                body.toDomain(userId)?.let { AppResult.Success(it) }
                    ?: AppResult.Failure(
                        message = "The service returned preferences this app does not recognise.",
                        code = "INVALID_PREFERENCES_RESPONSE",
                    )
            } else {
                AppResult.Failure(message = "The preferences request failed.", code = failureCode(response.code()))
            }
        } catch (exception: Exception) {
            AppResult.Failure(
                message = "No connection available.",
                cause = exception,
                code = "NETWORK_UNAVAILABLE",
            )
        }

    private fun failureCode(status: Int): String =
        when (status) {
            400 -> "REQUEST_VALIDATION_FAILED"
            else -> "REQUEST_FAILED"
        }
}
