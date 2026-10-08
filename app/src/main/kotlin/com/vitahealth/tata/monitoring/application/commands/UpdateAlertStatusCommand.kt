package com.vitahealth.tata.monitoring.application.commands

import com.vitahealth.tata.monitoring.domain.model.AlertStatus

data class UpdateAlertStatusCommand(
    val caregiverId: String,
    val olderAdultId: String,
    val alertId: Long,
    val status: AlertStatus,
)
