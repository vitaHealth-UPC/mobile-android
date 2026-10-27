package com.vitahealth.tata.preferences.presentation.accessibility.mappers

import com.vitahealth.tata.preferences.presentation.accessibility.viewmodel.AccessibilityMessage
import com.vitahealth.tata.shared.common.result.AppResult

/** Pure mapping of stable application failure codes to localized UI feedback state. */
internal fun accessibilityErrorMessage(failure: AppResult.Failure): AccessibilityMessage =
    when (failure.code) {
        "INVALID_USER_REFERENCE" -> AccessibilityMessage.ErrorUser
        "REQUEST_VALIDATION_FAILED" -> AccessibilityMessage.ErrorRejected
        else -> AccessibilityMessage.ErrorGeneric
    }
