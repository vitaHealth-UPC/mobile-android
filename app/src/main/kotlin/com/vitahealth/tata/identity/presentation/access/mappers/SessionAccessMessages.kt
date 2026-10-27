package com.vitahealth.tata.identity.presentation.access.mappers

import androidx.annotation.StringRes
import com.vitahealth.tata.R

/** Maps stable error codes to localized Android resources shared by PIN and email access. */
@StringRes
internal fun sessionAccessMessageRes(code: String): Int =
    when (code) {
        "PIN_LOCKED",
        "PIN_TEMPORARILY_BLOCKED" -> R.string.access_error_pin_locked
        "INVALID_PIN",
        "INVALID_CREDENTIALS",
        "PIN_INCORRECT" -> R.string.access_error_credentials
        "ACCOUNT_NOT_ACTIVE",
        "CONSENT_REQUIRED" -> R.string.access_error_consent
        "NETWORK_UNAVAILABLE" -> R.string.access_error_network
        "REQUIRED_FIELDS" -> R.string.access_error_required_fields
        "AUTHENTICATION_REQUIRED" -> R.string.access_error_authentication_required
        "SESSION_CHANGED" -> R.string.access_error_session_changed
        "OLDER_ADULT_SESSION_REQUIRED" -> R.string.access_error_adult_session
        "SOCIAL_UNAVAILABLE" -> R.string.access_error_social_unavailable
        "INVALID_SOCIAL_CREDENTIAL" -> R.string.access_error_social_credential
        "SOCIAL_EMAIL_REQUIRED" -> R.string.access_error_social_email
        "SOCIAL_LINK_REQUIRED", "SOCIAL_LINK_CONFLICT" -> R.string.access_error_social_link
        "SOCIAL_PROVIDER_UNAVAILABLE" -> R.string.access_error_social_provider
        else -> R.string.access_error_generic
    }
