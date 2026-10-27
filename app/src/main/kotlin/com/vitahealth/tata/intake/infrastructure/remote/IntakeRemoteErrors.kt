package com.vitahealth.tata.intake.infrastructure.remote

import com.google.gson.stream.MalformedJsonException
import com.vitahealth.tata.shared.common.result.AppResult
import java.io.IOException
import kotlinx.coroutines.CancellationException

/** Separates retryable connection failures from unusable server responses. */
internal fun intakeRemoteFailure(exception: Exception): AppResult.Failure {
    if (exception is CancellationException) throw exception
    return if (exception is IOException && exception !is MalformedJsonException) {
        AppResult.Failure(
            "No hay conexión. Inténtalo nuevamente.",
            cause = exception,
            code = "NETWORK_UNAVAILABLE",
        )
    } else {
        AppResult.Failure(
            "La respuesta de la toma no es válida.",
            cause = exception,
            code = "INVALID_RESPONSE",
        )
    }
}

/** Retains access failures as failures so offline adapters cannot replace them with cached data. */
internal fun intakeHttpFailure(
    status: Int,
    missingCode: String = "INTAKE_NOT_FOUND",
): AppResult.Failure =
    AppResult.Failure(
        message = "No pudimos completar la consulta de la toma.",
        code =
            when (status) {
                401 -> "SESSION_REQUIRED"
                403 -> "ACCESS_DENIED"
                404 -> missingCode
                else -> "REQUEST_FAILED"
            },
    )

/** Rejects incomplete or unrelated successful payloads without enabling an offline fallback. */
internal fun invalidIntakeResponse(): AppResult.Failure =
    AppResult.Failure("La respuesta de la toma no es válida.", code = "INVALID_RESPONSE")
