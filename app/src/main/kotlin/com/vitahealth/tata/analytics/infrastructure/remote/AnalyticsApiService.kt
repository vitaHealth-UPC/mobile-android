package com.vitahealth.tata.analytics.infrastructure.remote

import java.time.ZoneId
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Retrofit endpoints for adherence summaries and detected patterns. */
interface AnalyticsApiService {
    @GET("api/v1/older-adults/{olderAdultId}/adherence/treatments/{treatmentId}/summary")
    suspend fun getTreatmentSummary(
        @Path("olderAdultId") olderAdultId: String,
        @Path("treatmentId") treatmentId: String,
        @Query("days") days: Int = 7,
        @Query("zone") zone: String = ZoneId.systemDefault().id,
    ): Response<AdherenceSummaryResponse>

    @GET("api/v1/older-adults/{olderAdultId}/adherence/summary")
    suspend fun getSummary(
        @Path("olderAdultId") olderAdultId: String,
        @Query("days") days: Int,
        @Query("zone") zone: String = ZoneId.systemDefault().id,
        @Query("treatmentId") treatmentId: String? = null,
    ): Response<AdherenceSummaryResponse>

    @GET("api/v1/older-adults/{olderAdultId}/adherence/insight")
    suspend fun getInsights(
        @Path("olderAdultId") olderAdultId: String,
        @Query("days") days: Int,
        @Query("zone") zone: String = ZoneId.systemDefault().id,
    ): Response<AdherenceInsightsResponse>
}
