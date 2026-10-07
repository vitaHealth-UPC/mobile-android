package com.vitahealth.tata.analytics.infrastructure.remote

import com.vitahealth.tata.analytics.application.AdherenceRecommendationsRepository
import com.vitahealth.tata.analytics.application.readmodels.AdherenceRecommendationsReadModel
import com.vitahealth.tata.shared.common.result.AppResult

class RemoteAdherenceRecommendationsRepository(
    private val api: AnalyticsApiService,
) : AdherenceRecommendationsRepository {
    override suspend fun getRecommendations(
        olderAdultId: String,
        periodDays: Int,
    ): AppResult<AdherenceRecommendationsReadModel?> {
        return try {
            val response = api.getInsights(olderAdultId, periodDays)
            val body = response.body()
            when {
                response.code() == 204 -> AppResult.Success(null)
                response.isSuccessful && body != null -> AppResult.Success(body.toReadModel())
                else -> AppResult.Failure(
                    message = "No pudimos cargar las recomendaciones.",
                    code = if (response.code() == 400) "INVALID_REQUEST" else "REQUEST_FAILED",
                )
            }
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
