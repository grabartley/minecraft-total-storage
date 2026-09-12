# ADR 0036: A long operation commits one step at a time

- **Status:** Accepted
- **Date:** 2026-09-12

## Context

Sorting a whole network and consolidating one are the two operations that touch every container at once. A forty-container network is thousands of slot reads and hundreds of stack moves, and doing all of it inside one tick is exactly the server stall the mod exists to avoid. The product requirements say so directly: the work is budgeted per tick and continues across ticks rather than blocking the server, and it can be cancelled while it runs.

That collides with how every other mutation in this mod is written. [ADR 0015](0015-one-runner-opens-every-transaction.md) puts one runner in charge of opening transactions, and every operation so far has been one transaction that either commits whole or rolls back whole. A Fabric transaction cannot be held open across a tick boundary: the transaction stack is thread-local and asserted to be closed by the time the tick ends. There is no "open it now, commit it in four ticks" available at any price.

So a multi-tick operation has to choose what atomicity it actually offers, and say so.

## Decision

**A long operation is a queue of steps, and each step is its own transaction.**

A step is one unit of work with a meaning of its own: gather one item variant into as few containers as it fits, or sort one container. `movesPerTickBudget` steps run per tick. Each step opens a transaction through the one runner, commits if it moved what it intended, and rolls back on any failure without touching the steps before or after it.

Cancelling, whether by clicking the action again or by the terminal losing power, stops the queue. The step in flight finishes or rolls back on its own terms; nothing further is started.

## Consequences

**Nothing is ever lost or duplicated.** Every item that moves does so inside a committed transaction, and a step that cannot complete rolls back to exactly the arrangement it found. Conservation is asserted over the whole test world in the gametests for both operations, including the cancellation and broken-container cases.

**A cancelled operation is not undone.** The containers already sorted stay sorted, and the stacks already gathered stay gathered. This is the drawback the decision accepts, and it is the honest reading of "cancel": stop doing more, rather than replay everything backwards. A player who cancels half way through a consolidation has a partly consolidated network, which is a valid state made of valid containers, not a corrupted one.

**A step reads the world fresh.** Roles, membership, and the index are resolved per step rather than captured when the operation started, so a container broken, excluded, or unloaded mid-run is simply skipped by the steps that follow. This is what makes a forty-container operation survive a player mining a chest while it runs.

**The unit of atomicity is visible in the summary.** The completion message counts containers sorted, stacks merged, and containers skipped, which are step outcomes. There is no whole-operation rollback to report because there is no whole-operation transaction to roll back.

A single container sort is unaffected: it is one step, so it keeps the all-or-nothing guarantee it always had.
