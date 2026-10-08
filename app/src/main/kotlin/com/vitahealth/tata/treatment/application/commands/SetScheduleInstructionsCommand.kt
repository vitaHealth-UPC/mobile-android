package com.vitahealth.tata.treatment.application.commands

data class SetScheduleInstructionsCommand(
    val scheduleText: String,
    val instructions: String,
)
