package com.vitahealth.tata.app.di

import android.content.Context
import com.vitahealth.tata.BuildConfig
import com.vitahealth.tata.analytics.infrastructure.di.AnalyticsDependencies
import com.vitahealth.tata.app.integration.consentwithdrawal.ConsentWithdrawalIntegration
import com.vitahealth.tata.app.integration.linkedperson.LinkedPersonIntegration
import com.vitahealth.tata.app.integration.linkedperson.LinkedPersonViewModel
import com.vitahealth.tata.app.integration.medicationoverview.MedicationOverviewIntegration
import com.vitahealth.tata.app.integration.pushnavigation.PushAlertResolver
import com.vitahealth.tata.app.integration.treatmentadherence.TreatmentAdherenceIntegration
import com.vitahealth.tata.carelink.infrastructure.di.CareLinkDependencies
import com.vitahealth.tata.carelink.infrastructure.remote.AuthorizedConsentWithdrawal
import com.vitahealth.tata.carelink.presentation.consent.ConsentWithdrawalViewModel
import com.vitahealth.tata.identity.application.ports.RememberedAdultProfileStore
import com.vitahealth.tata.identity.infrastructure.di.IdentityDependencies
import com.vitahealth.tata.intake.infrastructure.di.IntakeDependencies
import com.vitahealth.tata.inventory.infrastructure.di.InventoryDependencies
import com.vitahealth.tata.monitoring.infrastructure.di.MonitoringDependencies
import com.vitahealth.tata.omission.application.readmodels.PushDestination
import com.vitahealth.tata.omission.domain.model.valueobjects.OmissionPush
import com.vitahealth.tata.omission.infrastructure.di.OmissionDependencies
import com.vitahealth.tata.preferences.domain.model.valueobjects.AccessibilityPreferences
import com.vitahealth.tata.preferences.infrastructure.di.PreferencesDependencies
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory
import com.vitahealth.tata.shared.infrastructure.security.EncryptedSessionStore
import com.vitahealth.tata.treatment.infrastructure.di.TreatmentDependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit

/** Owns application-scoped adapters and composes factories for each user flow. */
class AppContainer(context: Context, private val baseUrl: String = BuildConfig.API_BASE_URL) {
    private val applicationContext = context.applicationContext
    private val sessions = EncryptedSessionStore(context)
    private val retrofit: Retrofit = ApiClientFactory.create(baseUrl, sessions::accessToken)

    private val identity = IdentityDependencies(applicationContext, retrofit, sessions, baseUrl)
    val rememberedAdultProfiles: RememberedAdultProfileStore = identity.rememberedAdultProfiles
    val passwordRecovery = identity.passwordRecovery
    val linkSocialIdentity = identity.linkSocialIdentity
    val socialCredentials =
        com.vitahealth.tata.identity.infrastructure.social.FirebaseSocialCredentialLauncher(applicationContext)
    val sessionAccessViewModelFactory = identity.sessionAccessViewModelFactory
    val caregiverRegistrationViewModelFactory = identity.caregiverRegistrationViewModelFactory
    val landingPlansViewModelFactory = identity.landingPlansViewModelFactory

    /** Ends access through the same session shared by all API adapters. */
    suspend fun signOut(): AppResult<Unit> {
        val expectedToken = sessions.accessToken()
        omission.beforeSignOut()
        if (sessions.accessToken() != expectedToken) {
            return AppResult.Failure(
                "La sesión cambió durante el cierre.",
                code = "SESSION_CHANGED",
            )
        }
        return identity.signOut()
    }

    fun consentWithdrawalFactory(adultId: String) =
        ConsentWithdrawalViewModel.Factory(
            adultId,
            ConsentWithdrawalIntegration(
                sessions,
                AuthorizedConsentWithdrawal(sessions, baseUrl),
                { omission.beforeSignOut() },
                { identity.signOut() },
            ),
        )

    /** Creates the subscription flow for the selected account. */
    fun planSubscriptionViewModelFactory(accountId: String) =
        identity.planSubscriptionViewModelFactory(accountId)

    /** Reads the device-local onboarding completion flag. */
    fun hasSeenOnboarding(): Boolean = identity.hasSeenOnboarding()

    /** Records onboarding completion through the identity composition. */
    fun completeOnboarding() = identity.completeOnboarding()

    private val careLink = CareLinkDependencies(retrofit, sessions, baseUrl)

    /** Creates the caregiver's linked-adult selector. */
    fun caregiverProfilesViewModelFactory(caregiverId: String) =
        careLink.caregiverProfilesViewModelFactory(caregiverId)

    /** Creates the care linking and consent flow. */
    fun careLinkViewModelFactory(caregiverId: String) =
        careLink.careLinkViewModelFactory(caregiverId)

    /** Treatment-owned factories consumed by the application navigation graph. */
    internal val treatment by lazy {
        TreatmentDependencies(retrofit) {
            TreatmentAdherenceIntegration(analytics.treatmentAdherenceRepository())
        }
    }

    private val monitoring = MonitoringDependencies(sessions, baseUrl)

    /** Composes the authorized profile, weekly monitoring and treatment catalog for Figma #16. */
    fun linkedPersonViewModelFactory(caregiverId: String, olderAdultId: String, name: String) =
        LinkedPersonViewModel.Factory(
            caregiverId,
            olderAdultId,
            name,
            LinkedPersonIntegration(
                monitoring.linkedPersonSummaryReader(),
                monitoring.linkedPersonProfileReader(),
                treatment.linkedPersonTreatmentsReader(),
                careLink.linkedPersonLinksReader(caregiverId),
            ),
            monitoring.linkedPersonContactReader(),
            careLink.linkedPersonProfileUpdater(caregiverId),
            careLink.linkedPersonEmergencyContactUpdater(caregiverId),
        )

    /** Creates the monitoring summary for the selected linked adult. */
    fun familySummaryViewModelFactory(caregiverId: String, olderAdultId: String, name: String) =
        monitoring.familySummaryViewModelFactory(caregiverId, olderAdultId, name)

    /** Creates the current account's personal notes flow. */
    fun personalNotesViewModelFactory() = monitoring.personalNotesViewModelFactory()

    /** Creates follow-up notes for the selected linked adult. */
    fun notesViewModelFactory(caregiverId: String, olderAdultId: String) =
        monitoring.notesViewModelFactory(caregiverId, olderAdultId)

    /** Creates the alert list for the selected linked adult. */
    fun alertsViewModelFactory(caregiverId: String, olderAdultId: String) =
        monitoring.alertsViewModelFactory(caregiverId, olderAdultId)

    /** Creates the selected alert detail flow. */
    fun alertDetailViewModelFactory(caregiverId: String, olderAdultId: String, alertId: Long) =
        monitoring.alertDetailViewModelFactory(caregiverId, olderAdultId, alertId)

    /** Omission owns FCM, device registration, local targets and access policy. */
    private val omission = OmissionDependencies(
        applicationContext, sessions, sessions, baseUrl, BuildConfig.PUSH_REGISTRATION_ENABLED,
    )

    fun canDisplayOmissionPush(push: OmissionPush): Boolean = omission.canDisplay(push)

    fun followCaregiverAlerts(caregiverId: String, olderAdultId: String) =
        omission.followCaregiver(caregiverId, olderAdultId)

    fun followDoseReminders(olderAdultId: String, olderAdultName: String) =
        omission.followAdultReminders(olderAdultId, olderAdultName)

    fun pushDestination(
        kind: String?,
        olderAdultId: String?,
        medicationName: String?,
        intakeId: String? = null,
    ): PushDestination? = omission.destination(kind, olderAdultId, medicationName, intakeId)

    fun canOpenPushDestination(destination: PushDestination): Boolean =
        omission.canOpen(destination)

    private val pushAlertResolver by lazy { PushAlertResolver(monitoring.getOpenAlerts) }

    suspend fun openAlertIdFor(destination: PushDestination.CaregiverAlerts): Long? =
        pushAlertResolver.openAlertIdFor(destination)

    private val preferences =
        PreferencesDependencies(applicationContext, sessions, sessions, baseUrl)

    /** Per-account application language, available to the activity before navigation. */
    val languagePreferences = preferences.languagePreferences

    fun initialLanguage() = preferences.initialLanguage()

    /** Intake-owned persistence, recovery and screen composition. */
    internal val intake = IntakeDependencies(applicationContext, sessions, sessions, baseUrl)

    /** Resumes intake work after login, session restoration or credential renewal. */
    fun observeSessionChanges(scope: CoroutineScope) {
        intake.observeSessionChanges(scope)
        preferences.startRecovery(scope)
        omission.startRegistration(scope)
    }

    fun onPushTokenChanged(token: String) = omission.onNewToken(token)

    fun observeOmissionNotificationSession(scope: CoroutineScope, clearNotifications: () -> Unit) =
        omission.observeNotificationSession(scope, clearNotifications)

    /** Composition bridge for Treatment catalog plus Intake's next dose. */
    private val medicationOverview by lazy {
        MedicationOverviewIntegration(sessions, baseUrl, intake)
    }

    fun myMedicationsViewModelFactory(olderAdultId: String) =
        medicationOverview.viewModelFactory(olderAdultId)

    /** Accessibility state shared with the application theme. */
    val accessibilityPreferences: Flow<AccessibilityPreferences> =
        preferences.accessibilityPreferences

    /** Creates the account's accessibility settings flow. */
    fun accessibilityViewModelFactory(userId: String) =
        preferences.accessibilityViewModelFactory(userId)

    /** Creates the account's notification preferences flow. */
    fun notificationPreferencesViewModelFactory(userId: String) =
        preferences.notificationPreferencesViewModelFactory(userId)

    private val inventory = InventoryDependencies(sessions, baseUrl)

    /** Creates inventory actions scoped to the account opening this screen. */
    fun inventoryViewModelFactory(medicationId: String, medicationName: String, unit: String) =
        inventory.inventoryViewModelFactory(medicationId, medicationName, unit)

    /** Analytics-owned factories for application navigation. */
    internal val analytics = AnalyticsDependencies(sessions, baseUrl)
}
