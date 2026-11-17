"""Totally Waifu? 6.0 — 8 chicas nuevas de mesas vanilla (reversibles)."""
import math
import gen_blocky as B
from gen_blocky import Part, Box, flat, pleats, band, hair_p, frill, metal, gem, stock, boot, sole, bow, chain
from gen_mobgirls import girl, hair3, cloth, lt, dk
from gen_girls4 import mirror_pair, stripes, WHITE

SK = B.skin_p


def yunque():                      # Kana — herrera del yunque
    IR, IRL, IRD = (74, 74, 80), (130, 130, 140), (44, 44, 50)
    AP = (110, 70, 40)
    apron = lambda f, x, y, w, h: AP if f == "front" and abs(x - w / 2) < w * 0.32 else IR
    P, info = girl(hair3((70, 70, 78)), "bob", ((40, 40, 50), (110, 110, 130), (220, 220, 240)), (30, 30, 36),
                   top=apron, waist=cloth(AP), hips=cloth(IRD), sleeve=cloth(IR), upper=SK, fore=metal(IRD, IRL), hand=flat((60, 40, 30)),
                   thigh=SK, calf=stock(IRD, (30, 30, 34)), shoes=(boot((50, 36, 26), (80, 60, 40), (30, 20, 14), accent=IRL), sole((20, 20, 22)), 9.6, metal(IR, IRL)),
                   skirt_spec=(cloth(AP), flat(IRL), 4.6, 6.6, 0.26),
                   extras=[[Part("hammer", "body", (1.8, 1.6, 2.2), (0, 0, 0.5), [Box(-0.4, -1.0, -0.4, 0.8, 7.0, 0.8, flat((110, 76, 44))),
                                                                                Box(-1.6, -2.4, -0.9, 3.2, 1.6, 1.8, metal(IR, IRL))])],
                           [Part("anvil_clip", "head", (3.6, -7.0, -3.4), (0, 0, 0.2), [Box(-1.0, -0.6, -0.3, 2.0, 0.6, 0.6, flat(IRD)), Box(-0.5, 0, -0.3, 1.0, 0.6, 0.6, flat(IRD)),
                                                                                       Box(-0.8, 0.6, -0.3, 1.6, 0.5, 0.6, flat(IRD))])]])
    return P, info


def muela():                       # Togi — afiladora
    ST, STL, WD = (140, 140, 140), (186, 186, 186), (120, 84, 50)
    P, info = girl(hair3((200, 204, 212)), "pony", ((40, 70, 110), (110, 150, 200), (220, 240, 255)), (60, 60, 70),
                   top=cloth(ST), waist=cloth(WD), hips=cloth(ST), sleeve=frill(STL, ST), upper=SK, fore=SK, hand=SK,
                   thigh=SK, calf=stock((70, 70, 74), (50, 50, 54)), shoes=(boot(WD, lt(WD), dk(WD)), sole(dk(WD)), 11.0, None),
                   skirt_spec=(pleats(ST, STL, (110, 110, 110)), flat(WD), 4.4, 6.6, 0.32),
                   extras=[[Part("wheel", "body", (0, 3.6, 2.1), (0, 0, 0), [Box(-3.0, -3.0, 0, 6.0, 6.0, 1.2,
                                                                              lambda f, x, y, w, h: (110, 110, 110) if (x - w / 2) ** 2 + (y - h / 2) ** 2 > (w * 0.42) ** 2 else STL),
                                                                          Box(-0.6, -0.6, 1.2, 1.2, 1.2, 0.6, flat(WD))])],
                           [Part("spark_clip", "head", (-3.7, -7.0, -3.4), (0, 0, 0.6), [Box(-0.5, -1.2, -0.3, 1.0, 2.4, 0.6, flat((255, 220, 120)))])]])
    return P, info


def telar():                       # Hilda — telar
    PK, YL, BL, WD = (232, 110, 140), (246, 210, 90), (90, 140, 220), (196, 150, 104)
    weave = lambda f, x, y, w, h: [PK, YL, BL, (120, 200, 140)][(y // 5) % 4]
    banner = lambda f, x, y, w, h: (240, 236, 226) if f != "front" else ((200, 60, 80) if abs(x - w / 2) < 3 or abs(y - h * 0.4) < 2 else (240, 236, 226))
    P, info = girl(hair3((240, 170, 120)), "braid", ((60, 110, 40), (130, 190, 80), (230, 250, 190)), (90, 60, 40),
                   top=weave, waist=cloth(WD), hips=weave, sleeve=frill((246, 240, 230), (226, 220, 210)), upper=cloth((246, 240, 230)), fore=SK, hand=SK,
                   thigh=SK, calf=stripes(PK, (246, 240, 230), 6), shoes=(boot(WD, lt(WD), dk(WD)), sole(dk(WD)), 11.4, None),
                   skirt_spec=(weave, frill(YL, (220, 180, 70)), 5.6, 6.6, 0.3),
                   extras=[chain("banner", "body", (0, 0.2, 2.1), (0.08, 0, 0), [(4.4, 6.0, 0.3, 0, 0), (4.0, 5.6, 0.3, 0.05, 0)], banner),
                           [Part("yarn%d" % i, "head", (x, -8.6, -1.2 + i), (0, 0, 0), [Box(-1.0, -1.6, -1.0, 2.0, 2.0, 2.0, flat(c))])
                            for i, (x, c) in enumerate(((-3.0, PK), (3.0, BL)))]])
    return P, info


def cortapiedras():                # Gema — cortapiedras
    ST, STD, AM, EM = (124, 124, 128), (88, 88, 94), (160, 110, 220), (60, 200, 120)
    studs = lambda f, x, y, w, h: AM if (x % 12 == 5 and y % 10 == 4) else (EM if (x % 12 == 11 and y % 10 == 9) else ST)
    P, info = girl(hair3((176, 140, 226)), "twin", ((80, 40, 130), (170, 110, 240), (240, 220, 255)), (70, 50, 100),
                   top=studs, waist=cloth(STD), hips=studs, sleeve=metal(ST, (170, 170, 176)), upper=SK, fore=SK, hand=SK,
                   thigh=SK, calf=stock(STD, (60, 60, 66)), shoes=(boot(STD, ST, (50, 50, 56), accent=AM), sole((40, 40, 44)), 10.8, None),
                   skirt_spec=(studs, flat(AM), 4.0, 6.6, 0.38),
                   extras=[[Part("saw", "head", (0, -8.8, 0.6), (0, 0, 0), [Box(-1.6, -1.6, -0.2, 3.2, 3.2, 0.4,
                                                                             lambda f, x, y, w, h: (220, 220, 230) if (x + y) % 3 else (150, 150, 160))])],
                           mirror_pair("gem_pauld", "right_arm", (0, -1.4, 0), (0, 0, 0.2), [Box(-1.6, -0.9, -1.2, 2.4, 1.2, 2.4, gem(AM, (230, 200, 255)))]),
                           [Part("neck_gem", "body", (0, 0.4, -1.95), (0, 0, 0.78), [Box(-0.6, -0.6, -0.3, 1.2, 1.2, 0.6, gem(EM, (200, 255, 220)))])]])
    return P, info


def herreria():                    # Tetsu — mesa de herrería
    DK, DKL, WD, NE = (46, 42, 50), (80, 74, 86), (100, 70, 46), (70, 60, 66)
    apron = lambda f, x, y, w, h: (30, 28, 32) if f == "front" and abs(x - w / 2) < w * 0.3 else DK
    P, info = girl(hair3((30, 28, 34)), "low", ((110, 60, 10), (220, 140, 40), (255, 220, 150)), (20, 18, 24),
                   top=apron, waist=cloth(WD), hips=cloth(DK), sleeve=cloth(DKL), upper=SK, fore=metal(NE, (110, 100, 106)), hand=flat((40, 30, 24)),
                   thigh=stock(DK, (28, 26, 30)), calf=stock(DK, (28, 26, 30)), shoes=(boot((40, 34, 30), (70, 60, 50), (24, 20, 18), accent=(200, 160, 80)), sole((20, 18, 16)), 8.8, metal(NE, (110, 100, 106))),
                   skirt_spec=None,
                   extras=[[Part("shorts", "body", (0, 0, 0), (0, 0, 0), [Box(-3.8, 7.6, -2.0, 7.6, 2.8, 4.0, cloth(DK))])],
                           [Part("goggles", "head", (0, -6.6, 0), (0, 0, 0), [Box(-4.7, -0.8, -4.75, 9.4, 1.4, 9.5, lambda f, x, y, w, h: None if f in ("top", "bottom") else (60, 50, 40)),
                                                                             Box(-3.4, -1.2, -5.0, 2.6, 2.0, 0.5, gem((120, 200, 230), (220, 250, 255))),
                                                                             Box(0.8, -1.2, -5.0, 2.6, 2.0, 0.5, gem((120, 200, 230), (220, 250, 255)))])],
                           [Part("tongs", "body", (-3.9, 7.0, 0.4), (0, 0, 0.1), [Box(-0.3, 0, -0.3, 0.6, 3.0, 0.6, metal(DKL, (140, 130, 140)))])]])
    return P, info


def cartografia():                 # Carta — cartógrafa exploradora
    BE, GR, BR = (226, 210, 170), (90, 120, 60), (120, 84, 50)
    mapp = lambda f, x, y, w, h: (200, 180, 130) if (x + y) % 9 == 0 else ((110, 160, 210) if (x // 6 + y // 5) % 5 == 0 else (236, 222, 180))
    P, info = girl(hair3((236, 200, 120)), "side", ((40, 90, 50), (90, 160, 90), (210, 250, 200)), (90, 70, 40),
                   top=cloth(BE), waist=cloth(BR), hips=cloth(GR), sleeve=cloth(BE), upper=cloth(BE), fore=SK, hand=SK,
                   thigh=SK, calf=stock((110, 90, 60), (90, 70, 46)), shoes=(boot(BR, lt(BR), dk(BR), lace=(220, 200, 150)), sole(dk(BR)), 9.4, None),
                   skirt_spec=None,
                   extras=[[Part("shorts", "body", (0, 0, 0), (0, 0, 0), [Box(-3.8, 7.6, -2.0, 7.6, 3.0, 4.0, cloth(GR))])],
                           [Part("hat_brim", "head", (0, -7.8, 0), (-0.05, 0, 0.05), [Box(-6.0, -0.5, -6.0, 12.0, 0.6, 12.0, cloth(BR))]),
                            Part("hat_top", "head", (0, -8.2, 0), (-0.05, 0, 0.05), [Box(-4.2, -2.4, -4.2, 8.4, 2.4, 8.4, cloth(BR)),
                                                                                   Box(-4.3, -0.8, -4.3, 8.6, 0.7, 8.6, flat((180, 50, 50)))])],
                           [Part("map_roll", "body", (0, 1.0, 2.3), (0, 0, 1.2), [Box(-0.8, -4.0, -0.8, 1.6, 8.0, 1.6, mapp)])],
                           [Part("compass", "body", (3.8, 7.0, 0.2), (0, 0, 0), [Box(-0.6, 0, -0.9, 1.2, 1.6, 1.8, lambda f, x, y, w, h: (220, 40, 40) if f == "right" and abs(y - h / 2) < 2 else (150, 150, 160))])]])
    return P, info


def ahumador():                    # Kemuri — chef del ahumador
    W, LOG, IR = (248, 248, 246), (90, 70, 46), (110, 110, 116)
    P, info = girl(hair3((150, 110, 80)), "short", ((90, 40, 10), (190, 110, 50), (250, 210, 150)), (60, 40, 26),
                   top=cloth(W), waist=cloth((60, 60, 64)), hips=cloth(LOG), sleeve=cloth(W), upper=cloth(W), fore=SK, hand=SK,
                   thigh=SK, calf=stock((60, 60, 64), (40, 40, 44)), shoes=(boot((40, 40, 44), (70, 70, 74), (24, 24, 26)), sole((20, 20, 22)), 11.6, None),
                   skirt_spec=(cloth(LOG, seam=dk(LOG)), flat(IR), 4.6, 6.6, 0.3),
                   extras=[[Part("chef_hat", "head", (0, -8.6, 0), (0, 0, 0), [Box(-3.6, -1.0, -3.6, 7.2, 1.2, 7.2, flat(W)),
                                                                              Box(-4.4, -5.6, -4.4, 8.8, 4.8, 8.8, lambda f, x, y, w, h: (226, 226, 224) if (x // 8) % 2 else W)])],
                           [Part("apron", "body", (0, 0, 0), (0, 0, 0), [Box(-2.6, 1.0, -2.05, 5.2, 9.6, 0.3, lambda f, x, y, w, h: (240, 230, 214) if y % 30 else (200, 80, 60))])],
                           [Part("ladle", "body", (-3.9, 6.6, 0.2), (0, 0, 0.1), [Box(-0.25, 0, -0.25, 0.5, 3.0, 0.5, metal(IR, (170, 170, 176))),
                                                                                 Box(-0.7, 3.0, -0.7, 1.4, 0.8, 1.4, metal(IR, (170, 170, 176)))])]])
    return P, info


def alto_horno():                  # Rekka — alto horno
    MT, MTL, MTD, OR = (104, 104, 110), (150, 150, 158), (62, 62, 68), (255, 130, 40)
    vents = lambda f, x, y, w, h: OR if (f == "front" and y % 8 < 2 and abs(x - w / 2) < w * 0.3) else (MTL if (x + y) % 15 == 0 else MT)
    P, info = girl(hair3((236, 96, 40)), "wild", ((150, 50, 0), (255, 140, 40), (255, 230, 150)), (110, 40, 20),
                   top=vents, waist=cloth(MTD), hips=vents, sleeve=metal(MT, MTL), upper=SK, fore=metal(MT, MTL), hand=flat(MTD),
                   thigh=SK, calf=metal(MT, MTL), shoes=(boot(MTD, MT, (40, 40, 44), accent=OR), sole((30, 30, 34), OR), 9.0, metal(MT, MTL)),
                   skirt_spec=(vents, flat(OR), 4.0, 6.6, 0.34),
                   extras=[[Part("chimney", "body", (0, 0.4, 2.0), (0, 0, 0), [Box(-1.4, -2.0, 0, 2.8, 6.0, 2.0, metal(MTD, MT)),
                                                                              Box(-1.0, -2.6, 0.4, 2.0, 0.6, 1.2, flat(OR))])],
                           [Part("ember_clip", "head", (3.6, -7.0, -3.4), (0, 0, 0.3), [Box(-0.8, -0.8, -0.3, 1.6, 1.6, 0.6, gem(OR, (255, 230, 150)))])]])
    return P, info


GIRLS = {"yunque": yunque, "muela": muela, "telar": telar, "cortapiedras": cortapiedras, "herreria": herreria,
         "cartografia": cartografia, "ahumador": ahumador, "alto_horno": alto_horno}
LOOK = {"yunque": ("athletic", "round"), "muela": ("slim", "gentle"), "telar": ("curvy", "gentle"), "cortapiedras": ("petite", "big"),
        "herreria": ("athletic", "round"), "cartografia": ("std", "gentle"), "ahumador": ("curvy", "round"), "alto_horno": ("athletic", "big")}
