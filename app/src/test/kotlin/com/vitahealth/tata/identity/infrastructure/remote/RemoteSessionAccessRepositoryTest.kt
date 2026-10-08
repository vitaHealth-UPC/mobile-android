package com.vitahealth.tata.identity.infrastructure.remote

import com.vitahealth.tata.shared.application.SessionStore
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class RemoteSessionAccessRepositoryTest {
    private class Store: SessionStore {
        var token: String? = null
        override fun accessToken() = token
        override fun save(accessToken: String, expiresAt: String) { token=accessToken }
        override fun clear() { token=null }
    }
    private fun api(call: (String,Array<out Any>)->Any?): SessionApiService = java.lang.reflect.Proxy.newProxyInstance(SessionApiService::class.java.classLoader,arrayOf(SessionApiService::class.java)) { _,method,args -> call(method.name,args ?: emptyArray()) } as SessionApiService
    @Test fun `password access saves token before returning caregiver identity`() = runBlocking {
        val store=Store()
        val remote=RemoteSessionAccessRepository(api { _,args -> assertEquals("care@example.com",(args[0] as SignInRequest).email); Response.success(SessionAccessResponse("care-1",null,"secret","2099-01-01T00:00:00Z")) },store)
        val result=remote.signIn(" care@example.com ","password") as AppResult.Success
        assertEquals("CAREGIVER",result.value.role)
        assertEquals("care-1",result.value.subjectId)
        assertEquals("secret",store.token)
    }
    @Test fun `pin access establishes older adult role`() = runBlocking {
        val store=Store()
        val remote=RemoteSessionAccessRepository(api { _,args -> assertEquals("1234",(args[0] as PinAccessRequest).pin);Response.success(SessionAccessResponse(null,"adult-1","adult-token","2099-01-01T00:00:00Z")) },store)
        val result=remote.signInWithPin("adult-1","1234") as AppResult.Success
        assertEquals("OLDER_ADULT",result.value.role)
        assertEquals("adult-token",store.token)
    }
    @Test fun `locked pin preserves existing session and server code`() = runBlocking {
        val store=Store().apply { token="existing" }
        val remote=RemoteSessionAccessRepository(api { _,_ -> Response.error<SessionAccessResponse>(403,"{\"code\":\"PIN_LOCKED\",\"message\":\"locked\"}".toResponseBody()) },store)
        val result=remote.signInWithPin("adult-1","0000") as AppResult.Failure
        assertEquals("PIN_LOCKED",result.code)
        assertEquals("existing",store.token)
    }
    @Test fun `expired session is cleared`() = runBlocking {
        val store=Store().apply { token="expired" }
        val remote=RemoteSessionAccessRepository(api { _,_ -> Response.error<CurrentSessionResponse>(401,"{}".toResponseBody()) },store)
        assertTrue(remote.current() is AppResult.Failure)
        assertNull(store.token)
    }
    @Test fun `logout clears session after successful revocation`() = runBlocking {
        val store=Store().apply { token="secret" }
        val remote=RemoteSessionAccessRepository(api { _,_ -> Response.success<Unit>(204,null) },store)
        assertTrue(remote.signOut() is AppResult.Success)
        assertNull(store.token)
    }
}
