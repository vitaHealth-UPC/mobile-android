package com.vitahealth.tata.identity.application.handlers

import com.vitahealth.tata.identity.application.IdentityRepository
import com.vitahealth.tata.identity.application.commands.VerifyCaregiverEmailCommand
import com.vitahealth.tata.identity.domain.model.CaregiverAccount
import com.vitahealth.tata.shared.common.result.AppResult

class VerifyCaregiverEmailCommandHandler(
    private val repository: IdentityRepository,
) {
    suspend operator fun invoke(command: VerifyCaregiverEmailCommand): AppResult<CaregiverAccount> {
        if (!command.code.matches(Regex("\\d{6}"))) {
            return AppResult.Failure("Verification code must contain 6 digits", code = "INVALID_VERIFICATION")
        }
        return repository.verifyEmail(command.email.trim(), command.code)
    }
}
