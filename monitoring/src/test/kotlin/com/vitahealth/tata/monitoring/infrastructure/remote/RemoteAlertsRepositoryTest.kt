package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.domain.model.AlertStatus
import com.vitahealth.tata.monitoring.swaggerAlertResponse
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.lang.reflect.Proxy

class RemoteAlertsRepositoryTest {
    private val calls = mutableListOf<List<Any?>>()

    private inline fun <reified T> proxy(crossinline answer: (String) -> Any): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, args ->
            calls += listOf(method.name) + args.orEmpty().dropLast(1)
            answer(method.name)
        } as T

    private fun repository(status: () -> Any = { error("unexpected status") }, detail: () -> Any = { error("unexpected detail") }) =
        RemoteAlertsRepository(proxy<FamilyMonitoringApiService> { status() }, proxy<AlertsApiService> { detail() })

    private fun <T> httpError(code: Int): Response<T> = Response.error(code, """{"code":"X","message":"m"}""".toResponseBody())

    private fun failureCode(result: AppResult<*>) = (result as AppResult.Failure).code

    @Test fun buildsTheListFromTheOpenAlertsOfTheStatus() = runBlocking {
        val status = StatusResponse(AdherenceResponse(3, 4), listOf(swaggerAlertResponse(), swaggerAlertResponse(id = 2, status = "ATTENDED")))
        val result = repository(status = { Response.success(status) }).openAlerts("caregiver-1", "adult-1")

        val alerts = (result as AppResult.Success).value
        assertEquals(listOf(1L, 2L), alerts.map { it.id })
        assertEquals(AlertStatus.ATTENDED, alerts[1].status)
        assertEquals(listOf("status", "adult-1", "caregiver-1"), calls.single())
    }

    @Test fun missingOpenAlertsMeansNoAlerts() = runBlocking {
        val status = StatusResponse(AdherenceResponse(0, 0), null)
        val result = repository(status = { Response.success(status) }).openAlerts("c", "a")

        assertTrue((result as AppResult.Success).value.isEmpty())
    }

    @Test fun mapsForbiddenToAccessDenied() = runBlocking {
        assertEquals(AlertFailureCodes.ACCESS_DENIED, failureCode(repository(status = { httpError<StatusResponse>(403) }).openAlerts("c", "a")))
        assertEquals(AlertFailureCodes.ACCESS_DENIED, failureCode(repository(detail = { httpError<AlertSummaryResponse>(403) }).alert("c", "a", 1)))
    }

    @Test fun mapsNotFound() = runBlocking {
        assertEquals(AlertFailureCodes.NOT_FOUND, failureCode(repository(status = { httpError<StatusResponse>(404) }).openAlerts("c", "a")))
        assertEquals(AlertFailureCodes.NOT_FOUND, failureCode(repository(detail = { httpError<AlertSummaryResponse>(404) }).alert("c", "a", 7)))
    }

    @Test fun mapsServerErrorsToRequestFailed() = runBlocking {
        assertEquals(AlertFailureCodes.REQUEST_FAILED, failureCode(repository(status = { httpError<StatusResponse>(500) }).openAlerts("c", "a")))
    }

    @Test fun mapsMissingConnectionToNetwork() = runBlocking {
        // A Java proxy would wrap the checked IOException, so this API is a plain implementation.
        val offline = object : AlertsApiService {
            override suspend fun detail(olderAdultId: String, alertId: Long, caregiverId: String): Response<AlertSummaryResponse> =
                throw IOException("offline")
            override suspend fun updateStatus(
                olderAdultId: String,
                alertId: Long,
                caregiverId: String,
                request: UpdateAlertStatusRequest,
            ): Response<AlertSummaryResponse> = throw IOException("offline")
        }
        val result = RemoteAlertsRepository(proxy<FamilyMonitoringApiService> { error("unexpected status") }, offline).alert("c", "a", 1)

        assertEquals(AlertFailureCodes.NETWORK, failureCode(result))
    }

    @Test fun rejectsAnUnknownStatusAsInvalidResponse() = runBlocking {
        val result = repository(detail = { Response.success(swaggerAlertResponse(status = "RESOLVED")) }).alert("c", "a", 1)

        assertEquals(AlertFailureCodes.INVALID_RESPONSE, failureCode(result))
    }

    @Test fun readsTheDetailWithTheCaregiverQuery() = runBlocking {
        val result = repository(detail = { Response.success(swaggerAlertResponse()) }).alert("caregiver-1", "adult-1", 1)

        assertEquals(1L, (result as AppResult.Success).value.id)
        assertEquals(listOf("detail", "adult-1", 1L, "caregiver-1"), calls.single())
    }

    @Test fun rejectsADetailOfAnotherAlert() = runBlocking {
        val result = repository(detail = { Response.success(swaggerAlertResponse(id = 2)) }).alert("c", "a", 1)

        assertEquals(AlertFailureCodes.INVALID_RESPONSE, failureCode(result))
    }

    @Test fun sendsTheRequestedStatusWithTheCaregiverQuery() = runBlocking {
        val attended = swaggerAlertResponse(status = "ATTENDED")
        val result = repository(detail = { Response.success(attended) }).updateStatus("caregiver-1", "adult-1", 1, AlertStatus.ATTENDED)

        assertEquals(AlertStatus.ATTENDED, (result as AppResult.Success).value.status)
        assertEquals(listOf("updateStatus", "adult-1", 1L, "caregiver-1", UpdateAlertStatusRequest("ATTENDED")), calls.single())
    }

    @Test fun mapsTheStatusUpdateFailures() = runBlocking {
        val expected = mapOf(
            400 to AlertFailureCodes.STATUS_NOT_ACCEPTED,
            403 to AlertFailureCodes.ACCESS_DENIED,
            404 to AlertFailureCodes.NOT_FOUND,
            409 to AlertFailureCodes.STATUS_CONFLICT,
            500 to AlertFailureCodes.REQUEST_FAILED,
        )
        expected.forEach { (status, code) ->
            val result = repository(detail = { httpError<AlertSummaryResponse>(status) }).updateStatus("c", "a", 1, AlertStatus.CLOSED)
            assertEquals("HTTP $status", code, failureCode(result))
        }
    }
}
