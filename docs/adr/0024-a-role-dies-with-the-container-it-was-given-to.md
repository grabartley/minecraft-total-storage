# ADR 0024: A role dies with the container it was given to

- **Status:** Accepted
- **Date:** 2026-09-06

## Context

ADR 0004 gave every connected container a role, stored on the terminal. Storing it on the terminal raised a question that ADR did not answer: what is the key?

A chest has no identity that survives being broken. It carries no persistent id, its block entity is discarded on removal, and a chest placed back on the same spot is indistinguishable from the one that was there before. The only key available is the position, which means a role outlives the container that earned it.

That is dangerous in exactly the direction ADR 0004 exists to guard. `Excluded` is a promise to a player that the network will not touch a container they curated. If the role sits on the position, the next chest placed on that spot inherits a promise nobody made about it, and it silently stops receiving deposits for reasons its owner cannot see. A hopper feeding a replaced chest that quietly refuses everything is a bug report nobody can write.

The mirror case is just as bad. A player who breaks and replaces a container to move its contents, and finds the network has forgotten a filter set they spent ten minutes building, will not thank the mod for being careful.

## Decision

Roles are keyed by position, and a container's assignment is forgotten the moment the world says that container has been removed.

Removal is the only event that forgets. ADR 0014 established that Fabric's block entity unload event fires on breaking and replacing, and that chunk unloading is a separate event; only the first is heard here. A container that leaves with its chunk and returns is the same container to its network and keeps everything decided about it.

A `Filtered` container's accepted tags and items are stored alongside its role and die with it, and a role that cannot use a filter never carries one, so demoting a filtered container drops what it accepted rather than leaving it stored where nothing reads it.

Forgetting is driven by the running network, so a terminal with no power hears nothing. Per ADR 0007 that is the whole of what unpowered means.

## Consequences

A replacement container starts general, which is the safe direction: it receives deposits and is sorted like any other container until somebody says otherwise. Being wrongly ordinary costs a player one reassignment; being wrongly excluded costs them a diagnosis.

The rule is the same one ADR 0014 already applies to membership, so there is one story about what a container leaving means rather than two. Nothing new has to be listened for, and no scan is spent.

Accepted drawback: a container broken and replaced while its terminal is unpowered keeps its old role, because nothing was running to hear it go. Closing that would mean either listening for every block entity removal in the world on behalf of terminals that are switched off, or force-loading terminals, and neither is worth what it buys. An operator who suspects a stale role can strip it by reassigning the container.

Accepted drawback: breaking one half of a double chest forgets the pair's role, for the same reason ADR 0014 drops the pair from the network. The role does not come back when the surviving half rejoins.
