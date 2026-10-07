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
import com.vitahealth.tata.treatment.application.handlers.ChangeTreatmentStatusCommandHandler
import com.vitahealth.tata.treatment.application.handlers.ConfigureTreatmentCommandHandler
import com.vitahealth.tata.treatment.application.handlers.CreateTreatmentCommandHandler
import com.vitahealth.tata.treatment.application.handlers.GetTreatmentDetailQueryHandler
import com.vitahealth.tata.treatment.application.handlers.RegisterMedicationCommandHandler
import com.vitahealth.tata.treatment.application.handlers.SetDoseFrequencyCommandHandler
import com.vitahealth.tata.treatment.application.handlers.SetReminderPolicyCommandHandler
import com.vitahealth.tata.treatment.application.handlers.SetScheduleInstructionsCommandHandler
import com.vitahealth.tata.treatment.infrastructure.remote.RemoteTreatmentRepository
import com.vitahealth.tata.treatment.infrastructure.remote.TreatmentApiService
import com.vitahealth.tata.treatment.presentation.medication.MedicationRegistrationViewModel
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentCreationViewModel
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentDetailViewModel
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentDoseFrequencyViewModel
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentLifecycleViewModel
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentReminderViewModel
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentScheduleInstructionsViewModel
import com.vitahealth.tata.preferences.application.handlers.ObserveAccessibilityPreferencesQueryHandler
import com.vitahealth.tata.preferences.application.handlers.SyncUserPreferencesCommandHandler
import com.vitahealth.tata.preferences.application.handlers.UpdateTextSizeCommandHandler
import com.vitahealth.tata.preferences.domain.model.AccessibilityPreferences
import com.vitahealth.tata.preferences.infrastructure.OfflineFirstUserPreferencesRepository
import com.vitahealth.tata.preferences.infrastructure.local.DataStoreAccessibilityLocalStore
import com.vitahealth.tata.preferences.infrastructure.remote.PreferencesApiService
import com.vitahealth.tata.preferences.infrastructure.remote.RemoteUserPreferences
import com.vitahealth.tata.preferences.presentation.accessibility.AccessibilityViewModel
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(
    context: Context,
    baseUrl: String = "http://10.0.2.2:8080/",
) {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val monitoringRepository = com.vitahealth.tata.monitoring.infrastructure.remote.RemoteFamilyMonitoringRepository(
        retrofit.create(com.vitahealth.tata.monitoring.infrastructure.remote.FamilyMonitoringApiService::class.java),
    )
    fun familySummaryViewModelFactory(caregiverId: String, olderAdultId: String, name: String) =
        com.vitahealth.tata.monitoring.presentation.summary.FamilySummaryViewModel.Factory(caregiverId, olderAdultId, name, monitoringRepository)

    private val identityApi: IdentityApiService = retrofit.create(IdentityApiService::class.java)
    private val identityRepository = RemoteIdentityRepository(identityApi)

    private val careLinkApi: CareLinkApiService = retrofit.create(CareLinkApiService::class.java)
    private val careLinkRepository = RemoteCareLinkRepository(careLinkApi)

    private val treatmentApi: TreatmentApiService = retrofit.create(TreatmentApiService::class.java)
    private val treatmentRepository = RemoteTreatmentRepository(treatmentApi)

    private val preferencesApi: PreferencesApiService = retrofit.create(PreferencesApiService::class.java)
    private val preferencesRepository = OfflineFirstUserPreferencesRepository(
        remote = RemoteUserPreferences(preferencesApi),
        local = DataStoreAccessibilityLocalStore(context),
    )

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
        syncPreferences = SyncUserPreferencesCommandHandler(preferencesRepository),
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
    )

    fun doseDetailViewModelFactory(
        intakeId: String,
    ) = DoseDetailViewModel.Factory(
        intakeId = intakeId,
        handler = GetDoseDetailQueryHandler(doseDetailRepository),
        confirmHandler = ConfirmDoseCommandHandler(doseConfirmationRepository),
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
