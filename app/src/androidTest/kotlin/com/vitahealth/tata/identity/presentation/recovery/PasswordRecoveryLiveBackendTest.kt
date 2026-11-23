package com.vitahealth.tata.identity.presentation.recovery

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.identity.infrastructure.remote.*
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import java.io.File
import java.net.URI
import java.util.Properties
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

/** Opt-in isolated Spring/H2 integration. No external mail or team accounts. */
class PasswordRecoveryLiveBackendTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun resetFromComposeRevokesOldSessionAndChangesPassword() {
        val path = InstrumentationRegistry.getArguments().getString("tata.liveContext")
        assumeTrue("Explicit isolated fixture required", path != null)
        val file = File(requireNotNull(path))
        try {
            val fixture = Properties().apply { file.inputStream().use { load(it) } }
            val baseUrl = fixture.getProperty("baseUrl")
            require(URI(baseUrl).host == "127.0.0.1")
            val email = requireNotNull(fixture.getProperty("recoveryEmail"))
            val code = requireNotNull(fixture.getProperty("recoveryCode"))
            val anonymous = ApiClientFactory.create(baseUrl) { null }
            val sessions = anonymous.create(SessionApiService::class.java)
            val old =
                ApiClientFactory.create(baseUrl) { fixture.getProperty("caregiverToken") }
                    .create(SessionApiService::class.java)
            runBlocking { assertEquals(200, old.current().code()) }
            val vm =
                PasswordRecoveryViewModel(
                    RemotePasswordRecovery(anonymous.create(PasswordRecoveryApi::class.java))
                )
            var closes = 0
            val base = InstrumentationRegistry.getInstrumentation().targetContext
            val spanish =
                base.createConfigurationContext(
                    android.content.res.Configuration(base.resources.configuration).apply {
                        setLocale(java.util.Locale.forLanguageTag("es-419"))
                    }
                )
            compose.setContent {
                CompositionLocalProvider(
                    LocalContext provides spanish,
                    LocalConfiguration provides spanish.resources.configuration,
                ) {
                    TataTheme { PasswordRecoveryDialog(vm) { closes++ } }
                }
            }
            compose.onNodeWithTag("recovery-email").performTextReplacement(email)
            compose.onNodeWithText("Enviar código").performClick()
            compose.waitUntil(15_000) {
                compose.onAllNodesWithTag("recovery-code").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("recovery-code").performTextReplacement(code)
            compose.onNodeWithTag("recovery-password").performTextReplacement("new-probe-password")
            compose.onNodeWithText("Guardar contraseña").performClick()
            compose.waitUntil(15_000) {
                compose.onAllNodesWithText("Volver al acceso").fetchSemanticsNodes().isNotEmpty()
            }
            compose.runOnIdle {
                assertEquals(0, closes)
                assertEquals("", vm.state.value.password)
            }
            runBlocking {
                assertEquals(401, old.current().code())
                assertEquals(
                    401,
                    sessions.signIn(SignInRequest(email, "old-probe-password")).code(),
                )
                assertEquals(
                    200,
                    sessions.signIn(SignInRequest(email, "new-probe-password")).code(),
                )
                assertEquals(
                    400,
                    anonymous
                        .create(PasswordRecoveryApi::class.java)
                        .complete(CompletePasswordRecoveryBody(email, code, "another-password"))
                        .code(),
                )
            }
            compose.onNodeWithText("Volver al acceso").performClick()
            compose.runOnIdle { assertEquals(1, closes) }
        } finally {
            file.delete()
        }
    }
}
