package com.vitahealth.tata.treatment.infrastructure.remote

import com.vitahealth.tata.shared.common.result.AppResult
import java.lang.reflect.Proxy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class TreatmentResourceIdentityTest {
    private val treatment =
        TreatmentResponse(
            "treatment",
            "adult",
            "Presión",
            "DRAFT",
            "medication",
            null,
            null,
            null,
            null,
            null,
        )

    @Test
    fun registrationReceiptsConfirmSubmittedDescriptions() = runBlocking {
        val medication = MedicationResponse("medication", "adult", "Losartán", "Comprimido", true)
        for (body in
            listOf(medication.copy(name = "Otro"), medication.copy(presentation = "Otra"))) {
            assertInvalid(
                repository(body)
                    .registration
                    .registerMedication("caregiver", "adult", "Losartán", "Comprimido")
            )
        }
        assertTrue(
            repository(medication)
                .registration
                .registerMedication("caregiver", "adult", " Losartán ", " Comprimido ")
                is AppResult.Success
        )
        assertInvalid(
            repository(treatment.copy(name = "Otro tratamiento"))
                .registration
                .createTreatment("caregiver", "adult", "Presión")
        )
        assertTrue(
            repository(treatment).registration.createTreatment("caregiver", "adult", " Presión ")
                is AppResult.Success
        )
    }

    @Test
    fun medicationListRejectsRepeatedIdentityInsteadOfDuplicatingRows() = runBlocking {
        val medication = MedicationResponse("medication", "adult", "Losartán", "Comprimido", true)
        for (second in listOf(medication, medication.copy(name = "Otra descripción"))) {
            assertInvalid(
                repository(listOf(medication, second))
                    .management
                    .listMedications("caregiver", "adult")
            )
        }
        assertTrue(
            repository(listOf(medication, medication.copy(id = "second")))
                .management
                .listMedications("caregiver", "adult") is AppResult.Success
        )
    }

    @Test
    fun medicationReceiptsConfirmEditedFieldsAndDeactivation() = runBlocking {
        val medication = MedicationResponse("medication", "adult", "Losartán", "Comprimido", true)
        for (body in
            listOf(medication.copy(name = "Otro"), medication.copy(presentation = "Otra"))) {
            assertInvalid(
                repository(body)
                    .management
                    .updateMedication("caregiver", "medication", "Losartán", "Comprimido")
            )
        }
        assertTrue(
            repository(medication)
                .management
                .updateMedication("caregiver", "medication", " Losartán ", " Comprimido ")
                is AppResult.Success
        )
        assertInvalid(
            repository(medication).management.deactivateMedication("caregiver", "medication")
        )
        assertTrue(
            repository(medication.copy(active = false))
                .management
                .deactivateMedication("caregiver", "medication") is AppResult.Success
        )
    }

    @Test
    fun configurationReceiptMustMatchTheSubmittedRegimen() = runBlocking {
        val configured =
            treatment.copy(
                dose = "1 comprimido",
                frequency = "Diaria",
                scheduledTimes = listOf("08:00:00", "20:00:00"),
                instructions = "Con agua",
                reminderLeadMinutes = 15,
            )
        suspend fun configure(body: TreatmentResponse) =
            repository(body)
                .lifecycle
                .configureTreatment(
                    "caregiver",
                    "treatment",
                    "medication",
                    "1 comprimido",
                    "Diaria",
                    listOf("20:00", "08:00"),
                    "Con agua",
                    15,
                )
        assertTrue(configure(configured) is AppResult.Success)
        for (body in
            listOf(
                configured.copy(dose = "2 comprimidos"),
                configured.copy(frequency = "Semanal"),
                configured.copy(instructions = "Otra indicación"),
                configured.copy(reminderLeadMinutes = 10),
                configured.copy(scheduledTimes = null),
                configured.copy(scheduledTimes = listOf("09:00", "20:00")),
                configured.copy(scheduledTimes = listOf("08:00", "08:00", "20:00")),
            )) assertInvalid(configure(body))
    }

    @Test
    fun lifecycleReceiptsMustConfirmTheRequestedState() = runBlocking {
        for (status in listOf("DRAFT", "PAUSED")) {
            val lifecycle = repository(treatment.copy(status = status)).lifecycle
            assertInvalid(lifecycle.activateTreatment("caregiver", "treatment"))
            assertInvalid(lifecycle.resumeTreatment("caregiver", "treatment"))
        }
        for (status in listOf("DRAFT", "ACTIVE")) {
            assertInvalid(
                repository(treatment.copy(status = status))
                    .lifecycle
                    .pauseTreatment("caregiver", "treatment")
            )
        }
        assertTrue(
            repository(treatment.copy(status = "ACTIVE"))
                .lifecycle
                .activateTreatment("caregiver", "treatment") is AppResult.Success
        )
        assertTrue(
            repository(treatment.copy(status = "ACTIVE"))
                .lifecycle
                .resumeTreatment("caregiver", "treatment") is AppResult.Success
        )
        assertTrue(
            repository(treatment.copy(status = "PAUSED"))
                .lifecycle
                .pauseTreatment("caregiver", "treatment") is AppResult.Success
        )
    }

    @Test
    fun successfulResponsesWithoutRequiredResourcesAreInvalid() = runBlocking {
        val repository = repository(null)

        assertInvalid(
            repository.registration.registerMedication(
                "caregiver",
                "adult",
                "Losartán",
                "Comprimido",
            )
        )
        assertInvalid(repository.management.listMedications("caregiver", "adult"))
        assertInvalid(
            repository.management.updateMedication(
                "caregiver",
                "medication",
                "Losartán",
                "Comprimido",
            )
        )
        assertInvalid(repository.management.deactivateMedication("caregiver", "medication"))
        assertInvalid(repository.registration.createTreatment("caregiver", "adult", "Presión"))
        assertInvalid(repository.lifecycle.activateTreatment("caregiver", "treatment"))
        assertInvalid(repository.lifecycle.pauseTreatment("caregiver", "treatment"))
        assertInvalid(repository.lifecycle.resumeTreatment("caregiver", "treatment"))
        assertInvalid(repository.detail.getTreatmentDetail("caregiver", "treatment"))
        assertInvalid(
            repository.lifecycle.configureTreatment(
                "caregiver",
                "treatment",
                "medication",
                "1 comprimido",
                "Diaria",
                listOf("08:00"),
                "Con agua",
                15,
            )
        )
    }

    @Test
    fun medicationMutationsRejectAnotherMedication() = runBlocking {
        val body = MedicationResponse("other", "adult", "Losartán", "Comprimido", true)
        val repository = repository(body)

        assertInvalid(
            repository.management.updateMedication(
                "caregiver",
                "medication",
                "Losartán",
                "Comprimido",
            )
        )
        assertInvalid(repository.management.deactivateMedication("caregiver", "medication"))
    }

    @Test
    fun lifecycleConfigurationAndDetailRejectAnotherTreatment() = runBlocking {
        val repository = repository(treatment.copy(id = "other"))

        assertInvalid(repository.lifecycle.activateTreatment("caregiver", "treatment"))
        assertInvalid(repository.lifecycle.pauseTreatment("caregiver", "treatment"))
        assertInvalid(repository.lifecycle.resumeTreatment("caregiver", "treatment"))
        assertInvalid(repository.detail.getTreatmentDetail("caregiver", "treatment"))
        assertInvalid(
            repository.lifecycle.configureTreatment(
                "caregiver",
                "treatment",
                "medication",
                "1 comprimido",
                "Diaria",
                listOf("08:00"),
                "Con agua",
                15,
            )
        )
    }

    @Test
    fun creationRejectsAnotherAdultAndConfigurationRejectsAnotherMedication() = runBlocking {
        assertInvalid(
            repository(treatment.copy(olderAdultId = "other"))
                .registration
                .createTreatment("caregiver", "adult", "Presión")
        )
        assertInvalid(
            repository(treatment.copy(medicationId = "other"))
                .lifecycle
                .configureTreatment(
                    "caregiver",
                    "treatment",
                    "medication",
                    "1 comprimido",
                    "Diaria",
                    listOf("08:00"),
                    "Con agua",
                    15,
                )
        )
    }

    @Test
    fun recognizedDraftAndUnknownStatusRetainTheirContracts() = runBlocking {
        val draft = repository(treatment).detail.getTreatmentDetail("caregiver", "treatment")
        assertTrue(draft is AppResult.Success)
        val unknown =
            repository(treatment.copy(status = "UNKNOWN"))
                .detail
                .getTreatmentDetail("caregiver", "treatment")
        assertEquals("INVALID_TREATMENT_STATE", (unknown as AppResult.Failure).code)
    }

    private fun assertInvalid(result: AppResult<*>) {
        assertEquals("INVALID_RESPONSE", (result as AppResult.Failure).code)
    }

    private fun repository(body: Any?): Repositories {
        val api =
            Proxy.newProxyInstance(
                TreatmentApiService::class.java.classLoader,
                arrayOf(TreatmentApiService::class.java),
            ) { _, _, _ ->
                Response.success(body)
            } as TreatmentApiService
        return Repositories(api)
    }

    private class Repositories(api: TreatmentApiService) {
        val registration = RemoteTreatmentRepository(api)
        val management = RemoteMedicationManagementRepository(api)
        val lifecycle = RemoteTreatmentLifecycleRepository(api)
        val detail = RemoteTreatmentDetailRepository(api)
    }
}
