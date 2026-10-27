package com.vitahealth.tata.preferences.infrastructure.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.preferences.application.sync.AccessibilityField
import com.vitahealth.tata.preferences.domain.model.valueobjects.AccessibilityPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.TextSizeLevel
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Uses real DataStore files isolated from the application's accessibility preferences. */
class DataStoreAccessibilityLocalStoreTest {
    private val file =
        File(
            InstrumentationRegistry.getInstrumentation().targetContext.cacheDir,
            "preferences-test-${UUID.randomUUID()}.preferences_pb",
        )
    private var job = SupervisorJob()
    private var scope = CoroutineScope(job + Dispatchers.IO)
    private var data = PreferenceDataStoreFactory.create(scope = scope, produceFile = { file })

    @After
    fun closeIsolatedStore() = runBlocking {
        scope.cancel()
        job.join()
        file.delete()
        Unit
    }

    @Test
    fun oldPendingMarkerRetainsEveryUnspecifiedFieldUntilAcknowledged() = runBlocking {
        val key = "CAREGIVER:old-format"
        data.edit { it[booleanPreferencesKey("owner:$key/pending_sync")] = true }
        val store = DataStoreAccessibilityLocalStore(data, key)
        assertEquals(AccessibilityField.entries.toSet(), store.pendingFields())
        store.saveState(store.current(), setOf(AccessibilityField.TEXT_SIZE))
        assertEquals(setOf(AccessibilityField.TEXT_SIZE), store.pendingFields())
    }

    @Test
    fun exactPendingFieldsPersistWithoutExpandingToAllSettings() = runBlocking {
        val store = DataStoreAccessibilityLocalStore(data, "CAREGIVER:fields")
        store.saveState(
            AccessibilityPreferences.Defaults.copy(highContrast = true),
            setOf(AccessibilityField.HIGH_CONTRAST),
        )
        assertEquals(setOf(AccessibilityField.HIGH_CONTRAST), store.pendingFields())
        assertEquals(
            setOf(AccessibilityField.HIGH_CONTRAST),
            DataStoreAccessibilityLocalStore(data, "CAREGIVER:fields").pendingFields(),
        )
        store.saveState(store.current(), emptySet())
        assertFalse(store.isPendingSync())
    }

    @Test
    fun partitionsValuesAndPendingStateByRoleAndSubject() = runBlocking {
        val caregiver = DataStoreAccessibilityLocalStore(data, "CAREGIVER:same-id")
        val adult = DataStoreAccessibilityLocalStore(data, "OLDER_ADULT:same-id")
        caregiver.saveState(AccessibilityPreferences.Defaults.copy(highContrast = true), true)
        assertTrue(caregiver.current().highContrast)
        assertTrue(caregiver.isPendingSync())
        assertFalse(adult.current().highContrast)
        assertFalse(adult.isPendingSync())
        adult.saveState(AccessibilityPreferences.Defaults.copy(reducedMotion = true), false)
        assertTrue(caregiver.current().highContrast)
        assertFalse(caregiver.current().reducedMotion)
        assertTrue(caregiver.isPendingSync())
    }

    @Test
    fun legacyValuesRemainUnclaimedByAnAuthenticatedOwner() = runBlocking {
        val legacy = DataStoreAccessibilityLocalStore(data, null)
        val owner = DataStoreAccessibilityLocalStore(data, "CAREGIVER:account")
        legacy.saveState(
            AccessibilityPreferences.Defaults.copy(textSize = TextSizeLevel.LARGE),
            true,
        )
        assertEquals(TextSizeLevel.MEDIUM, owner.current().textSize)
        assertFalse(owner.isPendingSync())
        owner.saveState(AccessibilityPreferences.Defaults.copy(highContrast = true), false)
        assertEquals(TextSizeLevel.LARGE, legacy.current().textSize)
        assertTrue(legacy.isPendingSync())
    }

    @Test
    fun valuesAndPendingStatePersistTogetherAcrossStoreReopening() = runBlocking {
        val key = "CAREGIVER:account"
        DataStoreAccessibilityLocalStore(data, key)
            .saveState(
                AccessibilityPreferences.Defaults.copy(
                    textSize = TextSizeLevel.EXTRA_LARGE,
                    highContrast = true,
                ),
                setOf(AccessibilityField.TEXT_SIZE, AccessibilityField.HIGH_CONTRAST),
            )
        val snapshot = data.data.first()
        assertEquals("EXTRA_LARGE", snapshot[stringPreferencesKey("owner:$key/text_size")])
        assertEquals(true, snapshot[booleanPreferencesKey("owner:$key/pending_sync")])
        scope.cancel()
        job.join()
        job = SupervisorJob()
        scope = CoroutineScope(job + Dispatchers.IO)
        data = PreferenceDataStoreFactory.create(scope = scope, produceFile = { file })
        val reopened = DataStoreAccessibilityLocalStore(data, key)
        assertEquals(TextSizeLevel.EXTRA_LARGE, reopened.current().textSize)
        assertTrue(reopened.current().highContrast)
        assertTrue(reopened.isPendingSync())
        assertEquals(
            setOf(AccessibilityField.TEXT_SIZE, AccessibilityField.HIGH_CONTRAST),
            reopened.pendingFields(),
        )
    }

    @Test
    fun darkThemePersistsOnlyForItsOwnerWithoutCreatingRemotePendingFields() = runBlocking {
        val owner = DataStoreAccessibilityLocalStore(data, "CAREGIVER:dark-owner")
        val other = DataStoreAccessibilityLocalStore(data, "OLDER_ADULT:dark-owner")
        owner.saveState(AccessibilityPreferences.Defaults.copy(darkTheme = true), emptySet())
        assertTrue(DataStoreAccessibilityLocalStore(data, "CAREGIVER:dark-owner").current().darkTheme)
        assertFalse(other.current().darkTheme)
        assertFalse(owner.isPendingSync())
    }
}
