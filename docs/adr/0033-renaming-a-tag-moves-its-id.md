# ADR 0033: Renaming a tag moves its id

- **Status:** Accepted
- **Date:** 2026-09-09

## Context

A custom tag carries an id and a display name. The id is the name normalised, and it is what everything else addresses the tag by: the set stores tags under it, `NetworkTagSet.tagIdsFor` returns ids, the filter column sends ids in the listing request, and the search box's `#tag` prefix matches against ids.

When a player renames "base build" to "castle", there are two ways to honour it. Keep the id and move only the display name, so nothing addressing the tag has to change. Or move the id too, so the tag is addressed by what it is now called.

Keeping the id is cheaper and obviously safe: no reference can dangle because no reference changes. The cost is that the two names drift apart permanently. The column reads `castle`, the tooltip reads `castle`, and `#castle` finds nothing while `#base build` finds everything, because the id nobody can see is still the old one. That is a bug the player cannot diagnose, and it gets worse with every rename.

## Decision

Renaming a custom tag moves its id as well as its display name. The tag is rewritten under the new id carrying the same item assignments, and the old id is dropped.

The new name is validated exactly as a new tag's name is, against the same rule: not blank, within the length limit, no punctuation the query language uses, not colliding with a preset, and not colliding with another tag on the network. A tag renamed to the name it already has is not a collision with itself.

## Consequences

The name a player sees is the name that works. `#castle` finds the tag called Castle, whatever it was called yesterday, and the column, the tooltip, the assignment prompt and the query language all agree.

This is only safe because a tag id is referenced from exactly one place that outlives the operation: the tag's own entry in the set. Item assignments live inside the tag and travel with it. Nothing else persists a tag id, so moving it cannot leave a dangling reference.

Accepted drawbacks:

- A filter a player has active on a tag being renamed by someone else at the same terminal stops matching. The screen drops active tags the network no longer offers on its next frame rather than filtering on an id nobody has, so this costs a cleared filter rather than an empty grid.
- If a later feature persists tag ids outside the tag itself, that feature has to migrate them on rename. Anything that stores a tag id, rather than resolving it each time, inherits this obligation.
