package com.vitahealth.tata.identity.application.commands

import com.vitahealth.tata.identity.application.ports.SessionAccessRepository
import com.vitahealth.tata.identity.application.readmodels.SessionSubject
import com.vitahealth.tata.shared.common.result.AppResult
/** Validates a four-digit PIN and adult reference before authenticating. */
class SignInWithPinCommandHandler(private val repository: SessionAccessRepository) {
    suspend operator fun invoke(command: SignInWithPinCommand): AppResult<SessionSubject> {
        if (command.olderAdultId.isBlank() || !command.pin.matches(Regex("[0-9]{4}"))) {
            return AppResult.Failure("Invalid PIN or account", code = "INVALID_PIN")
        }
        return repository.signInWithPin(command.olderAdultId, command.pin)
    }
}
