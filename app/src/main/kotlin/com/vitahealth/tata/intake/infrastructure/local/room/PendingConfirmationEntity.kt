package com.vitahealth.tata.intake.infrastructure.local.room

import androidx.room.ColumnInfo
import androidx.room.Entity

/** Durable intent retains its original queue time and any definitive rejection for inspection. */
@Entity(tableName = "pending_confirmations", primaryKeys = ["owner_key", "intake_id"])
data class PendingConfirmationEntity(
    @ColumnInfo(name = "owner_key") val ownerKey: String,
    @ColumnInfo(name = "intake_id") val intakeId: String,
    val channel: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "failure_code") val failureCode: String? = null,
)
