package com.vitahealth.tata.app.navigation.deeplink

import com.vitahealth.tata.app.navigation.destinations.RootDestination
class DeepLinkHandler {
    fun resolve(uri: String): RootDestination? =
        when {
            uri.endsWith("/onboarding") -> RootDestination.Onboarding
            else -> null
        }
}
