package com.vitahealth.tata.treatment.application

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.domain.model.Medication
import com.vitahealth.tata.treatment.domain.model.Treatment

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
