package com.vitahealth.tata.app.navigation

class DeepLinkHandler {
    fun resolve(uri: String): RootDestination? =
        when {
            uri.endsWith("/onboarding") -> RootDestination.Onboarding
            else -> null
        }
}
