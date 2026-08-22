# Architecture Decision Records

These records capture **why** Too Many Chests is built the way it is. They are not specifications: what the mod does lives in the repository's issues and epics, which the build workflow reads directly.

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

## Writing A New Record

Cover the context, the decision, and the consequences, including the drawbacks accepted. A record that lists only benefits is not a decision record, it is an advertisement.

Record reasoning that is not obvious from reading the code. If the next person would arrive at the same choice anyway, it does not need a record.
