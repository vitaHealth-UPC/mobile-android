package com.vitahealth.tata.treatment.application.ports

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.readmodels.TreatmentAdherenceReadModel

/** Supplies the management screen with authorized evidence for its exact adult and treatment. */
fun interface TreatmentAdherenceReader {
    suspend fun read(
        olderAdultId: String,
        treatmentId: String,
    ): AppResult<TreatmentAdherenceReadModel?>
}
