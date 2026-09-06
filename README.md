# Too Many Chests

A Fabric mod for Minecraft 1.21.1.

A better item storage solution for Minecraft.

## Planned features

- Better item storage solution

## The storage terminal

The storage terminal runs on redstone. A terminal reached by a signal is active: it indexes the containers around it once, keeps that listing current as they change, and can be opened. A terminal with no signal is completely inert, holding no listing and doing no work at all, and it says so when you try to open it. The block reports its own state, with a lit screen, vents and dust run when it is running.

Cutting power is an off switch rather than a factory reset. A terminal's radius, roles, tags and access settings live on the block and survive a power cut, a chunk unload, and a world reload.

Each terminal reaches as far as its own radius, a cube centred on the block, and a fresh terminal starts at 16 blocks. The server sets a maximum no terminal may exceed, and the maximum wins: a radius set above it is clamped as it is written and the clamp is reported, and lowering the maximum pulls in every terminal already running. What a terminal asked for is kept whatever the maximum does, so raising the maximum again hands its reach straight back. See [ADR 0021](docs/adr/0021-a-lowered-maximum-clamps-without-rewriting.md).

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
