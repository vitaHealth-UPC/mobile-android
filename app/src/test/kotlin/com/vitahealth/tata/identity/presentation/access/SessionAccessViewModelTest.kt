package com.vitahealth.tata.identity.presentation.access

import com.vitahealth.tata.identity.application.FakeSessionAccessRepository
import com.vitahealth.tata.identity.application.SessionSubject
import com.vitahealth.tata.identity.application.failure
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = SessionAccessViewModel(repository)

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
}
