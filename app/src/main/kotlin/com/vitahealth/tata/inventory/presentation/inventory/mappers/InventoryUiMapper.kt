package com.vitahealth.tata.inventory.presentation.inventory.mappers

import androidx.annotation.StringRes
import com.vitahealth.tata.R

/** Maps a stable backend/app error code to a localized message resource. */
@StringRes
internal fun inventoryErrorMessageRes(code: String?): Int =
    when (code) {
        "SESSION_REQUIRED",
        "SESSION_CHANGED" -> R.string.inventory_error_session
        "ACCESS_DENIED",
        "CARE_LINK_NOT_AUTHORIZED",
        "CARE_RELATIONSHIP_REQUIRED" -> R.string.inventory_error_access_denied
        "INVALID_QUANTITY" -> R.string.inventory_error_invalid_quantity
        "INVALID_THRESHOLD" -> R.string.inventory_error_invalid_threshold
        "INVALID_LOT" -> R.string.inventory_error_invalid_lot
        "INVALID_MEDICATION_REFERENCE" -> R.string.inventory_error_invalid_medication_reference
        "MEDICATION_NOT_FOUND" -> R.string.inventory_error_medication_not_found
        "MEDICATION_INACTIVE" -> R.string.inventory_error_medication_inactive
        "INVENTORY_NOT_FOUND" -> R.string.inventory_error_inventory_not_found
        "INVENTORY_ALREADY_EXISTS" -> R.string.inventory_error_inventory_already_exists
        "CONCURRENT_UPDATE" -> R.string.inventory_error_concurrent_update
        "REQUEST_FAILED" -> R.string.inventory_error_request_failed
        "NETWORK_UNAVAILABLE" -> R.string.inventory_error_network_unavailable
        "INVALID_RESPONSE" -> R.string.inventory_error_invalid_response
        else -> R.string.inventory_error_unknown
    }
