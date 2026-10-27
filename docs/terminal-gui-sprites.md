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

Where vanilla already ships the right sprite, the terminal draws vanilla's own through `VanillaSprite`, so it changes with a resource pack exactly as a chest or the creative inventory does:

| Vanilla sprite | Use |
|---|---|
| `widget/button`, `widget/button_highlighted`, `widget/button_disabled` | Every text button and icon button |
| `widget/text_field`, `widget/text_field_highlighted` | The search box and every prompt field |
| `container/slot` | A grid cell, and the icon slot in a container row |
| `container/creative_inventory/scroller`, `scroller_disabled` | The grid scroller |
| `icon/search` | The magnifier in the search box |

The mod's own sprites cover what vanilla has no sprite for. The set is named for the chrome it draws, not for the widget that draws it.

`Scaling` is what happens when the sprite is drawn at a size other than its native one. `stretch` sprites are meant to be drawn at their native size. `nine_slice` sprites keep their border at any size and tile only the edges and the middle, so a border of 3 means the outer 3 pixels on each side are fixed.

| Sprite id | Size | Scaling | Use |
|---|---|---|---|
| `chrome/panel` | 16x16 | nine_slice 4 | The window chassis, and the panel behind a prompt |
| `chrome/sunken` | 16x16 | nine_slice 1 | Any recessed surface: the empty grid, a list frame, a scrollbar channel, a checkbox |
| `chrome/raised` | 16x16 | nine_slice 3 | A list scrollbar thumb, and the rename and delete controls on a tag row |
| `chrome/raised_lit` | 16x16 | nine_slice 3 | The rename and delete controls under the pointer |
| `icon/check_light` | 6x6 | stretch | The tick inside a ticked checkbox |
| `icon/cross` | 8x8 | stretch | Clear every active filter |
| `icon/cross_disabled` | 8x8 | stretch | Clear, when nothing is filtering |
| `icon/fuzzy` | 8x8 | stretch | Fuzzy search, when it is on |
| `icon/fuzzy_off` | 8x8 | stretch | Fuzzy search, when it is off |
| `icon/rename` | 8x8 | stretch | Rename a tag |
| `icon/delete` | 8x8 | stretch | Delete a tag |
| `icon/items` | 12x12 | stretch | Items side tab |
| `icon/settings` | 12x12 | stretch | Settings side tab |
| `empty/network` | 32x32 | stretch | The network holds no containers |
| `empty/no_results` | 32x32 | stretch | The search and filters match nothing |

`slot/locked`, `icon/sort`, `sort_button/*` and `quick_stack_button/*` belong to their own features rather than to the terminal screen, and are documented in [`locked-slot-art.md`](locked-slot-art.md) and [`sort-button-art.md`](sort-button-art.md). They ship in this same tree, use this same palette, and are held to it by the same test.

## Layout Rules

- **The window is 278x222**, with the Items and Settings tabs hanging off its right edge. The selected tab joins the panel; the other sits apart.
- **A grid cell is a vanilla slot.** 18x18, with the 16x16 item drawn at `+1,+1`, which is exactly what a chest does. `TerminalScreenLayout.SLOT_SIZE` is the one constant behind both the 9x5 grid and the player inventory, so the grid's nine columns line up with the nine below them.
- **Hover is not a sprite.** A hovered cell is drawn by `HandledScreen.drawSlotHighlight`, the same call vanilla makes for a hovered slot.
- **Item overlays go through `DrawContext.drawItemInSlot`**, so a damaged tool shows vanilla's durability bar and an item on cooldown shows vanilla's sweep. The aggregated count is drawn over that, scaled down when the compacted label is wider than the item.
- **The left column holds the filters above the action dock.** Match all or any and the clear button sit on the search row, and the fuzzy search toggle closes the search row on the right, so the search box stops short of the scrollbar's edge; the dock under the filters shows the network gauge and the current view's text buttons.
- **The settings view is one raised frame behind three section tabs**: Terminal, Containers and Tags. A container row is 21 tall, so it holds a vanilla slot and a 20 tall button with room to spare.
- **Text never runs past its box.** A label that does not fit is cut with an ellipsis, and the full text shows in a tooltip.
- **Prompts are 180 wide and centred over a dimmed terminal.** Every prompt ends in keycap hints, and a refused entry turns the field border red and adds one warning line, without moving anything else.
- **Tooltips wrap at 200 scaled pixels**, or at half the screen when that is narrower, so a long line never pushes a tooltip off the edge at GUI scale 4.

## State Cues

Every state differs from its neighbour by shape, not only by colour, so the screen stays readable for players who cannot separate the accent red from the chassis grey.

| Pair | Shape cue |
|---|---|
| Button resting → hovered | Vanilla's white outline appears around the button |
| Button resting → disabled | Vanilla's flat disabled face, with grey text |
| Side tab current → not current | The current tab joins the panel; the others sit apart, darker and set back |
| Section tab current → not current | The current tab is taller and opens into the frame below it |
| Field unfocused → focused | Vanilla's focused field outline |
| Field accepted → refused | A red border and one warning line under the field |
| Grid cell resting → hovered | Vanilla's own slot highlight is painted over the item |
| Checkbox unticked → ticked | A tick appears inside the recess |
| Fuzzy search on → off | The tilde is struck through |
| Sort or Gather → armed | The button reads "Confirm?" on an accent face |
| Sort or Gather → running | One wide accent button reads "Stop" and the percentage done |
| Status line partial → failed | The same hoverable line, grey for a partial result and dark red for a failure, always led by a warning glyph |
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

No sprite paints anything else. One colour is the mod's own, `C43A2E`, and it is painted rather than shipped. The accent is redstone red because redstone power is the terminal's on switch, so the block and its screen share one visual idea. It marks something armed or running, never decoration: the face of Sort or Gather while it asks "Confirm?" and while it shows "Stop" with the percentage done, a tag armed for deletion, a terminal picked to copy tags from, the Excluded role on a container, and the network gauge when it is nearly full. None of them leans on the colour, since each also says in words what it is doing.

Two colours the mod draws are deliberately outside this palette and belong to other vocabularies: the in-world container outlines in `HighlightColour`, which have to read against arbitrary world geometry rather than against a grey panel, and the locked-slot wash in `LockedSlotTint`, documented in [`locked-slot-art.md`](locked-slot-art.md). Neither is chrome.

Text reuses chrome rather than inventing greys: secondary text is the chassis shadow, unavailable text is the recessed floor, and a text field's content is the bevel highlight, which is what vanilla's own in-container search field uses. The other text colours are `FF6060`, which is what vanilla writes "too expensive" in on an anvil, for a refused entry, and `AA0000`, vanilla dark red, for a failed action in the status line.

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
