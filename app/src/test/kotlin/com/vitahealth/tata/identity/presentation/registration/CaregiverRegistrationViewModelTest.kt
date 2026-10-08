package com.vitahealth.tata.identity.presentation.registration

import com.vitahealth.tata.identity.application.FakeIdentityRepository
import com.vitahealth.tata.identity.application.activeAccount
import com.vitahealth.tata.identity.application.failure
import com.vitahealth.tata.identity.application.handlers.RegisterCaregiverCommandHandler
import com.vitahealth.tata.identity.application.handlers.RequestNewVerificationCommandHandler
import com.vitahealth.tata.identity.application.handlers.VerifyCaregiverEmailCommandHandler
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
class CaregiverRegistrationViewModelTest {
    private val repository = FakeIdentityRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = CaregiverRegistrationViewModel(
        registerHandler = RegisterCaregiverCommandHandler(repository),
        verifyHandler = VerifyCaregiverEmailCommandHandler(repository),
        requestNewVerificationHandler = RequestNewVerificationCommandHandler(repository),
    ).also {
        it.onNameChange("Diego")
        it.onEmailChange("diego@example.com")
        it.onPasswordChange("password1")
    }

    @Test
    fun aValidRegistrationMovesToTheVerificationStep() {
        val model = viewModel()

        model.createAccount()

        assertEquals(RegistrationStep.Verification, model.state.value.step)
        assertEquals("account-1", model.state.value.accountId)
        assertFalse(model.state.value.isLoading)
        assertNull(model.state.value.errorMessage)
    }

    @Test
    fun aDuplicatedEmailStaysOnTheFormWithAClearMessage() {
        repository.result = failure("DUPLICATE_EMAIL")
        val model = viewModel()

        model.createAccount()

        assertEquals(RegistrationStep.Account, model.state.value.step)
        assertEquals("Este correo ya está registrado.", model.state.value.errorMessage)
        assertEquals("DUPLICATE_EMAIL", model.state.value.errorCode)
    }

    @Test
    fun aShortPasswordIsExplainedInsteadOfAGenericFailure() {
        val model = viewModel()
        model.onPasswordChange("short")

        model.createAccount()

        assertEquals("La contraseña debe tener al menos 8 caracteres.", model.state.value.errorMessage)
        assertTrue(repository.registrations.isEmpty())
    }

    @Test
    fun anInvalidEmailAndAMissingNameAreExplained() {
        val model = viewModel()
        model.onEmailChange("not-an-email")
        model.createAccount()
        assertEquals("Escribe un correo válido.", model.state.value.errorMessage)

        model.onEmailChange("diego@example.com")
        model.onNameChange("")
        model.createAccount()
        assertEquals("Escribe tu nombre.", model.state.value.errorMessage)
    }

    @Test
    fun withoutConnectionTheFormExplainsIt() {
        repository.result = failure("NETWORK_UNAVAILABLE")
        val model = viewModel()

        model.createAccount()

        assertEquals("No hay conexión. Inténtalo nuevamente.", model.state.value.errorMessage)
    }

    @Test
    fun typingClearsThePreviousError() {
        repository.result = failure("DUPLICATE_EMAIL")
        val model = viewModel()
        model.createAccount()

        model.onEmailChange("other@example.com")

        assertNull(model.state.value.errorMessage)
        assertNull(model.state.value.errorCode)
    }

    @Test
    fun theVerificationCodeKeepsOnlyDigitsAndAtMostSix() {
        val model = viewModel()

        model.onVerificationCodeChange("12-34 56789")

        assertEquals("123456", model.state.value.verificationCode)
    }

    @Test
    fun aCorrectCodeCompletesTheRegistration() {
        val model = viewModel()
        model.createAccount()
        model.onVerificationCodeChange("123456")
        repository.result = AppResult.Success(activeAccount)

        model.verifyEmail()

        assertEquals(RegistrationStep.Complete, model.state.value.step)
        assertEquals("diego@example.com" to "123456", repository.verifications.single())
    }

    @Test
    fun aWrongCodeShowsAMessageAndKeepsTheVerificationStep() {
        val model = viewModel()
        model.createAccount()
        model.onVerificationCodeChange("123456")
        repository.result = failure("INVALID_VERIFICATION")

        model.verifyEmail()

        assertEquals(RegistrationStep.Verification, model.state.value.step)
        assertEquals("El código de verificación no es válido.", model.state.value.errorMessage)
    }

    @Test
    fun anIncompleteCodeIsRefusedWithoutCallingTheBackend() {
        val model = viewModel()
        model.createAccount()
        model.onVerificationCodeChange("123")

        model.verifyEmail()

        assertEquals("El código de verificación no es válido.", model.state.value.errorMessage)
        assertTrue(repository.verifications.isEmpty())
    }

    @Test
    fun anExpiredCodeMovesToTheExpiredStepWithoutAnErrorBanner() {
        val model = viewModel()
        model.createAccount()
        model.onVerificationCodeChange("123456")
        repository.result = failure("VERIFICATION_EXPIRED")

        model.verifyEmail()

        assertEquals(RegistrationStep.VerificationExpired, model.state.value.step)
        assertNull(model.state.value.errorMessage)
    }

    @Test
    fun askingForANewCodeReturnsToVerificationWithAnEmptyCode() {
        val model = viewModel()
        model.createAccount()
        model.onVerificationCodeChange("123456")
        repository.result = failure("VERIFICATION_EXPIRED")
        model.verifyEmail()
        repository.result = AppResult.Success(com.vitahealth.tata.identity.application.pendingAccount)

        model.requestNewVerification()

        assertEquals(RegistrationStep.Verification, model.state.value.step)
        assertEquals("", model.state.value.verificationCode)
        assertEquals(listOf("diego@example.com"), repository.newCodeRequests)
    }

    @Test
    fun aNewCodeCanOnlyBeRequestedFromTheExpiredStep() {
        val model = viewModel()

        model.requestNewVerification()

        assertTrue(repository.newCodeRequests.isEmpty())
    }

    @Test
    fun aMissingAccountWhenRequestingANewCodeIsExplained() {
        val model = viewModel()
        model.createAccount()
        model.onVerificationCodeChange("123456")
        repository.result = failure("VERIFICATION_EXPIRED")
        model.verifyEmail()
        repository.result = failure("ACCOUNT_NOT_FOUND")

        model.requestNewVerification()

        assertEquals("No encontramos la cuenta asociada a este correo.", model.state.value.errorMessage)
        assertEquals(RegistrationStep.VerificationExpired, model.state.value.step)
    }
}
