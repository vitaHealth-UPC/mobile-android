package com.vitahealth.tata.intake.application.commands

import com.vitahealth.tata.intake.domain.model.valueobjects.ConfirmationChannel

/** Identifies the scheduled intake and the explicit interaction used to confirm it. */
data class ConfirmDoseCommand(val intakeId: String, val channel: ConfirmationChannel)
