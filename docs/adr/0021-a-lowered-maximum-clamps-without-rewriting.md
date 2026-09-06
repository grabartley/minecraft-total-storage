# ADR 0021: A lowered maximum clamps a terminal without rewriting it

- **Status:** Accepted
- **Date:** 2026-09-05

## Context

Each terminal owns its radius, and the server owns a maximum that no terminal may exceed. Both move: a player retunes their storage room, and an operator retunes the cap when the server starts feeling the cost.

A radius large enough to cover thousands of block entities turns every scan into a tick spike, so the cap has to be enforced rather than advised. That leaves the question of what enforcement does to a terminal that is already configured above the new cap, and whether the answer is the same for the two ways a terminal can end up over the line.

The obvious implementation stores one number and clamps it on write. That is correct for the player who asks for too much, and quietly destructive for everyone else: lowering the maximum would rewrite every terminal on the server, and raising it again would give none of them their reach back. An operator experimenting with the cap for one evening would have permanently shrunk every storage room on the server, with nothing left on disk to say what any of them had asked for.

## Decision

The two cases are treated differently, because they are different.

A radius written explicitly, from a command or an interface, is clamped as it is written. The value that lands on the terminal is one the server permits, and the player is told the clamp happened rather than being left to wonder why the number they typed is not the number they got.

A radius the terminal already holds is never rewritten by a change to the maximum. The clamp is applied where a scan reads the radius, so a terminal configured above the cap runs at the cap and asks for what it always asked for. Raising the maximum hands the reach straight back.

A change to the maximum still reaches terminals that are already running: each one is rebuilt against the region it should now be walking, and a terminal whose region did not move is left alone. Enforcement is therefore immediate rather than waiting for the next time somebody happens to cut power, and lowering the cap costs one scan per affected terminal rather than one per terminal on the server.

## Consequences

Two numbers describe a terminal's reach rather than one: what it asked for, and what it is allowed. Every reader has to be clear about which it wants, and the pair is modelled explicitly so the distinction is not carried around in an operator's head.

An operator can lower the maximum, see the effect, and undo it, which is the only way a cap gets tuned in practice. A player whose terminal is being held down keeps their configured radius, so the mod is not silently editing player state on an operator's behalf.

Accepted drawbacks: a terminal can be configured to a radius it does not currently reach, and the two numbers can disagree indefinitely. Anything reporting a radius has to report both when they differ, or it will look like the setting was ignored. Lowering the maximum on a server with many powered terminals costs one scan for each terminal that was over the line, which is a burst of work at the moment an operator asks for the cap to bind, rather than no work and no cap.
