package com.vitahealth.tata.treatment.application.commands

/** Carries comma-separated local times and administration instructions. */
data class SetScheduleInstructionsCommand(val scheduleText: String, val instructions: String)
