package com.vitahealth.tata.intake.infrastructure.remote

import com.vitahealth.tata.intake.application.commands.ConfirmDoseCommand
import com.vitahealth.tata.intake.domain.model.valueobjects.ConfirmationChannel
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.Response

/** Rejects unrelated/malformed payloads before they can replace offline intake state. */
class IntakeResponseBoundaryTest {
    @Test
    fun malformedJsonFromRealHttpDoesNotBecomeAnOfflineFailure() = runBlocking {
        MockWebServer().use { server ->
            val api =
                ApiClientFactory.create(server.url("/").toString()) { null }
                    .create(IntakeApiService::class.java)
            for (operation in operations(api)) {
                server.enqueue(MockResponse().setBody("{broken-json"))
                assertCode("INVALID_RESPONSE", operation())
            }
        }
    }

    private val from = Instant.parse("2026-10-06T00:00:00Z")
    private val to = from.plusSeconds(86400)
    private val dose =
        IntakeResponse(
            "intake",
            "treatment",
            "medication",
            "adult",
            "Losartán",
            "1 comprimido",
            null,
            "2026-10-06T12:00:00Z",
            "PENDING",
        )
    private val command = ConfirmDoseCommand("intake", ConfirmationChannel.TOUCH)

    @Test
    fun detailAndConfirmationRejectAnotherIntake() = runBlocking {
        val api =
            TestApi(
                dose.copy(
                    id = "another",
                    status = "CONFIRMED",
                    confirmedAt = "2026-10-06T12:01:00Z",
                )
            )
        assertCode("INVALID_RESPONSE", RemoteDoseDetailRepository(api).getDoseDetail("intake"))
        assertCode("INVALID_RESPONSE", RemoteDoseConfirmationRepository(api).confirm(command))
    }

    @Test
    fun nextAndAgendaRejectAnotherAdult() = runBlocking {
        val api = TestApi(dose.copy(olderAdultId = "another"))
        assertCode("INVALID_RESPONSE", RemoteNextDoseRepository(api).getNextDose("adult"))
        assertCode(
            "INVALID_RESPONSE",
            RemoteIntakeAgendaRepository(api).getAgenda("adult", from, to),
        )
    }

    @Test
    fun confirmationRequiresAnAcknowledgedStatusAndTimestamp() = runBlocking {
        for (body in
            listOf(
                dose,
                dose.copy(status = "CONFIRMED"),
                dose.copy(status = "LATE", confirmedAt = "invalid"),
            )) {
            assertCode(
                "INVALID_RESPONSE",
                RemoteDoseConfirmationRepository(TestApi(body)).confirm(command),
            )
        }
    }

    @Test
    fun missingRequiredSuccessBodyIsNotAnOfflineFailure() = runBlocking {
        for (operation in operations(TestApi(null))) assertCode("INVALID_RESPONSE", operation())
    }

    @Test
    fun malformedDataDoesNotTriggerOfflineFallback() = runBlocking {
        for (operation in
            operations(TestApi(dose, failure = IllegalStateException("malformed JSON")))) {
            assertCode("INVALID_RESPONSE", operation())
        }
    }

    @Test
    fun connectionFailureRemainsRetryableForEveryOperation() = runBlocking {
        for (operation in operations(TestApi(dose, failure = IOException("offline")))) {
            assertCode("NETWORK_UNAVAILABLE", operation())
        }
    }

    @Test
    fun cancellationPropagatesForEveryOperation() = runBlocking {
        val cancellation = CancellationException("screen closed")
        for (operation in operations(TestApi(dose, failure = cancellation))) {
            try {
                operation()
                fail("Cancellation must propagate")
            } catch (actual: CancellationException) {
                assertSame(cancellation, actual)
            }
        }
    }

    @Test
    fun accessFailuresAreNotReplacedWithCachedSuccess() = runBlocking {
        for ((status, code) in listOf(401 to "SESSION_REQUIRED", 403 to "ACCESS_DENIED")) {
            for (operation in operations(TestApi(dose, status = status))) assertCode(
                code,
                operation(),
            )
        }
    }

    @Test
    fun agendaRejectsDuplicatesAndOutOfRangeRowsAsAWhole() = runBlocking {
        for (rows in
            listOf(
                listOf(dose, dose),
                listOf(dose.copy(scheduledAt = to.toString())),
                listOf(dose.copy(scheduledAt = from.minusSeconds(1).toString())),
            )) {
            val api = TestApi(dose, rows = rows)
            assertCode(
                "INVALID_RESPONSE",
                RemoteIntakeAgendaRepository(api).getAgenda("adult", from, to),
            )
        }
    }

    @Test
    fun malformedSuppliedTimestampIsNotSilentlyRemoved() {
        assertCode("INVALID_RESPONSE", mapIntakeDetail(dose.copy(confirmedAt = "invalid")))
    }

    @Test
    fun blankRequiredIdentityAndMedicationFieldsAreRejected() {
        for (body in
            listOf(
                dose.copy(id = ""),
                dose.copy(treatmentId = " "),
                dose.copy(medicationId = ""),
                dose.copy(olderAdultId = ""),
                dose.copy(medicationName = ""),
                dose.copy(dose = ""),
            )) {
            assertCode("INVALID_RESPONSE", mapIntakeDetail(body))
        }
    }

    private fun operations(api: IntakeApiService): List<suspend () -> AppResult<*>> =
        listOf(
            { RemoteNextDoseRepository(api).getNextDose("adult") },
            { RemoteIntakeAgendaRepository(api).getAgenda("adult", from, to) },
            { RemoteDoseDetailRepository(api).getDoseDetail("intake") },
            { RemoteDoseConfirmationRepository(api).confirm(command) },
        )

    private fun assertCode(expected: String, result: AppResult<*>) {
        assertTrue("Expected $expected, got $result", result is AppResult.Failure)
        assertEquals(expected, (result as AppResult.Failure).code)
    }

    private class TestApi(
        private val body: IntakeResponse?,
        private val failure: Exception? = null,
        private val status: Int = 200,
        private val rows: List<IntakeResponse>? = body?.let(::listOf),
    ) : IntakeApiService {
        private fun <T> response(value: T?): Response<T> {
            failure?.let { throw it }
            return if (status == 200) Response.success(value)
            else Response.error(status, "{}".toResponseBody())
        }

        override suspend fun getAgenda(olderAdultId: String, from: String, to: String) =
            response(rows)

        override suspend fun getNextDose(olderAdultId: String) = response(body)

        override suspend fun getDoseDetail(intakeId: String) = response(body)

        override suspend fun confirmDose(intakeId: String, request: ConfirmIntakeRequest) =
            response(body)
    }
}
