"""Totally Waifu? 1.3: estilo cúbico (low-poly Minecraft) muy detallado, con cara de anime.

Base de todas: complexión delgada y atlética, melena bob con flequillo suave y volumen a los lados, ojos grandes
de anime con brillos marcados, orejas puntiagudas de elfa con la punta de color y piel pálida.
  Shiori · bob negro, ojos turquesa, puntas lavanda (la descripción exacta), túnica de maga
  Kozue  · bob castaño, ojos verdes, puntas verde hoja, overol de carpintera
  Homura · bob rojo con dos coletitas, ojos ámbar, puntas naranja, ropa de herrera
  Kura   · bob rubio, ojos azules, puntas rosa, vestido de sirvienta y mochila-cofre

Capas separadas (cada una con su textura):
  skin_*.png    cuerpo y cara (formato skin de jugador "slim", 512x512 = 8 px por píxel de Minecraft; 6 expresiones)
  clothes.png   ropa 3D (piezas c_ = pecho, l_ = piernas, f_ = pies: se ocultan con armadura)
  hair.png      cabello en bloques, orejas de elfa y accesorios de la cabeza
  armadura      la del juego (se dibuja encima si se la pones)
Además exporta skins de jugador 64x64 (skins/<nombre>_skin.png) con la misma apariencia.

Uso: python3 tools/gen_blocky.py [preview DIR]
"""
import math, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_tanny as G
import gen_smooth as SM
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEXR = os.path.join(ROOT, "src/main/resources/assets/totallywaifu/textures/entity")
SKINS = os.path.join(ROOT, "skins")
JAVA_DIR = os.path.join(ROOT, "src/main/java/com/totallywaifu/client")
Part, Box, mix = G.Part, G.Box, G.mix
T = (0, 0, 0, 0)
ANCHORS = [("body", (0, 0, 0)), ("right_arm", (-5, 2.5, 0)), ("left_arm", (5, 2.5, 0)), ("right_leg", (-1.9, 12, 0)), ("left_leg", (1.9, 12, 0))]
HEAD_ANCHOR = [("head", (0, 0, 0))]
SKIN, SKIN_SH, SKIN_HI = (252, 232, 222), (234, 202, 192), (255, 244, 238)       # piel pálida y limpia
EXPRS = [("normal", ""), ("blink", "_blink"), ("happy", "_happy"), ("surprised", "_surprised"), ("focused", "_focused"), ("tired", "_tired")]


# ================================================================== sombreado limpio
K = {"top": 1.08, "bottom": 0.7, "front": 1.0, "back": 0.86, "left": 0.92, "right": 0.92}


def cel(fn, outline=True, grad=0.12):
    def g(f, x, y, w, h):
        c = fn(f, x, y, w, h)
        if c is None or len(c) == 4:
            return c
        k = K[f]
        if f not in ("top", "bottom"):
            k *= 1.04 - grad * (y / max(1, h - 1))
        c = tuple(max(0, min(255, int(v * k))) for v in c)
        if outline and f != "top" and (x == 0 or x == w - 1 or y == h - 1):
            c = tuple(int(v * 0.72) for v in c)
        return c
    return g


def solid(c, **kw):
    return cel(lambda f, x, y, w, h: c, **kw)


def hide(*faces):
    def wrap(fn):
        return lambda f, x, y, w, h: None if f in faces else fn(f, x, y, w, h)
    return wrap


def fold(c, x, n=12, a=0.06):
    k = 1 - a * (0.5 + 0.5 * math.cos(x / n * 2 * math.pi))
    return tuple(int(v * k) for v in c)


def metal(c, hi):
    return cel(lambda f, x, y, w, h: hi if (f == "top" or (x + y) % 9 < 2) else c, outline=False)


def gem(c, hi):
    def fn(f, x, y, w, h):
        if f == "front" and x < w * 0.45 and y < h * 0.45:
            return (255, 255, 255)
        return mix(hi, c, (x + y) / max(1, w + h))
    return fn


def frill(c, sh):
    def fn(f, x, y, w, h):
        if f in ("top", "bottom"):
            return sh
        if y >= h - 2:
            return c if (x // 4) % 2 == 0 else T
        return c if (x // 4) % 2 == 0 else sh
    return fn


def boot(c, hi, sole, lace=None, cuff=None, toe=None):
    def fn(f, x, y, w, h):
        if f == "bottom" or (f != "top" and y >= h - 4):
            return sole
        if f == "top":
            return None
        if cuff and y < 6:
            return cuff
        if toe and f == "front" and y > h * 0.62:
            return toe
        if lace and f == "front" and y > 7 and abs(abs(x - w / 2) - (y % 10) * 0.6) < 0.9 and abs(x - w / 2) < 6:
            return lace                                                   # cordones cruzados finos
        return mix(c, hi, abs((x % 16) / 16.0 - 0.5) * 0.7)
    return cel(fn)


def wood(c, hi, dk):
    def fn(f, x, y, w, h):
        v = math.sin(x * 0.28 + math.sin(y * 0.12) * 2.0)
        return mix(c, hi, 0.5 + 0.5 * v) if v > 0 else mix(c, dk, -v * 0.6)
    return fn


def finish(P):
    m = G.mirror([p for p in P if p.name.endswith("_r") and p.parent in ("right_arm", "right_leg", "body", "head", "goggles")])
    for p in m:
        p.parent = p.parent.replace("right", "left")
    return P + m


def skirt(prefix, paint, hem, flare=0.3, length=5.4, y=8.2, w_front=8.7, z=2.25, x=4.35):
    P = []

    def panel(name, pivot, rot, w):
        return Part(prefix + name, "body", pivot, rot, [Box(-w / 2, 0, -0.45, w, length, 0.9, paint),
                                                        Box(-w / 2 + 0.1, length - 0.35, -0.3, w - 0.2, 0.85, 0.7, hem)])
    P.append(panel("skirt_front", (0, y, -z), (-flare, 0, 0), w_front))
    P.append(panel("skirt_back", (0, y, z), (-flare, math.pi, 0), w_front))
    P.append(panel("skirt_right", (-x, y, 0), (-flare, math.pi / 2, 0), 4.9))
    P.append(panel("skirt_left", (x, y, 0), (-flare, -math.pi / 2, 0), 4.9))
    for name, sx, sz, yaw in (("skirt_fr", -3.7, -1.85, math.pi / 4), ("skirt_fl", 3.7, -1.85, -math.pi / 4),
                              ("skirt_br", -3.7, 1.85, 3 * math.pi / 4), ("skirt_bl", 3.7, 1.85, -3 * math.pi / 4)):
        P.append(panel(name, (sx, y, sz), (-flare, yaw, 0), 3.1))
    return P


def bow(name, parent, pivot, rot, paint, s=1.0):
    return [Part(name + "_knot", parent, pivot, rot, [Box(-0.55 * s, -0.55 * s, -0.4 * s, 1.1 * s, 1.1 * s, 0.8 * s, paint)]),
            Part(name + "_wing_a", name + "_knot", (-0.4 * s, 0, 0), (0, 0.2, 0.3), [Box(-2.0 * s, -0.9 * s, -0.35 * s, 2.0 * s, 1.8 * s, 0.7 * s, paint)]),
            Part(name + "_wing_b", name + "_knot", (0.4 * s, 0, 0), (0, -0.2, -0.3), [Box(0, -0.9 * s, -0.35 * s, 2.0 * s, 1.8 * s, 0.7 * s, paint)]),
            Part(name + "_tail_a", name + "_knot", (-0.2 * s, 0.4 * s, 0), (0, 0, 0.25), [Box(-0.45 * s, 0, -0.25 * s, 0.9 * s, 2.4 * s, 0.5 * s, paint)]),
            Part(name + "_tail_b", name + "_knot", (0.2 * s, 0.4 * s, 0), (0, 0, -0.25), [Box(-0.45 * s, 0, -0.25 * s, 0.9 * s, 2.4 * s, 0.5 * s, paint)])]


# ================================================================== cabello bob en bloques + orejas de elfa
def hair_paint(c, hi, dk):
    """Cabello limpio: mechones verticales suaves, anillo de brillo (estilo anime) y puntas más oscuras."""
    def fn(f, x, y, w, h):
        if f == "bottom":
            return dk
        if f == "top":
            return mix(c, hi, 0.25 + 0.15 * math.sin(x * 0.4))
        t = y / max(1, h - 1)
        strand = 0.5 + 0.5 * math.sin(x * 0.55 + math.sin(x * 0.13) * 1.5)
        col = mix(c, dk, 0.25 * strand)
        if 0.18 < t < 0.34 and (x // 3) % 4 != 0:
            col = mix(col, hi, 0.55 * (1 - abs(t - 0.26) / 0.08))     # anillo de brillo
        if t > 0.7:
            col = mix(col, dk, (t - 0.7) * 1.2)
        if f != "top" and (y >= h - 2):
            col = mix(col, (0, 0, 0), 0.25)
        return col
    return fn


def ear_paint(tip, inner):
    def fn(f, x, y, w, h):
        if f == "front" and 0.2 * w < x < 0.85 * w and 0.25 * h < y < 0.85 * h:
            return inner
        return SKIN if f != "bottom" else SKIN_SH
    return fn


def bob(hair, tip, inner, extra_len=0.0):
    c, hi, dk = hair
    hp = cel(hair_paint(c, hi, dk), grad=0.0)
    L = extra_len
    P = [Part("h_top", "head", (0, 0, 0), (0, 0, 0), [Box(-4.55, -8.6, -4.55, 9.1, 2.7, 9.1, hp)]),
         Part("h_back", "head", (0, -7.2, 1.5), (0.06, 0, 0), [Box(-4.75, 0, 0, 9.5, 6.8 + L, 3.3, hp)]),
         Part("h_side_r", "head", (-4.35, -6.8, 0.0), (0, 0, 0.07), [Box(-1.0, 0, -3.2, 1.5, 5.9 + L, 6.6, hp)]),
         Part("h_frame_r", "head", (-3.7, -7.6, -3.9), (-0.05, 0, 0.06), [Box(-1.05, 0, -0.65, 1.7, 6.9 + L, 1.2, hp)]),
         Part("h_flare_r", "head", (-4.75, -1.6 + L, 0.2), (0, 0, 0.4), [Box(-1.1, 0, -3.3, 1.3, 1.5, 6.6, hp)]),
         Part("h_flare_back", "head", (0, -1.0 + L, 4.5), (-0.4, 0, 0), [Box(-4.6, 0, -1.2, 9.2, 1.4, 1.3, hp)]),
         Part("h_tuft_a", "head", (1.0, -8.5, -1.2), (0.35, 0.3, 0.5), [Box(-0.6, -1.7, -0.6, 1.2, 1.9, 1.2, hp)]),
         Part("h_tuft_b", "head", (-1.6, -8.4, 2.0), (-0.4, -0.4, -0.45), [Box(-0.6, -1.5, -0.6, 1.2, 1.7, 1.2, hp)]),
         Part("h_crown", "head", (0, -8.6, 0), (0, 0, 0), [Box(-3.6, -0.5, -3.6, 7.2, 0.6, 7.2, hp)])]
    # flequillo suave en mechones de distintos largos
    for i, (x, ln, rz) in enumerate(((-3.55, 2.7, 0.08), (-2.4, 3.3, 0.05), (-1.2, 2.9, 0.02), (0.0, 3.6, 0.0), (1.2, 3.0, -0.03),
                                     (2.4, 3.3, -0.05), (3.55, 2.6, -0.08))):
        P.append(Part("h_bang_%d" % i, "head", (x, -8.3, -4.45), (-0.14, 0, rz), [Box(-0.68, 0, -0.38, 1.36, ln, 0.76, hp)]))
    # orejas puntiagudas de elfa que salen entre el cabello
    ep = cel(ear_paint(tip, inner), grad=0.0)
    tp = cel(lambda f, x, y, w, h: tip if f != "bottom" else mix(tip, (0, 0, 0), 0.25), grad=0.0)
    P.append(Part("ear_r", "head", (-3.6, -3.6, 0.6), (0.05, 0.38, -0.36), [Box(-1.9, -1.2, -0.5, 2.3, 2.6, 1.0, ep),
                                                                           Box(-3.3, -1.8, -0.42, 1.5, 2.0, 0.84, ep),
                                                                           Box(-4.6, -2.5, -0.32, 1.4, 1.3, 0.64, tp)]))
    return P


# ================================================================== base de la skin (cara + cuerpo)
def head_paint(hair):
    c, hi, dk = hair

    def fn(f, x, y, w, h):
        if f == "top":
            return hi
        if f == "bottom":
            return SKIN_SH
        if f == "back":
            return c
        if f in ("left", "right"):
            front = x > w * 0.55 if f == "right" else x < w * 0.45
            return SKIN_SH if (front and y > h * 0.55) else c
        return SKIN
    return fn


def paint_skin(painters, hair, face_img):
    """Skin de jugador slim a 8 px por píxel (512x512): cabeza, cuerpo, brazos y piernas."""
    G.S = 8
    G.AO = False
    img = Image.new("RGBA", (512, 512), T)
    body, arm, leg = painters
    G.paint_region(img, 0, 0, 8, 8, 8, cel(head_paint(hair), outline=False, grad=0.0))
    G.paint_region(img, 16, 16, 8, 12, 4, cel(body))
    G.paint_region(img, 40, 16, 3, 12, 4, cel(arm))
    G.paint_region(img, 32, 48, 3, 12, 4, cel(arm))
    G.paint_region(img, 0, 16, 4, 12, 4, cel(leg))
    G.paint_region(img, 16, 48, 4, 12, 4, cel(leg))
    img.paste(face_img.resize((64, 64), Image.LANCZOS), (64, 64))
    return img


def pixel_face(skin64, iris, lash, brow, mouth=(206, 110, 120), blush=(250, 180, 186)):
    """Cara de 8x8 para la skin de jugador 64x64 (que se lea bien a resolución de Minecraft)."""
    dk, mid, hi = iris
    px = lambda x, y, c: skin64.putpixel((8 + x, 8 + y), c + (255,))
    for x in range(8):
        for y in range(8):
            px(x, y, SKIN)
    for s_, (a, b) in ((0, (1, 2)), (1, (6, 5))):
        px(a, 3, brow); px(b, 3, brow)
        px(a, 4, lash); px(b, 4, lash)
        px(a - 1 if s_ == 0 else a + 1, 4, lash)
        px(a, 5, hi if False else dk); px(b, 5, (255, 255, 255))
        px(a, 6, mid); px(b, 6, hi)
    px(0, 6, blush); px(7, 6, blush)
    px(3, 7, mouth); px(4, 7, mouth)


# ================================================================== las cuatro
GOLD, GOLD_HI = (230, 186, 70), (255, 232, 140)
IRON, IRON_HI = (164, 170, 180), (220, 226, 234)


def base_painters(top, skirtc, sleeve, glove, sock, sock_from, shoe):
    def body(f, x, y, w, h):
        if f == "front" and y < 6 and 0.35 * w <= x <= 0.65 * w:
            return SKIN
        if y >= 0.6 * h:
            return skirtc
        return fold(top, x)

    def arm(f, x, y, w, h):
        t = y / h
        if f == "top" or t < 0.36:
            return sleeve
        if glove and t > 0.8:
            return glove
        return SKIN

    def leg(f, x, y, w, h):
        t = y / h
        if f == "top" or t < 0.22:
            return skirtc
        if f == "bottom" or t > 0.86:
            return shoe
        if sock and t > sock_from:
            return sock
        return SKIN
    return body, arm, leg


# ---- Kozue (mesa de crafteo)
KZ = ((96, 60, 38), (150, 104, 66), (58, 34, 22))
OAK, OAK_HI, OAK_DK = (186, 140, 86), (214, 172, 118), (130, 92, 54)
CREAM, CREAM_SH = (250, 244, 228), (226, 214, 192)
LEAF, LEAF_HI, LEAF_DK = (92, 168, 74), (132, 204, 100), (56, 112, 44)


def kozue():
    blouse = cel(lambda f, x, y, w, h: fold(CREAM, x, 14))
    overall = cel(wood(OAK, OAK_HI, OAK_DK))
    P = [Part("c_shirt", "body", (0, 0, 0), (0, 0, 0), [Box(-4.25, -0.25, -2.25, 8.5, 7.2, 4.5,
              cel(lambda f, x, y, w, h: LEAF if (f == "front" and y < 10 and abs(x - w / 2) < 12 - y) else fold(CREAM, x, 14)))]),
         Part("c_bust", "body", (0, 2.9, -2.2), (-0.3, 0, 0), [Box(-3.3, 0, -0.9, 6.6, 2.9, 1.0, overall)]),
         Part("c_bib", "body", (0, 0, 0), (0, 0, 0), [Box(-3.1, 3.3, -2.55, 6.2, 4.6, 0.4,
              cel(lambda f, x, y, w, h: OAK_DK if (y < 3 or (abs(y - h * 0.6) < 3 and 0.25 * w < x < 0.75 * w)) else wood(OAK, OAK_HI, OAK_DK)(f, x, y, w, h)))]),
         Part("c_strap_r", "body", (-2.4, 0, 0), (0, 0, 0), [Box(-0.6, -0.35, -2.55, 1.2, 4.0, 5.1, hide("top", "bottom")(solid(OAK_DK)))]),
         Part("c_button_r", "body", (-2.4, 3.6, -2.85), (0, 0, 0), [Box(-0.45, -0.45, -0.2, 0.9, 0.9, 0.35, metal(GOLD, GOLD_HI))]),
         Part("c_puff_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.5, -2.55, -2.5, 4.0, 3.8, 5.0, blouse)]),
         Part("c_glove_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.2, 7.8, -2.2, 3.4, 2.4, 4.4, hide("top", "bottom")(solid(LEAF)))]),
         Part("l_hips", "body", (0, 0, 0), (0, 0, 0), [Box(-4.5, 7.4, -2.45, 9.0, 3.4, 4.9, overall)]),
         Part("l_toolbelt", "body", (0, 0, 0), (0, 0, 0), [Box(-4.65, 7.5, -2.6, 9.3, 1.3, 5.2,
              cel(lambda f, x, y, w, h: GOLD if f == "front" and abs(x - w / 2) < 5 else (98, 64, 38)))]),
         Part("l_hammer", "body", (4.85, 8.1, -0.5), (0, 0, -0.16), [Box(-0.3, 0, -0.3, 0.6, 3.8, 0.6, solid((128, 86, 50))),
                                                                     Box(-0.7, 3.6, -1.2, 1.4, 1.1, 2.4, metal(IRON, IRON_HI))]),
         Part("l_pouch_r", "body", (-3.4, 8.2, -2.65), (0, 0.22, 0), [Box(-1.1, 0, -1.0, 2.2, 2.4, 1.2,
              cel(lambda f, x, y, w, h: (84, 54, 30) if y < h * 0.35 else (112, 74, 44)))]),
         Part("f_boot_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.3, 6.6, -2.3, 4.6, 5.65, 4.6, boot((118, 76, 44), (150, 100, 62), (52, 36, 28), lace=CREAM, cuff=(96, 62, 36)))]),
         Part("f_toe_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.3, 10.25, -3.05, 4.6, 2.0, 0.8, boot((118, 76, 44), (150, 100, 62), (52, 36, 28)))])]
    P += skirt("l_", cel(lambda f, x, y, w, h: OAK_DK if y < 3 else fold(OAK, x, 10, 0.12)), solid(OAK_DK), flare=0.34, length=4.6)
    head = [Part("bandana", "head", (0, -7.0, 0), (0, 0, 0), [Box(-4.7, -1.2, -4.7, 9.4, 1.4, 9.4,
                 hide("top", "bottom")(cel(lambda f, x, y, w, h: LEAF_DK if (x // 6 + y // 6) % 4 == 0 else LEAF)))]),
            Part("knot", "head", (2.0, -6.8, 4.8), (0.3, 0, 0.3), [Box(-0.9, -0.9, -0.5, 1.8, 1.8, 1.0, solid(LEAF)),
                                                                  Box(-1.6, 0.4, 0.0, 1.1, 2.6, 0.45, solid(LEAF_HI)),
                                                                  Box(0.4, 0.4, 0.0, 1.1, 2.3, 0.45, solid(LEAF_HI))]),
            Part("pencil", "head", (-4.9, -4.6, -0.6), (0.25, 0, 0.2), [Box(-0.28, -0.28, -2.1, 0.56, 0.56, 4.2,
                 lambda f, x, y, w, h: (60, 50, 40) if x < 6 else (236, 196, 60))])]
    return finish(P), finish(bob(KZ, LEAF, (250, 200, 196)) + head), \
        base_painters(CREAM, OAK, CREAM, LEAF, (78, 56, 44), 0.42, (118, 76, 44))


# ---- Homura (horno)
HM = ((200, 50, 36), (255, 120, 70), (128, 24, 20))
CHAR, CHAR_HI = (48, 44, 52), (74, 68, 80)
FIRE1, FIRE2, FIRE3 = (255, 222, 100), (250, 144, 42), (218, 64, 30)
LEATHER, LEATHER_DK = (114, 72, 46), (76, 48, 30)
STONE, STONE_HI, STONE_DK = (124, 124, 130), (158, 158, 164), (86, 86, 94)


def cobble(f, x, y, w, h):
    bx, by = (x + (16 if (y // 16) % 2 else 0)) % 32, y % 16
    if bx == 0 or by == 0:
        return STONE_DK
    return mix(STONE, STONE_HI, ((x // 8 * 7 + y // 8 * 13) % 11) / 22.0)


def flames(base):
    def fn(f, x, y, w, h):
        if f in ("top", "bottom"):
            return base(f, x, y, w, h)
        edge = h - (6 + 9 * abs(math.sin(x * 0.2)))
        if y >= edge:
            t = (y - edge) / max(1, h - edge)
            return FIRE1 if t > 0.72 else FIRE2 if t > 0.35 else FIRE3
        return base(f, x, y, w, h)
    return fn


def homura():
    top = cel(lambda f, x, y, w, h: FIRE2 if (y < 4 and f == "front") else fold(CHAR, x, 10))
    leather = cel(lambda f, x, y, w, h: LEATHER)
    P = [Part("c_top", "body", (0, 0, 0), (0, 0, 0), [Box(-4.25, 0.6, -2.25, 8.5, 4.4, 4.5, top)]),
         Part("c_bust", "body", (0, 2.9, -2.2), (-0.3, 0, 0), [Box(-3.3, 0, -0.9, 6.6, 2.9, 1.0, top)]),
         Part("c_corset", "body", (0, 0, 0), (0, 0, 0), [Box(-4.35, 4.8, -2.35, 8.7, 2.8, 4.7,
              cel(lambda f, x, y, w, h: FIRE2 if (f == "front" and abs(x - w / 2) < 3 and (y // 4) % 2 == 0) else (LEATHER_DK if f == "front" else (36, 32, 40))))]),
         Part("c_choker", "body", (0, 0, 0), (0, 0, 0), [Box(-1.6, -0.9, -1.5, 3.2, 0.7, 3.0, hide("top", "bottom")(solid((40, 30, 34))))]),
         Part("c_ember", "body", (0, -0.5, -1.65), (0, 0, 0.78), [Box(-0.42, -0.42, -0.3, 0.84, 0.84, 0.45, gem(FIRE3, FIRE1))]),
         Part("c_gauntlet_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.3, 5.4, -2.3, 3.6, 4.8, 4.6, hide("top")(cel(lambda f, x, y, w, h:
              (150, 156, 166) if y < 5 else LEATHER)))]),
         Part("l_hips", "body", (0, 0, 0), (0, 0, 0), [Box(-4.5, 7.4, -2.45, 9.0, 3.4, 4.9, cel(cobble))]),
         Part("l_belt", "body", (0, 0, 0), (0, 0, 0), [Box(-4.6, 7.4, -2.55, 9.2, 1.0, 5.1, solid(LEATHER_DK))]),
         Part("l_tongs", "body", (4.9, 8.0, 0.3), (0, 0, -0.14), [Box(-0.24, 0, -0.5, 0.48, 4.2, 0.38, metal((64, 64, 70), IRON_HI)),
                                                                  Box(-0.24, 0, 0.15, 0.48, 4.2, 0.38, metal((64, 64, 70), IRON_HI))]),
         Part("f_boot_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.3, 5.8, -2.3, 4.6, 6.45, 4.6, boot((38, 34, 40), (66, 62, 72), (50, 50, 56), lace=FIRE2, cuff=(120, 124, 134)))]),
         Part("f_toe_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.3, 10.25, -3.05, 4.6, 2.0, 0.8, solid((128, 132, 142)))])]
    P += skirt("l_", cel(flames(cobble)), solid(FIRE2), flare=0.3, length=5.6)

    def apron(f, x, y, w, h):
        cx, cy = w / 2, h * 0.45
        if f == "front" and ((x - cx) ** 2 / (w * 0.13) ** 2 + (y - cy) ** 2 / (h * 0.2) ** 2) < 1:
            return FIRE2
        return LEATHER
    P.append(Part("l_apron", "l_skirt_front", (0, 0, -0.5), (0, 0, 0), [Box(-3.0, 0.1, -0.35, 6.0, 5.4, 0.35, cel(apron))]))
    flame = cel(lambda f, x, y, w, h: FIRE1 if y > h * 0.6 else FIRE2 if y > h * 0.3 else FIRE3, outline=False)
    hp = cel(hair_paint(*HM), grad=0.0)
    head = [Part("goggles", "head", (0, -8.4, -2.8), (-0.4, 0, 0), [Box(-4.7, -0.4, -0.3, 9.4, 0.8, 0.6, solid((40, 32, 30)))]),
            Part("lens_r", "goggles", (-1.8, 0, -0.5), (0, 0.1, 0), [Box(-1.25, -1.0, -0.6, 2.5, 2.0, 1.2,
                 lambda f, x, y, w, h: (255, 220, 160) if f == "front" and x < w * 0.35 and y < h * 0.35 else (226, 120, 40) if f == "front" else (110, 90, 60))]),
            Part("clip_r", "head", (-3.6, -6.8, -4.6), (0, 0, -0.35), [Box(-0.7, -1.8, -0.35, 1.4, 2.3, 0.7, flame)]),
            Part("tail_r", "head", (-4.0, -6.2, 3.6), (0.5, 0, 0.55), [Box(-0.9, 0, -0.9, 1.8, 4.4, 1.8, hp), Box(-0.7, 4.2, -0.7, 1.4, 1.6, 1.4, hp)]),
            Part("tie_r", "head", (-4.0, -6.2, 3.6), (0, 0, 0), [Box(-1.0, -1.0, -1.0, 2.0, 1.4, 2.0, solid(FIRE3))])]
    return finish(P), finish(bob(HM, FIRE2, (250, 196, 186)) + head), \
        base_painters(CHAR, STONE, SKIN, LEATHER, (34, 30, 36), 0.36, (38, 34, 40))


# ---- Kura (cofre)
KR = ((246, 214, 130), (255, 242, 196), (196, 156, 78))
CHEST, CHEST_HI, CHEST_DK = (166, 112, 54), (198, 144, 76), (106, 70, 32)
WHITE, WHITE_SH = (252, 250, 247), (226, 222, 222)
PINK = (246, 150, 178)


def kura():
    dress = cel(lambda f, x, y, w, h: fold(CHEST, x, 9, 0.1))
    white = cel(lambda f, x, y, w, h: WHITE)
    bodice = cel(lambda f, x, y, w, h: WHITE if f == "front" and 0.28 * w < x < 0.72 * w else fold(CHEST, x, 9, 0.1))
    P = [Part("c_dress", "body", (0, 0, 0), (0, 0, 0), [Box(-4.25, -0.25, -2.25, 8.5, 7.5, 4.5, bodice)]),
         Part("c_bust", "body", (0, 2.9, -2.2), (-0.3, 0, 0), [Box(-3.3, 0, -0.9, 6.6, 2.9, 1.0, bodice)]),
         Part("c_collar", "body", (0, 0, 0), (0, 0, 0), [Box(-4.35, -0.45, -2.35, 8.7, 1.1, 4.7, hide("bottom")(white))]),
         Part("c_latch", "body", (0, 1.5, -2.7), (0, 0, 0), [Box(-0.85, -0.4, -0.4, 1.7, 2.1, 0.55, metal(GOLD, GOLD_HI)),
                                                            Box(-0.32, 0.35, -0.55, 0.64, 0.64, 0.2, solid((60, 40, 20), outline=False))]),
         Part("c_sash", "body", (0, 0, 0), (0, 0, 0), [Box(-4.4, 6.8, -2.4, 8.8, 1.0, 4.8, white)]),
         Part("c_puff_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.55, -2.55, -2.55, 4.1, 4.0, 5.1, cel(lambda f, x, y, w, h:
              WHITE if y >= h - 5 else fold(CHEST, x, 8, 0.12)))]),
         Part("c_cuff_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.25, 7.6, -2.25, 3.5, 1.2, 4.5, frill(WHITE, WHITE_SH))]),
         Part("l_hips", "body", (0, 0, 0), (0, 0, 0), [Box(-4.5, 7.4, -2.45, 9.0, 3.4, 4.9, dress)]),
         Part("l_petticoat", "body", (0, 0, 0), (0, 0, 0), [Box(-4.9, 11.6, -2.8, 9.8, 1.2, 5.6, hide("top")(frill(WHITE, WHITE_SH)))]),
         Part("f_shoe_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.2, 9.9, -2.75, 4.4, 2.35, 4.95, boot((76, 46, 30), (112, 72, 48), (40, 26, 20), cuff=(76, 46, 30)))])]

    def chestbox(f, x, y, w, h):
        if y < 6 or abs(y - h * 0.38) < 3:
            return CHEST_DK
        if f == "back" and abs(x - w / 2) < 5 and abs(y - h * 0.45) < 7:
            return GOLD
        return wood(CHEST, CHEST_HI, CHEST_DK)(f, x, y, w, h)
    P.append(Part("c_backpack", "body", (0, 1.2, 2.3), (0.05, 0, 0), [Box(-3.4, 0, 0, 6.8, 6.0, 3.1, cel(chestbox))]))
    P.append(Part("c_pack_strap_r", "body", (-2.3, 0, 0), (0, 0, 0), [Box(-0.5, -0.4, -2.45, 1.0, 5.2, 4.9, hide("top", "bottom")(solid((96, 62, 30))))]))
    P += bow("c_back_bow", "body", (0, 7.3, 2.45), (0, 0, 0), white, 1.3)
    P += skirt("l_", dress, frill(WHITE, WHITE_SH), flare=0.32, length=5.8)
    P.append(Part("l_apron", "l_skirt_front", (0, 0, -0.5), (0, 0, 0), [Box(-3.0, 0.2, -0.35, 6.0, 4.8, 0.35, white),
                                                                         Box(-3.25, 4.9, -0.4, 6.5, 0.9, 0.45, frill(WHITE, WHITE_SH))]))
    head = [Part("headdress", "head", (0, -8.4, -1.6), (-0.45, 0, 0), [Box(-3.8, -1.6, -0.4, 7.6, 1.8, 0.8, frill(WHITE, WHITE_SH))]),
            Part("headband", "head", (0, -7.9, 0), (0, 0, 0), [Box(-4.75, -0.45, -1.4, 9.5, 0.9, 2.8, hide("top", "bottom")(solid(CHEST_DK)))])]
    head += bow("hair_bow", "head", (3.6, -7.6, 1.8), (0, -0.6, 0), solid(CHEST_DK), 0.8)
    return finish(P), finish(bob(KR, PINK, (250, 196, 200), extra_len=0.6) + head), \
        base_painters(CHEST, CHEST, CHEST, None, WHITE, 0.36, (76, 46, 30))


# ---- Shiori (mesa de encantamientos) — la descripción exacta
SH = ((26, 24, 34), (74, 72, 96), (12, 11, 16))                 # negro con brillo frío
LAV = (190, 150, 236)
OBS, OBS_HI, OBS_DK = (42, 32, 62), (74, 56, 108), (24, 18, 38)
DIA, DIA_HI, DIA_DK = (96, 224, 218), (204, 255, 250), (52, 152, 152)
RED, RED_DK = (182, 42, 56), (124, 26, 40)


def shiori():
    robe = cel(lambda f, x, y, w, h: DIA if (f == "front" and abs(x - w / 2) < 2) else fold(OBS, x, 12, 0.1))
    rune = cel(lambda f, x, y, w, h: DIA if (f in ("front", "back") and y > h - 9 and (x // 6) % 2 == 0) else fold(OBS, x, 12, 0.1))
    P = [Part("c_robe", "body", (0, 0, 0), (0, 0, 0), [Box(-4.25, -0.25, -2.25, 8.5, 8.0, 4.5, robe)]),
         Part("c_bust", "body", (0, 2.9, -2.2), (-0.3, 0, 0), [Box(-3.3, 0, -0.9, 6.6, 2.9, 1.0, robe)]),
         Part("c_collar", "body", (0, 0, 0), (0, 0, 0), [Box(-2.6, -1.5, -2.45, 5.2, 1.8, 4.9, hide("top", "bottom")(solid(DIA_DK)))]),
         Part("c_brooch", "body", (0, 1.0, -2.75), (0, 0, 0.78), [Box(-0.68, -0.68, -0.35, 1.36, 1.36, 0.55, gem(DIA, DIA_HI))]),
         Part("c_sash", "body", (0, 0, 0), (0, 0, 0), [Box(-4.4, 6.9, -2.4, 8.8, 1.1, 4.8, solid(RED))]),
         Part("c_cape", "body", (0, -0.2, 2.45), (0.12, 0, 0), [Box(-4.6, 0, 0, 9.2, 15.2, 0.45,
              cel(lambda f, x, y, w, h: RED_DK if (y >= h - 6 or x % 24 == 0) else RED, grad=0.25))]),
         Part("c_bell_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.65, 5.4, -2.65, 4.3, 3.4, 5.3,
              hide("top", "bottom")(cel(lambda f, x, y, w, h: DIA if y >= h - 4 else OBS_HI)))]),
         Part("l_hips", "body", (0, 0, 0), (0, 0, 0), [Box(-4.5, 7.4, -2.45, 9.0, 3.4, 4.9, robe)]),
         Part("f_boot_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.3, 6.2, -2.3, 4.6, 6.05, 4.6, boot(OBS, OBS_HI, (20, 16, 28), lace=DIA, cuff=DIA_DK))]),
         Part("f_toe_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.3, 10.25, -3.05, 4.6, 2.0, 0.8, boot(OBS, OBS_HI, (20, 16, 28)))])]
    P += skirt("l_", rune, solid(DIA), flare=0.22, length=8.2)
    head = [Part("circlet", "head", (0, -7.2, 0), (0, 0, 0), [Box(-4.72, -0.4, -4.72, 9.44, 0.75, 9.44, hide("top", "bottom")(metal((206, 174, 70), GOLD_HI)))]),
            Part("tiara_gem", "head", (0, -7.35, -4.95), (0, 0, 0.78), [Box(-0.8, -0.8, -0.35, 1.6, 1.6, 0.6, gem(DIA, DIA_HI))]),
            Part("earring_r", "head", (-6.6, -2.4, 0.4), (0, 0, 0), [Box(-0.25, 0, -0.25, 0.5, 1.3, 0.5, gem(LAV, (240, 220, 255)))])]
    return finish(P), finish(bob(SH, LAV, (232, 190, 236)) + head), \
        base_painters(OBS, OBS, OBS, None, (30, 24, 40), 0.25, OBS)


WAIFUS = {
    "kozue": dict(fn=kozue, hair=KZ, iris=((30, 90, 40), (84, 170, 76), (190, 240, 150)), lash=(50, 28, 20), brow=(80, 50, 30)),
    "homura": dict(fn=homura, hair=HM, iris=((140, 46, 10), (236, 140, 32), (255, 224, 128)), lash=(60, 16, 14), brow=(140, 34, 24), sharp=True),
    "kura": dict(fn=kura, hair=KR, iris=((24, 60, 150), (72, 134, 228), (176, 220, 255)), lash=(100, 66, 34), brow=(184, 144, 74)),
    "shiori": dict(fn=shiori, hair=SH, iris=((10, 92, 108), (40, 196, 200), (190, 255, 250)), lash=(16, 14, 24), brow=(30, 28, 40), blush=(236, 160, 196)),
}


def build(name):
    d = WAIFUS[name]
    out = os.path.join(TEXR, name)
    os.makedirs(out, exist_ok=True)
    for f in os.listdir(out):
        os.remove(os.path.join(out, f))
    clothes, hair, painters = d["fn"]()
    SM.SKIN, SM.SKIN_SH, SM.SKIN_HI = SKIN, SKIN_SH, SKIN_HI
    face = SM.make_face(d["iris"], d["lash"], d["brow"], d["hair"], blush=d.get("blush", (255, 160, 170)), sharp=d.get("sharp", False))
    for expr, suf in EXPRS:
        f128 = face(expr).crop((0, 0, 128, 128))
        paint_skin(painters, d["hair"], f128).save(os.path.join(out, "skin" + suf + ".png"))
    G.S, G.AO = 8, False
    G.jit = lambda c, a=4: c
    cw, ch = G.pack(clothes, 64)
    G.build_texture(clothes, cw, ch).save(os.path.join(out, "clothes.png"))
    hw, hh = G.pack(hair, 64)
    G.build_texture(hair, hw, hh).save(os.path.join(out, "hair.png"))
    # skin de jugador 64x64 (con la ropa principal pintada encima del cuerpo)
    os.makedirs(SKINS, exist_ok=True)
    big = Image.open(os.path.join(out, "skin.png"))
    small = Image.new("RGBA", (64, 64), T)
    for y in range(64):
        for x in range(64):
            small.putpixel((x, y), big.getpixel((x * 8 + 4, y * 8 + 4)))
    pixel_face(small, d["iris"], d["lash"], d["brow"])
    hc = d["hair"]
    for (u, v, w, h) in ((8, 0, 8, 8), (24, 8, 8, 8), (0, 8, 8, 8), (16, 8, 8, 8)):     # cabello sobre la cabeza (arriba, atrás, lados)
        for yy in range(v, v + h):
            for xx in range(u, u + w):
                if not (u in (0, 16) and v == 8 and yy > 12 and ((u == 0 and xx > 4) or (u == 16 and xx < 19))):
                    small.putpixel((xx, yy), (hc[0] if (xx + yy) % 5 else hc[1]) + (255,))
    for xx in range(8, 16):                                                              # flequillo
        small.putpixel((xx, 8), hc[0] + (255,))
        if xx in (8, 9, 11, 12, 14, 15):
            small.putpixel((xx, 9), hc[0] + (255,))
    small.putpixel((8, 10), hc[0] + (255,)); small.putpixel((15, 10), hc[0] + (255,))
    small.save(os.path.join(SKINS, name + "_skin.png"))
    big.save(os.path.join(SKINS, name + "_skin_hd.png"))
    return clothes, hair, (cw, ch), (hw, hh)


def main():
    java = ["// GENERADO por tools/gen_blocky.py — no editar a mano.", "package com.totallywaifu.client;", "",
            "import net.minecraft.client.model.geom.ModelPart;", "import net.minecraft.client.model.geom.PartPose;",
            "import net.minecraft.client.model.geom.builders.*;", "", "import java.util.LinkedHashMap;", "import java.util.Map;", "",
            "/** Capas de las waifus: ropa (c_ pecho, l_ piernas, f_ pies) y cabello/orejas/accesorios de la cabeza. */",
            "public final class WaifuModels {", "    private WaifuModels() {}", ""]
    for name in WAIFUS:
        clothes, hair, (cw, ch), (hw, hh) = build(name)
        cap = name.capitalize()
        java += G.java_layer("clothes" + cap, clothes, ANCHORS, cw, ch) + [""]
        java += G.java_bake("bakeClothes" + cap, clothes, ANCHORS) + [""]
        java += G.java_layer("hair" + cap, hair, HEAD_ANCHOR, hw, hh) + [""]
        java += G.java_bake("bakeHair" + cap, hair, HEAD_ANCHOR) + [""]
        print("%s: ropa %dx%d (%d piezas) · cabello %dx%d (%d piezas)" % (name, cw, ch, len(clothes), hw, hh, len(hair)))
    java += ["}", ""]
    for f in ("WaifuShapes.java", "WaifuMesh.java", "WaifuHair.java", "WaifuHairMesh.java"):
        p = os.path.join(JAVA_DIR, f)
        if os.path.exists(p):
            os.remove(p)
    open(os.path.join(JAVA_DIR, "WaifuModels.java"), "w").write("\n".join(java))


def preview(outdir, suf=""):
    os.makedirs(outdir, exist_ok=True)
    for name, d in WAIFUS.items():
        out = os.path.join(TEXR, name)
        sk = Image.open(os.path.join(out, "skin%s.png" % suf)).convert("RGBA")
        ct = Image.open(os.path.join(out, "clothes.png")).convert("RGBA")
        ht = Image.open(os.path.join(out, "hair.png")).convert("RGBA")
        clothes, hair, _ = d["fn"]()
        quads = []
        base = [((0, 0, 0), (0, 0), (-4, -8, -4, 8, 8, 8)), ((0, 0, 0), (16, 16), (-4, 0, -2, 8, 12, 4)),
                ((-5, 2.5, 0), (40, 16), (-2, -2, -2, 3, 12, 4)), ((5, 2.5, 0), (32, 48), (-1, -2, -2, 3, 12, 4)),
                ((-1.9, 12, 0), (0, 16), (-2, 0, -2, 4, 12, 4)), ((1.9, 12, 0), (16, 48), (-2, 0, -2, 4, 12, 4))]
        for piv, (u, v), bx in base:
            quads += G.quads_for(sk, *bx, u, v, [(piv, (0, 0, 0))], 1)
        for parts, tex, anchors in ((clothes, ct, ANCHORS), (hair, ht, HEAD_ANCHOR)):
            G.pack(parts, 64)
            tf = G.chains(parts, anchors)
            for p in parts:
                for b in p.boxes:
                    quads += G.quads_for(tex, b.x, b.y, b.z, b.w, b.h, b.d, b.u, b.v, tf[p.name], 1)
        G.draw(quads, os.path.join(outdir, name + "_preview.png"), 13, 140,
               [(math.pi, 0.05), (math.pi - 0.7, 0.12), (math.pi / 2, 0.05), (0.0, 0.08)], (280, 480))
        head = [qc for qc in quads if min(p[1] for p in qc[0]) < 1.0]
        G.draw(head, os.path.join(outdir, name + "_face.png"), 26, 260, [(math.pi, 0.0), (math.pi - 0.6, 0.1)], (330, 330))


if __name__ == "__main__":
    main()
    if len(sys.argv) > 2 and sys.argv[1] == "preview":
        preview(sys.argv[2])
