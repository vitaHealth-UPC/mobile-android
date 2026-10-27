# Domain events — Android scope

**Decision (2026-10-24): do not implement a placeholder Android event bus.**

The old `shared/eventing` package (`DomainEvent`, `DomainEventPublisher` and
`NoOpDomainEventPublisher`) had no productive producers or consumers and
was removed during the transversal normalization. The unused `SyncResult`
was removed from `shared` as well.

The backend owns clinical Intake confirmation, omission/escalation,
inventory write transactions and their reliable publication. The
Android client **consumes** HTTP and FCM messages and observes local
synchronization; it must not pretend that those are local domain-event
transitions with exactly-once delivery.

If a future local event becomes warranted, first specify event identity,
owner BC, actor, payload, actual producer/consumer, persistence, cancellation,
failure policy and tests. Then decide whether a narrowly scoped local
mechanism is needed. Do not reintroduce a `NoOp` publisher solely to
match a theoretical DDD diagram.

See ADR-002 and `CODEX-FUNCTIONAL-FIGMA-HANDOFF.md`.
