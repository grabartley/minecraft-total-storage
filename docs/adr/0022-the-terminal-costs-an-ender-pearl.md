# ADR 0022: The terminal costs an ender pearl, and nothing rarer

- **Status:** Accepted
- **Date:** 2026-09-06

## Context

The terminal is the mod. Everything else in Total Storage is reachable through it, so its recipe is not a balance detail at the edge of the mod, it is the gate on whether anybody sees the mod at all.

That gate can be set wrong in both directions. Cheap enough to craft on day one and the mod replaces the storage problem before the player has one, which removes the very thing it was built to make pleasant. Expensive enough to need the End and the mod becomes an endgame reward, arriving after the player has already built the chest room it was meant to spare them. The second failure is the one storage mods usually make, and it is worse, because a feature nobody reaches may as well not ship.

The recipe also has to read as a terminal. A player who has never installed the mod should be able to look at the grid and guess roughly what it does, because that is how a recipe teaches without a wiki.

## Decision

The terminal is crafted from a chest, four iron ingots, three redstone dust, and one ender pearl, arranged so the parts say what they do.

```
I E I
R C R
I R I
```

The chest at the centre is the thing being extended. The iron corners are the frame. The redstone is the power requirement, which is real: an unpowered terminal does nothing at all, and the recipe says so before the player places the block. The ender pearl on top is the remote access, and it is the only ingredient on the grid that costs the player a trip.

An ender pearl is deliberately the ceiling. It needs a night, an enderman, and some luck, which lands the terminal at the point where a player has a base, has filled a few chests, and has started to resent looking through them. It does not need the End, an eye of ender, netherite, or anything else that pushes the mod past the problem it solves.

The recipe unlocks in the recipe book on picking up an ender pearl rather than on any earlier ingredient, so it appears at the moment it becomes craftable rather than sitting greyed out through the early game.

## Consequences

The terminal is a mid-game goal a player can aim at, and the aiming is part of it: the pearl is a specific errand rather than a grind, and it arrives at roughly the point the storage problem does.

Recipe balance is now a decision with a stated reason, so a later change to it is an argument about the progression tier rather than a taste dispute about ingredients.

Accepted drawbacks: a peaceful-difficulty world has no endermen and therefore no terminal without trading for pearls, which is a real cost paid by a small group of players to keep the recipe legible for everyone else. And a single ender pearl is cheap for a player who already has a farm, so a returning player crafts the terminal immediately rather than progressing toward it. That is the correct failure to accept, since the mod is more useful the more storage a player already has.
