package com.vitahealth.tata.preferences.presentation.navigation

import android.net.Uri

/** Defines the account-scoped entry contract for accessibility settings. */
object AccessibilityDestination {
    const val userIdArgument = "userId"
    val route = "accessibility/{$userIdArgument}"

    /** Encodes the account identifier as a single navigation path segment. */
    fun createRoute(userId: String) = "accessibility/" + Uri.encode(userId)
}
