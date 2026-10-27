package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.swaggerAlertResponse
import com.vitahealth.tata.shared.common.result.AppResult
import java.lang.reflect.Proxy
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class RemoteAlertTimelineReaderTest {
    private val day = LocalDate.of(2026, 10, 24)
    private val zone = ZoneId.of("America/Lima")
    private val calls = mutableListOf<String>()
    private var agenda: Response<List<DoseResponse>> = Response.success(listOf(dose()))
    private var profile = OlderAdultProfileResponse("adult", "Rosa Vargas")
    private val reader =
        RemoteAlertTimelineReader(
            Proxy.newProxyInstance(
                FamilyMonitoringApiService::class.java.classLoader,
                arrayOf(FamilyMonitoringApiService::class.java),
            ) { _, method, args ->
                calls += method.name
                when (method.name) {
                    "status" ->
                        Response.success(
                            StatusResponse(AdherenceResponse(0, 0), listOf(swaggerAlertResponse()))
                        )
                    "agenda" -> {
                        assertEquals("2026-10-24T05:00:00Z", args[1])
                        assertEquals("2026-10-25T05:00:00Z", args[2])
                        agenda
                    }
                    "profile" -> Response.success(profile)
                    else -> error("Unexpected request: ${method.name}")
                }
            } as FamilyMonitoringApiService
        )

    private fun dose(id: String = "dose", scheduled: String = "2026-10-24T18:00:00Z") =
        DoseResponse(id, "med", "Losartán 50 mg", "1 comprimido", scheduled, "PENDING")

    @Test
    fun combinesDistinctAlertAndDoseEvidenceForTheRequestedLocalDay() = runBlocking {
        val result = reader.read("caregiver", "adult", day, zone) as AppResult.Success
        assertEquals("Rosa Vargas", result.value.olderAdultName)
        assertEquals(1, result.value.alerts.size)
        assertEquals("dose", result.value.doses.single().id)
        assertEquals(listOf("status", "agenda", "profile"), calls)
    }

    @Test
    fun revocationOnAgendaDiscardsTheEntireTimelineAndSkipsProfile() = runBlocking {
        agenda = Response.error(403, "".toResponseBody())
        val result = reader.read("caregiver", "adult", day, zone) as AppResult.Failure
        assertEquals(AlertFailureCodes.ACCESS_DENIED, result.code)
        assertEquals(listOf("status", "agenda"), calls)
    }

    @Test
    fun rejectsDuplicateDosesAndDosesOutsideTheRequestedDay() = runBlocking {
        listOf(listOf(dose(), dose()), listOf(dose(scheduled = "2026-10-24T04:59:59Z"))).forEach {
            agenda = Response.success(it)
            val result = reader.read("caregiver", "adult", day, zone) as AppResult.Failure
            assertEquals(AlertFailureCodes.INVALID_RESPONSE, result.code)
        }
    }

    @Test
    fun rejectsAProfileBelongingToAnotherAdult() = runBlocking {
        profile = OlderAdultProfileResponse("another-adult", "Otra persona")
        val result = reader.read("caregiver", "adult", day, zone) as AppResult.Failure
        assertEquals(AlertFailureCodes.INVALID_RESPONSE, result.code)
    }
}
