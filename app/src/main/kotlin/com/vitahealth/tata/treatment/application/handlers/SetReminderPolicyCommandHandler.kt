package com.vitahealth.tata.treatment.application.handlers

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.commands.SetReminderPolicyCommand
import com.vitahealth.tata.treatment.domain.model.ReminderDelayMinutes
import com.vitahealth.tata.treatment.domain.model.ReminderPolicy

class SetReminderPolicyCommandHandler {
    operator fun invoke(command: SetReminderPolicyCommand): AppResult<ReminderPolicy> {
        val delay = ReminderDelayMinutes.parse(command.followUpDelayMinutes)
            ?: return AppResult.Failure(
                message = "Ingresa un intervalo entre 0 y 1440 minutos.",
                code = "INVALID_REMINDER_DELAY",
            )

        return AppResult.Success(
            ReminderPolicy(followUpDelayMinutes = delay),
        )
    }
}
