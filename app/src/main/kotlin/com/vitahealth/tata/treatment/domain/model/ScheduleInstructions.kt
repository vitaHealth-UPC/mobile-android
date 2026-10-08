package com.vitahealth.tata.treatment.domain.model

data class ScheduleInstructions(
    val schedule: AdministrationSchedule,
    val instructions: AdministrationInstructions,
)
