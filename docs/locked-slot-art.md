# Locked Slot Art

The art that tells a player a slot is locked, and the placement contract it puts on the renderer. It shares the palette in [`terminal-gui-sprites.md`](terminal-gui-sprites.md), so a locked slot in the terminal's player inventory reads as part of the same mod as the terminal screen above it.

A locked slot can carry two cues, and they are not interchangeable:

| Cue | Carries | Drawn | Default |
|---|---|---|---|
| The badge | The lock state | Over the item and its decorations | Always on |
| The tint | Reinforcement only | Behind the item, under everything else | Off |

**The badge is the indicator and the tint is the reinforcement.** The tint distinguishes a locked slot by colour alone, which is exactly what a player with a colour vision deficiency cannot use, so it can never be the only cue. That ordering is also why the tint is a per-player option and the badge is not: turning the tint off costs a player nothing they cannot get from the badge. The tint is off on a fresh install so a locked inventory looks like a vanilla one with a few small padlocks on it.

Locks are drawn in every screen that shows the player inventory, because quick stack and the inventory sort button skip locked slots there too. They are never drawn on the in-game hotbar.

## Sprite Reference

Under `src/client/resources/assets/total-storage/textures/gui/sprites/`, in the client source set because a dedicated server must never load it.

| Sprite id | Size | Scaling | Use |
|---|---|---|---|
| `slot/locked` | 5x6 | stretch | Lock badge on a locked inventory slot |

## Placement

The badge sits in the **top-left corner of the 16x16 item area**, at the slot's `x, y` with no offset. Vanilla's own slot decorations are all bottom-anchored, so the top-left corner is the one region a badge can take without contending with them:

| Decoration | Region it owns | Overlap with the badge |
|---|---|---|
| Stack count | Bottom-right, drawn from `y + 9` down | None, the badge's last row is `y + 5` |
| Durability bar | `x + 2, y + 13` to `x + 15, y + 15` | None |
| Cooldown overlay | Bottom-anchored, up to the whole slot | Draws under the badge |

Because the badge is 5x6 of a 16x16 slot it covers about an eighth of the item, leaving almost all of it visible, including the lower right where most vanilla item silhouettes carry their identifying detail.

**Draw order is badge last.** The tint goes down first, then vanilla draws the item and its decorations, then the badge. Drawing the badge before the item hides it behind anything opaque, and drawing the tint after the item washes out the stack it is meant to be describing.

## Why The Badge Reads Everywhere

The badge is a padlock with a `FFFFFF` body and a one pixel `373737` outline and keyhole. Both are vanilla's own, from the palette in [`terminal-gui-sprites.md`](terminal-gui-sprites.md): `373737` is a slot's shadow edge, so the outline reads as part of the slot rather than as a black sticker on top of it. Two tones is what makes it independent of whatever is behind it:

- Against a **bright** item such as glowstone dust, the dark outline separates it.
- Against a **dark** item such as coal, the light fill separates it.

Neither tone is doing the job alone, which is what a single-tone badge would need, and it is why the badge does not require an opaque plate behind it to stay legible.

The shape is a padlock silhouette rather than a letter or an abstract mark, so it survives greyscale and needs no translation. The keyhole is a single pixel; at 5x6 it is texture rather than information, and the arch above the body is what a player actually reads.

## Alt Ring

Holding Alt in a screen that shows the player inventory shows which slots can be locked. `LockHintRing` draws a dotted ring, one pixel on and one off, inset 2 pixels inside every lockable slot that is not locked yet, in white at about 55% opacity (`0x8CFFFFFF`). A locked slot keeps its badge instead. The ring is drawn over the item, so it shows on full and empty slots alike, and it is only there while Alt is held, so it never clutters the screen.

While Alt is held, a locked item's tooltip also adds a line with the badge, "Locked: sorting and deposits skip it", and a keycap hint for Alt plus click to unlock it. Without Alt the tooltip is the vanilla one.

## Tint

`LockedSlotTint.LOCKED` is `0x60FFAA00`: amber at 38% opacity. It is drawn only for a player who turned it on.

Amber is the vanilla language for a soft warning, and the alpha is deliberately low. At full strength the tint floods the empty pixels around the item and the slot stops reading as a slot; at this strength it is a wash that groups locked slots into a visible region while every item on top of it stays its own colour. The badge is legible at any tint strength, so the tint is free to stay quiet.

## Editing The Badge

The PNG that ships is the badge, and the repository holds nothing else that describes it. Author an edit the way the terminal sprites are authored, as a palette-mapped file kept outside the repository and rendered straight into place. [`terminal-gui-sprites.md`](terminal-gui-sprites.md) sets out the format:

```bash
python3 tools/gui_sprite.py build <scratch>/locked.sprite.txt \
	src/client/resources/assets/total-storage/textures/gui/sprites/slot/locked.png
```

Check any edit at every GUI scale before committing, since the badge is the smallest art in the mod and a pixel moved at 4x can close the arch at 1x:

```bash
python3 tools/gui_sprite.py scales <scratch>/locked.sprite.txt <scratch>/locked.png
```
