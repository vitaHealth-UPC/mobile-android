package com.vitahealth.tata.carelink.presentation.profiles.viewmodel

import com.vitahealth.tata.carelink.application.readmodels.LinkedAdult
import com.vitahealth.tata.carelink.application.readmodels.ProfileLinkingCode
import java.time.LocalDate

/**
 * Profile list and registration draft, retaining a created identity for code-generation retries.
 */
data class CaregiverProfilesState(
    val adults: List<LinkedAdult> = emptyList(),
    val name: String = "",
    val birthDate: LocalDate? = null,
    val contactName: String = "",
    val relationship: String = "",
    val phone: String = "",
    val showForm: Boolean = false,
    val busy: Boolean = false,
    val error: String? = null,
    val createdAdult: LinkedAdult? = null,
    val code: ProfileLinkingCode? = null,
) {
    /** Invalid credentials or ownership require leaving this account-scoped screen. */
    val requiresAccountRecovery: Boolean
        get() =
            error in setOf("AUTHENTICATION_REQUIRED", "RESOURCE_ACCESS_DENIED", "SESSION_CHANGED")
}
