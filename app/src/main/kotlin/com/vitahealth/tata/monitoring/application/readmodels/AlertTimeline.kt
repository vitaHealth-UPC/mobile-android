package com.vitahealth.tata.monitoring.application.readmodels

import com.vitahealth.tata.monitoring.domain.model.entities.CaregiverAlert
import java.time.LocalDate

/** Alert follow-up and dose events retain their distinct identities and actions. */
data class AlertTimeline(
    val alerts: List<CaregiverAlert>,
    val doses: List<MonitoredDose>,
    val olderAdultName: String,
    val day: LocalDate,
)
