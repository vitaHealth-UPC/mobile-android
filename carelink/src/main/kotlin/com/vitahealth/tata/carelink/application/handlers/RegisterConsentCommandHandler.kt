package com.vitahealth.tata.carelink.application.handlers

import com.vitahealth.tata.carelink.application.CareLinkRepository
import com.vitahealth.tata.carelink.application.commands.RegisterConsentCommand

class RegisterConsentCommandHandler(
    private val repository: CareLinkRepository,
) {
    suspend operator fun invoke(command: RegisterConsentCommand) =
        repository.registerConsent(command.careLinkId, command.accepted)
}
