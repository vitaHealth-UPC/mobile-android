package com.vitahealth.tata.identity.application.commands

import com.vitahealth.tata.identity.application.FakeSessionAccessRepository
import com.vitahealth.tata.identity.application.readmodels.SessionSubject
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
/** Verifies that session use cases delegate without coupling to UI or transport adapters. */
class SessionAccessCommandHandlersTest {

    private val repository = FakeSessionAccessRepository()

    @Test
    fun signInAndPinAuthenticationPreserveTheRequestedCredentials() = runTest {
        val signIn = SignInCommandHandler(repository)
        val signInWithPin = SignInWithPinCommandHandler(repository)

        val emailResult = signIn(SignInCommand("ana@example.com", "password123"))
        val pinResult = signInWithPin(SignInWithPinCommand("adult-1", "1234"))

        assertTrue(emailResult is AppResult.Success<SessionSubject>)
        assertTrue(pinResult is AppResult.Success<SessionSubject>)
        assertEquals(listOf("ana@example.com" to "password123"), repository.signIns)
        assertEquals(listOf("adult-1" to "1234"), repository.pinSignIns)
    }

    @Test
    fun pinRegistrationAndRestorationUseDistinctCommands() = runTest {
        val register = RegisterPinCommandHandler(repository)
        val restore = RestoreSessionCommandHandler(repository)

        val registered = register(RegisterPinCommand("adult-1", "9876"))
        val restored = restore()

        assertTrue(registered is AppResult.Success<Unit>)
        assertTrue(restored is AppResult.Success<SessionSubject>)
        assertEquals(listOf("adult-1" to "9876"), repository.pinRegistrations)
        assertEquals(1, repository.currentCalls)
    }

    @Test
    fun signOutIsAnExplicitMutation() = runTest {
        val signOut = SignOutCommandHandler(repository)

        assertTrue(signOut() is AppResult.Success<Unit>)
        assertEquals(1, repository.signOuts)
    }

    @Test
    fun credentialCommandsDoNotPrintSecrets() {
        val password = SignInCommand("ana@example.com", "very-sensitive-password")
        val pin = SignInWithPinCommand("adult-1", "4537")
        val setup = RegisterPinCommand("adult-1", "9876")

        assertTrue(!password.toString().contains("very-sensitive-password"))
        assertTrue(!pin.toString().contains("4537"))
        assertTrue(!setup.toString().contains("9876"))
    }

    @Test
    fun invalidCredentialsAreRejectedBeforeContactingThePort() = runTest {
        val signIn = SignInCommandHandler(repository)
        val register = RegisterPinCommandHandler(repository)

        val loginResult = signIn(SignInCommand(" ", " "))
        val pinResult = register(RegisterPinCommand("", "bad"))

        assertTrue(loginResult is AppResult.Failure)
        assertTrue(pinResult is AppResult.Failure)
        assertTrue(repository.signIns.isEmpty())
        assertTrue(repository.pinRegistrations.isEmpty())
    }

}
