package com.vitahealth.tata.intake.infrastructure.local.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Copies all v1 data into an unbound partition instead of assigning it to the next login. */
object IntakeDatabaseMigration : Migration(1, 2) {
    const val LEGACY_OWNER_KEY = "legacy:unbound"

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE cached_intakes_new (owner_key TEXT NOT NULL, id TEXT NOT NULL, treatment_id TEXT NOT NULL, medication_id TEXT NOT NULL, older_adult_id TEXT NOT NULL, medication_name TEXT NOT NULL, dose TEXT NOT NULL, instructions TEXT NOT NULL, scheduled_at INTEGER NOT NULL, status TEXT NOT NULL, confirmed_at INTEGER, PRIMARY KEY(owner_key, id))"
        )
        db.execSQL(
            "INSERT INTO cached_intakes_new SELECT 'legacy:unbound', id, treatment_id, medication_id, older_adult_id, medication_name, dose, instructions, scheduled_at, status, confirmed_at FROM cached_intakes"
        )
        db.execSQL(
            "CREATE TABLE pending_confirmations_new (owner_key TEXT NOT NULL, intake_id TEXT NOT NULL, channel TEXT NOT NULL, created_at INTEGER NOT NULL, failure_code TEXT, PRIMARY KEY(owner_key, intake_id))"
        )
        db.execSQL(
            "INSERT INTO pending_confirmations_new SELECT 'legacy:unbound', intake_id, channel, created_at, NULL FROM pending_confirmations"
        )
        db.execSQL("DROP TABLE cached_intakes")
        db.execSQL("DROP TABLE pending_confirmations")
        db.execSQL("ALTER TABLE cached_intakes_new RENAME TO cached_intakes")
        db.execSQL("ALTER TABLE pending_confirmations_new RENAME TO pending_confirmations")
        db.execSQL(
            "CREATE INDEX index_cached_intakes_owner_key_older_adult_id_scheduled_at ON cached_intakes(owner_key, older_adult_id, scheduled_at)"
        )
    }
}
