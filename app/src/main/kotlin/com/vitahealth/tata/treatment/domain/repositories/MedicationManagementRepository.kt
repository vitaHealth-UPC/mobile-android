package com.vitahealth.tata.treatment.domain.repositories

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.domain.model.entities.Medication

/** Lists and maintains medications without removing their recorded history. */
interface MedicationManagementRepository {
    /** Ordered by name by the backend, inactive medications included. */
    suspend fun listMedications(
        caregiverId: String,
        olderAdultId: String,
    ): AppResult<List<Medication>>

    suspend fun updateMedication(
        caregiverId: String,
        medicationId: String,
        name: String,
        presentation: String,
    ): AppResult<Medication>

    /** The medication keeps its history; it just stops producing new intakes. */
    suspend fun deactivateMedication(
        caregiverId: String,
        medicationId: String,
    ): AppResult<Medication>
}
