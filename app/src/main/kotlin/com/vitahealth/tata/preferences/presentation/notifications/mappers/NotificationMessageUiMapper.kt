package com.vitahealth.tata.preferences.presentation.notifications.mappers

import com.vitahealth.tata.preferences.presentation.notifications.viewmodel.NotificationMessage
import com.vitahealth.tata.shared.common.result.AppResult

/** Pure mapping of stable application failure codes to localized UI feedback state. */
internal fun notificationErrorMessage(failure: AppResult.Failure): NotificationMessage =
    when (failure.code) {
        "INVALID_USER_REFERENCE" -> NotificationMessage.ErrorUser
        "REQUEST_VALIDATION_FAILED",
        "DUPLICATE_CHANNEL" -> NotificationMessage.ErrorRejected
        "NETWORK_UNAVAILABLE" -> NotificationMessage.ErrorOffline
        else -> NotificationMessage.ErrorGeneric
    }
