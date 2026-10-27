package com.vitahealth.tata.treatment.domain.repositories

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.domain.model.entities.Medication
import com.vitahealth.tata.treatment.domain.model.entities.Treatment

/** Creates medication records and draft treatments for a caregiver and selected adult. */
interface TreatmentRepository {
    suspend fun registerMedication(
        caregiverId: String,
        olderAdultId: String,
        name: String,
        presentation: String,
    ): AppResult<Medication>

    suspend fun createTreatment(
        caregiverId: String,
        olderAdultId: String,
        name: String,
    ): AppResult<Treatment>
}
