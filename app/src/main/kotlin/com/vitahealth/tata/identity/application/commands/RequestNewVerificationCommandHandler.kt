package com.vitahealth.tata.identity.application.commands

import com.vitahealth.tata.identity.domain.model.entities.CaregiverAccount
import com.vitahealth.tata.identity.domain.repositories.IdentityRepository
import com.vitahealth.tata.shared.common.result.AppResult
/** Requests a new verification challenge for a supplied email address. */
class RequestNewVerificationCommandHandler(private val repository: IdentityRepository) {
    suspend operator fun invoke(
        command: RequestNewVerificationCommand
    ): AppResult<CaregiverAccount> {
        val email = command.email.trim()
        if (email.isBlank()) {
            return AppResult.Failure(message = "E-mail is required", code = "VALIDATION_ERROR")
        }
        return repository.requestNewVerification(email)
    }
}
