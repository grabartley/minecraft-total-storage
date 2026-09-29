# ADR 0018: Two gates decide what may be sorted

- **Status:** Accepted
- **Date:** 2026-08-29

## Context

ADR 0003 put sorting on the server, which means the server decides what a sort request is allowed to touch. Deciding that is harder than it looks.

Most screens a player opens are not interchangeable rows of slots. A furnace's three slots are input, fuel, and output. An anvil's second slot is the sacrifice. A crafting grid's layout is the recipe. Rearranging any of them is not a tidier version of the same thing, it is a different outcome, and in the furnace's case it is fuel in the smelting slot.

Two obvious approaches each fail on their own:

- **An allow list of known container types** refuses every modded chest, which is most of the value. The mod is meant to work with the blocks players already have, and modpack storage is overwhelmingly modded.
- **A deny list of known bad types** cannot name a screen it has never heard of. Every modded machine ships a screen this repository has never seen, and a deny list waves all of them through into exactly the corruption it exists to prevent.

## Decision

Both, as two independent gates, and a screen must pass both.

**The first gate is a deny list of screen handler type identifiers**, in `ScreenSortPolicy`. It names the vanilla screens whose slots mean something: crafting, the three furnaces, brewing, enchanting, anvil, smithing, merchant, beacon, grindstone, loom, stonecutter, cartography, lectern, and the crafter. It is a pure function over an identifier, so it is unit tested rather than gametested. A screen with no registered type is refused here.

**The second gate is structural**, in `SortableSlots`. Every slot the sort would touch must accept two unrelated probe items, hand its contents back to the acting player, and hold a full stack. Every sortable slot must also belong to one backing inventory. Any slot failing any of that refuses the whole screen, not just that slot.

Sorting a container never touches the player's own slots, and sorting the player's inventory covers only its twenty seven main slots, so the hotbar, armour, the offhand, and the crafting grid fall out for free.

`/tmc sort` run with no container open sorts the container block the player is looking at, since an unmodified client cannot open chat over a container screen. It builds the screen handler that block would open for that player, runs both gates against it exactly as if it were open, and closes it again. A block whose screen fails either gate is refused rather than swapped for the inventory, so looking at a furnace never sorts anything, and a block that will not build a screen for that player, a locked container or any block for a spectator, is refused the same way. Building the handler is opening the container, so it has every consequence opening does: the open and close sounds, the lid, the game events a sculk sensor hears, and a loot table rolled on first open. That is deliberate. A sort that reached into a container without opening it would be a way round everything vanilla attaches to opening one.

## Consequences

An unknown modded machine is refused by the structural gate without anyone having to name it, because a machine's input, filter, and output slots restrict what they accept and that is precisely what the probes detect. An unknown modded chest passes both gates and sorts, because a row of interchangeable slots has nothing to restrict.

The vanilla deny list carries the cases the structural gate cannot. A furnace's input slot is an ordinary slot that accepts anything, so the probes pass it; only naming the furnace refuses it. This is the reason the deny list exists at all, and the reason it is worth maintaining rather than replacing with a cleverer heuristic.

Refusing the whole screen when one slot fails is deliberate. A partly sortable machine is a machine whose contents are about to be rearranged into slots that will not hold them, and a sort that gets it half right is worse than one that declines.

Accepted drawback: a modded machine built entirely out of unrestricted full-stack slots sharing one inventory will sort. There is no signal left to distinguish it from a chest, and a mod author who wanted their slots treated as special has the ordinary means to say so. The failure mode is a rearranged machine rather than a corrupted one, since every item stays in the same inventory and the total is conserved.

Accepted drawback: the deny list has to be maintained as vanilla adds screens. A new vanilla screen that is special-purpose but built from unrestricted slots would sort until it is named. That is a known cost of not having an allow list, and the alternative costs every modded container.
