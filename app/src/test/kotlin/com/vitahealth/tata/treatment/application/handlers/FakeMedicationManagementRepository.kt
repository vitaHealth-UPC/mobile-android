package com.vitahealth.tata.treatment.application.handlers

import com.vitahealth.tata.treatment.application.MedicationManagementRepository
import com.vitahealth.tata.treatment.domain.model.Medication
import com.vitahealth.tata.shared.common.result.AppResult

/** An in-memory backend for the medications of one older adult; it can be told to fail. */
class FakeMedicationManagementRepository(
    var medications: List<Medication> = emptyList(),
) : MedicationManagementRepository {
    var failWith: AppResult.Failure? = null
    var listCalls = 0
    val updates = mutableListOf<Triple<String, String, String>>()
    val deactivations = mutableListOf<String>()

    override suspend fun listMedications(caregiverId: String, olderAdultId: String): AppResult<List<Medication>> {
        listCalls++
        return failWith ?: AppResult.Success(medications)
    }

    override suspend fun updateMedication(
        caregiverId: String,
        medicationId: String,
        name: String,
        presentation: String,
    ): AppResult<Medication> {
        updates += Triple(medicationId, name, presentation)
        failWith?.let { return it }
        val current = medications.first { it.id == medicationId }
        val updated = current.copy(name = name, presentation = presentation)
        medications = medications.map { if (it.id == medicationId) updated else it }
        return AppResult.Success(updated)
    }

    override suspend fun deactivateMedication(caregiverId: String, medicationId: String): AppResult<Medication> {
        deactivations += medicationId
        failWith?.let { return it }
        val updated = medications.first { it.id == medicationId }.copy(active = false)
        medications = medications.map { if (it.id == medicationId) updated else it }
        return AppResult.Success(updated)
    }
}

val losartan = Medication("med-1", "adult-1", "Losartán", "50 mg, comprimido", active = true)
val metformin = Medication("med-2", "adult-1", "Metformina", "850 mg, comprimido", active = false)
val NotLinked = AppResult.Failure(message = "no link", code = "CARE_LINK_NOT_AUTHORIZED")
val NoConnection = AppResult.Failure(message = "offline", code = "NETWORK_UNAVAILABLE")
val Conflict = AppResult.Failure(message = "inactive", code = "CONFLICT")
val Missing = AppResult.Failure(message = "missing", code = "RESOURCE_NOT_FOUND")
