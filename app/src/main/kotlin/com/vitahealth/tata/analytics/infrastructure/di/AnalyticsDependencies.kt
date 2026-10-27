package com.vitahealth.tata.analytics.infrastructure.di

import com.vitahealth.tata.analytics.application.ports.TreatmentAdherenceRepository
import com.vitahealth.tata.analytics.application.queries.GetAdherenceRecommendationsQueryHandler
import com.vitahealth.tata.analytics.application.queries.GetAdherenceSummaryQueryHandler
import com.vitahealth.tata.analytics.infrastructure.remote.AnalyticsApiService
import com.vitahealth.tata.analytics.infrastructure.security.SessionScopedAnalyticsRepositories
import com.vitahealth.tata.analytics.presentation.history.viewmodel.AdherenceHistoryViewModel
import com.vitahealth.tata.analytics.presentation.recommendations.viewmodel.AdherenceRecommendationsViewModel
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory

/** Composes each analytics screen with its initiating owner and fixed-credential API requests. */
internal class AnalyticsDependencies(
    private val sessions: SessionStore,
    private val baseUrl: String,
) {
    private fun repositoriesForScreen() =
        SessionScopedAnalyticsRepositories(
            sessions = sessions,
            screenOwner = sessions.session()?.owner,
            apiFor = { captured ->
                ApiClientFactory.create(baseUrl) { captured.accessToken }
                    .create(AnalyticsApiService::class.java)
            },
        )

    fun treatmentAdherenceRepository(): TreatmentAdherenceRepository = repositoriesForScreen()

    /** Captures the owner of the history screen when its factory is requested. */
    fun adherenceHistoryViewModelFactory(olderAdultId: String) =
        AdherenceHistoryViewModel.Factory(
            olderAdultId,
            GetAdherenceSummaryQueryHandler(repositoriesForScreen()),
        )

    /** Captures the owner of the recommendations screen when its factory is requested. */
    fun adherenceRecommendationsViewModelFactory(olderAdultId: String) =
        AdherenceRecommendationsViewModel.Factory(
            olderAdultId,
            GetAdherenceRecommendationsQueryHandler(repositoriesForScreen()),
        )
}
