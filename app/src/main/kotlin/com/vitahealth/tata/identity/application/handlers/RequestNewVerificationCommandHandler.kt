package com.vitahealth.tata.identity.application.handlers

import com.vitahealth.tata.identity.application.IdentityRepository
import com.vitahealth.tata.identity.application.commands.RequestNewVerificationCommand
import com.vitahealth.tata.identity.domain.model.CaregiverAccount
import com.vitahealth.tata.shared.common.result.AppResult

class RequestNewVerificationCommandHandler(
    private val repository: IdentityRepository,
) {
    suspend operator fun invoke(command: RequestNewVerificationCommand): AppResult<CaregiverAccount> {
        val email = command.email.trim()
        if (email.isBlank()) {
            return AppResult.Failure(
                message = "E-mail is required",
                code = "VALIDATION_ERROR",
            )
        }
        return repository.requestNewVerification(email)
    }
}
