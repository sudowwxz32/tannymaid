"""Totally Waifu? 2.0 — las 4 chicas en estilo de bloques (basado en el modelo de Noa).

Cuerpo propio, alto y esbelto: cabeza 8x8x8, cuello, pecho con busto, cintura estrecha, cadera ancha, brazos finos
y piernas largas (muslo, pantorrilla y calzado). Cabello hecho de bloques (flequillo recto, coletas, cabello largo...)
y ropa de bloques según el oficio de cada una. Texturas limpias a 8 px por unidad; caras en píxel con expresiones.

Uso: python3 tools/gen_blocky.py [preview DIR]
"""
import math, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_tanny as G
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEXR = os.path.join(ROOT, "src/main/resources/assets/totallywaifu/textures/entity")
JAVA = os.path.join(ROOT, "src/main/java/com/totallywaifu/client/WaifuModels.java")
Part, Box, mix = G.Part, G.Box, G.mix
T = (0, 0, 0, 0)
ARM_X, ARM_Y, LEG_X, LEG_Y = 3.9, 1.6, 1.75, 9.0
ANCHORS = [("head", (0, 0, 0)), ("hat", (0, 0, 0)), ("body", (0, 0, 0)), ("right_arm", (-ARM_X, ARM_Y, 0)), ("left_arm", (ARM_X, ARM_Y, 0)),
           ("right_leg", (-LEG_X, LEG_Y, 0)), ("left_leg", (LEG_X, LEG_Y, 0))]
SKIN, SKIN_SH, SKIN_DK = (246, 230, 226), (224, 202, 204), (196, 170, 178)


# ================================================================== pintores
def flat(c, edge=None, top=None, bottom=None):
    dk = edge or tuple(int(v * 0.8) for v in c)

    def fn(f, x, y, w, h):
        if f == "top":
            return top or tuple(min(255, int(v * 1.06)) for v in c)
        if f == "bottom":
            return bottom or dk
        if x == 0 or x == w - 1 or y == h - 1:
            return dk
        return c
    return fn


def grad(c, hi, dk, seam=None, every=24):
    """Tela: más clara arriba, costuras cada tanto y borde inferior más oscuro."""
    def fn(f, x, y, w, h):
        if f == "top":
            return hi
        if f == "bottom":
            return dk
        if y >= h - 2:
            return dk
        if seam and (x % every == 0):
            return seam
        return mix(hi, c, min(1.0, y / max(1, h) * 1.3))
    return fn


def pleats(c, hi, dk, n=6):
    def fn(f, x, y, w, h):
        if f in ("top", "bottom"):
            return dk
        if y >= h - 2:
            return dk
        k = (x // n) % 2
        return hi if k == 0 and x % n < n - 1 else (c if k == 0 else mix(c, dk, 0.4))
    return fn


def skin_p(f, x, y, w, h):
    if f == "bottom":
        return SKIN_SH
    if x == 0 or x == w - 1:
        return SKIN_SH
    return SKIN


def band(base, stripes):
    def fn(f, x, y, w, h):
        if f in ("top", "bottom"):
            return base(f, x, y, w, h)
        t = y / max(1, h - 1)
        for a, b, c in stripes:
            if a <= t <= b:
                return c
        return base(f, x, y, w, h)
    return fn


def hair_p(c, hi, dk):
    def fn(f, x, y, w, h):
        if f == "top":
            return hi if (x // 8 + y // 8) % 3 == 0 else c
        if f == "bottom":
            return dk
        stripe = (x // 6) % 4
        col = hi if stripe == 0 and y < h * 0.4 else dk if stripe == 2 else c
        if y >= h - 2:
            col = dk
        return col
    return fn


def frill(c, sh):
    def fn(f, x, y, w, h):
        if f in ("top", "bottom"):
            return sh
        if y >= h - 2:
            return c if (x // 4) % 2 == 0 else T
        return c if (x // 4) % 2 == 0 else sh
    return fn


def metal(c, hi):
    return lambda f, x, y, w, h: hi if (x + y) % 9 == 0 or f == "top" else c


def gem(c, hi):
    def fn(f, x, y, w, h):
        if f == "front" and x < w * 0.45 and y < h * 0.45:
            return (255, 255, 255)
        return mix(hi, c, (x + y) / max(1, w + h))
    return fn


def stock(c, dk, hi=None):
    """Medias: más claras al frente de la rodilla."""
    def fn(f, x, y, w, h):
        if f in ("top", "bottom"):
            return c
        col = c if f == "front" else dk
        if hi and f == "front" and h * 0.75 < y < h and abs(x - w / 2) < w * 0.3:
            col = mix(col, hi, 0.45)
        if x == 0 or x == w - 1:
            col = mix(col, dk, 0.6)
        return col
    return fn


def boot(c, sh, dk, accent=None, lace=None):
    def fn(f, x, y, w, h):
        if f == "top":
            return c
        if f == "bottom":
            return dk
        if accent and y < 3:
            return accent
        if lace and f == "front" and (y // 6) % 2 == 0 and abs(x - w / 2) < 4 and y > 4:
            return lace
        if f in ("left", "right") and (y // 10) % 2 == 1 and 3 < x < w - 3:
            return sh
        if x == 0 or x == w - 1:
            return dk
        return c
    return fn


def sole(c, top_accent=None):
    return lambda f, x, y, w, h: (top_accent if (f == "top" and top_accent and y < h * 0.3) else c)


# ================================================================== cuerpo
# Silueta: ancho de pecho, busto (ancho, fondo), cintura y cadera. Cada chica elige una.
SHAPES = {"std": (1.0, 1.0, 1.0, 1.0, 1.0), "curvy": (1.0, 1.04, 1.55, 0.94, 1.07), "slim": (0.95, 0.95, 0.65, 0.96, 0.96),
          "petite": (0.92, 0.9, 0.5, 0.92, 0.93), "athletic": (1.07, 1.0, 0.9, 1.06, 1.0), "busty": (1.02, 1.06, 1.8, 0.92, 1.08)}
SHAPE = SHAPES["std"]


def figure(top, waist, hips, sleeve, upper, fore, hand, thigh, calf, bust=None):
    cw, bw, bd, ww, hw = SHAPE
    return [Part("neck", "body", (0, 0, 0), (0, 0, 0), [Box(-1.1, -1.3, -1.0, 2.2, 1.7, 2.0, skin_p)]),
            Part("chest", "body", (0, 0, 0), (0, 0, 0), [Box(-3.3 * cw, 0.0, -1.7, 6.6 * cw, 4.4, 3.4, top)]),
            Part("bust", "body", (0, 1.55, -1.65), (-0.32 - 0.04 * (bd - 1), 0, 0), [Box(-2.9 * bw, 0.0, -1.1 * bd + 0.1, 5.8 * bw, 2.5 + 0.25 * (bd - 1), 1.1 * bd, bust or top)]),
            Part("waist", "body", (0, 0, 0), (0, 0, 0), [Box(-2.7 * ww, 4.2, -1.5, 5.4 * ww, 2.4, 3.0, waist)]),
            Part("hips", "body", (0, 0, 0), (0, 0, 0), [Box(-3.6 * hw, 6.4, -1.85, 7.2 * hw, 3.2, 3.7, hips)]),
            Part("shoulder_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.5, -1.6, -1.4, 2.8, 2.4, 2.8, sleeve)]),
            Part("upper_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.2, 0.6, -1.1, 2.2, 4.6, 2.2, upper)]),
            Part("fore_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.1, 5.0, -1.0, 2.0, 4.4, 2.0, fore)]),
            Part("hand_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.15, 9.2, -1.1, 2.1, 2.0, 2.2, hand)]),
            Part("thigh_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-1.8, -0.4, -1.85, 3.6, 7.4, 3.7, thigh)]),
            Part("calf_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-1.5, 6.8, -1.5, 3.0, 6.6, 3.0, calf)])]


def shoe_parts(paint, sole_paint, top_y=10.0, cuff=None, flap=None, heel=True):
    P = [Part("boot_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-1.8, top_y, -2.0, 3.6, 13.2 - top_y, 3.9, paint)]),
         Part("foot_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-1.8, 12.6, -3.0, 3.6, 1.0, 4.9, paint)]),
         Part("sole_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-1.95, 13.6, -3.2, 3.9, 1.4, 5.3, sole_paint)])]
    if cuff:
        P.append(Part("cuff_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.15, top_y - 0.8, -2.35, 4.3, 1.6, 4.6, cuff)]))
    if flap:
        P.append(Part("flap_r", "right_leg", (0, top_y - 1.0, -2.4), (-0.15, 0, 0), [Box(-1.2, 0, -0.45, 2.4, 2.6, 0.5, flap)]))
    return P


def skirt(paint, hem, length=4.6, y=6.6, flare=0.32):
    P = []

    def panel(name, pivot, rot, w):
        return Part(name, "body", pivot, rot, [Box(-w / 2, 0, -0.4, w, length, 0.8, paint), Box(-w / 2 + 0.1, length - 0.3, -0.3, w - 0.2, 0.7, 0.6, hem)])
    hw = SHAPE[4]
    P.append(panel("skirt_front", (0, y, -1.95), (-flare, 0, 0), 7.4 * hw))
    P.append(panel("skirt_back", (0, y, 1.95), (-flare, math.pi, 0), 7.4 * hw))
    P.append(panel("skirt_right", (-3.65 * hw, y, 0), (-flare, math.pi / 2, 0), 4.0))
    P.append(panel("skirt_left", (3.65 * hw, y, 0), (-flare, -math.pi / 2, 0), 4.0))
    return P


def bow(name, parent, pivot, rot, paint, s=1.0):
    return [Part(name + "_knot", parent, pivot, rot, [Box(-0.5 * s, -0.45 * s, -0.3 * s, 1.0 * s, 0.9 * s, 0.6 * s, paint)]),
            Part(name + "_wl", name + "_knot", (-0.4 * s, 0, 0), (0, 0, 0.25), [Box(-1.7 * s, -0.6 * s, -0.2 * s, 1.7 * s, 1.2 * s, 0.4 * s, paint)]),
            Part(name + "_wr", name + "_knot", (0.4 * s, 0, 0), (0, 0, -0.25), [Box(0, -0.6 * s, -0.2 * s, 1.7 * s, 1.2 * s, 0.4 * s, paint)])]


# ================================================================== cabello
def hair_base(hp, bottoms, back_len=6.4, lock_len=5.4):
    P = [Part("hair_top", "head", (0, 0, 0), (0, 0, 0), [Box(-4.55, -8.95, -4.65, 9.1, 1.6, 9.5, hp)]),
         Part("hair_back", "head", (0, 0, 0), (0, 0, 0), [Box(-4.65, -8.0, -0.2, 9.3, back_len, 4.9, hp)]),
         Part("hair_side_r", "head", (0, 0, 0), (0, 0, 0), [Box(-4.9, -8.0, -4.0, 1.45, 4.6, 7.8, hp)]),
         Part("hair_lock_r", "head", (0, 0, 0), (0, 0, 0), [Box(-4.75, -6.4, -4.55, 1.35, lock_len, 1.5, hp)])]
    for i, b in enumerate(bottoms):
        x0 = -4.45 + i * 1.1125
        P.append(Part("bang%d" % i, "head", (0, 0, 0), (0, 0, 0), [Box(x0, -8.6, -4.75, 1.12, 8.6 + b, 0.9, hp)]))
    return P


def chain(name, parent, pivot, rot, segs, hp):
    """Mechón articulado: lista de (largo, ancho, fondo, giro x, giro z) que va bajando."""
    P = []
    par = parent
    piv = pivot
    r = rot
    for i, (l, w, d, rx, rz) in enumerate(segs):
        n = "%s%d" % (name, i)
        P.append(Part(n, par, piv, r if i == 0 else (rx, 0, rz), [Box(-w / 2, 0, -d / 2, w, l, d, hp)]))
        par, piv = n, (0, l - 0.2, 0)
    return P


# ================================================================== caras (16x16 en píxel → 64 px)
EYE = {"normal": ["LLLLLL", ".DTTHW", ".TTTTW", "..ttt."], "focused": ["......", "LLLLLL", ".DTTHW", "..ttt."],
       "tired": ["......", "......", "LLLLLL", "..tTt."], "surprised": [".LLLLL", ".WTTHW", ".TTTTW", ".TTttW", "..ttt."],
       "blink": ["......", "......", "......", "LLLLLL"], "happy": ["......", "..LLL.", ".L...L", "......"]}
MOUTH = {"normal": ["....", ".MM."], "focused": ["....", ".MM."], "tired": ["....", ".mm."], "surprised": ["....", ".oo."],
         "blink": ["....", ".MM."], "happy": ["m..m", ".MM."]}
EXPRS = [("normal", ""), ("blink", "_blink"), ("happy", "_happy"), ("surprised", "_surprised"), ("focused", "_focused"), ("tired", "_tired")]


# Formas de ojo (fila 0 arriba). "focused"/"tired" se derivan de "normal".
EYES = {"round": (["LLLLLL", ".DTTHW", ".TTTTW", "..ttt."], 8),
        "sharp": (["LL....", ".LLLLL", ".DTTHW", "..ttt."], 8),
        "sleepy": (["......", "LLLLLL", ".TTTHW", "..tt.."], 8),
        "big": (["LLLLLL", "LDTTHW", ".TTTTW", ".TttHW", "..ttt."], 7),
        "cat": (["LLLLLL", ".DTKTW", ".TTKTW", "..tKt."], 8),
        "gentle": (["..LLLL", ".LDTHW", ".TTTTW", "..tt.."], 8),
        "blind": (["......"], 8)}


def eye_rows(expr, style):
    rows, ey = EYES.get(style, EYES["round"])
    if style == "blind":
        return [], ey
    if expr == "normal":
        return rows, ey
    if expr == "focused":
        return ["......", "LLLLLL"] + rows[-3:-1] if len(rows) >= 4 else EYE["focused"], 8
    if expr == "tired":
        return ["......", "......", "LLLLLL", rows[-1]], 8
    if expr == "surprised":
        return EYE["surprised"], 7
    return EYE[expr], 8


def face_tex(expr, iris, lash, hair, blush=(240, 170, 190), eye="round", marks=(), fang=False):
    dk, mid, hi = iris
    N = 16
    g = [[SKIN] * N for _ in range(N)]
    for y in range(N):
        for x in range(N):
            c = SKIN
            if y < 6:
                c = mix(SKIN_SH, SKIN, y / 6)
            if min(x, N - 1 - x) < 1:
                c = mix(c, SKIN_SH, 0.6)
            if y >= 15:
                c = SKIN_SH
            g[y][x] = c
    col = {"K": lash, "L": lash, "D": mix(lash, mid, 0.4), "T": dk, "t": mid, "H": hi, "W": (240, 240, 246),
           "M": (196, 128, 146), "m": (222, 184, 196), "o": (130, 60, 80)}
    for (mx, my, mc) in marks:
        g[my][mx] = mc
    rows, ey = eye_rows(expr, eye)
    if eye == "blind":
        rows = []
    for j, row in enumerate(rows):
        for i, ch in enumerate(row):
            if ch != ".":
                g[ey + j][1 + i] = col[ch]
                g[ey + j][N - 2 - i] = col[ch]
    if expr in ("normal", "happy", "blink"):
        for x in (2, 3, 12, 13):
            g[12][x] = mix(g[12][x], blush, 0.4)
    for j, row in enumerate(MOUTH[expr]):
        for i, ch in enumerate(row):
            if ch != ".":
                g[12 + j][6 + i] = col[ch]
    if fang:
        g[14][8] = (250, 250, 250)
    img = Image.new("RGBA", (N, N))
    for y in range(N):
        for x in range(N):
            img.putpixel((x, y), g[y][x] + (255,))
    tex = Image.new("RGBA", (256, 128), T)
    S = 8

    def fill(u, v, w, h, c):
        tex.paste(Image.new("RGBA", (w * S, h * S), c + (255,)), (u * S, v * S))
    fill(8, 0, 8, 8, hair)
    fill(16, 0, 8, 8, SKIN_SH)
    fill(0, 8, 8, 8, SKIN_SH)
    fill(16, 8, 8, 8, SKIN_SH)
    fill(24, 8, 8, 8, hair)
    tex.paste(img.resize((64, 64), Image.NEAREST), (64, 64))
    return tex


# ================================================================== KOZUE — carpintera (mesa de crafteo)
def kozue():
    H, HH, HD = (120, 74, 44), (162, 108, 66), (82, 48, 28)
    hp = hair_p(H, HH, HD)
    CREAM, CREAM_HI, CREAM_DK = (246, 238, 220), (255, 250, 238), (214, 200, 176)
    OAK, OAK_HI, OAK_DK = (178, 132, 80), (208, 166, 112), (124, 86, 50)
    LEAF, LEAF_HI = (88, 162, 70), (128, 200, 98)
    SOCK, SOCK_DK = (92, 62, 44), (66, 44, 30)
    BOOT_C, BOOT_SH, BOOT_DK = (126, 82, 48), (104, 66, 38), (78, 50, 28)
    blouse = grad(CREAM, CREAM_HI, CREAM_DK)
    overall = grad(OAK, OAK_HI, OAK_DK, seam=OAK_DK, every=20)
    P = figure(top=blouse, waist=overall, hips=overall, sleeve=blouse, upper=blouse, fore=skin_p,
               hand=band(skin_p, [(0.0, 0.55, LEAF)]), thigh=band(skin_p, [(0.62, 0.7, LEAF), (0.7, 1.0, SOCK)]),
               calf=stock(SOCK, SOCK_DK))
    P += shoe_parts(boot(BOOT_C, BOOT_SH, BOOT_DK, lace=CREAM), sole((70, 46, 30)), top_y=10.0, cuff=flat(BOOT_SH))
    # peto del overol, tirantes, botones, pañuelo verde
    P.append(Part("bib", "body", (0, 0, 0), (0, 0, 0), [Box(-2.4, 2.4, -2.05, 4.8, 4.2, 0.4, overall)]))
    P.append(Part("pocket", "body", (0, 0, 0), (0, 0, 0), [Box(-1.2, 3.8, -2.2, 2.4, 1.4, 0.2, flat(OAK_DK))]))
    P.append(Part("strap_r", "body", (-2.0, 0, 0), (0, 0, 0), [Box(-0.5, -0.3, -1.9, 1.0, 3.0, 3.8, flat(OAK_DK))]))
    P.append(Part("button_r", "body", (-1.9, 2.6, -2.25), (0, 0, 0), [Box(-0.4, -0.4, -0.15, 0.8, 0.8, 0.3, metal((226, 182, 66), (255, 230, 130)))]))
    P.append(Part("scarf", "body", (0, 0.0, -1.85), (0.3, 0, 0), [Box(-1.5, -0.4, -0.4, 3.0, 2.0, 0.5, flat(LEAF, top=LEAF_HI))]))
    P += skirt(pleats(OAK, OAK_HI, OAK_DK), flat(OAK_DK), length=3.8)
    # cinturón de herramientas: martillo, regla y bolsa
    P.append(Part("toolbelt", "body", (0, 0, 0), (0, 0, 0), [Box(-3.75, 6.2, -2.0, 7.5, 0.9, 4.0,
              lambda f, x, y, w, h: (226, 182, 66) if f == "front" and abs(x - w / 2) < 3 else (96, 62, 36))]))
    P.append(Part("hammer", "body", (3.7, 6.8, -0.4), (0, 0, -0.18), [Box(-0.28, 0, -0.28, 0.56, 3.4, 0.56, flat((128, 86, 50))),
                                                                     Box(-0.6, 3.2, -1.0, 1.2, 0.9, 2.0, metal((150, 156, 166), (210, 216, 224)))]))
    P.append(Part("pouch_r", "body", (-2.6, 6.9, -1.9), (0, 0.3, 0), [Box(-0.9, 0, -0.9, 1.8, 1.9, 1.0, flat((112, 74, 44)))]))
    P.append(Part("ruler", "body", (-3.7, 6.6, 1.0), (0, 0, 0.12), [Box(-0.2, 0, -0.4, 0.4, 3.6, 0.8, lambda f, x, y, w, h: (40, 30, 20) if y % 6 == 0 else (236, 200, 90))]))
    # manga abullonada remangada
    P.append(Part("puff_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.8, -1.9, -1.7, 3.4, 3.4, 3.4, blouse)]))
    P.append(Part("roll_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.4, 4.4, -1.3, 2.6, 0.9, 2.6, flat(CREAM_DK))]))
    # cabello: coleta alta, flequillo, pañuelo con nudo, lápiz
    P += hair_base(hp, [-5.2, -4.9, -5.5, -5.0, -5.1, -5.5, -4.9, -5.3], back_len=5.8)
    P += chain("tail", "head", (0, -6.6, 4.6), (0.75, 0, 0), [(3.0, 2.8, 2.4, 0, 0), (3.6, 2.5, 2.1, -0.55, 0), (3.4, 2.0, 1.7, -0.3, 0), (2.4, 1.3, 1.1, -0.1, 0)], hp)
    P.append(Part("bandana", "head", (0, 0, 0), (0, 0, 0), [Box(-4.75, -7.6, -4.8, 9.5, 1.2, 9.7,
              lambda f, x, y, w, h: None if f in ("top", "bottom") else ((56, 112, 44) if (x // 4 + y // 4) % 4 == 0 else LEAF))]))
    P.append(Part("knot", "head", (2.0, -7.0, 4.9), (0.3, 0, 0.3), [Box(-0.8, -0.8, -0.4, 1.6, 1.6, 0.8, flat(LEAF)),
                                                                   Box(-1.4, 0.3, 0.0, 1.0, 2.2, 0.4, flat(LEAF_HI)), Box(0.4, 0.3, 0.0, 1.0, 2.0, 0.4, flat(LEAF_HI))]))
    P.append(Part("pencil", "head", (-4.9, -3.9, -0.2), (0.25, 0, 0.2), [Box(-0.25, -0.25, -2.0, 0.5, 0.5, 4.0, flat((236, 196, 60)))]))
    return P, dict(iris=((30, 92, 40), (84, 170, 76), (180, 236, 140)), lash=(58, 32, 20), hair=H)


# ================================================================== HOMURA — herrera (horno)
def homura():
    H, HH, HD = (206, 58, 36), (246, 116, 62), (140, 32, 22)
    hp = hair_p(H, HH, HD)
    CHAR, CHAR_HI, CHAR_DK = (46, 42, 50), (70, 64, 76), (28, 26, 32)
    FIRE1, FIRE2, FIRE3 = (255, 220, 100), (250, 142, 40), (218, 64, 30)
    LEATHER, LEATHER_HI, LEATHER_DK = (112, 70, 44), (146, 96, 62), (74, 46, 30)
    STONE, STONE_HI, STONE_DK = (124, 124, 130), (158, 158, 164), (86, 86, 94)
    top = band(grad(CHAR, CHAR_HI, CHAR_DK), [(0.0, 0.08, FIRE2)])
    corset = lambda f, x, y, w, h: FIRE2 if (f == "front" and abs(x - w / 2) < 2 and (y // 4) % 2 == 0) else (LEATHER_DK if f == "front" else CHAR_DK)

    def flames(f, x, y, w, h):
        if f in ("top", "bottom"):
            return STONE_DK
        edge = h - (3 + 4 * abs(math.sin(x * 0.4)))
        if y >= edge:
            t = (y - edge) / max(1, h - edge)
            return FIRE1 if t > 0.7 else FIRE2 if t > 0.35 else FIRE3
        bx, by = (x + (6 if (y // 6) % 2 else 0)) % 12, y % 6
        return STONE_DK if bx == 0 or by == 0 else STONE
    tights = stock((52, 44, 56), (34, 30, 38), (92, 80, 96))
    P = figure(top=top, waist=corset, hips=grad(STONE, STONE_HI, STONE_DK), sleeve=skin_p, upper=skin_p,
               fore=grad(LEATHER, LEATHER_HI, LEATHER_DK), hand=grad(LEATHER, LEATHER_HI, LEATHER_DK),
               thigh=band(skin_p, [(0.36, 0.44, FIRE2), (0.44, 1.0, (52, 44, 56))]), calf=tights)
    P += shoe_parts(boot(CHAR, CHAR_HI, CHAR_DK, accent=FIRE2, lace=FIRE2), sole((90, 92, 104), (150, 156, 166)), top_y=9.2,
                    cuff=metal((110, 112, 122), (170, 176, 186)))
    P.append(Part("choker", "body", (0, 0, 0), (0, 0, 0), [Box(-1.25, -0.9, -1.1, 2.5, 0.6, 2.2, flat((40, 30, 34)))]))
    P.append(Part("ember", "body", (0, -0.5, -1.25), (0, 0, 0.78), [Box(-0.35, -0.35, -0.2, 0.7, 0.7, 0.35, gem(FIRE3, FIRE1))]))
    P += skirt(flames, flat(FIRE2), length=4.6)

    def apron(f, x, y, w, h):
        cx, cy = w / 2, h * 0.45
        if f == "front" and ((x - cx) ** 2 / (w * 0.14) ** 2 + (y - cy) ** 2 / (h * 0.2) ** 2) < 1:
            return FIRE2
        return LEATHER_HI if (x + y) % 13 == 0 else LEATHER
    P.append(Part("apron", "body", (0, 6.4, -2.1), (-0.28, 0, 0), [Box(-2.3, 0, -0.3, 4.6, 5.0, 0.3, apron)]))
    P.append(Part("apron_belt", "body", (0, 0, 0), (0, 0, 0), [Box(-3.75, 6.1, -2.0, 7.5, 0.8, 4.0, flat(LEATHER_DK))]))
    P.append(Part("tongs", "body", (3.7, 6.6, 0.3), (0, 0, -0.14), [Box(-0.2, 0, -0.4, 0.4, 3.8, 0.35, metal((64, 64, 70), (180, 186, 196))),
                                                                   Box(-0.2, 0, 0.15, 0.4, 3.8, 0.35, metal((64, 64, 70), (180, 186, 196)))]))
    P.append(Part("strap_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.35, 4.6, -1.25, 2.5, 1.0, 2.5, flat(FIRE3))]))
    P.append(Part("gauntlet_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.4, 5.6, -1.3, 2.6, 1.6, 2.6, metal((100, 102, 112), (170, 176, 186)))]))
    P.append(Part("pauldron_r", "right_arm", (0, -1.4, 0), (0, 0, 0.25), [Box(-2.0, -0.6, -1.6, 3.0, 1.4, 3.2, grad(LEATHER, LEATHER_HI, LEATHER_DK))]))
    # cabello: coletas largas, flequillo en picos, gafas de forja, broches de llama
    P += hair_base(hp, [-5.6, -4.8, -5.4, -4.6, -5.5, -4.7, -5.3, -5.0], back_len=6.0)
    for s, nm in ((-1, "r"), (1, "l")):
        P += chain("twin_" + nm, "head", (s * 3.9, -7.2, 2.4), (0.25, 0, -s * 0.35),
                   [(4.0, 2.6, 2.6, 0, 0), (4.4, 2.4, 2.3, 0.1, s * 0.18), (4.2, 2.1, 2.0, 0.1, s * 0.12), (3.0, 1.5, 1.4, 0.08, s * 0.08)], hp)
        P.append(Part("tie" + nm.upper(), "head", (s * 3.9, -7.2, 2.4), (0, 0, 0), [Box(-0.9, -0.9, -0.9, 1.8, 1.8, 1.8, flat(FIRE3))]))
    P.append(Part("goggles", "head", (0, -8.0, -2.8), (-0.45, 0, 0), [Box(-4.7, -0.4, -0.3, 9.4, 0.8, 0.6, flat((40, 32, 30)))]))
    for s, nm in ((-1, "r"), (1, "l")):
        P.append(Part("lens_" + nm, "goggles", (s * 1.8, 0, -0.5), (0, 0, 0), [Box(-1.2, -1.0, -0.6, 2.4, 2.0, 1.2,
                  lambda f, x, y, w, h: (255, 220, 160) if f == "front" and x < w * 0.35 and y < h * 0.35 else (226, 120, 40) if f == "front" else (110, 90, 60))]))
    flame = lambda f, x, y, w, h: FIRE1 if y > h * 0.6 else FIRE2 if y > h * 0.3 else FIRE3
    P.append(Part("clip_r", "head", (-3.6, -5.9, -4.4), (0, 0, -0.35), [Box(-0.6, -1.6, -0.3, 1.2, 2.0, 0.6, flame)]))
    return P, dict(iris=((140, 46, 10), (236, 140, 32), (255, 224, 128)), lash=(60, 18, 16), hair=H)


# ================================================================== KURA — sirvienta (cofre)
def kura():
    H, HH, HD = (246, 214, 128), (255, 238, 184), (200, 160, 80)
    hp = hair_p(H, HH, HD)
    CH, CH_HI, CH_DK = (162, 108, 50), (196, 142, 74), (104, 68, 30)
    WH, WH_SH = (252, 250, 247), (222, 216, 216)
    GOLD, GOLD_HI = (226, 180, 64), (255, 230, 130)
    dress = grad(CH, CH_HI, CH_DK, seam=CH_DK, every=16)
    bodice = lambda f, x, y, w, h: (WH if (x + y) % 9 else WH_SH) if f == "front" and 0.25 * w < x < 0.75 * w else dress(f, x, y, w, h)
    P = figure(top=bodice, waist=flat(WH, WH_SH), hips=dress, sleeve=dress, upper=skin_p, fore=skin_p, hand=skin_p,
               thigh=band(skin_p, [(0.4, 0.5, CH_DK), (0.5, 1.0, WH)]), calf=stock(WH, WH_SH), bust=bodice)
    P += shoe_parts(boot((76, 46, 28), (104, 66, 40), (52, 32, 20), accent=(76, 46, 28)), sole((46, 30, 20)), top_y=11.6)
    P.append(Part("strap_shoe_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-1.9, 11.9, -2.1, 3.8, 0.5, 4.1, flat((52, 32, 20)))]))
    P.append(Part("collar", "body", (0, 0, 0), (0, 0, 0), [Box(-3.4, -0.5, -1.8, 6.8, 1.0, 3.6, flat(WH, WH_SH))]))
    P.append(Part("collar_tip_r", "body", (-0.7, 0.4, -1.85), (0, 0, 0.5), [Box(-1.8, -0.5, -0.25, 1.8, 1.4, 0.35, flat(WH, WH_SH))]))
    P.append(Part("latch", "body", (0, 1.2, -2.3), (0.15, 0, 0), [Box(-0.7, -0.4, -0.35, 1.4, 1.7, 0.5, metal(GOLD, GOLD_HI)),
                                                                 Box(-0.25, 0.25, -0.5, 0.5, 0.5, 0.2, flat((60, 40, 20)))]))
    P += bow("back_bow", "body", (0, 5.4, 1.65), (0, 0, 0), flat(WH, WH_SH), 1.3)
    P += skirt(pleats(CH, CH_HI, CH_DK, 5), frill(WH, WH_SH), length=5.0, flare=0.36)
    P.append(Part("petticoat", "body", (0, 0, 0), (0, 0, 0), [Box(-4.0, 9.6, -2.3, 8.0, 1.4, 4.6, frill(WH, WH_SH))]))
    P.append(Part("apron", "body", (0, 6.3, -2.1), (-0.34, 0, 0), [Box(-2.3, 0, -0.3, 4.6, 4.4, 0.3, flat(WH, WH_SH)),
                                                                   Box(-2.5, 4.3, -0.35, 5.0, 0.7, 0.4, frill(WH, WH_SH))]))

    def chestbox(f, x, y, w, h):
        if y < 6 or abs(y - h * 0.38) < 3:
            return CH_DK
        if f == "back" and abs(x - w / 2) < 5 and abs(y - h * 0.45) < 7:
            return GOLD
        return CH if (x // 6) % 2 else CH_HI
    P.append(Part("backpack", "body", (0, 0.6, 1.6), (0.06, 0, 0), [Box(-2.6, 0, 0, 5.2, 4.8, 2.5, chestbox)]))
    P.append(Part("pack_strap_r", "body", (-1.9, 0, 0), (0, 0, 0), [Box(-0.4, -0.3, -1.85, 0.8, 4.0, 3.7, flat((96, 62, 30)))]))
    P.append(Part("puff_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.85, -1.9, -1.75, 3.5, 3.6, 3.5, band(dress, [(0.8, 1.0, WH)]))]))
    P.append(Part("cuff_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.3, 8.6, -1.25, 2.4, 0.8, 2.5, frill(WH, WH_SH))]))
    # cabello largo y liso hasta la espalda, cofia de sirvienta
    P += hair_base(hp, [-5.0, -5.3, -4.9, -5.2, -5.2, -4.9, -5.3, -5.0], back_len=7.6, lock_len=10.5)
    P.append(Part("long_back", "head", (0, -1.0, 3.2), (0.05, 0, 0), [Box(-4.5, 0, -1.4, 9.0, 9.5, 2.0, hp)]))
    P.append(Part("long_side_r", "head", (-4.2, -1.0, 2.0), (0, 0, 0.06), [Box(-1.0, 0, -1.8, 1.8, 7.5, 2.6, hp)]))
    P.append(Part("headdress", "head", (0, -8.6, -1.6), (-0.45, 0, 0), [Box(-3.4, -1.5, -0.4, 6.8, 1.7, 0.8, frill(WH, WH_SH))]))
    P.append(Part("headband", "head", (0, 0, 0), (0, 0, 0), [Box(-4.7, -8.3, -1.2, 9.4, 0.7, 2.4, flat(CH_DK))]))
    P += bow("hair_bow", "head", (3.4, -7.4, 1.6), (0, -0.6, 0), flat(CH_DK), 0.8)
    return P, dict(iris=((24, 60, 150), (72, 134, 228), (176, 220, 255)), lash=(100, 70, 40), hair=H)


# ================================================================== SHIORI — maga (mesa de encantamientos)
def shiori():
    H, HH, HD = (70, 50, 146), (112, 92, 196), (40, 28, 96)
    hp = hair_p(H, HH, HD)
    OBS, OBS_HI, OBS_DK = (42, 32, 62), (74, 56, 108), (24, 18, 38)
    DIA, DIA_HI, DIA_DK = (96, 224, 218), (204, 255, 250), (52, 152, 152)
    RED, RED_DK = (182, 42, 56), (124, 26, 40)
    GOLD, GOLD_HI = (226, 180, 64), (255, 230, 130)
    robe = band(grad(OBS, OBS_HI, OBS_DK, seam=DIA_DK, every=26), [(0.0, 0.08, DIA)])
    corset = lambda f, x, y, w, h: DIA if f == "front" and (abs(x - w / 2) < 1 or y in (1, h - 2)) else OBS_DK
    rune = lambda f, x, y, w, h: DIA if (f in ("front", "back") and y >= h - 6 and (x // 3) % 2 == 0) else pleats(OBS, OBS_HI, OBS_DK, 7)(f, x, y, w, h)
    P = figure(top=robe, waist=corset, hips=grad(OBS, OBS_HI, OBS_DK), sleeve=robe, upper=robe, fore=robe, hand=skin_p,
               thigh=stock((40, 32, 52), (26, 20, 34)), calf=stock((40, 32, 52), (26, 20, 34)))
    P += shoe_parts(boot(OBS, OBS_HI, OBS_DK, accent=DIA, lace=DIA_DK), sole((30, 24, 40), DIA), top_y=7.6, cuff=flat(DIA_DK), flap=flat(OBS_HI, DIA_DK))
    P.append(Part("collar", "body", (0, 0, 0), (0, 0, 0), [Box(-1.8, -1.6, -1.3, 3.6, 1.8, 2.6, flat(DIA_DK))]))
    P.append(Part("brooch", "body", (0, 0.7, -1.95), (0, 0, 0.78), [Box(-0.6, -0.6, -0.3, 1.2, 1.2, 0.5, gem(DIA, DIA_HI))]))
    P.append(Part("sash", "body", (0, 0, 0), (0, 0, 0), [Box(-2.85, 5.7, -1.65, 5.7, 0.8, 3.3, flat(RED, RED_DK))]))
    P += skirt(rune, flat(DIA), length=5.6, flare=0.26)
    P.append(Part("cape", "body", (0, -0.4, 1.75), (0.1, 0, 0), [Box(-3.6, 0, 0, 7.2, 16.0, 0.4, lambda f, x, y, w, h: RED_DK if y >= h - 6 or x % 14 == 0 else RED)]))
    P.append(Part("cape_clasp_r", "body", (-2.3, -0.2, -0.1), (0, 0, 0), [Box(-0.45, -0.45, -1.7, 0.9, 0.9, 0.9, metal(GOLD, GOLD_HI))]))
    P.append(Part("bell_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.75, 5.0, -1.75, 3.3, 3.6, 3.5, band(grad(OBS_HI, OBS_HI, OBS), [(0.8, 1.0, DIA)]))]))
    # corte hime: flequillo recto, mechones laterales rectos y cabello muy largo; diadema y orejas de elfa
    P += hair_base(hp, [-5.2] * 8, back_len=7.8, lock_len=11.0)
    P.append(Part("long_back", "head", (0, -0.6, 3.2), (0.04, 0, 0), [Box(-4.5, 0, -1.4, 9.0, 13.5, 2.0, hp)]))
    P.append(Part("circlet", "head", (0, 0, 0), (0, 0, 0), [Box(-4.75, -7.4, -4.8, 9.5, 0.7, 9.7, lambda f, x, y, w, h: None if f in ("top", "bottom") else GOLD)]))
    P.append(Part("tiara_gem", "head", (0, -7.1, -4.95), (0, 0, 0.78), [Box(-0.75, -0.75, -0.3, 1.5, 1.5, 0.55, gem(DIA, DIA_HI))]))

    def ear(f, x, y, w, h):
        if f == "front" and 2 < y < h - 2 and x > 3:
            return (200, 160, 220)
        return SKIN
    P.append(Part("ear_r", "head", (-4.3, -2.8, 0.6), (0, 0.2, -0.12), [Box(-3.4, -0.9, -0.55, 3.4, 1.8, 1.1, ear), Box(-4.6, -0.7, -0.4, 1.3, 1.0, 0.8, ear)]))
    P.append(Part("earring_r", "ear_r", (-3.0, 0.9, 0), (0, 0, 0), [Box(-0.25, 0, -0.25, 0.5, 1.2, 0.5, gem(DIA, DIA_HI))]))
    return P, dict(iris=((18, 98, 112), (58, 202, 202), (206, 255, 250)), lash=(34, 20, 66), hair=H)


WAIFUS = {"kozue": kozue, "homura": homura, "kura": kura, "shiori": shiori}


def mirror_all(P):
    m = G.mirror([p for p in P if p.name.endswith("_r") and p.parent in ("head", "body", "right_arm", "right_leg")])
    for p in m:
        p.parent = p.parent.replace("right", "left")
    return P + m


def build(name):
    P, info = WAIFUS[name]()
    P = mirror_all(P)
    out = os.path.join(TEXR, name)
    os.makedirs(out, exist_ok=True)
    for f in os.listdir(out):
        os.remove(os.path.join(out, f))
    for expr, suf in EXPRS:
        face_tex(expr, info["iris"], info["lash"], info["hair"]).save(os.path.join(out, "face" + suf + ".png"))
    G.S = 8
    G.AO = True
    G.jit = lambda c, a=4: c
    w, h = G.pack(P, 64)
    G.build_texture(P, w, h).save(os.path.join(out, "body.png"))
    return P, (w, h)


def main():
    java = ["// GENERADO por tools/gen_blocky.py — no editar a mano.", "package com.totallywaifu.client;", "",
            "import net.minecraft.client.model.geom.ModelPart;", "import net.minecraft.client.model.geom.PartPose;",
            "import net.minecraft.client.model.geom.builders.*;", "", "import java.util.LinkedHashMap;", "import java.util.Map;", "",
            "/** Modelos de bloques de las waifus (cuerpo alto y esbelto + ropa de su oficio). */",
            "public final class WaifuModels {", "    private WaifuModels() {}", "",
            "    public static final float ARM_X = %s, ARM_Y = %s, LEG_X = %s, LEG_Y = %s;" % tuple(G.fnum(v) for v in (ARM_X, ARM_Y, LEG_X, LEG_Y)), "",
            "    public static LayerDefinition face() {",
            "        MeshDefinition mesh = new MeshDefinition();",
            "        mesh.getRoot().addOrReplaceChild(\"head\", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.ZERO);",
            "        return LayerDefinition.create(mesh, 32, 16);", "    }", ""]
    for name in WAIFUS:
        P, (w, h) = build(name)
        cap = name.capitalize()
        java += G.java_layer("body" + cap, P, ANCHORS, w, h) + [""]
        java += G.java_bake("bakeBody" + cap, P, ANCHORS) + [""]
        print("%s: %d piezas, textura %dx%d" % (name, len(P), w * 8, h * 8))
    java += ["}", ""]
    open(JAVA, "w").write("\n".join(java))


def preview(outdir):
    os.makedirs(outdir, exist_ok=True)
    G.S = 8
    for name in WAIFUS:
        P, info = WAIFUS[name]()
        P = mirror_all(P)
        G.pack(P, 64)
        out = os.path.join(TEXR, name)
        bt = Image.open(os.path.join(out, "body.png")).convert("RGBA")
        ft = Image.open(os.path.join(out, "face.png")).convert("RGBA")
        f512 = Image.new("RGBA", (512, 256), T)
        f512.paste(ft, (0, 0))
        quads = G.quads_for(f512, -4, -8, -4, 8, 8, 8, 0, 0, [((0, 0, 0), (0, 0, 0))], 1)
        tf = G.chains(P, ANCHORS)
        for p in P:
            for b in p.boxes:
                quads += G.quads_for(bt, b.x, b.y, b.z, b.w, b.h, b.d, b.u, b.v, tf[p.name], 1)
        G.draw(quads, os.path.join(outdir, name + "_preview.png"), 12.5, 140,
               [(math.pi, 0.05), (math.pi / 2, 0.05), (math.pi - 0.7, 0.1), (0.0, 0.05)], (270, 500))


if __name__ == "__main__":
    main()
    if len(sys.argv) > 2 and sys.argv[1] == "preview":
        preview(sys.argv[2])
