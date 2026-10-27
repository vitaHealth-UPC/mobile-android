package com.vitahealth.tata.treatment.domain.model.valueobjects

/** Validated local administration times paired with optional instructions. */
data class ScheduleInstructions(
    val schedule: AdministrationSchedule,
    val instructions: AdministrationInstructions,
)
