# In-Screen Button Art

The buttons this mod draws inside screens it does not own: the one that sorts a container from inside that container's own screen, and the one that quick stacks from inside the player inventory. Unlike every other piece of art in this mod, it is drawn on top of a screen the mod does not own, so it is the one place where the terminal palette is the wrong answer. It wears vanilla's greys instead. The reasoning is recorded in [ADR 0020](adr/0020-the-sort-button-wears-vanilla-chrome.md).

## Sprite Reference

Under `src/client/resources/assets/too-many-chests/textures/gui/sprites/`, in the client source set because a dedicated server must never load them.

Every one is drawn at its native size and carries no `.png.mcmeta`, so the default `stretch` scaling applies and never runs. The glyph is baked into each sprite rather than composed from a separate icon, so there is no offset for an implementation to get wrong.

| Sprite id | Size | Scaling | Use |
|---|---|---|---|
| `sort_button/normal` | 14x14 | stretch | Sort button, resting |
| `sort_button/hover` | 14x14 | stretch | Sort button, hovered |
| `sort_button/pressed` | 14x14 | stretch | Sort button, held |
| `sort_button/disabled` | 14x14 | stretch | Sort button, sorting unavailable |
| `quick_stack_button/normal` | 14x14 | stretch | Quick stack button, resting |
| `quick_stack_button/hover` | 14x14 | stretch | Quick stack button, hovered |
| `quick_stack_button/pressed` | 14x14 | stretch | Quick stack button, held |
| `quick_stack_button/disabled` | 14x14 | stretch | Quick stack button, unavailable |

## Why 14x14

Vanilla's container panel leaves a fixed band of clean face between the top bevel and the first slot row, and that band is what the button has to live in. Measured off `textures/gui/container/generic_54.png`:

| Rows | Content |
|---|---|
| `y = 0` | Outer black border |
| `y = 1` to `y = 2` | White bevel highlight |
| `y = 3` to `y = 16` | Clean `C6C6C6` face, 14 pixels |
| `y = 17` | First slot row's `373737` border |

**Fourteen pixels is the whole budget, so the button is 14x14 and sits flush in it.** A 16x16 button has to overlap either the bevel above or the slot border below; there is no third option, and both read as a control that was dropped on the panel rather than built into it. The double chest is the tightest of the vanilla screens: the hopper and the furnace leave more room at the same anchor, so a button fitted to the chest fits all of them.

There is no small variant. Below 14 the glyph area drops under 8x8 once the outline, bevel and hover gutter are paid for, and at that point the arrowhead collapses into the shaft and the mark stops reading as sorting. A screen too tight for 14x14 is a placement problem, not a size problem, and the anchor belongs outside the panel rather than over a slot.

## Geometry

Four concentric rings, which is what keeps the four states describing one object:

| Ring | Rows and columns | Carries |
|---|---|---|
| Outline | 0 | Separates the button from the container face behind it |
| Bevel | 1 | Raised when resting, inverted when held, absent when disabled |
| Gutter | 2 | Empty until hover, when the highlight ring closes through it |
| Glyph | 3 to 10, an 8x8 area | Three descending bars and a downward arrow, or two items dropping into a tray |

The quick stack button is the same object wearing a different glyph. Every ring is identical, so the two sit beside each other as one control family rather than as two buttons that happen to be the same size, and only the 8x8 glyph area differs: two shafts with arrowheads falling into an open tray, which is the deposit mark doubled rather than a new vocabulary.

## Placement

Anchor the button into the band, right aligned to the same 8 pixel inset vanilla gives the title on the left:

```
x = backgroundWidth - 8 - 14
y = 3
```

On every vanilla container that is `x = 154, y = 3` relative to the panel origin, clear of the title text, which ends well short of it, and clear of the slot grid, whose right edge stops at `x = 169`.

The quick stack button takes the same anchor on the player inventory, `x = 154, y = 3`, because on that screen it is the only place in the band that is free. The `Crafting` label starts at `x = 97` and runs to roughly `x = 133`, so a button at `x = 136` lands hard against the final letter, which is exactly what it looked like the first time: a control dropped on top of the title rather than built into the panel. Everything from the label's end to `x = 154` is too narrow for 14 pixels plus a gutter.

The consequence for a screen that eventually wants two of these: they cannot both live in the band on the player inventory, and the second one belongs outside the panel rather than over the label or a slot. The inventory panel also moves when the recipe book opens, so the button follows the panel every frame rather than being placed once.

The button must never overlap a slot. It is a click target sitting in someone else's screen, and a click that lands on both is a click that does the wrong thing.

## Anchors

A player who wants these buttons somewhere else moves them with `screenButtonAnchor` in `config/too-many-chests/client.json`. The anchor moves every button this mod draws on a screen it does not own, so they stay one control family wherever they land:

| Anchor | First button | Every button after it |
|---|---|---|
| `inside_top_right` | In the band, `x = backgroundWidth - 8 - 14`, `y = 3` | Down the right side, outside the panel |
| `outside_top_right` | Down the right side, aligned with the band | Down the right side |
| `outside_bottom_right` | Up the right side, aligned with the panel's bottom inset | Up the right side |

Every anchor is measured from the panel origin and re-read every frame, so the buttons follow the panel when the recipe book opens and at every GUI scale. The outside column sits two pixels clear of the panel and never crosses it, and the left side is not offered: that is where vanilla opens the recipe book.

## State Cues

Every state differs from its neighbour by shape and not only by colour, the same rule the terminal sprites follow, so the button stays readable for a player who cannot separate the greys.

| Pair | Shape cue |
|---|---|
| Resting → hovered | The bevel highlight closes into a full ring around the face, where the resting button highlights only the top and left |
| Resting → held | The bevel inverts and the glyph moves down and right by one pixel |
| Resting → disabled | The bevel is removed entirely and the button reads flat |

The hover ring is white rather than dark, and that is not a free choice. The glyph runs edge to edge inside its 8x8 area: the bars start hard against the left of it and the arrowhead ends hard against the right. A dark ring in the gutter sits directly against both and swallows them, so the mark loses its ends exactly when the player is pointing at it. A white ring separates the glyph instead of absorbing it, and closing the highlight all the way round is a shape the resting button does not have. It is also the whole of the hover cue: the face stays `C6C6C6`, because a lit face would need a grey vanilla does not paint.

The held state deliberately lands on vanilla's own recessed treatment: a dark outline, a dark top-left edge, a light bottom-right edge and an `8B8B8B` face is exactly how vanilla draws an inventory slot. A held button reading as a recess is the point, and the state lasts only as long as the mouse is down.

Disabled keeps the glyph rather than hiding it, at `8B8B8B` instead of `404040`. A button that empties out reads as broken; a button that goes flat and quiet reads as unavailable.

## Palette

Vanilla's container chrome, sampled from `textures/gui/container/generic_54.png`.

This is the same palette every other sprite in the mod uses, and it is listed in full in [`terminal-gui-sprites.md`](terminal-gui-sprites.md). The button needs six of its keys:

| Key | Hex | Role |
|---|---|---|
| `B` | `373737` | Vanilla slot border, used here as the button outline |
| `W` | `FFFFFF` | Bevel highlight, up and left |
| `K` | `555555` | Bevel shadow, down and right |
| `M` | `C6C6C6` | Vanilla container face |
| `D` | `8B8B8B` | Vanilla chassis shadow, the held face and the disabled glyph |
| `o` | `404040` | Glyph, vanilla's label dark |

## Editing A Sprite

The PNGs that ship are the sprites, exactly as they are for the terminal. Author an edit as a palette-mapped file kept outside the repository and rendered straight into place; [`terminal-gui-sprites.md`](terminal-gui-sprites.md) sets out the format:

```bash
python3 tools/gui_sprite.py build <scratch>/sort_button/normal.sprite.txt \
	src/client/resources/assets/too-many-chests/textures/gui/sprites/sort_button/normal.png
```

Check any change at the size it ships at, not zoomed in:

```bash
# 1x through 4x legibility strip
python3 tools/gui_sprite.py scales <scratch>/sort_button/normal.sprite.txt <scratch>/sort.png

# every state side by side
python3 tools/gui_sprite.py sheet <scratch>/states.png <scratch>/sort_button/*.sprite.txt
```

Nothing here is nine-sliced, so `slice` has nothing to say about it. The check that matters is the one above at GUI scale 1, where the glyph has eight pixels to work with and every pixel is load bearing.
