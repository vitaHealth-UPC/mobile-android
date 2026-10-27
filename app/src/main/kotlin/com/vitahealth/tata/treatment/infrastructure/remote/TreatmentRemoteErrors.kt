package com.vitahealth.tata.treatment.infrastructure.remote

import com.google.gson.stream.MalformedJsonException
import com.vitahealth.tata.shared.common.result.AppResult
import java.io.IOException
import kotlinx.coroutines.CancellationException

/** Human-readable diagnostic for treatment endpoint HTTP failures. */
internal fun treatmentMessage(status: Int): String =
    when (status) {
        400 -> "Revisa los datos obligatorios."
        401 -> "Inicia sesión para continuar."
        403 -> "Necesitas un vínculo de cuidado activo para continuar."
        404 -> "No encontramos el recurso solicitado."
        409 -> "El tratamiento no puede cambiar a ese estado."
        else -> "No pudimos completar la solicitud."
    }

/** Stable application failure code for treatment endpoint HTTP failures. */
internal fun treatmentCode(status: Int): String =
    when (status) {
        400 -> "REQUEST_VALIDATION_FAILED"
        401 -> "SESSION_REQUIRED"
        403 -> "CARE_LINK_NOT_AUTHORIZED"
        404 -> "RESOURCE_NOT_FOUND"
        409 -> "CONFLICT"
        else -> "REQUEST_FAILED"
    }

/** Classifies transport and payload failures while propagating cancellation. */
internal fun requestFailure(exception: Exception): AppResult.Failure =
    when (exception) {
        is CancellationException -> throw exception
        is MalformedJsonException ->
            AppResult.Failure(
                message = "La respuesta del servidor no es válida.",
                cause = exception,
                code = "INVALID_RESPONSE",
            )
        is IOException ->
            AppResult.Failure(
                message = "No hay conexión disponible.",
                cause = exception,
                code = "NETWORK_UNAVAILABLE",
            )
        is RuntimeException ->
            AppResult.Failure(
                message = "La respuesta del servidor no es válida.",
                cause = exception,
                code = "INVALID_RESPONSE",
            )
        else -> throw exception
    }
