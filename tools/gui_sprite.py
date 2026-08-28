#!/usr/bin/env python3
"""Render palette-mapped text sprite sources into GUI sprite PNGs, and preview them.

Usage:
  gui_sprite.py build  <source.sprite.txt> <out.png>
  gui_sprite.py zoom   <source.sprite.txt> <out.png> [factor]
  gui_sprite.py sheet  <out.png> <source.sprite.txt> [...]        contact sheet, labelled
  gui_sprite.py slice  <source.sprite.txt> <out.png> <w> <h> <border>
  gui_sprite.py scales <source.sprite.txt> <out.png>              1x/2x/3x/4x strip
  gui_sprite.py buildall <src-root> <out-root>                    mirror a tree of sources

A source is a palette block of `# k = RRGGBB[AA]` comment lines, a `# size = WxH` line, and
then one line per pixel row where each character is a palette key or `.`/space for
transparent. A short row is padded; a long row, an unknown character or the wrong row count
is an error rather than a silent crop.

`slice` applies the same nine-slice rule the game does, so a border that does not survive
being stretched to a real widget size shows up here rather than in a screenshot.
"""
import os
import re
import sys
from PIL import Image

TRANSPARENT = ".", " "
SLATE = (0x2B, 0x2B, 0x2E, 255)
LABEL = (0xC8, 0xC8, 0xD2, 255)


def parse(path):
    palette = {}
    size = None
    rows = []
    for raw in open(path, encoding="utf-8").read().splitlines():
        line = raw.rstrip("\n")
        if line.startswith("#"):
            body = line.lstrip("#").strip()
            dimensions = re.fullmatch(r"size\s*=\s*(\d+)x(\d+)", body)
            if dimensions:
                size = (int(dimensions.group(1)), int(dimensions.group(2)))
                continue
            entry = re.fullmatch(r"(\S)\s*=\s*#?([0-9A-Fa-f]{6}(?:[0-9A-Fa-f]{2})?)(\s.*)?", body)
            if entry:
                value = entry.group(2)
                channels = [int(value[i : i + 2], 16) for i in range(0, len(value), 2)]
                if len(channels) == 3:
                    channels.append(255)
                palette[entry.group(1)] = tuple(channels)
            continue
        if not line.strip() and not rows:
            continue
        rows.append(line)

    while rows and not rows[-1].strip():
        rows.pop()
    if size is None:
        raise SystemExit(f"{path}: missing a `# size = WxH` line")
    width, height = size
    if len(rows) != height:
        raise SystemExit(f"{path}: expected {height} art rows, found {len(rows)}")
    for y, row in enumerate(rows):
        if len(row) > width:
            raise SystemExit(f"{path}: row {y} is {len(row)} chars, canvas is {width}")
        for x, char in enumerate(row):
            if char not in TRANSPARENT and char not in palette:
                raise SystemExit(f"{path}: row {y} col {x}: {char!r} is not in the palette")
    return palette, size, rows


def render(path):
    palette, (width, height), rows = parse(path)
    image = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, char in enumerate(row.ljust(width)):
            if char not in TRANSPARENT:
                image.putpixel((x, y), palette[char])
    return image


def nine_slice(image, target_w, target_h, border):
    """Stretch like the game's nine-slice: corners fixed, edges and centre stretched."""
    left = top = right = bottom = border
    src_w, src_h = image.size
    out = Image.new("RGBA", (target_w, target_h), (0, 0, 0, 0))
    mid_src_w, mid_src_h = src_w - left - right, src_h - top - bottom
    mid_dst_w, mid_dst_h = max(0, target_w - left - right), max(0, target_h - top - bottom)

    def blit(box, dest, size):
        if size[0] <= 0 or size[1] <= 0:
            return
        out.paste(image.crop(box).resize(size, Image.NEAREST), dest)

    blit((0, 0, left, top), (0, 0), (left, top))
    blit((src_w - right, 0, src_w, top), (target_w - right, 0), (right, top))
    blit((0, src_h - bottom, left, src_h), (0, target_h - bottom), (left, bottom))
    blit((src_w - right, src_h - bottom, src_w, src_h), (target_w - right, target_h - bottom), (right, bottom))
    blit((left, 0, src_w - right, top), (left, 0), (mid_dst_w, top))
    blit((left, src_h - bottom, src_w - right, src_h), (left, target_h - bottom), (mid_dst_w, bottom))
    blit((0, top, left, src_h - bottom), (0, top), (left, mid_dst_h))
    blit((src_w - right, top, src_w, src_h - bottom), (target_w - right, top), (right, mid_dst_h))
    blit((left, top, src_w - right, src_h - bottom), (left, top), (mid_dst_w, mid_dst_h))
    if mid_src_w <= 0 or mid_src_h <= 0:
        raise SystemExit(f"border {border} leaves no centre in a {src_w}x{src_h} sprite")
    return out


def text(draw_on, xy, message):
    from PIL import ImageDraw

    ImageDraw.Draw(draw_on).text(xy, message, fill=LABEL)


def contact_sheet(sources, zoom=4, pad=14, label_h=14, columns=6):
    tiles = [(os.path.basename(s).replace(".sprite.txt", ""), render(s)) for s in sources]
    cell_w = max(image.width for _, image in tiles) * zoom + pad
    cell_h = max(image.height for _, image in tiles) * zoom + pad + label_h
    rows = (len(tiles) + columns - 1) // columns
    sheet = Image.new("RGBA", (cell_w * min(columns, len(tiles)) + pad, cell_h * rows + pad), SLATE)
    for index, (name, image) in enumerate(tiles):
        col, row = index % columns, index // columns
        x = pad + col * cell_w
        y = pad + row * cell_h
        sheet.alpha_composite(image.resize((image.width * zoom, image.height * zoom), Image.NEAREST), (x, y))
        text(sheet, (x, y + image.height * zoom + 2), f"{name} {image.width}x{image.height}")
    return sheet


def scale_strip(image):
    zooms = (1, 2, 3, 4)
    pad = 10
    width = sum(image.width * z for z in zooms) + pad * (len(zooms) + 1)
    height = image.height * max(zooms) + pad * 2
    strip = Image.new("RGBA", (width, height), SLATE)
    x = pad
    for zoom in zooms:
        scaled = image.resize((image.width * zoom, image.height * zoom), Image.NEAREST)
        strip.alpha_composite(scaled, (x, (height - scaled.height) // 2))
        x += scaled.width + pad
    return strip.resize((width * 4, height * 4), Image.NEAREST)


def build_all(src_root, out_root):
    count = 0
    for directory, _, files in os.walk(src_root):
        for name in sorted(files):
            if not name.endswith(".sprite.txt"):
                continue
            source = os.path.join(directory, name)
            relative = os.path.relpath(source, src_root).replace(".sprite.txt", ".png")
            target = os.path.join(out_root, relative)
            os.makedirs(os.path.dirname(target), exist_ok=True)
            render(source).save(target)
            print(f"wrote {target}")
            count += 1
    print(f"{count} sprites")


def main():
    if len(sys.argv) < 3:
        raise SystemExit(__doc__)
    mode = sys.argv[1]
    if mode == "buildall":
        build_all(sys.argv[2], sys.argv[3])
        return
    if mode == "sheet":
        contact_sheet(sys.argv[3:]).save(sys.argv[2])
        print(f"wrote {sys.argv[2]}")
        return

    source, out = sys.argv[2], sys.argv[3]
    image = render(source)
    if mode == "build":
        os.makedirs(os.path.dirname(out) or ".", exist_ok=True)
        image.save(out)
    elif mode == "zoom":
        zoom = int(sys.argv[4]) if len(sys.argv) > 4 else 12
        pad = 28
        scaled = image.resize((image.width * zoom, image.height * zoom), Image.NEAREST)
        sheet = Image.new("RGBA", (scaled.width + pad * 2, scaled.height + pad * 2), SLATE)
        sheet.alpha_composite(scaled, (pad, pad))
        sheet.save(out)
    elif mode == "slice":
        target_w, target_h, border = int(sys.argv[4]), int(sys.argv[5]), int(sys.argv[6])
        stretched = nine_slice(image, target_w, target_h, border)
        zoom = 4
        pad = 20
        scaled = stretched.resize((target_w * zoom, target_h * zoom), Image.NEAREST)
        sheet = Image.new("RGBA", (scaled.width + pad * 2, scaled.height + pad * 2), SLATE)
        sheet.alpha_composite(scaled, (pad, pad))
        sheet.save(out)
    elif mode == "scales":
        scale_strip(image).save(out)
    else:
        raise SystemExit(f"unknown mode {mode!r}")
    print(f"wrote {out}")


if __name__ == "__main__":
    main()
