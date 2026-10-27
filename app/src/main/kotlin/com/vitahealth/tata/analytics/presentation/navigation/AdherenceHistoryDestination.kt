package com.vitahealth.tata.analytics.presentation.navigation

import android.net.Uri

/** Defines the adult-scoped navigation entry for adherence-history. */
object AdherenceHistoryDestination {
    const val olderAdultIdArgument = "olderAdultId"
    val route = "adherence-history/{$olderAdultIdArgument}"

    /** Encodes the adult identifier as a single route segment. */
    fun createRoute(olderAdultId: String) = "adherence-history/" + Uri.encode(olderAdultId)
}
