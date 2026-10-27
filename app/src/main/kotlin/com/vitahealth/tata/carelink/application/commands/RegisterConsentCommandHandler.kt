package com.vitahealth.tata.carelink.application.commands

import com.vitahealth.tata.carelink.domain.repositories.CareLinkRepository
import com.vitahealth.tata.carelink.domain.model.entities.CareLink
import com.vitahealth.tata.shared.common.result.AppResult

/** Records a consent decision through the care-link repository. */
class RegisterConsentCommandHandler(private val repository: CareLinkRepository) {
    suspend operator fun invoke(command: RegisterConsentCommand): AppResult<CareLink> {
        if (command.careLinkId.isBlank()) {
            return AppResult.Failure("Invalid reference", code = "INVALID_CARE_LINK_REFERENCE")
        }
        return repository.registerConsent(command.careLinkId, command.accepted)
    }
}
