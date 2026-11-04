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


# ---------------------------------------------------------------- objetos y bloques
def ring():
    im = Image.new("RGBA", (16, 16), T)
    d = ImageDraw.Draw(im)
    d.ellipse((2, 6, 13, 15), outline=(30, 150, 60, 255), width=2)
    d.ellipse((3, 7, 12, 14), outline=(90, 240, 120, 255), width=1)
    d.rectangle((5, 1, 10, 6), fill=(30, 150, 60, 255))
    d.rectangle((6, 2, 9, 5), fill=(120, 255, 150, 255))
    im.putpixel((6, 2), (230, 255, 235, 255))
    save(im, f"{A}/textures/item/anillo_verde.png")


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
    """Textura del candelabro: metal verde oscuro, verde brillante, velas y llamas."""
    im = Image.new("RGBA", (16, 16), T)
    for y in range(16):
        for x in range(16):
            if y < 4:
                c = (30, 120, 50) if (x + y) % 3 else (50, 160, 70)            # metal oscuro
            elif y < 8:
                c = (70, 210, 100) if (x + y) % 3 else (110, 240, 140)         # metal brillante
            elif y < 12:
                c = (220, 255, 225) if x % 4 else (190, 240, 200)              # vela
            else:
                c = (120, 255, 150) if (x + y) % 2 else (200, 255, 210)        # llama verde
            im.putpixel((x, y), c + (255,))
    save(im, f"{A}/textures/block/candelabro.png")


def candelabra_model():
    """Modelo 3D del candelabro: base, columna, brazos y tres velas con llama verde."""
    M = {"dark": [0, 0, 16, 4], "light": [0, 4, 16, 8], "candle": [0, 8, 16, 12], "flame": [0, 12, 16, 16]}

    def el(fr, to, tex):
        u = M[tex]
        faces = {f: {"uv": u, "texture": "#t"} for f in ("north", "south", "east", "west", "up", "down")}
        return {"from": fr, "to": to, "faces": faces}

    E = [el([5, 0, 5], [11, 1, 11], "dark"), el([6, 1, 6], [10, 2, 10], "light"), el([7.25, 2, 7.25], [8.75, 9, 8.75], "light"),
         el([6.5, 4, 6.5], [9.5, 5, 9.5], "dark"),
         el([2.5, 8.5, 7.5], [13.5, 9.5, 8.5], "light"),                    # brazos
         el([2, 9.5, 7], [4, 10.5, 9], "dark"), el([12, 9.5, 7], [14, 10.5, 9], "dark"), el([7, 9, 7], [9, 10, 9], "dark"),
         el([2.5, 10.5, 7.5], [3.5, 13, 8.5], "candle"), el([12.5, 10.5, 7.5], [13.5, 13, 8.5], "candle"),
         el([7.5, 10, 7.5], [8.5, 13.5, 8.5], "candle"),
         el([2.6, 13, 7.6], [3.4, 14.5, 8.4], "flame"), el([12.6, 13, 7.6], [13.4, 14.5, 8.4], "flame"), el([7.6, 13.5, 7.6], [8.4, 15, 8.4], "flame")]
    jdump({"parent": "block/block", "textures": {"t": "linterna:block/candelabro", "particle": "linterna:block/candelabro"},
           "elements": E,
           "display": {"gui": {"rotation": [30, 225, 0], "translation": [0, 1, 0], "scale": [0.9, 0.9, 0.9]},
                       "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 2, 0], "scale": [0.6, 0.6, 0.6]},
                       "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.5, 0.5, 0.5]},
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
    for n in ("anillo_verde", "pico_de_energia", "espada_de_energia", "bomba_de_energia", "canon_de_energia", "guadana_de_energia"):
        parent = "item/handheld" if n in ("pico_de_energia", "espada_de_energia", "guadana_de_energia") else "item/generated"
        jdump({"parent": parent, "textures": {"layer0": "linterna:item/" + n}}, f"{A}/models/item/{n}.json")


def lang():
    es = {"item.linterna.anillo_verde": "Anillo Verde", "item.linterna.candelabro_de_poder": "Candelabro de Poder",
          "block.linterna.candelabro_de_poder": "Candelabro de Poder", "block.linterna.constructo": "Constructo de energía",
          "item.linterna.pico_de_energia": "Pico de energía", "item.linterna.espada_de_energia": "Espada de energía",
          "item.linterna.bomba_de_energia": "Bomba de energía", "item.linterna.canon_de_energia": "Cañón de energía",
          "item.linterna.guadana_de_energia": "Guadaña de energía", "entity.linterna.rayo_verde": "Rayo verde",
          "key.categories.linterna": "Linterna (anillo verde)", "key.linterna.fly": "Volar", "key.linterna.armor": "Armadura de energía",
          "key.linterna.pick": "Pico de energía", "key.linterna.ghost": "Intangible", "key.linterna.guns": "Armas de energía",
          "key.linterna.hammer": "Martillo", "key.linterna.suit": "Traje", "key.linterna.catalog": "Catálogo del anillo"}
    en = {"item.linterna.anillo_verde": "Green Ring", "item.linterna.candelabro_de_poder": "Power Candelabra",
          "block.linterna.candelabro_de_poder": "Power Candelabra", "block.linterna.constructo": "Energy Construct",
          "item.linterna.pico_de_energia": "Energy Pickaxe", "item.linterna.espada_de_energia": "Energy Sword",
          "item.linterna.bomba_de_energia": "Energy Bomb", "item.linterna.canon_de_energia": "Energy Cannon",
          "item.linterna.guadana_de_energia": "Energy Scythe", "entity.linterna.rayo_verde": "Green Bolt",
          "key.categories.linterna": "Linterna (green ring)", "key.linterna.fly": "Fly", "key.linterna.armor": "Energy armor",
          "key.linterna.pick": "Energy pickaxe", "key.linterna.ghost": "Intangible", "key.linterna.guns": "Energy guns",
          "key.linterna.hammer": "Hammer", "key.linterna.suit": "Suit", "key.linterna.catalog": "Ring catalog"}
    for code in ("es_es", "es_mx", "es_ec", "es_ar", "es_cl", "es_co", "es_pe", "es_ve", "es_uy"):
        jdump(es, f"{A}/lang/{code}.json")
    jdump(en, f"{A}/lang/en_us.json")


if __name__ == "__main__":
    energy(); rayo(); suit(); armor(); cannon_tex(); scythe_tex(); ring(); pick(); sword(); bomb(); construct(); candelabra_tex(); candelabra_model(); items(); lang()
    print("linterna ok")
