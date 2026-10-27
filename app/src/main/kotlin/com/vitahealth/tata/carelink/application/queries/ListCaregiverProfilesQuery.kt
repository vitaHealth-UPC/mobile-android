package com.vitahealth.tata.carelink.application.queries

import com.vitahealth.tata.carelink.application.readmodels.LinkedAdult

/** Typed request for the caregiver-owned profile port. */
data class ListCaregiverProfilesQuery(val caregiverId: String)
