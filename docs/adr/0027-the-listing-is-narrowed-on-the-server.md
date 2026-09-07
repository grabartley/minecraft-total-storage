# ADR 0027: The listing is narrowed on the server and answered by revision

- **Status:** Accepted
- **Date:** 2026-09-07

## Context

ADR 0025 decided that the network is not modelled as slots and that the listing is paged. That leaves two questions it did not answer: where searching and filtering happen, and what a client does when answers arrive out of order.

Filtering on the client is the cheaper-looking option, and it is what a cached list invites: send the rows once, let the screen narrow them as the player types. It only works if the client already holds every row it might need to narrow, which for a network of thousands of variants is exactly the payload paging exists to avoid. A client can only ever filter what it was sent.

Answers also do not arrive in the order they were asked for. A player dragging a scrollbar has several requests in flight at once, and the network is free to deliver them in any order. Nothing in a page says when it was asked for, so a slower answer that lands after a faster one looks exactly like the newest state of the world.

## Decision

A request carries what the player did: the rows they scrolled to, the text they typed, and the tags they clicked. The server parses the search, evaluates it and the tags against the live index, and returns only the window that matched, along with how many rows matched in total so a scrollbar can be drawn.

Every response is stamped with a revision that increases per screen. A client draws a page only if it is newer than the one it is showing, and only if it belongs to the screen that is open.

A page is capped at a fixed number of rows on both sides of the wire. A client asking for more is answered with the cap rather than refused, so no response can be made larger by asking for a larger one.

Changes are pushed rather than polled. At the end of each tick, a terminal somebody is watching whose index owes a recompute is sent the page its viewers are already asking for. Reading the index to build that page is what clears the debt, so a network nobody changed costs one boolean per tick.

## Consequences

What a listing costs is set by the size of the grid rather than by the size of the network, in both directions: the client sends criteria rather than receiving rows to apply them to, and the server sends one window rather than a list to be narrowed.

A player sees a container fill up behind them without touching anything, and a tick's worth of changes to one terminal costs one page however many times it was written to.

Search behaviour cannot drift between what the server holds and what a screen shows, because there is only one implementation of it and the screen never runs it.

Accepted drawback: every keystroke in the search box is a request. That is the same cost as a scroll and bounded the same way, but it does mean a terminal is chattier while somebody is typing than one filtering a cached list would be.

Accepted drawback: the client cache can lag the network by up to a tick, so a row can briefly show a count that is no longer true. Nothing is decided from it: per ADR 0009 a withdrawal names what it wants and the server resolves it against the containers, so a stale row costs a refused request rather than an item that never existed.
