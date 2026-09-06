# Too Many Chests

A Fabric mod for Minecraft 1.21.1.

A better item storage solution for Minecraft.

## Planned features

- Better item storage solution

## The storage terminal

The storage terminal runs on redstone. A terminal reached by a signal is active: it indexes the containers around it once, keeps that listing current as they change, and can be opened. A terminal with no signal is completely inert, holding no listing and doing no work at all, and it says so when you try to open it. The block reports its own state, with a lit screen, vents and dust run when it is running.

Cutting power is an off switch rather than a factory reset. A terminal's radius, roles, tags and access settings live on the block and survive a power cut, a chunk unload, and a world reload.

Each terminal reaches as far as its own radius, a cube centred on the block, and a fresh terminal starts at 16 blocks. The server sets a maximum no terminal may exceed, and the maximum wins: a radius set above it is clamped as it is written and the clamp is reported, and lowering the maximum pulls in every terminal already running. What a terminal asked for is kept whatever the maximum does, so raising the maximum again hands its reach straight back. See [ADR 0021](docs/adr/0021-a-lowered-maximum-clamps-without-rewriting.md).

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
