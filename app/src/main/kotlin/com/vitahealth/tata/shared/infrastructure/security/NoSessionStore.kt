package com.vitahealth.tata.shared.infrastructure.security

import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession

/** Explicit no-persistence adapter used only by callers that do not retain authenticated access. */
object NoSessionStore : SessionStore {
    override fun save(accessToken: String, expiresAt: String) = Unit

    override fun save(accessToken: String, expiresAt: String, owner: SessionOwner) = Unit

    override fun session(): StoredSession? = null

    override fun accessToken(): String? = null

    override fun clear() = Unit
}
