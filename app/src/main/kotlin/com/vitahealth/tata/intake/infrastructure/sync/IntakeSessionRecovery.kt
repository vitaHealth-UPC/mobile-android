package com.vitahealth.tata.intake.infrastructure.sync

import com.vitahealth.tata.shared.application.session.SessionObserver
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.intake.application.ports.SyncScheduler
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Resumes pending intake work when usable credentials arrive, independently of the current screen.
 */
class IntakeSessionRecovery(
    private val observer: SessionObserver,
    private val scheduler: SyncScheduler,
    private val now: () -> Instant = Instant::now,
) {
    private var job: Job? = null

    /** Owns a single collector for the application lifetime; cancellation releases the observer. */
    @Synchronized
    fun start(scope: CoroutineScope): Job {
        job?.takeIf { it.isActive }
            ?.let {
                return it
            }
        return scope
            .launch {
                var previous: StoredSession? = null
                observer.observeSessions().collect { session ->
                    val eligible =
                        session?.takeIf {
                            it.owner != null &&
                                it.owner.role != "LINK_SETUP" &&
                                it.expiresAt.isAfter(now())
                        }
                    if (eligible == null) {
                        previous = null
                    } else if (
                        eligible.accessToken != previous?.accessToken ||
                            eligible.owner != previous?.owner
                    ) {
                        scheduler.schedule()
                        previous = eligible
                    }
                }
            }
            .also { job = it }
    }
}
