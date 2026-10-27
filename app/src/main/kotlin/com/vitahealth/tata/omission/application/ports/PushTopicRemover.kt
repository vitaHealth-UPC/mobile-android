package com.vitahealth.tata.omission.application.ports

import com.vitahealth.tata.shared.common.result.AppResult

/** Completes only after the transport acknowledges the unsubscribe request. */
fun interface PushTopicRemover {
    suspend fun remove(topic: String): AppResult<Unit>
}
