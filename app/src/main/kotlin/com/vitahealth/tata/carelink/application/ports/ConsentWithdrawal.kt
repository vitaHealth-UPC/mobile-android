package com.vitahealth.tata.carelink.application.ports

import com.vitahealth.tata.shared.common.result.AppResult

/**
 * Withdraws only the relationship of the authenticated adult; never accepts an actor or link from
 * UI input.
 */
fun interface ConsentWithdrawal {
    suspend fun withdraw(olderAdultId: String): AppResult<Unit>
}
