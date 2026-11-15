"""Totally Waifu? 1.1: modelos estilizados de las 4 waifus (cuerpo propio, no la skin del jugador).

Proporciones de anime: cabeza grande, cuello, pecho, cintura estrecha, cadera ancha, piernas largas y afinadas,
brazos delgados con hombros. Sombreado "cel" (luz desde arriba, contorno oscuro) y caras de anime en alta
resolución (ojos grandes con degradado, brillos y pestañas), con 6 expresiones.

  Kozue  · mesa de crafteo        · carpintera: coleta castaña, pañuelo verde, overol de roble
  Homura · horno                  · herrera: coletas rojo fuego, gafas de forja, top carbón, falda de piedra con llamas
  Kura   · cofre                  · sirvienta: cabello largo rubio, vestido de roble, delantal con encaje, mochila-cofre
  Shiori · mesa de encantamientos · maga: corte hime índigo, túnica de obsidiana, runas de diamante y capa roja

Diseños originales del mod. Uso: python3 tools/gen_waifus.py [preview DIR]
"""
import math, os, random, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_tanny as G
import hair_mesh as H
from PIL import Image, ImageDraw, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEXR = os.path.join(ROOT, "src/main/resources/assets/totallywaifu/textures/entity")
JAVA_DIR = os.path.join(ROOT, "src/main/java/com/totallywaifu/client")
Part, Box, mix = G.Part, G.Box, G.mix
T = (0, 0, 0, 0)
ARM_X, ARM_Y, LEG_X, LEG_Y = 4.45, 1.7, 1.85, 10.5
HEAD_SCALE = 0.88
ANCHORS = [("head", (0, 0, 0)), ("hat", (0, 0, 0)), ("body", (0, 0, 0)), ("right_arm", (-ARM_X, ARM_Y, 0)), ("left_arm", (ARM_X, ARM_Y, 0)),
           ("right_leg", (-LEG_X, LEG_Y, 0)), ("left_leg", (LEG_X, LEG_Y, 0))]
SKIN, SKIN_SH, SKIN_HI = (250, 222, 204), (232, 190, 172), (255, 236, 222)


# ================================================================== sombreado cel
FACE_K = {"top": 1.1, "bottom": 0.66, "front": 1.0, "back": 0.84, "left": 0.9, "right": 0.9}


def cel(fn, outline=True, grad=0.16):
    """Envuelve un pintor: luz desde arriba, caras laterales más oscuras y un contorno fino (estilo Blockbench)."""
    def g(f, x, y, w, h):
        c = fn(f, x, y, w, h)
        if c is None or len(c) == 4:
            return c
        k = FACE_K[f]
        if f not in ("top", "bottom"):
            k *= 1.05 - grad * (y / max(1, h - 1))
        c = tuple(max(0, min(255, int(v * k))) for v in c)
        if outline and f != "top" and (x == 0 or x == w - 1 or y == h - 1):
            c = tuple(int(v * 0.74) for v in c)
        return c
    return g


def solid(c, **kw):
    return cel(lambda f, x, y, w, h: c, **kw)


RAW_SKIN = lambda f, x, y, w, h: SKIN if f != "bottom" else SKIN_SH


def skin(**kw):
    return cel(lambda f, x, y, w, h: SKIN if f != "bottom" else SKIN_SH, **kw)


def pleats(c, hi, dk, n=8):
    return lambda f, x, y, w, h: mix(c, hi if (x // n) % 2 else dk, abs((x % n) / n - 0.5) * 0.9)


def hidden(*faces):
    def wrap(fn):
        return lambda f, x, y, w, h: None if f in faces else fn(f, x, y, w, h)
    return wrap


def band(base, stripes):
    """stripes: [(desde, hasta, color)] en fracción del alto; el resto usa base (un pintor)."""
    def fn(f, x, y, w, h):
        if f in ("top", "bottom"):
            return base(f, x, y, w, h)
        t = y / max(1, h - 1)
        for a, b, c in stripes:
            if a <= t <= b:
                return c
        return base(f, x, y, w, h)
    return fn


def frill(c, sh):
    def fn(f, x, y, w, h):
        if f in ("top", "bottom"):
            return sh
        if y == h - 1:
            return c if (x // 3) % 2 == 0 else T
        return c if (x // 3) % 2 == 0 else sh
    return fn


def metal(c, hi):
    return cel(lambda f, x, y, w, h: hi if (x + y) % 7 == 0 or f == "top" else c, outline=False)


def gem(c, hi):
    def fn(f, x, y, w, h):
        if f == "front" and x < w * 0.45 and y < h * 0.45:
            return (255, 255, 255)
        return mix(hi, c, (x + y) / max(1, w + h))
    return fn


def wood(c, hi, dk):
    def fn(f, x, y, w, h):
        v = math.sin(x * 0.5 + math.sin(y * 0.22) * 2.0)
        return mix(c, hi, 0.5 + 0.5 * v) if v > 0 else mix(c, dk, -v * 0.6)
    return fn


def boot(c, hi, sole=(40, 30, 28), lace=None, cuff=None):
    def fn(f, x, y, w, h):
        if f == "bottom" or (f != "top" and y >= h - 3):
            return sole
        if cuff and y < 4 and f != "top":
            return cuff
        if lace and f == "front" and (y // 4) % 2 == 0 and abs(x - w / 2) < 3 and y > 4:
            return lace
        return mix(c, hi, abs((x % 10) / 10.0 - 0.5) * 0.8)
    return cel(fn)


# ================================================================== figura (cuerpo estilizado)
def figure(top, waist, hips, sleeve, fore, hand, thigh, calf, shoe, bust_paint=None, neck=None):
    """El cuerpo: cuello, pecho, busto, cintura estrecha, cadera ancha, brazos finos y piernas largas afinadas."""
    P = [Part("neck", "body", (0, 0, 0), (0, 0, 0), [Box(-1.15, -1.3, -1.0, 2.3, 1.8, 2.1, neck or skin(outline=False))]),
         Part("chest", "body", (0, 0, 0), (0, 0, 0), [Box(-3.55, 0.0, -1.85, 7.1, 4.6, 3.7, top)]),
         Part("bust", "body", (0, 1.9, -1.8), (-0.34, 0, 0), [Box(-3.1, 0.0, -1.15, 6.2, 2.7, 1.25, bust_paint or top)]),
         Part("waist", "body", (0, 0, 0), (0, 0, 0), [Box(-2.85, 4.5, -1.55, 5.7, 2.8, 3.1, waist)]),
         Part("hips", "body", (0, 0, 0), (0, 0, 0), [Box(-3.65, 7.2, -1.95, 7.3, 3.6, 3.9, hips)]),
         # brazo: hombro redondeado, brazo, antebrazo y mano
         Part("shoulder_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.45, -1.55, -1.4, 2.7, 2.6, 2.8, sleeve)]),
         Part("upper_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.15, 0.9, -1.1, 2.2, 4.2, 2.2, sleeve)]),
         Part("fore_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.05, 5.0, -1.0, 2.0, 4.2, 2.0, fore)]),
         Part("hand_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.1, 9.1, -1.15, 2.0, 2.1, 2.3, hand)]),
         # pierna: muslo ancho que se afina, pantorrilla y pie
         Part("thigh_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-1.95, -0.3, -1.95, 3.85, 6.6, 3.9, thigh)]),
         Part("calf_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-1.55, 6.2, -1.55, 3.05, 5.7, 3.1, calf)]),
         Part("foot_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-1.55, 11.7, -2.45, 3.1, 1.8, 3.9, shoe)])]
    return P


def skirt(paint, hem, flare=0.3, length=5.6, y=7.6):
    P = []

    def panel(name, pivot, rot, w):
        return Part(name, "body", pivot, rot, [Box(-w / 2, 0, -0.45, w, length, 0.9, paint),
                                              Box(-w / 2 + 0.1, length - 0.35, -0.3, w - 0.2, 0.85, 0.7, hem)])
    P.append(panel("skirt_front", (0, y, -2.0), (-flare, 0, 0), 7.6))
    P.append(panel("skirt_back", (0, y, 2.0), (-flare, math.pi, 0), 7.6))
    P.append(panel("skirt_right", (-3.7, y, 0), (-flare, math.pi / 2, 0), 4.3))
    P.append(panel("skirt_left", (3.7, y, 0), (-flare, -math.pi / 2, 0), 4.3))
    for name, x, z, yaw in (("skirt_fr", -3.15, -1.65, math.pi / 4), ("skirt_fl", 3.15, -1.65, -math.pi / 4),
                            ("skirt_br", -3.15, 1.65, 3 * math.pi / 4), ("skirt_bl", 3.15, 1.65, -3 * math.pi / 4)):
        P.append(panel(name, (x, y, z), (-flare, yaw, 0), 2.9))
    return P


def bow(name, parent, pivot, rot, paint, size=1.0):
    s = size
    return [Part(name + "_knot", parent, pivot, rot, [Box(-0.55 * s, -0.55 * s, -0.4 * s, 1.1 * s, 1.1 * s, 0.8 * s, paint)]),
            Part(name + "_wing_r", name + "_knot", (-0.4 * s, 0, 0), (0, 0.2, 0.3), [Box(-2.0 * s, -0.9 * s, -0.35 * s, 2.0 * s, 1.8 * s, 0.7 * s, paint)]),
            Part(name + "_wing_l", name + "_knot", (0.4 * s, 0, 0), (0, -0.2, -0.3), [Box(0, -0.9 * s, -0.35 * s, 2.0 * s, 1.8 * s, 0.7 * s, paint)]),
            Part(name + "_tail_r", name + "_knot", (-0.2 * s, 0.4 * s, 0), (0, 0, 0.25), [Box(-0.45 * s, 0, -0.25 * s, 0.9 * s, 2.4 * s, 0.5 * s, paint)]),
            Part(name + "_tail_l", name + "_knot", (0.2 * s, 0.4 * s, 0), (0, 0, -0.25), [Box(-0.45 * s, 0, -0.25 * s, 0.9 * s, 2.4 * s, 0.5 * s, paint)])]


def finish(P):
    """Refleja las piezas *_r (brazo/pierna derecha) al lado izquierdo."""
    m = G.mirror([p for p in P if p.name.endswith("_r") and p.parent in ("right_arm", "right_leg", "body", "head", "goggles")])
    for p in m:
        p.parent = p.parent.replace("right", "left")
    return P + m


# ================================================================== cara de anime (alta resolución)
def make_face(iris, lash=(34, 20, 26), brow=None, mouth=(196, 96, 110), blush=(255, 160, 170), sharp=False):
    """Ojos grandes de anime: blanco, iris con degradado y aro oscuro, pupila, dos brillos, pestaña gruesa con alita,
    pestaña inferior, cejas finas, rubor con rayitas y boca pequeña. Se dibuja a 4x y se reduce (bordes suaves)."""
    dk, mid, hi = iris
    brow = brow or lash
    SS = 4

    def face(expr):
        W = 64 * SS
        img = Image.new("RGBA", (W, W), SKIN + (255,))
        d = ImageDraw.Draw(img)
        k = lambda v: v * SS
        # sombra bajo el flequillo y mejillas
        for yy in range(k(14)):
            a = 1 - yy / k(14)
            d.line([(0, yy), (W, yy)], fill=mix(SKIN, SKIN_SH, 0.55 * a) + (255,))
        if blush and expr != "focused":
            bl = Image.new("RGBA", (W, W), T)
            bd = ImageDraw.Draw(bl)
            for cx in (k(13), k(51)):
                bd.ellipse([cx - k(7), k(45), cx + k(7), k(52)], fill=blush + (110 if expr == "happy" else 70,))
            bl = bl.filter(ImageFilter.GaussianBlur(k(2)))
            img.alpha_composite(bl)
            d = ImageDraw.Draw(img)
            for cx in (k(13), k(51)):
                for i in range(3):
                    x0 = cx - k(4) + i * k(3)
                    d.line([(x0, k(50)), (x0 + k(2), k(47))], fill=mix(blush, (220, 90, 110), 0.5) + (255,), width=k(1) // 2 + 1)
        for side in (0, 1):
            cx = k(18) if side == 0 else k(46)
            out = -1 if side == 0 else 1                  # hacia afuera de la cara
            ey0, ey1 = k(29), k(46)
            if expr == "surprised":
                ey0 -= k(2)
            # ceja
            by = k(23 if expr != "surprised" else 20)
            if expr == "focused":
                d.line([(cx - out * k(6), by + k(2)), (cx + out * k(6), by - k(1))], fill=brow + (255,), width=k(1) + 2)
            elif expr == "tired":
                d.line([(cx - out * k(6), by - k(1)), (cx + out * k(6), by + k(2))], fill=brow + (255,), width=k(1) + 2)
            else:
                d.arc([cx - k(7), by, cx + k(7), by + k(6)], 200, 340, fill=brow + (255,), width=k(1) + 2)
            if expr in ("blink", "happy"):
                # ojo cerrado: arco (feliz hacia arriba ^, parpadeo hacia abajo)
                if expr == "happy":
                    d.arc([cx - k(7), k(34), cx + k(7), k(46)], 200, 340, fill=lash + (255,), width=k(2))
                else:
                    d.arc([cx - k(7), k(30), cx + k(7), k(42)], 20, 160, fill=lash + (255,), width=k(2))
                    d.line([(cx + out * k(7), k(36)), (cx + out * k(9), k(34))], fill=lash + (255,), width=k(1) + 2)
                continue
            # blanco del ojo
            d.ellipse([cx - k(7), ey0, cx + k(7), ey1], fill=(250, 250, 252, 255))
            # iris con degradado vertical y aro
            iw = k(5) if expr != "surprised" else k(4)
            ix0, ix1 = cx - iw - out * k(0.5), cx + iw - out * k(0.5)
            irisimg = Image.new("RGBA", (W, W), T)
            idr = ImageDraw.Draw(irisimg)
            for yy in range(ey0, ey1 + 1):
                t = (yy - ey0) / max(1, ey1 - ey0)
                c = mix(dk, mid, t * 1.6) if t < 0.6 else mix(mid, hi, (t - 0.6) * 2.5)
                idr.line([(ix0, yy), (ix1, yy)], fill=c + (255,))
            mask = Image.new("L", (W, W), 0)
            ImageDraw.Draw(mask).ellipse([ix0, ey0 + k(1), ix1, ey1], fill=255)
            img.paste(irisimg, (0, 0), mask)
            d = ImageDraw.Draw(img)
            d.ellipse([ix0, ey0 + k(1), ix1, ey1], outline=mix(dk, (0, 0, 0), 0.5) + (255,), width=k(1) // 2 + 1)
            # pupila
            pc = ((ix0 + ix1) // 2, ey0 + k(8))
            pw = k(2) if not sharp else k(1)
            d.ellipse([pc[0] - pw, pc[1] - k(3), pc[0] + pw, pc[1] + k(3)], fill=mix(dk, (0, 0, 0), 0.6) + (255,))
            # brillos
            d.ellipse([ix0 + k(1), ey0 + k(3), ix0 + k(4), ey0 + k(7)], fill=(255, 255, 255, 255))
            d.ellipse([ix1 - k(3), ey1 - k(5), ix1 - k(1), ey1 - k(3)], fill=(255, 255, 255, 230))
            # párpado: entrecerrado (concentrada / cansada)
            lid = ey0 + (k(5) if expr == "focused" else k(7) if expr == "tired" else 0)
            if lid > ey0:
                d.rectangle([cx - k(8), ey0 - k(2), cx + k(8), lid], fill=SKIN + (255,))
            # pestaña superior gruesa con alita hacia afuera
            d.arc([cx - k(8), lid - k(1), cx + k(8), lid + k(12)], 195, 345, fill=lash + (255,), width=k(2))
            d.polygon([(cx + out * k(6), lid + k(2)), (cx + out * k(10), lid - k(1)), (cx + out * k(8), lid + k(4))], fill=lash + (255,))
            # pestaña inferior
            d.line([(cx - k(4), ey1), (cx + k(4), ey1)], fill=mix(lash, SKIN, 0.4) + (255,), width=k(1) // 2 + 1)
        # nariz (un toque) y boca
        d.line([(k(32), k(48)), (k(33), k(49))], fill=SKIN_SH + (255,), width=k(1))
        if expr == "happy":
            d.chord([k(27), k(51), k(37), k(58)], 0, 180, fill=(170, 60, 80, 255))
            d.chord([k(29), k(54), k(35), k(58)], 0, 180, fill=(240, 130, 140, 255))
        elif expr == "surprised":
            d.ellipse([k(29), k(52), k(35), k(58)], fill=(150, 50, 70, 255))
        elif expr == "focused":
            d.line([(k(29), k(55)), (k(35), k(54))], fill=mouth + (255,), width=k(1))
        elif expr == "tired":
            d.arc([k(28), k(54), k(36), k(58)], 200, 340, fill=mouth + (255,), width=k(1))
        else:
            d.arc([k(28), k(51), k(36), k(56)], 20, 160, fill=mouth + (255,), width=k(1))
        return img.resize((64, 64), Image.LANCZOS)
    return face


def head_texture(face_img, hair):
    """Cubo de la cabeza (8x8x8) a 8 px por unidad: la cara de anime al frente, cabello en el resto."""
    c, hi, dk = hair
    img = Image.new("RGBA", (256, 128), T)
    S = 8

    def fill(u, v, w, h, fn):
        for y in range(v * S, (v + h) * S):
            for x in range(u * S, (u + w) * S):
                img.putpixel((x, y), fn(x - u * S, y - v * S, w * S, h * S) + (255,))
    fill(8, 0, 8, 8, lambda x, y, w, h: c if (x + y) % 9 else hi)                           # arriba
    fill(16, 0, 8, 8, lambda x, y, w, h: SKIN_SH)                                           # abajo
    fill(24, 8, 8, 8, lambda x, y, w, h: c if x % 7 else dk)                                # atrás
    for u, front_left in ((0, False), (16, True)):                                          # lados (mandíbula de piel adelante)
        def side(x, y, w, h, fl=front_left):
            front = x > w * 0.55 if not fl else x < w * 0.45
            return SKIN_SH if (front and y > h * 0.55) else (c if x % 6 else dk)
        fill(u, 8, 8, 8, side)
    img.paste(face_img, (64, 64))
    return img


# ================================================================== KOZUE (mesa de crafteo)
KZ_HAIR = ((128, 76, 44), (170, 110, 66), (86, 48, 26))
OAK, OAK_HI, OAK_DK = (182, 138, 86), (212, 170, 116), (128, 90, 52)
CREAM, CREAM_SH = (248, 240, 222), (224, 210, 186)
LEAF, LEAF_HI, LEAF_DK = (86, 160, 70), (126, 196, 96), (54, 108, 42)
SOCK = (74, 52, 40)
GOLD, GOLD_HI = (226, 182, 66), (255, 230, 130)
IRON, IRON_HI = (150, 156, 164), (210, 216, 224)


def kozue():
    blouse = cel(pleats(CREAM, (255, 250, 236), CREAM_SH, 10))
    overall = cel(wood(OAK, OAK_HI, OAK_DK))
    sock = cel(lambda f, x, y, w, h: SOCK if (y // 5) % 6 else mix(SOCK, LEAF, 0.6))
    P = figure(top=blouse, waist=overall, hips=overall, sleeve=blouse, fore=skin(), hand=cel(lambda f, x, y, w, h: LEAF if y < h * 0.6 else SKIN),
               thigh=cel(band(RAW_SKIN, [(0.55, 0.64, LEAF), (0.64, 1.0, SOCK)])), calf=sock,
               shoe=boot((116, 74, 42), (150, 100, 60), lace=CREAM))
    # peto del overol con bolsillo, botones dorados y tirantes cruzados
    P.append(Part("bib", "body", (0, 0, 0), (0, 0, 0), [Box(-2.6, 2.9, -2.35, 5.2, 4.4, 0.4,
              cel(lambda f, x, y, w, h: OAK_DK if (y < 2 or (abs(y - h * 0.55) < 1.2 and 0.25 * w < x < 0.75 * w)) else wood(OAK, OAK_HI, OAK_DK)(f, x, y, w, h)))]))
    P.append(Part("strap_r", "body", (-2.2, 0, 0), (0, 0, 0), [Box(-0.55, -0.3, -2.0, 1.1, 3.4, 4.0, hidden("top", "bottom")(solid(OAK_DK)))]))
    P.append(Part("button_r", "body", (-2.1, 3.3, -2.5), (0, 0, 0), [Box(-0.45, -0.45, -0.2, 0.9, 0.9, 0.35, metal(GOLD, GOLD_HI))]))
    # pañuelo verde al cuello
    P.append(Part("scarf", "body", (0, 0.2, -1.9), (0.25, 0, 0), [Box(-1.6, -0.3, -0.45, 3.2, 2.2, 0.6,
              cel(lambda f, x, y, w, h: LEAF if (x + y) % 6 else LEAF_HI))]))
    P += skirt(cel(lambda f, x, y, w, h: OAK_DK if y < 2 else pleats(OAK, OAK_HI, OAK_DK, 7)(f, x, y, w, h)), solid(OAK_DK), flare=0.34, length=4.4)
    # cinturón de herramientas: martillo, regla y bolsa
    P.append(Part("toolbelt", "body", (0, 0, 0), (0, 0, 0), [Box(-3.95, 7.1, -2.2, 7.9, 1.2, 4.4,
              cel(lambda f, x, y, w, h: GOLD if f == "front" and abs(x - w / 2) < 3 else (96, 62, 36)))]))
    P.append(Part("hammer", "body", (4.1, 7.8, -0.3), (0, 0, -0.18), [Box(-0.28, 0, -0.28, 0.56, 3.6, 0.56, solid((120, 80, 46))),
                                                                     Box(-0.65, 3.4, -1.1, 1.3, 1.0, 2.2, metal(IRON, IRON_HI))]))
    P.append(Part("pouch_r", "body", (-3.0, 7.8, -2.1), (0, 0.25, 0), [Box(-1.0, 0, -1.0, 2.0, 2.2, 1.1,
              cel(lambda f, x, y, w, h: (84, 54, 30) if y < h * 0.35 else (112, 74, 44)))]))
    P.append(Part("ruler", "body", (-4.0, 7.4, 1.0), (0, 0, 0.12), [Box(-0.22, 0, -0.45, 0.44, 4.0, 0.9,
              lambda f, x, y, w, h: (40, 30, 20) if y % 6 == 0 else (236, 200, 90))]))
    # manga abullonada y remangada
    P.append(Part("puff_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.75, -1.8, -1.7, 3.3, 3.4, 3.4, blouse)]))
    P.append(Part("roll_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.4, 4.0, -1.35, 2.7, 1.0, 2.7, hidden("top", "bottom")(solid(CREAM_SH)))]))
    # cabeza: pañuelo-diadema con nudo, lápiz detrás de la oreja y una hoja de adorno
    P.append(Part("bandana", "head", (0, -6.7, 0), (0, 0, 0), [Box(-4.62, -1.3, -4.62, 9.24, 1.5, 9.24,
              hidden("top", "bottom")(cel(lambda f, x, y, w, h: LEAF_DK if (x // 3 + y // 3) % 4 == 0 else LEAF)))]))
    P.append(Part("knot", "head", (1.8, -6.6, 4.65), (0.3, 0, 0.3), [Box(-0.9, -0.9, -0.5, 1.8, 1.8, 1.0, solid(LEAF)),
                                                                    Box(-1.6, 0.4, 0.0, 1.1, 2.6, 0.45, solid(LEAF_HI)),
                                                                    Box(0.4, 0.4, 0.0, 1.1, 2.3, 0.45, solid(LEAF_HI))]))
    P.append(Part("pencil", "head", (-4.65, -3.8, -0.2), (0.25, 0, 0.2), [Box(-0.28, -0.28, -2.1, 0.56, 0.56, 4.2,
              lambda f, x, y, w, h: (60, 50, 40) if x < 3 else (236, 196, 60))]))
    P.append(Part("leaf_clip", "head", (3.3, -6.0, -3.6), (0, 0, 0.6), [Box(-0.6, -1.4, -0.3, 1.2, 2.4, 0.5, cel(lambda f, x, y, w, h: LEAF_HI if x < w / 2 else LEAF))]))
    return finish(P)


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
    for i in range(14):           # coleta alta con vuelo hasta media espalda
        phi = math.pi + math.radians(-30 + i * 4.6) + j(0.03)
        L.append([phi, 0.34 + j(0.03), 0.72 + j(0.04), 11.5 + j(1.2), 0.2 + j(0.05), 0.4 + j(0.12), 2.6, 0.95, 1.4 + j(0.15), 1.0, H.KIND_LONG])
    for s_ in (-1, 1):
        L.append([s_ * math.radians(48), 0.3, math.pi / 2 - 0.05, 6.4, 0.06, 0.35, 1.8, 0.85, 0.55, 1.0, H.KIND_LONG])
        L.append([s_ * math.radians(58), 0.3, math.pi / 2 - 0.03, 4.8, 0.1, 0.35, 1.7, 0.85, 0.75, 1.0, H.KIND_LONG])
        for deg in (80, 94):
            L.append([s_ * math.radians(deg), 0.3, math.pi / 2, 2.4, 0.15, -0.2, 2.2, 0.9, 0.35, 1.0, H.KIND_BOB])
    for deg, hang in ((-40, 2.1), (-30, 2.7), (-19, 2.3), (-8, 2.9), (3, 2.4), (14, 2.8), (25, 2.3), (35, 2.5), (42, 1.9)):
        L.append([math.radians(deg), 0.3, 1.0, hang + j(0.2), 0.12, -0.12, 2.0, 0.8, 0.55, 1.0, H.KIND_SOFTBANG])
    L.append([math.radians(14), 0.0, 0.0, 2.8, 0, 0, 1.0, 0.7, 0.6, 1.0, H.KIND_AHOGE])
    return L


# ================================================================== HOMURA (horno)
HM_HAIR = ((218, 64, 38), (255, 126, 66), (150, 34, 22))
STONE, STONE_HI, STONE_DK = (116, 116, 122), (150, 150, 156), (76, 76, 84)
CHAR, CHAR_HI = (44, 40, 46), (70, 64, 72)
FIRE1, FIRE2, FIRE3 = (255, 220, 96), (250, 142, 40), (216, 62, 30)
LEATHER, LEATHER_HI, LEATHER_DK = (110, 68, 42), (144, 94, 60), (72, 44, 28)


def cobble(f, x, y, w, h):
    bx, by = (x + (8 if (y // 8) % 2 else 0)) % 16, y % 8
    if bx == 0 or by == 0:
        return STONE_DK
    return mix(STONE, STONE_HI, ((x * 7 + y * 13) % 11) / 22.0)


def flames(base):
    def fn(f, x, y, w, h):
        if f in ("top", "bottom"):
            return base(f, x, y, w, h)
        edge = h - (3 + 5 * abs(math.sin(x * 0.42)))
        if y >= edge:
            t = (y - edge) / max(1, h - edge)
            return FIRE1 if t > 0.72 else FIRE2 if t > 0.35 else FIRE3
        return base(f, x, y, w, h)
    return fn


def homura():
    top = cel(lambda f, x, y, w, h: FIRE2 if (y < 3 and f == "front") else (CHAR if (x + y) % 9 else CHAR_HI))
    corset = cel(lambda f, x, y, w, h: FIRE2 if (f == "front" and abs(x - w / 2) < 1.5 and y % 4 < 2) else LEATHER_DK if f == "front" else (34, 30, 36))
    leather = cel(lambda f, x, y, w, h: LEATHER_HI if (x + y) % 11 == 0 else LEATHER)
    stocking = cel(lambda f, x, y, w, h: (30, 26, 30) if (x % 8) else (44, 40, 46))
    P = figure(top=top, waist=corset, hips=cel(cobble), sleeve=skin(), fore=cel(lambda f, x, y, w, h: LEATHER_DK if y < 3 else LEATHER),
               hand=leather, thigh=cel(band(RAW_SKIN, [(0.5, 0.6, FIRE2), (0.6, 1.0, (30, 26, 30))])), calf=stocking,
               shoe=boot((36, 32, 36), (64, 60, 66), lace=FIRE2, cuff=(90, 90, 98)), bust_paint=top)
    P.append(Part("choker", "body", (0, 0, 0), (0, 0, 0), [Box(-1.3, -0.9, -1.15, 2.6, 0.6, 2.3, hidden("top", "bottom")(solid((40, 30, 34))))]))
    P.append(Part("ember", "body", (0, -0.4, -1.3), (0, 0, 0.78), [Box(-0.4, -0.4, -0.3, 0.8, 0.8, 0.45, gem(FIRE3, FIRE1))]))
    P += skirt(cel(flames(cobble)), solid(FIRE2), flare=0.3, length=5.4)
    # delantal de herrera con una llama grabada
    def apron(f, x, y, w, h):
        cx, cy = w / 2, h * 0.45
        if f == "front" and ((x - cx) ** 2 / (w * 0.13) ** 2 + (y - cy) ** 2 / (h * 0.2) ** 2) < 1 and y > cy - h * 0.2 + abs(x - cx) * 0.9:
            return FIRE2
        return LEATHER_HI if (x + y) % 11 == 0 else LEATHER
    P.append(Part("apron", "skirt_front", (0, 0, -0.5), (0, 0, 0), [Box(-2.6, -0.4, -0.35, 5.2, 5.4, 0.35, cel(apron))]))
    P.append(Part("apron_belt", "body", (0, 0, 0), (0, 0, 0), [Box(-3.85, 7.0, -2.15, 7.7, 0.9, 4.3, solid(LEATHER_DK))]))
    P.append(Part("tongs", "body", (4.0, 7.5, 0.3), (0, 0, -0.14), [Box(-0.22, 0, -0.45, 0.44, 4.0, 0.35, metal((64, 64, 70), IRON_HI)),
                                                                   Box(-0.22, 0, 0.15, 0.44, 4.0, 0.35, metal((64, 64, 70), IRON_HI))]))
    # mangas sueltas con borde de fuego (hombros al aire)
    P.append(Part("sleeve_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.55, 2.4, -1.5, 2.9, 3.0, 3.0, hidden("top")(cel(flames(lambda f, x, y, w, h: CHAR))))]))
    P.append(Part("gauntlet_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.3, 6.4, -1.25, 2.5, 1.6, 2.5, metal((90, 90, 98), IRON_HI))]))
    # cabeza: gafas de forja, broches de llama y lazos de las coletas
    P.append(Part("goggles", "head", (0, -8.2, -2.5), (-0.35, 0, 0), [Box(-4.5, -0.4, -0.3, 9.0, 0.8, 0.6, solid((40, 32, 30)))]))
    P.append(Part("lens_r", "goggles", (-1.7, 0, -0.5), (0, 0.1, 0), [Box(-1.2, -1.0, -0.6, 2.4, 2.0, 1.2,
              lambda f, x, y, w, h: (255, 220, 160) if f == "front" and x < w * 0.35 and y < h * 0.35 else (226, 120, 40) if f == "front" else (110, 90, 60))]))
    flame = cel(lambda f, x, y, w, h: FIRE1 if y > h * 0.6 else FIRE2 if y > h * 0.3 else FIRE3, outline=False)
    P.append(Part("clip_r", "head", (-3.5, -6.6, -2.6), (0, 0, -0.35), [Box(-0.7, -1.7, -0.35, 1.4, 2.2, 0.7, flame), Box(-0.35, -2.5, -0.25, 0.7, 1.0, 0.5, flame)]))
    P.append(Part("tie_r", "head", (-3.4, -6.4, 3.4), (0, 0, 0), [Box(-0.9, -0.9, -0.9, 1.8, 1.8, 1.8, solid(FIRE3))]))
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
    for s_ in (-1, 1):            # dos coletas largas con mucho vuelo
        for i in range(10):
            phi = s_ * (math.radians(118) + math.radians(-15 + i * 3.4)) + j(0.03)
            L.append([phi, 0.5 + j(0.03), 0.95 + j(0.04), 13.5 + j(1.2), 0.34 + j(0.05), 0.5 + j(0.1), 2.5, 0.95, 1.5 + j(0.2), 1.0, H.KIND_LONG])
    for s_ in (-1, 1):
        L.append([s_ * math.radians(48), 0.3, math.pi / 2 - 0.05, 6.2, 0.05, 0.3, 1.8, 0.85, 0.55, 1.0, H.KIND_LONG])
        L.append([s_ * math.radians(60), 0.3, math.pi / 2 - 0.03, 4.4, 0.1, 0.35, 1.7, 0.85, 0.8, 1.0, H.KIND_LONG])
    for deg, hang in ((-41, 2.0), (-31, 2.9), (-20, 2.3), (-9, 3.1), (2, 2.5), (13, 3.1), (24, 2.3), (34, 2.8), (42, 1.9)):
        L.append([math.radians(deg), 0.3, 1.0, hang + j(0.2), 0.14, -0.16, 1.9, 0.8, 0.55, 1.0, H.KIND_BANG])
    L.append([math.radians(-10), 0.0, 0.0, 3.2, 0, 0, 1.1, 0.7, 0.6, 1.0, H.KIND_AHOGE])
    return L


# ================================================================== KURA (cofre)
KR_HAIR = ((255, 226, 140), (255, 244, 196), (212, 172, 88))
CHEST, CHEST_HI, CHEST_DK = (162, 110, 52), (196, 142, 74), (104, 68, 30)
WHITE, WHITE_SH, WHITE_DK = (252, 250, 246), (228, 222, 222), (196, 190, 196)


def kura():
    dress = cel(pleats(CHEST, CHEST_HI, CHEST_DK, 9))
    white = cel(lambda f, x, y, w, h: WHITE if (x + y) % 9 else WHITE_SH)
    bodice = cel(lambda f, x, y, w, h: (WHITE if (x + y) % 9 else WHITE_SH) if f == "front" and 0.26 * w < x < 0.74 * w
                 else pleats(CHEST, CHEST_HI, CHEST_DK, 9)(f, x, y, w, h))
    stock = cel(lambda f, x, y, w, h: WHITE if (x % 9) else WHITE_SH)
    P = figure(top=bodice, waist=white, hips=dress, sleeve=dress, fore=skin(), hand=skin(),
               thigh=cel(band(RAW_SKIN, [(0.48, 0.58, CHEST_DK), (0.58, 1.0, WHITE)])), calf=stock,
               shoe=boot((70, 42, 26), (110, 70, 46), cuff=(70, 42, 26)), bust_paint=bodice)
    P.append(Part("collar", "body", (0, 0, 0), (0, 0, 0), [Box(-3.6, -0.5, -1.95, 7.2, 1.0, 3.9, hidden("bottom")(solid(WHITE)))]))
    P.append(Part("collar_tip_r", "body", (-0.8, 0.4, -2.0), (0, 0, 0.5), [Box(-2.0, -0.5, -0.25, 2.0, 1.5, 0.35, solid(WHITE))]))
    # cerrojo dorado del cofre en el pecho (su emblema)
    P.append(Part("latch", "body", (0, 1.1, -2.3), (0, 0, 0), [Box(-0.8, -0.4, -0.4, 1.6, 2.0, 0.55, metal(GOLD, GOLD_HI)),
                                                              Box(-0.3, 0.3, -0.55, 0.6, 0.6, 0.2, solid((60, 40, 20), outline=False))]))
    P += bow("back_bow", "body", (0, 5.8, 1.75), (0, 0, 0), solid(WHITE), 1.4)
    P += skirt(dress, frill(WHITE, WHITE_SH), flare=0.32, length=5.6)
    # enagua blanca debajo de la falda
    P.append(Part("petticoat", "body", (0, 0, 0), (0, 0, 0), [Box(-3.9, 10.4, -2.2, 7.8, 1.3, 4.4, hidden("top")(frill(WHITE, WHITE_SH)))]))
    P.append(Part("apron", "skirt_front", (0, 0, -0.5), (0, 0, 0), [Box(-2.6, 0.1, -0.35, 5.2, 4.6, 0.35, white),
                                                                     Box(-2.85, 4.6, -0.4, 5.7, 0.8, 0.45, frill(WHITE, WHITE_SH))]))
    # mochila con forma de cofre
    def chestbox(f, x, y, w, h):
        if y < 3 or (f in ("front", "back", "left", "right") and abs(y - h * 0.38) < 1.5):
            return CHEST_DK
        if f == "back" and abs(x - w / 2) < 3 and abs(y - h * 0.45) < 4:
            return GOLD
        return wood(CHEST, CHEST_HI, CHEST_DK)(f, x, y, w, h)
    P.append(Part("backpack", "body", (0, 0.8, 1.9), (0.06, 0, 0), [Box(-3.1, 0, 0, 6.2, 5.6, 2.9, cel(chestbox))]))
    P.append(Part("pack_strap_r", "body", (-2.1, 0, 0), (0, 0, 0), [Box(-0.45, -0.35, -2.05, 0.9, 4.6, 4.1, hidden("top", "bottom")(solid((96, 62, 30))))]))
    # mangas abullonadas con puño blanco
    P.append(Part("puff_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.8, -1.85, -1.75, 3.4, 3.6, 3.5, cel(lambda f, x, y, w, h:
              WHITE if y >= h - 3 else pleats(CHEST, CHEST_HI, CHEST_DK, 6)(f, x, y, w, h)))]))
    P.append(Part("cuff_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.3, 8.2, -1.25, 2.4, 0.9, 2.5, frill(WHITE, WHITE_SH))]))
    # cabeza: cofia de sirvienta con encaje y un lacito
    P.append(Part("headdress", "head", (0, -8.0, -1.5), (-0.4, 0, 0), [Box(-3.6, -1.5, -0.4, 7.2, 1.7, 0.8, frill(WHITE, WHITE_SH))]))
    P.append(Part("headband", "head", (0, -7.5, 0), (0, 0, 0), [Box(-4.58, -0.45, -1.3, 9.16, 0.85, 2.6, hidden("top", "bottom")(solid(CHEST_DK)))]))
    P += bow("hair_bow", "head", (3.4, -7.4, 1.6), (0, -0.6, 0), solid(CHEST_DK), 0.8)
    return finish(P)


def kura_locks():
    return H.yunn_locks()


# ================================================================== SHIORI (mesa de encantamientos)
SH_HAIR = ((76, 54, 156), (120, 98, 206), (44, 30, 102))
OBS, OBS_HI, OBS_DK = (38, 28, 56), (70, 52, 102), (20, 14, 32)
DIAMOND, DIAMOND_HI, DIAMOND_DK = (92, 222, 216), (200, 255, 250), (48, 150, 150)
RED, RED_DK = (178, 40, 54), (120, 24, 38)


def shiori():
    robe = cel(lambda f, x, y, w, h: mix(OBS, OBS_HI, 0.7) if (x * 3 + y * 5) % 41 == 0 else pleats(OBS, OBS_HI, OBS_DK, 11)(f, x, y, w, h))
    corset = cel(lambda f, x, y, w, h: DIAMOND if f == "front" and (abs(x - w / 2) < 1 or y in (2, h - 3)) else OBS_DK)
    rune_hem = cel(lambda f, x, y, w, h: DIAMOND if (f in ("front", "back") and y > h - 5 and (x // 3) % 2 == 0)
                   else pleats(OBS, OBS_HI, OBS_DK, 11)(f, x, y, w, h))
    stock = cel(lambda f, x, y, w, h: (26, 20, 34) if x % 8 else (40, 32, 52))
    P = figure(top=robe, waist=corset, hips=robe, sleeve=robe, fore=robe, hand=skin(),
               thigh=stock, calf=stock, shoe=boot(OBS, OBS_HI, lace=DIAMOND, cuff=DIAMOND_DK), bust_paint=robe)
    P.append(Part("collar", "body", (0, 0, 0), (0, 0, 0), [Box(-1.9, -1.5, -1.3, 3.8, 1.6, 2.6, hidden("bottom", "top")(solid(DIAMOND_DK)))]))
    P.append(Part("brooch", "body", (0, 0.8, -2.2), (0, 0, 0.78), [Box(-0.65, -0.65, -0.35, 1.3, 1.3, 0.55, gem(DIAMOND, DIAMOND_HI))]))
    P.append(Part("sash", "body", (0, 0, 0), (0, 0, 0), [Box(-3.0, 6.7, -1.75, 6.0, 0.9, 3.5, solid(RED))]))
    P += skirt(rune_hem, solid(DIAMOND), flare=0.24, length=8.4)
    # capa roja (como el paño de la mesa) que cae de los hombros
    P.append(Part("cape", "body", (0, -0.4, 2.05), (0.1, 0, 0), [Box(-4.2, 0, 0, 8.4, 15.0, 0.45,
              cel(lambda f, x, y, w, h: RED_DK if (y >= h - 3 or x % 12 == 0) else RED, outline=True, grad=0.25))]))
    P.append(Part("cape_clasp_r", "body", (-2.6, -0.1, -0.2), (0, 0, 0), [Box(-0.5, -0.5, -1.8, 1.0, 1.0, 1.0, metal(GOLD, GOLD_HI))]))
    # mangas anchas acampanadas con borde de diamante
    P.append(Part("bell_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.75, 5.2, -1.75, 3.3, 3.2, 3.5,
              hidden("top", "bottom")(cel(lambda f, x, y, w, h: DIAMOND if y >= h - 2 else OBS_HI)))]))
    # cabeza: diadema dorada con un diamante y cristales colgantes
    P.append(Part("circlet", "head", (0, -6.9, 0), (0, 0, 0), [Box(-4.56, -0.4, -4.56, 9.12, 0.75, 9.12, hidden("top", "bottom")(metal((206, 174, 70), GOLD_HI)))]))
    P.append(Part("tiara_gem", "head", (0, -7.05, -4.75), (0, 0, 0.78), [Box(-0.8, -0.8, -0.35, 1.6, 1.6, 0.6, gem(DIAMOND, DIAMOND_HI))]))
    P.append(Part("earring_r", "head", (-4.7, -2.4, -0.6), (0, 0, 0), [Box(-0.25, 0, -0.25, 0.5, 1.3, 0.5, gem(DIAMOND, DIAMOND_HI))]))
    return finish(P)


def shiori_locks():
    rnd = random.Random(5)
    j = lambda a: rnd.uniform(-a, a)
    L = []
    for i in range(14):
        phi = i / 14 * 2 * math.pi + 0.1
        L.append([phi, 0.04, 1.1, 0.0, 0, 0, 3.0, 0.9, 0.8, 1.0, H.KIND_BOB])
    for n, lift, hang in ((13, 0.9, 15.0), (11, 0.5, 15.5), (9, 0.2, 13.5)):
        for i in range(n):
            phi = math.radians(104 + i * (152.0 / (n - 1))) + j(0.02)
            L.append([phi, 0.15, math.pi / 2 + 0.1, hang + j(0.4), 0.05, 0.08, 2.9, 0.95, lift, 1.0, H.KIND_LONG])
    for s_ in (-1, 1):
        for deg in (52, 60, 68):
            L.append([s_ * math.radians(deg), 0.3, math.pi / 2 - 0.02, 9.2 + j(0.3), 0.03, 0.05, 2.0, 0.9, 0.6, 1.0, H.KIND_LONG])
        for deg in (78, 92):
            L.append([s_ * math.radians(deg), 0.2, math.pi / 2 + 0.05, 6.0, 0.15, 0.1, 2.5, 0.95, 0.7, 1.0, H.KIND_BOB])
    for deg in range(-40, 44, 9):
        L.append([math.radians(deg), 0.3, 1.0, 2.6, 0.08, -0.05, 2.1, 0.8, 0.5, 1.0, H.KIND_SOFTBANG])
    return L


# ================================================================== las 4
WAIFUS = {
    "kozue": dict(hair=KZ_HAIR, parts=kozue, locks=kozue_locks,
                  face=make_face(((34, 92, 40), (82, 166, 74), (176, 232, 140)), lash=(58, 30, 20), brow=(96, 56, 32))),
    "homura": dict(hair=HM_HAIR, parts=homura, locks=homura_locks,
                   face=make_face(((140, 50, 10), (234, 140, 30), (255, 220, 120)), lash=(70, 18, 16), brow=(150, 40, 26), sharp=True)),
    "kura": dict(hair=KR_HAIR, parts=kura, locks=kura_locks,
                 face=make_face(((26, 64, 150), (70, 132, 226), (170, 216, 255)), lash=(110, 76, 40), brow=(190, 150, 80))),
    "shiori": dict(hair=SH_HAIR, parts=shiori, locks=shiori_locks,
                   face=make_face(((20, 104, 116), (56, 200, 200), (200, 255, 250)), lash=(36, 22, 70), brow=(60, 40, 120), blush=(236, 150, 180))),
}
EXPRS = [("normal", ""), ("blink", "_blink"), ("happy", "_happy"), ("surprised", "_surprised"), ("focused", "_focused"), ("tired", "_tired")]


def build(name):
    d = WAIFUS[name]
    G.AO = False
    out = os.path.join(TEXR, name)
    os.makedirs(out, exist_ok=True)
    for f in os.listdir(out):
        os.remove(os.path.join(out, f))
    for expr, suf in EXPRS:
        head_texture(d["face"](expr), d["hair"]).save(os.path.join(out, "face" + suf + ".png"))
    parts = d["parts"]()
    w, h = G.pack(parts, 64)
    G.build_texture(parts, w, h).save(os.path.join(out, "body.png"))
    G.strand_texture().save(os.path.join(out, "hair_strand.png"))
    return parts, (w, h)


def main():
    java = ["// GENERADO por tools/gen_waifus.py — no editar a mano.", "package com.totallywaifu.client;", "",
            "import net.minecraft.client.model.geom.ModelPart;", "import net.minecraft.client.model.geom.PartPose;",
            "import net.minecraft.client.model.geom.builders.*;", "", "import java.util.LinkedHashMap;", "import java.util.Map;", "",
            "public final class WaifuModels {", "    private WaifuModels() {}", "",
            "    public static final float ARM_X = %s, ARM_Y = %s, LEG_X = %s, LEG_Y = %s, HEAD_SCALE = %s;" % tuple(G.fnum(v) for v in (ARM_X, ARM_Y, LEG_X, LEG_Y, HEAD_SCALE)), "",
            "    /** La cabeza (cubo 8x8x8) con la cara: se dibuja aparte con la textura de cada expresión. */",
            "    public static LayerDefinition face() {",
            "        MeshDefinition mesh = new MeshDefinition();",
            "        mesh.getRoot().addOrReplaceChild(\"head\", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.ZERO);",
            "        return LayerDefinition.create(mesh, 32, 16);", "    }", ""]
    hair = ["// GENERADO por tools/gen_waifus.py — no editar a mano.", "package com.totallywaifu.client;", "",
            "/** Mechones de cada waifu: phi, th0, th1, caída, vuelo, curva, ancho, grosor, separación, tono, tipo. */",
            "public final class WaifuHair {", "    private WaifuHair() {}", "",
            "    public static final float CY = %s, R = %s, P = %s;" % (H.fjava(H.CY), H.fjava(H.R), H.fjava(H.P)),
            "    public static final int SCALP_STEPS = %d, HANG_STEPS = %d, SIDES = %d;" % (H.SCALP_STEPS, H.HANG_STEPS, H.SIDES),
            "    public static final float VOX_STEP = %s, WAVE_AMP = %s, WAVE_FREQ = %s;" % (H.fjava(H.VOX_STEP), H.fjava(H.WAVE_AMP), H.fjava(H.WAVE_FREQ)),
            "    public static final int[] PALETTE = {" + ", ".join("0x%02X%02X%02X" % c for c in H.PALETTE) + "};", ""]
    for name, d in WAIFUS.items():
        parts, (w, h) = build(name)
        cap = name.capitalize()
        java += G.java_layer("body" + cap, parts, ANCHORS, w, h) + [""]
        java += G.java_bake("bakeBody" + cap, parts, ANCHORS) + [""]
        hair.append("    public static final int COLOR_%s = 0x%02X%02X%02X;" % ((name.upper(),) + d["hair"][0]))
        hair.append("    public static final float[][] %s = {" % name.upper())
        for l in d["locks"]():
            hair.append("            {" + ", ".join(H.fjava(v) for v in l) + "},")
        hair += ["    };", ""]
        print("%s: cuerpo %dx%d (%d piezas) · mechones %d" % (name, w, h, len(parts), len(d["locks"]())))
    java += ["}", ""]
    hair += ["}", ""]
    os.makedirs(JAVA_DIR, exist_ok=True)
    open(os.path.join(JAVA_DIR, "WaifuModels.java"), "w").write("\n".join(java))
    open(os.path.join(JAVA_DIR, "WaifuHair.java"), "w").write("\n".join(hair))


def preview(outdir, expr=""):
    os.makedirs(outdir, exist_ok=True)
    for name, d in WAIFUS.items():
        out = os.path.join(TEXR, name)
        bt = Image.open(os.path.join(out, "body.png")).convert("RGBA")
        ft = Image.open(os.path.join(out, "face%s.png" % expr)).convert("RGBA")
        parts = d["parts"]()
        G.pack(parts, 64)
        tf = G.chains(parts, ANCHORS)
        quads = []
        # quads_for usa img.width/64 como escala; la cara (32 de ancho a 8 px) también da 8/... => se reescala a 64 unidades
        face64 = Image.new("RGBA", (512, 256), T)
        face64.paste(ft, (0, 0))
        hq = G.quads_for(face64, -4, -8, -4, 8, 8, 8, 0, 0, [((0, 0, 0), (0, 0, 0))], 1)
        for p in parts:
            root = p.name
            while root not in dict(ANCHORS):
                root = next(x.parent for x in parts if x.name == root)
            for b in p.boxes:
                (hq if root == "head" else quads).extend(G.quads_for(bt, b.x, b.y, b.z, b.w, b.h, b.d, b.u, b.v, tf[p.name], 2))
        Ld = H.norm((0.35, -0.8, -0.5))
        for q, c, n in H.mesh_quads(table=d["locks"](), color=d["hair"][0]):
            l = 0.55 + 0.5 * max(0.0, -(n[0] * Ld[0] + n[1] * Ld[1] + n[2] * Ld[2]))
            hq.append((q, tuple(int(min(255, v * l)) for v in c)))
        quads += [([(x * HEAD_SCALE, y * HEAD_SCALE, z * HEAD_SCALE) for x, y, z in q], c) for q, c in hq]
        G.draw(quads, os.path.join(outdir, name + "_preview.png"), 13, 140,
               [(math.pi, 0.05), (math.pi - 0.7, 0.12), (math.pi / 2, 0.05), (0.0, 0.08)], (280, 480))
        head = [qc for qc in quads if min(p[1] for p in qc[0]) < 1.0]
        G.draw(head, os.path.join(outdir, name + "_face.png"), 26, 260, [(math.pi, 0.0), (math.pi - 0.5, 0.1)], (300, 330))


if __name__ == "__main__":
    main()
    if len(sys.argv) > 2 and sys.argv[1] == "preview":
        preview(sys.argv[2], sys.argv[3] if len(sys.argv) > 3 else "")
