# ADR 0028: Results are ranked before they are paged

- **Status:** Accepted
- **Date:** 2026-09-07

## Context

ADR 0013 left ordering to the terminal grid, on the grounds that whether a row belongs in the results and how well it fits are different questions and only the first one is the evaluator's. ADR 0027 then decided that the results are narrowed on the server and the client is sent one window of them.

Those two together leave the grid unable to do the job it was given. A page is a slice of an order that already exists, so a client sorting the rows it holds would rank one screenful against itself, and the best answer to a query would sit on whichever page it happened to land on. Ranking has to happen where the results are still whole, which is the same side that narrowed them.

There is also a question of what strength means for a query of several terms. Every name term has to match for a row to be shown at all, so a row that answered one term outright and barely scraped the second is not as good as one that answered both.

## Decision

Search results are ranked on the server, between filtering and paging. Rows are ordered by how strongly they answered the query: the whole string, then a prefix, then a substring, then a subsequence.

Acceptance and ranking read the same strength. Whether a row matches is whether its strength is anything other than none, so a row cannot be accepted under one rule and ordered under another that disagrees, and one pass over the index answers both.

A query of several name terms is ranked by its weakest term. Rows of equal strength keep the index's own order, which is by registry identifier.

Only bare text is ranked. A tag or a mod identifier term either holds of a row or does not, and a query carrying no bare text leaves the index order untouched.

## Consequences

Typing the start of a name puts that name first, whatever page it would otherwise have landed on, and the ordering is the same for every client because there is only one implementation of it.

Ties keeping index order is what makes the grid stable while somebody types. A character that narrows nothing leaves the rows where they were rather than reshuffling equals, so the item a player is reaching for does not move out from under the cursor.

Narrowing and ordering are one pass over the index rather than two. Reading an item as plain data means reaching for the registry, and a search that decided membership and then decided position would pay that twice for every row on every keystroke.

Accepted drawback: this refines ADR 0013, which said the grid would do the ordering. It does not change how a query is accepted, only where the order of what it accepted is decided, and that record carries a pointer here.

Accepted drawback: ranking is by match strength alone. How much of a thing the network holds does not lift it, so a stack of one exact match still outranks a thousand of a fuzzy one. That is the right answer for a search box and the wrong one for a player who wanted the big pile, and the sort control is where that preference will live.
