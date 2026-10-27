package com.vitahealth.tata.intake.infrastructure.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** Opens the existing intake cache with a non-destructive, versioned owner migration. */
@Database(
    entities = [CachedIntakeEntity::class, PendingConfirmationEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class IntakeDatabase : RoomDatabase() {
    abstract fun intakes(): IntakeDao

    companion object {
        const val DATABASE_NAME = "tata_intake_cache.db"
        @Volatile private var instance: IntakeDatabase? = null

        /** Shares one connection owner between UI flows and WorkManager in this process. */
        fun getInstance(context: Context): IntakeDatabase =
            instance
                ?: synchronized(this) {
                    instance
                        ?: Room.databaseBuilder(
                                context.applicationContext,
                                IntakeDatabase::class.java,
                                DATABASE_NAME,
                            )
                            .addMigrations(IntakeDatabaseMigration)
                            .build()
                            .also { instance = it }
                }
    }
}
