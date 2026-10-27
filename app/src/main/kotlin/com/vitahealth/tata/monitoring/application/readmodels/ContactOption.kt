package com.vitahealth.tata.monitoring.application.readmodels

import com.vitahealth.tata.monitoring.domain.model.valueobjects.ContactChannel

/**
 * What the alert detail and the family summary need to offer contact: [channel] is null when none
 * exists.
 */
data class ContactOption(val channel: ContactChannel?, val firstName: String?)
