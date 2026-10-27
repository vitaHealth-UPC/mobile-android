package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.shared.common.result.AppResult
import java.lang.reflect.Proxy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class RemoteMonitoredDoseReaderTest {
    private fun reader(dose: DoseResponse) = RemoteMonitoredDoseReader(
        Proxy.newProxyInstance(FamilyMonitoringApiService::class.java.classLoader, arrayOf(FamilyMonitoringApiService::class.java)) { _, method, _ ->
            check(method.name == "dose")
            Response.success(dose)
        } as FamilyMonitoringApiService)
    private fun dose(id: String = "intake", adult: String = "adult") = DoseResponse(id, "med", "Medicine", "2 tablets", "2026-10-24T13:00:00Z", "PENDING", adult)

    @Test fun preservesActualDoseAmount() = runBlocking {
        val result = reader(dose()).read("caregiver", "adult", "intake") as AppResult.Success
        assertEquals("2 tablets", result.value.dose)
    }
    @Test fun rejectsAnotherIntakeOrAnotherAdult() = runBlocking {
        listOf(dose(id = "other"), dose(adult = "other")).forEach {
            val result = reader(it).read("caregiver", "adult", "intake") as AppResult.Failure
            assertEquals(AlertFailureCodes.INVALID_RESPONSE, result.code)
        }
    }
}
