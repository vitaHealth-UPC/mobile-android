package com.vitahealth.tata.carelink.infrastructure.remote
import com.vitahealth.tata.carelink.application.*
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.CancellationException
import retrofit2.Response

class RemoteCaregiverProfilesRepository(private val api: CaregiverProfilesApiService): CaregiverProfilesRepository {
    override suspend fun list(caregiverId: String): AppResult<List<LinkedAdult>> = request {
        val response=api.list(caregiverId)
        val body=response.body()
        if(response.isSuccessful && body!=null) AppResult.Success(body.map{LinkedAdult(it.olderAdultId,it.olderAdultName)}) else failure(response)
    }
    override suspend fun register(caregiverId: String,profile: NewAdultProfile): AppResult<LinkedAdult> = request {
        val response=api.register(RegisterAdultRequest(caregiverId,profile.name.trim(),profile.birthDate.toString(),profile.contactName.trim().ifBlank{null},profile.relationship.trim().ifBlank{null},profile.phone.trim().ifBlank{null}))
        val body=response.body()
        if(response.isSuccessful && body!=null) AppResult.Success(LinkedAdult(body.id,body.fullName)) else failure(response)
    }
    override suspend fun linkingCode(caregiverId: String,adult: LinkedAdult): AppResult<ProfileLinkingCode> = request {
        val response=api.code(GenerateCodeRequest(caregiverId,adult.id))
        val body=response.body()
        if(response.isSuccessful && body?.linkingCode!=null && body.codeExpiresAt!=null) AppResult.Success(ProfileLinkingCode(adult.id,adult.name,body.linkingCode,body.codeExpiresAt)) else failure(response)
    }
    private fun failure(response: Response<*>) = AppResult.Failure("Profile request failed",code=when(response.code()){401->"AUTHENTICATION_REQUIRED";403->"RESOURCE_ACCESS_DENIED";400->"VALIDATION_ERROR";else->"REQUEST_FAILED"})
    private suspend fun <T> request(call: suspend ()->AppResult<T>): AppResult<T> = try{call()} catch(exception: CancellationException){throw exception} catch(exception: java.io.IOException){AppResult.Failure("Network unavailable",code="NETWORK_UNAVAILABLE")} catch(exception: RuntimeException){AppResult.Failure("Invalid response",code="INVALID_RESPONSE")}
}
