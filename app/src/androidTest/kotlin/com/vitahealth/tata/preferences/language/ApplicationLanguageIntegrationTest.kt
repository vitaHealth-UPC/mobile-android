package com.vitahealth.tata.preferences.language

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vitahealth.tata.R
import com.vitahealth.tata.app.withTataAppLocale
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.preferences.domain.model.valueobjects.AppLanguage
import com.vitahealth.tata.preferences.infrastructure.local.AccountLanguageRepository
import com.vitahealth.tata.shared.application.session.SessionObserver
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApplicationLanguageIntegrationTest {
    private class TestSessions : SessionStore, SessionObserver {
        val current = MutableStateFlow<StoredSession?>(null)
        override fun observeSessions() = current
        override fun session() = current.value
        override fun accessToken() = current.value?.accessToken
        override fun save(accessToken: String, expiresAt: String) {
            current.value = StoredSession(accessToken, Instant.parse(expiresAt), null)
        }
        override fun save(accessToken: String, expiresAt: String, owner: SessionOwner) {
            current.value = StoredSession(accessToken, Instant.parse(expiresAt), owner)
        }
        override fun clear() { current.value = null }
    }

    @Test fun languageIsPersistedPerVerifiedOwnerAndResetsBetweenAccounts() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = TestSessions()
        val first = SessionOwner("locale-fixture-" + UUID.randomUUID(), "CAREGIVER")
        val other = SessionOwner("locale-other-" + UUID.randomUUID(), "CAREGIVER")
        val expiry = Instant.now().plusSeconds(3600).toString()
        store.save("fixture-A", expiry, first)
        val repository = AccountLanguageRepository(context, store, store)
        assertEquals(AppLanguage.SPANISH, withTimeout(5000) {
            repository.observe().first()
        })
        assertTrue(repository.change(first.subjectId, AppLanguage.ENGLISH)
            is AppResult.Success)
        assertEquals(AppLanguage.ENGLISH,
            AccountLanguageRepository.languageFor(context, first.storageKey))
        assertEquals(AppLanguage.ENGLISH, withTimeout(5000) {
            repository.observe().first { it == AppLanguage.ENGLISH }
        })
        store.save("fixture-B", expiry, other)
        assertEquals(AppLanguage.SPANISH, withTimeout(5000) {
            repository.observe().first { it == AppLanguage.SPANISH }
        })
        assertEquals("RESOURCE_ACCESS_DENIED",
            (repository.change(first.subjectId, AppLanguage.ENGLISH)
                as AppResult.Failure).code)
        store.save("fixture-A", expiry, first)
        assertEquals(AppLanguage.ENGLISH, withTimeout(5000) {
            repository.observe().first { it == AppLanguage.ENGLISH }
        })
        assertTrue(repository.change(first.subjectId, AppLanguage.SPANISH)
            is AppResult.Success)
        store.clear()
        assertEquals(AppLanguage.SPANISH, withTimeout(5000) {
            repository.observe().first { it == AppLanguage.SPANISH }
        })
    }

    @Test fun resourceContextUsesTheAccountLanguageForAdultHome() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val en = base.withTataAppLocale("en")
        val es = base.withTataAppLocale("es-419")
        assertEquals("Next dose", en.getString(R.string.home_next_dose))
        assertEquals("Próxima toma", es.getString(R.string.home_next_dose))
        assertEquals("Language", en.getString(R.string.accessibility_language_title))
        assertEquals("Idioma", es.getString(R.string.accessibility_language_title))
    }

    @Test fun expiredSessionCannotChangeLanguage() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = TestSessions()
        val owner = SessionOwner("locale-expired-" + UUID.randomUUID(), "OLDER_ADULT")
        store.save("fixture-expired", Instant.now().minusSeconds(120).toString(), owner)
        val repository = AccountLanguageRepository(context, store, store)
        assertEquals("AUTHENTICATION_REQUIRED",
            (repository.change(owner.subjectId, AppLanguage.ENGLISH)
                as AppResult.Failure).code)
        assertEquals(AppLanguage.SPANISH, withTimeout(5000) {
            repository.observe().first()
        })
    }
}
