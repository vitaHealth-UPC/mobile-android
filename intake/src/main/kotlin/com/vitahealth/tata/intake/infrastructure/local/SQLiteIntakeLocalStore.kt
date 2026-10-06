package com.vitahealth.tata.intake.infrastructure.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.vitahealth.tata.intake.application.commands.ConfirmDoseCommand
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.domain.model.ConfirmationChannel
import com.vitahealth.tata.intake.domain.model.DoseStatus
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SQLiteIntakeLocalStore(
    context: Context,
) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION,
), IntakeLocalStore {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE cached_intakes (
                id TEXT PRIMARY KEY,
                treatment_id TEXT NOT NULL,
                medication_id TEXT NOT NULL,
                older_adult_id TEXT NOT NULL,
                medication_name TEXT NOT NULL,
                dose TEXT NOT NULL,
                instructions TEXT NOT NULL,
                scheduled_at INTEGER NOT NULL,
                status TEXT NOT NULL,
                confirmed_at INTEGER
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX idx_cached_intakes_adult_schedule
            ON cached_intakes(older_adult_id, scheduled_at)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE pending_confirmations (
                intake_id TEXT PRIMARY KEY,
                channel TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
            """.trimIndent(),
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    ) {
        db.execSQL("DROP TABLE IF EXISTS pending_confirmations")
        db.execSQL("DROP TABLE IF EXISTS cached_intakes")
        onCreate(db)
    }

    override suspend fun cacheDose(dose: DoseDetailReadModel) = withContext(Dispatchers.IO) {
        writeDose(writableDatabase, dose)
    }

    override suspend fun replaceAgenda(
        olderAdultId: String,
        from: Instant,
        to: Instant,
        doses: List<DoseDetailReadModel>,
    ) = withContext(Dispatchers.IO) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(
                "cached_intakes",
                "older_adult_id = ? AND scheduled_at >= ? AND scheduled_at < ?",
                arrayOf(
                    olderAdultId,
                    from.toEpochMilli().toString(),
                    to.toEpochMilli().toString(),
                ),
            )
            doses.forEach { writeDose(db, it) }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override suspend fun clearFutureDoses(
        olderAdultId: String,
        from: Instant,
    ) = withContext(Dispatchers.IO) {
        writableDatabase.delete(
            "cached_intakes",
            "older_adult_id = ? AND scheduled_at >= ?",
            arrayOf(olderAdultId, from.toEpochMilli().toString()),
        )
    }

    override suspend fun findDose(
        intakeId: String,
    ): DoseDetailReadModel? = withContext(Dispatchers.IO) {
        readableDatabase.query(
            "cached_intakes",
            CACHED_COLUMNS,
            "id = ?",
            arrayOf(intakeId),
            null,
            null,
            null,
            "1",
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.toDose() else null
        }
    }

    override suspend fun findNextDose(
        olderAdultId: String,
        from: Instant,
    ): DoseDetailReadModel? = withContext(Dispatchers.IO) {
        readableDatabase.query(
            "cached_intakes",
            CACHED_COLUMNS,
            "older_adult_id = ? AND scheduled_at >= ? AND status = ?",
            arrayOf(
                olderAdultId,
                from.toEpochMilli().toString(),
                DoseStatus.PENDING.name,
            ),
            null,
            null,
            "scheduled_at ASC, id ASC",
            "1",
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.toDose() else null
        }
    }

    override suspend fun findAgenda(
        olderAdultId: String,
        from: Instant,
        to: Instant,
    ): List<DoseDetailReadModel> = withContext(Dispatchers.IO) {
        readableDatabase.query(
            "cached_intakes",
            CACHED_COLUMNS,
            "older_adult_id = ? AND scheduled_at >= ? AND scheduled_at < ?",
            arrayOf(
                olderAdultId,
                from.toEpochMilli().toString(),
                to.toEpochMilli().toString(),
            ),
            null,
            null,
            "scheduled_at ASC, id ASC",
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.toDose())
                }
            }
        }
    }

    override suspend fun enqueueConfirmation(
        command: ConfirmDoseCommand,
    ) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put("intake_id", command.intakeId)
            put("channel", command.channel.name)
            put("created_at", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict(
            "pending_confirmations",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE,
        )
    }

    override suspend fun pendingConfirmations(): List<ConfirmDoseCommand> =
        withContext(Dispatchers.IO) {
            readableDatabase.query(
                "pending_confirmations",
                arrayOf("intake_id", "channel"),
                null,
                null,
                null,
                null,
                "created_at ASC, intake_id ASC",
            ).use { cursor ->
                buildList {
                    val intakeIdIndex = cursor.getColumnIndexOrThrow("intake_id")
                    val channelIndex = cursor.getColumnIndexOrThrow("channel")
                    while (cursor.moveToNext()) {
                        val channel = runCatching {
                            ConfirmationChannel.valueOf(cursor.getString(channelIndex))
                        }.getOrNull() ?: continue
                        add(
                            ConfirmDoseCommand(
                                intakeId = cursor.getString(intakeIdIndex),
                                channel = channel,
                            ),
                        )
                    }
                }
            }
        }

    override suspend fun removePendingConfirmation(
        intakeId: String,
    ) = withContext(Dispatchers.IO) {
        writableDatabase.delete(
            "pending_confirmations",
            "intake_id = ?",
            arrayOf(intakeId),
        )
    }

    private fun writeDose(
        db: SQLiteDatabase,
        dose: DoseDetailReadModel,
    ) {
        val values = ContentValues().apply {
            put("id", dose.id)
            put("treatment_id", dose.treatmentId)
            put("medication_id", dose.medicationId)
            put("older_adult_id", dose.olderAdultId)
            put("medication_name", dose.medicationName)
            put("dose", dose.dose)
            put("instructions", dose.instructions)
            put("scheduled_at", dose.scheduledAt.toEpochMilli())
            put("status", dose.status.name)
            if (dose.confirmedAt == null) {
                putNull("confirmed_at")
            } else {
                put("confirmed_at", dose.confirmedAt.toEpochMilli())
            }
        }
        db.insertWithOnConflict(
            "cached_intakes",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE,
        )
    }

    private fun Cursor.toDose(): DoseDetailReadModel {
        val confirmedAtIndex = getColumnIndexOrThrow("confirmed_at")
        return DoseDetailReadModel(
            id = getString(getColumnIndexOrThrow("id")),
            treatmentId = getString(getColumnIndexOrThrow("treatment_id")),
            medicationId = getString(getColumnIndexOrThrow("medication_id")),
            olderAdultId = getString(getColumnIndexOrThrow("older_adult_id")),
            medicationName = getString(getColumnIndexOrThrow("medication_name")),
            dose = getString(getColumnIndexOrThrow("dose")),
            instructions = getString(getColumnIndexOrThrow("instructions")),
            scheduledAt = Instant.ofEpochMilli(
                getLong(getColumnIndexOrThrow("scheduled_at")),
            ),
            status = DoseStatus.valueOf(
                getString(getColumnIndexOrThrow("status")),
            ),
            confirmedAt = if (isNull(confirmedAtIndex)) {
                null
            } else {
                Instant.ofEpochMilli(getLong(confirmedAtIndex))
            },
        )
    }

    private companion object {
        const val DATABASE_NAME = "tata_intake_cache.db"
        const val DATABASE_VERSION = 1

        val CACHED_COLUMNS = arrayOf(
            "id",
            "treatment_id",
            "medication_id",
            "older_adult_id",
            "medication_name",
            "dose",
            "instructions",
            "scheduled_at",
            "status",
            "confirmed_at",
        )
    }
}
