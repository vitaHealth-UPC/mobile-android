package com.vitahealth.tata.identity.application.commands

import com.vitahealth.tata.identity.application.ports.SessionAccessRepository
import com.vitahealth.tata.identity.application.readmodels.SessionSubject
import com.vitahealth.tata.shared.common.result.AppResult
/** Validates caregiver credentials before requesting an authenticated session. */
class SignInCommandHandler(private val repository: SessionAccessRepository) {
    suspend operator fun invoke(command: SignInCommand): AppResult<SessionSubject> {
        if (command.email.isBlank() || command.password.isBlank()) {
            return AppResult.Failure("Email and password are required", code = "REQUIRED_FIELDS")
        }
        return repository.signIn(command.email, command.password)
    }
}
