"""Tanny en alta resolución (texturas 4x) y modelos 3D generados.

Produce:
  - textures/entity/tanny/tanny*.png      skin 256x256 (brazos delgados) con 6 expresiones y ojos anime
  - textures/entity/tanny/hair.png         cabello 3D con brillo anime ("anillo de luz") y cofia
  - textures/entity/tanny/outfit.png       vestido 3D: falda redonda con enaguas, delantal, lazos, puños, medias y zapatos
  - client/TannyGeneratedModels.java       geometría Forge del cabello y del vestido
Uso:  python3 tools/gen_tanny.py [preview DIR]
"""
import math, os, random, sys
from PIL import Image, ImageDraw, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX = os.path.join(ROOT, "src/main/resources/assets/tannymaid/textures/entity/tanny")
JAVA = os.path.join(ROOT, "src/main/java/com/tanny/tannymaid/client/TannyGeneratedModels.java")
S = 4                      # resolución: 4 píxeles por píxel del modelo
T = (0, 0, 0, 0)
rnd = random.Random(7)

# ----------------------------------------------------------------- paleta
SKIN, SKIN_SH, SKIN_HI, SKIN_DK = (204, 148, 108), (184, 128, 92), (220, 166, 126), (160, 108, 76)
HAIR, HAIR_HI, HAIR_DK, HAIR_SHINE = (24, 22, 30), (52, 50, 70), (12, 11, 16), (96, 100, 132)
IRIS_DK, IRIS, IRIS_HI = (58, 30, 18), (128, 70, 34), (206, 132, 62)
LASH, BROW = (22, 14, 16), (40, 26, 24)
BLUSH, BLUSH_DK, LIP, MOUTH = (236, 132, 136), (214, 104, 112), (190, 86, 96), (110, 36, 48)
BLACK, BLACK_HI, BLACK_DK = (30, 28, 36), (50, 47, 60), (18, 17, 22)
WHITE, WHITE_SH, WHITE_DK = (248, 246, 244), (224, 220, 222), (196, 190, 196)
RED, RED_DK, RED_HI = (204, 44, 70), (150, 28, 50), (236, 96, 116)
PINK = (242, 160, 184)
SHOE, SHOE_HI = (32, 24, 26), (84, 70, 76)


def jit(c, a=4):
    if len(c) == 4:
        return c
    return tuple(max(0, min(255, v + rnd.randint(-a, a))) for v in c)


def mix(a, b, t):
    t = max(0.0, min(1.0, t))
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))


# ----------------------------------------------------------------- estructura
class Box:
    def __init__(self, x, y, z, w, h, d, paint):
        self.x, self.y, self.z, self.w, self.h, self.d, self.paint = x, y, z, w, h, d, paint
        self.u = self.v = 0


class Part:
    def __init__(self, name, parent, pivot=(0, 0, 0), rot=(0, 0, 0), boxes=()):
        self.name, self.parent, self.pivot, self.rot, self.boxes = name, parent, pivot, rot, list(boxes)


def flip_name(n):
    return n.replace("_r", "_l") if n.endswith("_r") or "_r_" in n else n.replace("right", "left")


def mirror(parts):
    out = []
    names = {p.name for p in parts}
    for p in parts:
        name = flip_name(p.name)
        parent = flip_name(p.parent) if p.parent in names else p.parent.replace("right", "left")
        px, py, pz = p.pivot
        rx, ry, rz = p.rot
        boxes = [Box(-(b.x + b.w), b.y, b.z, b.w, b.h, b.d, b.paint) for b in p.boxes]
        out.append(Part(name, parent, (-px, py, pz), (rx, -ry, -rz), boxes))
    return out


def pack(parts, width):
    rects = []
    for p in parts:
        for b in p.boxes:
            rects.append((math.ceil(b.d + b.h), math.ceil(2 * (b.d + b.w)), b))
    rects.sort(key=lambda r: (-r[0], -r[1]))
    x = y = shelf = 0
    for rh, rw, b in rects:
        if x + rw > width:
            x, y, shelf = 0, y + shelf, 0
        b.u, b.v = x, y
        x += rw + 1
        shelf = max(shelf, rh + 1)
    h = 32
    while h < y + shelf:
        h *= 2
    return width, h


def face_rects(u, v, w, h, d):
    return {"top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d), "right": (u, v + d, d, h),
            "front": (u + d, v + d, w, h), "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h)}


AO = True


def ao_factor(f, x, y, w, h):
    """Sombra suave en los bordes de cada cara (da profundidad a las piezas 3D)."""
    if not AO or f == "top" or w < 6 or h < 6:
        return 1.0
    edge = min(x, w - 1 - x)
    k = 1.0 - 0.10 * max(0.0, 1.0 - edge / 3.0)
    k *= 1.0 - 0.14 * max(0.0, 1.0 - (h - 1 - y) / 4.0)
    return k


def paint_region(img, u, v, w, h, d, fn):
    """Pinta las 6 caras de una caja a resolución S. fn(cara, x, y, ancho_px, alto_px)."""
    for f, (fx, fy, fw, fh) in face_rects(u, v, w, h, d).items():
        x0, y0 = int(round(fx * S)), int(round(fy * S))
        x1, y1 = int(round((fx + fw) * S)), int(round((fy + fh) * S))
        cw, ch = max(1, x1 - x0), max(1, y1 - y0)
        for py in range(y0, y0 + ch):
            for px in range(x0, x0 + cw):
                c = fn(f, px - x0, py - y0, cw, ch)
                if c is None or not (0 <= px < img.width and 0 <= py < img.height):
                    continue
                if len(c) == 3:
                    k = ao_factor(f, px - x0, py - y0, cw, ch)
                    c = jit(tuple(int(ch_ * k) for ch_ in c), 2) + (255,)
                img.putpixel((px, py), c)


# ----------------------------------------------------------------- pintores: cabello
def hair_paint(band=None, tip=True, seed=0):
    """Cabello negro con mechones finos; band=(desde, hasta) en fracción de alto para el brillo anime."""
    r = random.Random(seed)
    strands = [r.choice([0, 0, 0, 1, 2, -1]) for _ in range(256)]

    def fn(f, x, y, w, h):
        s = strands[(x * 3 + (7 if f in ("left", "right") else 0)) % 256]
        if f == "top":
            c = HAIR_HI if (x + 2 * y) % 9 == 0 else HAIR
            return mix(c, HAIR_SHINE, 0.18) if (x // 3 + y // 3) % 5 == 0 else c
        if f == "bottom":
            return HAIR_DK
        t = y / max(1, h - 1)
        c = HAIR
        if s == 1:
            c = HAIR_HI
        elif s == -1 or x % 6 == 0:
            c = HAIR_DK
        if band and band[0] <= t <= band[1]:
            m = 1 - abs((t - (band[0] + band[1]) / 2) / ((band[1] - band[0]) / 2 + 1e-6))
            if (x + int(t * 10)) % 5 != 0:
                c = mix(c, HAIR_SHINE, 0.2 + 0.45 * m)
        if tip and t > 0.82:
            c = mix(c, HAIR_DK, (t - 0.82) * 3)
        return c
    return fn


def lace(f, x, y, w, h):
    if f in ("top", "bottom"):
        return WHITE_SH
    if y >= h - 2:
        return WHITE if (x // 2) % 2 == 0 else (T if y == h - 1 else WHITE_DK)
    if y % 4 == 1 and x % 4 == 2:
        return WHITE_DK                                    # agujeritos del encaje
    return mix(WHITE, WHITE_SH, abs((x % 6) - 3) / 6.0)


def solid(c, a=3):
    return lambda f, x, y, w, h: jit(c, a)


def ribbon(f, x, y, w, h):
    if f in ("bottom",):
        return RED_DK
    if f == "top":
        return RED_HI
    t = y / max(1, h - 1)
    c = mix(RED_HI, RED, t * 2) if t < 0.5 else mix(RED, RED_DK, (t - 0.5) * 1.6)
    if abs(x - w / 2) < 1.2 and f in ("front", "back"):
        c = mix(c, RED_DK, 0.35)                           # pliegue central del lazo
    return c


def pink_bow(f, x, y, w, h):
    return mix(PINK, RED_HI, y / max(1, h - 1) * 0.6)


# ----------------------------------------------------------------- pintores: vestido
def fabric(f, x, y, w, h):
    """Tela negra con pliegues verticales suaves."""
    if f == "top":
        return BLACK
    if f == "bottom":
        return BLACK_DK
    k = (x % 10) / 10.0
    c = mix(BLACK, BLACK_HI, 0.5 + 0.5 * math.sin(k * math.pi * 2)) if f in ("front", "back", "left", "right") else BLACK
    return c


def skirt_panel(apron=False):
    def fn(f, x, y, w, h):
        if f == "top":
            return BLACK_DK
        if f == "bottom":
            return WHITE_SH                      # enaguas blancas por debajo
        t = y / max(1, h - 1)
        if y >= h - 3:                           # encaje del borde
            return WHITE if (x // 2) % 2 == 0 else WHITE_SH
        if y == h - 4:
            return WHITE_DK
        pleat = (x % 12) / 12.0
        c = mix(BLACK_HI, BLACK_DK, abs(pleat - 0.5) * 2)
        c = mix(c, BLACK_DK, t * 0.35)
        if apron and f == "front" and w * 0.12 < x < w * 0.88:
            c = WHITE if (x + y) % 9 else WHITE_SH
            if x < w * 0.12 + 2 or x > w * 0.88 - 2:
                c = WHITE_DK
        return c
    return fn


def hem_lace(f, x, y, w, h):
    if f in ("top", "bottom"):
        return WHITE_SH
    if y == h - 1:
        return WHITE if (x // 2) % 2 == 0 else T
    return WHITE if (x // 3) % 2 == 0 else WHITE_SH


def apron(f, x, y, w, h):
    if f in ("top",):
        return WHITE
    if f == "bottom" or y >= h - 3:
        return WHITE if (x // 2) % 2 == 0 else WHITE_DK
    if f in ("left", "right"):
        return WHITE_SH
    c = mix(WHITE, WHITE_SH, abs((x % 14) - 7) / 14.0 + (0.15 if y % 16 == 0 else 0))
    if 0.35 * w < x < 0.65 * w and 0.2 * h < y < 0.45 * h:      # bolsillo
        c = WHITE_SH if not (y == int(0.2 * h) + 1) else WHITE_DK
    return c


def bust_fn(f, x, y, w, h):
    if f in ("front", "top"):
        if 0.3 * w <= x <= 0.7 * w:
            return WHITE if (x + y) % 7 else WHITE_SH     # pechera del delantal
        if abs(x - 0.3 * w) < 2 or abs(x - 0.7 * w) < 2:
            return WHITE_DK                               # volante
    return fabric(f, x, y, w, h)


def collar(f, x, y, w, h):
    if f == "bottom":
        return WHITE_DK
    t = y / max(1, h - 1)
    return mix(WHITE, WHITE_SH, 0.15 + 0.5 * t)


def puff(f, x, y, w, h):
    if f == "top":
        return BLACK_HI
    if f == "bottom" or y >= h - 3:
        return WHITE if (x // 2) % 2 == 0 else WHITE_SH  # puño blanco con encaje
    t = (x % 8) / 8.0
    return mix(BLACK_HI, BLACK_DK, abs(t - 0.5) * 2)


def stocking_frill(f, x, y, w, h):
    if f in ("top", "bottom"):
        return WHITE_SH
    if y == h - 1:
        return WHITE if (x // 2) % 2 == 0 else T
    if f == "front" and y in (1, 2) and abs(x - w // 2) < 3:
        return RED                                         # lacito rojo
    return WHITE if (x // 3) % 2 else WHITE_SH


def shoe(f, x, y, w, h):
    if f == "bottom" or (f != "top" and y >= h - 2):
        return (14, 10, 12)
    if f == "top":
        return SHOE_HI if (x + y) % 9 == 0 else SHOE
    if y < 3 and (x % 9) < 7:
        return (54, 40, 44)                                 # borde y correa
    if f == "front" and 2 <= y <= 4 and 1 < x < 5:
        return SHOE_HI                                      # brillo
    return SHOE


# ================================================================= CABELLO 3D
BANG_TILT = -0.14


def hair_parts():
    """Solo la cofia de maid es de cajas; el cabello es una malla de mechones (tools/hair_mesh.py)."""
    P = []
    P.append(Part("headband", "head", (0, -8.65, -1.6), (-0.3, 0, 0), [Box(-5.1, -0.6, -0.75, 10.2, 1.1, 1.5, collar)]))
    for i in range(7):
        x = -3.9 + i * 1.3
        P.append(Part("ruffle_%d" % i, "headband", (x, -0.55, 0.0), (0.0, 0, (i - 3) * 0.08),
                      [Box(-0.72, -1.6, -0.3, 1.44, 1.6, 0.55, lace)]))
    P.append(Part("hb_bow_r", "headband", (-4.9, -0.05, 0.3), (0, 0.25, 0.35), [Box(-1.0, -0.5, -0.3, 1.0, 1.0, 0.6, pink_bow)]))
    P += mirror([p for p in P if p.name.endswith("_r")])
    return P, [("head", (0, 0, 0))]


def strand_texture():
    """Textura de los mechones (gris claro con hebras finas); el color lo pone cada vértice."""
    r = random.Random(5)
    img = Image.new("RGBA", (32, 64), T)
    cols = [r.uniform(0.78, 1.0) for _ in range(32)]
    for x in range(32):
        for y in range(64):
            v = cols[x]
            if x % 5 == 0:
                v *= 0.82
            v *= 0.94 + 0.06 * math.sin(y * 0.4 + x)
            c = int(255 * max(0.0, min(1.0, v)))
            img.putpixel((x, y), (c, c, c, 255))
    return img


# ================================================================= VESTIDO 3D
SKIRT_FLARE = 0.24


def outfit_parts():
    P = []
    P.append(Part("bust", "body", (0, 3.0, -2.0), (-0.28, 0, 0), [Box(-3.5, 0, -0.9, 7.0, 3.0, 1.0, bust_fn)]))
    P.append(Part("collar", "body", (0, 0, 0), (0, 0, 0), [Box(-4.25, -0.4, -2.3, 8.5, 1.1, 4.6, collar)]))
    P.append(Part("collar_tip_r", "body", (-1.0, 0.5, -2.35), (0, 0, 0.55), [Box(-2.2, -0.5, -0.3, 2.2, 1.6, 0.4, collar)]))
    # lazo rojo del cuello
    P.append(Part("ribbon_knot", "body", (0, 1.0, -2.55), (0, 0, 0), [Box(-0.55, -0.55, -0.5, 1.1, 1.1, 0.8, ribbon)]))
    P.append(Part("ribbon_r", "body", (-0.4, 1.0, -2.5), (0, 0.15, 0.3), [Box(-2.0, -1.0, -0.45, 2.0, 2.0, 0.7, ribbon)]))
    P.append(Part("ribbon_tail_r", "body", (-0.3, 1.4, -2.5), (-0.1, 0, 0.25), [Box(-0.5, 0, -0.3, 0.9, 2.4, 0.5, ribbon)]))
    # cinturón del delantal y lazo grande en la espalda
    P.append(Part("sash", "body", (0, 0, 0), (0, 0, 0), [Box(-4.35, 7.2, -2.35, 8.7, 1.1, 4.7, collar)]))
    P.append(Part("bow_knot", "body", (0, 7.7, 2.5), (0, 0, 0), [Box(-1.0, -1.0, -0.4, 2.0, 2.0, 1.0, collar)]))
    P.append(Part("bow_r", "body", (-0.8, 7.7, 2.5), (0, 0.2, 0.35), [Box(-4.2, -1.6, -0.45, 4.2, 3.2, 0.9, collar)]))
    P.append(Part("bow_tail_r", "body", (-0.7, 8.2, 2.7), (0.15, 0, 0.15), [Box(-1.0, 0, -0.35, 2.0, 6.4, 0.7, apron)]))
    # falda redonda: 4 paneles principales (animados) + 4 esquinas, cada uno con encaje de enagua
    def panel(name, pivot, rot, w, apron_front=False):
        return Part(name, "body", pivot, rot, [Box(-w / 2, 0, -0.5, w, 8.0, 1.0, skirt_panel(apron_front)),
                                              Box(-w / 2 + 0.15, 7.6, -0.25, w - 0.3, 1.0, 0.7, hem_lace)])
    P.append(panel("skirt_front", (0, 8.0, -2.2), (-SKIRT_FLARE, 0, 0), 8.6))
    P.append(panel("skirt_back", (0, 8.0, 2.2), (-SKIRT_FLARE, math.pi, 0), 8.6))
    P.append(panel("skirt_right", (-4.2, 8.0, 0), (-SKIRT_FLARE, math.pi / 2, 0), 4.8))
    P.append(panel("skirt_left", (4.2, 8.0, 0), (-SKIRT_FLARE, -math.pi / 2, 0), 4.8))
    for name, x, z, yaw in (("skirt_fr", -3.55, -1.75, math.pi / 4), ("skirt_fl", 3.55, -1.75, -math.pi / 4),
                            ("skirt_br", -3.55, 1.75, 3 * math.pi / 4), ("skirt_bl", 3.55, 1.75, -3 * math.pi / 4)):
        P.append(panel(name, (x, 8.0, z), (-SKIRT_FLARE, yaw, 0), 3.0))
    # delantal sobre la falda (sigue al panel delantero)
    P.append(Part("apron", "skirt_front", (0, 0, -0.55), (0, 0, 0), [Box(-3.0, 0.2, -0.4, 6.0, 6.4, 0.4, apron),
                                                                     Box(-3.3, 6.5, -0.45, 6.6, 0.9, 0.5, hem_lace)]))
    # mangas abullonadas y puños
    P.append(Part("puff_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.55, -2.45, -2.55, 4.1, 4.1, 5.1, puff)]))
    P.append(Part("cuff_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.25, 7.4, -2.25, 3.5, 1.3, 4.5, lace)]))
    # medias con volante y zapatos Mary Jane
    P.append(Part("frill_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.25, 5.0, -2.25, 4.5, 0.9, 4.5, stocking_frill)]))
    P.append(Part("shoe_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.2, 9.9, -2.75, 4.4, 2.15, 4.95, shoe)]))
    mirrorable = [p for p in P if p.name.endswith("_r")]
    m = mirror(mirrorable)
    for p in m:   # los de brazos y piernas cuelgan de su lado
        if p.parent == "right_arm":
            p.parent = "left_arm"
        if p.parent == "right_leg":
            p.parent = "left_leg"
    P += m
    anchors = [("body", (0, 0, 0)), ("right_arm", (-5, 2.5, 0)), ("left_arm", (5, 2.5, 0)),
               ("right_leg", (-1.9, 12, 0)), ("left_leg", (1.9, 12, 0))]
    return P, anchors


# ================================================================= SKIN BASE (4x)
def head_paint(f, x, y, w, h):
    if f == "top":
        return HAIR if (x + y) % 7 else HAIR_HI
    if f == "bottom":
        return SKIN_SH
    if f == "back":
        return HAIR if (x % 6) else HAIR_DK
    if f in ("left", "right"):
        if y < h * 0.82:
            return HAIR if x % 5 else HAIR_DK
        return SKIN_SH                                   # oreja / mandíbula
    # frente: piel (la cara se dibuja aparte); línea del pelo arriba
    if y < 3:
        return HAIR
    t = y / h
    c = mix(SKIN_HI, SKIN, t * 1.4)
    if y >= h - 2:
        c = SKIN_SH                                      # sombra bajo la barbilla
    if x < 2 or x >= w - 2:
        c = mix(c, SKIN_SH, 0.6)
    return c


def body_paint(f, x, y, w, h):
    t = y / h
    if f == "top":
        return BLACK
    if f == "bottom":
        return BLACK_DK
    if f == "front":
        if y < 4 and 0.36 * w <= x <= 0.64 * w:
            return SKIN_SH if y < 2 else SKIN               # cuello
        if 0.28 * w <= x <= 0.72 * w and 4 <= y < 0.6 * h:
            if abs(x - w / 2) < 1 and y > 9 and (y // 5) % 2 == 0:
                return WHITE_DK                          # botones
            return WHITE if (x + y) % 8 else WHITE_SH     # pechera
        if (abs(x - 0.28 * w) < 1.5 or abs(x - 0.72 * w) < 1.5) and y < 0.6 * h:
            return WHITE_DK                              # volantes de la pechera
    if f == "back":
        # tirantes del delantal en X
        a = abs((x - w / 2) - (y - h * 0.3) * 0.55)
        b = abs((x - w / 2) + (y - h * 0.3) * 0.55)
        if y < h * 0.6 and (a < 2 or b < 2):
            return WHITE if (x + y) % 5 else WHITE_SH
    if 0.6 * h <= y < 0.68 * h:
        return WHITE_SH                                  # cintura (bajo el lazo 3D)
    return fabric(f, x, y, w, h)


def arm_paint(f, x, y, w, h):
    t = y / h
    if f == "top":
        return BLACK
    if f == "bottom":
        return SKIN_SH
    if t < 0.25:
        return fabric(f, x, y, w, h)                       # manga (bajo la manga 3D)
    if t < 0.29:
        return WHITE
    c = mix(SKIN_HI, SKIN, (t - 0.29) * 1.6)
    if f in ("back", "right") and x % 7 == 0:
        c = mix(c, SKIN_SH, 0.3)
    if t > 0.9:
        c = mix(c, SKIN_SH, 0.5)                          # mano
    return c


def leg_paint(f, x, y, w, h):
    t = y / h
    if f == "top":
        return BLACK
    if f == "bottom":
        return (14, 10, 12)
    if t < 0.36:
        return fabric(f, x, y, w, h)                       # bajo la falda
    if t < 0.42:
        return mix(SKIN_HI, SKIN, (t - 0.36) * 10)         # muslo
    if t < 0.84:
        c = mix(WHITE, WHITE_SH, abs((x % 10) - 5) / 10.0)   # medias blancas con caída suave
        if f in ("left", "right"):
            c = mix(c, WHITE_DK, 0.25)
        return c
    return SHOE


def eye_layer(expr, side):
    """Dibuja un ojo anime a 8x sobre 72x80 (luego se reduce). side: -1 izquierda de la imagen, 1 derecha."""
    W, H = 80, 96
    im = Image.new("RGBA", (W, H), T)
    d = ImageDraw.Draw(im)
    outer = 0 if side < 0 else W     # comisura externa
    if expr in ("normal", "surprised", "focused"):
        top = 14 if expr != "focused" else 28
        bot = 88 if expr == "surprised" else 84
        if expr == "surprised":
            top = 8
        # blanco del ojo
        d.ellipse((6, top + 4, W - 6, bot), fill=(250, 246, 246, 255))
        # iris con degradado
        ix0, ix1 = 9, W - 9
        if expr == "surprised":
            ix0, ix1 = 18, W - 18
        for yy in range(top, bot + 1):
            tt = (yy - top) / max(1, bot - top)
            col = mix(IRIS_DK, IRIS, tt * 1.4) if tt < 0.6 else mix(IRIS, IRIS_HI, (tt - 0.6) * 2.4)
            d.line((ix0, yy, ix1, yy), fill=col + (255,))
        mask = Image.new("L", (W, H), 0)
        ImageDraw.Draw(mask).ellipse((ix0, top, ix1, bot + 2), fill=255)
        eye_mask = Image.new("L", (W, H), 0)
        ImageDraw.Draw(eye_mask).ellipse((4, top + 2, W - 4, bot + 2), fill=255)
        iris = Image.new("RGBA", (W, H), T)
        iris.paste(im, (0, 0), mask)
        base = Image.new("RGBA", (W, H), T)
        sclera = Image.new("RGBA", (W, H), (250, 246, 246, 255))
        base.paste(sclera, (0, 0), eye_mask)
        base.alpha_composite(iris)
        im = base
        d = ImageDraw.Draw(im)
        # pupila
        px0, px1 = (W // 2 - 12, W // 2 + 12) if expr != "surprised" else (W // 2 - 7, W // 2 + 7)
        d.ellipse((px0, (top + bot) // 2 - 16, px1, (top + bot) // 2 + 14), fill=(28, 12, 8, 255))
        # brillos
        hx = W // 2 + (-14 if side < 0 else 6)
        d.ellipse((hx, top + 8, hx + 20, top + 30), fill=(255, 255, 255, 255))
        d.ellipse((W // 2 + (6 if side < 0 else -14), bot - 22, W // 2 + (14 if side < 0 else -6), bot - 13), fill=(255, 250, 240, 230))
        # pestañas superiores gruesas con "ala" hacia afuera
        lash_y = top
        d.line((2, lash_y + 8, W // 2, lash_y - 2, W - 2, lash_y + 8), fill=LASH + (255,), width=12, joint="curve")
        if side < 0:
            d.polygon((0, lash_y + 2, -2, lash_y + 16, 12, lash_y + 6), fill=LASH + (255,))
        else:
            d.polygon((W, lash_y + 2, W + 2, lash_y + 16, W - 12, lash_y + 6), fill=LASH + (255,))
        # pestañas inferiores finas (lado externo)
        if side < 0:
            d.line((4, bot - 6, 26, bot + 2), fill=(90, 50, 44, 255), width=4)
        else:
            d.line((W - 4, bot - 6, W - 26, bot + 2), fill=(90, 50, 44, 255), width=4)
        if expr == "focused":
            # párpado bajo: mirada decidida
            d.rectangle((0, 0, W, top + 4), fill=T)
    elif expr == "happy":
        d.arc((8, 34, W - 8, 100), 200, 340, fill=LASH + (255,), width=11)         # ^ ^
    elif expr == "blink":
        d.arc((6, -10, W - 6, 66), 25, 155, fill=LASH + (255,), width=8)
    elif expr == "tired":
        d.line((6, 54, W - 6, 50), fill=LASH + (255,), width=8)
        d.arc((12, 40, W - 12, 80), 20, 160, fill=(90, 50, 44, 255), width=5)
    return im


def face_image(expr):
    """Cara de 32x32 (8x8 del modelo a 4x), dibujada a 8x y reducida para un acabado suave."""
    K = 8
    big = Image.new("RGBA", (32 * K, 32 * K), T)
    d = ImageDraw.Draw(big)
    # rubor con rayitas anime
    for cx in (8.5, 23.5):
        blush = Image.new("RGBA", big.size, T)
        ImageDraw.Draw(blush).ellipse(((cx - 4.5) * K, 23 * K, (cx + 4.5) * K, 26.5 * K), fill=BLUSH + (150,))
        blush = blush.filter(ImageFilter.GaussianBlur(6))
        big.alpha_composite(blush)
        for i in range(3):
            x0 = (cx - 2.6 + i * 1.8) * K
            d.line((x0 + 6, 23.6 * K, x0 - 2, 25.6 * K), fill=BLUSH_DK + (200,), width=5)
    # cejas finas
    if expr == "focused":
        d.line((4 * K, 12.4 * K, 12 * K, 13.6 * K), fill=BROW + (255,), width=8)
        d.line((20 * K, 13.6 * K, 28 * K, 12.4 * K), fill=BROW + (255,), width=8)
    elif expr in ("surprised",):
        d.arc((4 * K, 10 * K, 12 * K, 14 * K), 200, 340, fill=BROW + (255,), width=7)
        d.arc((20 * K, 10 * K, 28 * K, 14 * K), 200, 340, fill=BROW + (255,), width=7)
    elif expr == "tired":
        d.line((4 * K, 13 * K, 12 * K, 12.2 * K), fill=BROW + (255,), width=7)
        d.line((20 * K, 12.2 * K, 28 * K, 13 * K), fill=BROW + (255,), width=7)
    else:
        d.arc((4 * K, 11.4 * K, 12 * K, 15 * K), 205, 335, fill=BROW + (255,), width=7)
        d.arc((20 * K, 11.4 * K, 28 * K, 15 * K), 205, 335, fill=BROW + (255,), width=7)
    # ojos grandes
    for side, x0 in ((-1, 3.0), (1, 19.0)):
        e = eye_layer(expr, side)
        e = e.resize((int(10 * K), int(12 * K)), Image.LANCZOS)
        big.alpha_composite(e, (int(x0 * K), int(13.4 * K)))
    # nariz: un toque de sombra
    d.line((15.6 * K, 24.2 * K, 16.6 * K, 25.0 * K), fill=SKIN_DK + (200,), width=5)
    # boca
    if expr in ("normal", "blink"):
        d.arc((13.4 * K, 25.6 * K, 18.6 * K, 28.8 * K), 20, 160, fill=LIP + (255,), width=7)
    elif expr == "happy":
        d.chord((13.0 * K, 25.6 * K, 19.0 * K, 30.2 * K), 0, 180, fill=MOUTH + (255,))
        d.chord((14.4 * K, 27.8 * K, 17.6 * K, 30.0 * K), 180, 360, fill=(232, 120, 132, 255))
        d.line((13.4 * K, 26.4 * K, 18.6 * K, 26.4 * K), fill=(250, 246, 246, 255), width=5)
    elif expr == "surprised":
        d.ellipse((14.6 * K, 25.8 * K, 17.4 * K, 29.0 * K), fill=MOUTH + (255,))
    elif expr == "focused":
        d.line((14.2 * K, 27.0 * K, 17.8 * K, 26.8 * K), fill=LIP + (255,), width=6)
    elif expr == "tired":
        d.line((14.0 * K, 27.4 * K, 15.6 * K, 26.8 * K, 17.0 * K, 27.4 * K, 18.2 * K, 26.9 * K), fill=LIP + (255,), width=5)
        d.ellipse((26.0 * K, 15.0 * K, 27.6 * K, 17.4 * K), fill=(170, 210, 240, 220))      # gotita
    small = big.resize((32, 32), Image.LANCZOS)
    return small


def skin(expr):
    global AO
    AO = False
    rnd.seed(7)
    img = Image.new("RGBA", (64 * S, 64 * S), T)
    paint_region(img, 0, 0, 8, 8, 8, head_paint)
    AO = True
    face = face_image(expr)
    img.alpha_composite(face, (8 * S, 8 * S))
    paint_region(img, 16, 16, 8, 12, 4, body_paint)
    paint_region(img, 40, 16, 3, 12, 4, arm_paint)          # brazo derecho (delgado)
    paint_region(img, 32, 48, 3, 12, 4, arm_paint)          # brazo izquierdo
    paint_region(img, 0, 16, 4, 12, 4, leg_paint)
    paint_region(img, 16, 48, 4, 12, 4, leg_paint)
    AO = True
    return img


# ================================================================= salida
def fnum(v):
    s = ("%.4f" % v).rstrip("0").rstrip(".")
    return ("0" if s in ("-0", "") else s) + "F"


def java_layer(method, parts, anchors, tw, th):
    L = ["    public static LayerDefinition %s() {" % method,
         "        MeshDefinition mesh = new MeshDefinition();",
         "        PartDefinition root = mesh.getRoot();"]
    var = {}
    for a, piv in anchors:
        L.append('        PartDefinition p_%s = root.addOrReplaceChild("%s", CubeListBuilder.create(), PartPose.offset(%s, %s, %s));'
                 % (a, a, fnum(piv[0]), fnum(piv[1]), fnum(piv[2])))
        var[a] = "p_" + a
    for p in parts:
        cubes = "CubeListBuilder.create()"
        for b in p.boxes:
            cubes += ".texOffs(%d, %d).addBox(%s, %s, %s, %s, %s, %s)" % (b.u, b.v, fnum(b.x), fnum(b.y), fnum(b.z),
                                                                        fnum(b.w), fnum(b.h), fnum(b.d))
        L.append('        PartDefinition p_%s = %s.addOrReplaceChild("%s", %s, PartPose.offsetAndRotation(%s, %s, %s, %s, %s, %s));' % (
            p.name, var[p.parent], p.name, cubes, fnum(p.pivot[0]), fnum(p.pivot[1]), fnum(p.pivot[2]),
            fnum(p.rot[0]), fnum(p.rot[1]), fnum(p.rot[2])))
        var[p.name] = "p_" + p.name
    L += ["        return LayerDefinition.create(mesh, %d, %d);" % (tw, th), "    }"]
    return L


def java_bake(method, parts, anchors):
    L = ["    public static Map<String, ModelPart> %s(ModelPart root) {" % method,
         "        Map<String, ModelPart> m = new LinkedHashMap<>();"]
    for a, _ in anchors:
        L.append('        m.put("%s", root.getChild("%s"));' % (a, a))
    for p in parts:
        L.append('        m.put("%s", m.get("%s").getChild("%s"));' % (p.name, p.parent, p.name))
    L += ["        return m;", "    }"]
    return L


def build_texture(parts, w, h):
    rnd.seed(3)
    img = Image.new("RGBA", (w * S, h * S), T)
    for p in parts:
        for b in p.boxes:
            paint_region(img, b.u, b.v, b.w, b.h, b.d, b.paint)
    return img


def main():
    os.makedirs(TEX, exist_ok=True)
    hparts, hanchors = hair_parts()
    hw, hh = pack(hparts, 64)
    oparts, oanchors = outfit_parts()
    ow, oh = pack(oparts, 64)
    build_texture(hparts, hw, hh).save(os.path.join(TEX, "hair.png"))
    strand_texture().save(os.path.join(TEX, "hair_strand.png"))
    build_texture(oparts, ow, oh).save(os.path.join(TEX, "outfit.png"))
    for expr, name in [("normal", "tanny"), ("blink", "tanny_blink"), ("happy", "tanny_happy"),
                       ("surprised", "tanny_surprised"), ("focused", "tanny_focused"), ("tired", "tanny_tired")]:
        skin(expr).save(os.path.join(TEX, name + ".png"))
    out = ["// GENERADO por tools/gen_tanny.py — no editar a mano.", "package com.tanny.tannymaid.client;", "",
           "import net.minecraft.client.model.geom.ModelPart;", "import net.minecraft.client.model.geom.PartPose;",
           "import net.minecraft.client.model.geom.builders.*;", "", "import java.util.LinkedHashMap;", "import java.util.Map;", "",
           "public final class TannyGeneratedModels {", "    private TannyGeneratedModels() {}", "",
           "    public static final float BANG_TILT = %s;" % fnum(BANG_TILT),
           "    public static final float SKIRT_FLARE = %s;" % fnum(SKIRT_FLARE), ""]
    out += java_layer("createHair", hparts, hanchors, hw, hh) + [""]
    out += java_bake("bakeHair", hparts, hanchors) + [""]
    out += java_layer("createOutfit", oparts, oanchors, ow, oh) + [""]
    out += java_bake("bakeOutfit", oparts, oanchors) + ["}", ""]
    open(JAVA, "w").write("\n".join(out))
    print("cabello %dx%d (%d partes) · vestido %dx%d (%d partes)" % (hw, hh, len(hparts), ow, oh, len(oparts)))
    return hparts, hanchors, oparts, oanchors


# ================================================================= vista previa
def rot(v, rx, ry, rz):
    x, y, z = v
    y, z = y * math.cos(rx) - z * math.sin(rx), y * math.sin(rx) + z * math.cos(rx)
    x, z = x * math.cos(ry) + z * math.sin(ry), -x * math.sin(ry) + z * math.cos(ry)
    x, y = x * math.cos(rz) - y * math.sin(rz), x * math.sin(rz) + y * math.cos(rz)
    return (x, y, z)


def chains(parts, anchors):
    tf = {a: [(piv, (0, 0, 0))] for a, piv in anchors}
    for p in parts:
        tf[p.name] = tf[p.parent] + [(p.pivot, p.rot)]
    return tf


def apply_chain(chain, v):
    for piv, r in reversed(chain):
        v = rot(v, *r)
        v = (v[0] + piv[0], v[1] + piv[1], v[2] + piv[2])
    return v


def quads_for(img, x0, y0, z0, w, h, d, u, v, chain, step=2):
    quads = []
    x1, y1, z1 = x0 + w, y0 + h, z0 + d
    corners = {"front": ((x0, y0, z0), (x1, y0, z0), (x0, y1, z0)), "back": ((x1, y0, z1), (x0, y0, z1), (x1, y1, z1)),
               "right": ((x0, y0, z1), (x0, y0, z0), (x0, y1, z1)), "left": ((x1, y0, z0), (x1, y0, z1), (x1, y1, z0)),
               "top": ((x0, y0, z1), (x1, y0, z1), (x0, y0, z0)), "bottom": ((x0, y1, z0), (x1, y1, z0), (x0, y1, z1))}
    shade = {"front": 1.0, "back": 0.75, "left": 0.86, "right": 0.86, "top": 1.08, "bottom": 0.6}
    for f, (a, bb, c) in corners.items():
        fx, fy, fw, fh = face_rects(u, v, w, h, d)[f]
        nx, ny = max(1, round(fw * S / step)), max(1, round(fh * S / step))
        for i in range(nx):
            for j in range(ny):
                px, py = int((fx + (i + 0.5) * fw / nx) * S), int((fy + (j + 0.5) * fh / ny) * S)
                if not (0 <= px < img.width and 0 <= py < img.height):
                    continue
                col = img.getpixel((px, py))
                if col[3] < 128:
                    continue
                L = lambda s, t: tuple(a[k] + (bb[k] - a[k]) * s + (c[k] - a[k]) * t for k in range(3))
                q = [apply_chain(chain, L(i / nx, j / ny)), apply_chain(chain, L((i + 1) / nx, j / ny)),
                     apply_chain(chain, L((i + 1) / nx, (j + 1) / ny)), apply_chain(chain, L(i / nx, (j + 1) / ny))]
                quads.append((q, tuple(int(min(255, ch * shade[f])) for ch in col[:3])))
    return quads


def draw(quads, path, scale, cy, views, size=(300, 600)):
    W, Hh = size
    out = Image.new("RGB", (W * len(views), Hh), (214, 226, 238))
    dr = ImageDraw.Draw(out)
    for k, (yaw, pitch) in enumerate(views):
        proj = []
        for q, col in quads:
            pts, dep = [], 0
            for (x, y, z) in q:
                X = x * math.cos(yaw) - z * math.sin(yaw)
                Z = x * math.sin(yaw) + z * math.cos(yaw)
                Y, Z = y * math.cos(pitch) - Z * math.sin(pitch), y * math.sin(pitch) + Z * math.cos(pitch)
                pts.append((W * k + W / 2 - X * scale, cy + Y * scale))
                dep += Z
            proj.append((dep / 4, pts, col))
        proj.sort(key=lambda t: t[0])
        for _, pts, col in proj:
            dr.polygon(pts, fill=col, outline=col)
    out.save(path)


def preview(outdir, hparts, hanchors, oparts, oanchors, expr="tanny", step=1):
    sk = Image.open(os.path.join(TEX, expr + ".png")).convert("RGBA")
    ht = Image.open(os.path.join(TEX, "hair.png")).convert("RGBA")
    ot = Image.open(os.path.join(TEX, "outfit.png")).convert("RGBA")
    quads = []
    base = [("head", (0, 0, 0), (0, 0), (-4, -8, -4, 8, 8, 8)), ("body", (0, 0, 0), (16, 16), (-4, 0, -2, 8, 12, 4)),
            ("right_arm", (-5, 2.5, 0), (40, 16), (-2, -2, -2, 3, 12, 4)), ("left_arm", (5, 2.5, 0), (32, 48), (-1, -2, -2, 3, 12, 4)),
            ("right_leg", (-1.9, 12, 0), (0, 16), (-2, 0, -2, 4, 12, 4)), ("left_leg", (1.9, 12, 0), (16, 48), (-2, 0, -2, 4, 12, 4))]
    for name, piv, (u, v), bx in base:
        quads += quads_for(sk, *bx, u, v, [(piv, (0, 0, 0))], step)
    for parts, anchors, tex in ((hparts, hanchors, ht), (oparts, oanchors, ot)):
        if parts is oparts:
            anchors = [("body", (0, 0, 0)), ("right_arm", (-5, 2.5, 0)), ("left_arm", (5, 2.5, 0)),
                       ("right_leg", (-1.9, 12, 0)), ("left_leg", (1.9, 12, 0))]
        tf = chains(parts, anchors)
        for p in parts:
            for b in p.boxes:
                quads += quads_for(tex, b.x, b.y, b.z, b.w, b.h, b.d, b.u, b.v, tf[p.name], step)
    draw(quads, os.path.join(outdir, "tanny_preview.png"), 14, 150,
         [(math.pi, 0.05), (math.pi - 0.7, 0.12), (math.pi / 2, 0.05), (-0.6, 0.12)], (300, 520))
    # primer plano de la cara
    head = [qc for qc in quads if min(p[1] for p in qc[0]) < 0.5]
    draw(head, os.path.join(outdir, "tanny_face.png"), 34, 330, [(math.pi, 0.0), (math.pi - 0.55, 0.1)], (420, 420))


if __name__ == "__main__":
    hp, ha, op, oa = main()
    if len(sys.argv) > 2 and sys.argv[1] == "preview":
        preview(sys.argv[2], hp, ha, op, oa, sys.argv[3] if len(sys.argv) > 3 else "tanny")
