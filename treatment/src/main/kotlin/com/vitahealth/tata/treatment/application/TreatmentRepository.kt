package com.vitahealth.tata.treatment.application

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.domain.model.Medication

interface TreatmentRepository {
    suspend fun registerMedication(
        caregiverId: String,
        olderAdultId: String,
        name: String,
        presentation: String,
    ): AppResult<Medication>
}
