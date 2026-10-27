package com.vitahealth.tata.preferences.infrastructure.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.preferences.domain.model.valueobjects.AppLanguage
import com.vitahealth.tata.shared.application.session.SessionObserver
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountLanguageRepositoryTest {
    private class FakeStore(initial: StoredSession?) : SessionStore, SessionObserver {
        val sessions = MutableStateFlow(initial)
        override fun observeSessions(): Flow<StoredSession?> = sessions
        override fun session(): StoredSession? = sessions.value
        override fun accessToken(): String? = sessions.value?.accessToken
        override fun save(accessToken: String, expiresAt: String) {}
        override fun save(accessToken: String, expiresAt: String, owner: SessionOwner) {}
        override fun clear() { sessions.value = null }
    }

    @Test fun languagePersistsForItsOwnerWithoutLeakingIntoAnotherAccount() = runBlocking {
        val now = Instant.now()
        val accountA = "locale-a-" + System.nanoTime()
        val accountB = "locale-b-" + System.nanoTime()
        val ownerA = SessionOwner(accountA, "CAREGIVER")
        val ownerB = SessionOwner(accountB, "CAREGIVER")
        val store = FakeStore(StoredSession("token-a", now.plusSeconds(3600), ownerA))
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AccountLanguageRepository(context, store, store)
        assertEquals(AppLanguage.SPANISH, repository.observe().first())
        assertTrue(repository.change(accountA, AppLanguage.ENGLISH) is AppResult.Success)
        assertEquals(AppLanguage.ENGLISH, repository.observe().first())
        assertEquals(
            AppLanguage.ENGLISH,
            AccountLanguageRepository.languageFor(context, ownerA.storageKey),
        )
        store.sessions.value = StoredSession("token-b", now.plusSeconds(3600), ownerB)
        assertEquals(AppLanguage.SPANISH, repository.observe().first())
        assertEquals(
            "RESOURCE_ACCESS_DENIED",
            (repository.change(accountA, AppLanguage.SPANISH) as AppResult.Failure).code,
        )
        store.sessions.value = StoredSession("token-a", now.plusSeconds(3600), ownerA)
        assertEquals(AppLanguage.ENGLISH, repository.observe().first())
    }

    @Test fun noAccountOrExpiredCredentialsCannotPersistLanguage() = runBlocking {
        val expired = Instant.now().minusSeconds(3600)
        val store = FakeStore(StoredSession(
            "expired-token", expired, SessionOwner("expired", "CAREGIVER"),
        ))
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AccountLanguageRepository(context, store, store)
        assertEquals(AppLanguage.SPANISH, repository.observe().first())
        assertEquals(
            "AUTHENTICATION_REQUIRED",
            (repository.change("expired", AppLanguage.ENGLISH) as AppResult.Failure).code,
        )
        store.clear()
        assertEquals(AppLanguage.SPANISH, repository.observe().first())
    }
}
