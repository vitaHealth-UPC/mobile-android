package com.vitahealth.tata.shared.application

interface SessionStore {
    fun save(accessToken: String, expiresAt: String)
    fun accessToken(): String?
    fun clear()
}

object NoSessionStore : SessionStore {
    override fun save(accessToken: String, expiresAt: String) = Unit
    override fun accessToken(): String? = null
    override fun clear() = Unit
}
