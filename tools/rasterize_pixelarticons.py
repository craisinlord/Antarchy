import re
from pathlib import Path

from PIL import Image


SOURCE_DIR = (
    Path(__file__).resolve().parents[1]
    / "common-1.21.1/src/main/resources/assets/antarchy/textures/gui/antpaint"
)
ICON_NAMES = ("undo", "redo", "pencil", "eraser", "trash")
TOKEN = re.compile(r"[MmLlHhVvZz]|-?(?:\d+(?:\.\d*)?|\.\d+)")


def polygons_from_path(data):
    tokens = TOKEN.findall(data)
    polygons = []
    points = []
    x = y = start_x = start_y = 0.0
    command = None
    i = 0

    while i < len(tokens):
        if tokens[i].isalpha():
            command = tokens[i]
            i += 1
            if command in "Zz":
                if len(points) > 2:
                    polygons.append(points)
                points = []
                x, y = start_x, start_y
                command = None
                continue

        if command is None:
            raise ValueError(f"Unexpected SVG path data near token {i}: {data}")

        relative = command.islower()
        op = command.upper()
        if op in ("M", "L"):
            px = float(tokens[i])
            py = float(tokens[i + 1])
            i += 2
            if relative:
                px += x
                py += y
            x, y = px, py
            if op == "M":
                if len(points) > 2:
                    polygons.append(points)
                points = [(x, y)]
                start_x, start_y = x, y
                command = "l" if relative else "L"
            else:
                points.append((x, y))
        elif op == "H":
            px = float(tokens[i])
            i += 1
            x = x + px if relative else px
            points.append((x, y))
        elif op == "V":
            py = float(tokens[i])
            i += 1
            y = y + py if relative else py
            points.append((x, y))
        else:
            raise ValueError(f"Unsupported SVG command {command!r}: {data}")

    if len(points) > 2:
        polygons.append(points)
    return polygons


def contains_point(polygon, px, py):
    inside = False
    previous = polygon[-1]
    for current in polygon:
        x1, y1 = previous
        x2, y2 = current
        if (y1 > py) != (y2 > py):
            crossing_x = (x2 - x1) * (py - y1) / (y2 - y1) + x1
            if px < crossing_x:
                inside = not inside
        previous = current
    return inside


def main():
    for icon_name in ICON_NAMES:
        source = (SOURCE_DIR / f"{icon_name}.svg").read_text(encoding="utf-8")
        paths = re.findall(r'<path\b[^>]*\bd="([^"]+)"', source)
        if not paths:
            raise ValueError(f"No SVG path found in {icon_name}.svg")
        polygons = [polygon for path in paths for polygon in polygons_from_path(path)]

        mask = [
            [
                any(contains_point(poly, x + 0.5, y + 0.5) for poly in polygons)
                for x in range(24)
            ]
            for y in range(24)
        ]
        image = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
        pixels = image.load()
        for y in range(24):
            for x in range(24):
                if mask[y][x]:
                    pixels[x, y] = (101, 255, 101, 255)
                    continue
                outlined = any(
                    0 <= nx < 24 and 0 <= ny < 24 and mask[ny][nx]
                    for nx, ny in (
                        (x - 1, y),
                        (x + 1, y),
                        (x, y - 1),
                        (x, y + 1),
                    )
                )
                if outlined:
                    pixels[x, y] = (3, 6, 3, 255)
        image.save(SOURCE_DIR / f"{icon_name}.png")


if __name__ == "__main__":
    main()
