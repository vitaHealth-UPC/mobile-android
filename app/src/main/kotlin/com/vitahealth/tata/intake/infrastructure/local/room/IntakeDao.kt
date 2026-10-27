package com.vitahealth.tata.intake.infrastructure.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

/** Keeps every cache and queue operation within an explicit owner partition. */
@Dao
abstract class IntakeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveDose(dose: CachedIntakeEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertPending(command: PendingConfirmationEntity)

    @Query(
        "UPDATE pending_confirmations SET failure_code = NULL WHERE owner_key = :owner AND intake_id = :id AND failure_code IS NOT NULL"
    )
    abstract suspend fun retryRejected(owner: String, id: String)

    /**
     * A new explicit attempt can retry a rejected intent without changing its original queue time.
     */
    @Transaction
    open suspend fun enqueue(command: PendingConfirmationEntity) {
        insertPending(command)
        retryRejected(command.ownerKey, command.intakeId)
    }

    @Query("SELECT * FROM cached_intakes WHERE owner_key = :owner AND id = :id LIMIT 1")
    abstract suspend fun dose(owner: String, id: String): CachedIntakeEntity?

    @Query(
        "SELECT * FROM cached_intakes WHERE owner_key = :owner AND older_adult_id = :adult AND scheduled_at >= :from AND status = 'PENDING' ORDER BY scheduled_at, id LIMIT 1"
    )
    abstract suspend fun next(owner: String, adult: String, from: Long): CachedIntakeEntity?

    @Query(
        "SELECT * FROM cached_intakes WHERE owner_key = :owner AND older_adult_id = :adult AND scheduled_at >= :from AND scheduled_at < :to ORDER BY scheduled_at, id"
    )
    abstract suspend fun agenda(
        owner: String,
        adult: String,
        from: Long,
        to: Long,
    ): List<CachedIntakeEntity>

    @Query(
        "DELETE FROM cached_intakes WHERE owner_key = :owner AND older_adult_id = :adult AND scheduled_at >= :from AND scheduled_at < :to"
    )
    abstract suspend fun deleteRange(owner: String, adult: String, from: Long, to: Long)

    @Query(
        "DELETE FROM cached_intakes WHERE owner_key = :owner AND older_adult_id = :adult AND scheduled_at >= :from"
    )
    abstract suspend fun clearFuture(owner: String, adult: String, from: Long)

    @Query(
        "SELECT * FROM pending_confirmations WHERE owner_key = :owner AND failure_code IS NULL ORDER BY created_at, intake_id"
    )
    abstract suspend fun pending(owner: String): List<PendingConfirmationEntity>

    @Query("DELETE FROM pending_confirmations WHERE owner_key = :owner AND intake_id = :id")
    abstract suspend fun removePending(owner: String, id: String)

    @Query(
        "UPDATE pending_confirmations SET failure_code = :code WHERE owner_key = :owner AND intake_id = :id"
    )
    abstract suspend fun rejectPending(owner: String, id: String, code: String)

    /** Preserves the old range if any replacement insert fails. */
    @Transaction
    open suspend fun replaceAgenda(
        owner: String,
        adult: String,
        from: Long,
        to: Long,
        doses: List<CachedIntakeEntity>,
    ) {
        require(
            doses.all {
                it.ownerKey == owner &&
                    it.olderAdultId == adult &&
                    it.scheduledAt >= from &&
                    it.scheduledAt < to
            }
        )
        deleteRange(owner, adult, from, to)
        doses.forEach { saveDose(it) }
    }

    /**
     * Commits the server outcome and removes only the matching owner's pending command atomically.
     */
    @Transaction
    open suspend fun acknowledge(dose: CachedIntakeEntity) {
        saveDose(dose)
        removePending(dose.ownerKey, dose.id)
    }
}
