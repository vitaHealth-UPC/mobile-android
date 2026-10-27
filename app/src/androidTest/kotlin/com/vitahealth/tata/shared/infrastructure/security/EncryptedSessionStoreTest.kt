package com.vitahealth.tata.shared.infrastructure.security

import android.content.Context
import android.content.ContextWrapper
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.StoredSession
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Exercises encrypted payload compatibility without reading or clearing the user's session file.
 */
class EncryptedSessionStoreTest {
    private val prefix = "session-test-${UUID.randomUUID()}-"
    private val context =
        object : ContextWrapper(InstrumentationRegistry.getInstrumentation().targetContext) {
            override fun getApplicationContext(): Context = this

            override fun getSharedPreferences(name: String, mode: Int) =
                super.getSharedPreferences(prefix + name, mode)
        }

    @After
    fun clearIsolatedPreferences() {
        context.getSharedPreferences("tata_session", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun ownerAndCredentialsSurviveReopeningWithoutPlaintextStorage() {
        val owner = SessionOwner("account-123", "CAREGIVER")
        EncryptedSessionStore(context).save("private-bearer-token", "2099-01-01T00:00:00Z", owner)

        val restored = EncryptedSessionStore(context).session()
        assertEquals("private-bearer-token", restored?.accessToken)
        assertEquals(owner, restored?.owner)
        val raw =
            context
                .getSharedPreferences("tata_session", Context.MODE_PRIVATE)
                .all
                .values
                .joinToString()
        assertFalse(raw.contains("private-bearer-token"))
        assertTrue(
            context.getSharedPreferences("tata_session", Context.MODE_PRIVATE).contains("payload")
        )
        assertFalse(raw.contains("account-123"))
        assertFalse(restored.toString().contains("private-bearer-token"))
    }

    @Test
    fun observerSeesInitialStateLoginAndExternalLogout() = runBlocking {
        val store = EncryptedSessionStore(context)
        val changes = Channel<StoredSession?>(Channel.UNLIMITED)
        val job =
            launch(Dispatchers.Default) { store.observeSessions().collect { changes.send(it) } }
        try {
            assertNull(withTimeout(5000) { changes.receive() })
            val owner = SessionOwner("observed-account", "CAREGIVER")
            store.save("observed-token", "2099-01-01T00:00:00Z", owner)
            assertEquals(owner, withTimeout(5000) { changes.receive() }?.owner)
            EncryptedSessionStore(context).clear()
            assertNull(withTimeout(5000) { changes.receive() })
        } finally {
            job.cancel()
            changes.close()
        }
    }

    @Test
    fun unboundLegacyPayloadCanBeBoundOnlyForItsExistingToken() {
        val store = EncryptedSessionStore(context)
        store.save("legacy-token", "2099-01-01T00:00:00Z")
        assertEquals("legacy-token", EncryptedSessionStore(context).accessToken())
        assertNull(store.session()?.owner)

        val owner = SessionOwner("adult-123", "OLDER_ADULT")
        assertFalse(store.bindOwner("different-token", owner))
        assertNull(store.session()?.owner)
        assertTrue(store.bindOwner("legacy-token", owner))
        assertEquals(owner, EncryptedSessionStore(context).session()?.owner)
        assertFalse(store.bindOwner("legacy-token", SessionOwner("another-adult", "OLDER_ADULT")))
        assertEquals(owner, store.session()?.owner)
    }

    @Test
    fun oldResponseCannotClearAReplacedEncryptedSession() {
        val store = EncryptedSessionStore(context)
        store.save("old-token", "2099-01-01T00:00:00Z", SessionOwner("old", "CAREGIVER"))
        store.save("new-token", "2099-01-01T00:00:00Z", SessionOwner("new", "CAREGIVER"))
        store.clearIfCurrent("old-token")
        assertEquals("new-token", store.accessToken())
        store.clearIfCurrent("new-token")
        assertNull(store.session())
    }
}
