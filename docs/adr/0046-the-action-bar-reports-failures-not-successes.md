# ADR 0046: The action bar reports failures, not successes

- **Status:** Accepted
- **Date:** 2026-11-26
- **Extends:** [ADR 0040](0040-the-terminal-reports-failures-not-successes.md) from the terminal to the action bar

## Context

[ADR 0040](0040-the-terminal-reports-failures-not-successes.md) stopped the terminal confirming what the player can already see. The action bar kept doing it. Quick stack said how many items it moved, the sort button said "Sorted", "Already sorted" or "Nothing to sort", a cooldown said to wait, and a network job that finished after the terminal closed said it had finished. These messages covered the hotbar area for a few seconds each and told the player nothing they could not see in the containers.

## Decision

**The action bar carries a failure, and nothing else.** A refused gesture, a terminal that will not open, a revoked access, or a sort that could not run still says why. A success, a "nothing to do", a cooldown and a partial result do not reach the action bar. Quick stack plays its sound when it moves something.

Inside the terminal nothing changes: ADR 0040 still decides what the status line shows. Commands still answer in chat.

## Consequences

The action bar is quiet in normal play and speaks only when something went wrong.

A network sort or consolidate that skipped containers and finished after the player closed the terminal no longer says so. The player sees the result when they next open it.

A player pressing quick stack or sort during its cooldown gets no message. Nothing happens, and pressing again after the cooldown works.
