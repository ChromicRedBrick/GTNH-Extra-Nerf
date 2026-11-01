"""Hand-authored pixel textures for an industrial gramophone. Requires Pillow.

The PNG/GIF previews compose the shipped assets; they are not in-game screenshots.
"""

from pathlib import Path
import argparse
import math

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
TEXTURES = ROOT / "src/main/resources/assets/gtnhextranerf/textures"
SOURCE, RECORD, DIRECTION, OUTPUT = (20, 48), (79, 12), (79, 76), (138, 48)
PALETTE = {
    "ink": "#24211f",
    "dark": "#49443e",
    "metal": "#787168",
    "silver": "#bcb5a8",
    "white": "#eee5d2",
    "panel": "#cbc3b3",
    "screen": "#2b2014",
    "amber": "#dca34d",
    "glow": "#ffe6a3",
    "label": "#bd955c",
    "cream": "#e8d5a7",
    "brass": "#a78243",
    "wood": "#825438",
    "wood_dark": "#57432b",
}


def compilation():
    im = Image.new("RGBA", (16, 16))
    d = ImageDraw.Draw(im)
    d.ellipse((1, 1, 14, 14), fill=PALETTE["ink"])
    d.ellipse((2, 2, 13, 13), outline=PALETTE["dark"])
    d.ellipse((4, 4, 11, 11), outline=PALETTE["dark"])
    d.line([(3, 5), (4, 3), (7, 2)], fill=PALETTE["metal"])
    d.line([(12, 10), (11, 12), (8, 13)], fill=PALETTE["metal"])
    d.ellipse((5, 4, 10, 11), fill=PALETTE["label"])
    d.line([(7, 5), (6, 6), (7, 7), (6, 9)], fill=PALETTE["cream"])
    d.line([(9, 6), (8, 7), (9, 9), (8, 10)], fill=PALETTE["cream"])
    d.rectangle((7, 7, 8, 8), fill=PALETTE["ink"])
    return im


def background():
    im = Image.new("RGBA", (176, 106), PALETTE["panel"])
    d = ImageDraw.Draw(im)
    d.rectangle((0, 0, 175, 105), outline=PALETTE["ink"], width=2)
    d.line([(2, 103), (2, 2), (173, 2)], fill=PALETTE["white"], width=2)
    d.line([(173, 3), (173, 103), (3, 103)], fill=PALETTE["dark"], width=2)
    d.rectangle((7, 7, 168, 98), outline=PALETTE["metal"])
    # Cast-metal panel, brass rivets and copper signal conduits.
    for x, y in [(11, 11), (163, 11), (11, 94), (163, 94)]:
        d.rectangle((x, y, x + 2, y + 2), fill=PALETTE["wood_dark"])
        d.point((x, y), fill=PALETTE["brass"])
    for a, b in [((28, 57), (62, 57)), ((112, 57), (145, 57)),
                 ((88, 27), (88, 33)), ((88, 68), (88, 80))]:
        d.line([a, b], fill=PALETTE["dark"], width=3)
        d.line([(a[0], a[1] - 1), (b[0], b[1] - 1)], fill=PALETTE["brass"])
    for x in [54, 130]:
        d.polygon([(x, 53), (x + 4, 57), (x, 61)], fill=PALETTE["dark"])
        d.line([(x, 54), (x + 2, 56)], fill=PALETTE["brass"])
    for x, y in [SOURCE, RECORD, DIRECTION, OUTPUT]:
        d.rectangle((x, y, x + 17, y + 17), fill=PALETTE["ink"])
        d.rectangle((x + 1, y + 1, x + 16, y + 16), fill=PALETTE["dark"])
        d.line([(x + 1, y + 16), (x + 16, y + 16), (x + 16, y + 1)], fill=PALETTE["white"])
        d.line([(x + 2, y + 2), (x + 15, y + 2)], fill=PALETTE["metal"])
    return im


def player(frame):
    im = Image.new("RGBA", (56, 40))
    d = ImageDraw.Draw(im)
    c = lambda name: PALETTE[name]
    d.rectangle((0, 1, 55, 38), fill=c("ink"))
    d.rectangle((1, 2, 54, 36), fill=c("dark"))
    d.rectangle((2, 2, 53, 3), fill=c("silver"))
    d.rectangle((2, 4, 53, 33), fill=c("dark"))
    d.rectangle((2, 4, 3, 33), fill=c("wood"))
    d.rectangle((52, 4, 53, 33), fill=c("wood"))
    d.rectangle((3, 34, 52, 35), fill=c("wood_dark"))
    d.line([(4, 34), (16, 34), (21, 35), (33, 35), (40, 34), (51, 34)], fill=c("wood"))
    for x, y in [(5, 5), (49, 5), (5, 32), (49, 32)]:
        d.point((x, y), fill=c("brass"))
    for x in [4, 49]:
        d.rectangle((x, 36, x + 3, 39), fill=c("ink"))
    # The turntable stays circular and pixel aligned; only highlights rotate.
    d.ellipse((4, 6, 29, 31), fill=c("ink"))
    d.ellipse((6, 8, 27, 29), outline=c("dark"))
    d.ellipse((8, 10, 25, 27), outline=c("dark"))
    for angle in [frame * math.pi / 4, frame * math.pi / 4 + math.pi]:
        points = [(round(16.5 + 10 * math.cos(angle + k * .14)),
                   round(18.5 + 10 * math.sin(angle + k * .14))) for k in range(4)]
        d.line(points, fill=c("metal"))
    d.ellipse((13, 15, 20, 22), fill=c("label"))
    d.line([(16, 16), (15, 18), (18, 19), (17, 21)], fill=c("cream"))
    d.rectangle((16, 18, 17, 19), fill=c("silver"))
    d.line([(29, 7), (27, 7), (26, 17), (22, 21)], fill=c("silver"), width=2)
    d.rectangle((29, 5, 30, 7), fill=c("ink"))
    # CRT, three TUNG pulses and stepped signal bars; no filtering or antialiasing.
    d.rectangle((32, 5, 52, 28), fill=c("brass"))
    d.rectangle((33, 6, 51, 27), fill=c("ink"))
    d.polygon([(35, 7), (49, 7), (50, 8), (50, 25), (49, 26),
               (35, 26), (34, 25), (34, 8)], fill=c("screen"))
    glyphs = ["11110010001000100010", "10011001100110010110",
              "10011101101110011001", "11101000101110010110"]
    for index, glyph in enumerate(glyphs):
        for i, pixel in enumerate(glyph):
            if pixel == "1":
                d.point((33 + index * 5 + i % 4, 8 + i // 4), fill=c("amber"))
    for i in range(6):
        height = 2 + (frame * 3 + i * 5) % 9
        d.rectangle((35 + i * 2, 24 - height, 35 + i * 2, 24), fill=c("amber"))
    for i, x in enumerate([34, 41, 48]):
        lit = frame == [1, 3, 5][i]
        d.rectangle((x - 1, 30, x + 3, 34), fill=c("ink"))
        d.rectangle((x, 31, x + 2, 33), fill=c("amber" if lit else "wood_dark"))
        if lit:
            d.point((x, 31), fill=c("glow"))
    return im


def world_textures():
    """16px wood housing and a controller face with a CRT, knobs and a brass rim."""
    folder = TEXTURES / "blocks/machines/brainrotter"
    folder.mkdir(parents=True, exist_ok=True)
    casing = Image.new("RGBA", (16, 16), "#825438")
    d = ImageDraw.Draw(casing)
    d.rectangle((0, 0, 15, 15), outline="#3d281f")
    d.line([(1, 14), (1, 1), (14, 1)], fill="#b48657")
    d.line([(14, 2), (14, 14), (2, 14)], fill="#553521")
    for y in [4, 8, 12]:
        d.line([(2, y), (13, y)], fill="#69422c")
        d.line([(3, y + 1), (10, y + 1)], fill="#956540")
    for x, y in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        d.point((x, y), fill="#c5a46c")
    casing.save(folder / "casing.png")
    faces = []
    for active in [False, True]:
        face = Image.new("RGBA", (16, 16))
        glow = Image.new("RGBA", (16, 16))
        f, g = ImageDraw.Draw(face), ImageDraw.Draw(glow)
        f.rectangle((2, 2, 13, 13), fill="#503629", outline="#bd955c")
        f.rectangle((3, 3, 12, 8), fill="#191917")
        f.line([(4, 7), (4, 4), (11, 4)], fill="#353629")
        waveform = [(5, 6), (6, 6), (7, 5), (8, 7), (9, 5), (10, 6)]
        f.line(waveform, fill="#e9b85b" if active else "#60523c")
        if active:
            g.line(waveform, fill="#ffd780")
        f.rectangle((4, 10, 6, 12), fill="#211c18")
        f.point((5, 10), fill="#e0c18a")
        f.point((6, 11), fill="#ad8657")
        for x in [8, 10, 12]:
            f.point((x, 10), fill="#f3b640" if active else "#66432b")
            if active:
                g.point((x, 10), fill="#ffc65e")
        f.line([(8, 12), (12, 12)], fill="#251e17")
        name = "front_active" if active else "front"
        face.save(folder / f"{name}.png")
        glow.save(folder / f"{name}_glow.png")
        view = casing.copy()
        view.alpha_composite(face)
        faces.append(view)
    preview = Image.new("RGBA", (480, 185), "#24211f")
    for i, (tile, label) in enumerate(zip([casing, *faces], ["Wood casing", "Idle controller", "Active controller"])):
        preview.paste(tile.resize((160, 160), Image.Resampling.NEAREST), (i * 160, 0))
        ImageDraw.Draw(preview).text((i * 160 + 8, 168), label, fill="#e8d5a7")
    preview.save(ROOT / "build/brainrotter-controller-preview.png")
    print("Saved wood casing, controller faces and glow layers")


def caption(image, point, text, color, font_atlas):
    if font_atlas is None:
        ImageDraw.Draw(image).text(point, text, font=ImageFont.load_default(), fill=color)
        return
    x, y = point
    for char in text:
        index = ord(char)
        glyph = font_atlas.crop((index % 16 * 8, index // 16 * 8,
                                index % 16 * 8 + 8, index // 16 * 8 + 8))
        mask = glyph.getchannel("A")
        ink = Image.new("RGBA", (8, 8), color)
        ink.putalpha(mask)
        image.alpha_composite(ink, (x, y))
        bounds = mask.getbbox()
        x += bounds[2] + 1 if bounds else 4


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--font-atlas", type=Path, help="Optional Minecraft 128x128 ASCII font for previews")
    parser.add_argument("--world-only", action="store_true", help="Export only casing/controller textures")
    args = parser.parse_args()
    world_textures()
    if args.world_only:
        return
    font_atlas = Image.open(args.font_atlas).convert("RGBA") if args.font_atlas else None
    if font_atlas is not None and font_atlas.size != (128, 128):
        raise ValueError("Preview font atlas must be 128x128")
    (TEXTURES / "gui").mkdir(parents=True, exist_ok=True)
    record = compilation()
    record.resize((32, 32), Image.Resampling.NEAREST).save(TEXTURES / "items/brainrot_compilation.png")
    panel = background()
    panel.save(TEXTURES / "gui/brainrotter_nei.png")
    atlas = Image.new("RGBA", (224, 80))
    frames = []
    for frame in range(8):
        sprite = player(frame)
        atlas.paste(sprite, ((frame % 4) * 56, (frame // 4) * 40))
        page = panel.copy()
        page.alpha_composite(sprite, (60, 31))
        page.alpha_composite(record, (RECORD[0] + 1, RECORD[1] + 1))
        # Layout-preview placeholders; actual ingredients and quantities are rendered by NEI.
        d = ImageDraw.Draw(page)
        for x, y in [SOURCE, OUTPUT]:
            d.polygon([(x + 3, y + 6), (x + 11, y + 3), (x + 14, y + 7),
                       (x + 14, y + 11), (x + 6, y + 14), (x + 3, y + 10)], fill="#6464a0")
            d.line([(x + 3, y + 6), (x + 11, y + 3), (x + 14, y + 7)], fill="#b0b0d9")
        x, y = DIRECTION
        d.polygon([(x + 3, y + 10), (x + 6, y + 5), (x + 11, y + 4),
                   (x + 14, y + 11), (x + 9, y + 14)], fill=PALETTE["label"])
        d.line([(x + 5, y + 9), (x + 10, y + 6), (x + 11, y + 9)], fill=PALETTE["wood"])
        caption(page, (104, 16), "Reusable", PALETTE["wood_dark"], font_atlas)
        caption(page, (104, 80), "Consumed", PALETTE["dark"], font_atlas)
        frames.append(page.crop((5, 11, 171, 106)).resize((664, 380), Image.Resampling.NEAREST))
    atlas.save(TEXTURES / "gui/brainrotter_player.png")
    preview = ROOT / "build/brainrotter-nei-preview.png"
    frames[1].save(preview)
    frames[0].save(ROOT / "build/brainrotter-nei-preview.gif", save_all=True,
                   append_images=frames[1:], duration=250, loop=0, disposal=2)
    print("Saved compilation, NEI panel and 8-frame gramophone atlas")
    print(f"Asset layout preview (not an in-game screenshot): {preview}")


if __name__ == "__main__":
    main()
