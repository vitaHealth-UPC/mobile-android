package com.vitahealth.tata.preferences.application.ports

import com.vitahealth.tata.preferences.domain.model.valueobjects.AppLanguage
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.flow.Flow

/** Owner-partitioned local app language. No health data or credentials are stored. */
interface ApplicationLanguageRepository {
    fun observe(): Flow<AppLanguage>
    suspend fun change(userId: String, language: AppLanguage): AppResult<AppLanguage>
}
