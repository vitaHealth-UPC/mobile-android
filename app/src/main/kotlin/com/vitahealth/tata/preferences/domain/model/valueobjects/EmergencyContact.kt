package com.vitahealth.tata.preferences.domain.model.valueobjects

/** Recipient configured by the user; this does not establish a care relationship. */
data class EmergencyContact(val name: String, val phone: String) {
    init {
        require(name.isNotBlank() && name.length <= 80)
        require(phone.matches(Regex("\\+?[0-9]{7,15}")))
    }

    companion object {
        fun fromInput(name: String, phone: String) =
            EmergencyContact(name.trim(), phone.trim().replace(Regex("[\\s()\\-]"), ""))
    }
}
