package com.vitahealth.tata.monitoring.application.handlers

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.application.AlertsRepository
import com.vitahealth.tata.monitoring.application.commands.UpdateAlertStatusCommand
import com.vitahealth.tata.monitoring.domain.model.AlertStatus
import com.vitahealth.tata.monitoring.domain.model.CaregiverAlert
import com.vitahealth.tata.shared.common.result.AppResult

/** Attends or closes an alert. OPEN is never sent: the backend only accepts ATTENDED and CLOSED. */
class UpdateAlertStatusCommandHandler(private val repository: AlertsRepository) {
    suspend operator fun invoke(command: UpdateAlertStatusCommand): AppResult<CaregiverAlert> {
        if (command.caregiverId.isBlank() || command.olderAdultId.isBlank() || command.alertId <= 0) {
            return AppResult.Failure("No se pudo identificar la alerta.", code = AlertFailureCodes.INVALID_REFERENCE)
        }
        if (command.status == AlertStatus.OPEN) {
            return AppResult.Failure("Una alerta no puede volver a quedar pendiente.", code = AlertFailureCodes.STATUS_NOT_ACCEPTED)
        }
        return repository.updateStatus(command.caregiverId.trim(), command.olderAdultId.trim(), command.alertId, command.status)
    }
}
