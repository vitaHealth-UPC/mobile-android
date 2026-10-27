package com.vitahealth.tata.intake.infrastructure.local

import android.content.Context
import android.database.sqlite.SQLiteException
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.intake.application.commands.ConfirmDoseCommand
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.domain.model.valueobjects.ConfirmationChannel
import com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus
import com.vitahealth.tata.intake.infrastructure.local.room.IntakeDatabase
import com.vitahealth.tata.intake.infrastructure.local.room.IntakeDatabaseMigration
import com.vitahealth.tata.shared.application.session.SessionOwner
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/** Opens real v1 SQLite files with Room and verifies durable intent and account isolation. */
class IntakeDatabaseMigrationTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val databaseName = "intake-migration-test-${UUID.randomUUID()}.db"
    private lateinit var database: IntakeDatabase
    private val ownerA = SessionOwner("account-a", "CAREGIVER")
    private val ownerB = SessionOwner("account-b", "CAREGIVER")
    private val dose =
        DoseDetailReadModel(
            "intake",
            "treatment",
            "medication",
            "adult",
            "Losartán",
            "1",
            "Con agua",
            Instant.ofEpochMilli(1000),
            DoseStatus.PENDING,
        )

    @After
    fun cleanup() {
        if (::database.isInitialized) database.close()
        context.deleteDatabase(databaseName)
    }

    private fun open(): IntakeDatabase {
        database =
            Room.databaseBuilder(context, IntakeDatabase::class.java, databaseName)
                .addMigrations(IntakeDatabaseMigration)
                .build()
        return database
    }

    @Test
    fun legacyDataAndOriginalQueueTimeSurviveWithoutBeingAssignedToTheNextLogin() = runBlocking {
        context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null).use { old ->
            old.execSQL(
                "CREATE TABLE cached_intakes (id TEXT PRIMARY KEY, treatment_id TEXT NOT NULL, medication_id TEXT NOT NULL, older_adult_id TEXT NOT NULL, medication_name TEXT NOT NULL, dose TEXT NOT NULL, instructions TEXT NOT NULL, scheduled_at INTEGER NOT NULL, status TEXT NOT NULL, confirmed_at INTEGER)"
            )
            old.execSQL(
                "CREATE INDEX idx_cached_intakes_adult_schedule ON cached_intakes(older_adult_id, scheduled_at)"
            )
            old.execSQL(
                "CREATE TABLE pending_confirmations (intake_id TEXT PRIMARY KEY, channel TEXT NOT NULL, created_at INTEGER NOT NULL)"
            )
            old.execSQL(
                "INSERT INTO cached_intakes VALUES ('intake','treatment','medication','adult','Losartán','1','Con agua',1000,'PENDING',NULL)"
            )
            old.execSQL("INSERT INTO pending_confirmations VALUES ('intake','TOUCH',123456789)")
            old.version = 1
        }
        val dao = open().intakes()
        val legacy = IntakeDatabaseMigration.LEGACY_OWNER_KEY
        assertEquals("Losartán", dao.dose(legacy, "intake")?.medicationName)
        assertEquals(1000L, dao.dose(legacy, "intake")?.scheduledAt)
        assertEquals(123456789L, dao.pending(legacy).single().createdAt)
        assertEquals("TOUCH", dao.pending(legacy).single().channel)
        for (owner in listOf(ownerA, ownerB)) {
            val store = RoomIntakeLocalStore(dao, owner)
            assertNull(store.findDose("intake"))
            assertTrue(store.pendingConfirmations().isEmpty())
        }
        RoomIntakeLocalStore(dao, ownerA).cacheDose(dose.copy(medicationName = "Updated"))
        assertEquals("Losartán", dao.dose(legacy, "intake")?.medicationName)
    }

    @Test
    fun sameIntakeRemainsIndependentAcrossAccountsIncludingAcknowledgement() = runBlocking {
        val dao = open().intakes()
        val a = RoomIntakeLocalStore(dao, ownerA)
        val b = RoomIntakeLocalStore(dao, ownerB)
        a.cacheDose(dose.copy(medicationName = "Cuenta A"))
        b.cacheDose(dose.copy(medicationName = "Cuenta B"))
        val command = ConfirmDoseCommand(dose.id, ConfirmationChannel.TOUCH)
        a.enqueueConfirmation(command)
        b.enqueueConfirmation(command)
        val first = dao.pending(ownerA.storageKey).single()
        a.enqueueConfirmation(command.copy(channel = ConfirmationChannel.VOICE))
        assertEquals(first, dao.pending(ownerA.storageKey).single())
        a.acknowledgeConfirmation(
            dose.copy(status = DoseStatus.CONFIRMED, confirmedAt = Instant.ofEpochMilli(2000))
        )
        assertTrue(a.pendingConfirmations().isEmpty())
        assertEquals(command, b.pendingConfirmations().single())
        assertEquals("Cuenta B", b.findDose(dose.id)?.medicationName)
        assertEquals(DoseStatus.PENDING, b.findDose(dose.id)?.status)
    }

    @Test
    fun interruptedAcknowledgementRollsBackCacheAndKeepsPendingIntent() = runBlocking {
        val store = RoomIntakeLocalStore(open().intakes(), ownerA)
        store.cacheDose(dose)
        store.enqueueConfirmation(ConfirmDoseCommand(dose.id, ConfirmationChannel.TOUCH))
        database.openHelper.writableDatabase.execSQL(
            "CREATE TRIGGER reject_test_ack BEFORE DELETE ON pending_confirmations BEGIN SELECT RAISE(ABORT, 'test interruption'); END"
        )
        try {
            store.acknowledgeConfirmation(
                dose.copy(status = DoseStatus.CONFIRMED, confirmedAt = Instant.ofEpochMilli(2000))
            )
            fail("Expected transaction interruption")
        } catch (_: SQLiteException) {
            assertEquals(DoseStatus.PENDING, store.findDose(dose.id)?.status)
            assertEquals(1, store.pendingConfirmations().size)
        }
    }
}
