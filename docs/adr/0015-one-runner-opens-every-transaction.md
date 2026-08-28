# ADR 0015: One runner opens every transaction

- **Status:** Accepted
- **Date:** 2026-08-28

## Context

ADR 0001 settled that item movement runs inside a Fabric `Transaction`. It did not settle who opens one.

Left to each feature, withdrawal, deposit, quick stack, sort, and consolidation would each open their own. That is five places to get the same three things right: committing only on success, opening a nested transaction rather than a second outer one, and never opening either off the server thread. The Transfer API's transaction stack is thread local, so an operation running off the server thread quietly gets its own stack while mutating shared world state, and the damage surfaces long after the mistake.

The failure modes are the worst a storage mod has. A half-applied move either duplicates items or destroys them, and a rollback that leaves the index believing a change happened shows players stock that is not there.

## Decision

`TransactionRunner` is the only place in the mod that opens a transaction. Features hand it a `NetworkOperation` and get back an `OperationResult`.

The operation decides, the runner enforces. A result that is not committed rolls back, an operation that throws rolls back and is reported rather than propagated, and a compound move runs its steps through `runNested` so one failing step neither survives alone nor takes the others with it. Every entry point refuses to run off the server thread.

Index bookkeeping goes through `DeferredInvalidations`, a `SnapshotParticipant` that holds invalidations until the commit that makes them true.

## Consequences

Every mutating path gets atomicity, thread safety, and fail-closed error handling without writing any of it, and there is one class to look at when asking whether the mod can duplicate items. Commit and rollback are unit tested against fake storages that participate in real transactions, so the guarantee is checked in a second rather than by a server run.

Returning a result rather than throwing is the awkward part: an operation cannot simply fall out of the middle, and callers carry a result type they must inspect. That is deliberate, because the alternative is a partially applied move escaping as an exception, and it means "did this work" is answered by the type rather than remembered.

Accepted drawbacks: a runtime exception inside an operation is turned into a rolled back result and logged rather than crashing, which keeps a dedicated server up but can hide a bug in a log nobody reads. The server thread check depends on a live server, so the runner refuses to run at all before the server starts, which is correct but surprising the first time it is hit.
