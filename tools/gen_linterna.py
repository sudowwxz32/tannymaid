"""Texturas, modelos y textos del mod Linterna. Uso: python3 tools/gen_linterna.py"""
import json, math, os, random
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A = os.path.join(ROOT, "src/main/resources/assets/linterna")
D = os.path.join(ROOT, "src/main/resources/data/linterna")
R = random.Random(3)
T = (0, 0, 0, 0)
G, GD, GL = (60, 220, 90), (24, 120, 44), (170, 255, 180)


def save(img, path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)


def jdump(obj, path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    json.dump(obj, open(path, "w", encoding="utf-8"), ensure_ascii=False, indent=2)


# ---------------------------------------------------------------- texturas de entidad
def energy():
    """Textura blanca con rejilla de holograma (se tiñe de verde al dibujar)."""
    im = Image.new("RGBA", (64, 64), (255, 255, 255, 200))
    for y in range(64):
        for x in range(64):
            a = 150 + (40 if (x % 4 == 0 or y % 4 == 0) else 0) + R.randint(-10, 10)
            im.putpixel((x, y), (255, 255, 255, max(0, min(255, a))))
    save(im, f"{A}/textures/entity/energia.png")


def rayo():
    im = Image.new("RGBA", (32, 32), T)
    for y in range(32):
        for x in range(32):
            d = math.hypot(x - 15.5, y - 15.5) / 16
            if d < 1:
                a = int(255 * (1 - d) ** 1.5)
                c = GL if d < 0.35 else G
                im.putpixel((x, y), c + (a,))
    save(im, f"{A}/textures/entity/rayo.png")


def suit():
    """Traje verde y negro con guantes y botas blancos (diseño propio, sin logotipos)."""
    S = 2
    im = Image.new("RGBA", (64 * S, 64 * S), T)
    BLK, BLK2, WHT, WHT2 = (20, 22, 24), (36, 38, 42), (240, 242, 244), (205, 208, 212)
    GR, GR2 = (44, 196, 78), (30, 150, 58)

    def faces(u, v, w, h, d):
        return {"top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d), "right": (u, v + d, d, h),
                "front": (u + d, v + d, w, h), "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h)}

    def paint(u, v, w, h, d, fn):
        for f, (fx, fy, fw, fh) in faces(u, v, w, h, d).items():
            for y in range(fy * S, (fy + fh) * S):
                for x in range(fx * S, (fx + fw) * S):
                    c = fn(f, (x - fx * S) / S, (y - fy * S) / S, fw, fh)
                    if c:
                        c = tuple(max(0, min(255, k + R.randint(-4, 4))) for k in c)
                        im.putpixel((x, y), c + (255,))

    def body(f, x, y, w, h):
        if f in ("top",):
            return BLK
        if f in ("left", "right"):
            return BLK if (int(y) % 4) else BLK2
        if f == "bottom":
            return BLK
        # frente y espalda: panel verde en V, costados negros
        cx = w / 2
        edge = 1.2 + y * 0.12
        if abs(x - cx) < cx - edge:
            if f == "front" and math.hypot(x - cx, y - 3.2) < 1.6:
                return BLK if math.hypot(x - cx, y - 3.2) > 0.8 else GR2     # pequeño círculo en el pecho
            return GR if (int(x) + int(y)) % 7 else GR2
        if y >= h - 1.2:
            return BLK2                                                     # cinturón
        return BLK

    def arm(f, x, y, w, h):
        if y >= h - 4 or f == "bottom":
            return WHT if f != "bottom" and (int(y) % 3) else WHT2           # guantes blancos
        if f == "top":
            return BLK
        if f in ("front", "back") and y < 4:
            return GR
        return GR if (f in ("left", "right") and y >= 4 and x < w / 2) else BLK

    def leg(f, x, y, w, h):
        if y >= h - 5 or f == "bottom":
            return WHT if f != "bottom" and (int(y) % 4) else WHT2          # botas blancas
        if f == "top":
            return BLK
        if f == "front":
            return GR if 0.8 < x < w - 0.8 else BLK
        return BLK if f != "back" else (GR2 if 1 < x < w - 1 else BLK)

    paint(16, 16, 8, 12, 4, body)
    paint(16, 32, 8, 12, 4, body)          # chaqueta (capa exterior)
    for u, v in ((40, 16), (32, 48), (40, 32), (48, 48)):
        paint(u, v, 4, 12, 4, arm)
    for u, v in ((0, 16), (16, 48), (0, 32), (0, 48)):
        paint(u, v, 4, 12, 4, leg)
    save(im, f"{A}/textures/entity/traje.png")


def armor():
    """Armadura de energía (formato de armadura 64x32): placas translúcidas con bordes brillantes."""
    S = 2
    im = Image.new("RGBA", (64 * S, 32 * S), T)

    def faces(u, v, w, h, d):
        return {"top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d), "right": (u, v + d, d, h),
                "front": (u + d, v + d, w, h), "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h)}

    def paint(u, v, w, h, d, plates):
        for f, (fx, fy, fw, fh) in faces(u, v, w, h, d).items():
            for y in range(fy * S, (fy + fh) * S):
                for x in range(fx * S, (fx + fw) * S):
                    lx, ly = (x - fx * S) / S, (y - fy * S) / S
                    edge = lx < 0.5 or ly < 0.5 or lx > fw - 0.5 or ly > fh - 0.5
                    seam = any(abs(ly - k) < 0.25 for k in plates) if f not in ("top", "bottom") else False
                    if edge or seam:
                        im.putpixel((x, y), (235, 255, 240, 255))
                    else:
                        a = 120 + (35 if (int(lx * 2) + int(ly * 2)) % 2 == 0 else 0)
                        im.putpixel((x, y), (255, 255, 255, a))

    paint(0, 0, 8, 8, 8, [5.5])              # casco con visera
    paint(16, 16, 8, 12, 4, [3.5, 7.0, 10.0])  # pechera por placas
    paint(40, 16, 4, 12, 4, [4.0, 8.0])      # brazos y hombreras
    paint(0, 16, 4, 12, 4, [4.0, 8.0])       # grebas y botas
    save(im, f"{A}/textures/entity/armadura_energia.png")


def cannon_tex():
    im = Image.new("RGBA", (16, 16), T)
    d = ImageDraw.Draw(im)
    d.rectangle((2, 6, 9, 10), fill=(40, 180, 75, 230), outline=(170, 255, 190, 255))
    d.rectangle((9, 7, 15, 9), fill=(90, 240, 125, 230))
    d.rectangle((4, 10, 6, 14), fill=(40, 180, 75, 230), outline=(170, 255, 190, 255))
    im.putpixel((15, 8), (230, 255, 235, 255))
    save(im, f"{A}/textures/item/canon_de_energia.png")


def scythe_tex():
    im = Image.new("RGBA", (16, 16), T)
    for i in range(13):
        im.putpixel((2 + i // 2 + (i % 2), 15 - i), (60, 200, 90, 230))
    d = ImageDraw.Draw(im)
    d.arc((3, 0, 17, 12), 180, 300, fill=(170, 255, 190, 255), width=2)
    save(im, f"{A}/textures/item/guadana_de_energia.png")



# ---------------------------------------------------------------- estilo holograma verde (igual que el pico)
def holo(mask_fn, name, size=16):
    """Dibuja la silueta con mask_fn(draw) y la convierte en holograma: borde brillante, interior translúcido con líneas."""
    m = Image.new("L", (size, size), 0)
    mask_fn(ImageDraw.Draw(m))
    im = Image.new("RGBA", (size, size), T)
    on = lambda x, y: 0 <= x < size and 0 <= y < size and m.getpixel((x, y)) > 0
    for y in range(size):
        for x in range(size):
            if not on(x, y):
                continue
            edge = not (on(x - 1, y) and on(x + 1, y) and on(x, y - 1) and on(x, y + 1))
            if edge:
                im.putpixel((x, y), (190, 255, 205, 255))
            else:
                a = 150 if y % 2 == 0 else 120
                im.putpixel((x, y), (70, 230, 105, a) if (x + y) % 5 else (130, 255, 160, a + 40))
    save(im, f"{A}/textures/item/{name}.png")


def weapons():
    holo(lambda d: (d.line((2, 13, 12, 3), fill=255, width=2), d.polygon([(9, 2), (13, 2), (14, 3), (14, 6), (12, 5)], fill=255),
                    d.polygon([(3, 2), (6, 2), (5, 4), (2, 5)], fill=255), d.line((3, 3, 12, 8), fill=255, width=2)), "pico_de_energia")
    holo(lambda d: (d.line((3, 12, 13, 2), fill=255, width=3), d.line((1, 11, 5, 15), fill=255, width=2), d.line((1, 15, 3, 13), fill=255, width=2)),
         "espada_de_energia")
    holo(lambda d: (d.line((3, 14, 11, 4), fill=255, width=2), d.polygon([(8, 2), (12, 1), (15, 4), (14, 9), (10, 7)], fill=255)), "hacha_de_energia")
    holo(lambda d: (d.line((2, 15, 14, 1), fill=255, width=2), d.polygon([(11, 1), (15, 0), (15, 5), (13, 3)], fill=255),
                    d.polygon([(0, 13), (3, 11), (5, 13), (2, 15)], fill=255)), "lanza_de_energia")
    holo(lambda d: (d.line((2, 14, 9, 7), fill=255, width=2), d.rectangle((8, 2, 13, 7), fill=255),
                    d.polygon([(10, 0), (11, 0), (11, 2), (10, 2)], fill=255), d.polygon([(14, 4), (15, 4), (15, 5), (14, 5)], fill=255),
                    d.polygon([(6, 4), (7, 4), (7, 5), (6, 5)], fill=255), d.polygon([(10, 8), (11, 8), (11, 9), (10, 9)], fill=255)), "mazo_de_energia")
    holo(lambda d: (d.line((3, 15, 11, 3), fill=255, width=2),
                    d.polygon([(11, 2), (8, 0), (4, 0), (1, 2), (0, 6), (2, 5), (4, 3), (7, 2), (10, 4)], fill=255)), "guadana_de_energia")
    holo(lambda d: (d.rectangle((1, 6, 10, 10), fill=255), d.rectangle((10, 7, 15, 9), fill=255), d.rectangle((3, 10, 6, 14), fill=255)), "canon_de_energia")
    holo(lambda d: (d.ellipse((2, 4, 13, 15), fill=255), d.line((9, 4, 11, 1), fill=255)), "bomba_de_energia")
    holo(lambda d: d.polygon([(2, 1), (13, 1), (13, 8), (11, 12), (7.5, 15), (4, 12), (2, 8)], fill=255), "escudo_de_energia")
    def bez(p0, p1, p2, n=12):
        return [((1 - t) ** 2 * p0[0] + 2 * (1 - t) * t * p1[0] + t * t * p2[0], (1 - t) ** 2 * p0[1] + 2 * (1 - t) * t * p1[1] + t * t * p2[1])
                for t in [i / n for i in range(n + 1)]]
    for n, pull in (("arco_de_energia", 0), ("arco_de_energia_0", 1), ("arco_de_energia_1", 2), ("arco_de_energia_2", 3)):
        def bow(d, pull=pull):
            d.line(bez((13, 1), (0, 0), (1, 14)), fill=255, width=2)            # el arco curvado
            mid = (6 + pull * 1.5, 6 + pull * 1.5)
            d.line([(13, 1), mid, (1, 14)], fill=255)                              # la cuerda
            if pull:
                d.line([mid, (mid[0] - 6, mid[1] - 6)], fill=255)                 # flecha de luz
        holo(bow, n)

# ---------------------------------------------------------------- objetos y bloques
def ring():
    im = Image.new("RGBA", (16, 16), T)
    d = ImageDraw.Draw(im)
    d.ellipse((2, 5, 13, 15), outline=(26, 120, 50, 255), width=2)
    d.ellipse((3, 6, 12, 14), outline=(110, 230, 140, 255), width=1)
    d.rounded_rectangle((4, 0, 11, 6), 2, fill=(26, 110, 46, 255), outline=(150, 255, 170, 255))
    d.ellipse((6, 1, 9, 4), fill=(200, 255, 210, 255))
    im.putpixel((7, 2), (255, 255, 255, 255))
    save(im, f"{A}/textures/item/anillo_verde.png")
    t = Image.new("RGBA", (16, 16), T)
    for y in range(16):
        for x in range(16):
            if y < 8:
                c = (28, 112, 52) if (x + y) % 4 else (70, 180, 95)
                if y in (0, 7):
                    c = (110, 220, 135)
            else:
                dd = math.hypot(x - 7.5, y - 11.5)
                c = (235, 255, 240) if dd < 1.4 else (120, 255, 150) if dd < 3 else (60, 220, 100)
            t.putpixel((x, y), c + (255,))
    save(t, f"{A}/textures/item/anillo_modelo.png")


def ring_model():
    def el(fr, to, uv, glow=False):
        e = {"from": fr, "to": to, "faces": {f: {"uv": uv, "texture": "#t"} for f in ("north", "south", "east", "west", "up", "down")}}
        if glow:
            e["forge_data"] = {"block_light": 15, "sky_light": 15}
        return e
    M, G = [0, 0, 16, 8], [4, 8, 12, 16]
    E = [el([4.5, 2, 7], [11.5, 3.5, 9], M), el([4.5, 9.5, 7], [11.5, 11, 9], M),
         el([4.5, 3.5, 7], [6, 9.5, 9], M), el([10, 3.5, 7], [11.5, 9.5, 9], M),
         el([5, 11, 6], [11, 12.5, 10], M),
         el([5.5, 12.5, 6.5], [10.5, 13, 9.5], M),
         el([6, 12.5, 7], [10, 14.5, 9], G, True),
         el([6.5, 14.5, 7.5], [9.5, 15, 8.5], G, True)]
    jdump({"parent": "block/block", "render_type": "minecraft:cutout", "gui_light": "front",
           "textures": {"t": "linterna:item/anillo_modelo", "particle": "linterna:item/anillo_verde"}, "elements": E,
           "display": {"gui": {"rotation": [20, -35, 0], "translation": [0, 0, 0], "scale": [1.05, 1.05, 1.05]},
                       "ground": {"translation": [0, 2, 0], "scale": [0.5, 0.5, 0.5]},
                       "fixed": {"rotation": [0, 0, 0], "scale": [0.9, 0.9, 0.9]},
                       "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 1.5, 1.5], "scale": [0.45, 0.45, 0.45]},
                       "thirdperson_lefthand": {"rotation": [75, 45, 0], "translation": [0, 1.5, 1.5], "scale": [0.45, 0.45, 0.45]},
                       "firstperson_righthand": {"rotation": [0, -30, 15], "translation": [1, 3, 0], "scale": [0.55, 0.55, 0.55]},
                       "firstperson_lefthand": {"rotation": [0, -30, 15], "translation": [1, 3, 0], "scale": [0.55, 0.55, 0.55]}}},
          f"{A}/models/item/anillo_verde.json")

def pick():
    im = Image.new("RGBA", (16, 16), T)
    for i in range(10):
        x, y = 3 + i, 13 - i
        for k in (0, 1):
            if 0 <= x + k < 16:
                im.putpixel((x + k, y), (90, 255, 130, 200))
    head = [(3, 3), (4, 2), (5, 2), (6, 2), (7, 2), (8, 3), (9, 4), (10, 5), (11, 6), (12, 7), (13, 8), (12, 3), (13, 4), (14, 6), (14, 7)]
    for x, y in [(4, 3), (5, 3), (6, 3), (7, 3), (8, 4), (9, 5), (10, 6), (11, 7), (12, 8), (13, 9), (3, 4), (2, 4)] + head:
        im.putpixel((x, y), (150, 255, 170, 230))
    save(im, f"{A}/textures/item/pico_de_energia.png")


def sword():
    im = Image.new("RGBA", (16, 16), T)
    for i in range(11):
        im.putpixel((4 + i, 11 - i), (170, 255, 190, 230))
        if 5 + i < 16:
            im.putpixel((5 + i, 11 - i), (80, 240, 120, 200))
    for x, y in ((2, 11), (3, 12), (4, 13), (5, 12), (6, 11), (3, 10), (2, 13), (1, 14)):
        im.putpixel((x, y), (40, 170, 70, 255))
    save(im, f"{A}/textures/item/espada_de_energia.png")


def bomb():
    im = Image.new("RGBA", (16, 16), T)
    d = ImageDraw.Draw(im)
    d.ellipse((2, 4, 13, 15), fill=(40, 190, 80, 220), outline=(150, 255, 170, 255))
    d.ellipse((4, 6, 7, 9), fill=(200, 255, 210, 255))
    d.line((9, 4, 11, 1), fill=(120, 255, 150, 255))
    save(im, f"{A}/textures/item/bomba_de_energia.png")


def construct():
    im = Image.new("RGBA", (16, 16), T)
    for y in range(16):
        for x in range(16):
            edge = x in (0, 15) or y in (0, 15)
            a = 210 if edge else 90 + (20 if (x + y) % 4 == 0 else 0)
            c = GL if edge else G
            im.putpixel((x, y), c + (a,))
    save(im, f"{A}/textures/block/constructo.png")


def candelabra_tex():
    """Lámpara de aceite antigua verde: bronce verde oscuro, metal brillante, cristal y llama verde."""
    im = Image.new("RGBA", (16, 16), T)
    for y in range(16):
        for x in range(16):
            if y < 4:
                c = (24, 92, 44, 255) if (x * 3 + y) % 5 else (48, 140, 66, 255)
            elif y < 8:
                c = (64, 190, 92, 255) if (x + y) % 4 else (150, 245, 170, 255)
                if y == 4:
                    c = (190, 255, 200, 255)
            elif y < 12:
                hl = x in (2, 3) or (x == 12 and y % 2 == 0)
                c = (210, 255, 220, 150) if hl else (90, 255, 130, 95)
                if x in (0, 15):
                    c = (40, 150, 70, 220)
            else:
                dd = math.hypot(x - 7.5, (y - 12) * 1.6)
                c = (245, 255, 245, 255) if dd < 2 else (150, 255, 170, 255) if dd < 4.5 else (60, 230, 100, 255)
            im.putpixel((x, y), c)
    save(im, f"{A}/textures/block/lampara_de_poder.png")


def candelabra_model():
    """Lámpara de aceite antigua: base, depósito, chimenea de cristal con llama verde, jaula, tapa y asa."""
    M = {"dark": [0, 0, 16, 4], "light": [0, 4, 16, 8], "glass": [0, 8, 16, 12], "flame": [4, 12, 12, 16]}

    def el(fr, to, tex, glow=False, faces=("north", "south", "east", "west", "up", "down")):
        e = {"from": fr, "to": to, "faces": {f: {"uv": M[tex], "texture": "#t"} for f in faces}}
        if glow:
            e["forge_data"] = {"block_light": 15, "sky_light": 15}
        return e

    E = [el([4, 0, 4], [12, 1, 12], "dark"), el([4.5, 1, 4.5], [11.5, 3.5, 11.5], "light"), el([5, 3.5, 5], [11, 4.5, 11], "dark"),
         el([7.5, 4.5, 7.5], [8.5, 5.5, 8.5], "dark"),
         el([7, 5.5, 7], [9, 9, 9], "flame", True), el([7.5, 9, 7.5], [8.5, 10.5, 8.5], "flame", True),
         el([5.5, 4.5, 5.5], [10.5, 11.5, 10.5], "glass", True),
         el([4.75, 4, 4.75], [5.5, 12, 5.5], "dark"), el([10.5, 4, 4.75], [11.25, 12, 5.5], "dark"),
         el([4.75, 4, 10.5], [5.5, 12, 11.25], "dark"), el([10.5, 4, 10.5], [11.25, 12, 11.25], "dark"),
         el([4.75, 7.75, 4.75], [11.25, 8.25, 11.25], "light", faces=("north", "south", "east", "west")),
         el([4.5, 11.5, 4.5], [11.5, 12.5, 11.5], "light"), el([5.5, 12.5, 5.5], [10.5, 13.5, 10.5], "dark"),
         el([7, 13.5, 7], [9, 14.5, 9], "light"),
         el([7.5, 12.5, 4], [8.5, 15, 5], "dark"), el([7.5, 12.5, 11], [8.5, 15, 12], "dark"), el([7.5, 15, 4], [8.5, 16, 12], "dark")]
    jdump({"parent": "block/block", "render_type": "minecraft:translucent", "ambientocclusion": False,
           "textures": {"t": "linterna:block/lampara_de_poder", "particle": "linterna:block/lampara_de_poder"},
           "elements": E,
           "display": {"gui": {"rotation": [25, 225, 0], "translation": [0, 0, 0], "scale": [0.95, 0.95, 0.95]},
                       "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 2, 0], "scale": [0.6, 0.6, 0.6]},
                       "firstperson_lefthand": {"rotation": [0, 45, 0], "translation": [0, 2, 0], "scale": [0.6, 0.6, 0.6]},
                       "thirdperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 1, 1], "scale": [0.55, 0.55, 0.55]},
                       "thirdperson_lefthand": {"rotation": [0, 45, 0], "translation": [0, 1, 1], "scale": [0.55, 0.55, 0.55]},
                       "ground": {"translation": [0, 3, 0], "scale": [0.5, 0.5, 0.5]}}},
          f"{A}/models/block/candelabro_de_poder.json")
    jdump({"variants": {"": {"model": "linterna:block/candelabro_de_poder"}}}, f"{A}/blockstates/candelabro_de_poder.json")
    jdump({"parent": "linterna:block/candelabro_de_poder"}, f"{A}/models/item/candelabro_de_poder.json")
    jdump({"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "linterna:candelabro_de_poder"}]}]},
          f"{D}/loot_tables/blocks/candelabro_de_poder.json")
    # constructo
    jdump({"parent": "block/cube_all", "render_type": "minecraft:translucent", "textures": {"all": "linterna:block/constructo"}},
          f"{A}/models/block/constructo.json")
    jdump({"variants": {"": {"model": "linterna:block/constructo"}}}, f"{A}/blockstates/constructo.json")
    jdump({"parent": "linterna:block/constructo"}, f"{A}/models/item/constructo.json")


def items():
    hand = ("pico_de_energia", "espada_de_energia", "guadana_de_energia", "hacha_de_energia", "lanza_de_energia", "mazo_de_energia")
    for n in hand + ("bomba_de_energia", "canon_de_energia"):
        jdump({"parent": "item/handheld" if n in hand else "item/generated", "textures": {"layer0": "linterna:item/" + n}}, f"{A}/models/item/{n}.json")
    bow_disp = {"thirdperson_righthand": {"rotation": [-80, 260, -40], "translation": [-1, -2, 2.5], "scale": [0.9, 0.9, 0.9]},
                "thirdperson_lefthand": {"rotation": [-80, -280, 40], "translation": [-1, -2, 2.5], "scale": [0.9, 0.9, 0.9]},
                "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [1.13, 3.2, 1.13], "scale": [0.68, 0.68, 0.68]},
                "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [1.13, 3.2, 1.13], "scale": [0.68, 0.68, 0.68]}}
    jdump({"parent": "item/generated", "textures": {"layer0": "linterna:item/arco_de_energia"}, "display": bow_disp,
           "overrides": [{"predicate": {"pulling": 1}, "model": "linterna:item/arco_de_energia_0"},
                         {"predicate": {"pulling": 1, "pull": 0.65}, "model": "linterna:item/arco_de_energia_1"},
                         {"predicate": {"pulling": 1, "pull": 0.9}, "model": "linterna:item/arco_de_energia_2"}]},
          f"{A}/models/item/arco_de_energia.json")
    for i in range(3):
        jdump({"parent": "item/generated", "textures": {"layer0": f"linterna:item/arco_de_energia_{i}"}, "display": bow_disp},
              f"{A}/models/item/arco_de_energia_{i}.json")
    shield = {"thirdperson_righthand": {"rotation": [0, 90, 0], "translation": [0, 1, 1.5], "scale": [0.9, 0.9, 0.9]},
              "thirdperson_lefthand": {"rotation": [0, 90, 0], "translation": [0, 1, 1.5], "scale": [0.9, 0.9, 0.9]},
              "firstperson_righthand": {"rotation": [0, 10, 0], "translation": [3, 1, -2], "scale": [0.9, 0.9, 0.9]},
              "firstperson_lefthand": {"rotation": [0, -10, 0], "translation": [3, 1, -2], "scale": [0.9, 0.9, 0.9]}}
    blocking = {"thirdperson_righthand": {"rotation": [45, 135, 0], "translation": [3.5, 1, 1], "scale": [0.9, 0.9, 0.9]},
                "thirdperson_lefthand": {"rotation": [45, 135, 0], "translation": [3.5, 1, 1], "scale": [0.9, 0.9, 0.9]},
                "firstperson_righthand": {"rotation": [0, 0, 0], "translation": [-3, 3, -1], "scale": [1.1, 1.1, 1.1]},
                "firstperson_lefthand": {"rotation": [0, 0, 0], "translation": [-3, 3, -1], "scale": [1.1, 1.1, 1.1]}}
    jdump({"parent": "item/generated", "textures": {"layer0": "linterna:item/escudo_de_energia"}, "display": shield,
           "overrides": [{"predicate": {"blocking": 1}, "model": "linterna:item/escudo_de_energia_bloqueando"}]},
          f"{A}/models/item/escudo_de_energia.json")
    jdump({"parent": "item/generated", "textures": {"layer0": "linterna:item/escudo_de_energia"}, "display": blocking},
          f"{A}/models/item/escudo_de_energia_bloqueando.json")
    ring_model()


def lang():
    es = {"item.linterna.anillo_verde": "Anillo Verde", "item.linterna.candelabro_de_poder": "Lámpara de Poder",
          "block.linterna.candelabro_de_poder": "Lámpara de Poder", "block.linterna.constructo": "Constructo de energía",
          "item.linterna.pico_de_energia": "Pico de energía", "item.linterna.espada_de_energia": "Espada de energía",
          "item.linterna.bomba_de_energia": "Bomba de energía", "item.linterna.canon_de_energia": "Cañón de energía",
          "item.linterna.guadana_de_energia": "Guadaña de energía", "item.linterna.hacha_de_energia": "Hacha de energía", "item.linterna.escudo_de_energia": "Escudo de energía", "item.linterna.arco_de_energia": "Arco de energía", "item.linterna.lanza_de_energia": "Lanza de energía", "item.linterna.mazo_de_energia": "Mazo de energía", "entity.linterna.rayo_verde": "Rayo verde",
          "key.categories.linterna": "Linterna (anillo verde)", "key.linterna.fly": "Volar", "key.linterna.armor": "Armadura de energía",
          "key.linterna.pick": "Pico de energía", "key.linterna.ghost": "Intangible", "key.linterna.guns": "Armas de energía",
          "key.linterna.hammer": "Martillo", "key.linterna.suit": "Traje", "key.linterna.catalog": "Catálogo del anillo"}
    en = {"item.linterna.anillo_verde": "Green Ring", "item.linterna.candelabro_de_poder": "Power Lamp",
          "block.linterna.candelabro_de_poder": "Power Lamp", "block.linterna.constructo": "Energy Construct",
          "item.linterna.pico_de_energia": "Energy Pickaxe", "item.linterna.espada_de_energia": "Energy Sword",
          "item.linterna.bomba_de_energia": "Energy Bomb", "item.linterna.canon_de_energia": "Energy Cannon",
          "item.linterna.guadana_de_energia": "Energy Scythe", "item.linterna.hacha_de_energia": "Energy Axe", "item.linterna.escudo_de_energia": "Energy Shield", "item.linterna.arco_de_energia": "Energy Bow", "item.linterna.lanza_de_energia": "Energy Lance", "item.linterna.mazo_de_energia": "Energy Mace", "entity.linterna.rayo_verde": "Green Bolt",
          "key.categories.linterna": "Linterna (green ring)", "key.linterna.fly": "Fly", "key.linterna.armor": "Energy armor",
          "key.linterna.pick": "Energy pickaxe", "key.linterna.ghost": "Intangible", "key.linterna.guns": "Energy guns",
          "key.linterna.hammer": "Hammer", "key.linterna.suit": "Suit", "key.linterna.catalog": "Ring catalog"}
    for code in ("es_es", "es_mx", "es_ec", "es_ar", "es_cl", "es_co", "es_pe", "es_ve", "es_uy"):
        jdump(es, f"{A}/lang/{code}.json")
    jdump(en, f"{A}/lang/en_us.json")


if __name__ == "__main__":
    energy(); rayo(); suit(); armor(); weapons(); ring(); construct(); candelabra_tex(); candelabra_model(); items(); lang()
    print("linterna ok")
