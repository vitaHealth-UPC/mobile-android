package com.vitahealth.tata.carelink.application.commands

import com.vitahealth.tata.carelink.application.ports.LinkedPersonProfileUpdater
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.LocalDate

data class UpdateLinkedPersonProfileCommand(
    val caregiverId: String,
    val olderAdultId: String,
    val fullName: String,
    val birthDate: LocalDate,
)

/** Validates the requested basic profile change before crossing the CareLink port. */
class UpdateLinkedPersonProfileCommandHandler(private val updater: LinkedPersonProfileUpdater) {
    suspend operator fun invoke(command: UpdateLinkedPersonProfileCommand): AppResult<Unit> {
        val name = command.fullName.trim()
        if (command.caregiverId.isBlank() || command.olderAdultId.isBlank() ||
            name.isBlank() || name.length > 160 || command.birthDate.isAfter(LocalDate.now())
        ) return AppResult.Failure("Datos del perfil no validos.", code = "VALIDATION_ERROR")
        return updater.update(
            command.caregiverId.trim(), command.olderAdultId.trim(), name, command.birthDate
        )
    }
}
