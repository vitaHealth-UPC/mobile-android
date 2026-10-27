package com.vitahealth.tata.shared.application.session

/** Atomically stores credentials with their backend-validated local data owner. */
interface SessionStore {
    /**
     * Retains the legacy unbound format until the current-session endpoint confirms its subject.
     */
    fun save(accessToken: String, expiresAt: String)

    /** Replaces credentials and their owner as one persisted operation. */
    fun save(accessToken: String, expiresAt: String, owner: SessionOwner)

    /** Returns one consistent snapshot; absent owner denotes a legacy unbound session. */
    fun session(): StoredSession?

    /** Returns usable credentials without exposing an expired stored token. */
    fun accessToken(): String?

    /** Removes credentials and their owner together. */
    fun clear()

    /** Compares complete credential snapshots without relying on token usability filtering. */
    fun isCurrent(expected: StoredSession?): Boolean =
        synchronized(this) {
            val current = session()
            current?.accessToken == expected?.accessToken &&
                current?.owner == expected?.owner &&
                current?.expiresAt == expected?.expiresAt
        }

    /** Installs onboarding credentials only if no login or logout superseded their request. */
    fun replaceIfCurrent(expected: StoredSession?, replacement: StoredSession): Boolean =
        synchronized(this) {
            if (!isCurrent(expected)) return@synchronized false
            val owner = requireNotNull(replacement.owner)
            save(replacement.accessToken, replacement.expiresAt.toString(), owner)
            true
        }

    /** Attaches a verified subject only while the response still belongs to the current token. */
    fun bindOwner(expectedToken: String, owner: SessionOwner): Boolean =
        synchronized(this) {
            val current = session() ?: return@synchronized false
            if (
                current.accessToken != expectedToken ||
                    (current.owner != null && current.owner != owner)
            ) {
                return@synchronized false
            }
            save(current.accessToken, current.expiresAt.toString(), owner)
            true
        }

    /** Prevents a delayed unauthorized response from clearing a newer login. */
    fun clearIfCurrent(expectedToken: String) =
        synchronized(this) { if (accessToken() == expectedToken) clear() }
}
