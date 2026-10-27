package com.vitahealth.tata.carelink.application.ports

import com.vitahealth.tata.carelink.application.inputs.NewAdultProfile
import com.vitahealth.tata.carelink.application.readmodels.LinkedAdult
import com.vitahealth.tata.carelink.application.readmodels.ProfileLinkingCode
import com.vitahealth.tata.shared.common.result.AppResult

/** Provides the caregiver's adult profiles and their temporary linking codes. */
interface CaregiverProfilesRepository {
    suspend fun list(caregiverId: String): AppResult<List<LinkedAdult>>

    suspend fun register(caregiverId: String, profile: NewAdultProfile): AppResult<LinkedAdult>

    suspend fun linkingCode(caregiverId: String, adult: LinkedAdult): AppResult<ProfileLinkingCode>
}
