package com.vitahealth.tata.analytics.infrastructure.remote

import com.google.gson.stream.MalformedJsonException
import com.vitahealth.tata.analytics.application.ports.AdherenceRecommendationsRepository
import com.vitahealth.tata.analytics.application.readmodels.AdherenceRecommendationsReadModel
import com.vitahealth.tata.shared.common.result.AppResult
import java.io.IOException
import kotlinx.coroutines.CancellationException

/** Retrieves adherence recommendations from the analytics API. */
class RemoteAdherenceRecommendationsRepository(private val api: AnalyticsApiService) :
    AdherenceRecommendationsRepository {
    override suspend fun getRecommendations(
        olderAdultId: String,
        periodDays: Int,
    ): AppResult<AdherenceRecommendationsReadModel?> {
        return try {
            val response = api.getInsights(olderAdultId, periodDays)
            val body = response.body()
            when {
                response.code() == 401 ->
                    AppResult.Failure(
                        "Inicia sesión para consultar la adherencia.",
                        code = "SESSION_REQUIRED",
                    )
                response.code() == 403 ->
                    AppResult.Failure(
                        "No tienes acceso a la adherencia de este adulto.",
                        code = "ACCESS_DENIED",
                    )
                response.isSuccessful && response.code() != 204 && body == null ->
                    AppResult.Failure(
                        "La respuesta del servidor no es válida.",
                        code = "INVALID_RESPONSE",
                    )
                response.code() == 204 -> AppResult.Success(null)
                response.isSuccessful && body != null -> AppResult.Success(body.toReadModel())
                else ->
                    AppResult.Failure(
                        message = "No pudimos cargar las recomendaciones.",
                        code = if (response.code() == 400) "INVALID_REQUEST" else "REQUEST_FAILED",
                    )
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: MalformedJsonException) {
            AppResult.Failure(
                message = "La respuesta del servidor no es válida.",
                cause = exception,
                code = "INVALID_RESPONSE",
            )
        } catch (exception: IOException) {
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
