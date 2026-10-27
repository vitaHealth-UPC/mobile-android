package com.vitahealth.tata.treatment.application.ports

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.domain.model.entities.Medication

/** Restores medication availability without automatically resuming any treatment. */
fun interface MedicationReactivationPort {
    suspend fun reactivate(caregiverId: String, medicationId: String): AppResult<Medication>
}
