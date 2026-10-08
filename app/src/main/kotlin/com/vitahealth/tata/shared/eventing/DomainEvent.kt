package com.vitahealth.tata.shared.eventing

interface DomainEvent {
    val eventName: String
    val occurredAtEpochMillis: Long
}
