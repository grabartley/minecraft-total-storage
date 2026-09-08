# ADR 0032: A commit resyncs everyone who can see what it moved

- **Status:** Accepted
- **Date:** 2026-09-08

## Context

A terminal listing is a picture of a network, and the network changes under it constantly. [ADR 0027](0027-the-listing-is-narrowed-on-the-server.md) already keeps a viewer current against slow drift: once a tick, a network that owes a recompute pushes a fresh page to whoever is looking at it.

That is enough for a hopper filling a chest in the background. It is not enough for another player.

The gap it leaves is exactly one tick wide and it opens at the worst possible moment. Two players stand at one terminal, one of them takes the last stack, and for the rest of that tick the other is looking at a listing offering an item that no longer exists. Their next click is a request the server has to refuse, and per [ADR 0009](0009-clients-send-intent.md) refusing it means answering with a resync anyway. The sweep was always going to send that page; the only question is whether it sends it before or after the click it exists to prevent.

Two more audiences make the same point harder to ignore:

- **Overlapping terminals.** Radii overlap freely, and a chest inside two of them belongs to both networks. A move made through one is a move the other is wrong about, and the second terminal has no way of hearing about it except from the world.
- **Containers open by hand.** A player standing at a chest is looking at slots rather than at a listing. The tick sweep does not know they exist, because it walks terminals. Until now only sorting told them anything, and a withdrawal writing into the chest they are staring at told them nothing at all.

The standards this mod is built to are blunt about this: two players are always the test case, and the acting player is never the only one who needs telling.

## Decision

A committed operation resyncs every viewer of every container it really changed, at the moment it commits.

**Driven by the commit, not by the tick.** The resync is the last thing an operation does, in the same tick and the same call as the move. A sweep can only answer "has this network moved since I last looked", which corrects a listing eventually; the commit knows the answer already.

**What it moved, not what it might have.** An operation knows the containers it could touch and not the ones it did, because how much leaves each is decided inside the transaction. The containers are therefore listened to for the length of the operation, and what announces itself is what changed. A container announces on commit and not before, so an operation that rolled back is witnessed as having touched nothing and resyncs nobody, without that being a case anyone had to write.

**Two audiences, told separately.** A terminal viewer gets a fresh page, because what they are looking at is computed from the index. A player with the container open by hand gets a container sync, because what they are looking at is slots. A player who is both gets both, once each. Half of a double chest counts as the whole of it: the network holds each half as its own container while the player opened one screen over both.

**Nobody else hears anything.** Terminals are narrowed to those whose network really holds a changed container, and container screens to those really showing one. A withdrawal from one chest costs nothing to a player at an unrelated terminal, which is what keeps the cost of a move the size of its audience rather than the size of the server.

**The acting player is left out of the fan-out.** Their own operation answers them directly, including when it refused and moved nothing. Sending them a second page would be the one duplicate the design can see and avoid.

## Consequences

The tick sweep stays. It is still the only thing that answers a hopper, a comparator, or any other writer that is not an operation of this mod's, and it costs one boolean per network per tick when nothing has happened. What it no longer has to be is the mechanism that keeps two players in step.

A move now costs a walk of the running terminals to find the ones holding a changed container. That is bounded by the number of powered terminals rather than by players or by world size, and it only runs for an operation that committed something.

Listening to a network's containers for the length of an operation means registering a listener per container and dropping it afterwards. It is one set insertion each against a registry the network is already registered with, and it is what buys the precision: without it the choice would be between resyncing every container the operation might have touched and threading bookkeeping through every transfer.

The witness must be closed. One left registered would keep hearing about containers for the rest of the server's life and would hold the operation that opened it alive with them, so it is an `AutoCloseable` used in a try-with-resources and never handed out.
