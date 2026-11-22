"""Totally Waifu? 7.0 — Inari, la Diosa Zorro Blanca: chica (con cola esponjosa y orejas nuevas), zorro dios gigante,
caras con muchas expresiones y el círculo dorado de su divinidad.

Uso: python3 tools/gen_inari.py [preview DIR]
"""
import math, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_tanny as G
import gen_blocky as B
import gen_mobgirls as M
from gen_blocky import Part, Box, flat, hair_p, frill, metal, gem, stock, boot, sole, bow, chain
from gen_mobgirls import hair3, cloth, lt, dk
from PIL import Image, ImageDraw, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX = os.path.join(ROOT, "src/main/resources/assets/totallywaifu/textures/entity/diosa")
JAVA = os.path.join(ROOT, "src/main/java/com/totallywaifu/client/InariModels.java")
T = (0, 0, 0, 0)

WH, WHS, WHD = (250, 250, 255), (226, 226, 238), (196, 196, 214)
GOLD, GOLD_L, GOLD_D = (238, 190, 70), (255, 232, 150), (176, 126, 30)
CRIM = (206, 46, 64)
SKY = (176, 196, 246)
INNER = (255, 214, 214)


# ================================================================== la chica
def kimono(f, x, y, w, h):
    # cuello cruzado con ribete dorado
    if f == "front":
        d = abs(x - w / 2) - (h - y) * 0.45
        if -1.2 < d < 0.8 and y > 1:
            return GOLD
    if f in ("front", "back") and (x + y * 2) % 37 == 0:
        return GOLD_L                       # bordado de pétalos
    return WH


def hakama(f, x, y, w, h):
    if y > h - 4:
        return GOLD
    if (x * 3 + y * 5) % 47 == 0:
        return GOLD_L
    return WH if (x // 6) % 2 == 0 else WHS


def obi(f, x, y, w, h):
    if y in (2, h - 3):
        return GOLD
    return SKY if (x + y) % 11 else lt(SKY)


def ear_fox(f, x, y, w, h):
    if y < 5:
        return GOLD                          # puntas doradas
    if f == "front" and 2 < x < w - 3 and y > 6:
        return INNER if (x + y) % 5 else (255, 240, 236)
    return WH


def ear_fox2(c, inner):
    def fn(f, x, y, w, h):
        if f == "front" and 2 < x < w - 3:
            return inner
        return c
    return fn


def fur(f, x, y, w, h):
    if f == "bottom":
        return WHS
    if (x * 5 + y * 3) % 23 == 0:
        return WHS
    return WH


def tail_fur(i, n):
    def fn(f, x, y, w, h):
        if i == n - 1 and y > h * 0.35:
            return GOLD_L if (x + y) % 6 else GOLD
        if (x * 3 + y * 7) % 13 == 0:
            return WHS
        return WH
    return fn


def inari_girl():
    B.SHAPE = B.SHAPES["slim"]
    try:
        P = B.figure(top=kimono, waist=obi, hips=kimono, sleeve=cloth(WH), upper=cloth(WH), fore=B.skin_p, hand=B.skin_p,
                     thigh=B.skin_p, calf=stock(WH, WHS, GOLD_L))
        P += B.shoe_parts(boot(WH, WHS, WHD, accent=GOLD), sole(GOLD_D, GOLD), 11.8, None)
        P += B.skirt(hakama, flat(GOLD), 6.8, 6.6, 0.26)
    finally:
        B.SHAPE = B.SHAPES["std"]
    P += M.hair_style("hime", hair_p(*hair3((244, 244, 252))), None)
    # mechones largos que caen por la espalda
    P += chain("lock", "head", (0, -1.0, 3.6), (0.15, 0, 0), [(4.0, 6.8, 1.2, 0, 0), (4.0, 6.2, 1.2, 0.05, 0), (3.4, 5.4, 1.0, 0.05, 0)], hair_p(*hair3((244, 244, 252))))
    # orejas de zorro nuevas: altas, abiertas, con mechón interior y puntas doradas
    # orejas de zorro triangulares (base ancha, punta dorada) con mechón interior color crema
    CREAM = (255, 240, 222)
    P.append(Part("iear_r", "head", (-2.6, -8.0, -0.2), (-0.1, 0.25, -0.48), [Box(-2.1, -2.0, -0.8, 4.2, 2.2, 1.6, ear_fox2(WH, CREAM)),
                                                                               Box(-1.6, -3.8, -0.7, 3.2, 1.9, 1.4, ear_fox2(WH, CREAM)),
                                                                               Box(-1.0, -5.2, -0.6, 2.0, 1.5, 1.2, flat(WH)),
                                                                               Box(-0.55, -6.2, -0.5, 1.1, 1.1, 1.0, flat(GOLD)),
                                                                               Box(-1.2, -2.6, -1.0, 2.4, 2.4, 0.4, flat(CREAM))]))
    # mangas furisode (anchas, con borde dorado)
    P.append(Part("furi_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.6, 2.4, -2.0, 3.8, 5.6, 4.0, lambda f, x, y, w, h: GOLD if y > h - 3 else WH),
                                                                Box(-2.4, 8.0, -1.2, 3.4, 2.4, 3.0, lambda f, x, y, w, h: GOLD if y > h - 3 else WHS)]))
    # cola enorme y esponjosa (6 segmentos con mechones extra)
    segs = [(3.0, 3.0, 3.0, 0, 0), (3.6, 4.6, 4.6, -0.25, 0), (3.6, 5.6, 5.6, -0.3, 0), (3.4, 5.6, 5.6, -0.3, 0), (3.0, 4.6, 4.6, -0.25, 0), (2.6, 3.0, 3.0, -0.2, 0)]
    n = len(segs)
    for i, (l, w, d, rx, rz) in enumerate(segs):
        name = "tail%d" % i
        par = "body" if i == 0 else "tail%d" % (i - 1)
        piv = (0, 9.0, 2.0) if i == 0 else (0, segs[i - 1][0] - 0.3, 0)
        rot = (0.95, 0, 0) if i == 0 else (rx, 0, rz)
        boxes = [Box(-w / 2, 0, -d / 2, w, l, d, tail_fur(i, n))]
        if 0 < i < n - 1:                         # mechones esponjosos a los lados
            boxes.append(Box(-w / 2 - 0.5, l * 0.25, -d / 2 + 0.6, 0.6, l * 0.55, d - 1.2, tail_fur(i, n)))
            boxes.append(Box(w / 2 - 0.1, l * 0.25, -d / 2 + 0.6, 0.6, l * 0.55, d - 1.2, tail_fur(i, n)))
            boxes.append(Box(-w / 2 + 0.6, l * 0.2, d / 2 - 0.1, w - 1.2, l * 0.6, 0.6, tail_fur(i, n)))
        P.append(Part(name, par, piv, rot, boxes))
    # adornos: horquillas doradas con borla, collar de cuerda sagrada y lazo celeste
    P.append(Part("kanzashi_r", "head", (-3.6, -7.4, 1.0), (0.2, 0, -0.5), [Box(-0.3, -0.3, -0.3, 0.6, 3.0, 0.6, metal(GOLD, GOLD_L)),
                                                                            Box(-0.9, -1.2, -0.9, 1.8, 1.0, 1.8, gem(CRIM, (255, 170, 180))),
                                                                            Box(-0.25, 2.6, -0.25, 0.5, 2.2, 0.5, flat(CRIM))]))
    P.append(Part("rope", "body", (0, -0.4, 0), (0, 0, 0), [Box(-2.0, -0.6, -1.6, 4.0, 0.8, 3.2, lambda f, x, y, w, h: GOLD if (x + y) % 4 < 2 else WH)]))
    P.append(Part("rope_bell", "body", (0, 0.4, -1.9), (0, 0, 0), [Box(-0.6, 0, -0.5, 1.2, 1.2, 1.0, metal(GOLD, GOLD_L))]))
    P += bow("ribbon", "body", (0, 5.2, 1.9), (0, math.pi, 0), flat(SKY), 1.3)
    return B.mirror_all(P)


# ------------------------------------------------------------------ caras (16x16 → 64 px), muchas expresiones
EYES = {   # 6 columnas (0 = lado de afuera), 5 filas
    "normal":    ["RLLLLL", ".hGGGW", ".GKKgW", ".gKKOW", "..OOO."],
    "blink":     ["......", "......", "......", "RLLLLL", "......"],
    "happy":     ["......", "......", "..LLL.", ".L...L", "R....."],
    "smile":     ["......", "RLLLLL", ".hGGGW", ".gKKOW", "..OO.."],
    "surprised": [".LLLL.", "RhGGGW", "WGKKgW", "WgKKOW", ".OOOO."],
    "focused":   ["......", "RRLLLL", ".hYYYW", ".YKKYW", "..YY.."],
    "relaxed":   ["......", "......", "RLLLLL", ".gKKOW", "..OO.."],
    "love":      ["RLLLLL", ".HHGHH", ".HHHHH", "..HHH.", "...H.."],
    "playful":   ["RLLLLL", ".hGGGW", ".GKKgW", ".gKKOW", "..OOO."],
    "shy":       ["RLLLLL", ".WhGGG", ".WGKKg", ".WgKKO", "..OOO."],
    "sleep":     ["......", "......", "R....L", ".LLLL.", "......"],
    "wink":      ["RLLLLL", ".hGGGW", ".GKKgW", ".gKKOW", "..OOO."],
}
MOUTH = {"normal": ["....", ".mm."], "blink": ["....", ".mm."], "happy": ["M..M", ".MM."], "smile": ["....", "M..M"],
         "surprised": [".oo.", ".oo."], "focused": ["....", ".MM."], "relaxed": ["....", ".mm."], "love": ["M..M", ".MM."],
         "playful": ["M..M", ".tt."], "shy": ["....", "..m."], "sleep": ["....", ".mm."], "wink": ["M..M", ".MM."]}
FACES = ["normal", "blink", "happy", "smile", "surprised", "focused", "relaxed", "love", "playful", "shy", "sleep", "wink"]


def face_grid(expr):
    S, SH = B.SKIN, B.SKIN_SH
    N = 16
    g = [[S] * N for _ in range(N)]
    for y in range(N):
        for x in range(N):
            c = S
            if y < 5:
                c = G.mix(SH, S, y / 5)
            if min(x, N - 1 - x) < 1:
                c = G.mix(c, SH, 0.5)
            if y >= 15:
                c = G.mix(S, SH, 0.6)
            g[y][x] = c
    col = {"K": (90, 40, 10), "L": (60, 40, 50), "R": CRIM, "G": (214, 150, 30), "g": (246, 196, 60), "O": (255, 236, 150),
           "Y": (255, 220, 80), "h": (255, 255, 255), "H": (255, 110, 150), "W": (252, 250, 252),
           "M": (200, 96, 116), "m": (226, 156, 166), "o": (130, 46, 66), "t": (240, 110, 130)}
    left = EYES["normal" if expr == "wink" else expr]
    right = EYES["happy" if expr == "wink" else expr]
    for rows, side in ((left, 0), (right, 1)):
        for j, row in enumerate(rows):
            for i, ch in enumerate(row):
                if ch == ".":
                    continue
                x = 1 + i if side == 0 else N - 2 - i
                g[7 + j][x] = col[ch]
    # sombra roja de zorro en el rabillo del ojo (marca de kitsune divina)
    if expr not in ("sleep",):
        for x, y in ((1, 9), (14, 9)):
            g[y][x] = G.mix(g[y][x], CRIM, 0.7)
    # cejas blancas finas
    brow = (210, 206, 220)
    if expr == "focused":
        for x in (3, 4, 5):
            g[6][x] = brow; g[6][N - 1 - x] = brow
    elif expr != "sleep":
        for x in (2, 3, 4):
            g[5][x] = brow; g[5][N - 1 - x] = brow
    # marca dorada en la frente (media luna)
    for x, y in ((7, 2), (8, 2), (6, 3), (9, 3)):
        g[y][x] = GOLD
    blush = {"love": 0.85, "shy": 0.9, "happy": 0.5, "smile": 0.4, "playful": 0.5, "wink": 0.5, "sleep": 0.4, "relaxed": 0.35}.get(expr, 0.25)
    for x in (2, 3, 12, 13):
        g[12][x] = G.mix(g[12][x], (246, 150, 170), blush)
    g[11][7] = G.mix(S, SH, 0.7)
    for j, row in enumerate(MOUTH[expr]):
        for i, ch in enumerate(row):
            if ch != ".":
                g[12 + j][6 + i] = col[ch]
    if expr == "playful":
        g[14][8] = (240, 110, 130)               # sacando la lengüita
    return g


def face_tex(expr):
    g = face_grid(expr)
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            img.putpixel((x, y), g[y][x] + (255,))
    tex = Image.new("RGBA", (256, 128), T)
    S = 8

    def fill(u, v, w, h, c):
        tex.paste(Image.new("RGBA", (w * S, h * S), c + (255,)), (u * S, v * S))
    hair = (244, 244, 252)
    fill(8, 0, 8, 8, hair)
    fill(16, 0, 8, 8, B.SKIN_SH)
    fill(0, 8, 8, 8, B.SKIN_SH)
    fill(16, 8, 8, 8, B.SKIN_SH)
    fill(24, 8, 8, 8, hair)
    tex.paste(img.resize((64, 64), Image.NEAREST), (64, 64))
    return tex


# ================================================================== el zorro dios gigante
def fox_body(f, x, y, w, h):
    if f == "top":                            # el círculo de su divinidad marcado en el pelaje
        cx, cy = w / 2, h * 0.42
        d = math.hypot((x - cx) / (w * 0.46), (y - cy) / (w * 0.46))
        if 0.80 < d < 0.98:
            return GOLD
        if 0.55 < d < 0.62:
            return GOLD_L
        for k in range(8):                    # rayitos
            a = k * math.pi / 4
            px, py = cx + math.cos(a) * w * 0.31, cy + math.sin(a) * w * 0.31
            if math.hypot(x - px, y - py) < 1.6:
                return GOLD
    if f in ("left", "right") and y > h * 0.55 and (x // 10) % 3 == 1 and y < h * 0.62 + (x % 10) * 0.4:
        return GOLD_L                         # trazos dorados en los costados
    return fur(f, x, y, w, h)


def fox_leg(f, x, y, w, h):
    if y > h - 3:
        return WHS
    if f == "front" and abs(x - w / 2) < 2 and h * 0.3 < y < h * 0.45:
        return CRIM                           # marquitas rojas de kitsune
    return fur(f, x, y, w, h)


def fox_head(f, x, y, w, h):
    if f == "front":
        return WH
    if f == "top" and abs(x - w / 2) < 2.5 and y > h * 0.55:
        return GOLD                           # la marca dorada de la frente
    return fur(f, x, y, w, h)


def fox_ear(f, x, y, w, h):
    if y < 8:
        return GOLD
    if f == "front" and 2 < x < w - 3 and y > 10:
        return INNER
    return WH


def inari_fox():
    def ear3(f, x, y, w, h):
        if f == "front" and 2 < x < w - 3:
            return INNER
        return WH
    P = [Part("torso", "fox", (0, 0, 0), (0, 0, 0), [Box(-4.0, -4.0, -9.0, 8.0, 7.6, 18.0, fox_body)]),
         Part("ruff", "torso", (0, 0.4, -8.6), (0.15, 0, 0), [Box(-4.8, -4.4, -2.6, 9.6, 9.4, 4.4, fur)]),
         Part("neck", "torso", (0, -1.6, -8.0), (0.85, 0, 0), [Box(-2.8, -5.6, -2.8, 5.6, 6.0, 5.6, fur)]),
         Part("head", "fox", (0, -6.4, -12.6), (0, 0, 0), [Box(-4.2, -4.0, -5.0, 8.4, 7.0, 7.0, fox_head)]),
         Part("snout", "head", (0, 1.2, -5.0), (0, 0, 0), [Box(-2.0, -1.4, -4.4, 4.0, 3.0, 4.4, fur), Box(-0.8, -1.6, -4.8, 1.6, 1.0, 0.6, flat((40, 30, 36)))]),
         Part("jaw", "snout", (0, 1.6, -0.2), (0, 0, 0), [Box(-1.7, 0, -3.8, 3.4, 1.0, 3.8, flat(WHS))]),
         Part("cheek_r", "head", (-4.0, 1.0, -2.0), (0, 0.3, 0.2), [Box(-1.6, -1.6, -1.6, 1.8, 3.4, 3.4, fur)]),
         Part("ear_r", "head", (-2.6, -3.8, -0.6), (-0.05, 0.2, -0.3), [Box(-2.2, -2.6, -1.0, 4.4, 2.8, 2.0, ear3), Box(-1.6, -4.8, -0.8, 3.2, 2.4, 1.6, ear3),
                                                                     Box(-1.0, -6.4, -0.6, 2.0, 1.8, 1.2, flat(WH)), Box(-0.5, -7.4, -0.5, 1.0, 1.2, 1.0, flat(GOLD))]),
         Part("fleg_r", "fox", (-2.6, 2.0, -6.4), (0, 0, 0), [Box(-1.5, 0, -1.5, 3.0, 7.2, 3.0, fox_leg), Box(-1.3, 7.0, -1.6, 2.6, 5.0, 2.8, fox_leg)]),
         Part("bleg_r", "fox", (-2.8, 2.0, 6.4), (0, 0, 0), [Box(-2.0, -1.4, -2.6, 4.0, 7.6, 5.0, fur), Box(-1.3, 6.0, -0.8, 2.6, 6.0, 2.6, fox_leg)])]
    segs = [(4.4, 4.2, 4.2), (5.0, 6.6, 6.6), (5.4, 7.6, 7.6), (5.0, 6.8, 6.8), (3.8, 4.4, 4.4)]
    n = len(segs)
    for i, (l, w, d) in enumerate(segs):
        name = "tail%d" % i
        par = "torso" if i == 0 else "tail%d" % (i - 1)
        piv = (0, -2.4, 8.6) if i == 0 else (0, segs[i - 1][0] - 0.5, 0)
        rot = (1.15, 0, 0) if i == 0 else (-0.28, 0, 0)
        boxes = [Box(-w / 2, 0, -d / 2, w, l, d, tail_fur(i, n))]
        if 0 < i < n - 1:
            boxes.append(Box(-w / 2 - 0.7, l * 0.2, -d / 2 + 0.8, 0.8, l * 0.6, d - 1.6, tail_fur(i, n)))
            boxes.append(Box(w / 2 - 0.1, l * 0.2, -d / 2 + 0.8, 0.8, l * 0.6, d - 1.6, tail_fur(i, n)))
        P.append(Part(name, par, piv, rot, boxes))
    m = G.mirror([p for p in P if p.name.endswith("_r") and p.parent in ("head", "fox")])
    return P + m


FOX_FACES = ["normal", "blink", "happy", "focused"]


def fox_face(expr):
    """Ojos del zorro (capa en la frente de la cabeza): 8x7 unidades a 8 px."""
    img = Image.new("RGBA", (256, 128), T)
    d = ImageDraw.Draw(img)
    O, OD = (255, 200, 60), (210, 130, 20)
    if expr == "focused":
        O, OD = (255, 240, 140), (255, 190, 40)
    for cx in (14, 50):
        if expr in ("normal", "focused"):
            d.polygon([(cx - 9, 26), (cx + 8, 22), (cx + 9, 32), (cx - 6, 33)], fill=O)
            d.polygon([(cx - 6, 30), (cx + 9, 28), (cx + 9, 32), (cx - 6, 33)], fill=OD)
            d.rectangle([cx - 1, 23, cx + 1, 33], fill=(60, 30, 10))
            d.rectangle([cx + 3, 24, cx + 5, 26], fill=(255, 255, 255))
            d.line([(cx - 11, 26), (cx + 9, 20)], fill=CRIM, width=2)      # delineado rojo
        elif expr == "happy":
            d.line([(cx - 8, 30), (cx, 24), (cx + 8, 30)], fill=(90, 60, 60), width=2)
        else:
            d.line([(cx - 9, 30), (cx + 9, 29)], fill=(90, 60, 60), width=2)
        d.rectangle([cx - 10, 36, cx - 6, 44], fill=CRIM if expr != "happy" else (240, 150, 170))  # mejilla
    return img


def halo():
    """El círculo dorado de su divinidad (translúcido, con runas)."""
    N = 256
    img = Image.new("RGBA", (N, N), T)
    px = img.load()
    c = (N - 1) / 2
    for y in range(N):
        for x in range(N):
            r = math.hypot(x - c, y - c) / c
            a = math.atan2(y - c, x - c)
            v = 0
            v = max(v, math.exp(-((r - 0.92) / 0.025) ** 2))
            v = max(v, 0.7 * math.exp(-((r - 0.78) / 0.015) ** 2))
            if 0.80 < r < 0.90 and (math.sin(a * 24) > 0.65):
                v = max(v, 0.6)                    # runas
            for k in range(12):
                aa = k * math.pi / 6
                if abs(math.sin(a - aa)) < 0.02 and 0.35 < r < 0.78:
                    v = max(v, 0.45 * (1 - abs(r - 0.56) / 0.22))
            v = max(v, 0.5 * math.exp(-((r - 0.34) / 0.02) ** 2))
            if v > 0.01:
                px[x, y] = (255, int(214 + 30 * v), int(110 + 90 * v), int(min(1, v) * 235))
    glow = img.filter(ImageFilter.GaussianBlur(3))
    return Image.alpha_composite(glow, img)


# ================================================================== Java
def main():
    os.makedirs(TEX, exist_ok=True)
    G.S = 8
    G.AO = True
    G.jit = lambda c, a=4: c
    B.SKIN, B.SKIN_SH = (254, 238, 232), (238, 214, 210)
    girl = inari_girl()
    w, h = G.pack(girl, 64)
    G.build_texture(girl, w, h).save(os.path.join(TEX, "girl.png"))
    for e in FACES:
        face_tex(e).save(os.path.join(TEX, "face_%s.png" % e))
    fox = inari_fox()
    fw, fh = G.pack(fox, 64)
    G.build_texture(fox, fw, fh).save(os.path.join(TEX, "fox.png"))
    for e in FOX_FACES:
        fox_face(e).save(os.path.join(TEX, "fox_face_%s.png" % e))
    halo().save(os.path.join(TEX, "halo.png"))
    java = ["// GENERADO por tools/gen_inari.py — no editar a mano.", "package com.totallywaifu.client;", "",
            "import net.minecraft.client.model.geom.ModelPart;", "import net.minecraft.client.model.geom.PartPose;",
            "import net.minecraft.client.model.geom.builders.*;", "", "import java.util.LinkedHashMap;", "import java.util.Map;", "",
            "/** Modelos de Inari: la chica zorro diosa y el zorro dios gigante. */",
            "public final class InariModels {", "    private InariModels() {}", "",
            "    public static LayerDefinition foxFace() {",
            "        MeshDefinition mesh = new MeshDefinition();",
            "        mesh.getRoot().addOrReplaceChild(\"face\", CubeListBuilder.create().texOffs(0, 0).addBox(-4.2F, -4.0F, -5.02F, 8F, 7F, 0F), PartPose.ZERO);",
            "        return LayerDefinition.create(mesh, 32, 16);", "    }", ""]
    java += G.java_layer("girl", girl, B.ANCHORS, w, h) + [""]
    java += G.java_bake("bakeGirl", girl, B.ANCHORS) + [""]
    anchor = [("fox", (0, 10.0, 0))]
    java += G.java_layer("fox", fox, anchor, fw, fh) + [""]
    java += G.java_bake("bakeFox", fox, anchor) + ["}", ""]
    open(JAVA, "w").write("\n".join(java))
    B.SKIN, B.SKIN_SH = (246, 230, 226), (224, 202, 204)
    print("inari chica: %d piezas %dx%d · zorro: %d piezas %dx%d" % (len(girl), w * 8, h * 8, len(fox), fw * 8, fh * 8))
    return girl, fox


def preview(outdir):
    os.makedirs(outdir, exist_ok=True)
    girl, fox = main()
    G.S = 8
    G.pack(girl, 64)
    bt = Image.open(os.path.join(TEX, "girl.png")).convert("RGBA")
    ft = Image.open(os.path.join(TEX, "face_normal.png")).convert("RGBA")
    f512 = Image.new("RGBA", (512, 256), T)
    f512.paste(ft, (0, 0))
    quads = G.quads_for(f512, -4, -8, -4, 8, 8, 8, 0, 0, [((0, 0, 0), (0, 0, 0))], 1)
    tf = G.chains(girl, B.ANCHORS)
    for p in girl:
        for b in p.boxes:
            quads += G.quads_for(bt, b.x, b.y, b.z, b.w, b.h, b.d, b.u, b.v, tf[p.name], 1)
    G.draw(quads, os.path.join(outdir, "girl.png"), 12.5, 140, [(math.pi, 0.05), (math.pi - 0.7, 0.1), (0.0, 0.05)], (250, 470))
    G.pack(fox, 64)
    ct = Image.open(os.path.join(TEX, "fox.png")).convert("RGBA")
    anchors = [("fox", (0, 10.0, 0))]
    tf = G.chains(fox, anchors)
    quads = []
    for p in fox:
        for b in p.boxes:
            quads += G.quads_for(ct, b.x, b.y, b.z, b.w, b.h, b.d, b.u, b.v, tf[p.name], 1)
    G.draw(quads, os.path.join(outdir, "fox.png"), 9, 120, [(math.pi + 0.7, 0.25), (math.pi / 2, 0.05), (0.4, 0.2)], (420, 340))
    sheet = Image.new("RGBA", (12 * 70, 70), (40, 30, 50, 255))
    for i, e in enumerate(FACES):
        sheet.alpha_composite(Image.open(os.path.join(TEX, "face_%s.png" % e)).crop((64, 64, 128, 128)), (i * 70 + 3, 3))
    sheet.resize((sheet.width * 2, sheet.height * 2), Image.NEAREST).save(os.path.join(outdir, "faces.png"))


if __name__ == "__main__":
    if len(sys.argv) > 2 and sys.argv[1] == "preview":
        preview(sys.argv[2])
    else:
        main()
