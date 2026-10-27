package com.vitahealth.tata.preferences.infrastructure.sync

import com.vitahealth.tata.shared.application.session.SessionObserver
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/** Reconciles owner-scoped preferences on login, credential renewal and validated reconnection. */
internal class PreferencesSessionRecovery(
    private val sessions: SessionObserver,
    private val online: Flow<Boolean>,
    private val synchronize: suspend (String) -> Unit,
    private val now: () -> Instant = Instant::now,
) {
    private var job: Job? = null

    /** A new session or lost connection cancels the previous reconciliation. */
    @Synchronized
    fun start(scope: CoroutineScope): Job {
        job?.takeIf { it.isActive }
            ?.let {
                return it
            }
        return scope
            .launch {
                combine(sessions.observeSessions(), online.distinctUntilChanged()) {
                        session,
                        connected ->
                        session?.takeIf {
                            connected &&
                                it.owner != null &&
                                it.owner.role != "LINK_SETUP" &&
                                it.expiresAt.isAfter(now())
                        }
                    }
                    .collectLatest { session -> session?.owner?.let { synchronize(it.subjectId) } }
            }
            .also { job = it }
    }
}
