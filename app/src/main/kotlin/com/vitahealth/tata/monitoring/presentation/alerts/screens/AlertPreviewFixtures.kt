package com.vitahealth.tata.monitoring.presentation.alerts.screens

import com.vitahealth.tata.monitoring.domain.model.entities.CaregiverAlert
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AlertStatus
import java.time.Instant

/** Static Compose-preview data, never used for actual patient monitoring. */
internal val previewAlerts =
    listOf(
        CaregiverAlert(
            id = 1,
            intakeId = "101",
            medicationName = "Losartán 50 mg",
            scheduledAt = Instant.parse("2026-10-05T13:00:00Z"),
            reason = "Toma no confirmada dentro del periodo de tolerancia",
            status = AlertStatus.OPEN,
            openedAt = Instant.parse("2026-10-05T13:30:00Z"),
            closedAt = null,
        ),
        CaregiverAlert(
            id = 2,
            intakeId = "98",
            medicationName = "Metformina 500 mg",
            scheduledAt = Instant.parse("2026-10-04T17:30:00Z"),
            reason = "Toma no confirmada dentro del periodo de tolerancia",
            status = AlertStatus.ATTENDED,
            openedAt = Instant.parse("2026-10-04T18:00:00Z"),
            closedAt = null,
        ),
    )
