package com.vitahealth.tata.monitoring.infrastructure.remote

import com.google.gson.stream.MalformedJsonException
import com.vitahealth.tata.monitoring.application.ports.FamilyMonitoringRepository
import com.vitahealth.tata.monitoring.application.readmodels.FamilySummary
import com.vitahealth.tata.monitoring.application.readmodels.IntakeHistoryEntry
import com.vitahealth.tata.monitoring.application.readmodels.OpenAlert
import com.vitahealth.tata.monitoring.application.readmodels.StockAttention
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AdherenceCounts
import com.vitahealth.tata.shared.common.result.AppResult
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.CancellationException

/** Aggregates protected monitoring evidence while distinguishing optional connectivity failures. */
class RemoteFamilyMonitoringRepository(private val api: FamilyMonitoringApiService) :
    FamilyMonitoringRepository {
    override suspend fun summary(
        caregiverId: String,
        olderAdultId: String,
        name: String,
        day: LocalDate,
        zone: ZoneId,
    ): AppResult<FamilySummary> = guarded {
        val response = api.status(olderAdultId, caregiverId)
        if (response.code() == 403)
            return@guarded AppResult.Failure(
                "El vínculo de cuidado ya no está activo.",
                code = "CARE_RELATIONSHIP_REQUIRED",
            )
        accessFailure(response.code())?.let {
            return@guarded it
        }
        val status = response.body()
        if (!response.isSuccessful) return@guarded failed()
        requireNotNull(status)
        val weekly =
            AdherenceCounts(
                requireNotNull(status.weeklyAdherence.confirmedIntakes),
                requireNotNull(status.weeklyAdherence.totalIntakes),
            )
        val agendaResponse =
            api.agenda(
                olderAdultId,
                day.atStartOfDay(zone).toInstant().toString(),
                day.plusDays(1).atStartOfDay(zone).toInstant().toString(),
            )
        accessFailure(agendaResponse.code())?.let {
            return@guarded it
        }
        val agenda = agendaResponse.body()
        if (!agendaResponse.isSuccessful) return@guarded failed()
        requireNotNull(agenda)
        val doses = agenda.map { it.toMonitoredDose() }
        require(doses.map { it.id }.distinct().size == doses.size) { "duplicate agenda intake" }
        require(doses.all { it.scheduledAt.atZone(zone).toLocalDate() == day }) {
            "agenda outside requested day"
        }
        val nextResponse = api.next(olderAdultId)
        accessFailure(nextResponse.code())?.let {
            return@guarded it
        }
        if (!nextResponse.isSuccessful) return@guarded failed()
        require(nextResponse.code() == 204 || nextResponse.body() != null)
        val next = nextResponse.body()?.toMonitoredDose()
        val stock = mutableListOf<StockAttention>()
        var inventoryAvailable = true
        (doses + listOfNotNull(next))
            .distinctBy { it.medicationId }
            .forEach { dose ->
                try {
                    val inventory = api.inventory(dose.medicationId)
                    accessFailure(inventory.code())?.let {
                        return@guarded it
                    }
                    if (inventory.isSuccessful) requireNotNull(inventory.body())
                    else if (inventory.code() != 404) inventoryAvailable = false
                    inventory
                        .body()
                        ?.takeIf { inventory.isSuccessful }
                        ?.let {
                            val remaining = requireNotNull(it.remainingStock)
                            val lowStock = requireNotNull(it.lowStock)
                            require(remaining >= 0 && it.medicationId == dose.medicationId)
                            if (lowStock)
                                stock +=
                                    StockAttention(it.medicationId, dose.medicationName, remaining)
                        }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: MalformedJsonException) {
                    throw exception
                } catch (exception: IOException) {
                    inventoryAvailable = false
                }
            }
        AppResult.Success(
            FamilySummary(
                name,
                day,
                weekly,
                AdherenceCounts(
                    doses.count { it.status == "CONFIRMED" || it.status == "LATE" },
                    doses.size,
                ),
                next,
                stock,
                status.openAlerts
                    .orEmpty()
                    .map { it.toDomain() }
                    .map { OpenAlert(it.id, it.medicationName, it.reason, it.scheduledAt) },
                inventoryAvailable,
            )
        )
    }

    override suspend fun history(
        caregiverId: String,
        olderAdultId: String,
    ): AppResult<List<IntakeHistoryEntry>> = guarded {
        val response = api.history(olderAdultId, caregiverId)
        accessFailure(response.code())?.let {
            return@guarded it
        }
        val body = response.body()
        if (!response.isSuccessful) return@guarded failed()
        requireNotNull(body)
        AppResult.Success(body.map { it.toHistoryEntry() })
    }

    private fun accessFailure(status: Int): AppResult.Failure? =
        when (status) {
            401 ->
                AppResult.Failure(
                    "Inicia sesión para consultar el seguimiento.",
                    code = "SESSION_REQUIRED",
                )
            403 ->
                AppResult.Failure(
                    "El vínculo de cuidado ya no está activo.",
                    code = "CARE_RELATIONSHIP_REQUIRED",
                )
            else -> null
        }

    private fun failed() =
        AppResult.Failure(
            "No pudimos consultar el seguimiento. Inténtalo nuevamente.",
            code = "REQUEST_FAILED",
        )

    private suspend fun <T> guarded(block: suspend () -> AppResult<T>): AppResult<T> =
        try {
            block()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: IllegalArgumentException) {
            AppResult.Failure(
                "La información recibida no es válida.",
                exception,
                "INVALID_RESPONSE",
            )
        } catch (exception: MalformedJsonException) {
            AppResult.Failure(
                "La información recibida no es válida.",
                exception,
                "INVALID_RESPONSE",
            )
        } catch (exception: IOException) {
            AppResult.Failure(
                "No hay conexión. Inténtalo nuevamente.",
                exception,
                "NETWORK_UNAVAILABLE",
            )
        } catch (exception: RuntimeException) {
            AppResult.Failure(
                "La información recibida no es válida.",
                exception,
                "INVALID_RESPONSE",
            )
        }
}
