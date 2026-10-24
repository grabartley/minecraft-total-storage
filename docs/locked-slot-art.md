# Locked Slot Art

The art that tells a player a slot is locked, and the placement contract it puts on the renderer. It shares the palette in [`terminal-gui-sprites.md`](terminal-gui-sprites.md), so a locked slot in the terminal's player inventory reads as part of the same mod as the terminal screen above it.

A locked slot carries two cues, and they are not interchangeable:

| Cue | Carries | Drawn |
|---|---|---|
| The badge | The lock state | Over the item and its decorations |
| The tint | Reinforcement only | Behind the item, under everything else |

**The badge is the indicator and the tint is the reinforcement.** The tint distinguishes a locked slot by colour alone, which is exactly what a player with a colour vision deficiency cannot use, so it can never be the only cue. That ordering is also why the tint is configurable and the badge is not: turning the tint off costs a player nothing they cannot get from the badge.

## Sprite Reference

Under `src/client/resources/assets/total-storage/textures/gui/sprites/`, in the client source set because a dedicated server must never load it.

| Sprite id | Size | Scaling | Use |
|---|---|---|---|
| `slot/locked` | 7x8 | stretch | Lock badge on a locked inventory slot |

## Placement

The badge sits in the **top-left corner of the 16x16 item area**, at the slot's `x, y` with no offset. Vanilla's own slot decorations are all bottom-anchored, so the top-left corner is the one region a badge can take without contending with them:

| Decoration | Region it owns | Overlap with the badge |
|---|---|---|
| Stack count | Bottom-right, drawn from `y + 9` down | None, the badge's last row is `y + 7` |
| Durability bar | `x + 2, y + 13` to `x + 15, y + 15` | None |
| Cooldown overlay | Bottom-anchored, up to the whole slot | Draws under the badge |

Because the badge is 7x8 of a 16x16 slot it covers just over a fifth of the item, leaving the lower-right two thirds, which is where most vanilla item silhouettes carry their identifying detail.

**Draw order is badge last.** The tint goes down first, then vanilla draws the item and its decorations, then the badge. Drawing the badge before the item hides it behind anything opaque, and drawing the tint after the item washes out the stack it is meant to be describing.

## Why The Badge Reads Everywhere

The badge is a padlock in `FFFFFF` under a one pixel `000000` outline that closes all the way around, including inside the shackle's arch. Both are vanilla's own, from the palette in [`terminal-gui-sprites.md`](terminal-gui-sprites.md), and they are the two furthest apart it holds, which is what this badge needs. Two tones is what makes it independent of whatever is behind it:

- Against a **bright** item such as glowstone dust, the dark outline separates it.
- Against a **dark** item such as coal, the light fill separates it.

Neither tone is doing the job alone, which is what a single-tone badge would need, and it is why the badge does not require an opaque plate behind it to stay legible.

The shape is a padlock silhouette rather than a letter or an abstract mark, so it survives greyscale and needs no translation. The keyhole is a single pixel; at 7x8 it is texture rather than information, and the arch above the body is what a player actually reads.

## Tint

`LockedSlotTint.LOCKED` is `0x60FFAA00`: amber at 38% opacity.

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
