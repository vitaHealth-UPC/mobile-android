package com.vitahealth.tata.analytics.presentation.navigation

import android.net.Uri

/** Defines the adult-scoped navigation entry for adherence-recommendations. */
object AdherenceRecommendationsDestination {
    const val olderAdultIdArgument = "olderAdultId"
    val route = "adherence-recommendations/{$olderAdultIdArgument}"

    /** Encodes the adult identifier as a single route segment. */
    fun createRoute(olderAdultId: String) = "adherence-recommendations/" + Uri.encode(olderAdultId)
}
