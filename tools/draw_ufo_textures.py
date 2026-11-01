"""Hand-authored 16px pixel art, exported at 32px with nearest-neighbor scaling.

Requires Pillow. Run from any directory; assets go into the mod's item textures.
The contact sheet is a preview of the textures, not an in-game NEI screenshot.
"""

from pathlib import Path
import textwrap

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
TEXTURES = ROOT / "src/main/resources/assets/gtnhextranerf/textures/items"
PALETTE = {
    "ink": "#202a36",
    "dark": "#3b4c5d",
    "metal": "#7f96a5",
    "silver": "#c2d2d7",
    "white": "#edf8f4",
    "glass": "#236576",
    "teal": "#43cfc6",
    "glow": "#bcfff0",
    "purple": "#8659a3",
    "void": "#463451",
    "pink": "#ec80b0",
    "gold": "#e0b65f",
    "rust": "#bc714d",
}


class Sprite:
    def __init__(self):
        self.image = Image.new("RGBA", (16, 16))
        self.draw = ImageDraw.Draw(self.image)

    def rect(self, box, color):
        self.draw.rectangle(box, fill=PALETTE.get(color, color))

    def poly(self, points, color):
        self.draw.polygon(points, fill=PALETTE.get(color, color))

    def line(self, points, color, width=1):
        self.draw.line(points, fill=PALETTE.get(color, color), width=width)

    def ellipse(self, box, color):
        self.draw.ellipse(box, fill=PALETTE.get(color, color))

    def question(self, x, y, color="gold"):
        for dx, dy in [(0, 0), (1, 0), (2, 1), (1, 2), (1, 3), (1, 5)]:
            self.rect((x + dx, y + dy, x + dx, y + dy), color)


def ufo():
    s = Sprite()
    s.poly([(6, 11), (9, 11), (11, 15), (4, 15)], (67, 207, 198, 80))
    s.rect((7, 11, 8, 15), (188, 255, 240, 110))
    s.ellipse((4, 1, 11, 9), "ink")
    s.ellipse((5, 2, 10, 8), "glass")
    s.rect((6, 2, 8, 3), "teal")
    s.rect((6, 3, 7, 4), "glow")
    s.ellipse((0, 6, 15, 11), "ink")
    s.ellipse((1, 6, 14, 10), "metal")
    s.line([(3, 6), (5, 5), (10, 5), (12, 6)], "silver")
    s.rect((2, 7, 13, 8), "silver")
    s.rect((4, 7, 11, 7), "white")
    s.line([(2, 9), (4, 10), (11, 10), (13, 9)], "dark")
    for x in [3, 7, 11]:
        s.rect((x, 9, x + 1, 9), "teal")
        s.rect((x, 9, x, 9), "glow")
    return s.image


def rotor():
    s = Sprite()
    s.ellipse((1, 1, 14, 14), "ink")
    s.ellipse((2, 2, 13, 13), "teal")
    s.ellipse((3, 3, 12, 12), "ink")
    for blade in [
        [(7, 7), (5, 3), (7, 1), (9, 2), (9, 6)],
        [(8, 7), (12, 5), (14, 7), (13, 9), (9, 9)],
        [(8, 8), (10, 12), (8, 14), (6, 13), (6, 9)],
        [(7, 8), (3, 10), (1, 8), (2, 6), (6, 6)],
    ]:
        s.poly(blade, "metal")
    s.line([(7, 2), (7, 5)], "silver")
    s.line([(11, 7), (13, 7)], "silver")
    s.ellipse((5, 5, 10, 10), "dark")
    s.ellipse((6, 6, 9, 9), "silver")
    s.rect((8, 6, 9, 7), "pink")  # The axle is conspicuously off-center.
    return s.image


def chamber():
    s = Sprite()
    s.poly([(2, 4), (8, 1), (14, 4), (14, 12), (8, 15), (2, 12)], "ink")
    s.poly([(3, 4), (8, 2), (13, 4), (8, 7)], "silver")
    s.poly([(3, 5), (7, 7), (7, 13), (3, 11)], "metal")
    s.poly([(8, 7), (13, 5), (13, 11), (8, 14)], "purple")
    s.poly([(5, 5), (8, 4), (11, 5), (8, 6)], "void")
    s.rect((9, 8, 12, 11), "void")
    s.rect((9, 9, 12, 9), "glow")  # A minus sign inside the impossible chamber.
    s.line([(3, 4), (8, 2), (12, 4)], "white")
    return s.image


def belt():
    s = Sprite()
    s.rect((1, 4, 14, 10), "ink")
    s.rect((3, 5, 12, 7), "dark")
    for x in range(4, 12, 2):
        s.line([(x, 5), (x - 1, 7)], "metal")
    for x in [1, 11]:
        s.ellipse((x, 5, x + 3, 10), "silver")
        s.ellipse((x + 1, 6, x + 2, 9), "dark")
    s.rect((5, 9, 10, 11), (0, 0, 0, 0))
    s.line([(2, 11), (4, 11)], "rust")
    s.line([(11, 11), (13, 11)], "rust")
    s.rect((7, 10, 8, 10), "pink")
    s.rect((2, 3, 13, 3), "teal")
    return s.image


def rod():
    s = Sprite()
    s.rect((1, 6, 14, 10), "ink")
    s.rect((2, 7, 13, 9), "metal")
    for x, y1, y2 in [(2, 4, 12), (5, 5, 11), (9, 6, 10), (13, 6, 10)]:
        s.rect((x, y1, x + 1, y2), "dark")
        s.line([(x, y1), (x, y2 - 1)], "silver")
    s.rect((6, 6, 8, 6), "white")
    s.line([(7, 5), (7, 2), (10, 2), (10, 5)], "gold")
    s.rect((6, 5, 11, 9), "ink")
    s.rect((7, 5, 10, 8), "gold")
    s.rect((8, 6, 9, 7), "dark")
    s.rect((8, 8, 8, 8), "rust")
    return s.image


def photon():
    s = Sprite()
    s.poly([(3, 3), (10, 1), (14, 5), (14, 12), (6, 15), (2, 11)], "ink")
    s.poly([(4, 4), (10, 2), (13, 5), (7, 8)], "purple")
    s.poly([(3, 5), (6, 8), (6, 13), (3, 10)], "metal")
    s.poly([(7, 8), (13, 6), (13, 11), (7, 14)], "void")
    s.ellipse((7, 6, 12, 11), "gold")
    s.rect((9, 5, 10, 12), "white")
    s.rect((6, 8, 13, 9), "white")
    s.line([(7, 6), (12, 11)], "ink", 2)  # A shutter conceals the source.
    s.rect((10, 3, 11, 3), "teal")
    return s.image


def detector():
    s = Sprite()
    s.line([(7, 2), (7, 0), (10, 0)], "silver")
    s.rect((2, 3, 13, 14), "ink")
    s.rect((3, 4, 12, 13), "metal")
    s.rect((4, 5, 11, 11), "glass")
    s.rect((5, 6, 10, 10), "dark")
    s.question(6, 5, "glow")
    s.rect((4, 12, 5, 12), "teal")
    s.rect((8, 12, 8, 12), "gold")
    s.rect((10, 12, 10, 12), "pink")
    s.rect((3, 4, 4, 4), "silver")
    return s.image


def hand():
    s = Sprite()
    s.rect((5, 11, 10, 14), "ink")
    s.rect((6, 12, 9, 13), "glass")
    for x, top in [(4, 3), (7, 1), (10, 2)]:
        s.rect((x, top, x + 2, 8), "ink")
        s.rect((x + 1, top + 1, x + 1, 7), "silver")
    s.poly([(3, 7), (2, 5), (0, 6), (2, 10), (5, 12), (11, 11), (12, 7)], "ink")
    s.poly([(4, 7), (3, 6), (1, 6), (3, 9), (5, 11), (10, 10), (11, 7)], "metal")
    s.rect((5, 7, 10, 9), "silver")
    s.rect((7, 8, 8, 9), "purple")
    s.rect((6, 12, 8, 12), "teal")
    return s.image


def boundary():
    s = Sprite()
    for points in [[(2, 5), (2, 2), (6, 2)], [(9, 2), (13, 2), (13, 6)],
                   [(13, 9), (13, 13), (9, 13)], [(6, 13), (2, 13), (2, 9)]]:
        s.line(points, "ink", 3)
        s.line(points, "purple")
    s.line([(4, 4), (10, 4), (10, 10), (4, 10), (4, 7)], "teal")
    s.line([(4, 4), (7, 7), (10, 4)], "silver")
    s.line([(7, 7), (10, 10)], "silver")
    s.rect((6, 6, 8, 8), "void")
    s.rect((7, 7, 7, 7), "pink")
    return s.image


SPRITES = [
    ("ufo_assembly", "UFO Assembly", ufo),
    ("unidentified_flying_rotor", "Unidentified Flying Rotor", rotor),
    ("negative_volume_chamber", "Negative-Volume Chamber", chamber),
    ("missing_return_belt", "Missing Return Belt", belt),
    ("immovable_telescopic_rod", "Immovable Telescopic Rod", rod),
    ("unobserved_photon_source", "Unobserved Photon Source", photon),
    ("uncertainty_detector", "Uncertainty Detector", detector),
    ("unidentified_helping_hand", "Unidentified Helping Hand", hand),
    ("undefined_field_boundary", "Undefined Field Boundary", boundary),
]


def main():
    TEXTURES.mkdir(parents=True, exist_ok=True)
    sheet = Image.new("RGB", (720, 594), "#19232d")
    draw = ImageDraw.Draw(sheet)
    font_path = Path("C:/Windows/Fonts/segoeui.ttf")
    font = ImageFont.truetype(str(font_path), 15) if font_path.exists() else ImageFont.load_default()
    for index, (identifier, title, painter) in enumerate(SPRITES):
        image = painter().resize((32, 32), Image.Resampling.NEAREST)
        image.save(TEXTURES / f"{identifier}.png", optimize=True)
        x, y = (index % 3) * 240, (index // 3) * 198
        draw.rounded_rectangle((x + 8, y + 8, x + 232, y + 190), radius=8, fill="#24333e")
        for cy in range(y + 22, y + 118, 8):
            for cx in range(x + 72, x + 168, 8):
                color = "#31444f" if ((cx - x) // 8 + (cy - y) // 8) % 2 else "#293c46"
                draw.rectangle((cx, cy, cx + 7, cy + 7), fill=color)
        sheet.paste(image.resize((96, 96), Image.Resampling.NEAREST), (x + 72, y + 22),
                    image.resize((96, 96), Image.Resampling.NEAREST))
        for line_index, line in enumerate(textwrap.wrap(title, 25)):
            bbox = draw.textbbox((0, 0), line, font=font)
            draw.text((x + (240 - bbox[2]) / 2, y + 135 + line_index * 20), line,
                      font=font, fill="#e2eeee")
    preview = ROOT / "build/ufo-textures-preview.png"
    preview.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(preview)
    print(f"Saved {len(SPRITES)} transparent 32x32 textures to {TEXTURES}")
    print(f"Preview: {preview}")


if __name__ == "__main__":
    main()
