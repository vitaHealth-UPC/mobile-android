package com.vitahealth.tata.monitoring.application.readmodels

import java.time.LocalDate

/** Authorized profile evidence; absent birth date never becomes a guessed age. */
data class MonitoredPerson(val id: String, val fullName: String, val birthDate: LocalDate?)
