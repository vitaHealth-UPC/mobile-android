package com.vitahealth.tata.app.di

import android.content.Context
import com.vitahealth.tata.analytics.application.AdherenceRecommendationsRepository
import com.vitahealth.tata.analytics.application.AdherenceSummaryRepository
import com.vitahealth.tata.analytics.application.handlers.GetAdherenceRecommendationsQueryHandler
import com.vitahealth.tata.analytics.application.handlers.GetAdherenceSummaryQueryHandler
import com.vitahealth.tata.analytics.infrastructure.fake.FakeAdherenceRecommendationsRepository
import com.vitahealth.tata.analytics.infrastructure.fake.FakeAdherenceScenario
import com.vitahealth.tata.analytics.infrastructure.fake.FakeAdherenceSummaryRepository
import com.vitahealth.tata.analytics.infrastructure.fake.FakeRecommendationsScenario
import com.vitahealth.tata.analytics.infrastructure.remote.AnalyticsApiService
import com.vitahealth.tata.analytics.infrastructure.remote.RemoteAdherenceRecommendationsRepository
import com.vitahealth.tata.analytics.infrastructure.remote.RemoteAdherenceSummaryRepository
import com.vitahealth.tata.analytics.presentation.history.AdherenceHistoryViewModel
import com.vitahealth.tata.analytics.presentation.recommendations.AdherenceRecommendationsViewModel
import com.vitahealth.tata.carelink.application.handlers.AcceptCareLinkCommandHandler
import com.vitahealth.tata.carelink.application.handlers.GetOlderAdultProfileQueryHandler
import com.vitahealth.tata.carelink.application.handlers.RegisterConsentCommandHandler
import com.vitahealth.tata.carelink.infrastructure.remote.CareLinkApiService
import com.vitahealth.tata.carelink.infrastructure.remote.RemoteCareLinkRepository
import com.vitahealth.tata.carelink.presentation.link.CareLinkViewModel
import com.vitahealth.tata.identity.application.handlers.RegisterCaregiverCommandHandler
import com.vitahealth.tata.identity.application.handlers.RequestNewVerificationCommandHandler
import com.vitahealth.tata.identity.application.handlers.VerifyCaregiverEmailCommandHandler
import com.vitahealth.tata.identity.infrastructure.remote.IdentityApiService
import com.vitahealth.tata.identity.infrastructure.remote.RemoteIdentityRepository
import com.vitahealth.tata.identity.presentation.registration.CaregiverRegistrationViewModel
import com.vitahealth.tata.intake.application.handlers.ConfirmDoseCommandHandler
import com.vitahealth.tata.omission.application.commands.FollowCaregiverAlertsCommand
import com.vitahealth.tata.omission.application.commands.FollowDoseRemindersCommand
import com.vitahealth.tata.omission.application.handlers.FollowCaregiverAlertsCommandHandler
import com.vitahealth.tata.omission.application.handlers.FollowDoseRemindersCommandHandler
import com.vitahealth.tata.omission.application.handlers.ResolvePushDestinationQueryHandler
import com.vitahealth.tata.omission.application.queries.PushDestination
import com.vitahealth.tata.monitoring.application.queries.GetOpenAlertsQuery
import com.vitahealth.tata.app.navigation.alertForPush
import com.vitahealth.tata.omission.infrastructure.push.FirebaseTopicSubscriptions
import com.vitahealth.tata.omission.infrastructure.push.SharedPreferencesPushTargetStore
import com.vitahealth.tata.omission.presentation.notifications.OmissionPushIntent
import com.vitahealth.tata.intake.application.handlers.GetDoseDetailQueryHandler
import com.vitahealth.tata.intake.application.handlers.GetNextDoseQueryHandler
import com.vitahealth.tata.intake.infrastructure.local.OfflineFirstDoseConfirmationRepository
import com.vitahealth.tata.intake.infrastructure.local.OfflineFirstDoseDetailRepository
import com.vitahealth.tata.intake.infrastructure.local.OfflineFirstIntakeAgendaRepository
import com.vitahealth.tata.intake.infrastructure.local.OfflineFirstNextDoseRepository
import com.vitahealth.tata.intake.infrastructure.local.SQLiteIntakeLocalStore
import com.vitahealth.tata.intake.infrastructure.remote.IntakeApiService
import com.vitahealth.tata.intake.infrastructure.remote.RemoteDoseConfirmationRepository
import com.vitahealth.tata.intake.infrastructure.remote.RemoteDoseDetailRepository
import com.vitahealth.tata.intake.infrastructure.remote.RemoteIntakeAgendaRepository
import com.vitahealth.tata.intake.infrastructure.remote.RemoteNextDoseRepository
import com.vitahealth.tata.intake.infrastructure.sync.WorkManagerIntakeSyncScheduler
import com.vitahealth.tata.intake.presentation.agenda.IntakeAgendaViewModel
import com.vitahealth.tata.intake.presentation.detail.DoseDetailViewModel
import com.vitahealth.tata.intake.presentation.home.NextDoseHomeViewModel
import com.vitahealth.tata.inventory.application.handlers.GetInventoryStockQueryHandler
import com.vitahealth.tata.monitoring.application.handlers.GetAlertDetailQueryHandler
import com.vitahealth.tata.monitoring.application.handlers.GetOpenAlertsQueryHandler
import com.vitahealth.tata.monitoring.application.handlers.GetContactOptionQueryHandler
import com.vitahealth.tata.monitoring.application.handlers.GetFollowUpNotesQueryHandler
import com.vitahealth.tata.monitoring.application.handlers.RegisterFollowUpNoteCommandHandler
import com.vitahealth.tata.monitoring.application.handlers.UpdateAlertStatusCommandHandler
import com.vitahealth.tata.monitoring.infrastructure.remote.NotesApiService
import com.vitahealth.tata.monitoring.infrastructure.remote.RemoteContactRepository
import com.vitahealth.tata.monitoring.infrastructure.remote.RemoteNotesRepository
import com.vitahealth.tata.monitoring.presentation.notes.NotesViewModel
import com.vitahealth.tata.monitoring.infrastructure.remote.AlertsApiService
import com.vitahealth.tata.monitoring.infrastructure.remote.FamilyMonitoringApiService
import com.vitahealth.tata.monitoring.infrastructure.remote.RemoteAlertsRepository
import com.vitahealth.tata.monitoring.presentation.alerts.AlertDetailViewModel
import com.vitahealth.tata.monitoring.presentation.alerts.AlertsViewModel
import com.vitahealth.tata.inventory.application.handlers.RegisterInitialInventoryCommandHandler
import com.vitahealth.tata.inventory.application.handlers.RegisterReplenishmentCommandHandler
import com.vitahealth.tata.inventory.infrastructure.remote.InventoryApiService
import com.vitahealth.tata.inventory.infrastructure.remote.RemoteInventoryRepository
import com.vitahealth.tata.inventory.presentation.inventory.InventoryViewModel
import com.vitahealth.tata.treatment.application.handlers.ChangeTreatmentStatusCommandHandler
import com.vitahealth.tata.treatment.application.handlers.ConfigureTreatmentCommandHandler
import com.vitahealth.tata.treatment.application.handlers.CreateTreatmentCommandHandler
import com.vitahealth.tata.treatment.application.handlers.DeactivateMedicationCommandHandler
import com.vitahealth.tata.treatment.application.handlers.GetTreatmentDetailQueryHandler
import com.vitahealth.tata.treatment.application.handlers.ListMedicationsQueryHandler
import com.vitahealth.tata.treatment.application.handlers.RegisterMedicationCommandHandler
import com.vitahealth.tata.treatment.application.handlers.SetDoseFrequencyCommandHandler
import com.vitahealth.tata.treatment.application.handlers.SetReminderPolicyCommandHandler
import com.vitahealth.tata.treatment.application.handlers.SetScheduleInstructionsCommandHandler
import com.vitahealth.tata.treatment.application.handlers.UpdateMedicationCommandHandler
import com.vitahealth.tata.treatment.infrastructure.remote.RemoteTreatmentRepository
import com.vitahealth.tata.treatment.infrastructure.remote.TreatmentApiService
import com.vitahealth.tata.treatment.presentation.medication.MedicationManagementViewModel
import com.vitahealth.tata.treatment.presentation.medication.MedicationRegistrationViewModel
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentCreationViewModel
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentDetailViewModel
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentDoseFrequencyViewModel
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentLifecycleViewModel
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentReminderViewModel
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentScheduleInstructionsViewModel
import com.vitahealth.tata.preferences.application.handlers.GetNotificationPreferencesQueryHandler
import com.vitahealth.tata.preferences.application.handlers.ObserveAccessibilityPreferencesQueryHandler
import com.vitahealth.tata.preferences.application.handlers.SyncUserPreferencesCommandHandler
import com.vitahealth.tata.preferences.application.handlers.UpdateHighContrastCommandHandler
import com.vitahealth.tata.preferences.application.handlers.UpdateNotificationPreferencesCommandHandler
import com.vitahealth.tata.preferences.application.handlers.UpdateReadingAssistanceCommandHandler
import com.vitahealth.tata.preferences.application.handlers.UpdateReducedMotionCommandHandler
import com.vitahealth.tata.preferences.application.handlers.UpdateTextSizeCommandHandler
import com.vitahealth.tata.preferences.application.handlers.UpdateVoiceConfirmationCommandHandler
import com.vitahealth.tata.preferences.domain.model.AccessibilityPreferences
import com.vitahealth.tata.preferences.infrastructure.OfflineFirstUserPreferencesRepository
import com.vitahealth.tata.preferences.infrastructure.local.DataStoreAccessibilityLocalStore
import com.vitahealth.tata.preferences.infrastructure.remote.PreferencesApiService
import com.vitahealth.tata.preferences.infrastructure.remote.RemoteNotificationPreferencesRepository
import com.vitahealth.tata.preferences.infrastructure.remote.RemoteUserPreferences
import com.vitahealth.tata.preferences.presentation.accessibility.AccessibilityViewModel
import com.vitahealth.tata.preferences.presentation.notifications.NotificationPreferencesViewModel
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(
    context: Context,
    baseUrl: String = com.vitahealth.tata.BuildConfig.API_BASE_URL,
) {
    private val sessions = com.vitahealth.tata.shared.infrastructure.security.EncryptedSessionStore(context)
    private val httpClient = okhttp3.OkHttpClient.Builder()
        .connectTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(90, java.util.concurrent.TimeUnit.SECONDS)
        .callTimeout(110, java.util.concurrent.TimeUnit.SECONDS)
        .addInterceptor { chain ->
        val request = chain.request().newBuilder()
        sessions.accessToken()?.let { request.header("Authorization", "Bearer $it") }
        chain.proceed(request.build())
    }.build()
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val sessionAccessRepository = com.vitahealth.tata.identity.infrastructure.remote.RemoteSessionAccessRepository(retrofit.create(com.vitahealth.tata.identity.infrastructure.remote.SessionApiService::class.java), sessions)
    val sessionAccessViewModelFactory = com.vitahealth.tata.identity.presentation.access.SessionAccessViewModel.Factory(sessionAccessRepository)

    suspend fun signOut() = sessionAccessRepository.signOut()

    private val subscriptionRepository = com.vitahealth.tata.identity.infrastructure.remote.RemoteSubscriptionRepository(
        retrofit.create(com.vitahealth.tata.identity.infrastructure.remote.SubscriptionApiService::class.java),
    )

    fun planSubscriptionViewModelFactory(accountId: String) =
        com.vitahealth.tata.identity.presentation.subscription.PlanSubscriptionViewModel.Factory(
            accountId = accountId,
            getSubscription = com.vitahealth.tata.identity.application.handlers.GetCurrentSubscriptionQueryHandler(subscriptionRepository),
            listPlans = com.vitahealth.tata.identity.application.handlers.ListAvailablePlansQueryHandler(subscriptionRepository),
            changeSubscription = com.vitahealth.tata.identity.application.handlers.ChangeSubscriptionCommandHandler(subscriptionRepository),
        )

    private val onboardingRepository =
        com.vitahealth.tata.identity.infrastructure.local.SharedPreferencesOnboardingRepository(context)
    private val getOnboardingStatus =
        com.vitahealth.tata.identity.application.handlers.GetOnboardingStatusQueryHandler(onboardingRepository)
    private val completeOnboardingHandler =
        com.vitahealth.tata.identity.application.handlers.CompleteOnboardingCommandHandler(onboardingRepository)

    /** False on the first launch of the app, so the welcome screen is shown once. */
    fun hasSeenOnboarding(): Boolean = getOnboardingStatus()

    fun completeOnboarding() = completeOnboardingHandler()

    private val caregiverProfilesRepository = com.vitahealth.tata.carelink.infrastructure.remote.RemoteCaregiverProfilesRepository(retrofit.create(com.vitahealth.tata.carelink.infrastructure.remote.CaregiverProfilesApiService::class.java))
    fun caregiverProfilesViewModelFactory(caregiverId: String) = com.vitahealth.tata.carelink.presentation.profiles.CaregiverProfilesViewModel.Factory(caregiverId,caregiverProfilesRepository)

    private val treatmentCatalog = com.vitahealth.tata.treatment.infrastructure.remote.RemoteTreatmentCatalogRepository(retrofit.create(TreatmentApiService::class.java))
    fun treatmentListViewModelFactory(caregiverId: String,olderAdultId: String) = com.vitahealth.tata.treatment.presentation.treatment.TreatmentListViewModel.Factory(caregiverId,olderAdultId,com.vitahealth.tata.treatment.application.handlers.ListTreatmentsQueryHandler(treatmentCatalog))

    private val monitoringRepository = com.vitahealth.tata.monitoring.infrastructure.remote.RemoteFamilyMonitoringRepository(
        retrofit.create(com.vitahealth.tata.monitoring.infrastructure.remote.FamilyMonitoringApiService::class.java),
    )
    fun familySummaryViewModelFactory(caregiverId: String, olderAdultId: String, name: String) =
        com.vitahealth.tata.monitoring.presentation.summary.FamilySummaryViewModel.Factory(
            caregiverId, olderAdultId, name, monitoringRepository, GetContactOptionQueryHandler(contactRepository),
        )

    private val contactRepository = RemoteContactRepository(
        retrofit.create(com.vitahealth.tata.monitoring.infrastructure.remote.FamilyMonitoringApiService::class.java),
    )

    private val personalNotesRepository = com.vitahealth.tata.monitoring.infrastructure.remote.RemotePersonalNotesRepository(
        retrofit.create(com.vitahealth.tata.monitoring.infrastructure.remote.PersonalNotesApiService::class.java))
    fun personalNotesViewModelFactory() = com.vitahealth.tata.monitoring.presentation.personalnotes.PersonalNotesViewModel.Factory(personalNotesRepository)

    private val notesRepository = RemoteNotesRepository(retrofit.create(NotesApiService::class.java))
    fun notesViewModelFactory(caregiverId: String, olderAdultId: String) = NotesViewModel.Factory(
        caregiverId, olderAdultId,
        GetFollowUpNotesQueryHandler(notesRepository),
        RegisterFollowUpNoteCommandHandler(notesRepository),
    )

    private val alertsRepository = RemoteAlertsRepository(
        monitoringApi = retrofit.create(FamilyMonitoringApiService::class.java),
        alertsApi = retrofit.create(AlertsApiService::class.java),
    )
    fun alertsViewModelFactory(caregiverId: String, olderAdultId: String) =
        AlertsViewModel.Factory(caregiverId, olderAdultId, GetOpenAlertsQueryHandler(alertsRepository))
    fun alertDetailViewModelFactory(caregiverId: String, olderAdultId: String, alertId: Long) =
        AlertDetailViewModel.Factory(
            caregiverId, olderAdultId, alertId,
            GetAlertDetailQueryHandler(alertsRepository),
            UpdateAlertStatusCommandHandler(alertsRepository),
            RegisterFollowUpNoteCommandHandler(notesRepository),
            GetContactOptionQueryHandler(contactRepository),
        )

    // Omission & Escalation push (US-22): FCM topics named after the backend recipients.
    private val pushTargets = SharedPreferencesPushTargetStore(context)
    private val pushTopics = FirebaseTopicSubscriptions(context)
    private val followCaregiverAlertsHandler = FollowCaregiverAlertsCommandHandler(pushTargets, pushTopics)
    private val followDoseRemindersHandler = FollowDoseRemindersCommandHandler(pushTargets, pushTopics)
    private val resolvePushDestinationHandler = ResolvePushDestinationQueryHandler(pushTargets)

    fun followCaregiverAlerts(caregiverId: String, olderAdultId: String) =
        followCaregiverAlertsHandler(FollowCaregiverAlertsCommand(caregiverId, olderAdultId))

    fun followDoseReminders(olderAdultId: String, olderAdultName: String) =
        followDoseRemindersHandler(FollowDoseRemindersCommand(olderAdultId, olderAdultName))

    /** Screen to open for an intent that came from an omission notification, or null. */
    fun pushDestination(kind: String?, olderAdultId: String?, medicationName: String?): PushDestination? =
        OmissionPushIntent.queryFrom(kind, olderAdultId, medicationName)?.let { resolvePushDestinationHandler(it) }

    /** Open alert a caregiver push refers to, read from the recent status (`openAlerts`), or null. */
    suspend fun openAlertIdFor(destination: PushDestination.CaregiverAlerts): Long? =
        when (val result = GetOpenAlertsQueryHandler(alertsRepository)(GetOpenAlertsQuery(destination.caregiverId, destination.olderAdultId))) {
            is com.vitahealth.tata.shared.common.result.AppResult.Success -> alertForPush(result.value, destination.medicationName)?.id
            is com.vitahealth.tata.shared.common.result.AppResult.Failure -> null
        }

    private val identityApi: IdentityApiService = retrofit.create(IdentityApiService::class.java)
    private val identityRepository = RemoteIdentityRepository(identityApi, sessions)

    private val careLinkApi: CareLinkApiService = retrofit.create(CareLinkApiService::class.java)
    private val careLinkRepository = RemoteCareLinkRepository(careLinkApi, sessions)

    private val treatmentApi: TreatmentApiService = retrofit.create(TreatmentApiService::class.java)
    private val treatmentRepository = RemoteTreatmentRepository(treatmentApi)

    private val inventoryApi: InventoryApiService = retrofit.create(InventoryApiService::class.java)
    private val inventoryRepository = RemoteInventoryRepository(inventoryApi)
    private val preferencesApi: PreferencesApiService = retrofit.create(PreferencesApiService::class.java)
    private val preferencesRepository = OfflineFirstUserPreferencesRepository(
        remote = RemoteUserPreferences(preferencesApi),
        local = DataStoreAccessibilityLocalStore(context),
    )

    private val notificationPreferencesRepository = RemoteNotificationPreferencesRepository(preferencesApi)

    private val intakeApi: IntakeApiService = retrofit.create(IntakeApiService::class.java)
    private val intakeLocalStore = SQLiteIntakeLocalStore(context)
    private val intakeSyncScheduler = WorkManagerIntakeSyncScheduler(context, baseUrl)

    private val intakeAgendaRepository = OfflineFirstIntakeAgendaRepository(
        remote = RemoteIntakeAgendaRepository(intakeApi),
        local = intakeLocalStore,
    )
    private val nextDoseRepository = OfflineFirstNextDoseRepository(
        remote = RemoteNextDoseRepository(intakeApi),
        local = intakeLocalStore,
    )
    private val doseDetailRepository = OfflineFirstDoseDetailRepository(
        remote = RemoteDoseDetailRepository(intakeApi),
        local = intakeLocalStore,
    )
    private val doseConfirmationRepository = OfflineFirstDoseConfirmationRepository(
        remote = RemoteDoseConfirmationRepository(intakeApi),
        local = intakeLocalStore,
        syncScheduler = intakeSyncScheduler,
    )

    init {
        intakeSyncScheduler.schedule()
    }

    fun myMedicationsViewModelFactory(olderAdultId: String) =
        com.vitahealth.tata.treatment.presentation.medication.MyMedicationsViewModel.Factory(
            query = com.vitahealth.tata.treatment.application.handlers.GetMyMedicationsQueryHandler(
                com.vitahealth.tata.treatment.infrastructure.remote.RemoteMyMedicationsRepository(treatmentApi)),
            nextDoseQuery = {
                when (val result = nextDoseRepository.getNextDose(olderAdultId)) {
                    is com.vitahealth.tata.shared.common.result.AppResult.Success -> result.value?.let {
                        com.vitahealth.tata.treatment.application.readmodels.MedicationNextDose(it.id, it.medicationId, it.scheduledAt)
                    }
                    is com.vitahealth.tata.shared.common.result.AppResult.Failure -> null
                }
            },
        )

    val caregiverRegistrationViewModelFactory = CaregiverRegistrationViewModel.Factory(
        registerHandler = RegisterCaregiverCommandHandler(identityRepository),
        verifyHandler = VerifyCaregiverEmailCommandHandler(identityRepository),
        requestNewVerificationHandler = RequestNewVerificationCommandHandler(identityRepository),
    )

    fun careLinkViewModelFactory(caregiverId: String) = CareLinkViewModel.Factory(
        caregiverId = caregiverId,
        acceptCareLinkHandler = AcceptCareLinkCommandHandler(careLinkRepository),
        registerConsentHandler = RegisterConsentCommandHandler(careLinkRepository),
        getOlderAdultHandler = GetOlderAdultProfileQueryHandler(careLinkRepository),
    )

    fun medicationRegistrationViewModelFactory(
        caregiverId: String,
        olderAdultId: String,
        olderAdultName: String,
    ) = MedicationRegistrationViewModel.Factory(
        caregiverId = caregiverId,
        olderAdultId = olderAdultId,
        olderAdultName = olderAdultName,
        registerMedicationHandler = RegisterMedicationCommandHandler(treatmentRepository),
    )

    fun medicationManagementViewModelFactory(
        caregiverId: String,
        olderAdultId: String,
        olderAdultName: String,
    ) = MedicationManagementViewModel.Factory(
        caregiverId = caregiverId,
        olderAdultId = olderAdultId,
        olderAdultName = olderAdultName,
        listMedications = ListMedicationsQueryHandler(treatmentRepository),
        updateMedication = UpdateMedicationCommandHandler(treatmentRepository),
        deactivateMedication = DeactivateMedicationCommandHandler(treatmentRepository),
    )

    fun treatmentCreationViewModelFactory(
        caregiverId: String,
        olderAdultId: String,
        olderAdultName: String,
        medicationId: String,
        medicationLabel: String,
    ) = TreatmentCreationViewModel.Factory(
        caregiverId = caregiverId,
        olderAdultId = olderAdultId,
        olderAdultName = olderAdultName,
        medicationId = medicationId,
        medicationLabel = medicationLabel,
        createTreatmentHandler = CreateTreatmentCommandHandler(treatmentRepository),
    )

    fun treatmentDoseFrequencyViewModelFactory(
        caregiverId: String,
        olderAdultId: String,
        olderAdultName: String,
        medicationId: String,
        medicationLabel: String,
        treatmentId: String,
        treatmentName: String,
    ) = TreatmentDoseFrequencyViewModel.Factory(
        caregiverId = caregiverId,
        olderAdultId = olderAdultId,
        olderAdultName = olderAdultName,
        medicationId = medicationId,
        medicationLabel = medicationLabel,
        treatmentId = treatmentId,
        treatmentName = treatmentName,
        handler = SetDoseFrequencyCommandHandler(),
    )

    fun treatmentScheduleInstructionsViewModelFactory(
        caregiverId: String,
        olderAdultId: String,
        olderAdultName: String,
        medicationId: String,
        medicationLabel: String,
        treatmentId: String,
        treatmentName: String,
        dosage: String,
        frequency: String,
    ) = TreatmentScheduleInstructionsViewModel.Factory(
        caregiverId = caregiverId,
        olderAdultId = olderAdultId,
        olderAdultName = olderAdultName,
        medicationId = medicationId,
        medicationLabel = medicationLabel,
        treatmentId = treatmentId,
        treatmentName = treatmentName,
        dosage = dosage,
        frequency = frequency,
        handler = SetScheduleInstructionsCommandHandler(),
    )

    fun treatmentReminderViewModelFactory(
        caregiverId: String,
        olderAdultId: String,
        olderAdultName: String,
        medicationId: String,
        medicationLabel: String,
        treatmentId: String,
        treatmentName: String,
        dosage: String,
        frequency: String,
        scheduleText: String,
        instructions: String,
    ) = TreatmentReminderViewModel.Factory(
        caregiverId = caregiverId,
        olderAdultId = olderAdultId,
        olderAdultName = olderAdultName,
        medicationId = medicationId,
        medicationLabel = medicationLabel,
        treatmentId = treatmentId,
        treatmentName = treatmentName,
        dosage = dosage,
        frequency = frequency,
        scheduleText = scheduleText,
        instructions = instructions,
        handler = SetReminderPolicyCommandHandler(),
    )

    fun treatmentLifecycleViewModelFactory(
        caregiverId: String,
        olderAdultId: String,
        olderAdultName: String,
        medicationId: String,
        medicationLabel: String,
        treatmentId: String,
        treatmentName: String,
        dosage: String,
        frequency: String,
        scheduleText: String,
        instructions: String,
        reminderDelayMinutes: Int,
    ) = TreatmentLifecycleViewModel.Factory(
        caregiverId = caregiverId,
        olderAdultId = olderAdultId,
        olderAdultName = olderAdultName,
        medicationId = medicationId,
        medicationLabel = medicationLabel,
        treatmentId = treatmentId,
        treatmentName = treatmentName,
        dosage = dosage,
        frequency = frequency,
        scheduleText = scheduleText,
        instructions = instructions,
        reminderDelayMinutes = reminderDelayMinutes,
        configureTreatmentHandler = ConfigureTreatmentCommandHandler(treatmentRepository),
        changeStatusHandler = ChangeTreatmentStatusCommandHandler(treatmentRepository),
    )

    fun treatmentDetailViewModelFactory(
        caregiverId: String,
        olderAdultName: String,
        treatmentId: String,
        medicationLabelHint: String,
    ) = TreatmentDetailViewModel.Factory(
        caregiverId = caregiverId,
        olderAdultName = olderAdultName,
        treatmentId = treatmentId,
        medicationLabelHint = medicationLabelHint,
        handler = GetTreatmentDetailQueryHandler(treatmentRepository),
    )

    /** Device copy of the accessibility settings; the theme reads it so they apply on every screen. */
    val accessibilityPreferences: Flow<AccessibilityPreferences> =
        ObserveAccessibilityPreferencesQueryHandler(preferencesRepository)()

    fun accessibilityViewModelFactory(userId: String) = AccessibilityViewModel.Factory(
        userId = userId,
        observeAccessibility = ObserveAccessibilityPreferencesQueryHandler(preferencesRepository),
        updateTextSize = UpdateTextSizeCommandHandler(preferencesRepository),
        updateHighContrast = UpdateHighContrastCommandHandler(preferencesRepository),
        updateReducedMotion = UpdateReducedMotionCommandHandler(preferencesRepository),
        updateVoiceConfirmation = UpdateVoiceConfirmationCommandHandler(preferencesRepository),
        updateReadingAssistance = UpdateReadingAssistanceCommandHandler(preferencesRepository),
        syncPreferences = SyncUserPreferencesCommandHandler(preferencesRepository),
    )

    fun notificationPreferencesViewModelFactory(userId: String) = NotificationPreferencesViewModel.Factory(
        userId = userId,
        getPreferences = GetNotificationPreferencesQueryHandler(notificationPreferencesRepository),
        updatePreferences = UpdateNotificationPreferencesCommandHandler(notificationPreferencesRepository),
    )

    fun intakeAgendaViewModelFactory(
        olderAdultId: String,
    ) = IntakeAgendaViewModel.Factory(
        olderAdultId = olderAdultId,
        repository = intakeAgendaRepository,
    )

    fun nextDoseHomeViewModelFactory(
        olderAdultId: String,
        olderAdultName: String,
    ) = NextDoseHomeViewModel.Factory(
        olderAdultId = olderAdultId,
        olderAdultName = olderAdultName,
        handler = GetNextDoseQueryHandler(nextDoseRepository),
        progressHandler = com.vitahealth.tata.intake.application.handlers.GetDailyDoseProgressQueryHandler(intakeAgendaRepository),
    )

    fun doseDetailViewModelFactory(
        intakeId: String,
    ) = DoseDetailViewModel.Factory(
        intakeId = intakeId,
        handler = GetDoseDetailQueryHandler(doseDetailRepository),
        confirmHandler = ConfirmDoseCommandHandler(doseConfirmationRepository),
        nextDoseHandler = GetNextDoseQueryHandler(nextDoseRepository),
    )

    fun inventoryViewModelFactory(
        medicationId: String,
        medicationName: String,
        unit: String,
    ) = InventoryViewModel.Factory(
        medicationId = medicationId,
        medicationName = medicationName,
        unit = unit,
        getStockHandler = GetInventoryStockQueryHandler(inventoryRepository),
        registerInitialHandler = RegisterInitialInventoryCommandHandler(inventoryRepository),
        registerReplenishmentHandler = RegisterReplenishmentCommandHandler(inventoryRepository),
    )
    private val analyticsApi: AnalyticsApiService = retrofit.create(AnalyticsApiService::class.java)

    // Con USE_ANALYTICS_BACKEND = false las pantallas usan datos de ejemplo, sin backend.
    // Escenarios de ejemplo: Content, NoData, EmptyWeek, Error.
    private val adherenceSummaryRepository: AdherenceSummaryRepository =
        if (USE_ANALYTICS_BACKEND) {
            RemoteAdherenceSummaryRepository(analyticsApi)
        } else {
            FakeAdherenceSummaryRepository(FakeAdherenceScenario.Content)
        }

    fun adherenceHistoryViewModelFactory(olderAdultId: String) = AdherenceHistoryViewModel.Factory(
        olderAdultId = olderAdultId,
        handler = GetAdherenceSummaryQueryHandler(adherenceSummaryRepository),
    )

    // Escenarios de ejemplo: Content, InsufficientEvidence, Error.
    private val adherenceRecommendationsRepository: AdherenceRecommendationsRepository =
        if (USE_ANALYTICS_BACKEND) {
            RemoteAdherenceRecommendationsRepository(analyticsApi)
        } else {
            FakeAdherenceRecommendationsRepository(FakeRecommendationsScenario.Content)
        }

    fun adherenceRecommendationsViewModelFactory(olderAdultId: String) = AdherenceRecommendationsViewModel.Factory(
        olderAdultId = olderAdultId,
        handler = GetAdherenceRecommendationsQueryHandler(adherenceRecommendationsRepository),
    )

    private companion object {
        const val USE_ANALYTICS_BACKEND = true
    }

}
