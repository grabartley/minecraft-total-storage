# Too Many Chests

A Fabric mod for Minecraft 1.21.1.

A better item storage solution for Minecraft.

## Planned features

- Better item storage solution

## The storage terminal

The storage terminal runs on redstone. A terminal reached by a signal is active: it indexes the containers around it once, keeps that listing current as they change, and can be opened. A terminal with no signal is completely inert, holding no listing and doing no work at all, and it says so when you try to open it. The block reports its own state, with a lit screen, vents and dust run when it is running.

Cutting power is an off switch rather than a factory reset. A terminal's radius, roles, tags and access settings live on the block and survive a power cut, a chunk unload, and a world reload.

Each terminal reaches as far as its own radius, a cube centred on the block, and a fresh terminal starts at 16 blocks. The server sets a maximum no terminal may exceed, and the maximum wins: a radius set above it is clamped as it is written and the clamp is reported, and lowering the maximum pulls in every terminal already running. What a terminal asked for is kept whatever the maximum does, so raising the maximum again hands its reach straight back. See [ADR 0021](docs/adr/0021-a-lowered-maximum-clamps-without-rewriting.md).

## Opening a terminal

Right-click a powered terminal you are allowed to open and its screen comes up, showing the terminal's own window above your inventory. Two players can have the same terminal open at once, each with their own screen, and a change either of them commits refreshes both.

The screen is not a container. What the network holds is far more than a container screen can address, so the listing is paged and every operation is resolved by the server against the containers as they stand, rather than against a slot the client picked. See [ADR 0025](docs/adr/0025-the-network-is-not-an-inventory.md) and [ADR 0009](docs/adr/0009-clients-send-intent.md).

Searching and filtering narrow the whole network rather than the page you are looking at, because the server does the narrowing before it sends anything back. The listing also keeps itself current: a chest filling up behind you changes the numbers on screen without you touching anything. See [ADR 0027](docs/adr/0027-the-listing-is-narrowed-on-the-server.md).

The search box takes the keyboard the moment the terminal opens, so you type rather than click first. Matching is fuzzy against an item's name and its identifier, so `dch` finds a Dark Oak Chest, and the closest answers come first: the whole name, then what starts with what you typed, then what merely contains it. Escape empties the box before it closes the screen, so a query typed by accident costs one key rather than a reopened terminal. See [ADR 0028](docs/adr/0028-results-are-ranked-before-they-are-paged.md).

Two prefixes make a query precise. `#tag` searches the network's tags and `@modid` narrows to one mod, and both combine with plain text, so `@create gear` finds the gears Create adds. Several tags or names each narrow the results while several mods widen them, because an item comes from exactly one mod and asking for two of them could only ever return nothing. See [ADR 0013](docs/adr/0013-search-terms-combine-by-kind.md).

The network is drawn as a grid of cells, each one an item and how much of that exact item the whole network holds. Items that differ only by their data components are separate cells with separate counts, so a damaged pickaxe and a fresh one never add up into one number. Counts are written out in full while they fit and compacted once they stop, always downwards: a cell reading `12.3k` is never promising more than is there, and the tooltip carries the real figure alongside how many containers it is spread across.

Scroll the grid with the wheel or by dragging the scrollbar. Each scroll asks the server for the rows the grid is now showing, so what a terminal costs to page through is set by the size of the grid rather than by the size of the network.

A grid with nothing in it says which nothing it means, because the empty cases are not the same problem. An empty network, a search that matched none of what is there, and a terminal whose network has gone each get their own line. A search that found nothing gets its own picture as well, a magnifier rather than a crate, so having nothing and finding nothing are told apart without reading a word. The last case is how a screen tells you it has gone stale, which is what you see if the terminal loses power or is broken while you are looking at it.

Power, permission and distance are re-checked every tick the screen is up, not just when it opened. Cutting the terminal's power, breaking it, losing access to it, walking away from it, or dying all close the screen straight away.

## Taking things out

Click a cell and the item comes to you. Left click takes a stack, right click takes half, shift and left click sends it straight to your inventory, and middle click asks how much when the amount you want is not one a gesture can express.

Nothing is ever guessed at. What you click is a name and an amount, never a place: the server works out which containers really hold the item at the moment it extracts, inside one transaction. A listing that has gone stale under you therefore costs a shortfall rather than an item that never existed, and a container broken in the same tick you click it hands over nothing. Two players reaching for the last stack together produce exactly one success. See [ADR 0009](docs/adr/0009-clients-send-intent.md).

Taking more than the network holds takes everything there is and tells you what you did not get. A full inventory takes what fits and leaves the rest in the network. Nothing is ever dropped on the floor, and a locked slot is never used as somewhere to put a withdrawal.

## Putting things in

Shift-click a stack in your inventory and the network takes it. Pick a stack up and left click the grid to hand over all of it, or right click to hand over one. The deposit all button beside the grid empties your whole inventory in one press.

**Locked slots, armour and the offhand are never deposited**, by any of these, including deposit all. That is the whole reason locking exists, and it is enforced on the server rather than filtered on the client, so a modified client cannot empty a slot you pinned. Shift-click is the one mouse gesture the terminal takes over; picking up, splitting, and dragging all behave exactly as they do in any container. The keyboard belongs to the search box while it has focus, so a number key writes a digit rather than swapping a hotbar slot. See [ADR 0029](docs/adr/0029-the-grid-takes-clicks-like-a-container-does.md).

Where each item lands is the network's decision, made per item against the roles you gave your containers. A `Filtered` container that has declared the item takes it first, and one that already holds it is preferred over one that does not, so like items gather rather than scatter. `General` containers take what is left. `Excluded` containers are never written to, under any configuration.

A full `Filtered` container falls through to a `General` one rather than refusing the item. A network with room for some of what you offered stores that much and tells you what is still in your hands, and one with no room at all leaves your inventory exactly as it was and says so. Having nothing to store is a different message from having nowhere to put it.

## Configuring a network

The terminal has two views, switched with the tabs down its right-hand side: the items it holds, and how it is configured. The settings view is where a storage area stops being whatever happened to be in range and becomes something you arranged.

It shows the terminal's radius against the server's cap, who may open it, who it trusts, and every container it is currently reaching, nearest first, with what each one holds. Clicking a container walks its role forwards and right-clicking walks it back, so marking a chest `Excluded` is two clicks from opening the terminal.

Only the owner and operators can change any of it. Everyone else sees the same panel with its controls greyed out, which is the truth rather than a missing control. Permission is checked again when each change arrives, so a modified client that draws itself the buttons gains buttons that refuse it.

## Seeing what a terminal reaches

"Why isn't that chest included" is the question a radius always produces, so the terminal answers it by outlining the containers it is holding, in the world. The outlines appear when you change the radius, and whenever you ask for them from the settings view or with `/tmc terminal <pos> highlight`. They fade after a few seconds: this is feedback, not an overlay.

Each outline is coloured by that container's role, and an `Excluded` one is drawn as a doubled line as well as a different colour, so the distinction survives a player who cannot separate the red from the green. An unpowered terminal outlines nothing, because it is holding nothing.

## Container roles

Every container a terminal can see carries a role, which is what decides how far the network is allowed to go with it.

| Role | Receives deposits | Reordered by sorting | Listed in the terminal | Withdrawable |
|---|---|---|---|---|
| **General** (default) | Yes | Yes | Yes | Yes |
| **Filtered** | Matching items only, preferred first | Yes | Yes | Yes |
| **Excluded** | Never | Never | Yes | Yes |

`Excluded` is the one that makes the rest safe to turn on. A curated potion chest stays exactly as you arranged it while staying searchable from the terminal. A `Filtered` container additionally records the tags and items it accepts, and that record is stored with its role.

Roles live on the terminal, not on the container, so a chest reached by two overlapping terminals may hold a different role in each. See [ADR 0004](docs/adr/0004-container-roles.md).

Set them from the terminal's settings view, or from the command line for a server with no client mod. A role edit is the one request in the mod that names a block position, and the terminal refuses any container its network does not already reach; see [ADR 0030](docs/adr/0030-a-role-names-a-container-and-the-network-validates-it.md) for why that is not the thing [ADR 0009](docs/adr/0009-clients-send-intent.md) forbids.

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
| `/tmc terminal <pos> info` | anyone | Summarises one terminal: power, radius, access, and its containers by role |
| `/tmc terminal <pos> role <container>` | anyone | What one container's role is |
| `/tmc terminal <pos> role <container> <general\|filtered\|excluded>` | owner or operators | Sets it, refusing a container the terminal does not reach |
| `/tmc terminal <pos> rescan` | owner or operators | Makes the terminal walk its region again |
| `/tmc terminal <pos> highlight` | players | Outlines the containers it is holding |
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
