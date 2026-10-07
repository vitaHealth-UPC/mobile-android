package com.vitahealth.tata.intake.application.commands

import com.vitahealth.tata.intake.domain.model.ConfirmationChannel

data class ConfirmDoseCommand(val intakeId: String, val channel: ConfirmationChannel)
