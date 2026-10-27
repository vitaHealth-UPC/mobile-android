package com.vitahealth.tata.treatment.application.commands

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.domain.model.valueobjects.Dosage
import com.vitahealth.tata.treatment.domain.model.valueobjects.Frequency
import com.vitahealth.tata.treatment.domain.model.valueobjects.RegimenBasics

/** Parses dose and frequency into the domain values used by the regimen wizard. */
class SetDoseFrequencyCommandHandler {
    operator fun invoke(command: SetDoseFrequencyCommand): AppResult<RegimenBasics> {
        val dosage =
            Dosage.parse(command.dosage)
                ?: return AppResult.Failure(
                    message = "Ingresa una dosis válida.",
                    code = "INVALID_DOSAGE",
                )
        val frequency =
            Frequency.parse(command.frequency)
                ?: return AppResult.Failure(
                    message = "Ingresa una frecuencia válida.",
                    code = "INVALID_FREQUENCY",
                )

        return AppResult.Success(RegimenBasics(dosage = dosage, frequency = frequency))
    }
}
