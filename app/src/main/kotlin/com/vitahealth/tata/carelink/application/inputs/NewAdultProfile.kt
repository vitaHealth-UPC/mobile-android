package com.vitahealth.tata.carelink.application.inputs

import java.time.LocalDate

/** Profile and emergency-contact details submitted when registering an adult. */
data class NewAdultProfile(
    val name: String,
    val birthDate: LocalDate,
    val contactName: String,
    val relationship: String,
    val phone: String,
)
