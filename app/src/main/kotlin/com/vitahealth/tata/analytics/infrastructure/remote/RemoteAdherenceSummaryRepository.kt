package com.vitahealth.tata.analytics.infrastructure.remote

import com.google.gson.stream.MalformedJsonException
import com.vitahealth.tata.analytics.application.ports.AdherenceSummaryRepository
import com.vitahealth.tata.analytics.application.ports.TreatmentAdherenceRepository
import com.vitahealth.tata.analytics.application.readmodels.AdherenceSummaryReadModel
import com.vitahealth.tata.shared.common.result.AppResult
import java.io.IOException
import java.time.ZoneId
import kotlinx.coroutines.CancellationException

/** Retrieves adherence summary from the analytics API. */
class RemoteAdherenceSummaryRepository(private val api: AnalyticsApiService) :
    AdherenceSummaryRepository, TreatmentAdherenceRepository {
    override suspend fun getSummary(
        olderAdultId: String,
        periodDays: Int,
    ): AppResult<AdherenceSummaryReadModel?> = readSummary(olderAdultId, periodDays, null)

    override suspend fun getTreatmentSummary(
        olderAdultId: String,
        treatmentId: String,
        periodDays: Int,
    ): AppResult<AdherenceSummaryReadModel?> {
        if (treatmentId.isBlank())
            return AppResult.Failure("Selecciona un tratamiento válido.", code = "INVALID_REQUEST")
        return readSummary(olderAdultId, periodDays, treatmentId)
    }

    private suspend fun readSummary(
        olderAdultId: String,
        periodDays: Int,
        treatmentId: String?,
    ): AppResult<AdherenceSummaryReadModel?> {
        return try {
            val zone = ZoneId.systemDefault()
            val response =
                if (treatmentId == null) api.getSummary(olderAdultId, periodDays, zone.id)
                else api.getTreatmentSummary(olderAdultId, treatmentId, periodDays, zone.id)
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
                response.isSuccessful && body != null -> {
                    require(body.periodDays == periodDays)
                    require(body.adherencePercent in 0..100 && body.onTimePercent in 0..100)
                    require(body.trend.orEmpty().all { it.adherencePercent in 0..100 })
                    AppResult.Success(body.toReadModel(zone))
                }
                else ->
                    AppResult.Failure(
                        message = "No pudimos cargar el historial.",
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
