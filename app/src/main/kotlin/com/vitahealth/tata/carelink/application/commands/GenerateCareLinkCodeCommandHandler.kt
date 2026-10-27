package com.vitahealth.tata.carelink.application.commands

import com.vitahealth.tata.carelink.application.ports.CaregiverProfilesRepository
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.carelink.application.readmodels.LinkedAdult
import com.vitahealth.tata.carelink.application.readmodels.ProfileLinkingCode

/** Enforces required references before calling the account-scoped repository. */
class GenerateCareLinkCodeCommandHandler(private val repository: CaregiverProfilesRepository) {
    suspend operator fun invoke(command: GenerateCareLinkCodeCommand): AppResult<ProfileLinkingCode> {
        if (command.caregiverId.isBlank() || command.adult.id.isBlank()) {
            return AppResult.Failure("Invalid linking reference", code = "VALIDATION_ERROR")
        }
        return repository.linkingCode(command.caregiverId, command.adult)
    }
}
