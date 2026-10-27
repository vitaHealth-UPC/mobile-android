package com.vitahealth.tata.treatment.infrastructure.remote

import com.vitahealth.tata.shared.common.result.AppResult
import java.lang.reflect.Proxy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import retrofit2.Response

class MedicationResponseValidationTest {
    private val valid = MedicationResponse("medication", "adult", "Losartán", "Comprimido", true)

    @Test
    fun missingIdentityOrRequiredDetailsCannotBecomeADomainMedication() {
        for (body in listOf(
            valid.copy(id = ""),
            valid.copy(olderAdultId = ""),
            valid.copy(name = " "),
            valid.copy(presentation = " "),
        )) {
            assertThrows(IllegalArgumentException::class.java) { body.toDomain() }
        }
        assertEquals("adult", valid.toDomain().olderAdultId)
    }

    @Test
    fun registrationAndListingRejectAnotherAdultsMedication() = runBlocking {
        val api = Proxy.newProxyInstance(
            TreatmentApiService::class.java.classLoader,
            arrayOf(TreatmentApiService::class.java),
        ) { _, method, _ ->
            if (method.name == "listMedications") Response.success(listOf(valid))
            else Response.success(valid)
        } as TreatmentApiService
        val repository = RemoteTreatmentRepository(api)

        val registered = repository.registerMedication("caregiver", "another-adult", "Losartán", "Comprimido")
        val listed = RemoteMedicationManagementRepository(api).listMedications("caregiver", "another-adult")

        assertEquals("INVALID_RESPONSE", (registered as AppResult.Failure).code)
        assertEquals("INVALID_RESPONSE", (listed as AppResult.Failure).code)
    }
}
