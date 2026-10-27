package com.vitahealth.tata.monitoring

import com.vitahealth.tata.monitoring.domain.model.valueobjects.AdherenceCounts
import com.vitahealth.tata.monitoring.infrastructure.remote.*
import com.vitahealth.tata.shared.common.result.AppResult
import java.lang.reflect.Proxy
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class FamilySummaryRepositoryTest {
    @Test
    fun missingWeeklyCountsAreNotInterpretedAsNoEvidence() = runBlocking {
        for (counts in listOf(AdherenceResponse(null, 1), AdherenceResponse(0, null))) {
            val repository =
                RemoteFamilyMonitoringRepository(
                    api { method ->
                        when (method) {
                            "status" -> Response.success(StatusResponse(counts, emptyList()))
                            "agenda" -> Response.success(emptyList<DoseResponse>())
                            "next" -> Response.success<DoseResponse>(204, null)
                            else -> error("Unexpected call")
                        }
                    }
                )
            assertEquals(
                "INVALID_RESPONSE",
                (repository.summary(
                        "caregiver",
                        "adult",
                        "Ana",
                        LocalDate.of(2026, 10, 6),
                        ZoneId.of("America/Bogota"),
                    ) as AppResult.Failure)
                    .code,
            )
        }
    }

    @Test
    fun duplicateOrOutOfDayAgendaDoesNotProduceMisleadingAdherence() = runBlocking {
        val dose =
            DoseResponse(
                "intake",
                "med",
                "Medicina",
                "1 tableta",
                "2026-10-06T13:00:00Z",
                "CONFIRMED",
            )
        for (agenda in
            listOf(
                listOf(dose, dose),
                listOf(dose.copy(scheduledAt = "2026-10-07T05:00:00Z")),
                listOf(dose.copy(scheduledAt = "2026-10-06T04:59:59Z")),
            )) {
            val calls = mutableListOf<String>()
            val repository =
                RemoteFamilyMonitoringRepository(
                    api { method ->
                        calls += method
                        when (method) {
                            "status" ->
                                Response.success(
                                    StatusResponse(AdherenceResponse(1, 1), emptyList())
                                )
                            "agenda" -> Response.success(agenda)
                            else -> error("Invalid agenda must be rejected before further queries")
                        }
                    }
                )
            val result =
                repository.summary(
                    "caregiver",
                    "adult",
                    "Ana",
                    LocalDate.of(2026, 10, 6),
                    ZoneId.of("America/Bogota"),
                )
            assertEquals("INVALID_RESPONSE", (result as AppResult.Failure).code)
            assertEquals(listOf("status", "agenda"), calls)
        }
    }

    @Test
    fun historyRetainsTypedInstantAndRejectsUnknownOutcome() = runBlocking {
        val instant = "2026-10-06T13:00:00Z"
        val repository =
            RemoteFamilyMonitoringRepository(
                api { Response.success(listOf(HistoryResponse("Medicina", instant, "LATE"))) }
            )
        val entries = (repository.history("caregiver", "adult") as AppResult.Success).value
        assertEquals(java.time.Instant.parse(instant), entries.single().scheduledAt)
        assertEquals("LATE", entries.single().outcome.name)
        for (entry in
            listOf(
                HistoryResponse("Medicina", instant, "UNKNOWN"),
                HistoryResponse(" ", instant, "CONFIRMED"),
                HistoryResponse("Medicina", "invalid", "CONFIRMED"),
            )) {
            val invalid = RemoteFamilyMonitoringRepository(api { Response.success(listOf(entry)) })
            assertEquals(
                "INVALID_RESPONSE",
                (invalid.history("caregiver", "adult") as AppResult.Failure).code,
            )
        }
    }

    @Test
    fun unauthorizedRelationshipDoesNotRequestProtectedDoseData() = runBlocking {
        val requests = mutableListOf<String>()
        val repository =
            RemoteFamilyMonitoringRepository(
                api { method ->
                    requests += method
                    check(method == "status") { "Protected data was requested after access denial" }
                    Response.error<StatusResponse>(403, "{}".toResponseBody())
                }
            )
        val result =
            repository.summary(
                "caregiver",
                "adult",
                "Ana",
                LocalDate.of(2026, 10, 7),
                ZoneId.of("America/Bogota"),
            )
        assertEquals("CARE_RELATIONSHIP_REQUIRED", (result as AppResult.Failure).code)
        assertEquals(listOf("status"), requests)
    }

    private fun api(answer: (String) -> Any): FamilyMonitoringApiService =
        Proxy.newProxyInstance(
            FamilyMonitoringApiService::class.java.classLoader,
            arrayOf(FamilyMonitoringApiService::class.java),
        ) { _, method, _ ->
            answer(method.name)
        } as FamilyMonitoringApiService

    @Test
    fun emptyEvidenceHasNoPercentage() {
        assertNull(AdherenceCounts(0, 0).percentage)
        assertEquals(75.0, AdherenceCounts(3, 4).percentage!!, 0.001)
    }

    @Test
    fun emptyAgendaAndNoNextDoseRemainValid() = runBlocking {
        val repository =
            RemoteFamilyMonitoringRepository(
                api { method ->
                    when (method) {
                        "status" ->
                            Response.success(StatusResponse(AdherenceResponse(0, 0), emptyList()))
                        "agenda" -> Response.success(emptyList<DoseResponse>())
                        "next" -> Response.success<DoseResponse>(204, null)
                        else -> error("Unexpected request: $method")
                    }
                }
            )
        val result =
            repository.summary(
                "caregiver",
                "adult",
                "Ana",
                LocalDate.of(2026, 10, 6),
                ZoneId.of("America/Bogota"),
            )
        val summary = (result as AppResult.Success).value
        assertNull(summary.nextDose)
        assertNull(summary.weekly.percentage)
        assertNull(summary.today.percentage)
        assertTrue(summary.stockAttention.isEmpty())
    }

    @Test
    fun inventoryFailurePreservesAdherenceAndNextDose() = runBlocking {
        val dose = DoseResponse("i", "m", "Medicina", "1 tableta", "2026-10-06T13:00:00Z", "LATE")
        val repository =
            RemoteFamilyMonitoringRepository(
                api { method ->
                    when (method) {
                        "status" ->
                            Response.success(StatusResponse(AdherenceResponse(3, 4), emptyList()))
                        "agenda" -> Response.success(listOf(dose))
                        "next" -> Response.success(dose.copy(id = "next", status = "PENDING"))
                        "inventory" -> Response.error<InventoryResponse>(503, "{}".toResponseBody())
                        else -> error("Unexpected request: $method")
                    }
                }
            )
        val summary =
            (repository.summary(
                    "c",
                    "a",
                    "Ana",
                    LocalDate.of(2026, 10, 6),
                    ZoneId.of("America/Bogota"),
                ) as AppResult.Success)
                .value
        assertEquals(100.0, summary.today.percentage!!, 0.001)
        assertEquals(75.0, summary.weekly.percentage!!, 0.001)
        assertEquals("next", summary.nextDose!!.id)
        assertFalse(summary.inventoryAvailable)
    }

    @Test
    fun malformedInventoryDoesNotMasqueradeAsUnavailableStock() = runBlocking {
        for (inventory in
            listOf(
                InventoryResponse("other-medication", 5, true),
                InventoryResponse("m", -1, true),
            )) {
            val dose = DoseResponse("i", "m", "Medicina", "1", "2026-10-06T13:00:00Z", "PENDING")
            val repository =
                RemoteFamilyMonitoringRepository(
                    api { method ->
                        when (method) {
                            "status" ->
                                Response.success(
                                    StatusResponse(AdherenceResponse(0, 1), emptyList())
                                )
                            "agenda" -> Response.success(listOf(dose))
                            "next" -> Response.success<DoseResponse>(204, null)
                            "inventory" -> Response.success(inventory)
                            else -> error("Unexpected $method")
                        }
                    }
                )
            val result =
                repository.summary(
                    "caregiver",
                    "adult",
                    "Ana",
                    LocalDate.of(2026, 10, 6),
                    ZoneId.of("America/Bogota"),
                )
            assertEquals("INVALID_RESPONSE", (result as AppResult.Failure).code)
        }
    }

    @Test
    fun consentRevokedDuringInventoryLookupDoesNotReturnProtectedSummary() = runBlocking {
        val dose = DoseResponse("i", "m", "Medicina", "1", "2026-10-06T13:00:00Z", "PENDING")
        for ((status, code) in
            listOf(401 to "SESSION_REQUIRED", 403 to "CARE_RELATIONSHIP_REQUIRED")) {
            val repository =
                RemoteFamilyMonitoringRepository(
                    api { method ->
                        when (method) {
                            "status" ->
                                Response.success(
                                    StatusResponse(AdherenceResponse(0, 1), emptyList())
                                )
                            "agenda" -> Response.success(listOf(dose))
                            "next" -> Response.success<DoseResponse>(204, null)
                            "inventory" ->
                                Response.error<InventoryResponse>(status, "{}".toResponseBody())
                            else -> error("Unexpected $method")
                        }
                    }
                )
            val result =
                repository.summary(
                    "caregiver",
                    "adult",
                    "Ana",
                    LocalDate.of(2026, 10, 6),
                    ZoneId.of("America/Bogota"),
                )
            assertEquals(code, (result as AppResult.Failure).code)
        }
    }
}
