# Too Many Chests

A Fabric mod for Minecraft 1.21.1.

A better item storage solution for Minecraft.

## Dependencies

| Mod | Required | Side | Why |
|---|---|---|---|
| [Fabric API](https://modrinth.com/mod/fabric-api) `>=0.116.12+1.21.1` | Yes | Client and server | Networking, item transfer, entity attachments, screen events and gametest all come from it. The floor is the version whose transfer and attachment APIs this mod calls. |
| [Mod Menu](https://modrinth.com/mod/modmenu) | No | Client | Adds the button that opens this mod's server settings screen. Without it the same settings are reachable from `/tmc config`. |
| [JEI](https://modrinth.com/mod/jei) | No | Client | The terminal's recipe shows up in it, and this mod keeps its overlay clear of the buttons it draws. |
| [EMI](https://modrinth.com/mod/emi) | No | Client | The same, for players who use EMI instead. |

**The mod has to be on the server.** Every operation that moves an item is decided server-side, which is what lets a completely vanilla client sort a chest with `/tmc sort`. Installing it on the client as well adds the terminal screen, the sort and quick stack buttons, slot locking, and the keybinds.

There are no other hard dependencies. Mod Menu, JEI and EMI are each loaded through a guarded integration, so a dedicated server that has none of them loads none of their classes.

## The storage terminal

The storage terminal runs on redstone. A terminal reached by a signal is active: it indexes the containers around it once, keeps that listing current as they change, and can be opened. A terminal with no signal is completely inert, holding no listing and doing no work at all, and it says so when you try to open it. The block reports its own state, with a lit screen, vents and dust run when it is running.

Powering one takes a moment's care, because the block answers a right-click by opening rather than by taking what you are holding. Sneak while you place a lever on it and the lever goes on the block, or put any solid block beside the terminal and attach the lever to that, which powers the terminal through its neighbour. A redstone torch, a button or any other signal reaching the block works the same way.

Cutting power is an off switch rather than a factory reset. A terminal's radius, roles, tags and access settings live on the block and survive a power cut, a chunk unload, and a world reload.

Each terminal reaches as far as its own radius, a cube centred on the block, and a fresh terminal starts at 16 blocks. The server sets a maximum no terminal may exceed, and the maximum wins: a radius set above it is clamped as it is written and the clamp is reported, and lowering the maximum pulls in every terminal already running. What a terminal asked for is kept whatever the maximum does, so raising the maximum again hands its reach straight back. See [ADR 0021](docs/adr/0021-a-lowered-maximum-clamps-without-rewriting.md).

## Opening a terminal

Right-click a powered terminal you are allowed to open and its screen comes up, showing the terminal's own window above your inventory. Two players can have the same terminal open at once, each with their own screen, and a change either of them commits refreshes both.

The window is a Minecraft window. Its chassis, its bevels, its slot frames and its label grey are vanilla's own, copied out of the game's textures rather than designed to sit beside them, and a grid cell is a vanilla slot: eighteen pixels with the item at the same inset a chest uses, so the grid's nine columns line up with the nine of your inventory underneath. Hovering, counts, durability bars and cooldown sweeps are drawn by the game's own calls, so an item in the grid renders exactly as it does in a chest. One colour is the mod's own, a redstone red, and it marks a row that is switched on or armed: a tag that is filtering, a container the network may not write to, and a tag armed for deletion. See [ADR 0035](docs/adr/0035-the-terminal-wears-vanillas-chrome.md).

The screen is not a container. What the network holds is far more than a container screen can address, so the listing is paged and every operation is resolved by the server against the containers as they stand, rather than against a slot the client picked. See [ADR 0025](docs/adr/0025-the-network-is-not-an-inventory.md) and [ADR 0009](docs/adr/0009-clients-send-intent.md).

Searching and filtering narrow the whole network rather than the page you are looking at, because the server does the narrowing before it sends anything back. The listing also keeps itself current: a chest filling up behind you changes the numbers on screen without you touching anything. See [ADR 0027](docs/adr/0027-the-listing-is-narrowed-on-the-server.md).

The search box takes the keyboard the moment the terminal opens, so you type rather than click first. Matching is fuzzy against an item's name and its identifier, so `dch` finds a Dark Oak Chest, and the closest answers come first: the whole name, then what starts with what you typed, then what merely contains it. Escape empties the box before it closes the screen, so a query typed by accident costs one key rather than a reopened terminal. See [ADR 0028](docs/adr/0028-results-are-ranked-before-they-are-paged.md).

Two prefixes make a query precise. `#tag` searches the network's tags and `@modid` narrows to one mod, and both combine with plain text, so `@create gear` finds the gears Create adds. Several tags or names each narrow the results while several mods widen them, because an item comes from exactly one mod and asking for two of them could only ever return nothing. See [ADR 0013](docs/adr/0013-search-terms-combine-by-kind.md).

The network is drawn as a grid of cells, each one an item and how much of that exact item the whole network holds. Items that differ only by their data components are separate cells with separate counts, so a damaged pickaxe and a fresh one never add up into one number. Counts are written out in full while they fit and compacted once they stop, always downwards: a cell reading `12.3k` is never promising more than is there, and the tooltip carries the real figure alongside how many containers it is spread across.

Scroll the grid with the wheel or by dragging the scrollbar. Each scroll asks the server for the rows the grid is now showing, so what a terminal costs to page through is set by the size of the grid rather than by the size of the network.

A grid with nothing in it says which nothing it means, because the empty cases are not the same problem. An empty network, a search that matched none of what is there, and a terminal whose network has gone each get their own line. A search that found nothing gets its own picture as well, a magnifier rather than a crate, so having nothing and finding nothing are told apart without reading a word. The last case is how a screen tells you it has gone stale, which is what you see if the terminal loses power or is broken while you are looking at it.

Power, permission and distance are re-checked every tick the screen is up, not just when it opened. Cutting the terminal's power, breaking it, losing access to it, walking away from it, or dying all close the screen straight away.

## Filtering by tag

Down the left of the screen is a column of the network's tags, and clicking one narrows the grid to the items carrying it. Clicking it again clears it. The column is how you browse by meaning when you do not have a word to type, and it works alongside the search box rather than instead of it: a tag and some typed text both apply, so `castle` plus `stone` finds the stone you filed under Castle.

Every tag shows how many of the network's items it matches **here**, so a tag holding nothing in this network reads as empty rather than looking like a filter that is broken. The column scrolls on its own when a network has more tags than fit.

Several tags can be active at once, and the button above the column decides how they combine. `All` narrows, so a stack has to carry every tag you picked. `Any` broadens, so carrying one of them is enough. Both are genuinely useful, which is why the choice is a button rather than a decision made for you: `stone` and `building` together is a narrowing question, `stone` or `wood` is a broadening one. The button beside it clears every active tag in one press.

Two kinds of tag share the column. **Preset tags** are the thirteen categories the mod works out for you from the item itself, so a diamond pickaxe is in Tools without anybody filing it. They cannot be renamed, deleted, or assigned by hand, because they are computed rather than stored. See [ADR 0011](docs/adr/0011-preset-membership-is-computed.md) and [ADR 0023](docs/adr/0023-presets-are-seeded-not-stored.md).

**Custom tags** are the ones you make, for the categories vanilla does not know about: a base build, a redstone project, the pile of things you owe the neighbours. They sit below the presets and carry a marker beside their name, so which tags are yours to edit reads without relying on colour.

## Making and assigning tags

Custom tags live on the terminal, in the `Tags` section of the settings tab, and they persist across a power cut and a world reload like every other network setting. `+ New tag` names one. Clicking a tag renames it, and the bin beside it deletes it, which asks once before it does, since deleting a tag unfiles everything that was in it.

A name has to be something the query language can address: not empty, at most 32 characters, letters, digits, spaces, dashes and underscores only, not already taken on this network, and not the name of a preset. A refused name says which of those it broke rather than failing quietly. Renaming moves the tag's id along with its name, so `#castle` finds the tag called Castle the moment you rename it, and the items it held come with it. See [ADR 0033](docs/adr/0033-renaming-a-tag-moves-its-id.md).

Ctrl-click any cell in the grid, or Cmd-click on macOS, to file that item. The panel lists your custom tags with the ones the item already carries ticked, and clicking one files or unfiles it immediately. Tags apply by item identifier, so filing a pickaxe files every pickaxe, damaged or not.

Filing a large storage area one item at a time is not a plan, so **shift-clicking a tag in that panel files everything the grid is currently showing**. Narrow with the search box or the other tags first, and the panel tells you how many items that is before you do it. That is what makes tagging an existing base something you finish rather than abandon.

**A tag set moves between terminals rather than being rebuilt.** The copy control on the `+ New tag` row lists every other powered terminal you may administer, nearest first, with how many custom tags each one holds. Clicking one merges its custom tags into this network. Shift-clicking replaces this network's set with it, and asks once before it does, since replacing unfiles everything you had. Only custom tags move, because preset tags are worked out from the item and every network already has them, and the items filed under each tag come with it. An item this world has never heard of is kept rather than dropped, so a temporarily missing mod does not destroy how you filed things.

The same set can be written to and read from a file, which is how a server owner seeds a standard set or moves one between worlds: `/tmc terminal <pos> tags export <file>` writes it to `too-many-chests/tag-sets/<file>.json` in the world directory, and `/tmc terminal <pos> tags import <file> <merge|replace>` reads it back. `/tmc terminal <pos> tags copy <pos> <merge|replace>` is the command form of the copy control. Every one of them needs administer permission on **both** terminals, so a tag set can never be lifted off a network you cannot already edit. A name a network cannot address is skipped and counted rather than failing the whole import.

Making, renaming, deleting and assigning all need the same permission as any other network setting: the terminal's owner, or an operator. Someone who can merely open the terminal can filter by your tags but cannot restructure them, and that is enforced on the server rather than by hiding the controls, so a modified client cannot rename a tag on a network it does not own.

## Taking things out

Click a cell and the item comes to you. Left click takes a stack, right click takes half, shift and left click sends it straight to your inventory, and middle click asks how much when the amount you want is not one a gesture can express.

Nothing is ever guessed at. What you click is a name and an amount, never a place: the server works out which containers really hold the item at the moment it extracts, inside one transaction. A listing that has gone stale under you therefore costs a shortfall rather than an item that never existed, and a container broken in the same tick you click it hands over nothing. Two players reaching for the last stack together produce exactly one success. See [ADR 0009](docs/adr/0009-clients-send-intent.md).

Taking more than the network holds takes everything there is and tells you what you did not get. A full inventory takes what fits and leaves the rest in the network. Nothing is ever dropped on the floor, and a locked slot is never used as somewhere to put a withdrawal.

Everybody else looking at what you moved is told the moment you move it, in the same tick rather than on the next sweep. That means the other player at your terminal, anybody at a second terminal whose radius overlaps the same chest, and anybody standing at that chest with it open by hand. Nobody who cannot see the container hears anything at all. See [ADR 0032](docs/adr/0032-a-commit-resyncs-everyone-who-can-see-it.md).

A request the network cannot meet says which of the two things went wrong: the item has gone, or you have nowhere to put it. Those send you to fix different things, so they are never reported as each other.

## Putting things in

Shift-click a stack in your inventory and the network takes it. Pick a stack up and left click the grid to hand over all of it, or right click to hand over one. The deposit all button beside the grid empties your whole inventory in one press, and so does the deposit all key, bound to `R` by default and rebindable in the controls screen. The key yields to the search box while it has focus, the way the inventory key does inside the creative search, so typing `redstone` searches for redstone rather than emptying your inventory on the `r`.

**Locked slots, armour and the offhand are never deposited**, by any of these, including deposit all. A server can also keep the hotbar out of deposit all with `depositAllSkipsHotbar`, off by default: locking already protects a chosen slot precisely, and this protects nine of them bluntly, for servers whose players would rather not learn about locking before losing a pickaxe. That is the whole reason locking exists, and it is enforced on the server rather than filtered on the client, so a modified client cannot empty a slot you pinned. Shift-click is the one mouse gesture the terminal takes over; picking up, splitting, and dragging all behave exactly as they do in any container. The keyboard belongs to the search box while it has focus, so a number key writes a digit rather than swapping a hotbar slot. See [ADR 0029](docs/adr/0029-the-grid-takes-clicks-like-a-container-does.md).

Where each item lands is the network's decision, made per item against the roles you gave your containers. A `Filtered` container that has declared the item takes it first, and one that already holds it is preferred over one that does not, so like items gather rather than scatter. `General` containers take what is left. `Excluded` containers are never written to, under any configuration.

A full `Filtered` container falls through to a `General` one rather than refusing the item. A network with room for some of what you offered stores that much and names what is still in your hands, by item rather than as a bare total, because "thirty cobblestone" tells you which chest filled up where "thirty items" does not. A network with no room at all leaves your inventory exactly as it was and says so. Having nothing to store is a different message from having nowhere to put it.

Deposit all is rate limited, since it reads every container in the network at once, and so is rescanning. The cooldown is `expensiveOperationCooldownTicks`, ten ticks by default and settable to `0` to switch it off. It is counted per player and per operation, so emptying your inventory never spends your next rescan; see [ADR 0031](docs/adr/0031-expensive-operations-are-paced-per-player-and-per-operation.md). Asking again too soon says so rather than doing nothing, and changes nothing at all: a limit that half-worked would cost you items.

## Tidying the chests themselves

Sorting reorders the contents of a container so opening it by hand still makes sense, and merges its part stacks while it is there. Consolidation goes wider: it gathers each item into as few containers as it fits in, then sorts what it touched.

Both are buttons beside the item grid, and both are deliberate: the network moves a lot of items when you press one. Above `largeOperationContainers` containers, eight by default, the first press asks rather than starts, and the second press within ten seconds goes ahead. Pressing the button again while the operation runs stops it. A server that would rather never be asked sets the value to `0`.

Where the items go is decided by the same rules as a deposit. A `Filtered` container that accepts the item takes it first; otherwise it gathers into a `General` container that already holds it. Consolidation never introduces an item into a `General` container that did not already have some, because "tidy" does not mean "spread into the empty chest in the corner".

**`Excluded` containers are neither read from nor written to**, and the summary says how many were skipped so an untouched chest is explained rather than mysterious. A container whose slots restrict what they accept, a furnace among them, is skipped for the same reason: the mod would rather leave a machine alone than rearrange its input into its fuel slot. See [ADR 0018](docs/adr/0018-two-gates-decide-what-may-be-sorted.md).

A network-wide operation is the mod's longest-running piece of work, so each tick spends an allowance of `movesPerTickBudget`, counted in the containers and slots the work actually reads, and continues on the next tick instead of stalling the server. The terminal stays usable while it runs, and progress is reported as it goes.

Everything these operations have to say is said **inside the terminal**, on a strip across the top of whichever tab is showing, because a question you must answer is no use on the action bar behind an open screen. On the settings tab the strip sits below the section tabs and the rows it covers step aside for it, so rescanning and highlighting report where you pressed them. A strip that is asking wears the lit chrome and stays up for ten seconds; one that is reporting wears the plain chrome and goes after five. With no terminal open, the same words arrive on the action bar as before. Each step commits on its own, which is what "cancel" means here: nothing further is started, the step in flight settles or rolls back, and no item is ever lost. See [ADR 0036](docs/adr/0036-a-long-operation-commits-one-step-at-a-time.md).

Only the owner and operators can run either, checked when the request arrives rather than when the button is drawn, and both are rate limited like deposit all.

## Sorting a container you are standing at

A chest does not need a terminal to be worth tidying, so **every container screen carries a sort button of its own**, in the clean band vanilla leaves above the first slot row. It is on the player inventory too, and on any modded screen that extends the vanilla container screen, without that mod knowing about this one. Screens whose slots mean something, a furnace or an anvil among them, never get one: rearranging an input into a fuel slot is not tidying. See [ADR 0018](docs/adr/0018-two-gates-decide-what-may-be-sorted.md).

The button sends nothing but the intent to sort the screen that is open, and the server decides what that means, so a modified client cannot use it to reach a container it does not have open. It reports what happened on the action bar: sorted, already sorted, or nothing to sort. A server that would rather not offer it sets `sortButtonEnabled` to `false`, which greys the button out and refuses what the button sends, checked on the server rather than trusted to the client. `/tmc sort` is a separate surface and keeps working, which is what a player with no client mod uses.

**`/tmc sort` sorts the container you have open, with no client mod at all.** That is the surface a completely unmodified client uses on a modded server, which is what makes this mod worth putting on a public one. There is no middle-click gesture: a survival client sends no slot-click packet for the pick-item button at all, so there is nothing for a server to read.

There is also a **sort keybind**, bound to `B` by default and rebindable under Too Many Chests in the controls screen. It sorts the container screen in front of you, or your inventory, and it keeps working with the button hidden, which is the point of having it.

**Locked slots stay where they are.** Sorting the player inventory sorts around them rather than through them, which is what makes it safe to press with a pickaxe pinned to a slot you know by muscle memory.

## Locking a slot

**Alt-click a slot in your inventory to lock it.** A locked slot is never deposited, never quick stacked, never used to land a withdrawal, and never moved by sorting, and every one of those is enforced on the server rather than filtered on the client.

A locked slot wears a padlock badge in the top-left corner of the item, over the item and its decorations, and an amber wash behind it. **The badge carries the state and the wash only reinforces it**, so a player who cannot separate the colours loses nothing. That ordering is why the wash can be turned off and the badge cannot: set `lockedSlotTintVisible` to `false` for a quieter inventory. The badge leaves the stack count and the durability bar alone, both of which vanilla draws along the bottom. It is drawn on the in-world hotbar too, so a pinned pickaxe reads as pinned while you are out mining and not only while a container is open. See [`docs/locked-slot-art.md`](docs/locked-slot-art.md).

## Quick stacking into what is nearby

Quick stack takes what you are carrying and tops up the containers around you. It is on a button in your inventory screen, on a keybind bound to `V` by default, and on `/tmc quickstack` for a player with no client mod. It works on plain chests with no terminal anywhere: this is a convenience, not a network feature.

It is deliberately conservative. **Quick stack only tops up a container that already holds the item**, never introduces a new one, which is what makes it safe to press without looking. It offers the whole inventory, hotbar included, since topping up an existing stack cannot cost you a tool: locking a slot is how you keep something out of it, and a locked slot is never quick stacked. Containers out of `quickStackRadius` are not considered, eight blocks by default, and neither are containers in unloaded chunks. Where a terminal does claim a container nearby, its role is honoured: `Excluded` is skipped and a `Filtered` container still only takes what it accepts. It is rate limited like the other operations that read a lot of containers at once, and it says how much it moved, or that there was nothing nearby that wanted anything. A server that would rather not offer it at all sets `quickStackEnabled` to `false`, which greys the button out and refuses the keybind and the command.

## Client options

Everything above is the server's to decide, because everything above moves items. Where a mod button sits and whether a wash is drawn move nothing, so those are yours, and they live in `config/too-many-chests/client.json` on your own machine. The file is written with its defaults the first time the mod starts, and read at startup.

| Setting | Default | What it does |
|---|---|---|
| `sortButtonVisible` | `true` | Draws the sort button in container screens; turn it off if you would rather use the command |
| `screenButtonAnchor` | `inside_top_right` | Where this mod's screen buttons sit: in the band above the slots, or `outside_top_right` / `outside_bottom_right` beside the panel |
| `lockedSlotTintVisible` | `true` | Draws the amber wash behind a locked slot; the badge stays either way |

Only one button fits the band vanilla leaves, so where a screen carries two, the player inventory being the one that does, the second moves outside the panel rather than crowding the title. See [`docs/sort-button-art.md`](docs/sort-button-art.md).

Three keybinds live under Too Many Chests in the vanilla controls screen, all rebindable, and none of them takes a key vanilla already uses.

| Key | Default | What it does |
|---|---|---|
| Sort container | `B` | Sorts the container screen in front of you, or your inventory. Keeps working with the button hidden |
| Deposit all | `R` | Empties your inventory into the terminal you have open. Yields to the search box while it holds a query |
| Quick stack | `V` | Tops up the containers around you, from the inventory screen or with no screen open at all |

**The server settings have a screen too.** With [Mod Menu](https://modrinth.com/mod/modmenu) installed, the configure button beside Too Many Chests opens every server-wide setting grouped by what it affects, each one showing its range and its default. An operator, or the owner of a singleplayer world whether or not cheats are on, can change them and the change goes to the server, which checks the permission again before applying it. Everybody else sees the same screen read-only, because knowing what the radius cap is on the server you are playing on is reasonable even when changing it is not. Values follow the server, so a setting moved by command updates the screen under you as long as you have not started editing.

## Configuring a network

The terminal has two views, switched with the tabs down its right-hand side: the items it holds, and how it is configured. The settings view is where a storage area stops being whatever happened to be in range and becomes something you arranged.

The settings view has two sections. **Terminal** holds the radius, stepped from either end of its own row, the access mode, cycled by clicking its row, the trust list, and how much room the network has left. **Containers** lists everything the network reaches, nearest first, each row showing what that container mostly holds so you can recognise it without reading coordinates. Clicking a row walks its role forwards and right-clicking walks it back, so marking a chest `Excluded` is two clicks from opening the terminal.

The two are separate because the window is already as tall as Minecraft will draw one on the smallest screen it supports. Sharing that height between a handful of settings and a list of fifty containers makes both unusable; giving each its own section costs one strip of tabs and leaves both room to read.

The Terminal section also shows how much room the network has left, counted in slots across every container it may write to. Slots rather than items, because slots are what runs out: a network holding one of each of a thousand things is full while holding almost nothing. `Excluded` containers are counted in neither total, since a chest the network may not write to is not room. The figure turns warning-coloured once the network is as full as `nearCapacityWarningPercent`, ninety by default, so you see it coming rather than discovering it on a failed deposit.

Only the owner and operators can change any of it. Everyone else sees the same panel with its controls greyed out, which is the truth rather than a missing control. Permission is checked again when each change arrives, so a modified client that draws itself the buttons gains buttons that refuse it.

## Seeing what a terminal reaches

"Why isn't that chest included" is the question a radius always produces, so the terminal answers it by outlining the containers it is holding, in the world. The outlines appear when you change the radius, and whenever you ask for them from the settings view or with `/tmc terminal <pos> highlight`. They fade after a few seconds: this is feedback, not an overlay. A double chest is outlined as the one container it is, across both of its blocks, and the outline follows the block state, so splitting a chest while the outline is up redraws it rather than leaving a stale box.

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

## Configuration reference

Server settings live in `too-many-chests/server-config.json` inside the world directory, so two worlds hold two independent sets. They are read at world load, written back with their defaults the first time, and every one of them is editable from `/tmc config` or from the Mod Menu settings screen. A value outside its range is clamped as the file loads and refused outright by the command, so a typo never silently half-applies. A malformed file is preserved beside itself and the defaults are loaded, rather than the broken file being overwritten.

**Defaults for new terminals**

| Setting | Default | Range | What it affects |
|---|---|---|---|
| `maxTerminalRadius` | `16` | 1 to 64 | The furthest any terminal on this server may reach. Lowering it pulls in every terminal already running; raising it hands the reach back. This is the main lever on how much a single scan costs. |
| `defaultTerminalAccessMode` | `owner` | `owner`, `trusted`, `open` | What a newly placed terminal starts as. Changing it never touches a terminal already placed. |
| `quickStackRadius` | `8` | 1 to 64 | How far quick stack looks for containers to top up. |

**Sorting**

| Setting | Default | Range | What it affects |
|---|---|---|---|
| `sortOrdering` | `registry_id` | `registry_id`, `display_name` | How sorted containers are ordered. |
| `sortButtonEnabled` | `true` | true or false | Whether the sort button and the sort keybind are offered. `/tmc sort` is a separate surface and keeps working. |

**Features**

| Setting | Default | Range | What it affects |
|---|---|---|---|
| `quickStackEnabled` | `true` | true or false | Whether quick stack is offered at all, on the button, the keybind and the command. |
| `depositAllSkipsHotbar` | `false` | true or false | Keeps the whole hotbar out of deposit all, bluntly, for servers whose players would rather not learn about locking first. |
| `nearCapacityWarningPercent` | `90` | 0 to 100 | How full a network has to be before the terminal colours its remaining capacity as a warning. |

**Performance**

These are the three settings a pack author should look at, because they are what stands between a very large storage area and a tick spike.

| Setting | Default | Range | What it affects |
|---|---|---|---|
| `expensiveOperationCooldownTicks` | `10` | 0 to 200 | Per player and per operation cooldown on rescan, deposit all, quick stack, network sort, consolidation and container sort. `0` switches it off. Raise it on a busy server before you lower anything else. |
| `movesPerTickBudget` | `64` | 1 to 1024 | How much work one network-wide operation may do in a single tick before continuing on the next. Lower means smoother ticks and slower operations. |
| `largeOperationContainers` | `8` | 0 to 1024 | How many containers a network sort or consolidation may touch before the first press asks instead of starting. `0` never asks. |

Container eligibility overrides are stored alongside these and edited with `/tmc config eligibility`. They force a block into or out of every network on the server, which is how you admit a modded container the rules do not recognise, or keep one out.

## Commands

Every server-wide setting is configurable from the console, so a server can be set up without a client mod and without editing files. `/toomanychests` and its alias `/tmc` are the same tree.

| Command | Who | What it does |
|---|---|---|
| `/tmc` | anyone | Lists the commands |
| `/tmc config list` | anyone | Every setting, its value, and its default |
| `/tmc config get <setting>` | anyone | One setting's value |
| `/tmc config set <setting> <value>` | operators, and a singleplayer host | Moves a setting, refusing a value outside its range |
| `/tmc config reset <setting>` | operators, and a singleplayer host | Restores one setting's default |
| `/tmc config reset all` | operators, and a singleplayer host | Restores every default |
| `/tmc config eligibility list` | anyone | Blocks forced into or out of every network |
| `/tmc config eligibility set <block> <true\|false>` | operators, and a singleplayer host | Forces a block eligible or ineligible |
| `/tmc config eligibility clear <block>` | operators, and a singleplayer host | Stops forcing that block |
| `/tmc config eligibility clear all` | operators, and a singleplayer host | Stops forcing every block |
| `/tmc terminal <pos> radius` | anyone | One terminal's radius, and what it actually reaches |
| `/tmc terminal <pos> radius <blocks>` | owner or operators | Sets one terminal's radius, clamped to the server maximum |
| `/tmc terminal <pos> access` | anyone | Which access mode one terminal is in |
| `/tmc terminal <pos> access <owner\|trusted\|open>` | owner or operators | Sets who may open that terminal |
| `/tmc terminal <pos> trust` | owner or operators | The players that terminal trusts |
| `/tmc terminal <pos> trust <players>` | owner or operators | Adds players to its trust list, online or not |
| `/tmc terminal <pos> untrust <players>` | owner or operators | Removes players from its trust list |
| `/tmc terminal <pos> info` | anyone | Summarises one terminal: power, radius, access, and its containers by role |
| `/tmc terminal <pos> role <container>` | anyone | What one container's role is |
| `/tmc terminal <pos> role <container> <general\|filtered\|excluded>` | owner or operators | Sets it, refusing a container the terminal does not reach |
| `/tmc terminal <pos> rescan` | owner or operators | Makes the terminal walk its region again, rate limited like the panel button |
| `/tmc terminal <pos> highlight` | anyone who may open it | Outlines the containers it is holding |
| `/tmc terminal <pos> tags export <file>` | owner or operators | Writes its custom tag set to the world directory |
| `/tmc terminal <pos> tags import <file> <merge\|replace>` | owner or operators | Reads a tag set back into it |
| `/tmc terminal <pos> tags copy <pos> <merge\|replace>` | owner or operators on both | Copies another terminal's custom tag set into it |
| `/tmc sort` | anyone | Sorts the container you have open |
| `/tmc quickstack` | anyone | Tops up matching stacks in the containers around you |

Setting names and their values tab-complete. Changes are saved to the world immediately and pushed to connected clients. Operators are permission level 2.

## Development

Gradle + Fabric Loom toolchain with Spotless formatting, Checkstyle, JaCoCo coverage, Fabric GameTest, and CI/CD via GitHub Actions, sharing the workflow of [minecraft-dogs-unleashed](https://github.com/grabartley/minecraft-dogs-unleashed).

- `./gradlew check` runs formatting, unit tests, side-safety verification, and the no-comments gate
- `./gradlew runClient` launches the dev client
- `./gradlew runGametest` runs the in-game test suite

## Documentation

- [`docs/prd.md`](docs/prd.md) states what the mod is: every use case it serves, who may do what, and the rules it holds
- [`docs/standards.md`](docs/standards.md) covers the engineering standards shared across these mods
- [`docs/adr/`](docs/adr/) records the architecture decisions behind this mod and the reasoning for each
- [`docs/terminal-block-art.md`](docs/terminal-block-art.md) is the reference for the terminal block's textures, models and sounds, and the blockstate it needs
- [`docs/terminal-gui-sprites.md`](docs/terminal-gui-sprites.md) is the sprite reference for the terminal screen, and how to edit the art
- [`docs/locked-slot-art.md`](docs/locked-slot-art.md) is the lock badge reference, and how a locked slot signals itself without relying on colour
- [`docs/sort-button-art.md`](docs/sort-button-art.md) is the reference for the buttons this mod draws on screens it does not own, and why they still need their own art
