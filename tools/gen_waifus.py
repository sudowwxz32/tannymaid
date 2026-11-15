"""Totally Waifu?: genera los modelos 3D (ropa y accesorios), el cabello en mechones y las texturas de las 4 waifus.

Diseños originales del mod (todas adultas, figura estilizada de anime):
  Kozue  · mesa de crafteo      · carpintera: coleta castaña, pañuelo verde, overol de roble, cinturón de herramientas
  Homura · horno                · herrera: coletas rojo fuego, vestido gris piedra con borde de llamas, delantal de cuero
  Kura   · cofre                · sirvienta: cabello largo rubio, vestido de roble con cerrojo dorado, delantal y mochila-cofre
  Shiori · mesa de encantamientos · maga: corte hime índigo, túnica de obsidiana, detalles de diamante y capa roja

Reutiliza los pintores y utilidades de gen_tanny.py y la malla de mechones de hair_mesh.py.
Uso: python3 tools/gen_waifus.py [preview DIR]
"""
import math, os, random, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_tanny as G
import hair_mesh as H
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEXR = os.path.join(ROOT, "src/main/resources/assets/totallywaifu/textures/entity")
JAVA_DIR = os.path.join(ROOT, "src/main/java/com/totallywaifu/client")
Part, Box, mix, jit = G.Part, G.Box, G.mix, G.jit
ANCHORS = [("body", (0, 0, 0)), ("right_arm", (-5, 2.5, 0)), ("left_arm", (5, 2.5, 0)),
           ("right_leg", (-1.9, 12, 0)), ("left_leg", (1.9, 12, 0))]
SKIN = ((246, 214, 192), (230, 192, 170), (254, 228, 210), (204, 160, 138))


# ================================================================== utilidades de pintura
def cloth(c, hi, dk, pleat=10, top=None, bottom=None, hem=None, hem_c=None):
    """Tela con pliegues suaves; hem = alto del borde inferior en píxeles con color hem_c."""
    def fn(f, x, y, w, h):
        if f == "top":
            return top if top is not None else hi
        if f == "bottom":
            return bottom if bottom is not None else dk
        if hem and y >= h - hem:
            return hem_c
        k = (x % pleat) / pleat
        return mix(c, hi, 0.5 + 0.5 * math.sin(k * math.pi * 2) * 0.8) if (x // pleat) % 2 else mix(c, dk, abs(k - 0.5) * 0.6)
    return fn


def flat(c, dk=None, edge=0):
    dk = dk or tuple(int(v * 0.75) for v in c)

    def fn(f, x, y, w, h):
        if edge and (y < edge or y >= h - edge) and f not in ("top", "bottom"):
            return dk
        return c if f != "bottom" else dk
    return fn


def none_faces(*faces):
    def wrap(fn):
        def g(f, x, y, w, h):
            return None if f in faces else fn(f, x, y, w, h)
        return g
    return wrap


def metal(c, hi):
    return lambda f, x, y, w, h: hi if (x + y) % 6 == 0 or f == "top" else c


def wood(c, hi, dk):
    """Vetas de madera (para el overol de Kozue, la mochila-cofre de Kura)."""
    def fn(f, x, y, w, h):
        v = math.sin(x * 0.55 + math.sin(y * 0.25) * 2.2)
        col = mix(c, hi, 0.5 + 0.5 * v) if v > 0 else mix(c, dk, -v * 0.7)
        return dk if f == "bottom" else col
    return fn


def boots(c, hi, sole=(36, 28, 26), lace=None):
    def fn(f, x, y, w, h):
        if f == "bottom" or (f != "top" and y >= h - 3):
            return sole
        if f == "top":
            return None
        if y < 3:
            return mix(c, (0, 0, 0), 0.3)
        if lace and f == "front" and (y // 4) % 2 == 0 and abs(x - w / 2) < 3:
            return lace
        return mix(c, hi, abs((x % 9) / 9.0 - 0.5))
    return fn


def frill(c, sh):
    def fn(f, x, y, w, h):
        if f in ("top", "bottom"):
            return sh
        if y == h - 1:
            return c if (x // 2) % 2 == 0 else G.T
        return c if (x // 3) % 2 == 0 else sh
    return fn


def gem(c, hi):
    def fn(f, x, y, w, h):
        if f == "front" and x < w * 0.45 and y < h * 0.45:
            return (255, 255, 255)
        return mix(hi, c, (x + y) / max(1, w + h))
    return fn


# ================================================================== cara (píxeles, estilo skin)
def make_face(iris, lash=(16, 12, 18), brow=(24, 20, 26), mouth=(206, 110, 120), blush=None):
    dark, mid, light = iris
    WHITE_EYE, WHITE_EYE_SH = (248, 248, 250), (214, 220, 230)

    def face(expr):
        N, K = 16, 4
        img = Image.new("RGBA", (N * K, N * K), (0, 0, 0, 0))

        def px(x, y, c):
            if 0 <= x < N and 0 <= y < N:
                for yy in range(y * K, y * K + K):
                    for xx in range(x * K, x * K + K):
                        img.putpixel((xx, yy), c + (255,))

        for side in (0, 1):
            m = (lambda x: x) if side == 0 else (lambda x: N - 1 - x)
            ox = 2
            by = 4 if expr == "surprised" else 5
            if expr == "focused":
                px(m(ox), 5, brow); px(m(ox + 1), 5, brow); px(m(ox + 2), 6, brow); px(m(ox + 3), 6, brow)
            elif expr == "tired":
                px(m(ox), 6, brow); px(m(ox + 1), 5, brow); px(m(ox + 2), 5, brow); px(m(ox + 3), 5, brow)
            else:
                px(m(ox), by + 1, brow)
                for x in (ox + 1, ox + 2, ox + 3):
                    px(m(x), by, brow)
            if expr in ("blink", "tired"):
                for x in range(ox, ox + 4):
                    px(m(x), 9, lash)
                px(m(ox - 1), 8, lash)
                continue
            if expr == "happy":
                px(m(ox - 1), 9, lash); px(m(ox), 9, lash); px(m(ox + 1), 8, lash); px(m(ox + 2), 8, lash); px(m(ox + 3), 9, lash)
                continue
            top = 8 if expr != "focused" else 9
            bot = 11 if expr != "surprised" else 12
            for x in range(ox, ox + 4):
                px(m(x), top - 1, lash)
            px(m(ox - 1), top - 1, lash)
            px(m(ox - 1), top, lash)
            for y in range(top, bot + 1):
                t = (y - top) / max(1, bot - top)
                sh = y == bot
                px(m(ox), y, WHITE_EYE_SH if sh else WHITE_EYE)
                for x in (ox + 1, ox + 2, ox + 3):
                    px(m(x), y, dark if t < 0.34 else mid if t < 0.7 else light)
            px(m(ox + 2), top, (255, 255, 255))
            px(m(ox + 1), bot - 1, (255, 255, 255))
        if blush and expr in ("happy", "normal"):
            for x in (2, 3, 12, 13):
                px(x, 12, blush)
        if expr == "happy":
            px(7, 13, mouth); px(8, 13, mouth); px(6, 12, mouth); px(9, 12, mouth)
        elif expr == "surprised":
            px(7, 13, (120, 50, 60)); px(8, 13, (120, 50, 60)); px(7, 14, (120, 50, 60)); px(8, 14, (120, 50, 60))
        elif expr == "focused":
            px(7, 13, mouth); px(8, 13, mouth); px(9, 13, mouth)
        else:
            px(7, 13, mouth); px(8, 13, mouth)
        return img
    return face


# ================================================================== base de la skin
def base_painters(top_c, top_dk, sleeve_c, glove_c, leg_top, sock_c, boot_c, skirt_c):
    S_, SH, HI, DK = SKIN

    def body(f, x, y, w, h):
        if f == "front" and y < 4 and 0.36 * w <= x <= 0.64 * w:
            return S_
        if y >= 0.62 * h:
            return skirt_c
        return mix(top_c, top_dk, (x % 12) / 24.0)

    def arm(f, x, y, w, h):
        t = y / h
        if f == "top" or t < 0.4:
            return sleeve_c
        if glove_c and t > 0.78:
            return glove_c
        return mix(HI, S_, (t - 0.4) * 2)

    def leg(f, x, y, w, h):
        t = y / h
        if f == "top" or t < 0.3:
            return leg_top
        if f == "bottom" or t > 0.82:
            return boot_c
        if sock_c and t > 0.42:
            return sock_c if (y // 6) % 5 else mix(sock_c, (0, 0, 0), 0.15)
        return mix(HI, S_, (t - 0.3) * 3)
    return body, arm, leg


def head_paint_for(hair):
    c, hi, dk = hair
    S_, SH, HI, DK = SKIN

    def fn(f, x, y, w, h):
        if f == "top":
            return c if (x + y) % 7 else hi
        if f == "bottom":
            return SH
        if f == "back":
            return c if x % 6 else dk
        if f in ("left", "right"):
            front = x < w * 0.5 if f == "right" else x > w * 0.5
            if y < h * 0.82 or not front:
                return c if x % 5 else dk
            return SH
        if y < 3:
            return c
        col = mix(HI, S_, y / h * 1.4)
        if y >= h - 2:
            col = SH
        if x < 2 or x >= w - 2:
            col = mix(col, SH, 0.6)
        return col
    return fn


# ================================================================== piezas comunes del cuerpo (curvas)
def bust(paint, z=-2.2):
    return Part("bust", "body", (0, 2.9, z), (-0.3, 0, 0), [Box(-3.3, 0, -0.9, 6.6, 3.0, 1.0, paint)])


def hips(paint):
    """Cadera un poco más ancha que el cuerpo (da la figura de reloj de arena bajo la falda)."""
    return Part("hips", "body", (0, 0, 0), (0, 0, 0), [Box(-4.55, 8.6, -2.45, 9.1, 3.6, 4.9, paint)])


def skirt(paint, hem_paint, flare=0.26, length=6.5, wide=8.8):
    P = []

    def panel(name, pivot, rot, w):
        return Part(name, "body", pivot, rot, [Box(-w / 2, 0, -0.5, w, length, 1.0, paint),
                                              Box(-w / 2 + 0.15, length - 0.4, -0.25, w - 0.3, 0.9, 0.7, hem_paint)])
    P.append(panel("skirt_front", (0, 8.4, -2.3), (-flare, 0, 0), wide))
    P.append(panel("skirt_back", (0, 8.4, 2.3), (-flare, math.pi, 0), wide))
    P.append(panel("skirt_right", (-4.35, 8.4, 0), (-flare, math.pi / 2, 0), 5.0))
    P.append(panel("skirt_left", (4.35, 8.4, 0), (-flare, -math.pi / 2, 0), 5.0))
    for name, x, z, yaw in (("skirt_fr", -3.7, -1.85, math.pi / 4), ("skirt_fl", 3.7, -1.85, -math.pi / 4),
                            ("skirt_br", -3.7, 1.85, 3 * math.pi / 4), ("skirt_bl", 3.7, 1.85, -3 * math.pi / 4)):
        P.append(panel(name, (x, 8.4, z), (-flare, yaw, 0), 3.1))
    return P


def finish(P):
    m = G.mirror([p for p in P if p.name.endswith("_r")])
    for p in m:
        p.parent = p.parent.replace("right", "left")
    return P + m


# ================================================================== KOZUE (mesa de crafteo)
KZ_HAIR = ((128, 76, 44), (164, 104, 64), (88, 50, 28))
OAK, OAK_HI, OAK_DK = (176, 132, 82), (204, 162, 108), (128, 92, 54)
CREAM, CREAM_SH = (246, 236, 214), (222, 206, 180)
LEAF, LEAF_HI, LEAF_DK = (84, 148, 64), (116, 182, 88), (56, 104, 42)
IRON, IRON_HI = (150, 156, 164), (206, 212, 220)


def kozue_outfit():
    shirt = cloth(CREAM, (255, 248, 230), CREAM_SH, 12)
    overall = wood(OAK, OAK_HI, OAK_DK)
    P = [Part("shirt", "body", (0, 0, 0), (0, 0, 0), [Box(-4.25, -0.3, -2.25, 8.5, 8.4, 4.5, shirt)]),
         bust(lambda f, x, y, w, h: OAK_DK if y < 3 and f == "front" else overall(f, x, y, w, h)),
         hips(overall)]
    # peto del overol con bolsillo y tirantes
    P.append(Part("bib", "body", (0, 0, 0), (0, 0, 0), [Box(-3.0, 3.4, -2.55, 6.0, 5.0, 0.4,
              lambda f, x, y, w, h: OAK_DK if (y < 2 or (f == "front" and abs(y - h * 0.55) < 1.5 and 0.25 * w < x < 0.75 * w)) else overall(f, x, y, w, h))]))
    P.append(Part("strap_r", "body", (-2.4, 0, 0), (0, 0, 0), [Box(-0.6, -0.4, -2.6, 1.2, 4.2, 5.2, none_faces("top", "bottom")(flat(OAK_DK)))]))
    P.append(Part("button_r", "body", (-2.4, 3.5, -2.85), (0, 0, 0), [Box(-0.45, -0.45, -0.2, 0.9, 0.9, 0.4, metal((214, 176, 70), (250, 220, 120)))]))
    # falda-overol corta y amplia
    P += skirt(lambda f, x, y, w, h: OAK_DK if y < 2 else overall(f, x, y, w, h), flat(OAK_DK), flare=0.3, length=5.2)
    # cinturón de herramientas con martillo y sierra pequeña
    P.append(Part("toolbelt", "body", (0, 0, 0), (0, 0, 0), [Box(-4.7, 7.6, -2.75, 9.4, 1.3, 5.5,
              lambda f, x, y, w, h: (220, 180, 80) if f == "front" and abs(x - w / 2) < 3 else (96, 62, 36))]))
    P.append(Part("hammer", "body", (4.9, 8.2, -0.6), (0, 0, -0.15), [Box(-0.3, 0, -0.3, 0.6, 3.8, 0.6, flat((120, 80, 46))),
                                                                      Box(-0.7, 3.6, -1.2, 1.4, 1.1, 2.4, metal(IRON, IRON_HI))]))
    P.append(Part("pouch_r", "body", (-3.4, 8.3, -2.7), (0, 0.2, 0), [Box(-1.1, 0, -1.0, 2.2, 2.4, 1.2,
              lambda f, x, y, w, h: (110, 72, 42) if y > 3 else (84, 54, 30))]))
    P.append(Part("ruler", "body", (-4.85, 7.9, 0.9), (0, 0, 0.1), [Box(-0.25, 0, -0.5, 0.5, 4.2, 1.0,
              lambda f, x, y, w, h: (40, 30, 20) if y % 6 == 0 else (236, 200, 90))]))
    # mangas remangadas, guantes de trabajo y botas
    P.append(Part("sleeve_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.35, -2.4, -2.35, 3.7, 4.3, 4.7, none_faces("bottom")(shirt))]))
    P.append(Part("roll_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.5, 1.6, -2.5, 4.0, 1.3, 5.0, none_faces("top", "bottom")(flat(CREAM_SH)))]))
    P.append(Part("glove_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.2, 7.6, -2.2, 3.4, 2.6, 4.4, none_faces("top", "bottom")(flat(LEAF, LEAF_DK, 2)))]))
    P.append(Part("boot_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.3, 6.6, -2.3, 4.6, 5.6, 4.6, boots((112, 70, 40), (146, 96, 58), lace=CREAM))]))
    P.append(Part("toe_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.3, 10.2, -3.05, 4.6, 2.0, 0.8, boots((112, 70, 40), (146, 96, 58)))]))
    return finish(P)


def kozue_head():
    P = [Part("bandana", "head", (0, -6.6, 0), (0, 0, 0), [Box(-4.6, -1.4, -4.6, 9.2, 1.6, 9.2,
          none_faces("top", "bottom")(lambda f, x, y, w, h: LEAF_DK if (x // 3 + y // 3) % 4 == 0 else LEAF))]),
         Part("knot", "head", (0, -6.4, 4.7), (0.3, 0, 0), [Box(-1.0, -1.0, -0.5, 2.0, 2.0, 1.0, flat(LEAF, LEAF_DK)),
                                                            Box(-1.6, 0.5, 0.0, 1.2, 2.6, 0.5, flat(LEAF_HI)), Box(0.4, 0.5, 0.0, 1.2, 2.4, 0.5, flat(LEAF_HI))]),
         Part("pencil", "head", (-4.6, -4.2, -0.4), (0.25, 0, 0.2), [Box(-0.3, -0.3, -2.2, 0.6, 0.6, 4.4,
          lambda f, x, y, w, h: (60, 50, 40) if x < 3 else (236, 196, 60))])]
    return P


def kozue_locks():
    rnd = random.Random(31)
    j = lambda a: rnd.uniform(-a, a)
    L = []
    for i in range(16):
        phi = i / 16 * 2 * math.pi + 0.1
        L.append([phi, 0.05, 1.45 if abs(math.cos(phi)) < 0.7 else 1.25, 0.0, 0, 0, 2.9, 0.9, 0.55, 1.0, H.KIND_BOB])
    for i in range(9):
        phi = math.radians(130 + i * 12.5)
        L.append([phi, 0.9, math.pi / 2 + 0.25, 0.0, 0, 0, 2.6, 0.9, 0.45, 1.0, H.KIND_BOB])
    # coleta alta media (hasta media espalda) con vuelo
    for i in range(12):
        phi = math.pi + math.radians(-28 + i * 5) + j(0.03)
        L.append([phi, 0.34 + j(0.03), 0.72 + j(0.04), 10.5 + j(1.0), 0.18 + j(0.05), 0.35 + j(0.1), 2.7, 0.95, 1.4 + j(0.15), 1.0, H.KIND_LONG])
    for s_ in (-1, 1):
        L.append([s_ * math.radians(50), 0.3, math.pi / 2 - 0.05, 5.6, 0.06, 0.3, 1.8, 0.85, 0.6, 1.0, H.KIND_LONG])
        for deg in (80, 94):
            L.append([s_ * math.radians(deg), 0.3, math.pi / 2, 2.2, 0.15, -0.2, 2.2, 0.9, 0.35, 1.0, H.KIND_BOB])
    for deg, hang in ((-38, 2.2), (-27, 2.6), (-15, 2.4), (-4, 2.7), (8, 2.3), (19, 2.6), (30, 2.3), (40, 2.0)):
        L.append([math.radians(deg), 0.3, 1.0, hang + j(0.2), 0.12, -0.12, 2.0, 0.8, 0.55, 1.0, H.KIND_SOFTBANG])
    L.append([math.radians(14), 0.0, 0.0, 2.6, 0, 0, 1.0, 0.7, 0.6, 1.0, H.KIND_AHOGE])
    return L


# ================================================================== HOMURA (horno)
HM_HAIR = ((214, 66, 38), (250, 120, 60), (150, 36, 24))
STONE, STONE_HI, STONE_DK = (112, 112, 116), (146, 146, 150), (74, 74, 80)
FIRE1, FIRE2, FIRE3 = (255, 214, 90), (250, 140, 40), (214, 62, 30)
LEATHER, LEATHER_HI = (104, 64, 40), (136, 88, 56)


def cobble(f, x, y, w, h):
    """Vestido gris piedra con dibujo de adoquines (como el horno)."""
    bx, by = (x + (8 if (y // 8) % 2 else 0)) % 16, y % 8
    if bx == 0 or by == 0:
        return STONE_DK
    return mix(STONE, STONE_HI, ((x * 7 + y * 13) % 11) / 22.0)


def flame_hem(base):
    def fn(f, x, y, w, h):
        if f in ("top", "bottom"):
            return base(f, x, y, w, h)
        edge = h - (3 + 4 * abs(math.sin(x * 0.45)))
        if y >= edge:
            t = (y - edge) / max(1, h - edge)
            return FIRE1 if t > 0.75 else FIRE2 if t > 0.35 else FIRE3
        return base(f, x, y, w, h)
    return fn


def homura_outfit():
    apron = lambda f, x, y, w, h: LEATHER_HI if (x + y) % 11 == 0 else (LEATHER if f != "bottom" else (70, 44, 28))
    P = [Part("dress", "body", (0, 0, 0), (0, 0, 0), [Box(-4.25, -0.3, -2.25, 8.5, 8.6, 4.5, cobble)]),
         bust(cobble), hips(cobble)]
    P.append(Part("collar", "body", (0, 0, 0), (0, 0, 0), [Box(-4.4, -0.5, -2.4, 8.8, 1.2, 4.8, flat(FIRE3, (150, 40, 20)))]))
    P += skirt(flame_hem(cobble), flat(FIRE2, FIRE3), flare=0.24, length=7.0)
    # delantal de herrera (cuero) sobre el pecho y la falda
    P.append(Part("apron_top", "body", (0, 0, 0), (0, 0, 0), [Box(-2.8, 4.6, -2.75, 5.6, 4.0, 0.35, apron)]))
    P.append(Part("apron", "skirt_front", (0, 0, -0.55), (0, 0, 0), [Box(-3.0, 0.1, -0.4, 6.0, 6.0, 0.4, apron)]))
    P.append(Part("apron_strap", "body", (0, 0, 0), (0, 0, 0), [Box(-4.45, 7.6, -2.6, 8.9, 0.9, 5.2, flat((70, 44, 28)))]))
    # pinzas de herrera colgando
    P.append(Part("tongs", "body", (4.9, 8.0, 0.2), (0, 0, -0.12), [Box(-0.25, 0, -0.5, 0.5, 4.4, 0.4, metal((60, 60, 66), IRON_HI)),
                                                                    Box(-0.25, 0, 0.15, 0.5, 4.4, 0.4, metal((60, 60, 66), IRON_HI))]))
    # mangas abullonadas con puño de fuego y guantes gruesos
    P.append(Part("sleeve_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.5, -2.45, -2.5, 4.0, 4.0, 5.0, flame_hem(cobble))]))
    P.append(Part("glove_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.3, 6.4, -2.3, 3.6, 3.8, 4.6, none_faces("top")(lambda f, x, y, w, h:
              (70, 44, 28) if y < 3 else apron(f, x, y, w, h)))]))
    P.append(Part("boot_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.3, 5.4, -2.3, 4.6, 6.8, 4.6, boots((40, 36, 38), (70, 66, 70), lace=FIRE2))]))
    P.append(Part("toe_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.3, 10.2, -3.05, 4.6, 2.0, 0.8, boots((40, 36, 38), (70, 66, 70)))]))
    return finish(P)


def homura_head():
    flame = lambda f, x, y, w, h: FIRE1 if y > h * 0.6 else FIRE2 if y > h * 0.3 else FIRE3
    P = [Part("clip_r", "head", (-3.4, -7.4, -2.4), (0, 0, -0.35), [Box(-0.8, -1.8, -0.4, 1.6, 2.4, 0.8, flame),
                                                                   Box(-0.4, -2.6, -0.3, 0.8, 1.0, 0.6, flame)]),
         Part("tie_r", "head", (-3.6, -6.0, 3.3), (0, 0, 0), [Box(-0.9, -0.9, -0.9, 1.8, 1.8, 1.8, flat(FIRE3))])]
    return finish(P)


def homura_locks():
    rnd = random.Random(12)
    j = lambda a: rnd.uniform(-a, a)
    L = []
    for i in range(16):
        phi = i / 16 * 2 * math.pi
        L.append([phi, 0.05, 1.4 if abs(math.cos(phi)) < 0.7 else 1.2, 0.0, 0, 0, 2.9, 0.9, 0.55, 1.0, H.KIND_BOB])
    for i in range(9):
        phi = math.radians(130 + i * 12.5)
        L.append([phi, 0.8, math.pi / 2 + 0.2, 1.4, 0.1, -0.2, 2.5, 0.9, 0.4, 1.0, H.KIND_BOB])
    # dos coletas largas que nacen a los lados de la coronilla y caen con mucho vuelo
    for s_ in (-1, 1):
        for i in range(9):
            phi = s_ * (math.radians(118) + math.radians(-14 + i * 3.5)) + j(0.03)
            L.append([phi, 0.5 + j(0.03), 0.95 + j(0.04), 12.5 + j(1.0), 0.32 + j(0.05), 0.45 + j(0.1), 2.6, 0.95, 1.5 + j(0.2), 1.0, H.KIND_LONG])
    for s_ in (-1, 1):
        L.append([s_ * math.radians(48), 0.3, math.pi / 2 - 0.05, 6.2, 0.05, 0.3, 1.8, 0.85, 0.55, 1.0, H.KIND_LONG])
        L.append([s_ * math.radians(60), 0.3, math.pi / 2 - 0.03, 4.4, 0.1, 0.35, 1.7, 0.85, 0.8, 1.0, H.KIND_LONG])
    # flequillo en picos (como llamas)
    for deg, hang in ((-40, 2.0), (-30, 2.8), (-20, 2.2), (-9, 3.0), (2, 2.4), (13, 3.0), (24, 2.2), (34, 2.7), (42, 1.9)):
        L.append([math.radians(deg), 0.3, 1.0, hang + j(0.2), 0.14, -0.16, 1.9, 0.8, 0.55, 1.0, H.KIND_BANG])
    L.append([math.radians(-10), 0.0, 0.0, 3.2, 0, 0, 1.1, 0.7, 0.6, 1.0, H.KIND_AHOGE])
    return L


# ================================================================== KURA (cofre)
KR_HAIR = ((255, 228, 140), (255, 244, 190), (214, 176, 90))
CHEST, CHEST_HI, CHEST_DK = (162, 112, 52), (192, 140, 72), (104, 70, 30)
GOLD, GOLD_HI = (222, 176, 60), (255, 226, 120)
WHITE, WHITE_SH, WHITE_DK = (250, 248, 244), (226, 222, 222), (196, 190, 196)


def kura_outfit():
    dress = cloth(CHEST, CHEST_HI, CHEST_DK, 10)
    apron = lambda f, x, y, w, h: WHITE_DK if f == "bottom" else (WHITE if (x + y) % 9 else WHITE_SH)
    P = [Part("dress", "body", (0, 0, 0), (0, 0, 0), [Box(-4.25, -0.3, -2.25, 8.5, 8.6, 4.5, dress)]),
         bust(lambda f, x, y, w, h: WHITE if f == "front" and 0.3 * w < x < 0.7 * w else dress(f, x, y, w, h)), hips(dress)]
    P.append(Part("collar", "body", (0, 0, 0), (0, 0, 0), [Box(-4.3, -0.4, -2.35, 8.6, 1.1, 4.7, flat(WHITE, WHITE_SH))]))
    # cerrojo dorado del cofre en el pecho (su emblema)
    P.append(Part("latch", "body", (0, 1.4, -2.7), (0, 0, 0), [Box(-0.9, -0.4, -0.45, 1.8, 2.2, 0.6, metal(GOLD, GOLD_HI)),
                                                              Box(-0.35, 0.4, -0.6, 0.7, 0.7, 0.2, flat((60, 40, 20)))]))
    P += skirt(dress, frill(WHITE, WHITE_SH), flare=0.27, length=6.6)
    P.append(Part("apron", "skirt_front", (0, 0, -0.55), (0, 0, 0), [Box(-3.0, 0.2, -0.4, 6.0, 5.6, 0.4, apron),
                                                                      Box(-3.3, 5.6, -0.45, 6.6, 0.9, 0.5, frill(WHITE, WHITE_SH))]))
    P.append(Part("sash", "body", (0, 0, 0), (0, 0, 0), [Box(-4.4, 7.4, -2.4, 8.8, 1.0, 4.8, flat(WHITE, WHITE_SH))]))
    # mochila con forma de cofre (madera, bandas de hierro y cerrojo)
    def chestbox(f, x, y, w, h):
        if y < 3 or (f in ("front", "back") and abs(y - h * 0.4) < 2):
            return CHEST_DK
        if f == "back" and abs(x - w / 2) < 3 and abs(y - h * 0.45) < 4:
            return GOLD
        return wood(CHEST, CHEST_HI, CHEST_DK)(f, x, y, w, h)
    P.append(Part("backpack", "body", (0, 1.2, 2.3), (0.05, 0, 0), [Box(-3.4, 0, 0, 6.8, 6.2, 3.2, chestbox)]))
    P.append(Part("pack_strap_r", "body", (-2.3, 0, 0), (0, 0, 0), [Box(-0.5, -0.45, -2.45, 1.0, 5.4, 4.9, none_faces("top", "bottom")(flat((90, 60, 30))))]))
    P.append(Part("puff_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.55, -2.45, -2.55, 4.1, 4.1, 5.1, flame_free_puff())]))
    P.append(Part("cuff_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.25, 7.4, -2.25, 3.5, 1.3, 4.5, frill(WHITE, WHITE_SH))]))
    P.append(Part("frill_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.25, 4.6, -2.25, 4.5, 0.9, 4.5, frill(WHITE, WHITE_SH))]))
    P.append(Part("shoe_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.2, 9.9, -2.75, 4.4, 2.15, 4.95, boots((60, 36, 24), (96, 60, 40)))]))
    return finish(P)


def flame_free_puff():
    def fn(f, x, y, w, h):
        if f == "top":
            return CHEST_HI
        if f == "bottom" or y >= h - 3:
            return WHITE if (x // 2) % 2 == 0 else WHITE_SH
        return mix(CHEST_HI, CHEST_DK, abs((x % 8) / 8.0 - 0.5) * 2)
    return fn


def kura_head():
    head = lambda f, x, y, w, h: WHITE_DK if f == "bottom" else (WHITE if (x // 2) % 2 == 0 or y < h - 2 else WHITE_SH)
    return [Part("headdress", "head", (0, -8.1, -1.4), (-0.35, 0, 0), [Box(-3.8, -1.6, -0.4, 7.6, 1.8, 0.8, head)]),
            Part("headband", "head", (0, -7.6, 0), (0, 0, 0), [Box(-4.55, -0.5, -1.2, 9.1, 0.9, 2.4, none_faces("top", "bottom")(flat(CHEST_DK)))])]


def kura_locks():
    L = H.yunn_locks()
    return L


# ================================================================== SHIORI (mesa de encantamientos)
SH_HAIR = ((72, 52, 150), (112, 92, 196), (44, 30, 100))
OBS, OBS_HI, OBS_DK = (36, 26, 52), (66, 48, 96), (20, 14, 30)
DIAMOND, DIAMOND_HI = (92, 220, 214), (200, 255, 250)
CLOTH_RED, CLOTH_RED_DK = (176, 40, 52), (120, 24, 36)


def shiori_outfit():
    robe = lambda f, x, y, w, h: (mix(OBS, OBS_HI, 0.6) if (x * 3 + y * 5) % 37 == 0 else
                                  cloth(OBS, OBS_HI, OBS_DK, 12)(f, x, y, w, h))
    rune = lambda f, x, y, w, h: DIAMOND if (f in ("front", "back") and y > h - 4 and (x // 3) % 2 == 0) else robe(f, x, y, w, h)
    P = [Part("robe", "body", (0, 0, 0), (0, 0, 0), [Box(-4.25, -0.3, -2.25, 8.5, 8.6, 4.5, robe)]),
         bust(robe), hips(robe)]
    P.append(Part("collar", "body", (0, 0, 0), (0, 0, 0), [Box(-4.4, -0.6, -2.45, 8.8, 1.4, 4.9, flat(DIAMOND, (50, 160, 156)))]))
    P.append(Part("brooch", "body", (0, 1.0, -2.75), (0, 0, 0.78), [Box(-0.7, -0.7, -0.4, 1.4, 1.4, 0.6, gem(DIAMOND, DIAMOND_HI))]))
    P.append(Part("sash", "body", (0, 0, 0), (0, 0, 0), [Box(-4.4, 7.2, -2.4, 8.8, 1.2, 4.8, flat(CLOTH_RED, CLOTH_RED_DK))]))
    P += skirt(rune, flat(DIAMOND, (50, 160, 156)), flare=0.2, length=8.6, wide=8.6)
    # capa roja (como el paño de la mesa) que cuelga de los hombros
    P.append(Part("cape", "body", (0, -0.2, 2.45), (0.12, 0, 0), [Box(-4.6, 0, 0, 9.2, 15.5, 0.5,
              lambda f, x, y, w, h: CLOTH_RED_DK if (y >= h - 3 or x % 14 == 0) else CLOTH_RED)]))
    # mangas anchas de maga con borde de diamante
    P.append(Part("sleeve_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.55, -2.45, -2.55, 4.1, 8.4, 5.1,
              none_faces("bottom")(lambda f, x, y, w, h: DIAMOND if y >= h - 3 else robe(f, x, y, w, h)))]))
    P.append(Part("bell_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.9, 5.6, -2.9, 4.8, 1.6, 5.8, none_faces("top", "bottom")(flat(OBS_HI, OBS)))]))
    P.append(Part("boot_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.3, 6.0, -2.3, 4.6, 6.2, 4.6, boots(OBS, OBS_HI, lace=DIAMOND))]))
    P.append(Part("toe_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.3, 10.2, -3.05, 4.6, 2.0, 0.8, boots(OBS, OBS_HI))]))
    return finish(P)


def shiori_head():
    return [Part("circlet", "head", (0, -6.9, 0), (0, 0, 0), [Box(-4.55, -0.45, -4.55, 9.1, 0.8, 9.1, none_faces("top", "bottom")(metal((200, 170, 70), GOLD_HI)))]),
            Part("tiara_gem", "head", (0, -7.1, -4.75), (0, 0, 0.78), [Box(-0.8, -0.8, -0.35, 1.6, 1.6, 0.6, gem(DIAMOND, DIAMOND_HI))])]


def shiori_locks():
    """Corte hime: flequillo recto, mechones laterales rectos hasta el pecho y cabello muy largo atrás."""
    rnd = random.Random(5)
    j = lambda a: rnd.uniform(-a, a)
    L = []
    for i in range(14):
        phi = i / 14 * 2 * math.pi + 0.1
        L.append([phi, 0.04, 1.1, 0.0, 0, 0, 3.0, 0.9, 0.8, 1.0, H.KIND_BOB])
    for layer, (n, lift, hang) in enumerate(((13, 0.9, 14.5), (11, 0.5, 15.0), (9, 0.2, 13.0))):
        for i in range(n):
            phi = math.radians(104 + i * (152.0 / (n - 1))) + j(0.02)
            L.append([phi, 0.15, math.pi / 2 + 0.1, hang + j(0.4), 0.05, 0.08, 2.9, 0.95, lift, 1.0, H.KIND_LONG])
    for s_ in (-1, 1):
        for deg in (52, 60, 68):
            L.append([s_ * math.radians(deg), 0.3, math.pi / 2 - 0.02, 8.8 + j(0.3), 0.03, 0.05, 2.0, 0.9, 0.6, 1.0, H.KIND_LONG])
        for deg in (78, 92):
            L.append([s_ * math.radians(deg), 0.2, math.pi / 2 + 0.05, 6.0, 0.15, 0.1, 2.5, 0.95, 0.7, 1.0, H.KIND_BOB])
    for deg in range(-40, 44, 9):
        L.append([math.radians(deg), 0.3, 1.0, 2.5, 0.08, -0.05, 2.1, 0.8, 0.5, 1.0, H.KIND_SOFTBANG])
    return L


# ================================================================== definición de las 4
WAIFUS = {
    "kozue": dict(hair=KZ_HAIR, iris=((40, 96, 40), (84, 160, 70), (160, 220, 120)), outfit=kozue_outfit, head=kozue_head, locks=kozue_locks,
                  base=(CREAM, CREAM_SH, CREAM, LEAF, OAK, (92, 64, 40), (112, 70, 40), OAK_DK), blush=(244, 170, 170)),
    "homura": dict(hair=HM_HAIR, iris=((150, 60, 10), (232, 140, 30), (255, 214, 110)), outfit=homura_outfit, head=homura_head, locks=homura_locks,
                   base=(STONE, STONE_DK, STONE, LEATHER, STONE, (40, 36, 40), (40, 36, 38), STONE_DK), blush=None),
    "kura": dict(hair=KR_HAIR, iris=((30, 70, 150), (70, 130, 220), (160, 210, 255)), outfit=kura_outfit, head=kura_head, locks=kura_locks,
                 base=(CHEST, CHEST_DK, CHEST, None, CHEST, WHITE, (60, 36, 24), CHEST_DK), blush=(244, 170, 176)),
    "shiori": dict(hair=SH_HAIR, iris=((20, 110, 120), (60, 200, 200), (190, 255, 250)), outfit=shiori_outfit, head=shiori_head, locks=shiori_locks,
                   base=(OBS, OBS_DK, OBS, None, OBS, (30, 22, 40), OBS, OBS), blush=None),
}


def setup(name):
    d = WAIFUS[name]
    c, hi, dk = d["hair"]
    G.SKIN, G.SKIN_SH, G.SKIN_HI, G.SKIN_DK = SKIN
    G.HAIR, G.HAIR_HI, G.HAIR_DK = c, hi, dk
    G.head_paint = head_paint_for(d["hair"])
    G.body_paint, G.arm_paint, G.leg_paint = base_painters(*d["base"])
    G.face_image = make_face(d["iris"], blush=d["blush"])


def build(name):
    d = WAIFUS[name]
    setup(name)
    out = os.path.join(TEXR, name)
    os.makedirs(out, exist_ok=True)
    for expr, suf in [("normal", ""), ("blink", "_blink"), ("happy", "_happy"), ("surprised", "_surprised"), ("focused", "_focused"), ("tired", "_tired")]:
        G.skin(expr).save(os.path.join(out, name + suf + ".png"))
    hp = d["head"]()
    hw, hh = G.pack(hp, 64)
    G.build_texture(hp, hw, hh).save(os.path.join(out, "head.png"))
    op = d["outfit"]()
    ow, oh = G.pack(op, 64)
    G.build_texture(op, ow, oh).save(os.path.join(out, "outfit.png"))
    G.strand_texture().save(os.path.join(out, "hair_strand.png"))
    return hp, op, (hw, hh), (ow, oh)


def main():
    java = ["// GENERADO por tools/gen_waifus.py — no editar a mano.", "package com.totallywaifu.client;", "",
            "import net.minecraft.client.model.geom.ModelPart;", "import net.minecraft.client.model.geom.PartPose;",
            "import net.minecraft.client.model.geom.builders.*;", "", "import java.util.LinkedHashMap;", "import java.util.Map;", "",
            "public final class WaifuModels {", "    private WaifuModels() {}", ""]
    hair = ["// GENERADO por tools/gen_waifus.py — no editar a mano.", "package com.totallywaifu.client;", "",
            "/** Mechones de cada waifu: phi, th0, th1, caída, vuelo, curva, ancho, grosor, separación, tono, tipo. */",
            "public final class WaifuHair {", "    private WaifuHair() {}", "",
            "    public static final float CY = %s, R = %s, P = %s;" % (H.fjava(H.CY), H.fjava(H.R), H.fjava(H.P)),
            "    public static final int SCALP_STEPS = %d, HANG_STEPS = %d, SIDES = %d;" % (H.SCALP_STEPS, H.HANG_STEPS, H.SIDES),
            "    public static final float VOX_STEP = %s, WAVE_AMP = %s, WAVE_FREQ = %s;" % (H.fjava(H.VOX_STEP), H.fjava(H.WAVE_AMP), H.fjava(H.WAVE_FREQ)),
            "    public static final int[] PALETTE = {" + ", ".join("0x%02X%02X%02X" % c for c in H.PALETTE) + "};", ""]
    for name, d in WAIFUS.items():
        hp, op, (hw, hh), (ow, oh) = build(name)
        cap = name.capitalize()
        java += G.java_layer("head" + cap, hp, [("head", (0, 0, 0))], hw, hh) + [""]
        java += G.java_bake("bakeHead" + cap, hp, [("head", (0, 0, 0))]) + [""]
        java += G.java_layer("outfit" + cap, op, ANCHORS, ow, oh) + [""]
        java += G.java_bake("bakeOutfit" + cap, op, ANCHORS) + [""]
        c = d["hair"][0]
        hair.append("    public static final int COLOR_%s = 0x%02X%02X%02X;" % (name.upper(), *c))
        hair.append("    public static final float[][] %s = {" % name.upper())
        for l in d["locks"]():
            hair.append("            {" + ", ".join(H.fjava(v) for v in l) + "},")
        hair += ["    };", ""]
        print("%s: cabeza %dx%d · ropa %dx%d (%d piezas) · mechones %d" % (name, hw, hh, ow, oh, len(op), len(d["locks"]())))
    java += ["}", ""]
    hair += ["}", ""]
    os.makedirs(JAVA_DIR, exist_ok=True)
    open(os.path.join(JAVA_DIR, "WaifuModels.java"), "w").write("\n".join(java))
    open(os.path.join(JAVA_DIR, "WaifuHair.java"), "w").write("\n".join(hair))


def preview(outdir):
    os.makedirs(outdir, exist_ok=True)
    for name, d in WAIFUS.items():
        setup(name)
        out = os.path.join(TEXR, name)
        sk = Image.open(os.path.join(out, name + ".png")).convert("RGBA")
        ot = Image.open(os.path.join(out, "outfit.png")).convert("RGBA")
        ht = Image.open(os.path.join(out, "head.png")).convert("RGBA")
        quads = []
        base = [((0, 0, 0), (0, 0), (-4, -8, -4, 8, 8, 8)), ((0, 0, 0), (16, 16), (-4, 0, -2, 8, 12, 4)),
                ((-5, 2.5, 0), (40, 16), (-2, -2, -2, 3, 12, 4)), ((5, 2.5, 0), (32, 48), (-1, -2, -2, 3, 12, 4)),
                ((-1.9, 12, 0), (0, 16), (-2, 0, -2, 4, 12, 4)), ((1.9, 12, 0), (16, 48), (-2, 0, -2, 4, 12, 4))]
        for piv, (u, v), bx in base:
            quads += G.quads_for(sk, *bx, u, v, [(piv, (0, 0, 0))], 1)
        op = d["outfit"]()
        G.pack(op, 64)
        tf = G.chains(op, ANCHORS)
        for p in op:
            for b in p.boxes:
                quads += G.quads_for(ot, b.x, b.y, b.z, b.w, b.h, b.d, b.u, b.v, tf[p.name], 2)
        hp = d["head"]()
        G.pack(hp, 64)
        tf = G.chains(hp, [("head", (0, 0, 0))])
        for p in hp:
            for b in p.boxes:
                quads += G.quads_for(ht, b.x, b.y, b.z, b.w, b.h, b.d, b.u, b.v, tf[p.name], 1)
        Ld = H.norm((0.35, -0.8, -0.5))
        for q, c, n in H.mesh_quads(table=d["locks"](), color=d["hair"][0]):
            l = 0.55 + 0.5 * max(0.0, -(n[0] * Ld[0] + n[1] * Ld[1] + n[2] * Ld[2]))
            quads.append((q, tuple(int(min(255, v * l)) for v in c)))
        G.draw(quads, os.path.join(outdir, name + "_preview.png"), 13, 140,
               [(math.pi, 0.05), (math.pi - 0.7, 0.12), (math.pi / 2, 0.05), (0.0, 0.08)], (280, 480))


if __name__ == "__main__":
    main()
    if len(sys.argv) > 2 and sys.argv[1] == "preview":
        preview(sys.argv[2])
