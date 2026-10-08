package com.vitahealth.tata.monitoring.application.queries

data class GetFollowUpNotesQuery(
    val caregiverId: String,
    val olderAdultId: String,
)
