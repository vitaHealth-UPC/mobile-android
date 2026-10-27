package com.vitahealth.tata.carelink.application.commands

import com.vitahealth.tata.carelink.application.readmodels.LinkedAdult
import com.vitahealth.tata.carelink.application.inputs.NewAdultProfile

/** Typed request for the caregiver-owned profile port. */
data class RegisterOlderAdultCommand(val caregiverId: String, val profile: NewAdultProfile)
