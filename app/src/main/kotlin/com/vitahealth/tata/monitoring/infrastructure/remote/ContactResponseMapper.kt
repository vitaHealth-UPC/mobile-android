package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.domain.model.valueobjects.ContactChannel
import com.vitahealth.tata.monitoring.domain.model.valueobjects.ContactChannelType

/** Validates channel type and value before exposing a contact option. */
fun ContactResponse.toDomain(): ContactChannel =
    ContactChannel(
        type =
            ContactChannelType.entries.firstOrNull { it.name == type }
                ?: throw IllegalArgumentException("unknown contact channel: $type"),
        value = requireNotNull(value?.takeIf { it.isNotBlank() }) { "contact value is missing" },
    )
