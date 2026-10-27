package com.vitahealth.tata.omission.application.services

import com.vitahealth.tata.omission.application.ports.DevicePushTokenSource
import com.vitahealth.tata.omission.application.ports.DeviceTokenRegistry
import com.vitahealth.tata.shared.application.session.SessionObserver
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

/** Serializes installation updates across login, SDK token rotation and authenticated logout. */
class PushDeviceRegistrationCoordinator(
    private val sessions: SessionStore,
    private val observer: SessionObserver,
    private val tokens: DevicePushTokenSource,
    private val registry: DeviceTokenRegistry,
    private val now: () -> Instant = Instant::now,
    private val retryDelaysMillis: List<Long> = listOf(5_000, 15_000, 60_000),
) {
    init {
        require(retryDelaysMillis.all { it > 0 })
    }

    private val mutex = Mutex()
    private var observerJob: Job? = null
    private var scope: CoroutineScope? = null
    private var latestToken: String? = null
    @Volatile private var signingOutToken: String? = null
    @Volatile private var registrationAttempted = false

    /** Starts one process-owned collector; each login also recovers a token issued before login. */
    @Synchronized
    fun start(scope: CoroutineScope): Job {
        observerJob
            ?.takeIf { it.isActive }
            ?.let {
                return it
            }
        this.scope = scope
        return scope
            .launch { observer.observeSessions().collectLatest { synchronize(it) } }
            .also { observerJob = it }
    }

    /** Keeps SDK callbacks independent of the short-lived Firebase service's scope. */
    fun onNewToken(token: String) {
        scope?.launch {
            mutex.withLock { latestToken = token }
            synchronize(sessions.session())
        }
    }

    private suspend fun synchronize(captured: StoredSession?) {
        if (captured == null || !usable(captured)) return
        var attempt = 0
        while (current(captured)) {
            val retry = synchronizeOnce(captured)
            if (!retry || attempt >= retryDelaysMillis.size) return
            delay(retryDelaysMillis[attempt++])
        }
    }

    /** Releases the mutation lock before backoff so logout and token rotation remain responsive. */
    private suspend fun synchronizeOnce(captured: StoredSession): Boolean =
        mutex.withLock {
            if (!current(captured)) return@withLock false
            val token =
                latestToken
                    ?: when (val result = withTimeoutOrNull(10_000) { tokens.currentToken() }) {
                        is AppResult.Success -> result.value
                        is AppResult.Failure -> return@withLock result.code in retryableCodes
                        null -> return@withLock true
                    }
                    ?: return@withLock false
            currentCoroutineContext().ensureActive()
            if (!current(captured)) return@withLock false
            registrationAttempted = true
            val result = registry.register(token, captured)
            result is AppResult.Failure && result.code in retryableCodes
        }

    private val retryableCodes =
        setOf("NETWORK_UNAVAILABLE", "REQUEST_FAILED", "PUSH_TOKEN_UNAVAILABLE")

    /** Bounds best-effort remote removal so unavailable push transport cannot trap sign-out. */
    suspend fun beforeSignOut() {
        val captured = sessions.session() ?: return
        signingOutToken = captured.accessToken
        if (!registrationAttempted) return
        withTimeoutOrNull(5_000) { mutex.withLock { registry.unregister(captured) } }
    }

    private fun current(captured: StoredSession): Boolean {
        val current = sessions.session() ?: return false
        return usable(current) &&
            current.owner == captured.owner &&
            current.accessToken == captured.accessToken &&
            signingOutToken != captured.accessToken
    }

    private fun usable(session: StoredSession?): Boolean =
        session?.owner != null &&
            session.owner.role != "LINK_SETUP" &&
            session.expiresAt.isAfter(now())
}
