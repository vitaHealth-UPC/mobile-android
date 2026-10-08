package com.vitahealth.tata.treatment.application.handlers

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.commands.SetDoseFrequencyCommand
import com.vitahealth.tata.treatment.domain.model.Dosage
import com.vitahealth.tata.treatment.domain.model.Frequency
import com.vitahealth.tata.treatment.domain.model.RegimenBasics

class SetDoseFrequencyCommandHandler {
    operator fun invoke(command: SetDoseFrequencyCommand): AppResult<RegimenBasics> {
        val dosage = Dosage.parse(command.dosage)
            ?: return AppResult.Failure(
                message = "Ingresa una dosis válida.",
                code = "INVALID_DOSAGE",
            )
        val frequency = Frequency.parse(command.frequency)
            ?: return AppResult.Failure(
                message = "Ingresa una frecuencia válida.",
                code = "INVALID_FREQUENCY",
            )

        return AppResult.Success(
            RegimenBasics(
                dosage = dosage,
                frequency = frequency,
            ),
        )
    }
}
