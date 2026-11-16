"""Totally Waifu? 1.2: cuerpos de MALLA SUAVE (no cubos) y caras de anime finas.

Cada parte del cuerpo es un "loft": una serie de secciones redondeadas (elipses o superelipses) a lo largo del hueso,
que se afinan y ensanchan (cuello, torso con busto/cintura/cadera, brazos, piernas, falda, mangas, botas...).
Java (client/WaifuMesh.java) construye exactamente la misma malla con normales suaves, así se ve lisa y redondeada.
Las texturas se pintan en alta resolución con colores limpios (sin ruido de píxeles) y contornos finos entre prendas.

Uso: python3 tools/gen_smooth.py [preview DIR]
"""
import math, os, random, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_tanny as G
import hair_mesh as H
import gen_waifus as W1
from PIL import Image, ImageDraw, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEXR = os.path.join(ROOT, "src/main/resources/assets/totallywaifu/textures/entity")
JAVA_DIR = os.path.join(ROOT, "src/main/java/com/totallywaifu/client")
T = (0, 0, 0, 0)
mix = G.mix
ARM_X, ARM_Y, LEG_X, LEG_Y, HEAD_SCALE = 3.75, 1.3, 1.75, 10.0, 0.86
BONES = {"head": 0, "body": 1, "right_arm": 2, "left_arm": 3, "right_leg": 4, "left_leg": 5}
ANCHORS = [("head", (0, 0, 0)), ("hat", (0, 0, 0)), ("body", (0, 0, 0)), ("right_arm", (-ARM_X, ARM_Y, 0)), ("left_arm", (ARM_X, ARM_Y, 0)),
           ("right_leg", (-LEG_X, LEG_Y, 0)), ("left_leg", (LEG_X, LEG_Y, 0))]
SKIN, SKIN_SH, SKIN_HI = (252, 226, 210), (236, 196, 180), (255, 240, 230)
PXV = 14          # píxeles de textura por unidad de largo


# ================================================================== lofts
class Loft:
    def __init__(self, name, bone, rings, paint, sides=18, p=2.0, cap=(True, True), skirt=False, pxu=96):
        self.name, self.bone, self.rings, self.paint = name, bone, rings, paint
        self.sides, self.p, self.cap, self.skirt, self.pxu = sides, p, cap, skirt, pxu
        self.mirror = False
        self.region = None


def mirrored(l):
    m = Loft(l.name.replace("_r", "_l"), l.bone.replace("right", "left"), l.rings, l.paint, l.sides, l.p, l.cap, l.skirt, l.pxu)
    m.mirror = True
    m.region = "share:" + l.name
    return m


def ring_points(l, ring):
    y, rx, rz, cx, cz = ring
    e = 2.0 / l.p
    pts = []
    for k in range(l.sides + 1):
        a = 2 * math.pi * k / l.sides
        sa, ca = math.sin(a), math.cos(a)
        x = cx + rx * math.copysign(abs(sa) ** e, sa)
        z = cz - rz * math.copysign(abs(ca) ** e, ca)
        pts.append((-x if l.mirror else x, y, z))
    return pts


def smooth_rings(spec, n=3):
    """Interpola anillos con una curva suave (Catmull-Rom) para que la silueta no tenga quiebres."""
    out = []
    for i in range(len(spec) - 1):
        p0, p1, p2, p3 = spec[max(0, i - 1)], spec[i], spec[i + 1], spec[min(len(spec) - 1, i + 2)]
        for s in range(n):
            t = s / n
            out.append(tuple(0.5 * ((2 * p1[j]) + (-p0[j] + p2[j]) * t + (2 * p0[j] - 5 * p1[j] + 4 * p2[j] - p3[j]) * t * t
                                    + (-p0[j] + 3 * p1[j] - 3 * p2[j] + p3[j]) * t ** 3) for j in range(5)))
    out.append(spec[-1])
    return out


# ================================================================== pintura (colores limpios + contornos)
def paint_region(img, x0, y0, w, h, l):
    """Pinta la región de un loft. paint(u, y) -> (rgb, id). u: 0 frente, .25 izquierda, .5 espalda, .75 derecha."""
    ya, yb = l.rings[0][0], l.rings[-1][0]
    ids = [[0] * w for _ in range(h)]
    cols = [[None] * w for _ in range(h)]
    for j in range(h):
        y = ya + (j + 0.5) / h * (yb - ya)
        for i in range(w):
            u = (i + 0.5) / w
            c, rid = l.paint(u, y)
            cols[j][i], ids[j][i] = c, rid
    for j in range(h):
        for i in range(w):
            c = cols[j][i]
            rid = ids[j][i]
            edge = False
            for di, dj in ((1, 0), (0, 1), (-1, 0), (0, -1)):
                ii, jj = (i + di) % w, j + dj
                if 0 <= jj < h and ids[jj][ii] > rid:
                    edge = True
            if edge:
                c = tuple(int(v * 0.62) for v in c)
            img.putpixel((x0 + i, y0 + j), c + (255,))


def F(u):
    """Cuánto mira al frente (1 frente, -1 espalda)."""
    return math.cos(2 * math.pi * u)


def S(u):
    """Lado: + izquierda de ella, - derecha."""
    return math.sin(2 * math.pi * u)


def fold(c, u, n=10, a=0.05):
    k = 1 - a * (0.5 + 0.5 * math.cos(u * 2 * math.pi * n))
    return tuple(int(v * k) for v in c)


def shade_y(c, y, ya, yb, a=0.1):
    t = (y - ya) / max(0.01, yb - ya)
    return tuple(int(v * (1.04 - a * t)) for v in c)


# ================================================================== cuerpo base (proporciones)
def torso_rings():
    # hombros y espalda estrechos, busto, cintura de avispa y cadera ancha (figura de reloj de arena)
    return smooth_rings([(-0.3, 2.2, 1.3, 0, 0.05), (0.5, 2.85, 1.5, 0, 0.02), (1.9, 2.85, 1.85, 0, -0.25), (3.0, 2.8, 2.15, 0, -0.5),
                         (4.2, 2.5, 1.65, 0, -0.15), (5.6, 2.2, 1.38, 0, 0), (7.1, 2.85, 1.62, 0, 0.05), (8.8, 3.4, 1.9, 0, 0.12),
                         (10.2, 3.25, 1.8, 0, 0.06), (11.0, 2.2, 1.35, 0, 0.05)], 3)


def arm_rings():
    # brazos finos y largos
    return smooth_rings([(-1.6, 0.3, 0.3, 0, 0), (-1.2, 0.95, 0.95, 0, 0), (-0.2, 1.12, 1.12, 0, 0), (1.4, 0.98, 0.98, 0, 0),
                         (4.9, 0.82, 0.85, 0, 0), (5.7, 0.78, 0.8, 0, 0), (6.9, 0.85, 0.85, 0, 0), (9.4, 0.62, 0.58, 0, 0),
                         (10.1, 0.76, 0.52, 0, 0), (11.3, 0.7, 0.48, 0, -0.05), (11.9, 0.26, 0.26, 0, -0.05)], 2)


def leg_rings():
    # piernas largas: muslo lleno, rodilla fina, pantorrilla marcada, tobillo delgado
    return smooth_rings([(-0.9, 1.7, 1.75, 0.05, 0), (0.6, 1.92, 1.92, 0.0, 0), (3.6, 1.62, 1.65, 0, 0), (6.3, 1.12, 1.18, 0, -0.05),
                         (7.9, 1.25, 1.32, 0, 0.15), (11.0, 0.82, 0.86, 0, 0.05), (12.5, 0.72, 0.76, 0, 0),
                         (13.0, 0.82, 1.18, 0, -0.45), (13.6, 0.88, 1.5, 0, -0.7), (14.0, 0.55, 1.15, 0, -0.65)], 2)


def neck_rings():
    return [(-2.2, 0.82, 0.86, 0, 0.1), (-0.6, 0.86, 0.9, 0, 0.1), (0.4, 1.1, 1.02, 0, 0.1)]


def base_body(torso, arm, leg, neck=None):
    L = [Loft("neck", "body", neck_rings(), neck or (lambda u, y: (SKIN, 0)), sides=14, cap=(False, False), pxu=48),
         Loft("torso", "body", torso_rings(), torso, sides=24, pxu=160),
         Loft("arm_r", "right_arm", arm_rings(), arm, sides=14, pxu=64),
         Loft("leg_r", "right_leg", leg_rings(), leg, sides=16, pxu=80)]
    return L


def skirt_rings(y0, y1, top, bottom, depth_k=0.72):
    rx0, rx1 = top, bottom
    out = []
    for i in range(5):
        t = i / 4
        y = y0 + (y1 - y0) * t
        r = rx0 + (rx1 - rx0) * (t ** 0.8)
        out.append((y, r, r * depth_k, 0, 0.05))
    return out


def tube(name, bone, ys, rs, paint, sides=16, cap=(False, False), skirt=False, pxu=96, dz=0.0):
    return Loft(name, bone, [(y, r[0], r[1], 0, dz) for y, r in zip(ys, rs)], paint, sides=sides, cap=cap, skirt=skirt, pxu=pxu)


def skin_arm(glove=None, glove_from=8.6, band=None):
    def fn(u, y):
        if band and band[0] <= y <= band[1]:
            return band[2], 2
        if glove and y >= glove_from:
            return shade_y(glove, y, glove_from, 11.3), 3
        return shade_y(SKIN, y, -1.7, 11.3, 0.06), 0
    return fn


def legwear(stock, top_y, garter=None, garter_h=0.6):
    def fn(u, y):
        if garter and top_y - garter_h <= y < top_y:
            return garter, 2
        if y >= top_y:
            c = fold(stock, u, 6, 0.04)
            return shade_y(c, y, top_y, 13.4, 0.12), 3
        return shade_y(SKIN, y, -0.9, top_y, 0.05), 0
    return fn


def boots(c, hi, sole, top_y, lace=None, cuff=None, toe=None):
    def fn(u, y):
        if y > 13.6:
            return sole, 6
        if cuff and y < top_y + 0.6:
            return cuff, 4
        if toe and y > 12.5 and F(u) > 0.3:
            return toe, 5
        if lace and F(u) > 0.82 and int((y - top_y) * 2.2) % 2 == 0 and y > top_y + 0.6:
            return lace, 7
        return mix(c, hi, 0.5 + 0.5 * S(u) * 0.4), 3
    return fn


def boot_loft(paint, top_y, extra=0.14):
    rings = [r for r in leg_rings() if r[0] >= top_y - 0.01]
    rings = [(top_y,) + tuple(leg_at(top_y)[1:])] + [r for r in rings if r[0] > top_y]
    rings = [(y, rx + extra, rz + extra, cx, cz) for y, rx, rz, cx, cz in rings]
    return Loft("boot_r", "right_leg", rings, paint, sides=16, cap=(False, True), pxu=80)


def leg_at(y):
    R = leg_rings()
    for a, b in zip(R, R[1:]):
        if a[0] <= y <= b[0]:
            t = (y - a[0]) / (b[0] - a[0])
            return tuple(a[j] + (b[j] - a[j]) * t for j in range(5))
    return R[-1]


def arm_at(y):
    R = arm_rings()
    for a, b in zip(R, R[1:]):
        if a[0] <= y <= b[0]:
            t = (y - a[0]) / (b[0] - a[0])
            return tuple(a[j] + (b[j] - a[j]) * t for j in range(5))
    return R[-1]


def torso_at(y):
    R = torso_rings()
    for a, b in zip(R, R[1:]):
        if a[0] <= y <= b[0]:
            t = (y - a[0]) / (b[0] - a[0])
            return tuple(a[j] + (b[j] - a[j]) * t for j in range(5))
    return R[-1]


def band_loft(name, bone, at, y0, y1, paint, extra=0.12, sides=20, pxu=96, p=2.0):
    """Anillo que abraza al cuerpo (cinturón, cuello, puño, liga...)."""
    a, b = at(y0), at(y1)
    return Loft(name, bone, [(y0, a[1] + extra, a[2] + extra, a[3], a[4]), (y1, b[1] + extra, b[2] + extra, b[3], b[4])], paint,
                sides=sides, cap=(False, False), pxu=pxu, p=p)


def puff_sleeve(paint, cuff_c):
    rings = smooth_rings([(-1.75, 0.35, 0.35, 0, 0), (-1.35, 1.35, 1.35, 0, 0), (0.2, 1.6, 1.6, 0, 0), (1.6, 1.4, 1.4, 0, 0),
                          (2.3, 1.12, 1.12, 0, 0)], 2)
    def fn(u, y):
        if y > 1.9:
            return cuff_c, 4
        return paint(u, y)
    return Loft("puff_r", "right_arm", rings, fn, sides=16, cap=(True, False), pxu=64)


# ================================================================== cabeza redondeada (cara plana al frente)
def head_rings():
    out = []
    n = 12
    for i in range(n + 1):
        s = -1 + 2 * i / n
        f = max(0.0, 1 - abs(s) ** 3.2) ** (1 / 3.2)
        rx = 4.05 * f * (1 - 0.18 * max(0, s - 0.25) / 0.75)         # mandíbula un poco más fina
        rz = 4.05 * f
        out.append((-4 + 4 * s, rx, rz, 0, -0.05 * max(0, s)))
    return out


HEAD = Loft("head", "head", head_rings(), None, sides=24, p=3.0, cap=(True, True))


# ================================================================== cara estilo skin detallada (32x32 → 128 px)
# Ojo izquierdo (el borde de afuera a la izquierda). L pestaña, S sombra de párpado, W blanco, U iris oscuro, I iris,
# J iris claro, P pupila, H brillo, l pestaña inferior suave, D borde exterior del ojo.
EYES = {
    "normal": ["LSSSSSSSS..",
               ".LLLLLLLLL.",
               "LLWUUUUUWL.",
               ".DWIIPPHIW.",
               "..WJIPPIJl.",
               "...lJJJJl..",
               "....llll..."],
    "focused": ["...........",
                "LSSSSSSSSS.",
                ".LLLLLLLLLL",
                "LLWUUPPUUWL",
                "..WJIPPHJl.",
                "...lllllll."],
    "tired": ["...........",
              "...........",
              "LSSSSSSSSS.",
              ".LLLLLLLLLL",
              "LDWIIPPIIWl",
              "..WJJJJJJl.",
              "...llllll.."],
    "surprised": ["LSSSSSSSS..",
                  ".LLLLLLLLL.",
                  "LLWWUUUWWL.",
                  ".DWUIPPUW..",
                  "..WIIPHIW..",
                  "..WJIIIJW..",
                  "...lJJJl...",
                  "....lll...."],
    "blink": ["...........",
              "...........",
              "...SSSSS...",
              "L.SSSSSSS..",
              ".LLLLLLLLL.",
              "..l.l.l.l.."],
    "happy": ["...........",
              "...........",
              "...LLLLL...",
              ".LL.....LL.",
              "L..........",
              "..........."],
}
BROWS = {"normal": [".BBBBBB...", "B.....BB.."], "focused": ["...BBBBBB.", ".BBB......"], "tired": ["BBBBB.....", ".....BBB.."],
         "surprised": ["..BBBBB...", ".B.....B.."], "blink": [".BBBBBB...", "B.....BB.."], "happy": [".BBBBBB...", "B.....BB.."]}
MOUTHS = {"normal": [".mMMMMm.", "..pPPp.."], "focused": [".mMMMMM.", "........"], "tired": ["..mMMm..", "..pPPp.."],
          "surprised": ["..mMMm..", "..MooM..", "..mMMm.."], "blink": [".mMMMMm.", "..pPPp.."],
          "happy": ["m......m", ".mMMMMm.", "..pPPp.."]}


def make_face(iris, lash, brow, hair, lip=(204, 108, 116), blush=(240, 168, 168), shadow=(176, 122, 120), sharp=False):
    dk, mid, hi = iris
    N = 32

    def face(expr):
        g = [[SKIN for _ in range(N)] for _ in range(N)]
        # sombreado de la piel: frente bajo el cabello, mejillas, mandíbula y cuello
        for y in range(N):
            for x in range(N):
                c = SKIN
                if y < 9:
                    c = mix(SKIN_SH, SKIN, y / 9)
                side = min(x, N - 1 - x)
                if side < 3:
                    c = mix(c, SKIN_SH, (3 - side) / 4)
                if y > 27:
                    c = mix(c, SKIN_SH, (y - 27) / 5)
                if y > 22 and side < 7:
                    c = mix(c, SKIN_SH, 0.25 * (7 - side) / 7)
                g[y][x] = c
        col = {"L": lash, "S": mix(SKIN, shadow, 0.55), "W": (246, 244, 246), "U": dk, "I": mid, "J": hi,
               "P": mix(dk, (0, 0, 0), 0.55), "H": (255, 255, 255), "l": mix(lash, SKIN, 0.45), "D": mix(lash, SKIN, 0.2),
               "B": brow, "m": mix(lip, SKIN, 0.5), "M": lip, "p": mix(lip, (255, 200, 200), 0.35), "P_": lip, "o": (110, 40, 56)}

        def stamp(rows, x0, y0, flip, special=None):
            for j, row in enumerate(rows):
                for i, ch in enumerate(row):
                    if ch == ".":
                        continue
                    x = x0 + ((len(row) - 1 - i) if flip else i)
                    c = col["P_"] if (ch == "P" and special == "mouth") else col[ch]
                    if 0 <= x < N and 0 <= y0 + j < N:
                        g[y0 + j][x] = c
        eye = EYES[expr]
        ey = 16 - (1 if expr == "surprised" else 0)
        stamp(BROWS[expr], 4, 12 - (1 if expr == "surprised" else 0), False)
        stamp(BROWS[expr], 18, 12 - (1 if expr == "surprised" else 0), True)
        stamp(eye, 3, ey - 1, False)
        stamp(eye, 18, ey - 1, True)
        # rubor
        if expr in ("normal", "happy", "blink", "surprised"):
            for (cx, cy) in ((8, 23), (23, 23)):
                for dx in range(-2, 3):
                    for dy in range(0, 2):
                        k = 0.45 if expr == "happy" else 0.28
                        g[cy + dy][cx + dx] = mix(g[cy + dy][cx + dx], blush, k * (1 - abs(dx) / 3))
        # nariz: sombra suave a un lado y punta
        g[21][16] = mix(SKIN, shadow, 0.35)
        g[22][16] = mix(SKIN, shadow, 0.55)
        g[22][15] = mix(SKIN, shadow, 0.25)
        # boca
        m = MOUTHS[expr]
        stamp([r.replace("P", "M") if False else r for r in m], 12, 25, False, "mouth")
        img = Image.new("RGBA", (N, N))
        for y in range(N):
            for x in range(N):
                img.putpixel((x, y), g[y][x] + (255,))
        small = img.resize((128, 128), Image.NEAREST)
        tex = Image.new("RGBA", (256, 128), T)
        tex.paste(small, (0, 0))
        tex.paste(Image.new("RGBA", (32, 32), hair[0] + (255,)), (160, 16))
        tex.paste(Image.new("RGBA", (32, 32), SKIN_SH + (255,)), (160, 64))
        return tex
    return face


# ================================================================== KOZUE
KZ = W1.KZ_HAIR
OAK, OAK_HI, OAK_DK = (186, 140, 86), (214, 172, 118), (130, 92, 54)
CREAM, CREAM_SH = (250, 244, 228), (226, 214, 192)
LEAF, LEAF_HI, LEAF_DK = (92, 168, 74), (132, 204, 100), (56, 112, 44)
SOCK = (76, 54, 42)
GOLD, GOLD_HI = (230, 186, 70), (255, 232, 140)


def kozue_parts():
    def torso(u, y):
        f, s = F(u), S(u)
        if y < 2.0 and f > 0.55 and abs(s) < 0.42 - (y + 0.3) * 0.12:
            return (LEAF if (int(u * 60) + int(y * 3)) % 7 else LEAF_HI), 3        # pañuelo al cuello
        strap = abs(abs(s) - 0.42) < 0.07 and y < 3.4
        if y >= 6.7:
            return fold(OAK, u, 12, 0.06), 2                                           # overol
        if (f > 0.3 and abs(s) < 0.5 and y >= 2.9) or strap:
            if f > 0.5 and abs(s) < 0.22 and 4.3 < y < 5.4:
                return OAK_DK, 4                                                       # bolsillo
            if f > 0.6 and abs(abs(s) - 0.38) < 0.05 and abs(y - 3.3) < 0.3:
                return GOLD, 5                                                         # botones
            return fold(OAK, u, 14, 0.05), 2
        return shade_y(fold(CREAM, u, 16, 0.04), y, -0.3, 7, 0.06), 1
    L = base_body(torso, skin_arm(glove=LEAF, glove_from=9.3), legwear(SOCK, 4.1, garter=LEAF, garter_h=0.55))
    L.append(puff_sleeve(lambda u, y: (fold(CREAM, u, 8, 0.07), 1), CREAM_SH))
    L.append(Loft("skirt", "body", skirt_rings(6.8, 11.6, 3.05, 4.5), lambda u, y: (fold(OAK, u, 18, 0.12), 1) if y < 11.3 else (OAK_DK, 2),
                  sides=24, cap=(False, False), skirt=True, pxu=160))
    L.append(band_loft("belt", "body", torso_at, 7.0, 7.8, lambda u, y: ((GOLD, 2) if F(u) > 0.93 else ((96, 62, 36), 1)), extra=0.55))
    L.append(boot_loft(boots((118, 76, 44), (150, 100, 62), (52, 36, 28), 9.6, lace=CREAM, cuff=(96, 62, 36)), 9.6))
    L.append(band_loft("bandana", "head", lambda y: (y, 4.25, 4.25, 0, 0), -6.6, -5.4,
                       lambda u, y: ((LEAF_DK if (int(u * 40) + int(y * 6)) % 5 == 0 else LEAF), 1), extra=0.0, sides=24, p=3.0))
    P = []
    P.append(W1.Part("hammer", "body", (3.45, 7.6, -0.4), (0, 0, -0.18), [W1.Box(-0.28, 0, -0.28, 0.56, 3.6, 0.56, flatp((128, 86, 50))),
                                                                       W1.Box(-0.65, 3.4, -1.1, 1.3, 1.0, 2.2, flatp((170, 176, 186)))]))
    P.append(W1.Part("pouch_r", "body", (-2.5, 7.7, -1.8), (0, 0.35, 0), [W1.Box(-1.0, 0, -0.9, 2.0, 2.0, 1.1, flatp((112, 74, 44)))]))
    P.append(W1.Part("knot", "head", (1.8, -6.1, 4.0), (0.3, 0, 0.3), [W1.Box(-0.8, -0.8, -0.45, 1.6, 1.6, 0.9, flatp(LEAF)),
                                                                      W1.Box(-1.5, 0.3, 0.0, 1.0, 2.4, 0.4, flatp(LEAF_HI)),
                                                                      W1.Box(0.4, 0.3, 0.0, 1.0, 2.1, 0.4, flatp(LEAF_HI))]))
    P.append(W1.Part("pencil", "head", (-4.3, -3.6, -0.2), (0.25, 0, 0.2), [W1.Box(-0.25, -0.25, -2.0, 0.5, 0.5, 4.0, flatp((236, 196, 60)))]))
    return L, P


# ================================================================== HOMURA
HM = W1.HM_HAIR
CHAR, CHAR_HI = (46, 42, 50), (70, 64, 76)
FIRE1, FIRE2, FIRE3 = (255, 222, 100), (250, 144, 42), (218, 64, 30)
LEATHER, LEATHER_DK = (112, 70, 44), (74, 46, 30)
STONE, STONE_HI, STONE_DK = (122, 122, 128), (156, 156, 162), (84, 84, 92)


def homura_parts():
    def torso(u, y):
        f, s = F(u), S(u)
        if y < 1.4 and f > 0.2 and y < 0.4 + (1 - abs(s)) * 1.4:
            return SKIN, 0                                                             # escote cuadrado / clavícula
        if y < 1.6 and abs(y - (0.4 + (1 - abs(s)) * 1.4)) < 0.25 and f > 0.2:
            return FIRE2, 3
        if 4.5 <= y < 7.3:
            if f > 0.75 and abs(s) < 0.12 and int(y * 3) % 2 == 0:
                return FIRE2, 5                                                        # cordones del corsé
            return (LEATHER_DK if f > 0 else (34, 30, 38)), 2
        return shade_y(CHAR, y, 0, 7, 0.08), 1
    L = base_body(torso, skin_arm(glove=LEATHER, glove_from=6.4, band=(6.1, 6.6, (150, 156, 166))),
                  legwear((32, 28, 34), 3.9, garter=FIRE2, garter_h=0.5),
                  neck=lambda u, y: (((40, 30, 34), 1) if y > -0.4 else (SKIN, 0)))
    def skirt(u, y):
        bx = int(u * 40 + (2 if int(y * 2.2) % 2 else 0)) % 4
        c = STONE_DK if bx == 0 or int(y * 2.2 * 4) % 4 == 0 else mix(STONE, STONE_HI, ((int(u * 40) * 7 + int(y * 3) * 3) % 5) / 10)
        edge = 12.0 - 0.8 * abs(math.sin(u * 2 * math.pi * 9))
        if y > edge:
            t = (y - edge) / max(0.1, 12.6 - edge)
            return (FIRE1 if t > 0.7 else FIRE2 if t > 0.35 else FIRE3), 3
        return c, 1
    L.append(Loft("skirt", "body", skirt_rings(6.8, 12.6, 3.05, 4.6), skirt, sides=24, cap=(False, False), skirt=True, pxu=192))
    L.append(boot_loft(boots((36, 32, 38), (66, 62, 72), (50, 50, 56), 9.2, lace=FIRE2, cuff=(110, 112, 122), toe=(120, 124, 134)), 9.2))
    L.append(band_loft("belt", "body", torso_at, 7.1, 7.8, lambda u, y: (LEATHER_DK, 1), extra=0.5))
    P = []
    def apron(f, x, y, w, h):
        cx, cy = w / 2, h * 0.45
        if f == "front" and ((x - cx) ** 2 / (w * 0.13) ** 2 + (y - cy) ** 2 / (h * 0.2) ** 2) < 1:
            return FIRE2
        return LEATHER
    P.append(W1.Part("apron", "body", (0, 7.4, -2.0), (-0.3, 0, 0), [W1.Box(-2.4, 0, -0.3, 4.8, 4.8, 0.3, apron)]))
    P.append(W1.Part("ember", "body", (0, -0.7, -1.05), (0, 0, 0.78), [W1.Box(-0.4, -0.4, -0.3, 0.8, 0.8, 0.45, W1.gem(FIRE3, FIRE1))]))
    P.append(W1.Part("goggles", "head", (0, -7.6, -2.6), (-0.4, 0, 0), [W1.Box(-4.3, -0.4, -0.3, 8.6, 0.8, 0.6, flatp((40, 32, 30)))]))
    P.append(W1.Part("lens_r", "goggles", (-1.7, 0, -0.5), (0, 0.1, 0), [W1.Box(-1.2, -1.0, -0.6, 2.4, 2.0, 1.2,
              lambda f, x, y, w, h: (255, 220, 160) if f == "front" and x < w * 0.35 and y < h * 0.35 else (226, 120, 40) if f == "front" else (110, 90, 60))]))
    flame = lambda f, x, y, w, h: FIRE1 if y > h * 0.6 else FIRE2 if y > h * 0.3 else FIRE3
    P.append(W1.Part("clip_r", "head", (-3.3, -6.3, -2.5), (0, 0, -0.35), [W1.Box(-0.7, -1.7, -0.35, 1.4, 2.2, 0.7, flame)]))
    P.append(W1.Part("tie_r", "head", (-3.2, -6.2, 3.2), (0, 0, 0), [W1.Box(-0.9, -0.9, -0.9, 1.8, 1.8, 1.8, flatp(FIRE3))]))
    return L, P


# ================================================================== KURA
KR = W1.KR_HAIR
CHEST, CHEST_HI, CHEST_DK = (166, 112, 54), (198, 144, 76), (106, 70, 32)
WHITE, WHITE_SH = (252, 250, 247), (226, 222, 222)


def kura_parts():
    def torso(u, y):
        f, s = F(u), S(u)
        if y < 0.7:
            return WHITE, 3                                                            # cuello blanco
        if 6.1 <= y < 7.2:
            return WHITE, 4                                                            # fajín
        if f > 0.2 and abs(s) < 0.42 - max(0, y - 4) * 0.04 and 1.0 < y < 6.1:
            frill = abs(abs(s) - (0.42 - max(0, y - 4) * 0.04)) < 0.05
            return (WHITE_SH if frill else WHITE), 2                                   # pechera blanca
        return shade_y(fold(CHEST, u, 18, 0.07), y, 0, 7, 0.08), 1
    L = base_body(torso, skin_arm(band=(9.0, 9.6, WHITE)), legwear(WHITE, 4.0, garter=CHEST_DK, garter_h=0.55))
    L.append(puff_sleeve(lambda u, y: (fold(CHEST, u, 8, 0.1), 1), WHITE))
    def skirt(u, y):
        if y > 11.6:
            return (WHITE if (int(u * 64) % 2 == 0 or y < 11.85) else WHITE_SH), 2
        return fold(CHEST, u, 20, 0.14), 1
    L.append(Loft("petticoat", "body", skirt_rings(8.0, 12.2, 3.4, 4.8), lambda u, y: ((WHITE if int(u * 80) % 2 else WHITE_SH), 1),
                  sides=24, cap=(False, False), skirt=True, pxu=128))
    L.append(Loft("skirt", "body", skirt_rings(6.8, 12.0, 3.05, 5.0), skirt, sides=24, cap=(False, False), skirt=True, pxu=192))
    L.append(boot_loft(boots((74, 44, 28), (112, 72, 48), (40, 26, 20), 11.8, cuff=(74, 44, 28)), 11.8))
    L.append(band_loft("headband", "head", lambda y: (y, 4.25, 4.25, 0, 0), -7.6, -7.0, lambda u, y: (CHEST_DK, 1), extra=0.0, sides=24, p=3.0))
    P = []
    P.append(W1.Part("latch", "body", (0, 1.4, -2.45), (0.15, 0, 0), [W1.Box(-0.75, -0.4, -0.35, 1.5, 1.8, 0.5, flatp(GOLD)),
                                                                     W1.Box(-0.28, 0.25, -0.5, 0.56, 0.56, 0.2, flatp((60, 40, 20)))]))
    P += [p for p in W1.bow("back_bow", "body", (0, 6.6, 1.65), (0, 0, 0), flatp(WHITE), 1.3)]
    P.append(W1.Part("apron", "body", (0, 7.2, -2.05), (-0.32, 0, 0), [W1.Box(-2.4, 0, -0.3, 4.8, 4.4, 0.3, flatp(WHITE)),
                                                                       W1.Box(-2.6, 4.3, -0.35, 5.2, 0.7, 0.4, W1.frill(WHITE, WHITE_SH))]))
    def chestbox(f, x, y, w, h):
        if y < 6 or abs(y - h * 0.38) < 3:
            return CHEST_DK
        if f == "back" and abs(x - w / 2) < 5 and abs(y - h * 0.45) < 7:
            return GOLD
        return CHEST if (x // 6) % 2 else CHEST_HI
    P.append(W1.Part("backpack", "body", (0, 0.9, 1.6), (0.06, 0, 0), [W1.Box(-2.6, 0, 0, 5.2, 4.8, 2.5, chestbox)]))
    P.append(W1.Part("headdress", "head", (0, -7.6, -1.5), (-0.45, 0, 0), [W1.Box(-3.4, -1.5, -0.4, 6.8, 1.7, 0.8, W1.frill(WHITE, WHITE_SH))]))
    P += W1.bow("hair_bow", "head", (3.2, -7.0, 1.6), (0, -0.6, 0), flatp(CHEST_DK), 0.8)
    return L, P


# ================================================================== SHIORI
SH = W1.SH_HAIR
OBS, OBS_HI, OBS_DK = (42, 32, 62), (74, 56, 108), (24, 18, 38)
DIA, DIA_HI, DIA_DK = (96, 224, 218), (204, 255, 250), (52, 152, 152)
RED, RED_DK = (182, 42, 56), (124, 26, 40)


def shiori_parts():
    def torso(u, y):
        f, s = F(u), S(u)
        if f > 0.8 and abs(s) < 0.05:
            return DIA, 4                                                              # línea de diamante al frente
        if 6.3 <= y < 7.0:
            return RED, 3                                                              # fajín rojo
        if 4.4 <= y < 6.3:
            if f > 0.5 and (abs(y - 4.6) < 0.12 or abs(y - 6.1) < 0.12):
                return DIA, 5
            return OBS_DK, 2                                                           # corsé
        return shade_y(fold(OBS, u, 16, 0.08), y, 0, 7, 0.08), 1
    L = base_body(torso, lambda u, y: ((SKIN, 0) if y > 9.5 else (fold(OBS, u, 8, 0.08), 1)),
                  legwear((30, 24, 40), 0.2), neck=lambda u, y: ((DIA_DK, 2) if y > -1.0 else (SKIN, 0)))
    def skirt(u, y):
        if y > 14.0 and int(u * 48) % 3 != 0:
            return DIA, 2
        if 13.6 < y <= 14.0:
            return DIA_DK, 3
        return fold(OBS, u, 18, 0.12), 1
    L.append(Loft("skirt", "body", skirt_rings(6.8, 14.6, 3.05, 4.5, 0.78), skirt, sides=24, cap=(False, False), skirt=True, pxu=192))
    L.append(Loft("bell_r", "right_arm", [(5.2, 0.92, 0.92, 0, 0), (7.4, 1.25, 1.25, 0, 0), (9.2, 1.8, 1.8, 0, 0.05)],
                  lambda u, y: ((DIA, 2) if y > 8.8 else (fold(OBS_HI, u, 8, 0.1), 1)), sides=16, cap=(False, False), pxu=64))
    L.append(boot_loft(boots(OBS, OBS_HI, (20, 16, 28), 9.0, lace=DIA, cuff=DIA_DK), 9.0))
    L.append(band_loft("circlet", "head", lambda y: (y, 4.25, 4.25, 0, 0), -7.0, -6.5, lambda u, y: (GOLD, 1), extra=0.0, sides=24, p=3.0))
    P = []
    P.append(W1.Part("brooch", "body", (0, 0.7, -1.85), (0, 0, 0.78), [W1.Box(-0.65, -0.65, -0.35, 1.3, 1.3, 0.55, W1.gem(DIA, DIA_HI))]))
    P.append(W1.Part("cape", "body", (0, -0.4, 1.7), (0.1, 0, 0), [W1.Box(-3.5, 0, 0, 7.0, 15.4, 0.4,
              lambda f, x, y, w, h: RED_DK if y >= h - 6 else RED)]))
    P.append(W1.Part("tiara_gem", "head", (0, -6.9, -4.35), (0, 0, 0.78), [W1.Box(-0.8, -0.8, -0.35, 1.6, 1.6, 0.6, W1.gem(DIA, DIA_HI))]))
    P.append(W1.Part("earring_r", "head", (-4.25, -2.6, -0.6), (0, 0, 0), [W1.Box(-0.25, 0, -0.25, 0.5, 1.3, 0.5, W1.gem(DIA, DIA_HI))]))
    return L, P


def flatp(c):
    """Pintor limpio para accesorios (cubos pequeños): sin ruido, con un borde apenas más oscuro."""
    dk = tuple(int(v * 0.72) for v in c)
    lt = tuple(min(255, int(v * 1.08)) for v in c)

    def fn(f, x, y, w, h):
        if f == "top":
            return lt
        if f == "bottom":
            return dk
        if x == 0 or x == w - 1 or y == h - 1:
            return dk
        return c
    return fn


def enrich(L, seed):
    """Cabello más rico: cada mechón con su propio tono y mechones finos extra entre ellos (capas y volumen)."""
    rnd = random.Random(seed)
    out = []
    for lk in L:
        lk = list(lk)
        kind = int(lk[10])
        lk[9] = round(rnd.uniform(0.86, 1.1), 3)
        out.append(lk)
        if lk[3] > 1.5 and kind in (H.KIND_LONG, H.KIND_BOB, H.KIND_BANG, H.KIND_SOFTBANG):
            for sgn in (-1, 1):
                if kind in (H.KIND_BANG, H.KIND_SOFTBANG) and sgn > 0:
                    continue
                t = list(lk)
                t[0] = lk[0] + sgn * rnd.uniform(0.05, 0.09)
                t[3] = lk[3] * rnd.uniform(0.82, 1.04)
                t[4] = lk[4] + rnd.uniform(0.0, 0.08)
                t[6] = lk[6] * 0.55
                t[7] = lk[7] * 0.85
                t[8] = lk[8] + rnd.uniform(0.05, 0.2)
                t[9] = round(rnd.uniform(0.8, 1.16), 3)
                out.append(t)
    return out


WAIFUS = {
    "kozue": dict(hair=KZ, parts=kozue_parts, locks=lambda: enrich(W1.kozue_locks(), 1),
                  face=make_face(((30, 90, 40), (84, 170, 76), (190, 240, 150)), (54, 30, 22), (110, 66, 38), KZ)),
    "homura": dict(hair=HM, parts=homura_parts, locks=lambda: enrich(W1.homura_locks(), 2),
                   face=make_face(((140, 46, 10), (236, 140, 32), (255, 224, 128)), (64, 18, 16), (160, 44, 28), HM, sharp=True)),
    "kura": dict(hair=KR, parts=kura_parts, locks=lambda: enrich(W1.kura_locks(), 3),
                 face=make_face(((24, 60, 150), (72, 134, 228), (176, 220, 255)), (104, 70, 38), (196, 156, 86), KR)),
    "shiori": dict(hair=SH, parts=shiori_parts, locks=lambda: enrich(W1.shiori_locks(), 4),
                   face=make_face(((18, 98, 112), (58, 202, 202), (206, 255, 250)), (34, 20, 66), (64, 44, 128), SH, blush=(236, 160, 190))),
}
EXPRS = [("normal", ""), ("blink", "_blink"), ("happy", "_happy"), ("surprised", "_surprised"), ("focused", "_focused"), ("tired", "_tired")]


# ================================================================== atlas y salida
def finish_lofts(L):
    out = []
    for l in L:
        out.append(l)
        if l.bone in ("right_arm", "right_leg"):
            out.append(mirrored(l))
    return out


def pack_lofts(L, width=512):
    x = y = shelf = 0
    regions = {}
    for l in L:
        if l.region and l.region.startswith("share:"):
            continue
        w = l.pxu
        h = max(16, int(math.ceil((l.rings[-1][0] - l.rings[0][0]) * PXV)))
        if x + w > width:
            x, y, shelf = 0, y + shelf, 0
        regions[l.name] = (x, y, w, h)
        l.region = (x, y, w, h)
        x += w + 2
        shelf = max(shelf, h + 2)
    for l in L:
        if isinstance(l.region, str):
            l.region = regions[l.region[6:]]
    H_ = 64
    while H_ < y + shelf:
        H_ *= 2
    return width, H_


def lofts_java(name, L, tw, th):
    rows = []
    for l in L:
        x, y, w, h = l.region
        ya, yb = l.rings[0][0], l.rings[-1][0]
        head = [BONES[l.bone], l.sides, l.p, 0, (x + 0.5) / tw, (y + 0.5) / th, (x + w - 0.5) / tw, (y + h - 0.5) / th,
                1 if l.cap[0] else 0, 1 if l.cap[1] else 0, 1 if l.mirror else 0, 1 if l.skirt else 0, len(l.rings)]
        for r in l.rings:
            head += list(r) + [(r[0] - ya) / max(0.001, yb - ya)]
        rows.append("            {" + ", ".join(H.fjava(v) for v in head) + "},")
    return ["    public static final float[][] %s = {" % name] + rows + ["    };", ""]


def head_java():
    l = HEAD
    head = [0, l.sides, l.p, 1, 0, 0, 0.5, 1, 1, 1, 0, 0, len(l.rings)]
    for r in l.rings:
        head += list(r) + [0]
    return ["    public static final float[][] HEAD = {", "            {" + ", ".join(H.fjava(v) for v in head) + "},", "    };", ""]


def build(name):
    d = WAIFUS[name]
    out = os.path.join(TEXR, name)
    os.makedirs(out, exist_ok=True)
    for f in os.listdir(out):
        os.remove(os.path.join(out, f))
    for expr, suf in EXPRS:
        d["face"](expr).save(os.path.join(out, "face" + suf + ".png"))
    L, P = d["parts"]()
    L = finish_lofts(L)
    tw, th = pack_lofts(L)
    img = Image.new("RGBA", (tw, th), T)
    for l in L:
        if not l.mirror:
            paint_region(img, *l.region, l)
    img.save(os.path.join(out, "body.png"))
    P = W1.finish(P)
    G.AO = False
    G.S = 8
    G.jit = lambda c, a=4: c
    pw, ph = G.pack(P, 32) if P else (32, 32)
    G.build_texture(P, pw, ph).save(os.path.join(out, "props.png"))
    G.strand_texture().save(os.path.join(out, "hair_strand.png"))
    return L, P, (tw, th), (pw, ph)


def main():
    java = ["// GENERADO por tools/gen_smooth.py — no editar a mano.", "package com.totallywaifu.client;", "",
            "import net.minecraft.client.model.geom.ModelPart;", "import net.minecraft.client.model.geom.PartPose;",
            "import net.minecraft.client.model.geom.builders.*;", "", "import java.util.LinkedHashMap;", "import java.util.Map;", "",
            "/** Accesorios (cubos pequeños) y huesos; el cuerpo liso está en WaifuShapes. */",
            "public final class WaifuModels {", "    private WaifuModels() {}", "",
            "    public static final float ARM_X = %s, ARM_Y = %s, LEG_X = %s, LEG_Y = %s, HEAD_SCALE = %s;" % tuple(G.fnum(v) for v in (ARM_X, ARM_Y, LEG_X, LEG_Y, HEAD_SCALE)), ""]
    shapes = ["// GENERADO por tools/gen_smooth.py — no editar a mano.", "package com.totallywaifu.client;", "",
              "/** Cuerpos de malla suave: por loft {hueso, lados, p, uvModo, u0, v0, u1, v1, tapaArriba, tapaAbajo, espejo, falda, nAnillos,",
              " *  luego por anillo: y, rx, rz, cx, cz, v}. */",
              "public final class WaifuShapes {", "    private WaifuShapes() {}", ""]
    shapes += head_java()
    hair = ["// GENERADO por tools/gen_smooth.py — no editar a mano.", "package com.totallywaifu.client;", "",
            "/** Mechones de cada waifu: phi, th0, th1, caída, vuelo, curva, ancho, grosor, separación, tono, tipo. */",
            "public final class WaifuHair {", "    private WaifuHair() {}", "",
            "    public static final float CY = %s, R = %s, P = %s;" % (H.fjava(H.CY), H.fjava(H.R), H.fjava(H.P)),
            "    public static final int SCALP_STEPS = %d, HANG_STEPS = %d, SIDES = %d;" % (H.SCALP_STEPS, H.HANG_STEPS, H.SIDES),
            "    public static final float VOX_STEP = %s, WAVE_AMP = %s, WAVE_FREQ = %s;" % (H.fjava(H.VOX_STEP), H.fjava(H.WAVE_AMP), H.fjava(H.WAVE_FREQ)),
            "    public static final int[] PALETTE = {" + ", ".join("0x%02X%02X%02X" % c for c in H.PALETTE) + "};", ""]
    for name, d in WAIFUS.items():
        L, P, (tw, th), (pw, ph) = build(name)
        cap = name.capitalize()
        java += G.java_layer("body" + cap, P, ANCHORS, pw, ph) + [""]
        java += G.java_bake("bakeBody" + cap, P, ANCHORS) + [""]
        shapes += lofts_java(name.upper(), L, tw, th)
        hair.append("    public static final int COLOR_%s = 0x%02X%02X%02X;" % ((name.upper(),) + d["hair"][0]))
        hair.append("    public static final float[][] %s = {" % name.upper())
        for lk in d["locks"]():
            hair.append("            {" + ", ".join(H.fjava(v) for v in lk) + "},")
        hair += ["    };", ""]
        print("%s: %d lofts (atlas %dx%d) · %d accesorios" % (name, len(L), tw, th, len(P)))
    java += ["}", ""]
    shapes += ["}", ""]
    hair += ["}", ""]
    open(os.path.join(JAVA_DIR, "WaifuModels.java"), "w").write("\n".join(java))
    open(os.path.join(JAVA_DIR, "WaifuShapes.java"), "w").write("\n".join(shapes))
    open(os.path.join(JAVA_DIR, "WaifuHair.java"), "w").write("\n".join(hair))


# ================================================================== vista previa (misma geometría que Java)
def loft_quads(l, tex, uv_rect, chain, scale=1.0, planar=False):
    grid = [ring_points(l, r) for r in l.rings]
    n = len(grid)
    x0, y0, x1, y1 = uv_rect
    ya, yb = l.rings[0][0], l.rings[-1][0]
    quads = []
    Ld = H.norm((0.35, -0.8, -0.6))
    for i in range(n - 1):
        for k in range(l.sides):
            q = [grid[i][k], grid[i + 1][k], grid[i + 1][k + 1], grid[i][k + 1]]
            qw = [G.apply_chain(chain, tuple(c * scale for c in p)) for p in q]
            a, b, c = qw[0], qw[1], qw[3]
            nrm = H.norm(((b[1] - a[1]) * (c[2] - a[2]) - (b[2] - a[2]) * (c[1] - a[1]),
                          (b[2] - a[2]) * (c[0] - a[0]) - (b[0] - a[0]) * (c[2] - a[2]),
                          (b[0] - a[0]) * (c[1] - a[1]) - (b[1] - a[1]) * (c[0] - a[0])))
            mx = sum(p[0] for p in q) / 4
            mz = sum(p[2] for p in q) / 4
            cc = (mx - l.rings[i][3], 0, mz - l.rings[i][4])
            if nrm[0] * cc[0] + nrm[2] * cc[2] < 0:
                nrm = tuple(-v for v in nrm)
            if planar:
                avgz = sum(p[2] for p in q) / 4
                my = sum(p[1] for p in q) / 4
                lum = 0.55 + 0.5 * max(0.0, -(nrm[0] * Ld[0] + nrm[1] * Ld[1] + nrm[2] * Ld[2]))
                if avgz < -1.6 and my < -0.6:
                    n_ = 8
                    for a_ in range(n_):
                        for b_ in range(n_):
                            def P_(s_, t_):
                                top = [q[0][j] + (q[3][j] - q[0][j]) * s_ for j in range(3)]
                                bot = [q[1][j] + (q[2][j] - q[1][j]) * s_ for j in range(3)]
                                return tuple(top[j] + (bot[j] - top[j]) * t_ for j in range(3))
                            sq = [P_(a_ / n_, b_ / n_), P_(a_ / n_, (b_ + 1) / n_), P_((a_ + 1) / n_, (b_ + 1) / n_), P_((a_ + 1) / n_, b_ / n_)]
                            cxy = P_((a_ + 0.5) / n_, (b_ + 0.5) / n_)
                            col = tex.getpixel((max(0, min(127, int((cxy[0] + 4) / 8 * 128))), max(0, min(127, int((cxy[1] + 8) / 8 * 128)))))
                            quads.append(([G.apply_chain(chain, tuple(c * scale for c in pp)) for pp in sq],
                                          tuple(int(min(255, ch * lum)) for ch in col[:3])))
                    continue
                col = tex.getpixel((176, 32))
            else:
                u = (k + 0.5) / l.sides
                v = ((l.rings[i][0] + l.rings[i + 1][0]) / 2 - ya) / max(0.01, yb - ya)
                col = tex.getpixel((int(x0 + u * (x1 - x0)), int(y0 + v * (y1 - y0))))
            lum = 0.55 + 0.5 * max(0.0, -(nrm[0] * Ld[0] + nrm[1] * Ld[1] + nrm[2] * Ld[2]))
            quads.append((qw, tuple(int(min(255, ch * lum)) for ch in col[:3])))
    return quads


def preview(outdir, expr=""):
    os.makedirs(outdir, exist_ok=True)
    tf_anchor = {a: [(piv, (0, 0, 0))] for a, piv in ANCHORS}
    for name, d in WAIFUS.items():
        out = os.path.join(TEXR, name)
        bt = Image.open(os.path.join(out, "body.png")).convert("RGBA")
        ft = Image.open(os.path.join(out, "face%s.png" % expr)).convert("RGBA")
        pt = Image.open(os.path.join(out, "props.png")).convert("RGBA")
        L, P = d["parts"]()
        L = finish_lofts(L)
        pack_lofts(L)
        quads = []
        quads += loft_quads(HEAD, ft, (0, 0, 0, 0), tf_anchor["head"], HEAD_SCALE, planar=True)
        for l in L:
            x, y, w, h = l.region
            sc = HEAD_SCALE if l.bone == "head" else 1.0
            quads += loft_quads(l, bt, (x, y, x + w, y + h), tf_anchor[l.bone], sc)
        P = W1.finish(P)
        G.pack(P, 32)
        tf = G.chains(P, ANCHORS)
        scaled = Image.new("RGBA", (512, max(512, pt.height)), T)
        scaled.paste(pt, (0, 0))
        for p in P:
            root = p.name
            while root not in dict(ANCHORS):
                root = next(x.parent for x in P if x.name == root)
            for b in p.boxes:
                qs = G.quads_for(scaled, b.x, b.y, b.z, b.w, b.h, b.d, b.u, b.v, tf[p.name], 1)
                if root == "head":
                    qs = [([(a * HEAD_SCALE, bb * HEAD_SCALE, c * HEAD_SCALE) for a, bb, c in q], col) for q, col in qs]
                quads += qs
        Ld = H.norm((0.35, -0.8, -0.5))
        for q, c, n in H.mesh_quads(table=d["locks"](), color=d["hair"][0]):
            l_ = 0.55 + 0.5 * max(0.0, -(n[0] * Ld[0] + n[1] * Ld[1] + n[2] * Ld[2]))
            quads.append(([(a * HEAD_SCALE, b * HEAD_SCALE, c * HEAD_SCALE) for a, b, c in q], tuple(int(min(255, v * l_)) for v in c)))
        G.draw(quads, os.path.join(outdir, name + "_preview.png"), 13, 140,
               [(math.pi, 0.05), (math.pi - 0.7, 0.12), (math.pi / 2, 0.05), (0.0, 0.08)], (280, 480))
        head = [qc for qc in quads if min(p[1] for p in qc[0]) < 1.0]
        G.draw(head, os.path.join(outdir, name + "_face.png"), 26, 260, [(math.pi, 0.0), (math.pi - 0.5, 0.1)], (300, 330))


if __name__ == "__main__":
    main()
    if len(sys.argv) > 2 and sys.argv[1] == "preview":
        preview(sys.argv[2], sys.argv[3] if len(sys.argv) > 3 else "")
