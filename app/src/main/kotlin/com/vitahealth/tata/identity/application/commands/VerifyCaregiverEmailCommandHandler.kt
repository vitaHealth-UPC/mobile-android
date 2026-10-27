package com.vitahealth.tata.identity.application.commands

import com.vitahealth.tata.identity.domain.model.entities.CaregiverAccount
import com.vitahealth.tata.identity.domain.repositories.IdentityRepository
import com.vitahealth.tata.shared.common.result.AppResult
/** Validates the verification code shape before submitting the challenge response. */
class VerifyCaregiverEmailCommandHandler(private val repository: IdentityRepository) {
    suspend operator fun invoke(command: VerifyCaregiverEmailCommand): AppResult<CaregiverAccount> {
        if (!command.code.matches(Regex("\\d{6}"))) {
            return AppResult.Failure(
                "Verification code must contain 6 digits",
                code = "INVALID_VERIFICATION",
            )
        }
        return repository.verifyEmail(command.email.trim(), command.code)
    }
}
