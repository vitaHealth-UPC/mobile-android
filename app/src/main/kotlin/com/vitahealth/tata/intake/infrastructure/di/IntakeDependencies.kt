package com.vitahealth.tata.intake.infrastructure.di

import android.content.Context
import com.vitahealth.tata.intake.application.commands.ConfirmDoseByVoiceCommandHandler
import com.vitahealth.tata.intake.application.commands.ConfirmDoseCommandHandler
import com.vitahealth.tata.intake.application.ports.DoseConfirmationRepository
import com.vitahealth.tata.intake.application.ports.DoseDetailRepository
import com.vitahealth.tata.intake.application.ports.IntakeAgendaRepository
import com.vitahealth.tata.intake.application.ports.NextDoseRepository
import com.vitahealth.tata.intake.application.queries.GetDailyDoseProgressQueryHandler
import com.vitahealth.tata.intake.application.queries.GetDoseDetailQueryHandler
import com.vitahealth.tata.intake.application.queries.GetIntakeAgendaQueryHandler
import com.vitahealth.tata.intake.application.queries.GetNextDoseQueryHandler
import com.vitahealth.tata.intake.infrastructure.audio.AndroidVoiceAudioRecorder
import com.vitahealth.tata.intake.infrastructure.local.OfflineFirstNextDoseRepository
import com.vitahealth.tata.intake.infrastructure.local.RoomIntakeLocalStore
import com.vitahealth.tata.intake.infrastructure.local.SessionScopedIntakeRepositories
import com.vitahealth.tata.intake.infrastructure.local.room.IntakeDatabase
import com.vitahealth.tata.intake.infrastructure.remote.IntakeApiService
import com.vitahealth.tata.intake.infrastructure.remote.RemoteNextDoseRepository
import com.vitahealth.tata.intake.infrastructure.remote.RemoteVoiceConfirmationRepository
import com.vitahealth.tata.intake.infrastructure.remote.SessionScopedVoiceConfirmationRepository
import com.vitahealth.tata.intake.infrastructure.remote.VoiceConfirmationApiService
import com.vitahealth.tata.intake.infrastructure.sync.IntakeSessionRecovery
import com.vitahealth.tata.intake.infrastructure.sync.WorkManagerIntakeSyncScheduler
import com.vitahealth.tata.intake.presentation.agenda.viewmodel.IntakeAgendaViewModel
import com.vitahealth.tata.intake.presentation.detail.viewmodel.DoseDetailViewModel
import com.vitahealth.tata.intake.presentation.home.viewmodel.NextDoseHomeViewModel
import com.vitahealth.tata.intake.presentation.voice.viewmodel.VoiceConfirmationViewModel
import com.vitahealth.tata.shared.application.session.SessionObserver
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory
import kotlinx.coroutines.CoroutineScope

/** Composes intake persistence, recovery and screen flows without depending on other contexts. */
internal class IntakeDependencies(
    context: Context,
    private val sessions: SessionStore,
    observer: SessionObserver,
    private val baseUrl: String,
) {
    private val applicationContext = context.applicationContext
    private val intakeSyncScheduler = WorkManagerIntakeSyncScheduler(applicationContext, baseUrl)
    private val intakeRepositories =
        SessionScopedIntakeRepositories(
            sessions = sessions,
            remoteFor = { captured ->
                ApiClientFactory.create(baseUrl) { captured.accessToken }
                    .create(IntakeApiService::class.java)
            },
            localFor = { owner ->
                RoomIntakeLocalStore(
                    IntakeDatabase.getInstance(applicationContext).intakes(),
                    owner,
                )
            },
            scheduler = intakeSyncScheduler,
        )
    private val intakeAgendaRepository: IntakeAgendaRepository = intakeRepositories
    private val nextDoseRepository: NextDoseRepository = intakeRepositories
    private val doseDetailRepository: DoseDetailRepository = intakeRepositories
    private val doseConfirmationRepository: DoseConfirmationRepository = intakeRepositories

    private val intakeSessionRecovery = IntakeSessionRecovery(observer, intakeSyncScheduler)

    /** Starts process-owned recovery when usable credentials are restored or changed. */
    fun observeSessionChanges(scope: CoroutineScope) {
        intakeSessionRecovery.start(scope)
    }

    /** Supplies a fixed-credential next-dose reader for application-level composition. */
    fun nextDoseFor(captured: StoredSession): NextDoseRepository =
        OfflineFirstNextDoseRepository(
            RemoteNextDoseRepository(
                ApiClientFactory.create(baseUrl) { captured.accessToken }
                    .create(IntakeApiService::class.java)
            ),
            RoomIntakeLocalStore(
                IntakeDatabase.getInstance(applicationContext).intakes(),
                requireNotNull(captured.owner),
            ),
        )

    /** Creates the daily agenda backed by the owner-scoped local store. */
    fun intakeAgendaViewModelFactory(olderAdultId: String) =
        IntakeAgendaViewModel.Factory(
            olderAdultId = olderAdultId,
            handler = GetIntakeAgendaQueryHandler(intakeAgendaRepository),
        )

    /** Creates the next-dose screen and daily progress reader. */
    fun nextDoseHomeViewModelFactory(olderAdultId: String, olderAdultName: String) =
        NextDoseHomeViewModel.Factory(
            olderAdultId = olderAdultId,
            olderAdultName = olderAdultName,
            handler = GetNextDoseQueryHandler(nextDoseRepository),
            progressHandler = GetDailyDoseProgressQueryHandler(intakeAgendaRepository),
        )

    /** Creates a screen-owned audio recorder and authenticated voice confirmation. */
    fun voiceConfirmationViewModelFactory(intakeId: String) =
        VoiceConfirmationViewModel.Factory(
            intakeId,
            GetDoseDetailQueryHandler(doseDetailRepository),
            ConfirmDoseByVoiceCommandHandler(
                SessionScopedVoiceConfirmationRepository(sessions) { captured ->
                    RemoteVoiceConfirmationRepository(
                        ApiClientFactory.create(baseUrl) { captured.accessToken }
                            .create(VoiceConfirmationApiService::class.java)
                    )
                }
            ),
            AndroidVoiceAudioRecorder(applicationContext),
            GetNextDoseQueryHandler(nextDoseRepository),
        )

    /** Creates dose details, confirmation and next-dose lookup. */
    fun doseDetailViewModelFactory(intakeId: String) =
        DoseDetailViewModel.Factory(
            intakeId = intakeId,
            handler = GetDoseDetailQueryHandler(doseDetailRepository),
            confirmHandler = ConfirmDoseCommandHandler(doseConfirmationRepository),
            nextDoseHandler = GetNextDoseQueryHandler(nextDoseRepository),
        )
}
