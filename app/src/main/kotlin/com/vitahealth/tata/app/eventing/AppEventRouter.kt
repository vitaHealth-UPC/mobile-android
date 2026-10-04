package com.vitahealth.tata.app.eventing

import com.vitahealth.tata.shared.eventing.DomainEvent
import com.vitahealth.tata.shared.eventing.DomainEventPublisher

class AppEventRouter(
    private val publisher: DomainEventPublisher,
) {
    suspend fun route(event: DomainEvent) {
        publisher.publish(event)
    }
}
