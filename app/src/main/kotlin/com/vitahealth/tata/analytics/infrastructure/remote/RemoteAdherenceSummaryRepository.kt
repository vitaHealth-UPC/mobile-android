package com.vitahealth.tata.analytics.infrastructure.remote

import com.vitahealth.tata.analytics.application.AdherenceSummaryRepository
import com.vitahealth.tata.analytics.application.readmodels.AdherenceSummaryReadModel
import com.vitahealth.tata.shared.common.result.AppResult

class RemoteAdherenceSummaryRepository(
    private val api: AnalyticsApiService,
) : AdherenceSummaryRepository {
    override suspend fun getSummary(
        olderAdultId: String,
        periodDays: Int,
    ): AppResult<AdherenceSummaryReadModel?> {
        return try {
            val response = api.getSummary(olderAdultId, periodDays)
            val body = response.body()
            when {
                response.code() == 204 -> AppResult.Success(null)
                response.isSuccessful && body != null -> AppResult.Success(body.toReadModel())
                else -> AppResult.Failure(
                    message = "No pudimos cargar el historial.",
                    code = if (response.code() == 400) "INVALID_REQUEST" else "REQUEST_FAILED",
                )
            }
        } catch (exception: java.util.concurrent.CancellationException) {
            throw exception
        } catch (exception: java.io.IOException) {
            AppResult.Failure(
                message = "No hay conexión. Inténtalo nuevamente.",
                cause = exception,
                code = "NETWORK_UNAVAILABLE",
            )
        } catch (exception: RuntimeException) {
            AppResult.Failure(
                message = "La respuesta del servidor no es válida.",
                cause = exception,
                code = "INVALID_RESPONSE",
            )
        }
    }
}
