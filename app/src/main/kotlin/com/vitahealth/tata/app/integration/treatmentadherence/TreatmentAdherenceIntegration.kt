package com.vitahealth.tata.app.integration.treatmentadherence

import com.vitahealth.tata.analytics.application.ports.TreatmentAdherenceRepository
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.ports.TreatmentAdherenceReader
import com.vitahealth.tata.treatment.application.readmodels.TreatmentAdherencePoint
import com.vitahealth.tata.treatment.application.readmodels.TreatmentAdherenceReadModel

/** Translates Analytics evidence into Treatment's screen contract at the composition boundary. */
class TreatmentAdherenceIntegration(private val repository: TreatmentAdherenceRepository) :
    TreatmentAdherenceReader {
    override suspend fun read(
        olderAdultId: String,
        treatmentId: String,
    ): AppResult<TreatmentAdherenceReadModel?> {
        if (olderAdultId.isBlank() || treatmentId.isBlank())
            return AppResult.Failure("Selecciona un tratamiento válido.", code = "INVALID_REQUEST")
        return when (val result = repository.getTreatmentSummary(olderAdultId, treatmentId, 7)) {
            is AppResult.Failure -> result
            is AppResult.Success ->
                AppResult.Success(
                    result.value?.let { summary ->
                        TreatmentAdherenceReadModel(
                            summary.adherencePercent,
                            summary.trend.map {
                                TreatmentAdherencePoint(it.date, it.adherencePercent)
                            },
                        )
                    }
                )
        }
    }
}
