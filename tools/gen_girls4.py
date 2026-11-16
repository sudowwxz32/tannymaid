"""Totally Waifu? 4.0 — chicas nuevas: Yume (cama), Nether, criaturas nuevas, hermanas TNT, Warden y las 4 sorpresas.

Mismo estilo de bloques que las demás (gen_blocky / gen_mobgirls), cada una con silueta, peinado, ojos y accesorios propios.
"""
import math
import gen_blocky as B
import gen_mobgirls as M
from gen_blocky import Part, Box, flat, grad, pleats, band, hair_p, frill, metal, gem, stock, boot, sole, bow, chain
from gen_mobgirls import girl, hair3, cloth, lt, dk

SK = B.skin_p
WHITE = (246, 246, 248)


def mirror_pair(name, parent, pivot, rot, boxes):
    """Pieza del lado derecho (se refleja sola al izquierdo)."""
    return [Part(name + "_r", parent, pivot, rot, boxes)]


def stars(base, star=(255, 236, 140), every=11):
    def fn(f, x, y, w, h):
        return star if ((x * 7 + y * 13) % (every * 5) == 0) else base
    return fn


def stripes(a, b, step=12):
    return lambda f, x, y, w, h: a if (y // step) % 2 == 0 else b


GLYPH = {"T": ["###", ".#.", ".#.", ".#.", ".#."], "N": ["#.#", "###", "###", "#.#", "#.#"]}


def tnt_band(red, white, ink=(20, 20, 20), band_y=(0.3, 0.72), text=True):
    def fn(f, x, y, w, h):
        y0, y1 = int(h * band_y[0]), int(h * band_y[1])
        if y0 <= y < y1:
            if text and f == "front":
                cell = max(1, (y1 - y0) // 6)
                gw = 3 * cell
                total = 3 * gw + 2 * cell
                ox, oy = (w - total) // 2, y0 + ((y1 - y0) - 5 * cell) // 2
                for i, ch in enumerate("TNT"):
                    gx = ox + i * (gw + cell)
                    cx, cy = (x - gx) // cell, (y - oy) // cell
                    if 0 <= cx < 3 and 0 <= cy < 5 and x >= gx and y >= oy and GLYPH[ch][cy][cx] == "#":
                        return ink
            return white
        return red if (x + y) % 17 else lt(red, 1.1)
    return fn


# ================================================================== YUME — la cama
def yume():
    BL, BLD, PK = (176, 204, 242), (130, 160, 210), (246, 180, 210)
    gown = stars(BL)
    cap = stars((150, 170, 230), (255, 240, 160), 9)
    P, info = girl(hair3((206, 178, 236)), "wavy", ((90, 70, 150), (150, 130, 220), (230, 220, 255)), (70, 50, 100),
                   top=gown, waist=lambda f, x, y, w, h: PK if 3 < y < h - 3 and f == "front" else BL, hips=gown, sleeve=frill(WHITE, (220, 220, 236)),
                   upper=gown, fore=SK, hand=SK, thigh=SK, calf=SK,
                   shoes=(boot(PK, lt(PK), dk(PK)), sole((220, 150, 180)), 12.0, flat(WHITE)),
                   skirt_spec=(gown, frill(WHITE, (220, 220, 236)), 6.6, 6.6, 0.22),
                   extras=[[Part("cap0", "head", (0, -8.5, 0), (-0.08, 0, 0.1), [Box(-4.8, -1.6, -4.8, 9.6, 1.6, 9.6, cap)]),
                            Part("cap_brim", "head", (0, -7.6, 0), (-0.08, 0, 0.1), [Box(-4.95, -0.9, -4.95, 9.9, 0.9, 9.9, flat(WHITE))]),
                            Part("cap1", "cap0", (0.6, -1.5, 0.6), (0.45, 0, 0.25), [Box(-3.2, -2.8, -3.2, 6.4, 2.8, 6.4, cap)]),
                            Part("cap2", "cap1", (0, -2.6, 0.5), (0.7, 0, 0.3), [Box(-1.8, -2.8, -1.8, 3.6, 2.8, 3.6, cap)]),
                            Part("pompom", "cap2", (0, -2.6, 0), (0, 0, 0), [Box(-1.3, -2.4, -1.3, 2.6, 2.6, 2.6, flat(WHITE))])],
                           bow("gown_bow", "body", (0, 0.6, -1.95), (0, 0, 0), flat(PK), 0.9),
                           [Part("moon_clip", "head", (3.6, -6.0, -3.8), (0, 0, 0.3), [Box(-0.8, -0.8, -0.3, 1.6, 1.6, 0.6, metal((250, 220, 120), (255, 250, 200)))])]])
    return P, info


# ================================================================== NUEVAS CRIATURAS
def slime():
    G, GL, GD = (118, 206, 96), (176, 238, 150), (70, 150, 60)
    goo = lambda f, x, y, w, h: GL if (x % 13 < 3 and y % 11 < 2) else (GD if y > h - 3 else G)
    P, info = girl(hair3((120, 212, 100)), "bob", ((30, 100, 30), (90, 190, 70), (210, 255, 180)), (40, 90, 40),
                   top=goo, waist=cloth(GD), hips=goo, sleeve=goo, upper=SK, fore=SK, hand=SK,
                   thigh=SK, calf=lambda f, x, y, w, h: G if y > 10 + (x * 5 % 9) else SK(f, x, y, w, h),
                   shoes=(boot(G, GL, GD), sole(GD), 10.8, flat(GL)),
                   skirt_spec=(goo, flat(GD), 3.8, 6.6, 0.42), skin_c=((228, 246, 218), (198, 224, 188)),
                   extras=[mirror_pair("cube", "head", (-4.7, -7.2, -2.0), (0.3, 0.2, 0.2), [Box(-1.1, -1.1, -1.1, 2.2, 2.2, 2.2, goo)]),
                           [Part("drip%d" % i, "head", (x, -1.6, -4.7), (0, 0, 0), [Box(-0.4, 0, -0.4, 0.8, l, 0.8, flat(G))])
                            for i, (x, l) in enumerate(((-3.1, 1.6), (-0.4, 1.0), (2.6, 2.0)))],
                           [Part("belly_cube", "body", (0, 5.4, -1.7), (0, 0, 0.78), [Box(-0.7, -0.7, -0.4, 1.4, 1.4, 0.7, gem(GD, GL))])]])
    return P, info


def conejo():
    BR, BRL, CR = (150, 104, 66), (186, 140, 96), (244, 232, 210)
    PKI = (246, 190, 196)
    ear = lambda f, x, y, w, h: PKI if (f == "front" and 2 < x < w - 3 and 3 < y < h - 2) else CR

    def overall(f, x, y, w, h):
        return (90, 120, 170) if not (f == "front" and y < 4) else (240, 200, 70)
    P, info = girl(hair3(BR), "short", ((110, 50, 30), (190, 100, 60), (250, 190, 150)), (70, 40, 30),
                   top=cloth(CR), waist=overall, hips=overall, sleeve=frill(CR, (226, 212, 190)), upper=cloth(CR), fore=SK, hand=SK,
                   thigh=band(SK, [(0.5, 0.58, BR)]), calf=stock(CR, (220, 206, 186)),
                   shoes=(boot(BR, BRL, dk(BR)), sole((80, 56, 36)), 11.2, flat(WHITE)),
                   skirt_spec=None,
                   extras=[mirror_pair("bear", "head", (-2.0, -8.7, -0.2), (-0.12, 0, -0.14), [Box(-1.0, -6.4, -0.55, 2.0, 6.4, 1.1, ear), Box(-1.0, -7.4, -0.2, 2.0, 1.2, 1.6, flat(CR))]),
                           [Part("shorts", "body", (0, 0, 0), (0, 0, 0), [Box(-3.75, 7.6, -2.0, 7.5, 3.0, 4.0, cloth((90, 120, 170)))])],
                           [Part("puff", "body", (0, 8.4, 2.0), (0, 0, 0), [Box(-1.5, -1.5, 0, 3.0, 3.0, 2.4, flat(WHITE))])],
                           [Part("carrot", "head", (3.7, -7.2, -3.4), (0, 0, 0.6), [Box(-0.5, -1.6, -0.4, 1.0, 2.4, 0.8, flat((240, 140, 40))),
                                                                                  Box(-0.6, -2.4, -0.3, 1.2, 0.8, 0.6, flat((80, 170, 60)))])]])
    return P, info


def panda():
    BK, BKL = (36, 34, 40), (64, 60, 70)
    GR = (110, 180, 80)

    def qipao(f, x, y, w, h):
        if f == "front" and abs(x - w * 0.62) < 1.5:
            return GR
        return WHITE if (x + y) % 19 else (232, 232, 236)
    marks = [(x, y, (200, 192, 204)) for x in (1, 2, 3, 4, 5, 6, 9, 10, 11, 12, 13, 14) for y in (8, 9, 10, 11) if not (y == 11 and x in (1, 14))]
    P, info = girl(((242, 242, 244), (255, 255, 255), (200, 200, 206)), "buns", ((20, 20, 24), (70, 64, 70), (200, 190, 200)), (20, 20, 24),
                   top=qipao, waist=cloth(BK), hips=qipao, sleeve=cloth(BK), upper=cloth(BK), fore=SK, hand=SK,
                   thigh=lambda f, x, y, w, h: SK(f, x, y, w, h) if f in ("front", "left", "right") and y < h * 0.4 else BK,
                   calf=stock(BK, (22, 20, 26)),
                   shoes=(boot(BK, BKL, (20, 18, 22), accent=GR), sole((20, 18, 22)), 11.6, None),
                   skirt_spec=(qipao, flat(BK), 6.0, 6.6, 0.18),
                   extras=[mirror_pair("pear", "head", (-3.4, -8.5, 0.6), (0, 0, -0.2), [Box(-1.4, -1.8, -0.7, 2.8, 2.4, 1.4, flat(BK))]),
                           [Part("bamboo", "body", (1.4, 1.0, 2.2), (0, 0, 0.5), [Box(-0.5, -4.0, -0.5, 1.0, 10.0, 1.0,
                                                                                   lambda f, x, y, w, h: (70, 140, 50) if y % 20 < 2 else GR)])],
                           [Part("bamboo_leaf", "bamboo", (0.4, -4.0, 0), (0, 0, -0.8), [Box(0, -0.4, -0.2, 2.6, 0.8, 0.4, flat((90, 170, 60)))])]])
    info["marks"] = marks
    return P, info


def caballo():
    BR, BRD, TAN = (124, 80, 46), (90, 56, 30), (216, 190, 146)
    VEST = (110, 70, 40)

    def shirt(f, x, y, w, h):
        if f == "front" and abs(x - w / 2) < w * 0.22:
            return WHITE
        return VEST if f in ("front", "back") else WHITE
    P, info = girl(hair3(BR), "pony", ((60, 40, 20), (130, 90, 50), (220, 180, 130)), (50, 30, 20),
                   top=shirt, waist=cloth(VEST), hips=cloth(TAN), sleeve=cloth(WHITE), upper=cloth(WHITE), fore=cloth(WHITE), hand=flat((60, 40, 30)),
                   thigh=cloth(TAN), calf=cloth(TAN),
                   shoes=(boot((30, 28, 30), (60, 56, 60), (16, 16, 18), lace=(140, 120, 80)), sole((16, 16, 18)), 5.6, flat((60, 56, 60))),
                   skirt_spec=None,
                   extras=[mirror_pair("hear", "head", (-2.6, -8.7, 0.2), (0, 0, -0.22), [Box(-0.9, -2.6, -0.6, 1.8, 2.6, 1.2,
                                                                                         lambda f, x, y, w, h: (230, 190, 170) if f == "front" and 1 < x < w - 2 and y > 4 else BR)]),
                           chain("htail", "body", (0, 8.6, 2.0), (0.45, 0, 0), [(3.0, 2.2, 1.6, 0, 0), (3.4, 2.4, 1.6, -0.2, 0), (3.0, 2.0, 1.4, -0.2, 0)], hair_p(*hair3(BRD))),
                           [Part("belt", "body", (0, 0, 0), (0, 0, 0), [Box(-3.75, 6.3, -1.95, 7.5, 0.9, 3.9, metal((50, 40, 30), (200, 170, 90)))])],
                           [Part("rosette", "body", (1.6, 1.0, -2.0), (0, 0, 0), [Box(-0.9, -0.9, -0.3, 1.8, 1.8, 0.5, flat((60, 100, 220))),
                                                                                Box(-0.4, 0.8, -0.2, 0.8, 1.6, 0.3, flat((60, 100, 220)))])]])
    return P, info


def abeja():
    Y, YL, BK = (250, 200, 50), (255, 230, 120), (34, 30, 30)
    wing = lambda f, x, y, w, h: (190, 220, 245) if (x + y) % 9 else (150, 180, 220)
    P, info = girl(hair3((244, 196, 84)), "bob", ((110, 70, 10), (210, 150, 30), (255, 230, 140)), (60, 40, 10),
                   top=stripes(Y, BK, 10), waist=cloth(BK), hips=stripes(BK, Y, 10), sleeve=frill(YL, Y), upper=SK, fore=SK, hand=flat(BK),
                   thigh=band(SK, [(0.55, 1.0, Y)]), calf=stripes(BK, Y, 10),
                   shoes=(boot(BK, (60, 56, 56), (20, 18, 18)), sole((20, 18, 18)), 11.0, flat(Y)),
                   skirt_spec=(stripes(Y, BK, 10), frill(YL, Y), 3.6, 6.6, 0.5),
                   extras=[mirror_pair("ant", "head", (-1.6, -8.9, -1.4), (-0.3, 0, -0.35), [Box(-0.25, -3.2, -0.25, 0.5, 3.2, 0.5, flat(BK)), Box(-0.7, -4.5, -0.7, 1.4, 1.4, 1.4, flat(BK))]),
                           mirror_pair("bwing", "body", (-0.8, 1.6, 2.0), (0.1, 0.55, 0.35), [Box(-5.2, -2.6, 0, 5.2, 5.2, 0.3, wing)]),
                           mirror_pair("bwing2", "body", (-0.8, 4.4, 2.0), (0.1, 0.45, -0.25), [Box(-3.6, -1.6, 0, 3.6, 3.2, 0.3, wing)]),
                           [Part("honey", "body", (3.7, 6.8, 0.2), (0, 0, -0.1), [Box(-0.7, 0, -0.7, 1.4, 1.8, 1.4,
                                                                                lambda f, x, y, w, h: (250, 170, 30) if y > 4 else (230, 230, 240))])]])
    return P, info


# ================================================================== NETHER
def piglin():
    G, GH = (240, 196, 60), (255, 236, 140)
    LEA, LEAD = (120, 74, 44), (84, 50, 28)
    PSK = ((244, 194, 182), (222, 166, 156))
    ear = lambda f, x, y, w, h: (236, 150, 150) if f == "front" and 1 < y < h - 2 else (246, 176, 170)
    P, info = girl(hair3((238, 192, 92)), "drills", ((120, 60, 20), (220, 140, 40), (255, 220, 130)), (90, 50, 30),
                   top=lambda f, x, y, w, h: G if y < 3 or y > h - 3 else LEA, waist=metal(G, GH), hips=cloth(LEA, seam=LEAD), sleeve=metal(G, GH),
                   upper=SK, fore=metal(G, GH), hand=SK, thigh=SK, calf=metal(G, GH),
                   shoes=(boot(LEA, (150, 96, 60), LEAD), sole(LEAD), 10.6, metal(G, GH)),
                   skirt_spec=(cloth(LEA, seam=LEAD), metal(G, GH), 3.8, 6.6, 0.32), skin_c=PSK,
                   extras=[mirror_pair("pe", "head", (-4.5, -6.4, 0.2), (0, 0, 0.95), [Box(-0.5, 0, -1.3, 1.0, 2.8, 2.6, ear)]),
                           [Part("crown", "head", (0, -8.9, 0), (0, 0, 0.08), [Box(-2.4, -1.4, -2.4, 4.8, 1.4, 4.8, metal(G, GH)),
                                                                             Box(-2.4, -2.2, -2.4, 1.0, 0.8, 1.0, metal(G, GH)), Box(1.4, -2.2, -2.4, 1.0, 0.8, 1.0, metal(G, GH)),
                                                                             Box(-0.5, -2.4, -2.5, 1.0, 1.0, 0.6, gem((220, 40, 60), (255, 140, 160)))])],
                           mirror_pair("ering", "head", (-4.6, -2.6, -0.6), (0, 0, 0), [Box(-0.4, 0, -0.4, 0.8, 1.2, 0.8, metal(G, GH))]),
                           [Part("gsword", "body", (-1.6, 2.0, 2.2), (0, 0, -0.45), [Box(-0.4, -4.0, -0.3, 0.8, 9.0, 0.6, metal(G, GH)),
                                                                                    Box(-1.5, 3.6, -0.4, 3.0, 0.7, 0.8, metal((140, 90, 40), G))])]])
    return P, info


def wither():
    AR, ARL, ARD = (44, 44, 50), (74, 74, 82), (24, 24, 28)
    BONE = (226, 222, 210)

    def armor(f, x, y, w, h):
        if f == "front" and (y // 6) % 2 == 0 and 3 < x < w - 4 and y > 6:
            return BONE
        return ARL if (x + y) % 13 == 0 else AR
    P, info = girl(hair3((30, 28, 34)), "long", ((60, 0, 0), (200, 40, 40), (255, 150, 140)), (16, 14, 18),
                   top=armor, waist=cloth(ARD), hips=cloth(AR), sleeve=metal(AR, ARL), upper=SK, fore=metal(AR, ARL), hand=flat(ARD),
                   thigh=SK, calf=metal(AR, ARL),
                   shoes=(boot(ARD, AR, (10, 10, 12), accent=BONE), sole((10, 10, 12)), 8.4, metal(AR, ARL)),
                   skirt_spec=(pleats(AR, ARL, ARD), flat(BONE), 5.0, 6.6, 0.26), skin_c=((168, 116, 80), (140, 94, 62)),
                   extras=[[Part("wsword", "body", (1.6, 1.6, 2.2), (0, 0, 0.5), [Box(-0.5, -5.0, -0.3, 1.0, 10.0, 0.6, metal((120, 120, 126), (170, 170, 176))),
                                                                                Box(-1.6, 4.4, -0.4, 3.2, 0.8, 0.8, flat((60, 40, 30)))])],
                           [Part("wrose", "head", (-3.5, -7.4, -3.4), (0, 0, -0.3), [Box(-1.0, -1.0, -0.5, 2.0, 2.0, 1.0, flat((20, 20, 24))),
                                                                                   Box(-0.4, -0.4, -0.6, 0.8, 0.8, 0.3, flat((60, 20, 20)))])],
                           [Part("bone_clip_r", "head", (-4.7, -4.4, 0.6), (0, 0, 0.4), [Box(-0.3, -1.4, -0.3, 0.6, 2.8, 0.6, flat(BONE))])],
                           [Part("skull_belt", "body", (0, 6.5, -2.0), (0, 0, 0), [Box(-0.9, -0.8, -0.4, 1.8, 1.6, 0.6, flat(BONE)),
                                                                                  Box(-0.6, -0.3, -0.5, 0.4, 0.4, 0.2, flat((20, 20, 20))), Box(0.2, -0.3, -0.5, 0.4, 0.4, 0.2, flat((20, 20, 20)))])]])
    return P, info


def blaze():
    Y, OR, SM = (252, 206, 60), (244, 140, 30), (60, 50, 50)
    fire = lambda f, x, y, w, h: Y if (y + (x * 3) % 7) < h * 0.45 else OR
    rods = []
    for i in range(6):
        a = i * math.pi / 3
        rods.append(Part("rod%d" % i, "body", (math.cos(a) * 5.2, 5.0 + (i % 2) * 1.6, math.sin(a) * 4.0), (0, -a, 0.15 if i % 2 else -0.15),
                         [Box(-0.5, -2.6, -0.5, 1.0, 5.2, 1.0, lambda f, x, y, w, h: OR if (y // 8) % 2 else Y)]))
    P, info = girl(((252, 180, 40), (255, 236, 120), (220, 100, 20)), "pony", ((140, 50, 0), (250, 150, 20), (255, 240, 150)), (120, 50, 10),
                   top=fire, waist=cloth(SM), hips=cloth(SM), sleeve=fire, upper=SK, fore=lambda f, x, y, w, h: OR if y > h - 6 else SK(f, x, y, w, h), hand=SK,
                   thigh=SK, calf=lambda f, x, y, w, h: SM if (y + x % 5) > 6 else Y,
                   shoes=(boot(OR, Y, (170, 80, 20)), sole((90, 40, 20), Y), 10.6, None),
                   skirt_spec=None, skin_c=((252, 222, 192), (230, 192, 160)),
                   extras=[rods, [Part("shorts", "body", (0, 0, 0), (0, 0, 0), [Box(-3.8, 7.6, -2.0, 7.6, 2.8, 4.0, cloth(SM))])],
                           [Part("flame_clip", "head", (3.6, -7.6, -3.4), (0, 0, 0.4), [Box(-0.7, -1.6, -0.4, 1.4, 2.0, 0.8, flat(OR)), Box(-0.4, -2.4, -0.3, 0.8, 0.9, 0.6, flat(Y))])]])
    return P, info


def ghast():
    W, WS, GR = (248, 248, 250), (214, 214, 222), (176, 176, 186)
    dress = lambda f, x, y, w, h: WS if (x // 6 + y // 9) % 5 == 0 else W
    tents = []
    for i in range(9):
        a = i * 2 * math.pi / 9
        tents.append(Part("tent%d" % i, "body", (math.cos(a) * 3.2, 11.0, math.sin(a) * 2.2), (math.sin(a) * 0.2, 0, -math.cos(a) * 0.15),
                          [Box(-0.5, 0, -0.5, 1.0, 4.0 + (i % 3) * 0.8, 1.0, lambda f, x, y, w, h: GR if y > h - 4 else W)]))
    marks = [(3, 12, (150, 200, 240)), (3, 13, (150, 200, 240)), (12, 12, (150, 200, 240)), (12, 13, (150, 200, 240)), (3, 14, (180, 220, 250)), (12, 14, (180, 220, 250))]
    P, info = girl(hair3((244, 244, 248)), "low", ((60, 60, 70), (130, 130, 150), (220, 220, 240)), (110, 110, 120),
                   top=dress, waist=cloth(W), hips=dress, sleeve=frill(W, WS), upper=dress, fore=SK, hand=SK,
                   thigh=SK, calf=stock(W, WS), shoes=(boot(W, WS, GR), sole(GR), 11.6, None),
                   skirt_spec=(dress, frill(W, WS), 5.4, 6.6, 0.3), skin_c=((250, 244, 244), (228, 220, 224)),
                   extras=[tents, bow("hair_bow", "head", (0, -8.9, 1.2), (0, 0, 0), flat((220, 80, 80)), 1.0),
                           [Part("tear_gem", "body", (0, 0.4, -1.95), (0, 0, 0.78), [Box(-0.6, -0.6, -0.3, 1.2, 1.2, 0.6, gem((120, 180, 230), (220, 240, 255)))])]])
    info["marks"] = marks
    return P, info


# ================================================================== HERMANAS TNT
def _tnt_extras(R, W):
    fuse = [Part("fuse", "head", (1.4, -8.9, 0.4), (0.25, 0, 0.35), [Box(-0.25, -2.6, -0.25, 0.5, 2.6, 0.5, flat((70, 66, 60)))]),
            Part("spark", "fuse", (0, -2.6, 0), (0, 0, 0), [Box(-0.6, -1.0, -0.6, 1.2, 1.0, 1.2, flat((255, 220, 60))), Box(-0.3, -1.6, -0.3, 0.6, 0.6, 0.6, flat((255, 140, 30)))])]
    belt = [Part("tnt_belt", "body", (0, 0, 0), (0, 0, 0), [Box(-3.8, 6.2, -2.0, 7.6, 0.8, 4.0, flat((40, 36, 36)))])]
    for i, x in enumerate((-3.4, 3.4)):
        belt.append(Part("tnt%d" % i, "body", (x, 7.0, -0.4), (0, 0, 0), [Box(-0.7, 0, -0.9, 1.4, 1.8, 1.8, tnt_band(R, W, text=False, band_y=(0.35, 0.65)))]))
    return [fuse, belt]


def dina():
    R, W, BK = (214, 44, 38), (246, 240, 236), (26, 24, 28)
    P, info = girl(((30, 28, 34), (60, 56, 70), (14, 12, 16)), "twin", ((90, 0, 0), (210, 40, 40), (255, 160, 140)), (14, 10, 14),
                   top=tnt_band(R, W), waist=SK, hips=tnt_band(R, W, text=False, band_y=(0.1, 0.35)), sleeve=SK, upper=SK, fore=lambda f, x, y, w, h: BK if y > h * 0.4 else SK(f, x, y, w, h),
                   hand=flat(BK), thigh=band(SK, [(0.42, 0.5, R), (0.5, 1.0, BK)]), calf=stock(BK, (14, 12, 16)),
                   shoes=(boot(R, lt(R), dk(R)), sole(BK), 9.4, flat(W)),
                   skirt_spec=(tnt_band(R, W, text=False, band_y=(0.55, 0.75)), flat(W), 3.2, 6.6, 0.42),
                   extras=_tnt_extras(R, W) + [bow("choker", "body", (0, -0.6, -1.25), (0, 0, 0), flat(R), 0.6)])
    return P, info


def mita():
    R, W, BK = (226, 60, 44), (250, 246, 240), (30, 28, 32)
    jacket = lambda f, x, y, w, h: BK if (f == "front" and abs(x - w / 2) < w * 0.28) else R
    P, info = girl(((34, 30, 38), (70, 64, 80), (16, 14, 18)), "pony", ((120, 40, 0), (240, 120, 40), (255, 210, 150)), (16, 12, 16),
                   top=jacket, waist=SK, hips=tnt_band(BK, W, text=False, band_y=(0.4, 0.6)), sleeve=cloth(R), upper=cloth(R), fore=SK, hand=SK,
                   thigh=SK, calf=stripes(R, W, 8),
                   shoes=(boot(BK, (60, 56, 64), (16, 14, 18), accent=R), sole(R), 10.4, None),
                   skirt_spec=None,
                   extras=_tnt_extras(R, W) + [[Part("shorts", "body", (0, 0, 0), (0, 0, 0), [Box(-3.8, 7.6, -2.0, 7.6, 2.6, 4.0, tnt_band(R, W, text=True, band_y=(0.2, 0.9)))])],
                                               [Part("bandeau", "body", (0, 1.2, -1.75), (0, 0, 0), [Box(-2.6, 0, -0.3, 5.2, 2.6, 0.4, tnt_band(W, R, text=False))])],
                                               [Part("fuse2", "head", (-1.6, -8.9, -0.2), (0.1, 0, -0.5), [Box(-0.25, -2.0, -0.25, 0.5, 2.0, 0.5, flat((70, 66, 60))),
                                                                                                          Box(-0.5, -2.8, -0.5, 1.0, 0.8, 1.0, flat((255, 200, 60)))])]])
    return P, info


# ================================================================== WARDEN
def warden():
    D, DL, DD = (22, 40, 48), (40, 66, 76), (10, 20, 24)
    CY = (60, 230, 226)

    def coat(f, x, y, w, h):
        if f == "front" and 6 < y < h - 4 and (y // 6) % 2 == 0 and abs(x - w / 2) < w * 0.3:
            return CY
        return DL if (x + y) % 17 == 0 else D
    tend = []
    for s, nm in ((-1, "r"), (1, "l")):
        tend += chain("tend_" + nm, "head", (s * 3.6, -8.0, 0.6), (-0.1, 0, s * 0.55),
                      [(3.2, 2.4, 1.8, 0, 0), (3.0, 2.0, 1.6, 0.2, s * 0.35), (2.6, 1.6, 1.4, 0.3, s * 0.4), (1.8, 1.2, 1.0, 0.3, s * 0.3)],
                      lambda f, x, y, w, h: CY if (f == "front" and y > h * 0.5) else (DL if (x + y) % 7 == 0 else D))
    blind = lambda f, x, y, w, h: None if f in ("top", "bottom") else ((46, 46, 54) if y % 6 else (70, 70, 80))
    P, info = girl(hair3((24, 58, 66)), "short", ((0, 0, 0), (0, 0, 0), (0, 0, 0)), (10, 20, 24),
                   top=coat, waist=cloth(DD), hips=coat, sleeve=metal(D, DL), upper=cloth(D), fore=lambda f, x, y, w, h: CY if y % 14 == 0 else D,
                   hand=flat(DD), thigh=stock(D, DD), calf=stock(D, DD),
                   shoes=(boot(DD, D, (6, 10, 12), accent=CY), sole((6, 10, 12), CY), 7.2, metal(D, DL)),
                   skirt_spec=(coat, flat(CY), 7.2, 6.6, 0.2), skin_c=((214, 222, 228), (186, 196, 204)),
                   extras=[tend, [Part("blindfold", "head", (0, 0, 0), (0, 0, 0), [Box(-4.8, -4.9, -4.9, 9.6, 2.4, 9.8, blind)])],
                           chain("bf_tail", "head", (0.6, -3.8, 4.8), (0.3, 0, 0.2), [(2.4, 0.9, 0.3, 0, 0), (2.4, 0.9, 0.3, 0.1, 0.1)], flat((46, 46, 54))),
                           [Part("soul_core", "body", (0, 2.4, -2.0), (0, 0, 0.78), [Box(-0.8, -0.8, -0.4, 1.6, 1.6, 0.7, gem((20, 160, 170), (180, 255, 250)))])],
                           mirror_pair("pauld", "right_arm", (0, -1.4, 0), (0, 0, 0.3), [Box(-2.4, -1.0, -2.0, 3.6, 2.0, 4.0, metal(D, DL))])])
    info["eye"] = "blind"
    return P, info


# ================================================================== SORPRESAS
def zorro():
    OR, ORL, WH = (230, 124, 44), (250, 170, 90), (250, 246, 240)
    RED = (200, 40, 50)
    ear = lambda f, x, y, w, h: WH if (f == "front" and 1 < x < w - 2 and y > 3) else (OR if y > 3 else (50, 30, 30))
    P, info = girl(hair3(OR), "wild", ((140, 90, 10), (240, 180, 40), (255, 240, 150)), (90, 40, 20),
                   top=lambda f, x, y, w, h: RED if f == "front" and abs(x - w / 2 - (y - h / 2) * 0.6) < 2 else WH, waist=cloth(RED), hips=pleats(RED, lt(RED), dk(RED)),
                   sleeve=frill(WH, (226, 222, 216)), upper=cloth(WH), fore=cloth(WH), hand=SK,
                   thigh=SK, calf=stock(WH, (226, 222, 216)), shoes=(boot((120, 80, 50), (150, 104, 66), (80, 50, 30)), sole((60, 40, 24)), 12.2, flat(RED)),
                   skirt_spec=(pleats(RED, lt(RED), dk(RED)), flat(WH), 5.6, 6.6, 0.3),
                   extras=[mirror_pair("fear", "head", (-2.7, -8.6, -0.2), (0, 0, -0.2), [Box(-1.5, -3.4, -0.6, 3.0, 3.4, 1.2, ear)]),
                           chain("ftail", "body", (0, 8.8, 2.0), (0.9, 0, 0.3), [(3.0, 3.0, 3.0, 0, 0), (3.4, 3.6, 3.4, -0.35, 0.1), (3.0, 3.0, 3.0, -0.4, 0.1)], flat(OR)),
                           [Part("ftail_tip", "ftail2", (0, 2.6, 0), (0, 0, 0), [Box(-1.2, 0, -1.2, 2.4, 1.8, 2.4, flat(WH))])],
                           bow("obi", "body", (0, 5.4, 1.9), (0, math.pi, 0), flat(RED), 1.2)])
    return P, info


def ajolote():
    PK, PKL, MAG = (246, 176, 204), (255, 214, 230), (222, 80, 146)
    suit = lambda f, x, y, w, h: PKL if (x + y) % 15 == 0 else PK
    P, info = girl(hair3((248, 170, 204)), "bob", ((20, 14, 20), (60, 40, 60), (160, 120, 160)), (40, 24, 40),
                   top=suit, waist=cloth(PK), hips=suit, sleeve=SK, upper=SK, fore=SK, hand=SK,
                   thigh=SK, calf=SK, shoes=(boot(MAG, lt(MAG), dk(MAG)), sole(dk(MAG)), 12.4, None),
                   skirt_spec=(frill(PKL, PK), flat(MAG), 2.8, 7.0, 0.55), skin_c=((252, 232, 236), (232, 206, 214)),
                   extras=[mirror_pair("gilla", "head", (-4.5, -7.6, 1.2), (0, -0.3, 0.5), [Box(-2.8, -0.5, -0.4, 2.8, 1.0, 0.8, flat(MAG))]),
                           mirror_pair("gillb", "head", (-4.6, -6.0, 1.4), (0, -0.3, 0.1), [Box(-3.0, -0.5, -0.4, 3.0, 1.0, 0.8, flat(MAG))]),
                           mirror_pair("gillc", "head", (-4.5, -4.4, 1.2), (0, -0.3, -0.35), [Box(-2.6, -0.5, -0.4, 2.6, 1.0, 0.8, flat(MAG))]),
                           chain("atail", "body", (0, 8.8, 2.0), (0.7, 0, 0), [(2.8, 2.0, 0.6, 0, 0), (2.8, 2.4, 0.5, -0.2, 0)], flat(PK))])
    return P, info


def allay():
    BL, BLL, BLD = (120, 210, 250), (190, 240, 255), (70, 150, 210)
    wing = lambda f, x, y, w, h: (220, 245, 255) if (x + y) % 7 else BLL
    P, info = girl(hair3((140, 214, 250)), "short", ((20, 90, 140), (60, 190, 240), (210, 250, 255)), (40, 100, 140),
                   top=cloth(BL), waist=cloth(BLD), hips=cloth(BL), sleeve=frill(BLL, BL), upper=SK, fore=SK, hand=SK,
                   thigh=SK, calf=stock(BLL, BL), shoes=(boot(BLD, BL, dk(BLD)), sole(dk(BLD)), 11.8, None),
                   skirt_spec=(frill(BL, BLD), flat(BLL), 4.2, 6.6, 0.5), skin_c=((236, 244, 252), (212, 224, 238)),
                   extras=[mirror_pair("awing", "body", (-0.6, 1.2, 2.0), (0.2, 0.6, 0.5), [Box(-4.4, -2.0, 0, 4.4, 4.0, 0.3, wing)]),
                           mirror_pair("awing2", "body", (-0.6, 3.6, 2.0), (0.2, 0.5, -0.2), [Box(-3.2, -1.2, 0, 3.2, 2.4, 0.3, wing)]),
                           [Part("note", "head", (3.6, -7.4, -3.4), (0, 0, 0.2), [Box(-0.4, -1.6, -0.3, 0.6, 2.0, 0.6, flat(BLD)), Box(-1.0, 0, -0.4, 1.0, 0.8, 0.8, flat(BLD))])]])
    return P, info


def rana():
    G, GL, GD = (110, 176, 70), (160, 214, 110), (70, 120, 46)
    Y, YD = (248, 210, 70), (210, 168, 40)
    coat = cloth(Y, seam=YD)
    feye = lambda f, x, y, w, h: ((20, 20, 20) if (f == "front" and abs(x - w / 2) < 4 and abs(y - h / 2) < 3) else (WHITE if f == "front" else G))
    P, info = girl(hair3((60, 110, 50)), "short", ((60, 40, 10), (190, 140, 40), (250, 220, 140)), (40, 60, 20),
                   top=coat, waist=coat, hips=coat, sleeve=coat, upper=coat, fore=coat, hand=SK,
                   thigh=SK, calf=SK, shoes=(boot(G, GL, GD), sole(GD), 9.6, flat(GL)),
                   skirt_spec=(coat, flat(YD), 5.0, 6.6, 0.26),
                   extras=[[Part("hood", "head", (0, 0, 0), (0, 0, 0), [Box(-4.9, -9.3, -4.9, 9.8, 1.4, 9.8, cloth(G)),
                                                                        Box(-4.95, -8.6, 2.2, 9.9, 6.8, 2.8, cloth(G))])],
                           mirror_pair("feye", "head", (-2.5, -9.2, -2.0), (0, 0, 0), [Box(-1.5, -2.0, -1.5, 3.0, 2.4, 3.0, feye)]),
                           [Part("buttons", "body", (0, 0, -1.8), (0, 0, 0), [Box(-0.4, 0.8, -0.2, 0.8, 0.8, 0.3, flat(GD)), Box(-0.4, 2.6, -0.2, 0.8, 0.8, 0.3, flat(GD)),
                                                                              Box(-0.4, 4.6, -0.2, 0.8, 0.8, 0.3, flat(GD))])]])
    return P, info


GIRLS = {"cama": yume, "slime": slime, "conejo": conejo, "panda": panda, "caballo": caballo, "abeja": abeja, "piglin": piglin, "wither": wither,
         "blaze": blaze, "ghast": ghast, "dina": dina, "mita": mita, "warden": warden, "zorro": zorro, "ajolote": ajolote, "allay": allay, "rana": rana}

# Silueta y ojos de TODAS (para que ninguna se parezca a otra).
LOOK = {"kozue": ("athletic", "round"), "homura": ("curvy", "sharp"), "kura": ("std", "big"), "shiori": ("slim", "gentle"),
        "oveja": ("petite", "gentle"), "vaca": ("busty", "gentle"), "cerdo": ("curvy", "big"), "gallina": ("petite", "big"),
        "perro": ("athletic", "sharp"), "gato": ("slim", "cat"), "golem": ("athletic", "round"), "aldeana": ("std", "gentle"),
        "zombie": ("std", "sleepy"), "esqueleto": ("slim", "sharp"), "creeper": ("petite", "round"), "arana": ("curvy", "sharp"),
        "bruja": ("busty", "sharp"), "enderman": ("slim", "sleepy"),
        "cama": ("curvy", "sleepy"), "slime": ("curvy", "big"), "conejo": ("petite", "big"), "panda": ("curvy", "round"), "caballo": ("athletic", "gentle"),
        "abeja": ("petite", "big"), "piglin": ("curvy", "gentle"), "wither": ("athletic", "round"), "blaze": ("slim", "big"), "ghast": ("slim", "sleepy"),
        "dina": ("busty", "round"), "mita": ("curvy", "big"), "warden": ("athletic", "blind"), "zorro": ("slim", "cat"), "ajolote": ("petite", "big"),
        "allay": ("petite", "big"), "rana": ("petite", "round")}
MARKS = {"zombie": [(11, 6, (150, 110, 120)), (12, 7, (150, 110, 120)), (13, 6, (150, 110, 120))], "arana": [(6, 5, (200, 20, 30)), (9, 5, (200, 20, 30))],
         "kozue": [(3, 11, (210, 150, 120)), (12, 11, (210, 150, 120)), (4, 12, (210, 150, 120))]}
FANG = {"piglin"}
