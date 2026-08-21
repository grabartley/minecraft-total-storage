# Engineering Standards

These standards apply to every mod in this family: Dogs Unleashed, More Arrows, More Doors, Teleport Effects, and Too Many Chests. They exist because the same decisions kept being re-made from memory.

Dogs Unleashed is the reference implementation. When this document and that codebase disagree, the codebase is probably right and this document needs updating.

## Multiplayer First

Every mod is designed for a dedicated server with many players, and single player is treated as the special case where the server happens to have one client. This is the opposite of the usual assumption and it changes design constantly.

Practical consequences:

- **The server owns all state that matters.** Anything a modified client could lie about must be decided server-side. If a client can suppress its own animation, cheat a cooldown, or claim an item it does not have, the design is wrong.
- **Clients receive state, they do not compute it.** A client-side value exists for rendering and prediction. It never drives an enforcement decision.
- **Two players are always the test case.** Ask what happens when two players do the same thing to the same object in the same tick, and write a test for it. Most of the genuinely hard bugs in these mods live here.
- **Assume different players see different things.** Departure and arrival in a teleport have different audiences. Two players at the same chest have different open screens. Broadcast accordingly, and never assume the acting player is the only one who needs telling.
- **Side separation is structural, not incidental.** Client-only code lives in the client source set. A dedicated server must start without loading a single client class, and the repository's side-safety verification enforces this.

## Configuration Through An OP-Gated CLI

**Every option a mod supports must be configurable from the command line by a server operator.** Not most options. Every option.

A server owner without a client mod, working over SSH on a headless box, must be able to configure the mod completely. Graphical configuration screens are a convenience layered on top, never the only way in.

The canonical implementations are **Loot Lock** and **Dogs Unleashed**. Follow their structure rather than inventing a new one:

- Server configuration is an immutable `record`, with a `DEFAULT_` constant and explicit bounds beside each field.
- It is persisted as JSON in the world save directory, so configuration is per world rather than global.
- It loads from the `LevelStorage.Session` before the server is constructed, because some values are needed earlier than server start.
- A missing file yields defaults and writes them out. A malformed file falls back to defaults, logs clearly, and preserves the broken file rather than overwriting it.
- Out-of-range values are clamped on load rather than rejected, so a bad edit never prevents a server starting.
- Mutating commands require **OP permission level 2**.
- Configuration changes sync to connected clients, so client-side UI reflects live server state.
- Every setting reachable from a configuration screen is also reachable from the command tree, and the two share their validation rather than reimplementing it.

## Code Structure

- **Single Responsibility Principle.** One class, one concern. Extract a collaborator rather than growing a class sideways.
- **No class exceeds 700 lines.** A class approaching the limit is split along responsibility seams, into small extracted helpers.
- **Unit tests map one to one onto classes.** A test exercising `CoatRolls` is named `CoatRollsTest` and lives in the matching package under `src/test/java`. A test named after a scenario rather than a class is a test nobody can find.
- **Logic worth testing has no Minecraft dependency.** Comparators, matchers, filter evaluation, and routing decisions are pure functions over plain data, so they are unit testable without a running game. Where this is possible it is not optional.

## Testing

- Any new behavioural code ships with unit tests in the same pull request. Documentation-only and configuration-only changes are exempt.
- Gametests cover behaviour that only exists in a running world: block interactions, networking, persistence across reload, and anything involving more than one player.
- Anything that moves items or grants rewards gets a test asserting **conservation**: the total in the world before equals the total after. Absence of an exception is not evidence of correctness.

## Compatibility

- **Work with vanilla wherever possible.** A feature that only works on the mod's own blocks is worth much less than one that works on the blocks players already have. Where a feature genuinely cannot apply to vanilla content, say so explicitly rather than leaving it ambiguous.
- **Optional integrations degrade cleanly.** Mod Menu, EMI, and JEI are suggested dependencies. The mod loads and runs correctly with none of them installed, and no class referencing their types loads when they are absent.
- **Do not assume the player's video settings.** Particles are culled on reduced particle settings, and a meaningful number of players run Minimal. An effect whose readability depends on particles is invisible to them. Silhouettes must be geometry.

## Documentation

- Every change updates the documentation it invalidates, in the same pull request.
- Reasoning behind an architectural decision belongs in an architecture decision record under `docs/adr/`, not in a comment and not in a commit message.
- Code comments explain **why**, and only when the reason is genuinely surprising. Code that needs a comment to explain what it does should be renamed instead.
