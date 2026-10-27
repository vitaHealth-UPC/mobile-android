package com.vitahealth.tata.identity.presentation.access

import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.identity.application.FakeSessionAccessRepository
import com.vitahealth.tata.identity.application.commands.RegisterPinCommandHandler
import com.vitahealth.tata.identity.application.commands.RestoreSessionCommandHandler
import com.vitahealth.tata.identity.application.commands.SignInCommandHandler
import com.vitahealth.tata.identity.application.commands.SignInWithPinCommandHandler
import com.vitahealth.tata.identity.application.failure
import com.vitahealth.tata.identity.application.ports.SessionAccessRepository
import com.vitahealth.tata.identity.application.readmodels.SessionSubject
import com.vitahealth.tata.identity.presentation.access.ui.DemoCredentials
import com.vitahealth.tata.identity.presentation.access.viewmodel.SessionAccessViewModel
import com.vitahealth.tata.shared.common.result.AppResult
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionAccessViewModelTest {
    private val repository = FakeSessionAccessRepository()

    @Test
    fun interactivePasswordAuthenticationFailureIsVisible() {
        repository.result = failure("AUTHENTICATION_REQUIRED")
        val model = viewModel()
        model.email("caregiver@example.com")
        model.password("password1")
        model.signIn()
        assertEquals("AUTHENTICATION_REQUIRED", model.state.value.error)
        assertFalse(model.state.value.busy)
        assertNull(model.state.value.subject)
        assertEquals(1, repository.signIns.size)
    }

    @Test
    fun interactivePinAuthenticationFailureIsVisible() {
        repository.result = failure("AUTHENTICATION_REQUIRED")
        val model = viewModel()
        model.type("1234")
        model.pin("adult", false)
        assertEquals("AUTHENTICATION_REQUIRED", model.state.value.error)
        assertFalse(model.state.value.busy)
        assertNull(model.state.value.subject)
        assertEquals(1, repository.pinSignIns.size)
    }

    @Test
    fun inFlightLoginKeepsItsDraftAndLateSuccessCannotPublishAfterCancellation() {
        lateinit var pending: Continuation<AppResult<SessionSubject>>
        var calls = 0
        val source =
            object : SessionAccessRepository by repository {
                override suspend fun signIn(
                    email: String,
                    password: String,
                ): AppResult<SessionSubject> {
                    calls++
                    return suspendCoroutine { pending = it }
                }
            }
        val model = viewModel(source)
        model.email("original@example.com")
        model.password("original-password")
        model.signIn()
        model.email("")
        model.password("")
        model.signIn()
        assertEquals(1, calls)
        assertEquals("original@example.com", model.state.value.email)
        assertEquals("original-password", model.state.value.password)
        assertNull(model.state.value.error)
        val before = model.state.value
        model.viewModelScope.cancel()
        pending.resume(AppResult.Success(SessionSubject("adult", "OLDER_ADULT")))
        assertEquals(before, model.state.value)
        assertNull(model.state.value.subject)
    }

    @Test
    fun latePinRegistrationCannotPublishSavedStateAfterCancellation() {
        lateinit var pending: Continuation<AppResult<Unit>>
        val source =
            object : SessionAccessRepository by repository {
                override suspend fun registerPin(
                    olderAdultId: String,
                    pin: String,
                ): AppResult<Unit> = suspendCoroutine { pending = it }
            }
        val model = viewModel(source)
        model.type("1234")
        model.pin("adult", true)
        model.viewModelScope.cancel()
        pending.resume(AppResult.Success(Unit))
        assertFalse(model.state.value.pinSaved)
        assertEquals("1234", model.state.value.pin)
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(source: SessionAccessRepository = repository): SessionAccessViewModel =
        SessionAccessViewModel(
            SignInCommandHandler(source),
            SignInWithPinCommandHandler(source),
            RegisterPinCommandHandler(source),
            RestoreSessionCommandHandler(source),
        )

    private fun SessionAccessViewModel.type(pin: String) = pin.forEach { digit(it.toString()) }

    @Test
    fun signingInWithoutEmailOrPasswordAsksForThemWithoutCallingTheBackend() {
        val model = viewModel()
        model.email("diego@example.com")

        model.signIn()

        assertEquals("REQUIRED_FIELDS", model.state.value.error)
        assertTrue(repository.signIns.isEmpty())
    }

    @Test
    fun aSuccessfulSignInExposesTheSubjectAndClearsThePassword() {
        val model = viewModel()
        model.email("diego@example.com")
        model.password("password1")

        model.signIn()

        assertEquals(SessionSubject("caregiver-1", "CAREGIVER"), model.state.value.subject)
        assertEquals("", model.state.value.password)
        assertFalse(model.state.value.busy)
        assertEquals("diego@example.com" to "password1", repository.signIns.single())
    }

    @Test
    fun wrongCredentialsShowTheCodeAndKeepTheUserOnTheForm() {
        repository.result = failure("INVALID_CREDENTIALS")
        val model = viewModel()
        model.email("diego@example.com")
        model.password("wrong")

        model.signIn()

        assertEquals("INVALID_CREDENTIALS", model.state.value.error)
        assertNull(model.state.value.subject)
    }

    @Test
    fun typingClearsThePreviousError() {
        repository.result = failure("INVALID_CREDENTIALS")
        val model = viewModel()
        model.email("a@b.com")
        model.password("x")
        model.signIn()

        model.email("a@b.co")

        assertNull(model.state.value.error)
    }

    @Test
    fun useDemoAccountPrefillsSeededCaregiverCredentials() {
        val model = viewModel()

        model.useDemoAccount()

        assertEquals(DemoCredentials.EMAIL, model.state.value.email)
        assertEquals(DemoCredentials.PASSWORD, model.state.value.password)
        assertNull(model.state.value.error)
    }

    @Test
    fun restoringWithoutASessionStaysSilent() {
        repository.result = failure("AUTHENTICATION_REQUIRED")
        val model = viewModel()

        model.restore()

        assertNull(model.state.value.error)
        assertNull(model.state.value.subject)
        assertEquals(1, repository.currentCalls)
    }

    @Test
    fun restoringAStoredSessionSignsInWithoutTyping() {
        val model = viewModel()

        model.restore()

        assertEquals("caregiver-1", model.state.value.subject?.subjectId)
    }

    @Test
    fun thePinTakesAtMostFourDigitsAndBackspaceRemovesOne() {
        val model = viewModel()

        model.type("12345")
        assertEquals("1234", model.state.value.pin)

        model.digit("⌫")
        assertEquals("123", model.state.value.pin)
    }

    @Test
    fun anIncompletePinIsRefusedWithoutCallingTheBackend() {
        val model = viewModel()
        model.type("12")

        model.pin("adult-1", setup = false)

        assertEquals("INVALID_PIN", model.state.value.error)
        assertTrue(repository.pinSignIns.isEmpty())
    }

    @Test
    fun aCorrectPinSignsTheOlderAdultIn() {
        repository.result = AppResult.Success(SessionSubject("adult-1", "OLDER_ADULT"))
        val model = viewModel()
        model.type("1234")

        model.pin("adult-1", setup = false)

        assertEquals("OLDER_ADULT", model.state.value.subject?.role)
        assertEquals("", model.state.value.pin)
        assertEquals("adult-1" to "1234", repository.pinSignIns.single())
    }

    @Test
    fun aLockedPinReportsTheLockAndKeepsTheDigitsForDisplay() {
        repository.result = failure("PIN_LOCKED")
        val model = viewModel()
        model.type("0000")

        model.pin("adult-1", setup = false)

        assertEquals("PIN_LOCKED", model.state.value.error)
        assertEquals("0000", model.state.value.pin)
    }

    @Test
    fun anUnexpectedFailureClearsThePin() {
        repository.result = failure("REQUEST_FAILED")
        val model = viewModel()
        model.type("1234")

        model.pin("adult-1", setup = false)

        assertEquals("", model.state.value.pin)
        assertEquals("REQUEST_FAILED", model.state.value.error)
    }

    @Test
    fun settingUpAPinSavesItAndFlagsTheCompletion() {
        val model = viewModel()
        model.type("4321")

        model.pin("adult-1", setup = true)

        assertTrue(model.state.value.pinSaved)
        assertEquals("", model.state.value.pin)
        assertEquals("adult-1" to "4321", repository.pinRegistrations.single())
        assertTrue(repository.pinSignIns.isEmpty())
    }

    @Test
    fun aPinSetupThatFailsShowsTheCodeAndClearsTheDigits() {
        repository.registerPinResult = failure("PIN_ALREADY_REGISTERED")
        val model = viewModel()
        model.type("4321")

        model.pin("adult-1", setup = true)

        assertFalse(model.state.value.pinSaved)
        assertEquals("PIN_ALREADY_REGISTERED", model.state.value.error)
        assertEquals("", model.state.value.pin)
    }

    @Test
    fun keypadRejectsTextMultipleDigitsAndNonAsciiNumbers() {
        val model = viewModel()
        for (input in listOf("", "12", "a", "١", " ")) model.digit(input)
        assertEquals("", model.state.value.pin)
        model.digit("7")
        assertEquals("7", model.state.value.pin)
    }
}
