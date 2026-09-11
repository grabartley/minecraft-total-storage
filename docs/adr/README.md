# Architecture Decision Records

These records capture **why** Too Many Chests is built the way it is. They are not specifications: what the mod is lives in [`../prd.md`](../prd.md), and the issues and epics remain the working specification on acceptance criteria.

A record is historical. When a decision changes, add a new record superseding the old one rather than editing it, so the reasoning behind the original choice is not lost.

Shared engineering standards across all the mods in this family live in [`../standards.md`](../standards.md).

| Record | Decision |
|---|---|
| [0001](0001-storage-providers-on-fabric-transfer-api.md) | Storage providers are built on the Fabric Transfer API |
| [0002](0002-loaded-chunks-only.md) | The network reflects loaded chunks only |
| [0003](0003-server-side-sorting.md) | Sorting runs on the server |
| [0004](0004-container-roles.md) | Containers have roles, and `Excluded` exists to earn trust |
| [0005](0005-registry-identifier-sort-order.md) | Sort ordering uses registry identifiers, not display names |
| [0006](0006-tags-scoped-per-network.md) | Tags are scoped per network |
| [0007](0007-redstone-gated-terminal.md) | The terminal is redstone gated |
| [0008](0008-terminals-owner-locked-by-default.md) | Terminals are owner-locked by default |
| [0009](0009-clients-send-intent.md) | Clients send intent, servers resolve it |
| [0010](0010-locked-slots.md) | Locked slots are slot-based and survive death |
| [0011](0011-preset-membership-is-computed.md) | Preset membership is computed, and Miscellaneous is derived |
| [0012](0012-network-index-is-generic-over-its-variant.md) | The network index is generic over its variant type |
| [0013](0013-search-terms-combine-by-kind.md) | Search terms combine by kind |
| [0014](0014-containers-leave-on-sight-and-return-on-chunk-load.md) | Containers leave the network on sight, and return only on a chunk load |
| [0015](0015-one-runner-opens-every-transaction.md) | One runner opens every transaction |
| [0016](0016-locks-are-toggled-by-a-modifier-click.md) | Locks are toggled by a modifier click, not a lock mode |
| [0017](0017-terminal-gui-art-ships-as-atlas-sprites.md) | Terminal GUI art ships as atlas sprites, not a sprite sheet |
| [0018](0018-two-gates-decide-what-may-be-sorted.md) | Two gates decide what may be sorted |
| [0019](0019-settings-are-described-once.md) | Settings are described once, not wired twice |
| [0020](0020-the-sort-button-wears-vanilla-chrome.md) | The sort button wears vanilla chrome and bakes in its glyph |
| [0021](0021-a-lowered-maximum-clamps-without-rewriting.md) | A lowered maximum clamps a terminal without rewriting it |
| [0022](0022-the-terminal-costs-an-ender-pearl.md) | The terminal costs an ender pearl, and nothing rarer |
| [0023](0023-presets-are-seeded-not-stored.md) | Presets are seeded into a tag set, not stored in it |
| [0024](0024-a-role-dies-with-the-container-it-was-given-to.md) | A role dies with the container it was given to |
| [0025](0025-the-network-is-not-an-inventory.md) | The terminal's screen handler holds no network slots |
| [0026](0026-world-callbacks-defer-their-scans.md) | A world callback defers its scan to the next tick |
| [0027](0027-the-listing-is-narrowed-on-the-server.md) | The listing is narrowed on the server and answered by revision |
| [0028](0028-results-are-ranked-before-they-are-paged.md) | Results are ranked before they are paged |
| [0029](0029-the-grid-takes-clicks-like-a-container-does.md) | The grid takes clicks the way a container does, and the cursor decides which way they go |
| [0030](0030-a-role-names-a-container-and-the-network-validates-it.md) | A role names a container, and the network validates it |
| [0031](0031-expensive-operations-are-paced-per-player-and-per-operation.md) | Expensive operations are paced per player and per operation |
| [0032](0032-a-commit-resyncs-everyone-who-can-see-it.md) | A commit resyncs everyone who can see what it moved |
| [0033](0033-renaming-a-tag-moves-its-id.md) | Renaming a tag moves its id |
| [0034](0034-a-linter-forbids-comments-rather-than-a-formatter-stripping-them.md) | A linter forbids comments rather than a formatter stripping them |
| [0035](0035-the-terminal-wears-vanillas-chrome.md) | The terminal wears vanilla's chrome rather than its own |

## Writing A New Record

Cover the context, the decision, and the consequences, including the drawbacks accepted. A record that lists only benefits is not a decision record, it is an advertisement.

Record reasoning that is not obvious from reading the code. If the next person would arrive at the same choice anyway, it does not need a record.
