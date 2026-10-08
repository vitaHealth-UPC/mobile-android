package com.vitahealth.tata.identity.infrastructure.local

import android.content.Context
import com.vitahealth.tata.identity.application.OnboardingRepository

class SharedPreferencesOnboardingRepository(
    context: Context,
) : OnboardingRepository {
    private val preferences = context.applicationContext
        .getSharedPreferences("tata_onboarding", Context.MODE_PRIVATE)

    override fun hasSeenOnboarding(): Boolean = preferences.getBoolean(SEEN_KEY, false)

    override fun markOnboardingSeen() {
        preferences.edit().putBoolean(SEEN_KEY, true).apply()
    }

    private companion object {
        const val SEEN_KEY = "seen"
    }
}
