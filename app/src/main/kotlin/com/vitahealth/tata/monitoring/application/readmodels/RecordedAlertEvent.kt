package com.vitahealth.tata.monitoring.application.readmodels

import java.time.Instant

data class RecordedAlertEvent(val type: Type, val occurredAt: Instant) {
    enum class Type {
        INTAKE_SCHEDULED,
        REINFORCED_REMINDER_SENT,
        ALERT_GENERATED,
        ALERT_CLOSED,
    }
}
