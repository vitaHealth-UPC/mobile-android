package com.vitahealth.tata.carelink.application.commands

import com.vitahealth.tata.carelink.application.readmodels.LinkedAdult
import com.vitahealth.tata.carelink.application.readmodels.ProfileLinkingCode

/** Typed request for the caregiver-owned profile port. */
data class GenerateCareLinkCodeCommand(val caregiverId: String, val adult: LinkedAdult)
