package com.vitahealth.tata.shared.sync

sealed interface SyncResult {
    data object Success : SyncResult
    data class Retry(val reason: String) : SyncResult
    data class Failure(val reason: String) : SyncResult
}
