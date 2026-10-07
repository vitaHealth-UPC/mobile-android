package com.vitahealth.tata.analytics.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class TrendPointResponse(
    val date: String,
    val adherencePercent: Int,
)

data class RecentIntakeResponse(
    val scheduledAt: String,
    val medicationName: String,
    val status: String,
    val minutesLate: Int?,
)

data class SummaryPatternResponse(
    val timeBand: String,
    val omittedCount: Int,
    val lateCount: Int,
)

data class AdherenceSummaryResponse(
    val periodDays: Int,
    val scheduledCount: Int,
    val adherencePercent: Int,
    val adherenceChangePercent: Int?,
    val onTimePercent: Int,
    val onTimeChangePercent: Int?,
    val lateCount: Int,
    val omittedCount: Int,
    val trend: List<TrendPointResponse>?,
    val recentIntakes: List<RecentIntakeResponse>?,
    val pattern: SummaryPatternResponse?,
)

data class InsightPatternResponse(
    val type: String,
    val timeBand: String,
    val omittedCount: Int,
    val lateCount: Int,
    val fromHour: Int,
    val toHour: Int,
)

data class AdherenceInsightsResponse(
    val periodDays: Int,
    val pattern: InsightPatternResponse,
    val concentration: List<List<Double>>,
    val recommendations: List<String>,
)

interface AnalyticsApiService {
    @GET("api/v1/older-adults/{olderAdultId}/adherence/summary")
    suspend fun getSummary(
        @Path("olderAdultId") olderAdultId: String,
        @Query("days") days: Int,
    ): Response<AdherenceSummaryResponse>

    @GET("api/v1/older-adults/{olderAdultId}/adherence/insights")
    suspend fun getInsights(
        @Path("olderAdultId") olderAdultId: String,
        @Query("days") days: Int,
    ): Response<AdherenceInsightsResponse>
}
