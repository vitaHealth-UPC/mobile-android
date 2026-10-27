package com.vitahealth.tata.app.integration.linkedperson

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.app.integration.consentwithdrawal.ConsentWithdrawalIntegration
import com.vitahealth.tata.carelink.infrastructure.di.CareLinkDependencies
import com.vitahealth.tata.carelink.infrastructure.remote.*
import com.vitahealth.tata.carelink.presentation.consent.ConsentWithdrawalAction
import com.vitahealth.tata.carelink.presentation.consent.ConsentWithdrawalViewModel
import com.vitahealth.tata.identity.infrastructure.remote.RemoteSessionAccessRepository
import com.vitahealth.tata.identity.infrastructure.remote.SessionApiService
import com.vitahealth.tata.monitoring.infrastructure.di.MonitoringDependencies
import com.vitahealth.tata.monitoring.infrastructure.remote.FamilyMonitoringApiService
import com.vitahealth.tata.shared.application.session.*
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import com.vitahealth.tata.treatment.infrastructure.di.TreatmentDependencies
import java.io.File
import java.net.URI
import java.time.Instant
import java.util.Properties
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

/**
 * Opt-in Compose-to-Spring check. Uses an isolated store, never the app's encrypted credentials.
 */
class LinkedPersonLiveBackendTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun profileContactAndWithdrawalUseLiveBackendFromCompose() {
        val fixturePath = InstrumentationRegistry.getArguments().getString("tata.liveContext")
        assumeTrue("Explicit local backend fixture required", fixturePath != null)
        val file = File(requireNotNull(fixturePath))
        val context = Properties().apply { file.inputStream().use { load(it) } }
        val baseUrl = context.getProperty("baseUrl")
        require(URI(baseUrl).host == "127.0.0.1")
        val adult = context.getProperty("adultId")
        val caregiver = context.getProperty("caregiverId")
        val store =
            IsolatedStore(
                StoredSession(
                    context.getProperty("caregiverToken"),
                    Instant.now().plusSeconds(300),
                    SessionOwner(caregiver, "CAREGIVER"),
                )
            )
        val retrofit = ApiClientFactory.create(baseUrl) { store.accessToken() }
        val care = CareLinkDependencies(retrofit, store, baseUrl)
        val monitoring = MonitoringDependencies(store, baseUrl)
        val treatment = TreatmentDependencies(retrofit)
        val factory =
            LinkedPersonViewModel.Factory(
                caregiver,
                adult,
                "Rosa",
                LinkedPersonIntegration(
                    monitoring.linkedPersonSummaryReader(),
                    monitoring.linkedPersonProfileReader(),
                    treatment.linkedPersonTreatmentsReader(),
                    care.linkedPersonLinksReader(caregiver),
                ),
                monitoring.linkedPersonContactReader(),
                care.linkedPersonProfileUpdater(caregiver),
                care.linkedPersonEmergencyContactUpdater(caregiver),
            )
        val identity =
            RemoteSessionAccessRepository(retrofit.create(SessionApiService::class.java), store) {
                captured ->
                ApiClientFactory.create(baseUrl) { captured.accessToken }
                    .create(SessionApiService::class.java)
            }
        // Transport cleanup/FCM is a separate gate; this test exercises real Identity logout.
        val withdrawal =
            ConsentWithdrawalViewModel.Factory(
                adult,
                ConsentWithdrawalIntegration(
                    store,
                    AuthorizedConsentWithdrawal(store, baseUrl),
                    {},
                    { identity.signOut() },
                ),
            )
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val spanish =
            base.createConfigurationContext(
                android.content.res.Configuration(base.resources.configuration).apply {
                    setLocales(android.os.LocaleList.forLanguageTags("es-419"))
                }
            )
        var adultMode by mutableStateOf(false)
        var exited by mutableStateOf(false)
        var exits = 0
        try {
            compose.setContent {
                CompositionLocalProvider(
                    LocalContext provides spanish,
                    LocalConfiguration provides spanish.resources.configuration,
                ) {
                    TataTheme {
                        when {
                            exited -> Text("Acceso cerrado")
                            adultMode ->
                                ConsentWithdrawalAction(withdrawal) {
                                    exits++
                                    exited = true
                                }
                            else -> LinkedPersonRoute(factory, {}, {}, {}, {}, {})
                        }
                    }
                }
            }
            compose.waitUntil(30_000) {
                compose.onAllNodesWithText("Rosa").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText("Editar").performClick()
            compose.onNodeWithText("Editar contacto de emergencia").performClick()
            compose.onNodeWithText("Cancelar").performClick()
            val profile = retrofit.create(CareLinkApiService::class.java)
            val before = runBlocking { requireNotNull(profile.getOlderAdult(adult).body()) }
            assertNull(before.emergencyContactName)
            compose.onNodeWithText("Editar").performClick()
            compose.onNodeWithText("Editar contacto de emergencia").performClick()
            compose.onNodeWithTag("linked-contact-name").performTextReplacement("Ana UI")
            compose.onNodeWithTag("linked-contact-relationship").performTextReplacement("Hija")
            compose.onNodeWithTag("linked-contact-phone").performTextReplacement("+51912345678")
            compose.onNodeWithText("Guardar").performClick()
            compose.waitUntil(30_000) {
                compose.onAllNodesWithTag("linked-contact-name").fetchSemanticsNodes().isEmpty()
            }
            val saved = runBlocking { requireNotNull(profile.getOlderAdult(adult).body()) }
            assertEquals("Ana UI", saved.emergencyContactName)
            assertEquals("Hija", saved.emergencyContactRelationship)
            assertEquals("+51912345678", saved.emergencyContactPhone)
            assertEquals(before.fullName, saved.fullName)
            assertEquals(before.birthDate, saved.birthDate)
            compose.runOnIdle {
                store.value =
                    StoredSession(
                        context.getProperty("adultToken"),
                        Instant.now().plusSeconds(300),
                        SessionOwner(adult, "OLDER_ADULT"),
                    )
                adultMode = true
            }
            compose.onNodeWithText("Retirar consentimiento").performClick()
            compose.onNodeWithText("Cancelar").performClick()
            val adultApi =
                ApiClientFactory.create(baseUrl) { context.getProperty("adultToken") }
                    .create(ConsentWithdrawalApi::class.java)
            assertEquals(200, runBlocking { adultApi.current().code() })
            compose.onNodeWithText("Retirar consentimiento").performClick()
            compose.onNodeWithText("Retirar").performClick()
            compose.waitUntil(30_000) {
                compose.onAllNodesWithText("Acceso cerrado").fetchSemanticsNodes().isNotEmpty()
            }
            compose.runOnIdle {
                assertEquals(1, exits)
                assertNull(store.session())
            }
            assertEquals(401, runBlocking { adultApi.current().code() })
            val caregiverApi =
                ApiClientFactory.create(baseUrl) { context.getProperty("caregiverToken") }
                    .create(FamilyMonitoringApiService::class.java)
            assertEquals(403, runBlocking { caregiverApi.status(adult, caregiver).code() })
        } finally {
            file.delete()
        }
    }

    private class IsolatedStore(@Volatile var value: StoredSession?) : SessionStore {
        override fun session() = value

        override fun accessToken() = value?.accessToken

        override fun save(accessToken: String, expiresAt: String) =
            error("Unexpected credential replacement")

        override fun save(accessToken: String, expiresAt: String, owner: SessionOwner) =
            error("Unexpected credential replacement")

        override fun clear() {
            value = null
        }
    }
}
