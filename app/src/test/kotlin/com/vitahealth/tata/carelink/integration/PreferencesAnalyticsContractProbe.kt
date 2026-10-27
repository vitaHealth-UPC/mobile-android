package com.vitahealth.tata.carelink.integration

import com.vitahealth.tata.analytics.infrastructure.remote.AnalyticsApiService
import com.vitahealth.tata.analytics.infrastructure.remote.RemoteAdherenceRecommendationsRepository
import com.vitahealth.tata.analytics.infrastructure.remote.RemoteAdherenceSummaryRepository
import com.vitahealth.tata.preferences.domain.model.valueobjects.TextSizeLevel
import com.vitahealth.tata.preferences.infrastructure.remote.PreferencesApiService
import com.vitahealth.tata.preferences.infrastructure.remote.RemoteUserPreferences
import com.vitahealth.tata.shared.common.result.AppResult
import retrofit2.Retrofit

/** Real owner-scoped reads and writes, independent from UI fixtures or provider configuration. */
internal object PreferencesAnalyticsContractProbe {
    suspend fun verify(
        caregiverClient: Retrofit,
        adultClient: Retrofit,
        caregiver: String,
        adult: String,
        treatment: String,
    ) {
        val preferences =
            RemoteUserPreferences(caregiverClient.create(PreferencesApiService::class.java))
        val before = preferences.get(caregiver).receipt()
        preferences.updateTextSize(caregiver, TextSizeLevel.LARGE).receipt()
        preferences.updateDarkTheme(caregiver, true).receipt()
        preferences.updateHighContrast(caregiver, true).receipt()
        preferences.updateReducedMotion(caregiver, true).receipt()
        preferences.updateReadingAssistance(caregiver, true).receipt()
        val stored = preferences.get(caregiver).receipt()
        check(stored.userId == caregiver)
        check(stored.accessibility.textSize == TextSizeLevel.LARGE)
        check(stored.accessibility.darkTheme && stored.accessibility.highContrast)
        check(stored.accessibility.reducedMotion && stored.accessibility.readingAssistance)
        check(stored.notifications == before.notifications) {
            "Accessibility writes changed notification policy"
        }
        val foreign = RemoteUserPreferences(adultClient.create(PreferencesApiService::class.java))
        check(
            (foreign.updateDarkTheme(caregiver, false) as? AppResult.Failure)?.code ==
                "PREFERENCES_ACCESS_DENIED"
        )
        check(preferences.get(caregiver).receipt().accessibility.darkTheme)
        val api = caregiverClient.create(AnalyticsApiService::class.java)
        val summary = RemoteAdherenceSummaryRepository(api)
        summary.getSummary(adult, 7).receipt()
        summary.getTreatmentSummary(adult, treatment, 7).receipt()
        RemoteAdherenceRecommendationsRepository(api).getRecommendations(adult, 7).receipt()
        check((summary.getSummary(caregiver, 7) as? AppResult.Failure)?.code == "ACCESS_DENIED")
        println(
            "PREFERENCES_ANALYTICS_CONTRACT_PASS: persisted accessibility, unchanged notification policy, rejected foreign mutation, authorized analytics and denied foreign scope"
        )
    }

    private fun <T> AppResult<T>.receipt(): T =
        when (this) {
            is AppResult.Success -> value
            is AppResult.Failure -> error("Preferences/analytics receipt failed: $code")
        }
}
