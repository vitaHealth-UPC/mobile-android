package com.vitahealth.tata.monitoring

import com.vitahealth.tata.monitoring.infrastructure.remote.*
import com.vitahealth.tata.monitoring.domain.model.AdherenceCounts
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response
import java.lang.reflect.Proxy
import java.time.LocalDate
import java.time.ZoneId
import okhttp3.ResponseBody.Companion.toResponseBody

class FamilySummaryRepositoryTest {
    @Test fun unauthorizedRelationshipDoesNotRequestProtectedDoseData() = runBlocking {
        val requests = mutableListOf<String>()
        val repository = RemoteFamilyMonitoringRepository(api { method ->
            requests += method
            check(method == "status") { "Protected data was requested after access denial" }
            Response.error<StatusResponse>(403, "{}".toResponseBody())
        })
        val result = repository.summary("caregiver", "adult", "Ana", LocalDate.of(2026, 10, 7), ZoneId.of("America/Bogota"))
        assertEquals("CARE_RELATIONSHIP_REQUIRED", (result as AppResult.Failure).code)
        assertEquals(listOf("status"), requests)
    }
    private fun api(answer: (String) -> Any): FamilyMonitoringApiService = Proxy.newProxyInstance(
        FamilyMonitoringApiService::class.java.classLoader, arrayOf(FamilyMonitoringApiService::class.java)
    ) { _, method, _ -> answer(method.name) } as FamilyMonitoringApiService

    @Test fun emptyEvidenceHasNoPercentage() {
        assertNull(AdherenceCounts(0, 0).percentage)
        assertEquals(75.0, AdherenceCounts(3, 4).percentage!!, 0.001)
    }

    @Test fun emptyAgendaAndNoNextDoseRemainValid() = runBlocking {
        val repository = RemoteFamilyMonitoringRepository(api { method -> when (method) {
            "status" -> Response.success(StatusResponse(AdherenceResponse(0, 0), emptyList()))
            "agenda" -> Response.success(emptyList<DoseResponse>())
            "next" -> Response.success<DoseResponse>(204, null)
            else -> error("Unexpected request: $method")
        } })
        val result = repository.summary("caregiver", "adult", "Ana", LocalDate.of(2026, 10, 6), ZoneId.of("America/Bogota"))
        val summary = (result as AppResult.Success).value
        assertNull(summary.nextDose)
        assertNull(summary.weekly.percentage)
        assertNull(summary.today.percentage)
        assertTrue(summary.stockAttention.isEmpty())
    }

    @Test fun inventoryFailurePreservesAdherenceAndNextDose() = runBlocking {
        val dose = DoseResponse("i", "m", "Medicina", "1 tableta", "2026-10-06T13:00:00Z", "LATE")
        val repository = RemoteFamilyMonitoringRepository(api { method -> when (method) {
            "status" -> Response.success(StatusResponse(AdherenceResponse(3, 4), emptyList()))
            "agenda" -> Response.success(listOf(dose))
            "next" -> Response.success(dose.copy(id = "next", status = "PENDING"))
            "inventory" -> throw java.io.IOException("offline")
            else -> error("Unexpected request: $method")
        } })
        val summary = (repository.summary("c", "a", "Ana", LocalDate.of(2026, 10, 6), ZoneId.of("America/Bogota")) as AppResult.Success).value
        assertEquals(100.0, summary.today.percentage!!, 0.001)
        assertEquals(75.0, summary.weekly.percentage!!, 0.001)
        assertEquals("next", summary.nextDose!!.id)
        assertFalse(summary.inventoryAvailable)
    }
}
