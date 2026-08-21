# ADR 0004: Containers have roles, and Excluded exists to earn trust

- **Status:** Accepted
- **Date:** 2026-08-22

## Context

The mod's headline feature is depositing everything at once and letting the network file it. Consolidation, gathering scattered stacks of the same item into as few containers as possible, follows naturally and is what players usually mean by tidy storage.

Both features share a problem. A system that silently rearranges a player's containers is frightening. Many players keep a carefully curated potion chest or a brewing supply chest arranged exactly how they like it, and an automatic network that reorganises it is the kind of thing that gets a mod uninstalled with an angry comment. The feature that makes the mod valuable is the same feature that makes it dangerous.

## Decision

Every connected container carries a role, set from the terminal.

| Role | Receives deposits | Reordered by sorting | Listed | Withdrawable |
|---|---|---|---|---|
| **General** (default) | Yes | Yes | Yes | Yes |
| **Filtered** | Matching items only, preferred first | Yes | Yes | Yes |
| **Excluded** | Never | Never | Yes | Yes |

Roles live on the terminal, so a container reached by two overlapping terminals may hold a different role in each.

## Consequences

`Excluded` is the reason the rest of the mod is safe to enable. A curated chest stays exactly as its owner arranged it while remaining searchable from the terminal, which is the combination players actually want and cannot get from either a pure automation mod or a pure search mod.

`Filtered` doubles as the routing configuration. A container that declares what it accepts has already told the network where to send those items, so rule-based distribution mostly falls out of the role system rather than needing a separate mechanism.

Every automated operation must check roles, and each new automated feature inherits that obligation. A path that forgets to check is a bug that destroys player trust rather than merely misbehaving.

Roles being per network follows from them living on the terminal. It is slightly surprising and is documented rather than designed around.

`Excluded` containers remain readable and withdrawable. A container that could be seen but not used would be pointless, and the risk being managed is unwanted writes, not reads.
