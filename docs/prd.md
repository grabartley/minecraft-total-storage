# Too Many Chests: Product Requirements

- **Release under specification:** `0.1.0`, pre-release alpha
- **Minecraft:** 1.21.1, Fabric
- **Last revised:** 2026-09-09

## How To Read This Document

This document states **what the product is**: the use cases it serves, the rules it holds, and what it deliberately does not do. Every requirement carries an identifier so it can be cited from an issue, a commit, or a review.

It says nothing about what is built. Progress lives on the project board, and duplicating it here would create a second source of truth that drifts out of step within a week. A requirement is written here whether it shipped a year ago or has not been started, because both are equally part of what the mod is.

This document changes when the **requirements** change, or when one of them turns out to be unclear. It does not change when work completes.

Architecture decision records under `docs/adr/` hold the reasoning. This document states rules; the records say why.

---

## 1. Product Summary

### 1.1 The problem

Vanilla storage does not scale. A base with thirty chests forces the player to remember which chest holds what, walk to it, and open it. Every item picked up creates a decision: where does this go. That decision, repeated thousands of times, is the friction the mod exists to remove.

### 1.2 What the mod is

A **storage terminal**: a redstone-powered block that indexes every container around it and presents their combined contents as one searchable screen. Withdraw from anywhere in the network, deposit an entire inventory in one click and let the network decide where each item belongs, and keep the physical chests tidy so walking up and opening one by hand still makes sense.

Alongside it, two conveniences that work with no terminal at all: a **universal sort button** on every container screen, and **inventory slot locking** so automated operations never disturb a player's carefully arranged hotbar.

### 1.3 What makes it different

| Claim | Why it holds |
|---|---|
| Safe in a modpack | Every item movement is one server-side transaction; conservation is asserted by adversarial gametests |
| Costs nothing when idle | An unpowered terminal registers no listeners and does no per-tick work at all |
| Works with the blocks players already own | Built on `ItemStorage.SIDED`, so vanilla and modded containers work with no per-block code |
| Vanilla clients are not second-class | Sorting logic is server-side, reachable by command and middle-click with no client mod |
| Trustworthy near a curated chest | The `Excluded` role means the network lists a container but never writes to or reorders it |

### 1.4 Non-goals

The mod is not a logistics system. There are no pipes, no crafting automation, no autocrafting, no wireless access, and no item transport. It indexes containers that already exist and moves items only when a player asks it to.

---

## 2. Release Scope: 0.1.0

`0.1.0` is a **pre-release alpha**. Every use case in section 5 is in scope. It is cut when each of those use cases holds and section 9's criteria are met, and the version signals that the mod has not yet been proven at scale on a public server.

### 2.1 What alpha means here

| Alpha accepts | Alpha does not accept |
|---|---|
| Rough edges in layout and feedback wording | Any path that can duplicate or destroy items |
| Settings whose defaults need tuning in the wild | A server an ordinary player can degrade |
| Missing convenience for rare workflows | A terminal that can be opened by someone it should not be |
| Features a pack author may want to disable | Data loss across reload, chunk unload, or death |

The distinction is that alpha tolerates **unpolished**, never **unsafe**. Section 8.1 is where that boundary is written down.

### 2.2 Deferred beyond this release

Recorded so the boundary is deliberate rather than accidental.

| Deferred | Reason |
|---|---|
| Loadout presets and their interface | The terminal layout leaves room; the feature waits |
| Backpack and entity-held storage providers | The provider abstraction avoids naming positions so this stays a later feature, not a later rewrite |
| Caching containers in unloaded chunks | The network reflects loaded chunks only, by design (ADR 0002) |
| Component-level tag assignment | Tags apply by item identifier, so an enchanted sword inherits the sword's tags |
| Comparator output proportional to network fullness | Nice-to-have, dropped unless free |
| Bundling library dependencies | Library mods stay separately installed |
| Autocrafting, item transport, wireless access | Not a logistics mod (§1.4) |

---

## 3. Actors

| Actor | Who they are | What they need |
|---|---|---|
| **Player** | Anyone with the mod on their client or connecting to a server running it | Find items fast, deposit without deciding, keep their hotbar intact |
| **Terminal owner** | The player who placed a given terminal | Control who reaches their storage and how it is organised |
| **Server operator** | Permission level 2 | Configure the mod without a client, cap what players can cost the server, moderate |
| **Pack author** | Assembles a modpack | Tune performance caps, override container eligibility, ship a standard configuration |
| **Vanilla client** | A player with no client mod, connected to a modded server | Sorting still works; the terminal does not |

**Vanilla client is an actor, not an edge case.** The mod is server-installed and the sorting logic is server-side deliberately so this actor is served (ADR 0003).

---

## 4. Domain Model

The vocabulary the rest of this document uses.

| Term | Definition |
|---|---|
| **Terminal** | The block. Owns the network's radius, access rules, container roles, and tag set. Settings live in its block entity and survive power loss. |
| **Network** | The set of eligible containers within a powered terminal's radius, in loaded chunks. Not an inventory (ADR 0025). |
| **Radius** | The half-extent of a cuboid centred on the terminal, not a sphere. A cuboid maps onto whole chunk columns without a per-position distance test. |
| **Container** | A block exposing `ItemStorage.SIDED` and admitted by the eligibility rules in §4.1. A double chest is one container, not two. |
| **Index** | The live aggregated view of the network's contents, keyed by `ItemVariant`. A read-only cache; every write goes through a transaction. |
| **Origin** | The identity of where a container's items live. A recorded origin is a hint, never a promise (ADR 0009). |
| **Role** | How the network may treat one container: `General`, `Filtered`, or `Excluded`. Per network, so an overlapped chest may hold a different role in each. |
| **Tag** | A label grouping items by meaning. Preset tags are derived and immutable; custom tags are player-created and stored on the terminal. |
| **Locked slot** | A player inventory slot no automated operation may move. Slot-based, not item-based. |
| **Viewer** | A player currently looking at something the network can change: a terminal screen, or a container open by hand. |

### 4.1 Container eligibility

| Eligible | Not eligible | Why not |
|---|---|---|
| Chests, trapped chests, double chests | Furnaces, blast furnaces, smokers | Working inventories, not storage |
| Barrels | Brewing stands, hoppers, dispensers, droppers, crafters | Same |
| Placed shulker boxes | Ender chests | Contents are per player; shared network semantics are meaningless |
| Modded containers exposing item storage | Chiseled bookshelves, jukeboxes, decorated pots | Single-purpose display blocks, not storage |
| | Chest minecarts, chest boats | They move; a position-keyed network cannot track them |

| ID | Requirement |
|---|---|
| ELIG-1 | Eligibility is decided in one place and consumed everywhere |
| ELIG-2 | A modded container not on the deny list is eligible by default |
| ELIG-3 | Defaults ship as a data-driven block tag a pack can add denials to; removing a shipped denial means replacing the tag or using a config override. An override decides in both directions, admitting a denied block or denying an untagged one |
| ELIG-4 | An operator can change eligibility without a restart |
| ELIG-5 | Eligibility is read at discovery, so a change triggers a fresh discovery scan on every affected network rather than waiting for one |

---

## 5. Use Cases

Each use case states the actor, what happens, what is explicitly **not** supported, and which side enforces it. Enforcement is named in every case because it is the requirement most easily lost in implementation.

### UC-1: Build a storage network

**Actor:** Player

A player crafts a terminal, places it near their chests, and powers it with redstone. The terminal indexes every eligible container within its radius and briefly highlights them in world so membership is never a mystery.

| ID | Requirement |
|---|---|
| TERM-1 | The terminal is craftable in a vanilla crafting table from mid-game materials, with no End content (ADR 0022) |
| TERM-2 | Placement records the owner by UUID and faces the player |
| TERM-3 | A powered terminal indexes its network; an unpowered one is wholly inert (ADR 0007) |
| TERM-4 | Powering on triggers exactly one discovery scan, not one per neighbour update |
| TERM-5 | A terminal powered when its chunk loads becomes active without needing a neighbour update |
| TERM-6 | Radius is per terminal, 16 on a fresh one, minimum 1, clamped to the server maximum on write with the clamp reported |
| TERM-7 | Lowering the server maximum clamps existing terminals without rewriting their configured value, and rebuilds the affected networks rather than waiting for a power cycle (ADR 0021) |
| TERM-8 | Anything reporting a radius reports both the configured value and the effective one whenever they differ |
| TERM-9 | Changing the radius triggers exactly one rescan |
| TERM-10 | Settings survive power loss, chunk unload, and world reload |
| TERM-11 | Losing power with the screen open completes any in-flight transaction, then closes the screen for every viewer |
| TERM-12 | Containers in range are highlighted when a terminal becomes powered, on radius change, and on demand |
| TERM-13 | The highlight distinguishes container roles by more than colour alone, and fades rather than persisting |
| TERM-14 | Breaking a terminal never affects the contents of any connected container |
| TERM-15 | A terminal's powered state is readable from the block itself at distance and in low light, by shape or emissive detail rather than colour alone |

**Not supported:** forcing chunks to keep a container in the network; indexing containers in unloaded chunks; a network spanning dimensions.

**Enforcement:** server. The client renders the highlight from a server-sent list and never computes membership.

---

### UC-2: Find and withdraw an item

**Actor:** Player with access to a terminal

The player opens a terminal, types, and the item is on screen. Clicking takes it.

| Input | Result |
|---|---|
| Left click | One full stack, up to the item's max stack size |
| Right click | Half a stack |
| Shift + left click | Straight to the player's inventory rather than the cursor |
| Middle click | Prompt for a custom amount, which lands in the inventory rather than on the cursor |

With a stack already on the cursor the same grid takes clicks the other way, depositing rather than withdrawing: left click hands over the whole stack, right click hands over one (ADR 0029).

| ID | Requirement |
|---|---|
| FIND-1 | The search box takes keyboard focus the moment the terminal opens; no click required |
| FIND-2 | Matching is ranked exact, then prefix, then substring, then fuzzy, over the display name and the identifier path; a term carrying a namespace separator is matched literally against the full identifier (ADR 0013, ADR 0028) |
| FIND-3 | `#tag` searches tags and `@modid` filters by source mod, both combining with free text. Name and tag terms narrow each other; mod identifier terms broaden, so `@create @ae2` matches either (ADR 0013) |
| FIND-4 | Counts are aggregated across the network and formatted for readability at scale |
| FIND-5 | Items differing only by data components are separate entries with individual counts |
| FIND-6 | An empty network and an empty search result read differently |
| FIND-7 | A stale view is visibly indicated rather than silently wrong |
| FIND-8 | A filter column lists the network's tags with their counts, combinable with AND by default or OR, applying alongside the search box and clearable in one action |
| FIND-9 | Escape clears the search before it closes the screen |
| GET-1 | Each interaction above yields the correct amount |
| GET-2 | Asking for more than exists grants everything available and reports the shortfall |
| GET-3 | A full inventory yields a partial withdrawal; the remainder stays in the network |
| GET-4 | Locked slots are never used as a destination |
| GET-5 | The two empty-handed endings are reported distinctly: the item is gone, or there is nowhere to put it |

**Not supported:** withdrawing to a specific container; naming a slot or position in a request; the client deciding what a withdrawal may take.

**Enforcement:** server. The client sends an `ItemVariant` and an amount and nothing positional (ADR 0009). Search and filtering are evaluated server-side against the live index (ADR 0027).

---

### UC-3: Deposit an inventory without deciding

**Actor:** Player with access to a terminal

The player presses deposit all. Every eligible slot empties into the network, each item routed to the container it belongs in.

Routing order, per item variant:

1. A `Filtered` container that accepts it **and already holds it**
2. Any `Filtered` container that accepts it
3. A `General` container that **already holds it**, topping up partial stacks
4. Any `General` container with free space
5. Nothing, and the item stays with the player

`Excluded` containers are never considered at any step.

| ID | Requirement |
|---|---|
| PUT-1 | Shift-click deposits a whole stack from any player inventory slot; a partial deposit is picking the stack up and right-clicking it onto the grid (ADR 0029) |
| PUT-2 | Deposit all empties the inventory in one action, from a button in the terminal and from a rebindable keybind |
| PUT-3 | The deposit-all keybind yields to the search box while it has focus, so typing never empties an inventory |
| PUT-4 | Locked slots, armour, and the offhand are never deposited, including by deposit all |
| PUT-5 | Deposit all optionally skips the hotbar, off by default since locking covers that case precisely |
| PUT-6 | Partial stacks are topped up before empty slots are used |
| PUT-7 | A stack that fits in no single container splits correctly across several |
| PUT-8 | A full `Filtered` container falls through to `General` rather than refusing the item |
| PUT-9 | A network with no room leaves the inventory untouched and says so |
| PUT-10 | Items that cannot be stored stay in their original slots where possible |
| PUT-11 | Feedback distinguishes "nothing to deposit" from "no space", and names what could not be stored by item and count |
| PUT-12 | The terminal shows network capacity, with a configurable near-full warning threshold |
| PUT-13 | Capacity counts only writable containers, excluding `Excluded` ones |
| PUT-14 | Ties between candidate containers resolve deterministically |
| PUT-15 | Quick-stack tops up matching stacks in nearby containers, introducing nothing new, with or without a terminal present |
| PUT-16 | Quick-stack is reachable from a button in the player inventory, a rebindable keybind, and `/tmc quickstack`, so a vanilla client can use it |

**Not supported:** depositing into an `Excluded` container under any configuration; dropping an item that will not fit; quick-stack introducing an item into a container that does not already hold some.

**Enforcement:** server. What is deposited is read from the player's real inventory at the moment of the move, so a client cannot claim to hand over something it does not hold.

---

### UC-4: Organise the storage area

**Actor:** Terminal owner or operator

The owner assigns roles to containers, creates tags, and files items under them, so the terminal indexes by meaning rather than by chest.

| Role | Receives deposits | Reordered by sorting | Listed | Withdrawable |
|---|---|---|---|---|
| `General` (default) | Yes | Yes | Yes | Yes |
| `Filtered` | Only matching items, preferred first; nothing at all while its filter is empty | Yes | Yes | Yes |
| `Excluded` | Never | Never by a network operation | Yes | Yes |

`Excluded` is the trust mechanism. A curated potion chest stays exactly as its owner arranged it while remaining searchable. Without it, an automatic storage network is something players are afraid to turn on (ADR 0004).

| ID | Requirement |
|---|---|
| ROLE-1 | Every discovered container defaults to `General` |
| ROLE-2 | Roles persist across reload, power loss, and chunk unload |
| ROLE-3 | A container broken and replaced at the same position is new and defaults to `General`, except where its terminal was unpowered or its own chunk unloaded throughout, since nothing was running to hear it go (ADR 0024) |
| ROLE-4 | Roles are per network; an overlapped chest may hold a different role in each |
| ROLE-5 | Role changes take effect immediately with no rescan |
| ROLE-6 | A `Filtered` container stores the tags or filters it accepts |
| ROLE-7 | The settings panel lists every connected container with its position, contents preview, and role |
| ROLE-8 | Selecting a container in the list highlights it in world |
| ROLE-9 | Making a container `Excluded` prevents future writes only, and the interface says so |
| TAG-1 | Preset tags ship derived from vanilla and convention item tags, and are not editable |
| TAG-2 | Every item resolves to at least one preset tag, with Miscellaneous as the fallback |
| TAG-3 | An item can carry many tags at once |
| TAG-4 | Tags are per network and live on the terminal |
| TAG-5 | Custom tags can be created, renamed, and deleted, with names validated for length, uniqueness, and preset collision |
| TAG-6 | Deleting a custom tag removes it from every item that carried it, after confirmation |
| TAG-7 | Custom tags are visually distinguished from presets |
| TAG-8 | Items can be assigned to custom tags individually and in bulk against the current filter, with the count shown before applying |
| TAG-9 | Assignment is by item identifier, so it applies to every variant of that item |
| TAG-10 | A tag set can be copied between owned terminals, merging or replacing, and exported and imported as JSON |
| TAG-11 | Import validates names, skips what it cannot accept, and reports both counts rather than failing wholesale |
| TAG-12 | Assignments referencing items absent from the world are retained rather than dropped |

**Not supported:** editing or deleting preset tags; tagging by data component rather than item identifier; copying a tag set from a terminal the player cannot access.

**Enforcement:** server, on the terminal's permission rules. A player who can merely *use* an `Open` terminal cannot restructure it.

---

### UC-5: Keep the physical chests tidy

**Actor:** Terminal owner or operator, for network operations; any player, for a container they have open

Sorting orders the contents of containers themselves, so opening a chest by hand still makes sense. Consolidation gathers each item into as few containers as possible.

Default order: category by preset tag grouping, then registry identifier, then data components, then stack count descending. Registry identifier rather than display name, so storage does not rearrange itself when a player changes language (ADR 0005).

| ID | Requirement |
|---|---|
| SORT-1 | The comparator is deterministic and stable; sorting a sorted container produces zero moves and no resync |
| SORT-2 | Ordering is language independent by default, with display-name ordering as an explicit option |
| SORT-3 | Full stacks sort ahead of partial stacks of the same item |
| SORT-4 | A sort request is honoured only for the screen handler the player currently has open |
| SORT-5 | Screens where reordering is meaningless or harmful are refused, unknown special-purpose screens included, by a deny list and a structural probe (ADR 0018) |
| SORT-6 | Sorting runs in one transaction; a failure part way rolls back completely |
| SORT-7 | Every viewer of a sorted container is resynced, both halves of a double chest included |
| SORT-8 | In-container sorting merges partial stacks before ordering, so sorting also compacts |
| SORT-9 | A container whose slot restrictions cannot be determined is skipped rather than risked |
| SORT-10 | Consolidation gathers each variant into the fewest containers, then sorts them |
| SORT-11 | `Excluded` containers are never written to or reordered by any network-initiated operation, and stay readable and withdrawable throughout (ADR 0004) |
| SORT-12 | `Filtered` containers receive only matching items during consolidation |
| SORT-13 | Network sort and consolidate are explicit terminal actions, with confirmation above a size threshold, progress, and a summary naming containers touched and `Excluded` ones skipped |
| SORT-14 | A long operation is budgeted per tick and continues across ticks rather than blocking the server |
| SORT-15 | An in-progress operation can be cancelled, rolling back cleanly |
| SORT-16 | The terminal stays responsive while a large operation runs |

**Not supported:** a network-initiated operation writing to or reordering an `Excluded` container; consolidation pushing a non-matching item into a `Filtered` container; a manual sort being blocked on a container the player has open, `Excluded` included, since that is a deliberate act on a container in front of them.

**Enforcement:** server. Network operations require owner or operator permission; sorting a container the player already has open does not.

---

### UC-6: Sort anything, from anywhere

**Actor:** Any player, including a vanilla client

A sort button appears on every container screen and the player inventory. A keybind does the same without the mouse. A vanilla client with no mod installed sorts by command or middle-click, because the logic is server-side.

| ID | Requirement |
|---|---|
| BTN-1 | Server-side sorting works on chests, barrels, shulker boxes, the player inventory, and modded containers |
| BTN-2 | A sort button appears on every container screen not on the deny list, and on the player inventory; the server may still refuse a screen that fails its structural gate (ADR 0018) |
| BTN-3 | A modded screen extending the vanilla handled screen receives the button with no mod-specific code |
| BTN-4 | The button never overlaps vanilla widgets at GUI scales 1 through 4 |
| BTN-5 | The button is positioned relative to the screen background so it tracks at every scale and window size |
| BTN-6 | The button is hidden when the server does not have the mod installed |
| BTN-7 | A rebindable keybind sorts the container the player has open, or their inventory |
| BTN-8 | No default keybinding conflicts with a vanilla key |
| BTN-9 | `/tmc sort` sorts the open container, or the inventory when none is open, for any player |
| BTN-10 | Middle-click sorts the open container with no client mod, without disturbing creative stack cloning |
| BTN-11 | The button and the middle-click gesture are each disableable in config, leaving the command working |

**Not supported:** sorting a container the player does not have open; sorting by replaying simulated slot clicks; a client-only installation providing sorting.

**Enforcement:** server, validated against the player's current screen handler. The client contributes the button, the keybind, and a packet.

---

### UC-7: Protect a hotbar layout

**Actor:** Any player

A player locks the slots holding their tools, weapons, and food. No automated operation moves them again.

| ID | Requirement |
|---|---|
| LOCK-1 | Locks are slot-based, not item-based |
| LOCK-2 | Locks persist across restart and survive death; items still drop per vanilla rules |
| LOCK-3 | The locked set syncs to the owning client and to nobody else |
| LOCK-4 | Alt with left click toggles the lock on the slot under the cursor, in any screen showing the player inventory. There is no lock mode (ADR 0016) |
| LOCK-5 | The gesture never moves an item, and is swallowed whether or not it lands on a lockable slot, so a mis-click does nothing rather than throwing something on the floor (ADR 0016) |
| LOCK-6 | An out-of-range slot index from a forged packet is rejected |
| LOCK-7 | A locked slot is tinted, and carries a badge readable without colour that leaves the stack count and durability bar untouched and the item still identifiable |
| LOCK-8 | The tint is a per-player client display option that can be turned off, leaving the badge as the indicator; it is not a server-wide setting |
| LOCK-9 | Every automated path honours locks: sorting by any entry point, deposit, deposit all, quick-stack, and any future loadout preset swap (ADR 0010) |
| LOCK-10 | Manual dragging, shift-clicking, and hotbar swapping are never blocked by a lock |
| LOCK-11 | Sorting an inventory with locked slots scattered through it sorts around them, leaving them in place |

**Not supported:** locking armour or the offhand, which are excluded from automated operations anyway; a lock preventing vanilla death drops; a lock fighting the player's own hands.

**Enforcement:** server, inside each operation. A forged packet cannot move a locked slot through an automated path.

---

### UC-8: Run a server

**Actor:** Server operator, and pack author

An operator configures every behaviour from the console, with no client mod and no file editing, and caps what any player can cost the server.

| ID | Requirement |
|---|---|
| CFG-1 | Server config is an immutable record persisted as JSON per world, not globally |
| CFG-2 | A missing file yields defaults and writes them; a malformed file falls back, logs clearly, and is preserved rather than overwritten |
| CFG-3 | Out-of-range values are clamped on load rather than rejected, so a bad edit never stops a server starting |
| CFG-4 | Every setting is reachable from the command tree, rooted at `/toomanychests` with the alias `/tmc`, with tab completion for names and values. Most sit under `config list`, `get`, `set`, and `reset`, with bare `config` listing them; container eligibility has its own verbs |
| CFG-5 | Mutating config commands require permission level 2; console execution works for all of them |
| CFG-6 | Value validation reports the valid range rather than silently clamping at the command |
| CFG-7 | Config syncs to clients on join and on change |
| CFG-8 | Every per-network setting reachable from the interface is also reachable by command, targeting a terminal by position |
| CFG-9 | Every server-wide setting described in the settings model is editable from a Mod Menu screen, read-only for non-operators rather than hidden; container eligibility overrides stay command-only, being a set keyed by block rather than one value (ADR 0019) |
| CFG-10 | Expensive operations are rate limited per player and per operation, with feedback naming the operation (ADR 0031) |
| CFG-11 | Every server-to-client list payload is capped, and either paged or truncated, rather than built unbounded |
| CFG-12 | Hitting a cap or budget logs once, not once per occurrence |

**Not supported:** a client-side value influencing a server enforcement decision; a setting reachable from the screen but not the command tree; a rate limit that moves some items before refusing.

#### Server-wide settings

| Setting | Default | Bound |
|---|---|---|
| Maximum terminal radius | 16 | 1 to 64. An effective ceiling applied where a scan reads a radius; lowering it never rewrites a terminal's configured value (ADR 0021) |
| Default terminal access mode | `Owner` | Applies to newly placed terminals only |
| Sort ordering strategy | Registry identifier | Display-name ordering is the alternative |
| Sort button enabled | On | |
| Middle-click sort gesture enabled | On | |
| Quick-stack radius | 8 | 1 to 64 |
| Deposit all skips hotbar | Off | |
| Near-capacity warning threshold | 90% | 0 to 100 |
| Expensive operation cooldown | 10 ticks | 0 to 200, where `0` disables |
| Per-tick move budget | 64 | 1 to 1024, applied to sorting and consolidation |
| Container eligibility overrides | Empty | |

---

## 6. Permissions

Permission is the difference between a storage mod and a griefing tool. Without it, any player walks up to any terminal and drains a base through one convenient interface.

### 6.1 Terminal access modes

| Mode | Who may open and use it |
|---|---|
| `Owner` (default) | Only the player who placed it |
| `Trusted` | The owner plus players on that terminal's trust list |
| `Open` | Anyone |

The server config sets the default mode for newly placed terminals, owner-locked out of the box (ADR 0008). A terminal with no recorded owner, which is one placed by something other than a player, is open to everyone whatever its mode, because there is nobody for the modes to be relative to. It is administrable by operators alone: with no owner, every capability the Owner column grants has no one to grant it to. Changing that default never retroactively changes existing terminals.

### 6.2 Capability matrix

| Capability | Other player | Trusted | Owner | Operator |
|---|---|---|---|---|
| Open an `Owner` terminal | No | No | Yes | Yes, logged |
| Open a `Trusted` terminal | No | Yes | Yes | Yes, logged |
| Open an `Open` terminal | Yes | Yes | Yes | Yes |
| Withdraw and deposit | Only if they may open it | Only if they may open it | Yes | Yes |
| Change radius, access mode, trust list | No | No | Yes | Yes |
| Change container roles | No | No | Yes | Yes |
| Create, assign, delete custom tags | No | No | Yes | Yes |
| Copy or import a tag set | No | No | Yes, on both terminals | Yes |
| Network sort and consolidate | No | No | Yes | Yes |
| Rescan | No | No | Yes | Yes |
| Re-highlight | Only if they may open it | Only if they may open it | Yes | Yes |
| Read a named network setting by command | Yes, excluding the trust list | Yes, excluding the trust list | Yes | Yes |
| Enumerate a network's membership | Only if they may open it | Only if they may open it | Yes | Yes |
| Change a network's settings by command | No | No | Own terminals | Any terminal |
| Read server config, by command or screen | Yes | Yes | Yes | Yes |
| Change server config, by command or screen | No | No | No | Yes, level 2 |
| `/tmc sort`, `/tmc quickstack` | Yes | Yes | Yes | Yes |
| Sort a container they have open | Yes | Yes | Yes | Yes |
| Lock their own slots | Yes | Yes | Yes | Yes |

### 6.3 Enforcement

| ID | Requirement |
|---|---|
| PERM-1 | Ownership is recorded by UUID, never by name, and survives reload and the owner logging out |
| PERM-2 | Permission is re-checked on **every operation**, not only when the screen opens, so a player whose access is revoked mid-session cannot continue |
| PERM-3 | Revoking access closes the screen of a player who already has it open, at the moment of revocation, and tells them why |
| PERM-4 | Operator bypass is logged once at open, never per tick |
| PERM-5 | Denied access produces clear feedback, never a silently failing interaction |
| PERM-6 | Trust list entries resolve for offline players known to the server's user cache, and are stored by UUID |
| PERM-7 | A settings change submitted by a client not entitled to make it is rejected and the panel resynced |
| PERM-8 | The rules the interface enforces and the rules the commands enforce are one implementation, so the two cannot drift |

**A modified client is assumed.** Every permission rule is verified by submitting the packet directly, because hiding a button is not a permission check.

---

## 7. Client and Server

There is no singleplayer mode in this architecture. A single-player world is an integrated server with one client attached, and it runs the same code down the same path as a dedicated server with two hundred. Every rule in this document therefore applies identically in both, and nothing in the mod may branch on which one it is running in.

Stating it this way is deliberate. A requirement written as "in multiplayer, check permission" invites an implementation that skips the check when it thinks it is alone, and the mod is then wrong the moment somebody opens their world to LAN. The boundary that matters is not how many players there are; it is which side decides.

### 7.1 The boundary

| The server | The client |
|---|---|
| Owns every item, every setting, and every permission decision | Renders what it has been told |
| Resolves where an item lives, at the moment of the move | Sends intent: what the player wants, never where it is |
| Re-checks permission on every operation | Hides controls the player may not use, as courtesy only |
| Decides what a search matches, against the live index | Displays the page it was sent |
| Holds the authoritative config | Mirrors config for presentation |

| ID | Requirement |
|---|---|
| SRV-1 | Every item movement, permission decision, and settings change is made on the server |
| SRV-2 | A request naming network contents carries intent only: an item and an amount, never a container position or slot index (ADR 0009). A page offset, a player's own slot index, and a container position naming a role edit are the deliberate exceptions, each validated server-side (ADR 0030) |
| SRV-3 | Any positional information reaching the server is treated as a stale hint and revalidated |
| SRV-4 | No client-side value influences a server enforcement decision, config mirror included |
| SRV-5 | A control hidden on the client is never the enforcement of a rule; the server refuses the packet regardless |
| SRV-6 | A rejected request resyncs the client rather than failing silently, so a desynced client self-corrects |
| SRV-7 | The mod is functional with the server alone. The client adds presentation: the terminal screen, the sort button and keybinds, locked slot rendering, the in-world container highlight, and the configuration screen |

**The mod must be installed on the server.** A client-only installation provides nothing, because nothing it could do would be authoritative.

### 7.2 Concurrency

Concurrent writers exist whatever the player count. A hopper, a comparator, and a chest the player left open all race the terminal in a world with one player in it.

| ID | Requirement |
|---|---|
| CONC-1 | Two players withdrawing the same last stack in the same tick yield one success and one clean shortfall, never two successes |
| CONC-2 | Two terminals with overlapping radii over one chest both list it, both withdraw from it, and totals stay correct |
| CONC-3 | A player with a connected chest open by hand is resynced when the network moves its contents |
| CONC-4 | A player viewing a container is resynced when another player sorts it |
| CONC-5 | Resync is driven by the commit, not by a tick sweep (ADR 0032) |
| CONC-6 | No resync reaches a player who cannot see the affected container |
| CONC-7 | The viewer registry returns to empty when viewers leave by any means, disconnect and death included |
| CONC-8 | Two players may have the same terminal open simultaneously, each with independent search, filters, and scroll |
| CONC-9 | A slow background writer such as a hopper is answered by a tick sweep, not per mutation |
| CONC-10 | A container mutated by anything other than this mod is reflected without a rescan |

### 7.3 Per-player state

| ID | Requirement |
|---|---|
| STATE-1 | Locked slots are per player and sync only to their owner |
| STATE-2 | Search text, active filters, and scroll position are per screen, not per terminal |
| STATE-3 | Rate-limit cooldowns are per player and per operation, so one player never throttles another and one operation never spends another |
| STATE-4 | Terminal ownership and trust are per terminal, recorded by UUID, and independent of who is online |

### 7.4 Side separation

How the boundary is kept in the source tree and verified at build time is an engineering standard, recorded in `docs/standards.md`.

| ID | Requirement |
|---|---|
| SIDE-1 | A dedicated server starts without loading a single client class |
| SIDE-2 | Optional integrations load no classes when the integration is absent |
| SIDE-3 | The mod loads and runs correctly with none of its optional integrations installed |

---

## 8. Non-Functional Requirements

### 8.1 Item safety

The property that decides whether the mod is safe in a modpack. How the suite is maintained as the code changes is an engineering standard, recorded in `docs/standards.md`.

| ID | Requirement |
|---|---|
| SAFE-1 | Every item movement runs inside one transaction, opened by one runner (ADR 0015) |
| SAFE-2 | An operation that fails part way leaves every container exactly as it was |
| SAFE-3 | Items are never fabricated to satisfy a request, under any circumstance |
| SAFE-4 | Items are never dropped on the floor by any operation |
| SAFE-5 | A recorded container position is a hint, revalidated against the world before every extraction |
| SAFE-6 | A request the network cannot meet is an ordinary race, handled quietly with a resync rather than an error |
| SAFE-7 | Adversarial gametests assert conservation of total item count across containers, inventories, and the floor |

### 8.2 Performance

| ID | Requirement |
|---|---|
| PERF-1 | An unpowered terminal registers no listeners and does no per-tick work |
| PERF-2 | The index is never polled; it reacts to container mutations and recomputes lazily on read |
| PERF-3 | A full discovery scan runs on power-on, radius change, explicit rescan, a change to container eligibility, and a chunk load touching the network's region (ADR 0014), and on nothing else |
| PERF-4 | Discovery iterates loaded chunk block-entity maps rather than testing every position in the volume |
| PERF-5 | A world callback never queries the chunk manager; scans defer to the next tick (ADR 0026) |
| PERF-6 | The listing is narrowed server-side and paged, never sent whole (ADR 0027) |
| PERF-7 | Index and filter evaluation over a large network complete well within one tick |
| PERF-8 | A scan exceeding its time budget logs a warning, so pack authors get a signal rather than silent lag |

### 8.3 Persistence

| What | Survives power loss | Survives chunk unload | Survives world reload | Survives death |
|---|---|---|---|---|
| Terminal radius, access mode, trust list | Yes | Yes | Yes | n/a |
| Container roles | Yes | Yes | Yes | n/a |
| Custom tags and assignments | Yes | Yes | Yes | n/a |
| Terminal ownership | Yes | Yes | Yes | n/a |
| Network index | No, rebuilt on power-on | Membership drops on sight, returns on chunk load (ADR 0014) | No, rebuilt | n/a |
| Locked slots | n/a | n/a | Yes | Yes, contents drop per vanilla |

### 8.4 Accessibility and localisation

| ID | Requirement |
|---|---|
| A11Y-1 | Every state distinction is carried by more than colour alone |
| A11Y-2 | The interface renders correctly at GUI scales 1 through 4 |
| A11Y-3 | Every player-facing string carries a translation key with an English fallback |
| A11Y-4 | Sounds are quiet, mechanical, and not fatiguing on repeat |

### 8.5 Interface style

Every screen the mod draws is a Minecraft screen. A player opens the terminal from the same hotbar, with their own inventory drawn underneath it by the game, so an interface in a style of its own reads as a seam rather than as an identity.

| ID | Requirement |
|---|---|
| UI-1 | Every interface the mod draws uses vanilla's container chrome: its chassis, its bevels, its slot frames and its label greys, copied from Minecraft's own textures rather than designed to resemble them |
| UI-2 | A slot is 18 pixels with its item drawn at `+1,+1` wherever the mod draws one, so the mod's grids line up column for column with the player inventory below them |
| UI-3 | Hovering, item counts, durability bars and cooldown sweeps are drawn by the game's own calls, so an item in a mod grid renders exactly as it does in a chest |
| UI-4 | No chrome the mod draws, meaning its panels, rows, controls, fields, slots, icons and text, paints a colour the palette does not hold, and every colour in that palette is one Minecraft itself uses, whether in a texture or in a screen's own drawing code, except one accent, which marks a row that is switched on or armed and is always accompanied by a shape and a word |
| UI-5 | Conformance is tested rather than reviewed: the build reads Minecraft's own textures and fails if a colour drifts or if any shipped sprite paints one that is not in the palette |
| UI-6 | A colour drawn over pixels the mod does not own, a state tint on a slot or an outline on world geometry, is exempt from UI-4 and is documented where it lives |

### 8.6 Compatibility

| ID | Requirement |
|---|---|
| COMPAT-1 | Vanilla and modded containers work through `ItemStorage.SIDED` with no per-block code |
| COMPAT-2 | A double chest is one container, never two |
| COMPAT-3 | Mod Menu, EMI, and JEI are optional, and each degrades cleanly when absent |
| COMPAT-4 | Recipe viewer integration covers every craftable the mod registers, in both viewers |
| COMPAT-5 | Running both recipe viewers at once causes no conflict |
| COMPAT-6 | Every declared dependency is genuinely used, correctly classified as required or optional, and documented for players |

---

## 9. Release Criteria

Conditions that must hold for a release, whatever the schedule.

| ID | Criterion |
|---|---|
| REL-1 | Every use case in section 5 is served in full |
| REL-2 | Every mutating path runs in a transaction and is covered by a conservation-asserting gametest |
| REL-3 | No payload is unbounded and no multi-tick operation is unbudgeted |
| REL-4 | Every automated path honours locked slots, verified per path |
| REL-5 | Every gated capability in the section 6 matrix is verified at the packet level |
| REL-6 | A dedicated server boots with no client classes loaded and no unused hard dependency |
| REL-7 | A player can build a working storage network from the README alone, and an operator can configure the mod from the reference |
| REL-8 | Automated QA evidence exists for every interactive surface, captured against a dedicated server with more than one client connected |
| REL-9 | A genuinely unmodded client is verified against a modded server for UC-6 |

---

## 10. References

- **Project board**: what is built, what is in progress, and what is next. This document deliberately holds none of that.
- **Issues and epics**: the working specification, authoritative on acceptance criteria and implementation detail.
- **`docs/adr/`**: architecture decision records holding the reasoning behind these rules.
- **`docs/standards.md`**: engineering standards shared across the mod family.
