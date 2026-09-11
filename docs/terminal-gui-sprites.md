# Terminal GUI Sprites

The art for the storage terminal screen. This is the reference the screen implementation reads so it never has to guess a size, a border, or which state sprite means what.

Every colour and every bevel here is Minecraft's. A terminal opened beside a chest wears the same chassis, the same slot frames and the same label grey, because the chrome is copied from vanilla's own textures rather than designed to sit beside them. [ADR 0035](adr/0035-the-terminal-wears-vanillas-chrome.md) records why.

Sprites live in the client source set, because a dedicated server must never load them:

```
src/client/resources/assets/too-many-chests/textures/gui/sprites/<path>.png
```

Anything under `textures/gui/sprites/` is stitched into the GUI atlas automatically. The screen never names a sprite by string. `TerminalSprite` is the catalogue, and a widget draws through it:

```java
TerminalSprite.SUNKEN.draw(context, x, y, width, height);
```

Why sprites rather than one sheet is recorded in [ADR 0017](adr/0017-terminal-gui-art-ships-as-atlas-sprites.md).

## Sprite Reference

The set is named for the chrome it draws, not for the widget that draws it, so a recessed row, a recessed scrollbar channel and a slot are one sprite rather than three identical ones.

`Scaling` is what happens when the sprite is drawn at a size other than its native one. `stretch` sprites are meant to be drawn at their native size. `nine_slice` sprites keep their border at any size and tile only the edges and the middle, so a border of 3 means the outer 3 pixels on each side are fixed.

| Sprite id | Size | Scaling | Use |
|---|---|---|---|
| `chrome/panel` | 16x16 | nine_slice 4 | The window chassis, and the panel behind a prompt |
| `chrome/sunken` | 16x16 | nine_slice 1 | Any recessed surface: a slot, a grid cell, the screen recess, a scrollbar channel, a checkbox |
| `chrome/raised` | 16x16 | nine_slice 3 | A raised control: a resting button, the current tab, a scrollbar thumb |
| `chrome/raised_lit` | 16x16 | nine_slice 3 | A raised control under the pointer |
| `chrome/raised_active` | 16x16 | nine_slice 4 | A control that is doing something, carrying the accent bar |
| `chrome/pressed` | 16x16 | nine_slice 3 | A control that has receded: a tab that is not the current one |
| `chrome/flat` | 16x16 | nine_slice 3 | A resting list row, and an unavailable control |
| `chrome/field` | 16x16 | nine_slice 3 | Text field, unfocused |
| `chrome/field_focused` | 16x16 | nine_slice 3 | Text field, focused |
| `icon/check` | 6x6 | stretch | The tick inside a ticked checkbox |
| `icon/deposit_all` | 12x12 | stretch | Deposit all action |
| `icon/rescan` | 12x12 | stretch | Rescan the network |
| `icon/highlight` | 12x12 | stretch | Re-highlight containers in world |
| `icon/sort` | 12x12 | stretch | Sort action |
| `icon/clear_all` | 8x8 | stretch | Clear every active filter |
| `icon/items` | 12x12 | stretch | Item view tab |
| `icon/settings` | 12x12 | stretch | Settings tab |
| `empty/network` | 32x32 | stretch | The network holds no containers |
| `empty/no_results` | 32x32 | stretch | The search and filters match nothing |

`slot/locked` and `sort_button/*` belong to their own features and are documented in [`locked-slot-art.md`](locked-slot-art.md) and [`sort-button-art.md`](sort-button-art.md). They use this same palette.

## Layout Rules

- **A grid cell is a vanilla slot.** 18x18, with the 16x16 item drawn at `+1,+1`, which is exactly what a chest does. `TerminalScreenLayout.SLOT_SIZE` is the one constant behind both the grid and the player inventory, so the grid's nine columns line up with the nine below them rather than merely resembling them.
- **Cells tile with no spacing.** Adjacent slot frames share their bevel the way vanilla's do, which is what makes a dense grid legible without a gutter.
- **Hover is not a sprite.** A hovered cell is drawn by `HandledScreen.drawSlotHighlight`, the same call vanilla makes for a hovered slot, so it highlights identically.
- **Item overlays go through `DrawContext.drawItemInSlot`**, so a damaged tool shows vanilla's durability bar and an item on cooldown shows vanilla's sweep. The aggregated count is drawn over that, scaled down when the compacted label is wider than the item.
- **Icons are 12x12 and centre in a 20x20 button at `+4,+4`.**
- **A settings row is 18px**, the slot pitch again, which is what lets a container row carry a 16x16 item icon and a settings row carry a 16x16 control.
- **The settings view splits into sections behind a strip of tabs.** Each section gets four 18px rows, which is what the recess height allows.
- **The toolbar is a column down the right of the window, not a row above the recess.** A row would have cost the grid a line of cells.
- **The filter column has no fixed width.** Every row sprite is nine-sliced, so the column may be any width without clipping the grid; widen the window rather than the grid when the column grows.
- **The scrollbar is 8 wide.** The track takes the full height of the grid area; the thumb is nine-sliced vertically and may be any height down to 6.

## State Cues

Every state differs from its neighbour by shape, not only by colour, so the screen stays readable for players who cannot separate the accent red from the chassis grey.

| Pair | Shape cue |
|---|---|
| Row resting → hovered | The flat face gains a bevel and a second highlight ring |
| Row hovered → active | The bevel stays and a two pixel accent bar appears on the left edge |
| Button resting → hovered | A second highlight ring closes around the face |
| Button resting → disabled | The bevel is removed entirely and the button reads flat |
| Tab current → not current | The bevel inverts, so the tab reads pressed in rather than standing proud |
| Search box unfocused → focused | A second ring appears inside the first, so focus reads as a deeper recess |
| Grid cell resting → hovered | Vanilla's own slot highlight is painted over the item |
| Checkbox unticked → ticked | A tick appears inside the recess |
| Scroll thumb → disabled | The bevel is removed entirely |
| Container row, general → excluded | The accent bar appears on the left edge, and the role is written out beside it |
| Empty network vs no results | A dashed crate outline against a magnifier: different silhouettes, not a recoloured one |
| In-world outline, general vs excluded | The excluded container is outlined twice, one box inset inside the other, so it reads as a doubled line rather than a differently coloured one |

## Palette

Every chrome colour is sampled from a Minecraft texture, and `VanillaPaletteTest` reads that texture off the test classpath and fails if a constant drifts from it.

| Key | Hex | Role | Where vanilla paints it |
|---|---|---|---|
| `N` | `000000` | Chassis outline | The black edge around a container window |
| `B` | `373737` | Sunken shadow, up and left | A slot's top and left edge |
| `K` | `555555` | Bevel shadow, down and right | The chassis's bottom and right bevel |
| `D` | `8B8B8B` | Recessed floor, and a muted glyph | A slot's interior |
| `M` | `C6C6C6` | Container face | The chassis itself |
| `W` | `FFFFFF` | Bevel highlight, up and left | The chassis's top and left bevel, and a slot's bottom and right edge |
| `o` | `404040` | Label text | The title of every vanilla container |
| `r` | `C43A2E` | Accent base | Not vanilla. See below |
| `R` | `ED6353` | Accent light | Not vanilla. See below |

Two colours are the mod's own, and they are the only two. The accent is redstone red because redstone power is the terminal's on switch, so the block and its screen share one visual idea. It appears on exactly two things: a filter that is currently filtering, and a container the network is not allowed to write to. Both are states where "this one is different from the rest" is the entire message, and neither is carried by colour alone.

Text reuses chrome rather than inventing greys: secondary text is the chassis shadow, unavailable text is the recessed floor, and a text field's content is the bevel highlight, which is what vanilla's own in-container search field uses. The one other text colour is `FF6060`, which is what vanilla writes "too expensive" in on an anvil.

## Editing A Sprite

The PNGs are generated. The reviewable source for each one is a palette-mapped text file under `art/gui/`, mirroring the sprite path:

```
art/gui/chrome/panel.sprite.txt  ->  .../textures/gui/sprites/chrome/panel.png
```

Every source carries the same palette block and a `# size = WxH` line, so a change to one reviews as a text diff showing exactly which pixels moved.

Edit the source, then regenerate every PNG:

```bash
python3 tools/gui_sprite.py buildall art/gui \
	src/client/resources/assets/too-many-chests/textures/gui/sprites
```

`tools/gui_sprite.py` also previews without launching the game:

```bash
# blow one sprite up
python3 tools/gui_sprite.py zoom art/gui/chrome/sunken.sprite.txt /tmp/sunken.png 12

# a labelled contact sheet of several
python3 tools/gui_sprite.py sheet /tmp/sheet.png art/gui/icon/*.sprite.txt

# apply the game's nine-slice rule at a real widget size
python3 tools/gui_sprite.py slice art/gui/chrome/panel.sprite.txt /tmp/panel.png 176 120 4

# 1x through 4x legibility strip
python3 tools/gui_sprite.py scales art/gui/icon/sort.sprite.txt /tmp/sort.png
```

Use `slice` on anything nine-sliced before committing. A detail that sits in a stretched band smears, and this is where that shows up rather than in a screenshot.

A sprite's `.png.mcmeta` declares its scaling. It is written by hand alongside the PNG and must agree with the table above; `TerminalSpriteTest` fails the build if it does not, and fails it again if a sprite paints a colour that is not in the palette above.
