package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.application.FamilyMonitoringRepository
import com.vitahealth.tata.monitoring.domain.model.*
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.CancellationException
import java.time.*

class RemoteFamilyMonitoringRepository(private val api: FamilyMonitoringApiService) : FamilyMonitoringRepository {
    override suspend fun summary(caregiverId: String, olderAdultId: String, name: String, day: LocalDate, zone: ZoneId): AppResult<FamilySummary> = guarded {
        val response = api.status(olderAdultId, caregiverId)
        if (response.code() == 403) return@guarded AppResult.Failure("El vínculo de cuidado ya no está activo.", code = "CARE_RELATIONSHIP_REQUIRED")
        val status = response.body()
        if (!response.isSuccessful || status == null) return@guarded failed()
        val agendaResponse = api.agenda(olderAdultId, day.atStartOfDay(zone).toInstant().toString(), day.plusDays(1).atStartOfDay(zone).toInstant().toString())
        val agenda = agendaResponse.body()
        if (!agendaResponse.isSuccessful || agenda == null) return@guarded failed()
        val nextResponse = api.next(olderAdultId)
        if (!nextResponse.isSuccessful) return@guarded failed()
        val doses = agenda.map { it.toDose() }.filter { it.scheduledAt.atZone(zone).toLocalDate() == day }
        val next = nextResponse.body()?.toDose()
        val stock = mutableListOf<StockAttention>()
        var inventoryAvailable = true
        (doses + listOfNotNull(next)).distinctBy { it.medicationId }.forEach { dose ->
            try {
                val inventory = api.inventory(dose.medicationId)
                if (inventory.code() != 404 && (!inventory.isSuccessful || inventory.body() == null)) inventoryAvailable = false
                inventory.body()?.takeIf { inventory.isSuccessful }?.let {
                    require(it.remainingStock >= 0 && it.medicationId == dose.medicationId)
                    if (it.lowStock) stock += StockAttention(it.medicationId, dose.medicationName, it.remainingStock)
                }
            } catch (exception: CancellationException) { throw exception }
            catch (exception: Exception) { inventoryAvailable = false }
        }
        AppResult.Success(FamilySummary(name, day,
            AdherenceCounts(status.weeklyAdherence.confirmedIntakes, status.weeklyAdherence.totalIntakes),
            AdherenceCounts(doses.count { it.status == "CONFIRMED" || it.status == "LATE" }, doses.size), next, stock,
            status.openAlerts.orEmpty().map { it.toDomain() }.map { OpenAlert(it.id, it.medicationName, it.reason, it.scheduledAt) }, inventoryAvailable))
    }
    override suspend fun contact(caregiverId: String, olderAdultId: String): AppResult<String> = guarded {
        val response = api.contact(olderAdultId, caregiverId)
        val body = response.body()
        if (response.code() == 404) AppResult.Failure("No hay un teléfono de contacto registrado.", code = "CONTACT_UNAVAILABLE")
        else if (!response.isSuccessful || body == null || body.type != "PHONE" || body.value.isBlank()) failed()
        else AppResult.Success(body.value)
    }
    override suspend fun history(caregiverId: String, olderAdultId: String): AppResult<List<String>> = guarded {
        val response = api.history(olderAdultId, caregiverId)
        val body = response.body()
        if (!response.isSuccessful || body == null) failed()
        else AppResult.Success(body.map { "${it.medicationName} · ${statusLabel(it.status)}\n${displayTime(it.scheduledAt)}" })
    }
    override suspend fun notes(caregiverId: String, olderAdultId: String): AppResult<List<String>> = guarded {
        val response = api.notes(olderAdultId, caregiverId)
        val body = response.body()
        if (!response.isSuccessful || body == null) failed()
        else AppResult.Success(body.map { "${it.text}\n${displayTime(it.recordedAt)}" })
    }
    private fun DoseResponse.toDose(): MonitoredDose {
        require(status in setOf("PENDING", "CONFIRMED", "LATE", "OMITTED"))
        return MonitoredDose(id, medicationId, medicationName, dose, Instant.parse(scheduledAt), status)
    }
    private fun failed() = AppResult.Failure("No pudimos consultar el seguimiento. Inténtalo nuevamente.", code = "REQUEST_FAILED")
    private fun statusLabel(status: String) = when (status) {
        "CONFIRMED" -> "Confirmada"; "LATE" -> "Confirmada con retraso"; "OMITTED" -> "Omitida"
        else -> throw IllegalArgumentException("unknown intake outcome")
    }
    private fun displayTime(time: String) = Instant.parse(time).atZone(ZoneId.systemDefault())
        .format(java.time.format.DateTimeFormatter.ofPattern("d MMM, h:mm a", java.util.Locale("es", "PE")))
    private suspend fun <T> guarded(block: suspend () -> AppResult<T>): AppResult<T> = try { block() }
    catch (exception: CancellationException) { throw exception }
    catch (exception: IllegalArgumentException) { AppResult.Failure("La información recibida no es válida.", exception, "INVALID_RESPONSE") }
    catch (exception: Exception) { AppResult.Failure("No hay conexión. Inténtalo nuevamente.", exception, "NETWORK_UNAVAILABLE") }
}
