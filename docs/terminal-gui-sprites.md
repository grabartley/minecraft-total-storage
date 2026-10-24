# Terminal GUI Sprites

The art for the storage terminal screen. This is the reference the screen implementation reads so it never has to guess a size, a border, or which state sprite means what.

Every colour and every bevel here is Minecraft's. A terminal opened beside a chest wears the same chassis, the same slot frames and the same label grey, because the chrome is copied from vanilla's own textures rather than designed to sit beside them. [ADR 0035](adr/0035-the-terminal-wears-vanillas-chrome.md) records why.

Sprites live in the client source set, because a dedicated server must never load them:

```
src/client/resources/assets/total-storage/textures/gui/sprites/<path>.png
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
| `chrome/pressed` | 16x16 | nine_slice 3 | A control that has receded: a tab that is not the current one |
| `chrome/flat` | 16x16 | nine_slice 3 | A resting list row, and an unavailable control |
| `chrome/field` | 16x16 | nine_slice 3 | Text field, unfocused |
| `chrome/field_focused` | 16x16 | nine_slice 3 | Text field, focused |
| `icon/check` | 6x6 | stretch | The tick inside a ticked checkbox |
| `icon/deposit_all` | 12x12 | stretch | Deposit all action |
| `icon/rescan` | 12x12 | stretch | Rescan the network |
| `icon/highlight` | 12x12 | stretch | Re-highlight containers in world |
| `icon/clear_all` | 8x8 | stretch | Clear every active filter |
| `icon/items` | 12x12 | stretch | Item view tab |
| `icon/settings` | 12x12 | stretch | Settings tab |
| `icon/sort_network` | 12x12 | stretch | Sort every container in the network |
| `icon/consolidate` | 12x12 | stretch | Gather scattered stacks together |
| `empty/network` | 32x32 | stretch | The network holds no containers |
| `empty/no_results` | 32x32 | stretch | The search and filters match nothing |

`slot/locked`, `icon/sort`, `sort_button/*` and `quick_stack_button/*` belong to their own features rather than to the terminal screen, and are documented in [`locked-slot-art.md`](locked-slot-art.md) and [`sort-button-art.md`](sort-button-art.md). They ship in this same tree, use this same palette, and are held to it by the same test.

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
- **A tag prompt is a list with its own 4-wide scrollbar**, the filter column's width, beside its rows. It is centred on the grid recess rather than the grid, which is what gives its footer room for two lines.
- **Tooltips wrap at 200 scaled pixels**, or at half the screen when that is narrower, so a long line never pushes a tooltip off the edge at GUI scale 4.

## State Cues

Every state differs from its neighbour by shape, not only by colour, so the screen stays readable for players who cannot separate the accent red from the chassis grey.

| Pair | Shape cue |
|---|---|
| Row resting → hovered | The flat face gains a bevel and a second highlight ring |
| Row hovered → active | A two pixel accent bar is painted down the left edge, over whichever chrome the row is already wearing |
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

No sprite paints anything else. One colour is the mod's own, `C43A2E`, and it is painted rather than shipped: `TerminalChrome` fills a two pixel bar down the left edge of a row, over whichever chrome that row is already wearing, so an active row keeps its hover state instead of trading it away. The accent is redstone red because redstone power is the terminal's on switch, so the block and its screen share one visual idea. It marks a row that is switched on or armed, which is three things: a tag that is currently filtering the listing, a container the network may not write to, and a tag armed for deletion. None of them leans on the colour, since the bar is a shape and each row also says in words what it is doing. A tag an item already carries is the one active state that does not take the bar: its row holds a ticked checkbox where the bar would go, and a tick already says it better.

Two colours the mod draws are deliberately outside this palette and belong to other vocabularies: the in-world container outlines in `HighlightColour`, which have to read against arbitrary world geometry rather than against a grey panel, and the locked-slot wash in `LockedSlotTint`, documented in [`locked-slot-art.md`](locked-slot-art.md). Neither is chrome.

Text reuses chrome rather than inventing greys: secondary text is the chassis shadow, unavailable text is the recessed floor, and a text field's content is the bevel highlight, which is what vanilla's own in-container search field uses. The one other text colour is `FF6060`, which is what vanilla writes "too expensive" in on an anvil.

## Editing A Sprite

The PNG that ships is the sprite, and the repository holds no second copy of it. Nothing stands between an edit and what the game loads, so what a reviewer sees under `textures/gui/sprites/` is exactly what a player gets. [ADR 0037](adr/0037-the-shipped-png-is-a-textures-only-source.md) records why the text sources that used to sit beside these PNGs are gone.

Pixel art is still easier to write as text than to push around in an editor, so `tools/gui_sprite.py` renders a palette-mapped file into place. Keep that file wherever scratch work lives, outside the repository, and commit only the PNG:

```bash
python3 tools/gui_sprite.py build <scratch>/panel.sprite.txt \
	src/client/resources/assets/total-storage/textures/gui/sprites/chrome/panel.png
```

A source is a palette block of `# k = RRGGBB[AA]` lines, a `# size = WxH` line, and one line per pixel row, each character a palette key or `.` for transparent. An unknown character or the wrong row count is an error rather than a silent crop. The tool also previews without launching the game:

```bash
# blow one sprite up
python3 tools/gui_sprite.py zoom <scratch>/sunken.sprite.txt <scratch>/sunken.png 12

# a labelled contact sheet of several
python3 tools/gui_sprite.py sheet <scratch>/sheet.png <scratch>/*.sprite.txt

# apply the game's nine-slice rule at a real widget size
python3 tools/gui_sprite.py slice <scratch>/panel.sprite.txt <scratch>/panel.png 176 120 4

# 1x through 4x legibility strip
python3 tools/gui_sprite.py scales <scratch>/sort.sprite.txt <scratch>/sort.png
```

Use `slice` on anything nine-sliced before committing. A detail that sits in a stretched band smears, and this is where that shows up rather than in a screenshot.

A sprite's `.png.mcmeta` declares its scaling. It is written by hand alongside the PNG and must agree with the table above. `TerminalSpriteTest` walks the whole shipped sprite tree, not just the catalogue, and fails the build if a sprite's metadata disagrees with its texture, if a sprite that is stretched to fit declares no scaling, if a marking sits in a band that tiles, or if any pixel anywhere is a colour that is not in the palette above.
