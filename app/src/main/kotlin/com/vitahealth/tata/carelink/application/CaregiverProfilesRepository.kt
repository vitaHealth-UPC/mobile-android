package com.vitahealth.tata.carelink.application
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.LocalDate

data class LinkedAdult(val id: String,val name: String)
data class NewAdultProfile(val name: String,val birthDate: LocalDate,val contactName: String,val relationship: String,val phone: String)
data class ProfileLinkingCode(val olderAdultId: String,val olderAdultName: String,val code: String,val expiresAt: String)
interface CaregiverProfilesRepository {
    suspend fun list(caregiverId: String): AppResult<List<LinkedAdult>>
    suspend fun register(caregiverId: String, profile: NewAdultProfile): AppResult<LinkedAdult>
    suspend fun linkingCode(caregiverId: String,adult: LinkedAdult): AppResult<ProfileLinkingCode>
}
