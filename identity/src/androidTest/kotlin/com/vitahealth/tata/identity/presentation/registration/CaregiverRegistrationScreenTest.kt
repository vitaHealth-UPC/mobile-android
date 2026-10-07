package com.vitahealth.tata.identity.presentation.registration

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.shared.design.theme.TataTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals

@RunWith(AndroidJUnit4::class)
class CaregiverRegistrationScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun editableCodeVerificationAndExpiredRecoveryPreserveTheFlow() {
        var state by mutableStateOf(CaregiverRegistrationUiState(name = "Diego Mendoza", email = "diego@example.test", password = "example-password"))
        var verified = 0
        var requested = 0
        compose.setContent {
            TataTheme {
                CaregiverRegistrationScreen(state,
                    onNameChange = { state = state.copy(name = it) },
                    onEmailChange = { state = state.copy(email = it) },
                    onPasswordChange = { state = state.copy(password = it) },
                    onVerificationCodeChange = { state = state.copy(verificationCode = it) },
                    onCreateAccount = { state = state.copy(step = RegistrationStep.Verification) },
                    onVerifyEmail = { verified++ },
                    onRequestNewVerification = { requested++; state = state.copy(step = RegistrationStep.Verification, verificationCode = "") },
                    modifier = Modifier.safeDrawingPadding())
            }
        }
        compose.onNodeWithText("Verificar correo").assertIsNotEnabled()
        compose.onNodeWithText("Crear cuenta").performClick()
        compose.onNodeWithText("Crear cuenta").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Código de verificación").performTextInput("4821967")
        compose.runOnIdle { assertEquals("482196", state.verificationCode) }
        compose.onNodeWithText("Verificar correo").assertIsEnabled()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        android.os.ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand("input keyevent 111")).use { it.readBytes() }
        compose.waitForIdle()
        android.os.ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand("screencap -p /data/local/tmp/registration-native.png")).use { it.readBytes() }
        compose.onNodeWithText("Verificar correo").performClick()
        compose.runOnIdle { assertEquals(1, verified); state = state.copy(step = RegistrationStep.VerificationExpired) }
        compose.onNodeWithText("Verificación vencida").assertExists()
        compose.onNodeWithText("Solicitar nuevo código").performClick()
        compose.runOnIdle { assertEquals(1, requested); assertEquals("", state.verificationCode) }
        compose.onNodeWithText("Verificar correo").assertIsNotEnabled()
        compose.runOnIdle { state = state.copy(step = RegistrationStep.Account, errorCode = "DUPLICATE_EMAIL", errorMessage = "Duplicate") }
        compose.onNodeWithText("Correo ya registrado").assertExists()
    }
}
