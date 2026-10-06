package com.vitahealth.tata.identity.application.handlers

import com.vitahealth.tata.identity.application.IdentityRepository
import com.vitahealth.tata.identity.application.commands.RegisterCaregiverCommand
import com.vitahealth.tata.identity.domain.model.CaregiverAccount
import com.vitahealth.tata.shared.common.result.AppResult

class RegisterCaregiverCommandHandler(
    private val repository: IdentityRepository,
) {
    suspend operator fun invoke(command: RegisterCaregiverCommand): AppResult<CaregiverAccount> {
        if (command.name.isBlank()) return AppResult.Failure("Name is required")
        if (!command.email.contains("@")) return AppResult.Failure("A valid e-mail is required")
        if (command.password.length < 8) return AppResult.Failure("Password must have at least 8 characters")
        return repository.registerCaregiver(command.name.trim(), command.email.trim(), command.password)
    }
}
