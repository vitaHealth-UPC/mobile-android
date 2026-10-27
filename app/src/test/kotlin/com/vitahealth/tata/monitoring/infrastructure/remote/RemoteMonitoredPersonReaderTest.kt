package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.shared.common.result.AppResult
import java.lang.reflect.Proxy
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class RemoteMonitoredPersonReaderTest {
    private fun reader(profile: OlderAdultProfileResponse) =
        RemoteMonitoredPersonReader(
            Proxy.newProxyInstance(
                FamilyMonitoringApiService::class.java.classLoader,
                arrayOf(FamilyMonitoringApiService::class.java),
            ) { _, method, _ ->
                check(method.name == "profile")
                Response.success(profile)
            } as FamilyMonitoringApiService
        )

    @Test
    fun preservesActualBirthDateAndDoesNotInventOneWhenMissing() = runBlocking {
        val result =
            reader(OlderAdultProfileResponse("adult", "Rosa Vargas", "1958-07-20"))
                .read("caregiver", "adult") as AppResult.Success
        assertEquals(LocalDate.of(1958, 7, 20), result.value.birthDate)
        assertEquals("Rosa Vargas", result.value.fullName)
        val noDate =
            reader(OlderAdultProfileResponse("adult", "Rosa Vargas")).read("caregiver", "adult")
                as AppResult.Success
        assertNull(noDate.value.birthDate)
    }

    @Test
    fun rejectsWrongIdentityAndInvalidBirthDates() = runBlocking {
        listOf(
                OlderAdultProfileResponse("other", "Name"),
                OlderAdultProfileResponse("adult", "Name", "invalid"),
                OlderAdultProfileResponse("adult", "Name", "2999-01-01"),
            )
            .forEach {
                val result = reader(it).read("caregiver", "adult") as AppResult.Failure
                assertEquals(AlertFailureCodes.INVALID_RESPONSE, result.code)
            }
    }
}
