# Terminal Block Art

The visual and audio identity of the storage terminal block, and the contract it puts on the block's registration. The screen the block opens has its own reference in [`terminal-gui-sprites.md`](terminal-gui-sprites.md); the two share a chassis palette and a redstone accent on purpose, so the block and its interface read as one machine.

## What The Block Must Expose

The art is already cut for a specific blockstate. Registration has to match it or the blockstate file will not resolve:

| Property | Values | Why the art needs it |
|---|---|---|
| `facing` | `north`, `east`, `south`, `west` | The screen is on one face only. The blockstate rotates a single model rather than shipping four. |
| `powered` | `false`, `true` | Each power state is a separate model over a separate texture set. |
| `near_capacity` | `false`, `true` | A powered terminal whose network is past `nearCapacityWarningPercent` grows a bar across the bottom of its screen face. Unpowered variants ignore it and resolve to the resting model. |

`blockstates/terminal.json` covers all sixteen combinations; the eight unpowered ones resolve to `block/terminal` whatever `near_capacity` reads, because an unpowered terminal is holding nothing to be near capacity with. Rotation follows the vanilla convention: the model is authored with its face on north, and `y` is `0`, `90`, `180`, `270` for north, east, south and west.

## Textures

All 16x16 and fully opaque, under `assets/too-many-chests/textures/block/`.

| Texture | Face | Unpowered | Powered |
|---|---|---|---|
| `terminal_front` / `terminal_front_on` | The screen face | Blank screen with a glint, dull indicator and dust run | Three readout lines, lit indicator, live dust run |
| `terminal_side` / `terminal_side_on` | Left, right and back | Dark louvre slots between posts | Louvre slots carrying current |
| `terminal_top` / `terminal_top_on` | Top | Cold vents, dull dust run | Lit vents, live dust run |
| `terminal_bottom` | Bottom | One texture for both states | |

The back uses the side texture, as vanilla does for the furnace and the dispenser.

**Power is signalled by content, not by tint.** The unpowered screen is blank and the powered one carries readout lines that simply are not present when the block is off. That difference survives greyscale, which a colour swap would not, and it is what makes the state legible at distance. The lit vents and dust run repeat the signal on the top and flanks, so a terminal read from behind or from above still reports its state.

Two complete texture sets rather than one set plus an overlay is the vanilla pattern, matching the furnace, the redstone lamp and the observer. It costs three extra textures and needs no emissive support, no block entity renderer and no shader.

## Models

| Model | Purpose |
|---|---|
| `block/terminal` | Unpowered, `minecraft:block/cube` with per-face textures |
| `block/terminal_on` | Powered, the same mapping over the `_on` textures |
| `block/terminal_full` | Powered and near capacity: the same cube written out as explicit elements, plus a 12x3x1 bar standing one pixel proud of the screen face |
| `item/terminal` | Inherits `block/terminal`, so the inventory icon shows the resting state |

`terminal_full` carries no texture of its own. The bar is mapped from `terminal_top_on`, which is already a lit vent run, so the near-capacity state costs a model and no new art. It is deliberately a **protruding shape** rather than a tint: TERM-15 and A11Y-1 both ask that a state distinction survive greyscale and distance, and a silhouette does where a colour swap does not.

## Sounds

Mono Vorbis under `assets/too-many-chests/sounds/terminal/`, registered through `sounds.json`. Registration has to declare these four identifiers:

| Sound event | File | Length | Intent |
|---|---|---|---|
| `block.terminal.power_on` | `terminal/power_on.ogg` | 0.90s | A soft rising hum as the block takes power |
| `block.terminal.power_off` | `terminal/power_off.ogg` | 0.55s | A short fall with a mechanical knock at the front |
| `block.terminal.screen_open` | `terminal/screen_open.ogg` | 0.26s | Two quiet blips as the interface opens |
| `block.terminal.deposit_confirm` | `terminal/deposit_confirm.ogg` | 0.18s | A short two-tone confirmation for a completed deposit |

Each carries a `subtitle` key, and the matching strings are in `lang/en_us.json`.

Three constraints hold across the set, and a replacement asset has to keep them:

- **Mono.** The game only pans and attenuates mono sounds by distance. A stereo file plays flat at full volume wherever the player stands, which for a block cue is simply wrong.
- **Peak normalised to the same level**, within a fraction of a decibel. Relative loudness is then decided once at the call site by the volume argument, rather than being baked unevenly into the files.
- **Sine based, with no sawtooth or square anywhere.** The upper harmonics of those waveforms are what make a repeated interface blip fatiguing, and this block sits in a base and is heard constantly.

## Palette

| Key | Hex | Role |
|---|---|---|
| `W` | `D8D8E0` | Bevel highlight |
| `L` | `B4B4C0` | Chassis light, up and left |
| `M` | `90909C` | Chassis base |
| `D` | `6E6E7A` | Chassis shadow |
| `K` | `4C4C56` | Bevel dark, down and right |
| `B` | `2A2A32` | Seam |
| `n` | `12151B` | Vent slot, unlit |
| `p` | `1B1F27` | Screen, unlit |
| `q` | `262C37` | Screen rule |
| `x` | `7A1C16` | Redstone, unpowered |
| `r` | `B93227` | Redstone, powered |
| `R` | `F05A45` | Redstone, powered highlight |
| `Y` | `FF9A6E` | Redstone glow, powered only |
| `i` | `C8CDD6` | Rivet light |
| `j` | `8A93A3` | Rivet mid |

## Regenerating The Assets

The sounds are generated and their generator is committed. The textures are not: the PNG that ships is the only copy of a texture this repository holds, for the reasons in [ADR 0037](adr/0037-the-shipped-png-is-a-textures-only-source.md).

Pixel art is still easier to write as text, and the GUI sprite tool renders a palette-mapped file, using the palette above, straight into place. Keep that file outside the repository and commit only the PNG:

```bash
python3 tools/gui_sprite.py build <scratch>/terminal_front.sprite.txt \
	src/main/resources/assets/too-many-chests/textures/block/terminal_front.png
```

Sounds are synthesised rather than recorded, so the source of truth is the synthesis itself:

```bash
python3 tools/terminal_sounds.py \
	src/main/resources/assets/too-many-chests/sounds/terminal
```

That needs `numpy` and either `oggenc` or an `ffmpeg` built with `libvorbis`. Editing a cue means editing its builder function, which states the frequencies, the decay constants and the envelope in one place.
