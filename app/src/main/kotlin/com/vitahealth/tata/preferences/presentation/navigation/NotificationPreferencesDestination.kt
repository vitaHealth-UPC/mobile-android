package com.vitahealth.tata.preferences.presentation.navigation

import android.net.Uri

/** Defines the account-scoped entry contract for notification preferences. */
object NotificationPreferencesDestination {
    const val userIdArgument = "userId"
    val route = "notification-preferences/{$userIdArgument}"

    /** Encodes the account identifier as a single navigation path segment. */
    fun createRoute(userId: String) = "notification-preferences/" + Uri.encode(userId)
}
