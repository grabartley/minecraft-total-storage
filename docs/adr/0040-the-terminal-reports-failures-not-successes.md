# ADR 0040: The terminal reports failures, not successes

- **Status:** Accepted
- **Date:** 2026-10-25
- **Supersedes:** the completion summary in [ADR 0036](0036-a-long-operation-commits-one-step-at-a-time.md)

## Context

The terminal used to report every operation in a notice box drawn over the item grid: sorted, gathered, scanned, outlined, stored, refused. Most of those reports told the player what they could already see. A sorted network looks sorted, and the terminal plays a sound when it accepts items. The box covered the rows the player was about to click, and it stayed up until it timed out.

The reports that matter are the ones the player cannot see for themselves: why an item did not come out, why a deposit left things behind, and which containers an operation left alone. Those must still be told inside the terminal, because the action bar renders behind an open screen.

## Decision

**A success says nothing beyond its sound and its visible result. A failure or a partial result is one line in the terminal's status line.**

The status line replaces the "Inventory" label for a few seconds. It shows a short line, and its tooltip holds the full message. It is never drawn over the item grid.

A network sort or consolidate that skipped containers, `Excluded` ones or ones the mod will not rearrange, is a partial result. It reports how many it skipped, because that is the only sign the player gets that `Excluded` did its job.

Confirming a large operation and following its progress happen on the button the player pressed. The first press arms it and its label becomes "Confirm?", a second press inside the ten second confirm window starts the operation, and while it runs the button is an accent Stop button showing the percentage done. Pressing Stop cancels, as ADR 0036 describes.

This covers the terminal screen only. Command and chat output keeps its reports, since a command has no other way to show its result.

## Consequences

The grid is never covered, so the player can keep working while the terminal talks.

A finished operation does not count the containers it sorted or the stacks it merged. Those counts described work the player can see in the containers themselves, so dropping them loses nothing the player needs.

The status line holds one message at a time. A second failure replaces the first, which is acceptable because each failure is about the action the player just took.
