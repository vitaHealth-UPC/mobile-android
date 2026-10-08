# Offline and synchronization

TS-13 keeps essential intake information available during temporary connectivity loss and defers confirmations that could not reach the backend.

## Local data

The intake module persists synchronized dose data in a local SQLite store. The cache is updated after successful reads for:

- next dose
- weekly agenda
- dose detail

If those requests fail because the network is unavailable, the repositories return the locally available data instead of requiring a live backend response.

## Pending confirmations

A confirmation that cannot reach the backend is stored in a local pending-confirmation queue only when the referenced intake is already available in the local cache. The intake identifier is the queue key, so repeated offline attempts replace the same pending operation instead of creating duplicates.

The UI is not told that the server already confirmed the dose. It receives a pending-sync result until the backend accepts the command.

## Synchronization

WorkManager schedules unique synchronization work with a connected-network constraint and exponential retry. When connectivity returns, pending confirmations are sent through the same backend confirmation endpoint used online.

Successful confirmations update the cache and remove the pending command. Permanent backend rejections such as an unknown or no-longer-confirmable intake also remove the pending command. Temporary network or request failures remain queued for retry.

This implements the TS-13 continuity requirements while preserving the backend as the source of truth.
