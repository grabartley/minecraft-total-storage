# Terminal GUI Sprites

The art for the storage terminal screen. This is the reference the screen implementation reads so it never has to guess a size, a border, or which state sprite means what.

Sprites live in the client source set, because a dedicated server must never load them:

```
src/client/resources/assets/too-many-chests/textures/gui/sprites/<path>.png
```

Anything under `textures/gui/sprites/` is stitched into the GUI atlas automatically. Draw one with its identifier, not with UV coordinates:

```java
context.drawGuiTexture(Identifier.of("too-many-chests", "panel/background"), x, y, width, height);
```

Why sprites rather than one sheet is recorded in [ADR 0017](adr/0017-terminal-gui-art-ships-as-atlas-sprites.md).

## Sprite Reference

`Scaling` is what happens when the sprite is drawn at a size other than its native one. `stretch` sprites are meant to be drawn at their native size. `nine_slice` sprites keep their border at any size and stretch only the middle, so a border of 4 means the outer 4 pixels on each side are fixed.

| Sprite id | Size | Scaling | Use |
|---|---|---|---|
| `panel/background` | 32x32 | nine_slice 6 | The terminal window chassis |
| `panel/inset` | 16x16 | nine_slice 4 | Recessed screen area holding the grid and the filter column |
| `search/box` | 16x16 | nine_slice 4 | Search field, unfocused |
| `search/box_focused` | 16x16 | nine_slice 4 | Search field, focused |
| `filter/row` | 16x16 | nine_slice 4 | Tag row, resting |
| `filter/row_hover` | 16x16 | nine_slice 4 | Tag row, hovered |
| `filter/row_active` | 16x16 | nine_slice 4 | Tag row, filtering |
| `slot/cell` | 20x20 | stretch | Grid cell, resting |
| `slot/cell_hover` | 20x20 | stretch | Grid cell, hovered |
| `slot/cell_selected` | 20x20 | stretch | Grid cell, selected |
| `scroll/track` | 8x16 | nine_slice 3 | Scrollbar channel |
| `scroll/thumb` | 8x16 | nine_slice 3 | Scrollbar thumb |
| `scroll/thumb_disabled` | 8x16 | nine_slice 3 | Scrollbar thumb when the content already fits |
| `tab/unselected` | 16x16 | nine_slice 4 | Section tab, unselected |
| `tab/selected` | 16x16 | nine_slice 4 | Section tab, selected |
| `button/normal` | 16x16 | nine_slice 4 | Toolbar button, resting |
| `button/hover` | 16x16 | nine_slice 4 | Toolbar button, hovered |
| `button/disabled` | 16x16 | nine_slice 4 | Toolbar button, unavailable |
| `icon/deposit_all` | 12x12 | stretch | Deposit all action |
| `icon/rescan` | 12x12 | stretch | Rescan the network |
| `icon/highlight` | 12x12 | stretch | Re-highlight containers in world |
| `icon/sort` | 12x12 | stretch | Sort action |
| `icon/clear_all` | 12x12 | stretch | Clear every active filter |
| `icon/items` | 12x12 | stretch | Item view tab |
| `icon/settings` | 12x12 | stretch | Settings tab |
| `empty/network` | 32x32 | stretch | The network holds no containers |
| `empty/no_results` | 32x32 | stretch | The search and filters match nothing |

## Layout Rules

- **Grid cells are 20x20 and the item is drawn at +2,+2.** The 16x16 item sits inside a one pixel bevel and a one pixel gutter. The gutter is where every cell state is drawn, so it must stay clear of the item.
- **Cells tile with no spacing.** Adjacent gutters read as a two pixel separator, which is what makes a dense grid legible.
- **Icons are 12x12 and centre in a 20x20 button at +4,+4.**
- **The toolbar is a column down the right of the window, not a row above the recess.** The window is already as tall as vanilla will draw one on a 320x240 screen, so a row would have cost the grid a line of cells. Buttons stack downwards from the top of the recess, and the window widens to hold them.
- **The filter column has no fixed width.** Every filter sprite is nine-sliced horizontally, so the column may be any width without clipping the grid; widen the window rather than the grid when the column grows.
- **The scrollbar is 8 wide.** The track takes the full height of the grid area; the thumb is nine-sliced vertically and may be any height down to 6.

## State Cues

Every state differs from its neighbour by shape, not only by colour, so the screen stays readable for players who cannot separate the accent red from the chassis grey.

| Pair | Shape cue |
|---|---|
| Grid cell resting → hovered | An outline appears in the gutter, where the resting cell has none |
| Grid cell hovered → selected | Corner brackets appear on all four corners |
| Search box unfocused → focused | A second ring plus corner ticks appear |
| Filter row resting → hovered | An outline appears around the whole row |
| Filter row hovered → active | A solid three pixel bar appears on the left edge |
| Tab unselected → selected | The bottom edge opens so the tab merges into the panel, and an accent rail appears under the top bevel |
| Button resting → hovered | An accent underline appears above the bottom bevel |
| Button resting → disabled | The bevel is removed entirely and the button reads flat |
| Scroll thumb → disabled | The bevel is removed entirely |
| Empty network vs no results | A dashed crate outline against a magnifier: different silhouettes, not a recoloured one |

## Palette

| Key | Hex | Role |
|---|---|---|
| `W` | `F0F0F5` | Bevel highlight, up and left |
| `L` | `C9C9D4` | Chassis light |
| `M` | `ACACB8` | Chassis base |
| `D` | `8A8A96` | Chassis shadow |
| `K` | `5E5E6A` | Bevel dark, down and right |
| `B` | `30303A` | Outer border |
| `n` | `15181E` | Screen recess floor |
| `p` | `1E222B` | Screen base |
| `q` | `2A303B` | Screen rule |
| `s` | `6B6B77` | Slot floor |
| `t` | `474751` | Slot recess wall |
| `x` | `8A211A` | Accent dark |
| `r` | `C43A2E` | Accent base |
| `R` | `ED6353` | Accent light |
| `i` | `E4E9F2` | Glyph light |
| `j` | `9AA3B3` | Glyph mid |
| `o` | `22262E` | Glyph dark |
| `e` | `4FD08A` | Phosphor |
| `E` | `9CF0C2` | Phosphor light |

The accent is redstone red because redstone power is the terminal's on switch, so the block and its screen share one visual idea.

## Editing A Sprite

The PNGs are generated. The reviewable source for each one is a palette-mapped text file under `art/gui/`, mirroring the sprite path:

```
art/gui/panel/background.sprite.txt  ->  .../textures/gui/sprites/panel/background.png
```

Each source carries its own palette and a `# size = WxH` line, so it is self-describing and a change to it reviews as a text diff showing exactly which pixels moved.

Edit the source, then regenerate every PNG:

```bash
python3 tools/gui_sprite.py buildall art/gui \
	src/client/resources/assets/too-many-chests/textures/gui/sprites
```

`tools/gui_sprite.py` also previews without launching the game:

```bash
# blow one sprite up
python3 tools/gui_sprite.py zoom art/gui/slot/cell.sprite.txt /tmp/cell.png 12

# a labelled contact sheet of several
python3 tools/gui_sprite.py sheet /tmp/sheet.png art/gui/icon/*.sprite.txt

# apply the game's nine-slice rule at a real widget size
python3 tools/gui_sprite.py slice art/gui/panel/background.sprite.txt /tmp/panel.png 176 120 6

# 1x through 4x legibility strip
python3 tools/gui_sprite.py scales art/gui/icon/sort.sprite.txt /tmp/sort.png
```

Use `slice` on anything nine-sliced before committing. A detail that sits in a stretched band smears, and this is where that shows up rather than in a screenshot.

A sprite's `.png.mcmeta` declares its scaling. It is written by hand alongside the PNG and must agree with the table above; a border larger than half the texture leaves no centre and the sprite will not draw.
