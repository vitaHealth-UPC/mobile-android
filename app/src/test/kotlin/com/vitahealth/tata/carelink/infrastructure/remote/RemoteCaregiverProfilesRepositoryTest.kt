package com.vitahealth.tata.carelink.infrastructure.remote
import com.vitahealth.tata.carelink.application.*
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response
import java.time.LocalDate

class RemoteCaregiverProfilesRepositoryTest {
    private fun api(call: (String,Array<out Any>)->Any?): CaregiverProfilesApiService = java.lang.reflect.Proxy.newProxyInstance(CaregiverProfilesApiService::class.java.classLoader,arrayOf(CaregiverProfilesApiService::class.java)){_,method,args->call(method.name,args ?: emptyArray())} as CaregiverProfilesApiService
    @Test fun `confirmed links map to adult identities rather than link ids`() = runBlocking {
        val repository=RemoteCaregiverProfilesRepository(api{_,args->assertEquals("care-1",args[0]);Response.success(listOf(ConfirmedLinkResponse("link-1","adult-1","Rosa")))})
        val result=repository.list("care-1") as AppResult.Success
        assertEquals(LinkedAdult("adult-1","Rosa"),result.value.single())
    }
    @Test fun `profile registration sends date and optional emergency contact`() = runBlocking {
        val repository=RemoteCaregiverProfilesRepository(api{_,args->val body=args[0] as RegisterAdultRequest;assertEquals("1958-05-12",body.birthDate);assertNull(body.emergencyContactPhone);Response.success(OlderAdultProfileResponse("adult-1","care-1",body.fullName,body.birthDate,null,null,null,null))})
        val result=repository.register("care-1",NewAdultProfile(" Rosa ",LocalDate.of(1958,5,12),"","","")) as AppResult.Success
        assertEquals("Rosa",result.value.name)
    }
    @Test fun `linking code preserves identity expiration and code for consent handoff`() = runBlocking {
        val repository=RemoteCaregiverProfilesRepository(api{_,args->assertEquals("adult-1",(args[0] as GenerateCodeRequest).olderAdultId);Response.success(CareLinkResponse("link-1","care-1","adult-1","PENDING","ABC123","2099-01-01T00:00:00Z",null,false,null,null))})
        val result=repository.linkingCode("care-1",LinkedAdult("adult-1","Rosa")) as AppResult.Success
        assertEquals("ABC123",result.value.code)
        assertEquals("adult-1",result.value.olderAdultId)
    }
}
