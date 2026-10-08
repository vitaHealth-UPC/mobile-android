package com.vitahealth.tata.preferences.infrastructure.remote

import com.vitahealth.tata.preferences.domain.model.UserPreferences
import com.vitahealth.tata.shared.common.result.AppResult
import retrofit2.Response

/** Runs a preferences call and turns the HTTP outcome into an [AppResult]. */
internal suspend fun preferencesRequest(
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
