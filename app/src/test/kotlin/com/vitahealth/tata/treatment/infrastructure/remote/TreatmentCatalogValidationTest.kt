package com.vitahealth.tata.treatment.infrastructure.remote

import com.vitahealth.tata.shared.common.result.AppResult
import java.lang.reflect.Proxy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class TreatmentCatalogValidationTest {
    private val draft =
        TreatmentResponse(
            "treatment",
            "adult",
            "Presión",
            "DRAFT",
            null,
            null,
            null,
            null,
            null,
            null,
        )

    @Test
    fun duplicateTreatmentIdentityIsRejectedEvenWhenContentDiffers() = runBlocking {
        for (second in listOf(draft, draft.copy(name = "Otra pauta"))) {
            val result = repository(listOf(draft, second)).listTreatments("caregiver", "adult")
            assertEquals("INVALID_RESPONSE", (result as AppResult.Failure).code)
        }
        assertTrue(
            repository(listOf(draft, draft.copy(id = "second")))
                .listTreatments("caregiver", "adult") is AppResult.Success
        )
    }

    @Test
    fun catalogRejectsAnotherAdultsTreatmentAndMissingRequiredIdentity() = runBlocking {
        for (resource in
            listOf(
                draft.copy(olderAdultId = "other"),
                draft.copy(id = ""),
                draft.copy(name = ""),
            )) {
            val result = repository(listOf(resource)).listTreatments("caregiver", "adult")
            assertEquals("INVALID_RESPONSE", (result as AppResult.Failure).code)
        }
    }

    @Test
    fun unknownStateRetainsTheDetailEndpointsErrorContract() = runBlocking {
        val result =
            repository(listOf(draft.copy(status = "UNKNOWN"))).listTreatments("caregiver", "adult")
        assertEquals("INVALID_TREATMENT_STATE", (result as AppResult.Failure).code)
    }

    @Test
    fun emptyCatalogIsValidButMissingBodyIsInvalid() = runBlocking {
        val empty = repository(emptyList()).listTreatments("caregiver", "adult")
        assertTrue(empty is AppResult.Success)
        assertEquals(emptyList<Any>(), (empty as AppResult.Success).value)
        val missing = repository(null).listTreatments("caregiver", "adult")
        assertEquals("INVALID_RESPONSE", (missing as AppResult.Failure).code)
    }

    private fun repository(body: List<TreatmentResponse>?): RemoteTreatmentCatalogRepository {
        val api =
            Proxy.newProxyInstance(
                TreatmentApiService::class.java.classLoader,
                arrayOf(TreatmentApiService::class.java),
            ) { _, _, _ ->
                Response.success(body)
            } as TreatmentApiService
        return RemoteTreatmentCatalogRepository(api)
    }
}
