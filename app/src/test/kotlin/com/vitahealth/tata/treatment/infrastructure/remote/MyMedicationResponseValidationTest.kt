package com.vitahealth.tata.treatment.infrastructure.remote

import com.google.gson.stream.MalformedJsonException
import com.vitahealth.tata.shared.common.result.AppResult
import java.lang.reflect.Proxy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class MyMedicationResponseValidationTest {
    private val medication = MedicationResponse("medication", "adult", "Losartán", "50 mg", true)
    private val treatment =
        TreatmentResponse(
            "treatment",
            "adult",
            "Presión",
            "ACTIVE",
            "medication",
            "1 comprimido",
            "Diaria",
            listOf("08:00"),
            "Con agua",
            10,
        )
    private val valid = MyMedicationResponse(medication, listOf(treatment))

    @Test
    fun rejectsUnknownStatesAndUnrelatedOrDuplicatedTreatments() = runBlocking {
        for (body in
            listOf(
                valid.copy(treatments = listOf(treatment.copy(status = "UNKNOWN"))),
                valid.copy(treatments = listOf(treatment.copy(olderAdultId = "other"))),
                valid.copy(treatments = listOf(treatment.copy(medicationId = "other"))),
                valid.copy(treatments = listOf(treatment.copy(id = ""))),
                valid.copy(treatments = listOf(treatment, treatment)),
                valid.copy(medication = medication.copy(name = "")),
            )) {
            assertEquals(
                "INVALID_RESPONSE",
                (repository { Response.success(listOf(body)) }.list() as AppResult.Failure).code,
            )
        }
    }

    @Test
    fun distinguishesEmptyCatalogFromMissingOrDuplicatedResources() = runBlocking {
        assertEquals(
            emptyList<Any>(),
            (repository { Response.success(emptyList()) }.list() as AppResult.Success).value,
        )
        assertEquals(
            "INVALID_RESPONSE",
            (repository { Response.success(null) }.list() as AppResult.Failure).code,
        )
        assertEquals(
            "INVALID_RESPONSE",
            (repository { Response.success(listOf(valid, valid)) }.list() as AppResult.Failure).code,
        )
        val result = repository { Response.success(listOf(valid)) }.list()
        assertTrue(result is AppResult.Success)
        assertEquals("1 comprimido", (result as AppResult.Success).value.single().dose)
    }

    @Test
    fun malformedJsonIsNotReportedAsLostConnectivity() = runBlocking {
        val result = repository { throw MalformedJsonException("Invalid JSON") }.list()
        assertEquals("INVALID_RESPONSE", (result as AppResult.Failure).code)
    }

    private fun repository(
        call: () -> Response<List<MyMedicationResponse>>
    ): RemoteMyMedicationsRepository {
        val api =
            Proxy.newProxyInstance(
                TreatmentApiService::class.java.classLoader,
                arrayOf(TreatmentApiService::class.java),
            ) { _, _, _ ->
                call()
            } as TreatmentApiService
        return RemoteMyMedicationsRepository(api)
    }
}
