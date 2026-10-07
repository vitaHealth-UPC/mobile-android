package com.vitahealth.tata.monitoring.domain.model

enum class ContactChannelType { PHONE, WHATSAPP }

/** Channel the caregiver can use to reach the older adult after an alert. */
data class ContactChannel(val type: ContactChannelType, val value: String) {
    init {
        require(value.isNotBlank()) { "contact value is required" }
    }

    /** Digits only, with the country code when the value carries it, as wa.me links need. */
    val digits: String get() = value.filter(Char::isDigit)
}

/** What the alert detail and the family summary need to offer contact: [channel] is null when none exists. */
data class ContactOption(val channel: ContactChannel?, val firstName: String?)
