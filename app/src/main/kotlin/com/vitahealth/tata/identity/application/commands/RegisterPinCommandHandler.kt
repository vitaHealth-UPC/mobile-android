package com.vitahealth.tata.identity.application.commands

import com.vitahealth.tata.identity.application.ports.SessionAccessRepository
import com.vitahealth.tata.shared.common.result.AppResult
/** Validates a four-digit PIN and adult reference before consent-bound setup. */
class RegisterPinCommandHandler(private val repository: SessionAccessRepository) {
    suspend operator fun invoke(command: RegisterPinCommand): AppResult<Unit> {
        if (command.olderAdultId.isBlank() || !command.pin.matches(Regex("[0-9]{4}"))) {
            return AppResult.Failure("Invalid PIN or account", code = "INVALID_PIN")
        }
        return repository.registerPin(command.olderAdultId, command.pin)
    }
}
