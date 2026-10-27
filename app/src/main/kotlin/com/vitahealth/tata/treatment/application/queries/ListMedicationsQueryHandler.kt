package com.vitahealth.tata.treatment.application.queries

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.domain.model.entities.Medication
import com.vitahealth.tata.treatment.domain.repositories.MedicationManagementRepository

/** Validates the caregiver and adult references before reading medications. */
class ListMedicationsQueryHandler(private val repository: MedicationManagementRepository) {
    suspend operator fun invoke(query: ListMedicationsQuery): AppResult<List<Medication>> {
        val caregiverId = query.caregiverId.trim()
        val olderAdultId = query.olderAdultId.trim()

        if (caregiverId.isBlank() || olderAdultId.isBlank()) {
            return AppResult.Failure(
                message = "The older adult could not be identified.",
                code = "INVALID_OLDER_ADULT_REFERENCE",
            )
        }
        return repository.listMedications(caregiverId, olderAdultId)
    }
}
