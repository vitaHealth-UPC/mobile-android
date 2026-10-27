package com.vitahealth.tata.monitoring.infrastructure.di

import com.vitahealth.tata.monitoring.application.commands.CreatePersonalNoteCommandHandler
import com.vitahealth.tata.monitoring.application.commands.RegisterFollowUpNoteCommandHandler
import com.vitahealth.tata.monitoring.application.commands.UpdateAlertStatusCommandHandler
import com.vitahealth.tata.monitoring.application.queries.GetAlertDetailQueryHandler
import com.vitahealth.tata.monitoring.application.queries.GetAlertTimelineQueryHandler
import com.vitahealth.tata.monitoring.application.queries.GetFamilyHistoryQueryHandler
import com.vitahealth.tata.monitoring.application.queries.GetFamilySummaryQueryHandler
import com.vitahealth.tata.monitoring.application.queries.GetFollowUpNotesQueryHandler
import com.vitahealth.tata.monitoring.application.queries.GetOpenAlertsQueryHandler
import com.vitahealth.tata.monitoring.application.queries.GetPersonalNotesQueryHandler
import com.vitahealth.tata.monitoring.infrastructure.remote.AlertsApiService
import com.vitahealth.tata.monitoring.infrastructure.remote.FamilyMonitoringApiService
import com.vitahealth.tata.monitoring.infrastructure.remote.NotesApiService
import com.vitahealth.tata.monitoring.infrastructure.remote.PersonalNotesApiService
import com.vitahealth.tata.monitoring.infrastructure.remote.RemoteAlertHistoryReader
import com.vitahealth.tata.monitoring.infrastructure.remote.RemoteAlertTimelineReader
import com.vitahealth.tata.monitoring.infrastructure.remote.RemoteAlertsRepository
import com.vitahealth.tata.monitoring.infrastructure.remote.RemoteContactRepository
import com.vitahealth.tata.monitoring.infrastructure.remote.RemoteFamilyMonitoringRepository
import com.vitahealth.tata.monitoring.infrastructure.remote.RemoteMonitoredDoseReader
import com.vitahealth.tata.monitoring.infrastructure.remote.RemoteMonitoredPersonReader
import com.vitahealth.tata.monitoring.infrastructure.remote.RemoteNotesRepository
import com.vitahealth.tata.monitoring.infrastructure.remote.RemotePersonalNotesRepository
import com.vitahealth.tata.monitoring.infrastructure.security.SessionScopedAlertHistoryReader
import com.vitahealth.tata.monitoring.infrastructure.security.SessionScopedAlertTimelineReader
import com.vitahealth.tata.monitoring.infrastructure.security.SessionScopedAlertsRepository
import com.vitahealth.tata.monitoring.infrastructure.security.SessionScopedContactOptionReader
import com.vitahealth.tata.monitoring.infrastructure.security.SessionScopedFamilyMonitoringRepository
import com.vitahealth.tata.monitoring.infrastructure.security.SessionScopedMonitoredDoseReader
import com.vitahealth.tata.monitoring.infrastructure.security.SessionScopedMonitoredPersonReader
import com.vitahealth.tata.monitoring.infrastructure.security.SessionScopedNotesRepository
import com.vitahealth.tata.monitoring.infrastructure.security.SessionScopedPersonalNotesRepository
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertDetailViewModel
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertsViewModel
import com.vitahealth.tata.monitoring.presentation.notes.viewmodel.NotesViewModel
import com.vitahealth.tata.monitoring.presentation.personalnotes.viewmodel.PersonalNotesViewModel
import com.vitahealth.tata.monitoring.presentation.summary.viewmodel.FamilySummaryViewModel
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory

/** Composes monitoring flows with repositories bound to the credentials of each operation. */
internal class MonitoringDependencies(sessions: SessionStore, baseUrl: String) {
    private val monitoringRepository =
        SessionScopedFamilyMonitoringRepository(sessions) { captured ->
            val api =
                ApiClientFactory.create(baseUrl) { captured.accessToken }
                    .create(FamilyMonitoringApiService::class.java)
            RemoteFamilyMonitoringRepository(api)
        }

    /** Exposes authorized public Monitoring ports to the application projection. */
    fun linkedPersonSummaryReader() = GetFamilySummaryQueryHandler(monitoringRepository)
    fun linkedPersonProfileReader() = monitoredPersonReader
    fun linkedPersonContactReader() = contactOptionReader

    /** Creates the linked-adult summary with contact actions for the same session. */
    fun familySummaryViewModelFactory(caregiverId: String, olderAdultId: String, name: String) =
        FamilySummaryViewModel.Factory(
            caregiverId,
            olderAdultId,
            name,
            GetFamilySummaryQueryHandler(monitoringRepository),
            GetFamilyHistoryQueryHandler(monitoringRepository),
            contactOptionReader,
        )

    private val contactOptionReader =
        SessionScopedContactOptionReader(sessions) { captured ->
            val api =
                ApiClientFactory.create(baseUrl) { captured.accessToken }
                    .create(FamilyMonitoringApiService::class.java)
            RemoteContactRepository(api)
        }

    private val personalNotesRepository =
        SessionScopedPersonalNotesRepository(sessions) { captured ->
            val api =
                ApiClientFactory.create(baseUrl) { captured.accessToken }
                    .create(PersonalNotesApiService::class.java)
            RemotePersonalNotesRepository(api)
        }

    /** Creates the account-scoped personal notes flow. */
    fun personalNotesViewModelFactory() =
        PersonalNotesViewModel.Factory(
            GetPersonalNotesQueryHandler(personalNotesRepository),
            CreatePersonalNoteCommandHandler(personalNotesRepository),
        )

    private val notesRepository =
        SessionScopedNotesRepository(sessions) { captured ->
            val api =
                ApiClientFactory.create(baseUrl) { captured.accessToken }
                    .create(NotesApiService::class.java)
            RemoteNotesRepository(api)
        }

    /** Creates follow-up note reading and registration for the selected adult. */
    fun notesViewModelFactory(caregiverId: String, olderAdultId: String) =
        NotesViewModel.Factory(
            caregiverId,
            olderAdultId,
            GetFollowUpNotesQueryHandler(notesRepository),
            RegisterFollowUpNoteCommandHandler(notesRepository),
            monitoredPersonReader,
        )

    private val alertsRepository =
        SessionScopedAlertsRepository(sessions) { captured ->
            val client = ApiClientFactory.create(baseUrl) { captured.accessToken }
            RemoteAlertsRepository(
                client.create(FamilyMonitoringApiService::class.java),
                client.create(AlertsApiService::class.java),
            )
        }

    private val alertTimelineReader =
        SessionScopedAlertTimelineReader(sessions) { captured ->
            val api =
                ApiClientFactory.create(baseUrl) { captured.accessToken }
                    .create(FamilyMonitoringApiService::class.java)
            RemoteAlertTimelineReader(api)
        }

    /** Creates the linked-adult alert list. */
    fun alertsViewModelFactory(caregiverId: String, olderAdultId: String) =
        AlertsViewModel.Factory(
            caregiverId,
            olderAdultId,
            GetOpenAlertsQueryHandler(alertsRepository),
            GetAlertTimelineQueryHandler(alertTimelineReader),
        )

    private val monitoredPersonReader =
        SessionScopedMonitoredPersonReader(sessions) { captured ->
            RemoteMonitoredPersonReader(
                ApiClientFactory.create(baseUrl) { captured.accessToken }
                    .create(FamilyMonitoringApiService::class.java)
            )
        }

    private val monitoredDoseReader =
        SessionScopedMonitoredDoseReader(sessions) { captured ->
            RemoteMonitoredDoseReader(
                ApiClientFactory.create(baseUrl) { captured.accessToken }
                    .create(FamilyMonitoringApiService::class.java)
            )
        }

    private val alertHistoryReader =
        SessionScopedAlertHistoryReader(sessions) { captured ->
            RemoteAlertHistoryReader(
                ApiClientFactory.create(baseUrl) { captured.accessToken }
                    .create(FamilyMonitoringApiService::class.java)
            )
        }

    /** Shares alert, note and contact adapters for the alert detail flow. */
    fun alertDetailViewModelFactory(caregiverId: String, olderAdultId: String, alertId: Long) =
        AlertDetailViewModel.Factory(
            caregiverId,
            olderAdultId,
            alertId,
            GetAlertDetailQueryHandler(alertsRepository),
            UpdateAlertStatusCommandHandler(alertsRepository),
            RegisterFollowUpNoteCommandHandler(notesRepository),
            contactOptionReader,
            monitoredPersonReader,
            monitoredDoseReader,
            alertHistoryReader,
        )

    /** Exposes the monitoring query used by application-level notification navigation. */
    val getOpenAlerts = GetOpenAlertsQueryHandler(alertsRepository)
}
