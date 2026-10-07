package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.application.AlertsRepository
import com.vitahealth.tata.monitoring.domain.model.CaregiverAlert
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException

/**
 * There is no alert list endpoint: the list comes from `openAlerts` of the recent status
 * (`GET /older-adults/{id}/status`) and the detail from `GET /older-adults/{id}/alerts/{alertId}`.
 */
class RemoteAlertsRepository(
    private val monitoringApi: FamilyMonitoringApiService,
    private val alertsApi: AlertsApiService,
) : AlertsRepository {

    override suspend fun openAlerts(caregiverId: String, olderAdultId: String): AppResult<List<CaregiverAlert>> = guarded {
        val response = monitoringApi.status(olderAdultId, caregiverId)
        val body = response.body()
        failureOf(response) ?: if (body == null) {
            failure(AlertFailureCodes.INVALID_RESPONSE)
        } else {
            AppResult.Success(body.openAlerts.orEmpty().map { it.toDomain() })
        }
    }

    override suspend fun alert(caregiverId: String, olderAdultId: String, alertId: Long): AppResult<CaregiverAlert> = guarded {
        val response = alertsApi.detail(olderAdultId, alertId, caregiverId)
        val body = response.body()
        failureOf(response) ?: if (body == null) {
            failure(AlertFailureCodes.INVALID_RESPONSE)
        } else {
            val alert = body.toDomain()
            if (alert.id != alertId) failure(AlertFailureCodes.INVALID_RESPONSE) else AppResult.Success(alert)
        }
    }

    private fun failureOf(response: Response<*>): AppResult.Failure? = when {
        response.isSuccessful -> null
        response.code() == 403 -> failure(AlertFailureCodes.ACCESS_DENIED)
        response.code() == 404 -> failure(AlertFailureCodes.NOT_FOUND)
        else -> failure(AlertFailureCodes.REQUEST_FAILED)
    }

    private fun failure(code: String, cause: Throwable? = null) = AppResult.Failure(
        message = when (code) {
            AlertFailureCodes.ACCESS_DENIED -> "El vínculo de cuidado no está activo o falta el consentimiento."
            AlertFailureCodes.NOT_FOUND -> "No encontramos la alerta o el seguimiento."
            AlertFailureCodes.NETWORK -> "No hay conexión. Inténtalo nuevamente."
            AlertFailureCodes.INVALID_RESPONSE -> "La información recibida no es válida."
            else -> "No pudimos consultar las alertas. Inténtalo nuevamente."
        },
        cause = cause,
        code = code,
    )

    private suspend fun <T> guarded(block: suspend () -> AppResult<T>): AppResult<T> = try {
        block()
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: IOException) {
        failure(AlertFailureCodes.NETWORK, exception)
    } catch (exception: IllegalArgumentException) {
        failure(AlertFailureCodes.INVALID_RESPONSE, exception)
    } catch (exception: RuntimeException) {
        failure(AlertFailureCodes.REQUEST_FAILED, exception)
    }
}
