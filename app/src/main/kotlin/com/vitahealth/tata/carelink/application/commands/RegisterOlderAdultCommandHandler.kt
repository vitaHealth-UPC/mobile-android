package com.vitahealth.tata.carelink.application.commands

import com.vitahealth.tata.carelink.application.ports.CaregiverProfilesRepository
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.carelink.application.readmodels.LinkedAdult
import com.vitahealth.tata.carelink.application.inputs.NewAdultProfile

/** Enforces required references before calling the account-scoped repository. */
class RegisterOlderAdultCommandHandler(private val repository: CaregiverProfilesRepository) {
    suspend operator fun invoke(command: RegisterOlderAdultCommand): AppResult<LinkedAdult> {
        if (command.caregiverId.isBlank() || command.profile.name.isBlank()) {
            return AppResult.Failure("Invalid adult profile", code = "VALIDATION_ERROR")
        }
        return repository.register(command.caregiverId, command.profile)
    }
}
