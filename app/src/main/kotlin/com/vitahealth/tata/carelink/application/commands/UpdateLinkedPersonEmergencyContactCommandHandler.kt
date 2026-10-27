package com.vitahealth.tata.carelink.application.commands

import com.vitahealth.tata.carelink.application.ports.LinkedPersonEmergencyContactUpdater
import com.vitahealth.tata.shared.common.result.AppResult

data class UpdateLinkedPersonEmergencyContactCommand(
    val caregiverId: String,
    val olderAdultId: String,
    val fullName: String,
    val relationship: String,
    val phone: String,
) {
    override fun toString() = "UpdateLinkedPersonEmergencyContactCommand[personalData=REDACTED]"
}

class UpdateLinkedPersonEmergencyContactCommandHandler(
    private val updater: LinkedPersonEmergencyContactUpdater
) {
    suspend operator fun invoke(
        command: UpdateLinkedPersonEmergencyContactCommand
    ): AppResult<Unit> {
        val name = command.fullName.trim()
        val relationship = command.relationship.trim()
        val phone = command.phone.trim()
        if (
            command.caregiverId.isBlank() ||
                command.olderAdultId.isBlank() ||
                name.isBlank() ||
                name.length > 160 ||
                relationship.isBlank() ||
                relationship.length > 80 ||
                phone.isBlank() ||
                phone.length > 40
        ) {
            return AppResult.Failure("Datos de contacto no válidos.", code = "VALIDATION_ERROR")
        }
        return updater.update(
            command.caregiverId.trim(),
            command.olderAdultId.trim(),
            name,
            relationship,
            phone,
        )
    }
}
