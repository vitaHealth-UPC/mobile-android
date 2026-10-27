package com.vitahealth.tata.omission.application.services

import com.vitahealth.tata.omission.application.ports.LegacyPushTopicSource
import com.vitahealth.tata.omission.application.ports.PushTopicRemover
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** Retires all known legacy audiences regardless of the currently signed-in owner. */
class LegacyPushTopicRetirement(
    private val source: LegacyPushTopicSource,
    private val transport: PushTopicRemover,
    private val retryMillis: Long = 60_000,
) {
    init {
        require(retryMillis > 0)
    }

    private var job: Job? = null

    @Synchronized
    fun start(scope: CoroutineScope): Job {
        job?.takeIf { it.isActive }
            ?.let {
                return it
            }
        return scope
            .launch {
                val pending = source.topics().toMutableSet()
                while (pending.isNotEmpty()) {
                    for (topic in pending.toList()) {
                        val result = withTimeoutOrNull(10_000) { transport.remove(topic) }
                        currentCoroutineContext().ensureActive()
                        when (result) {
                            is AppResult.Success -> pending.remove(topic)
                            is AppResult.Failure ->
                                if (result.code == "PUSH_NOT_CONFIGURED") return@launch
                            null -> Unit
                        }
                    }
                    if (pending.isNotEmpty()) delay(retryMillis)
                }
            }
            .also { job = it }
    }
}
