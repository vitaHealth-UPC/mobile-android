package com.vitahealth.tata.identity.presentation.access

import androidx.activity.ComponentActivity

enum class SocialProvider { GOOGLE, FACEBOOK }

/** Platform sign-in port; Firebase evidence is never treated as a Tata session. */
interface SocialCredentialLauncher {
    fun isAvailable(provider: SocialProvider): Boolean
    suspend fun request(provider: SocialProvider, activity: ComponentActivity): String
}
