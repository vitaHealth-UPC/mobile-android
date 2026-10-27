package com.vitahealth.tata.shared.application.session

import java.time.Instant

/** Atomic credentials and optional legacy subject binding read from encrypted local storage. */
class StoredSession(val accessToken: String, val expiresAt: Instant, val owner: SessionOwner?) {
    /** Keeps bearer credentials out of diagnostic messages. */
    override fun toString(): String = "StoredSession(expiresAt=$expiresAt, owner=$owner)"
}
