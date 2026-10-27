package com.vitahealth.tata.identity.presentation.recovery

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.identity.application.ports.PasswordRecovery
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class PasswordRecoveryDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    @Test fun validatesCodeRetryAndReturnsToAccessOnlyAfterReceipt() {
        var requests = 0; var completions = 0; var closes = 0
        val vm = PasswordRecoveryViewModel(object : PasswordRecovery {
            override suspend fun request(email: String): AppResult<Unit> { requests++; assertEquals("user@example.com", email); return AppResult.Success(Unit) }
            override suspend fun complete(email: String, code: String, newPassword: String): AppResult<Unit> {
                completions++
                return if (completions == 1) AppResult.Failure("Invalid", code = "INVALID_RECOVERY_CODE") else AppResult.Success(Unit)
            }
        })
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val localized = base.createConfigurationContext(android.content.res.Configuration(base.resources.configuration).apply { setLocale(java.util.Locale.forLanguageTag("es-419")) })
        compose.setContent { CompositionLocalProvider(LocalContext provides localized, LocalConfiguration provides localized.resources.configuration) {
            TataTheme { PasswordRecoveryDialog(vm) { closes++ } }
        } }
        compose.onNodeWithTag("recovery-email").performTextReplacement("user@example.com")
        compose.onNodeWithText("Enviar código").performClick()
        compose.onNodeWithTag("recovery-code").performTextReplacement("012345")
        compose.onNodeWithTag("recovery-password").performTextReplacement("new-password")
        compose.onNodeWithText("Guardar contraseña").performClick()
        compose.onNodeWithText("El código no es válido o ya no está disponible.").assertIsDisplayed()
        compose.runOnIdle { assertEquals("", vm.state.value.password); assertEquals(0, closes) }
        compose.onNodeWithTag("recovery-password").performTextReplacement("new-password")
        compose.onNodeWithText("Guardar contraseña").performClick()
        compose.onNodeWithText("Contraseña actualizada. Ingresa con tu nueva contraseña.").assertIsDisplayed()
        compose.onNodeWithText("Volver al acceso").performClick()
        compose.runOnIdle { assertEquals(1, requests); assertEquals(2, completions); assertEquals(1, closes); assertEquals("", vm.state.value.password) }
    }
}
