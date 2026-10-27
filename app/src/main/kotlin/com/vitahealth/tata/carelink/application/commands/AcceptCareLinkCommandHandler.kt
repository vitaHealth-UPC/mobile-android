package com.vitahealth.tata.carelink.application.commands

import com.vitahealth.tata.carelink.domain.repositories.CareLinkRepository
import com.vitahealth.tata.carelink.domain.model.entities.CareLink
import com.vitahealth.tata.shared.common.result.AppResult

/** Submits a temporary linking code through the care-link repository. */
class AcceptCareLinkCommandHandler(private val repository: CareLinkRepository) {
    suspend operator fun invoke(command: AcceptCareLinkCommand): AppResult<CareLink> {
        if (command.caregiverId.isBlank() || command.code.isBlank()) {
            return AppResult.Failure("Invalid reference", code = "INVALID_CAREGIVER_REFERENCE")
        }
        return repository.acceptLink(command.caregiverId, command.code)
    }
}
