package com.vitahealth.tata.omission.infrastructure.security

import com.vitahealth.tata.shared.application.session.SessionObserver
import com.vitahealth.tata.shared.application.session.SessionOwner
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Removes already displayed clinical notices when their local recipient session ceases to apply.
 */
class OmissionNotificationSessionCleanup(
    private val sessions: SessionObserver,
    private val clearNotifications: () -> Unit,
    private val now: () -> Instant = Instant::now,
) {
    private var job: Job? = null

    /** Clears pre-process notices once and observes owner changes and credential expiration. */
    @Synchronized
    fun start(scope: CoroutineScope): Job {
        job?.takeIf { it.isActive }
            ?.let {
                return it
            }
        return scope
            .launch {
                var initialized = false
                var previous: SessionOwner? = null
                sessions.observeSessions().collectLatest { session ->
                    val owner =
                        session?.owner?.takeIf {
                            it.role != "LINK_SETUP" && session.expiresAt.isAfter(now())
                        }
                    if (!initialized || previous != owner) clearNotifications()
                    initialized = true
                    previous = owner
                    if (owner != null) {
                        val remaining = Duration.between(now(), requireNotNull(session).expiresAt)
                        val waitMillis =
                            runCatching { remaining.toMillis() }.getOrDefault(Long.MAX_VALUE)
                        delay(waitMillis.coerceAtLeast(1))
                        clearNotifications()
                        previous = null
                    }
                }
            }
            .also { job = it }
    }
}
