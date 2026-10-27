package com.vitahealth.tata.intake.infrastructure.local.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/** Server intake snapshot isolated by the authenticated subject that obtained it. */
@Entity(
    tableName = "cached_intakes",
    primaryKeys = ["owner_key", "id"],
    indices = [Index(value = ["owner_key", "older_adult_id", "scheduled_at"])],
)
data class CachedIntakeEntity(
    @ColumnInfo(name = "owner_key") val ownerKey: String,
    val id: String,
    @ColumnInfo(name = "treatment_id") val treatmentId: String,
    @ColumnInfo(name = "medication_id") val medicationId: String,
    @ColumnInfo(name = "older_adult_id") val olderAdultId: String,
    @ColumnInfo(name = "medication_name") val medicationName: String,
    val dose: String,
    val instructions: String,
    @ColumnInfo(name = "scheduled_at") val scheduledAt: Long,
    val status: String,
    @ColumnInfo(name = "confirmed_at") val confirmedAt: Long?,
)
