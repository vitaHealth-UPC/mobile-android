package com.vitahealth.tata.preferences.application.commands

import com.vitahealth.tata.preferences.domain.model.TextSizeLevel

data class UpdateTextSizeCommand(
    val userId: String,
    val textSize: TextSizeLevel,
)
