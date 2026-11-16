"""Totally Waifu? 3.0 — chicas transformadas desde animales y monstruos (mismo estilo de bloques que las 4 de las mesas).

Cada una: cuerpo alto y esbelto (figure de gen_blocky), cabello de bloques con un peinado, ropa con los colores de su
criatura y accesorios que la identifican (orejas, cola, cuernos, cresta, sombrero de bruja, patas de araña...).
"""
import math
import gen_blocky as B
from gen_blocky import Part, Box, flat, grad, pleats, band, hair_p, frill, metal, gem, stock, boot, sole, figure, shoe_parts, skirt, bow, hair_base, chain

mix = B.mix


def lt(c, k=1.18):
    return tuple(min(255, int(v * k)) for v in c)


def dk(c, k=0.7):
    return tuple(int(v * k) for v in c)


def cloth(c, seam=None):
    return grad(c, lt(c), dk(c), seam=seam)


def skin():
    return B.skin_p


# ------------------------------------------------------------------ peinados
def hair_style(style, hp, bangs=None):
    b = bangs or [-5.2, -4.9, -5.5, -5.0, -5.1, -5.5, -4.9, -5.3]
    if style == "bob":
        P = hair_base(hp, b, back_len=6.2)
        P.append(Part("puff_r", "head", (-4.7, -3.6, 0.3), (0, 0, 0.3), [Box(-2.0, -1.0, -3.6, 2.6, 4.2, 7.2, hp)]))
        P.append(Part("nape", "head", (0, -1.8, 2.8), (-0.25, 0, 0), [Box(-4.9, -0.6, -1.6, 9.8, 2.4, 3.6, hp)]))
        return P
    if style == "short":
        return hair_base(hp, b, back_len=5.4, lock_len=4.2)
    if style == "wild":
        P = hair_base(hp, b, back_len=7.0, lock_len=6.0)
        for i, (x, z, r) in enumerate(((-2.8, -1.0, -0.4), (0.0, 0.5, 0.0), (2.6, -0.6, 0.4), (-1.4, 2.6, -0.2), (1.6, 2.4, 0.25))):
            P.append(Part("spike%d" % i, "head", (x, -8.6, z), (0.2 if z > 1 else -0.2, 0, r), [Box(-1.0, -1.8, -1.0, 2.0, 2.0, 2.0, hp)]))
        P.append(Part("wild_back", "head", (0, -1.4, 3.0), (0.15, 0, 0), [Box(-4.6, 0, -1.4, 9.2, 4.5, 2.2, hp)]))
        return P
    if style == "pony":
        P = hair_base(hp, b, back_len=5.8)
        P += chain("tail", "head", (0, -6.6, 4.6), (0.75, 0, 0), [(3.0, 2.8, 2.4, 0, 0), (3.6, 2.5, 2.1, -0.55, 0), (3.4, 2.0, 1.7, -0.3, 0), (2.4, 1.3, 1.1, -0.1, 0)], hp)
        return P
    if style == "twin":
        P = hair_base(hp, b, back_len=6.0)
        for s, nm in ((-1, "r"), (1, "l")):
            P += chain("twin_" + nm, "head", (s * 3.9, -7.2, 2.4), (0.25, 0, -s * 0.35),
                       [(3.6, 2.4, 2.4, 0, 0), (4.0, 2.2, 2.1, 0.1, s * 0.18), (3.4, 1.8, 1.7, 0.1, s * 0.12)], hp)
        return P
    if style == "long":
        P = hair_base(hp, b, back_len=7.6, lock_len=10.0)
        P.append(Part("long_back", "head", (0, -1.0, 3.2), (0.05, 0, 0), [Box(-4.5, 0, -1.4, 9.0, 10.0, 2.0, hp)]))
        return P
    if style == "hime":
        P = hair_base(hp, [-5.2] * 8, back_len=7.8, lock_len=11.0)
        P.append(Part("long_back", "head", (0, -0.6, 3.2), (0.04, 0, 0), [Box(-4.5, 0, -1.4, 9.0, 13.5, 2.0, hp)]))
        return P
    raise ValueError(style)


# ------------------------------------------------------------------ accesorios
def ears(kind, c, inner):
    def ear_p(f, x, y, w, h):
        if f == "front" and 1 < x < w - 2 and y > 2:
            return inner
        return c
    P = []
    if kind in ("cat", "wolf"):
        big = 1.2 if kind == "wolf" else 1.0
        P.append(Part("ear_r", "head", (-2.7, -8.6, -0.4), (0, 0, -0.18), [Box(-1.2 * big, -2.6 * big, -0.5, 2.4 * big, 2.6 * big, 1.0, ear_p),
                                                                         Box(-0.6 * big, -3.4 * big, -0.4, 1.2 * big, 0.9 * big, 0.8, flat(c))]))
    elif kind == "pig":
        P.append(Part("ear_r", "head", (-2.9, -8.6, -1.6), (-0.8, 0, -0.3), [Box(-1.2, -2.4, -0.4, 2.4, 2.4, 0.8, ear_p)]))
    elif kind == "cow":
        P.append(Part("ear_r", "head", (-4.6, -6.6, -0.6), (0, 0, 0.25), [Box(-2.6, -0.7, -0.5, 2.6, 1.4, 1.0, ear_p)]))
    elif kind == "sheep":
        P.append(Part("ear_r", "head", (-4.6, -5.6, 0.2), (0, 0, 0.6), [Box(-2.2, -0.6, -0.5, 2.2, 1.2, 1.0, ear_p)]))
    return P


def horns(kind, c):
    if kind == "cow":
        return [Part("horn_r", "head", (-3.6, -8.4, -0.8), (0, 0, -0.5), [Box(-0.5, -2.0, -0.5, 1.0, 2.0, 1.0, flat(c)), Box(-1.2, -2.4, -0.4, 1.0, 0.8, 0.8, flat(lt(c)))])]
    # oveja: cuernos enroscados a los lados
    return [Part("horn_r", "head", (-4.6, -6.8, -0.6), (0, 0, 0), [Box(-1.4, -1.2, -1.4, 1.4, 2.4, 2.8, flat(c)),
                                                                  Box(-2.0, 0.6, -1.0, 1.0, 2.2, 2.0, flat(dk(c, 0.85))),
                                                                  Box(-1.6, 2.4, -1.8, 1.2, 1.0, 1.4, flat(c))])]


def tail(kind, c, tip=None):
    tip = tip or c
    if kind == "cat":
        return chain("tail", "body", (0, 9.4, 1.9), (0.9, 0, 0), [(3.0, 0.9, 0.9, 0, 0), (3.0, 0.8, 0.8, -0.5, 0), (2.4, 0.8, 0.8, -0.6, 0)], flat(c)) + \
            [Part("tail_tip", "tail2", (0, 2.2, 0), (0, 0, 0), [Box(-0.45, 0, -0.45, 0.9, 1.0, 0.9, flat(tip))])]
    if kind == "wolf":
        return chain("tail", "body", (0, 9.2, 1.9), (0.7, 0, 0), [(2.8, 2.0, 2.0, 0, 0), (2.8, 2.2, 2.2, 0.2, 0), (2.0, 1.6, 1.6, 0.2, 0)], flat(c)) + \
            [Part("tail_tip", "tail2", (0, 1.8, 0), (0, 0, 0), [Box(-0.7, 0, -0.7, 1.4, 1.0, 1.4, flat(tip))])]
    if kind == "pig":
        return [Part("tail0", "body", (0, 9.4, 1.9), (0.4, 0, 0), [Box(-0.35, 0, -0.35, 0.7, 1.2, 0.7, flat(c))]),
                Part("tail1", "tail0", (0, 1.0, 0), (0, 0, 1.6), [Box(-0.35, 0, -0.35, 0.7, 1.0, 0.7, flat(c))]),
                Part("tail2", "tail1", (0, 0.9, 0), (0, 0, 1.6), [Box(-0.35, 0, -0.35, 0.7, 0.8, 0.7, flat(c))])]
    if kind == "sheep":
        return [Part("tail0", "body", (0, 9.0, 1.9), (0.3, 0, 0), [Box(-1.2, 0, -0.6, 2.4, 1.8, 1.8, flat(c))])]
    if kind == "cow":
        return chain("tail", "body", (0, 8.8, 1.9), (0.3, 0, 0), [(3.2, 0.6, 0.6, 0, 0), (2.4, 0.6, 0.6, -0.2, 0)], flat(c)) + \
            [Part("tail_tip", "tail1", (0, 2.2, 0), (0, 0, 0), [Box(-0.7, 0, -0.7, 1.4, 1.4, 1.4, flat(tip))])]
    return []


def collar(c, bell=None, tag=None):
    P = [Part("collar", "body", (0, 0, 0), (0, 0, 0), [Box(-1.3, -0.9, -1.15, 2.6, 0.7, 2.3, flat(c))])]
    if bell:
        P.append(Part("bell", "body", (0, -0.3, -1.35), (0, 0, 0), [Box(-0.5, 0, -0.45, 1.0, 1.0, 0.9, metal(bell, lt(bell)))]))
    if tag:
        P.append(Part("tag", "body", (0, -0.3, -1.3), (0, 0, 0.78), [Box(-0.35, -0.35, -0.2, 0.7, 0.7, 0.3, metal(tag, lt(tag)))]))
    return P


def witch_hat(c, band_c, gem_c):
    def hp(f, x, y, w, h):
        return lt(c) if f == "top" else c
    return [Part("hat_brim", "head", (0, -8.6, 0), (0, 0, 0.06), [Box(-6.4, -0.5, -6.4, 12.8, 0.6, 12.8, flat(dk(c, 0.9)))]),
            Part("hat_cone", "hat_brim", (0, -0.5, 0), (0, 0, 0), [Box(-4.0, -3.4, -4.0, 8.0, 3.4, 8.0, hp)]),
            Part("hat_band", "hat_cone", (0, -0.2, 0), (0, 0, 0), [Box(-4.15, -1.0, -4.15, 8.3, 1.0, 8.3, flat(band_c))]),
            Part("hat_gem", "hat_cone", (0, -0.7, -4.3), (0, 0, 0.78), [Box(-0.6, -0.6, -0.25, 1.2, 1.2, 0.5, gem(gem_c, lt(gem_c, 1.5)))]),
            Part("hat_mid", "hat_cone", (0, -3.4, 0.5), (-0.2, 0, 0.1), [Box(-2.6, -3.0, -2.6, 5.2, 3.0, 5.2, hp)]),
            Part("hat_tip", "hat_mid", (0, -3.0, 0.4), (-0.45, 0, 0.15), [Box(-1.2, -2.8, -1.2, 2.4, 2.8, 2.4, hp)])]


def spider_legs(c, joint):
    P = []
    for i, (y, a) in enumerate(((2.0, 0.5), (3.6, 0.15), (5.2, -0.2))):
        for s, nm in ((-1, "r"), (1, "l")):
            n = "sleg%d%s" % (i, nm)
            P.append(Part(n, "body", (s * 1.4, y, 2.0), (0, s * a, -s * 0.45), [Box(-0.45, -0.45, -0.45, 6.4, 0.9, 0.9, flat(c)) if s > 0 else Box(-5.95, -0.45, -0.45, 6.4, 0.9, 0.9, flat(c))]))
            P.append(Part(n + "b", n, (s * 5.8, 0, 0), (0, 0, s * 0.5), [Box(-0.4, -0.4, -0.4, 0.8, 5.0, 0.8, flat(joint))]))
    return P


def creeper_hood(c):
    face = {(2, 1), (3, 1), (6, 1), (7, 1), (2, 2), (3, 2), (6, 2), (7, 2), (4, 3), (5, 3), (3, 4), (4, 4), (5, 4), (6, 4)}

    def front(f, x, y, w, h):
        if f == "front":
            cx, cy = int(x / w * 10), int(y / h * 6)
            if (cx, cy) in face:
                return (24, 34, 24)
        return c if (x // 6 + y // 6) % 3 else lt(c, 1.12)
    return [Part("hood_top", "head", (0, 0, 0), (0, 0, 0), [Box(-4.95, -9.6, -4.95, 9.9, 1.2, 10.0, front)]),
            Part("hood_back", "head", (0, 0, 0), (0, 0, 0), [Box(-4.95, -8.6, 2.6, 9.9, 7.8, 2.4, front)]),
            Part("hood_side_r", "head", (0, 0, 0), (0, 0, 0), [Box(-5.1, -8.6, -4.8, 1.0, 6.6, 7.5, front)]),
            Part("hood_brim", "head", (0, 0, 0), (0, 0, 0), [Box(-4.95, -9.6, -5.2, 9.9, 3.0, 1.0, front)])]


# ------------------------------------------------------------------ constructor genérico
def girl(hair, style, iris, lash, top, waist, hips, sleeve, upper, fore, hand, thigh, calf, shoes, skirt_spec=None, extras=(), bangs=None, skin_c=None):
    P = figure(top=top, waist=waist, hips=hips, sleeve=sleeve, upper=upper, fore=fore, hand=hand, thigh=thigh, calf=calf)
    P += shoe_parts(*shoes)
    if skirt_spec:
        P += skirt(*skirt_spec)
    P += hair_style(style, hair_p(*hair), bangs)
    for e in extras:
        P += e
    info = dict(iris=iris, lash=lash, hair=hair[0])
    if skin_c:
        info["skin"] = skin_c
    return P, info


def hair3(c):
    return (c, lt(c, 1.3), dk(c, 0.68))


# ================================================================== GRANJA
def oveja():
    W, WS = (246, 244, 238), (214, 210, 204)
    wool = lambda f, x, y, w, h: W if ((x // 3 + y // 3) % 3) else WS
    PINK = (236, 170, 186)
    return girl(hair3((238, 232, 220)), "bob", ((70, 70, 90), (130, 130, 160), (210, 210, 230)), (60, 50, 60),
                top=wool, waist=cloth(PINK), hips=cloth(PINK), sleeve=wool, upper=wool, fore=B.skin_p, hand=B.skin_p,
                thigh=band(B.skin_p, [(0.55, 0.62, PINK), (0.62, 1.0, W)]), calf=stock(W, WS),
                shoes=(boot((90, 80, 76), (70, 62, 58), (50, 44, 40)), sole((40, 34, 30)), 11.4, flat(wool(None, 0, 0, 1, 1) if False else W)),
                skirt_spec=(pleats(PINK, lt(PINK), dk(PINK)), frill(W, WS), 4.4, 6.6, 0.38),
                extras=[ears("sheep", W, PINK), horns("sheep", (220, 200, 160)), tail("sheep", W), collar(PINK, bell=(230, 190, 70)),
                        [Part("fluff_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.9, -1.9, -1.8, 3.6, 3.6, 3.6, wool)])]])


def vaca():
    W, BLK = (246, 244, 240), (40, 36, 40)
    spots = lambda f, x, y, w, h: BLK if ((x * 7 + y * 3) % 37 < 9 and (x // 9 + y // 7) % 2 == 0) else W
    DEN = (70, 100, 150)
    return girl(hair3((60, 50, 50)), "long", ((90, 60, 40), (160, 110, 70), (220, 180, 130)), (40, 30, 30),
                top=spots, waist=cloth(DEN), hips=cloth(DEN), sleeve=spots, upper=B.skin_p, fore=B.skin_p, hand=B.skin_p,
                thigh=B.skin_p, calf=band(B.skin_p, [(0.3, 1.0, W)]),
                shoes=(boot((110, 80, 54), (90, 64, 42), (64, 44, 28), lace=W), sole((60, 42, 28)), 9.8, flat((90, 64, 42))),
                skirt_spec=(cloth(DEN, seam=dk(DEN)), flat(dk(DEN)), 3.4, 6.6, 0.3),
                extras=[ears("cow", W, (236, 170, 170)), horns("cow", (232, 224, 200)), tail("cow", W, BLK), collar((200, 40, 40), bell=(230, 190, 70)),
                        [Part("bandana", "head", (0, 0, 0), (0, 0, 0), [Box(-4.7, -7.4, -4.8, 9.4, 1.0, 9.7, lambda f, x, y, w, h: None if f in ("top", "bottom") else (200, 40, 40))])]])


def cerdo():
    PK, PKH, PKD = (242, 166, 176), (252, 200, 206), (210, 120, 136)
    return girl(hair3((236, 150, 170)), "twin", ((150, 60, 80), (220, 110, 130), (255, 190, 200)), (110, 50, 70),
                top=cloth(PK), waist=cloth(PKD), hips=cloth(PK), sleeve=frill(PKH, PK), upper=B.skin_p, fore=B.skin_p, hand=B.skin_p,
                thigh=band(B.skin_p, [(0.6, 0.68, PKD), (0.68, 1.0, (252, 240, 242))]), calf=stock((252, 240, 242), (226, 212, 216)),
                shoes=(boot(PKD, PK, dk(PKD)), sole((120, 70, 80)), 11.4, None),
                skirt_spec=(pleats(PK, PKH, PKD), frill((252, 240, 242), PKH), 4.6, 6.6, 0.4),
                extras=[ears("pig", PK, PKD), tail("pig", PK), bow("chest_bow", "body", (0, 0.4, -1.9), (0, 0, 0), flat(PKD), 0.9),
                        bow("hair_bow", "head", (0, -8.9, 0.6), (0, 0, 0), flat(PKD), 0.9)])


def gallina():
    W, WS = (250, 250, 246), (220, 220, 214)
    RED, YEL = (220, 40, 40), (250, 200, 50)
    feather = lambda f, x, y, w, h: WS if (y % 5 == 0 and (x // 4) % 2 == 0) else W
    return girl(hair3((250, 246, 236)), "short", ((140, 90, 10), (230, 170, 30), (255, 230, 120)), (80, 60, 40),
                top=feather, waist=cloth(YEL), hips=feather, sleeve=feather, upper=feather, fore=B.skin_p, hand=B.skin_p,
                thigh=B.skin_p, calf=band(B.skin_p, [(0.5, 1.0, YEL)]),
                shoes=(boot(YEL, lt(YEL, 1.1), dk(YEL)), sole((200, 140, 30)), 11.0, None),
                skirt_spec=(feather, frill(W, WS), 4.2, 6.6, 0.45),
                extras=[[Part("comb", "head", (0, -9.0, -1.0), (0, 0, 0), [Box(-0.5, -2.0, -1.6, 1.0, 2.0, 3.2, flat(RED)), Box(-0.5, -2.8, -0.6, 1.0, 0.8, 1.2, flat(RED))])],
                        [Part("wattle", "body", (0, -0.6, -1.3), (0, 0, 0), [Box(-0.6, 0, -0.4, 1.2, 1.4, 0.6, flat(RED))])],
                        [Part("wing_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.2, -1.6, -1.2, 0.8, 8.0, 3.4, feather)])],
                        [Part("tailfeathers", "body", (0, 7.2, 1.8), (0.6, 0, 0), [Box(-2.0, -0.4, 0, 4.0, 3.2, 0.6, feather)])]])


# ================================================================== COMPAÑERAS
def perro():
    G1, G2, G3 = (190, 190, 196), (150, 150, 158), (236, 236, 240)
    JK = (70, 80, 100)
    return girl(hair3((176, 176, 184)), "wild", ((140, 90, 20), (220, 160, 40), (255, 220, 120)), (50, 50, 60),
                top=cloth(JK), waist=cloth((40, 40, 50)), hips=cloth((40, 40, 50)), sleeve=cloth(JK), upper=cloth(JK), fore=cloth(G3), hand=flat((40, 40, 50)),
                thigh=B.skin_p, calf=stock((40, 40, 50), (26, 26, 34)),
                shoes=(boot((60, 60, 70), (80, 80, 92), (36, 36, 44), accent=(200, 40, 40)), sole((30, 30, 36)), 8.8, flat((200, 40, 40))),
                skirt_spec=None,
                extras=[ears("wolf", G1, (236, 200, 200)), tail("wolf", G1, G3), collar((200, 40, 40), tag=(230, 190, 70)),
                        [Part("shorts", "body", (0, 0, 0), (0, 0, 0), [Box(-3.75, 7.8, -2.0, 7.5, 3.0, 4.0, cloth((40, 40, 50), seam=(70, 70, 84)))])],
                        [Part("fur_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.5, 8.0, -1.4, 2.8, 1.4, 2.8, flat(G3))])]])


def gato():
    OR, ORD, CR = (230, 150, 60), (190, 110, 40), (250, 236, 210)
    stripes = lambda f, x, y, w, h: ORD if (y // 5) % 3 == 0 else OR
    return girl(hair3((226, 140, 60)), "bob", ((40, 110, 40), (110, 200, 70), (210, 255, 140)), (60, 36, 20),
                top=lambda f, x, y, w, h: CR if f == "front" and 0.3 * w < x < 0.7 * w else stripes(f, x, y, w, h), waist=cloth((60, 50, 60)), hips=cloth((60, 50, 60)),
                sleeve=stripes, upper=stripes, fore=stripes, hand=flat(CR),
                thigh=band(B.skin_p, [(0.55, 0.63, OR), (0.63, 1.0, (50, 44, 54))]), calf=stock((50, 44, 54), (34, 30, 38)),
                shoes=(boot(CR, (226, 214, 190), (190, 176, 150)), sole((150, 136, 110)), 11.4, None),
                skirt_spec=(pleats((60, 50, 60), (84, 74, 86), (40, 34, 42)), flat(OR), 4.2, 6.6, 0.34),
                extras=[ears("cat", OR, (250, 200, 200)), tail("cat", OR, CR), collar((60, 120, 200), bell=(230, 190, 70)),
                        [Part("paw_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.35, 8.8, -1.35, 2.5, 2.6, 2.7, flat(CR))])]])


def golem():
    IR, IRH, IRD = (210, 208, 200), (236, 234, 228), (160, 156, 150)
    VINE = (70, 130, 50)
    plate = lambda f, x, y, w, h: VINE if ((x + y * 3) % 23 < 2 and f == "front") else (IRH if (x + y) % 11 == 0 else IR)
    return girl(hair3((150, 110, 70)), "long", ((120, 40, 20), (220, 90, 40), (255, 170, 110)), (60, 40, 30),
                top=plate, waist=cloth((80, 70, 66)), hips=plate, sleeve=plate, upper=B.skin_p, fore=metal(IR, IRH), hand=metal(IRD, IR),
                thigh=B.skin_p, calf=metal(IR, IRH),
                shoes=(boot(IRD, IR, dk(IRD)), sole((110, 106, 100)), 7.8, metal(IR, IRH)),
                skirt_spec=(plate, flat(IRD), 4.0, 6.6, 0.25),
                extras=[[Part("pauldron_r", "right_arm", (0, -1.4, 0), (0, 0, 0.25), [Box(-2.2, -0.8, -1.8, 3.4, 1.8, 3.6, metal(IR, IRH))])],
                        [Part("vine", "body", (0, 0, 0), (0, 0, 0), [Box(-3.4, 1.0, -2.0, 1.0, 6.0, 0.4, flat(VINE)), Box(1.8, 2.0, -2.0, 1.0, 4.0, 0.4, flat(VINE))])],
                        [Part("poppy", "head", (3.4, -7.0, -3.4), (0, 0, 0.3), [Box(-0.9, -0.9, -0.4, 1.8, 1.8, 0.8, flat((220, 30, 30))), Box(-0.3, -0.3, -0.6, 0.6, 0.6, 0.3, flat((30, 30, 30)))])]])


def aldeana():
    ROBE, ROBE_D = (120, 90, 64), (88, 64, 44)
    GREEN = (70, 140, 80)
    EM = (40, 200, 90)
    return girl(hair3((110, 70, 44)), "long", ((60, 110, 50), (110, 170, 80), (200, 240, 160)), (60, 40, 30),
                top=cloth(ROBE, seam=ROBE_D), waist=cloth(GREEN), hips=cloth(ROBE), sleeve=cloth(ROBE), upper=cloth(ROBE), fore=cloth(ROBE), hand=B.skin_p,
                thigh=B.skin_p, calf=stock((90, 70, 54), (70, 52, 40)),
                shoes=(boot((90, 64, 40), (110, 80, 52), (64, 44, 28)), sole((54, 38, 24)), 11.0, None),
                skirt_spec=(pleats(ROBE, lt(ROBE), ROBE_D), flat(GREEN), 6.6, 6.4, 0.24),
                extras=[[Part("shawl", "body", (0, 0, 0), (0, 0, 0), [Box(-3.7, -0.6, -1.95, 7.4, 2.0, 3.9, cloth(GREEN))])],
                        [Part("emerald", "body", (0, 1.4, -2.05), (0, 0, 0.78), [Box(-0.5, -0.5, -0.25, 1.0, 1.0, 0.4, gem(EM, lt(EM, 1.5)))])],
                        [Part("headscarf", "head", (0, 0, 0), (0, 0, 0), [Box(-4.7, -9.0, -4.0, 9.4, 1.4, 8.8, flat(GREEN))])],
                        bow("hair_tie", "head", (0, -3.0, 4.7), (0, 0, 0), flat(GREEN), 0.8)])


# ================================================================== MONSTRUOS
def zombie():
    SK = (196, 222, 200)
    SH, SH2 = (60, 150, 160), (40, 110, 120)
    PU = (70, 60, 140)
    torn = lambda f, x, y, w, h: T if (f == "front" and y > h - 4 and (x * 5) % 7 < 2) else (SH if (x + y) % 13 else SH2)
    return girl(hair3((60, 80, 60)), "wild", ((20, 90, 30), (80, 180, 60), (180, 255, 130)), (30, 50, 30),
                top=torn, waist=cloth(SH2), hips=cloth(PU), sleeve=torn, upper=B.skin_p, fore=B.skin_p, hand=B.skin_p,
                thigh=B.skin_p, calf=band(B.skin_p, [(0.2, 0.3, (236, 230, 220)), (0.5, 0.58, (236, 230, 220))]),
                shoes=(boot((60, 54, 50), (80, 72, 66), (40, 36, 34)), sole((34, 30, 28)), 10.6, None),
                skirt_spec=(cloth(PU), frill(PU, dk(PU)), 3.6, 6.6, 0.3), skin_c=(SK, (170, 196, 176)),
                extras=[[Part("bandage", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.3, 6.4, -1.2, 2.4, 1.0, 2.4, flat((236, 230, 220)))])],
                        [Part("stitch", "head", (2.6, -2.4, -4.05), (0, 0, 0.5), [Box(-0.8, -0.1, -0.1, 1.6, 0.25, 0.2, flat((90, 60, 60)))])]])


def esqueleto():
    BONE, BONE_S, GR = (236, 234, 226), (196, 194, 186), (90, 90, 96)
    ribs = lambda f, x, y, w, h: BONE if (f == "front" and (y // 4) % 2 == 0 and 2 < x < w - 3) else GR
    return girl(hair3((214, 214, 222)), "hime", ((60, 60, 70), (140, 140, 160), (220, 220, 240)), (40, 40, 50),
                top=ribs, waist=cloth((60, 60, 66)), hips=cloth(GR), sleeve=cloth(GR), upper=cloth(GR), fore=cloth(BONE_S), hand=flat(BONE),
                thigh=B.skin_p, calf=band(stock((60, 60, 66), (40, 40, 46)), [(0.0, 0.1, BONE)]),
                shoes=(boot(GR, (110, 110, 118), (60, 60, 66), accent=BONE), sole((50, 50, 56)), 9.4, flat(BONE)),
                skirt_spec=(pleats(GR, (110, 110, 118), (60, 60, 66)), flat(BONE), 4.6, 6.6, 0.3), skin_c=((250, 246, 244), (226, 220, 222)),
                extras=[[Part("bow_back", "body", (1.0, 2.0, 2.2), (0, 0, 0.6), [Box(-0.3, -4.5, -0.3, 0.6, 9.0, 0.6, flat((120, 90, 60)))])],
                        [Part("quiver", "body", (-1.4, 1.0, 2.0), (0, 0, -0.4), [Box(-0.9, 0, 0, 1.8, 5.0, 1.4, flat((110, 80, 54))),
                                                                              Box(-0.7, -1.2, 0.2, 1.4, 1.2, 1.0, flat(BONE))])],
                        [Part("skull_clip", "head", (3.4, -6.8, -3.6), (0, 0, 0), [Box(-0.8, -0.8, -0.4, 1.6, 1.6, 0.8, flat(BONE))])]])


def creeper():
    GR, GRL, GRD = (90, 176, 70), (130, 210, 100), (54, 120, 44)
    pix = lambda f, x, y, w, h: GRL if (x // 4 + y // 4) % 3 == 0 else (GRD if (x // 4 * 3 + y // 4) % 5 == 0 else GR)
    return girl(hair3((60, 110, 50)), "short", ((20, 20, 20), (40, 80, 40), (120, 220, 100)), (20, 30, 20),
                top=pix, waist=cloth((30, 30, 34)), hips=cloth((30, 30, 34)), sleeve=pix, upper=pix, fore=pix, hand=B.skin_p,
                thigh=B.skin_p, calf=stock((30, 30, 34), (20, 20, 24)),
                shoes=(boot(GR, GRL, GRD), sole((30, 40, 30)), 10.4, flat(GRD)),
                skirt_spec=(pleats((30, 30, 34), (50, 50, 56), (20, 20, 24)), flat(GR), 4.0, 6.6, 0.32),
                extras=[creeper_hood(GR), [Part("tnt_r", "body", (-3.8, 6.8, -0.8), (0, 0, 0), [Box(-0.6, 0, -0.8, 1.2, 1.8, 1.6,
                        lambda f, x, y, w, h: (240, 240, 240) if abs(y - h / 2) < 2 else (210, 50, 40))])]])


def arana():
    BL, BLH = (36, 32, 40), (64, 58, 72)
    RED = (200, 20, 30)
    return girl(hair3((40, 36, 44)), "bob", ((120, 0, 10), (220, 30, 40), (255, 120, 120)), (20, 10, 14),
                top=lambda f, x, y, w, h: RED if (f == "front" and abs(x - w / 2) < 2 and y < h * 0.5) else (BLH if (x + y) % 9 == 0 else BL),
                waist=cloth(BL), hips=cloth(BL), sleeve=cloth(BL), upper=cloth(BL), fore=cloth(BLH), hand=flat(BL),
                thigh=B.skin_p, calf=lambda f, x, y, w, h: (BLH if (x + y) % 6 == 0 or (x - y) % 6 == 0 else BL),
                shoes=(boot(BL, BLH, (20, 18, 24), accent=RED), sole((20, 18, 24), RED), 8.4, None),
                skirt_spec=(cloth(BL), flat(RED), 4.4, 6.6, 0.3),
                extras=[spider_legs(BL, BLH), [Part("eyes_band", "head", (0, 0, 0), (0, 0, 0), [Box(-4.7, -8.4, -4.8, 9.4, 0.8, 9.7,
                        lambda f, x, y, w, h: None if f in ("top", "bottom") else (RED if (x // 6) % 3 == 0 else BL))])]])


def bruja():
    PU, PUH, PUD = (96, 44, 120), (130, 70, 160), (60, 26, 80)
    GR = (90, 160, 60)
    return girl(hair3((40, 30, 50)), "long", ((100, 20, 140), (170, 80, 210), (230, 170, 255)), (30, 16, 40),
                top=cloth(PU, seam=PUD), waist=lambda f, x, y, w, h: GR if f == "front" and abs(x - w / 2) < 1.5 else PUD, hips=cloth(PU),
                sleeve=cloth(PU), upper=cloth(PU), fore=cloth(PUH), hand=B.skin_p,
                thigh=B.skin_p, calf=lambda f, x, y, w, h: (PU if (y // 4) % 2 == 0 else (30, 24, 34)),
                shoes=(boot((40, 30, 46), (60, 48, 70), (24, 18, 28), accent=GR), sole((24, 18, 28)), 8.8, flat(PUD)),
                skirt_spec=(pleats(PU, PUH, PUD), frill(PUD, PU), 5.6, 6.6, 0.3),
                extras=[witch_hat((60, 30, 76), (40, 24, 50), GR), [Part("potion", "body", (3.6, 7.0, 0.0), (0, 0, -0.1), [Box(-0.6, 0, -0.6, 1.2, 1.6, 1.2,
                        lambda f, x, y, w, h: (220, 60, 160) if y > 3 else (230, 230, 240))])]])


def enderman():
    BL, BLH = (24, 20, 30), (48, 40, 60)
    PUR, PURL = (190, 80, 230), (230, 160, 255)
    return girl(hair3((26, 22, 32)), "hime", ((140, 30, 200), (210, 90, 255), (250, 200, 255)), (14, 10, 20),
                top=lambda f, x, y, w, h: PUR if (f == "front" and abs(x - w / 2) < 1) else (BLH if (x + y) % 11 == 0 else BL),
                waist=cloth(BL), hips=cloth(BL), sleeve=cloth(BL), upper=cloth(BL), fore=cloth(BL), hand=B.skin_p,
                thigh=stock(BL, (14, 12, 18)), calf=stock(BL, (14, 12, 18)),
                shoes=(boot(BL, BLH, (10, 8, 14), accent=PUR), sole((10, 8, 14), PUR), 7.6, flat(PUR)),
                skirt_spec=(cloth(BL), flat(PUR), 7.0, 6.4, 0.22), skin_c=((232, 222, 240), (204, 192, 216)),
                extras=[[Part("pearl", "body", (0, 0.4, -1.95), (0, 0, 0), [Box(-0.6, -0.6, -0.3, 1.2, 1.2, 0.6, gem((40, 120, 110), (140, 230, 210)))])],
                        [Part("ribbon", "head", (0, 0, 0), (0, 0, 0), [Box(-4.7, -7.8, -4.8, 9.4, 0.7, 9.7,
                         lambda f, x, y, w, h: None if f in ("top", "bottom") else PUR)])],
                        [Part("shard_r", "head", (-4.8, -2.4, -0.4), (0, 0, 0), [Box(-0.25, 0, -0.25, 0.5, 1.4, 0.5, gem(PUR, PURL))])]])


T = (0, 0, 0, 0)
GIRLS = {"oveja": oveja, "vaca": vaca, "cerdo": cerdo, "gallina": gallina, "perro": perro, "gato": gato, "golem": golem, "aldeana": aldeana,
         "zombie": zombie, "esqueleto": esqueleto, "creeper": creeper, "arana": arana, "bruja": bruja, "enderman": enderman}
