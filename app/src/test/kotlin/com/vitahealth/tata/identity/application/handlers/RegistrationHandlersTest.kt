package com.vitahealth.tata.identity.application.handlers

import com.vitahealth.tata.identity.application.FakeIdentityRepository
import com.vitahealth.tata.identity.application.activeAccount
import com.vitahealth.tata.identity.application.commands.RegisterCaregiverCommand
import com.vitahealth.tata.identity.application.commands.RequestNewVerificationCommand
import com.vitahealth.tata.identity.application.commands.VerifyCaregiverEmailCommand
import com.vitahealth.tata.identity.application.pendingAccount
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrationHandlersTest {
    private val repository = FakeIdentityRepository()
    private val register = RegisterCaregiverCommandHandler(repository)
    private val verify = VerifyCaregiverEmailCommandHandler(repository)
    private val requestNewCode = RequestNewVerificationCommandHandler(repository)

    @Test
    fun aBlankNameIsRejectedWithItsOwnCode() = runBlocking {
        val result = register(RegisterCaregiverCommand(" ", "diego@example.com", "password1"))

        assertEquals("NAME_REQUIRED", (result as AppResult.Failure).code)
        assertTrue(repository.registrations.isEmpty())
    }

    @Test
    fun anEmailWithoutAtIsRejected() = runBlocking {
        val result = register(RegisterCaregiverCommand("Diego", "diego.example.com", "password1"))

        assertEquals("INVALID_EMAIL", (result as AppResult.Failure).code)
        assertTrue(repository.registrations.isEmpty())
    }

    @Test
    fun aPasswordUnderEightCharactersIsRejected() = runBlocking {
        val result = register(RegisterCaregiverCommand("Diego", "diego@example.com", "1234567"))

        assertEquals("WEAK_PASSWORD", (result as AppResult.Failure).code)
        assertTrue(repository.registrations.isEmpty())
    }

    @Test
    fun aPasswordOfEightCharactersIsAccepted() = runBlocking {
        val result = register(RegisterCaregiverCommand("Diego", "diego@example.com", "12345678"))

        assertTrue(result is AppResult.Success)
    }

    @Test
    fun theNameAndEmailAreTrimmedButThePasswordIsNotChanged() = runBlocking {
        register(RegisterCaregiverCommand("  Diego Vargas ", " diego@example.com ", " pass word "))

        assertEquals(Triple("Diego Vargas", "diego@example.com", " pass word "), repository.registrations.single())
    }

    @Test
    fun theBackendAnswerIsReturnedUntouched() = runBlocking {
        val result = register(RegisterCaregiverCommand("Diego", "diego@example.com", "password1"))

        assertEquals(pendingAccount, (result as AppResult.Success).value)
    }

    @Test
    fun aVerificationCodeThatIsNotSixDigitsIsRejectedBeforeCallingTheBackend() = runBlocking {
        listOf("", "12345", "1234567", "12a456", "      ").forEach { code ->
            val result = verify(VerifyCaregiverEmailCommand("diego@example.com", code))

            assertEquals("INVALID_VERIFICATION", (result as AppResult.Failure).code)
        }
        assertTrue(repository.verifications.isEmpty())
    }

    @Test
    fun aSixDigitCodeIsSentWithTheTrimmedEmail() = runBlocking {
        repository.result = AppResult.Success(activeAccount)

        val result = verify(VerifyCaregiverEmailCommand(" diego@example.com ", "123456"))

        assertEquals(activeAccount, (result as AppResult.Success).value)
        assertEquals("diego@example.com" to "123456", repository.verifications.single())
    }

    @Test
    fun askingForANewCodeWithoutEmailIsRejected() = runBlocking {
        val result = requestNewCode(RequestNewVerificationCommand("  "))

        assertEquals("VALIDATION_ERROR", (result as AppResult.Failure).code)
        assertTrue(repository.newCodeRequests.isEmpty())
    }

    @Test
    fun askingForANewCodeSendsTheTrimmedEmail() = runBlocking {
        requestNewCode(RequestNewVerificationCommand(" diego@example.com "))

        assertEquals(listOf("diego@example.com"), repository.newCodeRequests)
    }
}
