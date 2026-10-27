package com.vitahealth.tata.identity.application.commands

import com.vitahealth.tata.identity.application.ports.SessionAccessRepository
import com.vitahealth.tata.identity.application.readmodels.SessionSubject
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/** The application layer never treats a blank or oversized bearer as login evidence. */
class SignInSocialCommandHandlerTest {
    @Test
    fun rejectsMalformedTokensBeforeTouchingTheRepository() = runBlocking {
        var called = false
        val repo = java.lang.reflect.Proxy.newProxyInstance(
            SessionAccessRepository::class.java.classLoader,
            arrayOf(SessionAccessRepository::class.java),
        ) { _, method, _ ->
            if (method.name == "signInSocial") called = true
            error("Unexpected repository operation")
        } as SessionAccessRepository
        val handler = SignInSocialCommandHandler(repo)
        assertEquals("INVALID_SOCIAL_CREDENTIAL", (handler("") as AppResult.Failure).code)
        assertEquals("INVALID_SOCIAL_CREDENTIAL", (handler("x".repeat(16385)) as AppResult.Failure).code)
        assertFalse(called)
    }
}
