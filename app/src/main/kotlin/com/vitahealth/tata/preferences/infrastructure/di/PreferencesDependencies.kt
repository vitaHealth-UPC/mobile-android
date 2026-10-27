package com.vitahealth.tata.preferences.infrastructure.di

import android.content.Context
import com.vitahealth.tata.preferences.application.commands.ChangeApplicationLanguageCommandHandler
import com.vitahealth.tata.preferences.application.commands.SyncUserPreferencesCommandHandler
import com.vitahealth.tata.preferences.application.commands.UpdateDarkThemeCommandHandler
import com.vitahealth.tata.preferences.application.commands.UpdateHighContrastCommandHandler
import com.vitahealth.tata.preferences.application.commands.UpdateNotificationPolicyCommandHandler
import com.vitahealth.tata.preferences.application.commands.UpdateNotificationPreferencesCommandHandler
import com.vitahealth.tata.preferences.application.commands.UpdateReadingAssistanceCommandHandler
import com.vitahealth.tata.preferences.application.commands.UpdateReducedMotionCommandHandler
import com.vitahealth.tata.preferences.application.commands.UpdateTextSizeCommandHandler
import com.vitahealth.tata.preferences.application.commands.UpdateVoiceConfirmationCommandHandler
import com.vitahealth.tata.preferences.application.queries.GetNotificationPreferencesQueryHandler
import com.vitahealth.tata.preferences.application.queries.ObserveAccessibilityPreferencesQueryHandler
import com.vitahealth.tata.preferences.domain.model.valueobjects.AccessibilityPreferences
import com.vitahealth.tata.preferences.infrastructure.local.AccountLanguageRepository
import com.vitahealth.tata.preferences.infrastructure.local.DataStoreAccessibilityLocalStore
import com.vitahealth.tata.preferences.infrastructure.remote.PreferencesApiService
import com.vitahealth.tata.preferences.infrastructure.remote.RemoteNotificationPreferencesRepository
import com.vitahealth.tata.preferences.infrastructure.remote.RemoteUserPreferences
import com.vitahealth.tata.preferences.infrastructure.security.SessionScopedNotificationPreferencesRepository
import com.vitahealth.tata.preferences.infrastructure.security.SessionScopedUserPreferencesRepository
import com.vitahealth.tata.preferences.infrastructure.sync.PreferencesSessionRecovery
import com.vitahealth.tata.preferences.infrastructure.sync.ValidatedNetworkObserver
import com.vitahealth.tata.preferences.presentation.accessibility.viewmodel.AccessibilityViewModel
import com.vitahealth.tata.preferences.presentation.notifications.viewmodel.NotificationPreferencesViewModel
import com.vitahealth.tata.shared.application.session.SessionObserver
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

/**
 * Composes owner-partitioned preferences and exposes accessibility state to the application theme.
 */
internal class PreferencesDependencies(
    private val context: Context,
    private val sessions: SessionStore,
    observer: SessionObserver,
    baseUrl: String,
) {
    private val languageRepository = AccountLanguageRepository(context, sessions, observer)
    val languagePreferences = languageRepository.observe()
    fun initialLanguage() = AccountLanguageRepository.languageFor(
        context, sessions.session()?.owner?.storageKey,
    )

    private val preferencesRepository =
        SessionScopedUserPreferencesRepository(
            sessions = sessions,
            observer = observer,
            remoteFor = { captured ->
                val api =
                    ApiClientFactory.create(baseUrl) { captured.accessToken }
                        .create(PreferencesApiService::class.java)
                RemoteUserPreferences(api)
            },
            localFor = { owner ->
                DataStoreAccessibilityLocalStore(context.applicationContext, owner.storageKey)
            },
        )
    private val recovery =
        PreferencesSessionRecovery(
            observer,
            ValidatedNetworkObserver(context).observe(),
            synchronize = { userId ->
                preferencesRepository.sync(userId)
                Unit
            },
        )

    /** Starts one process-owned recovery collector, independent of preference screens. */
    fun startRecovery(scope: CoroutineScope) = recovery.start(scope)

    private val notificationPreferencesRepository =
        SessionScopedNotificationPreferencesRepository(sessions) { captured ->
            val api =
                ApiClientFactory.create(baseUrl) { captured.accessToken }
                    .create(PreferencesApiService::class.java)
            RemoteNotificationPreferencesRepository(api)
        }

    /**
     * Device copy of the accessibility settings; the theme reads it so they apply on every screen.
     */
    val accessibilityPreferences: Flow<AccessibilityPreferences> =
        ObserveAccessibilityPreferencesQueryHandler(preferencesRepository)()

    /** Creates accessibility editing and synchronization for the selected account. */
    fun accessibilityViewModelFactory(userId: String) =
        AccessibilityViewModel.Factory(
            userId = userId,
            observeAccessibility =
                ObserveAccessibilityPreferencesQueryHandler(preferencesRepository),
            updateTextSize = UpdateTextSizeCommandHandler(preferencesRepository),
            updateDarkTheme = UpdateDarkThemeCommandHandler(preferencesRepository),
            updateHighContrast = UpdateHighContrastCommandHandler(preferencesRepository),
            updateReducedMotion = UpdateReducedMotionCommandHandler(preferencesRepository),
            updateVoiceConfirmation = UpdateVoiceConfirmationCommandHandler(preferencesRepository),
            updateReadingAssistance = UpdateReadingAssistanceCommandHandler(preferencesRepository),
            syncPreferences = SyncUserPreferencesCommandHandler(preferencesRepository),
            language = languagePreferences,
            changeLanguage = ChangeApplicationLanguageCommandHandler(languageRepository),
        )

    /** Creates notification settings and escalation policy editing. */
    fun notificationPreferencesViewModelFactory(userId: String) =
        NotificationPreferencesViewModel.Factory(
            userId = userId,
            getPreferences =
                GetNotificationPreferencesQueryHandler(notificationPreferencesRepository),
            updatePreferences =
                UpdateNotificationPreferencesCommandHandler(notificationPreferencesRepository),
            updatePolicy = UpdateNotificationPolicyCommandHandler(notificationPreferencesRepository),
        )
}
