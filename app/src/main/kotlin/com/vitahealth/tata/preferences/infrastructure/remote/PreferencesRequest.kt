package com.vitahealth.tata.preferences.infrastructure.remote

import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import com.vitahealth.tata.preferences.domain.model.entities.UserPreferences
import com.vitahealth.tata.shared.common.result.AppResult
import java.io.IOException
import kotlinx.coroutines.CancellationException
import retrofit2.Response

/**
 * Classifies transport, conversion and authorization without turning cancellation into offline
 * success.
 */
internal suspend fun preferencesRequest(
    userId: String,
    call: suspend () -> Response<UserPreferencesResponse>,
): AppResult<UserPreferences> =
    try {
        val response = call()
        if (!response.isSuccessful) {
            val code =
                when (response.code()) {
                    400 -> "REQUEST_VALIDATION_FAILED"
                    401 -> "SESSION_REQUIRED"
                    403 -> "PREFERENCES_ACCESS_DENIED"
                    else -> "REQUEST_FAILED"
                }
            AppResult.Failure("No pudimos consultar tus preferencias.", code = code)
        } else {
            response.body()?.toDomain(userId)?.let { AppResult.Success(it) } ?: invalidPreferences()
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: MalformedJsonException) {
        invalidPreferences(exception)
    } catch (exception: JsonParseException) {
        invalidPreferences(exception)
    } catch (exception: IOException) {
        AppResult.Failure(
            "No hay conexión. Inténtalo nuevamente.",
            exception,
            "NETWORK_UNAVAILABLE",
        )
    } catch (exception: RuntimeException) {
        invalidPreferences(exception)
    }

private fun invalidPreferences(cause: Throwable? = null) =
    AppResult.Failure(
        "La información de preferencias recibida no es válida.",
        cause,
        "INVALID_PREFERENCES_RESPONSE",
    )
