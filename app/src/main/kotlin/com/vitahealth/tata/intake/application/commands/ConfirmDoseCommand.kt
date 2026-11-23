package com.vitahealth.tata.intake.application.commands

import com.vitahealth.tata.intake.domain.model.valueobjects.ConfirmationChannel
import java.time.Instant

/** Identifies the scheduled intake and the interaction and original action time retained across offline retries. */
data class ConfirmDoseCommand(
    val intakeId: String,
    val channel: ConfirmationChannel,
    val confirmedAt: Instant = Instant.now(),
)
