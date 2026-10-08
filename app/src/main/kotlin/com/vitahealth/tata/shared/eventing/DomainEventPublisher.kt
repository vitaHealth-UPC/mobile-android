package com.vitahealth.tata.shared.eventing

fun interface DomainEventPublisher {
    suspend fun publish(event: DomainEvent)
}

object NoOpDomainEventPublisher : DomainEventPublisher {
    override suspend fun publish(event: DomainEvent) = Unit
}
