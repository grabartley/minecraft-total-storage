# Too Many Chests

A Fabric mod for Minecraft 1.21.1.

A better item storage solution for Minecraft.

## Planned features

- Better item storage solution

## The storage terminal

The storage terminal runs on redstone. A terminal reached by a signal is active: it indexes the containers around it once, keeps that listing current as they change, and can be opened. A terminal with no signal is completely inert, holding no listing and doing no work at all, and it says so when you try to open it. The block reports its own state, with a lit screen, vents and dust run when it is running.

Cutting power is an off switch rather than a factory reset. A terminal's radius, roles, tags and access settings live on the block and survive a power cut, a chunk unload, and a world reload.

Each terminal reaches as far as its own radius, a cube centred on the block, and a fresh terminal starts at 16 blocks. The server sets a maximum no terminal may exceed, and the maximum wins: a radius set above it is clamped as it is written and the clamp is reported, and lowering the maximum pulls in every terminal already running. What a terminal asked for is kept whatever the maximum does, so raising the maximum again hands its reach straight back. See [ADR 0021](docs/adr/0021-a-lowered-maximum-clamps-without-rewriting.md).

## Container roles

Every container a terminal can see carries a role, which is what decides how far the network is allowed to go with it.

| Role | Receives deposits | Reordered by sorting | Listed in the terminal | Withdrawable |
|---|---|---|---|---|
| **General** (default) | Yes | Yes | Yes | Yes |
| **Filtered** | Matching items only, preferred first | Yes | Yes | Yes |
| **Excluded** | Never | Never | Yes | Yes |

`Excluded` is the one that makes the rest safe to turn on. A curated potion chest stays exactly as you arranged it while staying searchable from the terminal. A `Filtered` container additionally records the tags and items it accepts, and that record is stored with its role.

Roles live on the terminal, not on the container, so a chest reached by two overlapping terminals may hold a different role in each. See [ADR 0004](docs/adr/0004-container-roles.md).

Break a container and its role goes with it: whatever you put back on that spot starts as a general container rather than inheriting a decision made about a different one. A container that only leaves because its chunk unloaded keeps everything, and gets it back when the chunk returns. A container broken while its terminal has no power is the exception, since an unpowered terminal is not running and hears nothing. See [ADR 0024](docs/adr/0024-a-role-dies-with-the-container-it-was-given-to.md).

## Crafting the terminal

The terminal is a mid-game goal rather than a starting block or an endgame reward. It costs a chest, four iron ingots, three redstone dust, and one ender pearl, and nothing that requires a trip to the End. See [ADR 0022](docs/adr/0022-the-terminal-costs-an-ender-pearl.md).

|   |   |   |
|---|---|---|
| Iron ingot | Ender pearl | Iron ingot |
| Redstone dust | Chest | Redstone dust |
| Iron ingot | Redstone dust | Iron ingot |

The recipe appears in the recipe book the first time you pick up an ender pearl, which is the moment it becomes craftable. The terminal is also in the Functional Blocks creative tab.

## Who can open a terminal

A terminal reaches every container around it through one screen, so on a shared server it is a key to a whole base. Each terminal therefore has an access mode, and it is owner-locked to begin with. See [ADR 0008](docs/adr/0008-terminals-owner-locked-by-default.md).

| Mode | Who can open it |
|---|---|
| `owner` (default) | Only the player who placed it |
| `trusted` | The owner, plus the players on that terminal's trust list |
| `open` | Anyone |

- The owner is recorded by UUID when the terminal is placed, so ownership survives a name change and outlives the owner logging out.
- Only the owner and operators can change the mode or edit the trust list, whatever mode the terminal is in. Being let into an open terminal is not a way to lock it again.
- Operators can open any terminal, for moderation. That bypass is written to the server log, naming the player and the terminal.
- Permission is checked server-side on every operation rather than only when the screen opens, and an edit binds the very next check. A player whose access is taken away while they have the screen open has it closed for them.
- A refused right-click says so instead of doing nothing.
- `defaultTerminalAccessMode` sets what newly placed terminals start as, so a friendly server can switch to `open` once rather than every player changing every terminal. Changing it never touches a terminal already placed.

## Commands

Every server-wide setting is configurable from the console, so a server can be set up without a client mod and without editing files. `/toomanychests` and its alias `/tmc` are the same tree.

| Command | Who | What it does |
|---|---|---|
| `/tmc` | anyone | Lists the commands |
| `/tmc config list` | anyone | Every setting, its value, and its default |
| `/tmc config get <setting>` | anyone | One setting's value |
| `/tmc config set <setting> <value>` | operators | Moves a setting, refusing a value outside its range |
| `/tmc config reset <setting>` | operators | Restores one setting's default |
| `/tmc config reset all` | operators | Restores every default |
| `/tmc config eligibility list` | anyone | Blocks forced into or out of every network |
| `/tmc config eligibility set <block> <true\|false>` | operators | Forces a block eligible or ineligible |
| `/tmc config eligibility clear <block>` | operators | Stops forcing that block |
| `/tmc config eligibility clear all` | operators | Stops forcing every block |
| `/tmc terminal <pos> radius` | anyone | One terminal's radius, and what it actually reaches |
| `/tmc terminal <pos> radius <blocks>` | operators | Sets one terminal's radius, clamped to the server maximum |
| `/tmc terminal <pos> access` | anyone | Which access mode one terminal is in |
| `/tmc terminal <pos> access <owner\|trusted\|open>` | owner or operators | Sets who may open that terminal |
| `/tmc terminal <pos> trust` | owner or operators | The players that terminal trusts |
| `/tmc terminal <pos> trust <players>` | owner or operators | Adds players to its trust list, online or not |
| `/tmc terminal <pos> untrust <players>` | owner or operators | Removes players from its trust list |
| `/tmc sort` | anyone | Sorts the container you have open |

Setting names and their values tab-complete. Changes are saved to the world immediately and pushed to connected clients. Operators are permission level 2.

## Development

Gradle + Fabric Loom toolchain with Spotless formatting, JaCoCo coverage, Fabric GameTest, and CI/CD via GitHub Actions, sharing the workflow of [minecraft-dogs-unleashed](https://github.com/grabartley/minecraft-dogs-unleashed).

- `./gradlew check` runs formatting, unit tests, and side-safety verification
- `./gradlew runClient` launches the dev client
- `./gradlew runGametest` runs the in-game test suite

## Documentation

- [`docs/standards.md`](docs/standards.md) covers the engineering standards shared across these mods
- [`docs/adr/`](docs/adr/) records the architecture decisions behind this mod and the reasoning for each
- [`docs/terminal-block-art.md`](docs/terminal-block-art.md) is the reference for the terminal block's textures, models and sounds, and the blockstate it needs
- [`docs/terminal-gui-sprites.md`](docs/terminal-gui-sprites.md) is the sprite reference for the terminal screen, and how to edit the art
- [`docs/locked-slot-art.md`](docs/locked-slot-art.md) is the lock badge reference, and how a locked slot signals itself without relying on colour
- [`docs/sort-button-art.md`](docs/sort-button-art.md) is the sort button reference, and why it wears vanilla's greys rather than the terminal's
