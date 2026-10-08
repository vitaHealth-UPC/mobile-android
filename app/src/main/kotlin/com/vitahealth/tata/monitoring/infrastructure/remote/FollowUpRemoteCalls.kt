package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException

/** Failure of the follow-up endpoints (alerts, notes, contact) with a stable code and a fallback message. */
internal fun followUpFailure(code: String, cause: Throwable? = null) = AppResult.Failure(
    message = when (code) {
        AlertFailureCodes.ACCESS_DENIED -> "El vínculo de cuidado no está activo o falta el consentimiento."
        AlertFailureCodes.NOT_FOUND -> "No encontramos la alerta o el seguimiento."
        AlertFailureCodes.NETWORK -> "No hay conexión. Inténtalo nuevamente."
        AlertFailureCodes.INVALID_RESPONSE -> "La información recibida no es válida."
        AlertFailureCodes.STATUS_CONFLICT -> "La alerta ya cambió de estado."
        AlertFailureCodes.STATUS_NOT_ACCEPTED -> "El estado solicitado no es válido."
        AlertFailureCodes.NOTE_REJECTED -> "El servidor no aceptó la nota."
        else -> "No pudimos completar la consulta. Inténtalo nuevamente."
    },
    cause = cause,
    code = code,
)

/** Maps an unsuccessful answer; [badRequestCode] names what a `400` means for that endpoint. */
internal fun followUpHttpFailure(response: Response<*>, badRequestCode: String = AlertFailureCodes.REQUEST_FAILED): AppResult.Failure? =
    when {
        response.isSuccessful -> null
        response.code() == 400 -> followUpFailure(badRequestCode)
        response.code() == 403 -> followUpFailure(AlertFailureCodes.ACCESS_DENIED)
        response.code() == 404 -> followUpFailure(AlertFailureCodes.NOT_FOUND)
        response.code() == 409 -> followUpFailure(AlertFailureCodes.STATUS_CONFLICT)
        else -> followUpFailure(AlertFailureCodes.REQUEST_FAILED)
    }

internal suspend fun <T> guardedFollowUp(block: suspend () -> AppResult<T>): AppResult<T> = try {
    block()
} catch (exception: CancellationException) {
    throw exception
} catch (exception: IOException) {
    followUpFailure(AlertFailureCodes.NETWORK, exception)
} catch (exception: IllegalArgumentException) {
    followUpFailure(AlertFailureCodes.INVALID_RESPONSE, exception)
} catch (exception: RuntimeException) {
    followUpFailure(AlertFailureCodes.REQUEST_FAILED, exception)
}
