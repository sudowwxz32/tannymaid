"""Totally Waifu? 7.0 — 8 waifus LEYENDA (se desbloquean al completar los extras)."""
import math
import gen_blocky as B
from gen_blocky import Part, Box, flat, pleats, band, hair_p, frill, metal, gem, stock, boot, sole, bow, chain
from gen_mobgirls import girl, hair3, cloth, lt, dk
from gen_girls4 import mirror_pair, stripes, WHITE

SK = B.skin_p


def _ear(c, inner, tip=None):
    def fn(f, x, y, w, h):
        if tip and y < 3:
            return tip
        if f == "front" and 1 < x < w - 2 and y > 3:
            return inner
        return c
    return fn


def burro():                       # Bruna — burra de carga, testaruda y fuerte
    GR, GRD, GRL = (130, 116, 104), (84, 72, 64), (190, 178, 166)
    DEN, TAN = (70, 96, 140), (196, 160, 110)

    def overall(f, x, y, w, h):
        if f == "front" and abs(x - w / 2) < w * 0.3 and y > 3:
            return DEN
        if f in ("front", "back") and (abs(x - w * 0.22) < 1 or abs(x - w * 0.78) < 1):
            return dk(DEN)
        return (236, 228, 214)
    P, info = girl(hair3((86, 70, 60)), "pony", ((50, 36, 26), (110, 84, 60), (210, 180, 140)), (40, 30, 26),
                   top=overall, waist=cloth(DEN), hips=cloth(DEN), sleeve=cloth((236, 228, 214)), upper=SK, fore=SK, hand=flat((120, 90, 60)),
                   thigh=cloth(DEN), calf=cloth(DEN), shoes=(boot(GRD, GR, (50, 44, 40)), sole((40, 36, 32)), 9.6, flat(GRD)),
                   skirt_spec=None,
                   extras=[mirror_pair("dear", "head", (-2.4, -8.6, 0.2), (0, 0, -0.32), [Box(-0.9, -4.4, -0.6, 1.8, 4.4, 1.2, _ear(GR, GRL, (40, 34, 30)))]),
                           [Part("muzzle_band", "head", (0, -8.2, 0), (0, 0, 0), [Box(-4.3, -0.6, -4.3, 8.6, 0.7, 8.6, flat((180, 60, 50)))])],
                           mirror_pair("saddlebag", "body", (-3.9, 7.4, 0), (0, 0, 0.05), [Box(-1.6, 0, -1.6, 1.6, 2.8, 3.2, metal(TAN, lt(TAN)))]),
                           chain("dtail", "body", (0, 8.8, 2.0), (0.4, 0, 0), [(3.2, 0.8, 0.8, 0, 0), (1.6, 1.4, 1.4, -0.1, 0)], flat(GRD))])
    return P, info


def cabra():                       # Chivi — cabra montañesa, salta y embiste
    W, WS, HORN = (246, 242, 232), (220, 214, 200), (150, 140, 128)
    SC, BR = (90, 150, 110), (130, 96, 64)
    P, info = girl(hair3((240, 234, 220)), "wavy", ((90, 70, 20), (190, 160, 70), (250, 230, 160)), (70, 60, 50),
                   top=cloth(W), waist=cloth(BR), hips=cloth(SC), sleeve=frill(W, WS), upper=cloth(W), fore=SK, hand=SK,
                   thigh=SK, calf=stock((230, 224, 212), WS), shoes=(boot((60, 56, 52), (90, 84, 78), (40, 38, 36)), sole((30, 28, 26)), 11.0, flat(W)),
                   skirt_spec=(pleats(SC, lt(SC), dk(SC)), flat(BR), 4.2, 6.6, 0.32),
                   extras=[mirror_pair("ghorn", "head", (-2.4, -8.4, 0.4), (-0.5, 0, -0.25), [Box(-0.6, -2.6, -0.6, 1.2, 2.6, 1.2, flat(HORN)),
                                                                                            Box(-0.5, -3.8, 0.0, 1.0, 1.4, 1.0, flat(lt(HORN)))]),
                           mirror_pair("gear", "head", (-4.3, -6.4, -0.4), (0, 0, 0.9), [Box(-2.2, -0.6, -0.5, 2.2, 1.2, 1.0, _ear(W, (236, 180, 180)))]),
                           [Part("scarf", "body", (0, -0.2, 0), (0, 0, 0), [Box(-2.4, -0.8, -1.6, 4.8, 1.4, 3.2, stripes((200, 60, 60), W, 3))]),
                            Part("scarf_end", "body", (1.6, 0.4, -1.7), (0, 0, 0.15), [Box(-0.6, 0, -0.3, 1.2, 3.6, 0.6, stripes((200, 60, 60), W, 3))])],
                           [Part("bell", "body", (0, 0.6, -1.9), (0, 0, 0), [Box(-0.5, 0, -0.4, 1.0, 1.0, 0.8, metal((220, 180, 70), (255, 230, 140)))])]])
    return P, info


def llama():                       # Kusi — llama andina, poncho de colores y chullo
    CR, CRD = (236, 220, 186), (206, 186, 150)
    R, Y, GN, BL = (210, 50, 60), (246, 200, 60), (60, 160, 90), (60, 110, 200)

    def poncho(f, x, y, w, h):
        band_ = [R, Y, GN, BL, R][(y // 4) % 5]
        if (x + y) % 7 == 0:
            return WHITE
        return band_ if y > 4 else R

    def chullo(f, x, y, w, h):
        return [R, Y, BL, GN][(y // 2) % 4] if f != "top" else R
    P, info = girl(hair3((196, 160, 110)), "braid", ((60, 40, 20), (140, 100, 60), (230, 200, 150)), (60, 40, 30),
                   top=poncho, waist=cloth(R), hips=cloth((40, 40, 50)), sleeve=cloth(CR), upper=cloth(CR), fore=SK, hand=SK,
                   thigh=SK, calf=stock(CR, CRD), shoes=(boot((110, 70, 40), (140, 96, 60), (80, 50, 30)), sole((60, 40, 24)), 11.2, None),
                   skirt_spec=(stripes(R, (40, 40, 50), 5), flat(Y), 5.0, 6.6, 0.34),
                   extras=[mirror_pair("lear", "head", (-2.5, -9.6, 0.0), (0, 0, -0.12), [Box(-0.8, -3.6, -0.6, 1.6, 3.6, 1.2, _ear(CR, (220, 170, 160)))]),
                           [Part("chullo", "head", (0, -8.0, 0), (0, 0, 0), [Box(-4.4, -2.6, -4.4, 8.8, 2.8, 8.8, chullo),
                                                                            Box(-1.0, -4.0, -1.0, 2.0, 1.6, 2.0, flat(Y))])],
                           mirror_pair("chullo_flap", "head", (-4.4, -6.2, 0.4), (0, 0, 0.08), [Box(-0.4, 0, -1.4, 0.6, 4.0, 2.8, chullo),
                                                                                                Box(-0.4, 4.0, -0.5, 0.6, 2.4, 1.0, flat(Y))]),
                           [Part("poncho_front", "body", (0, 0.2, -1.9), (-0.12, 0, 0), [Box(-4.0, 0, -0.4, 8.0, 6.4, 0.5, poncho)]),
                            Part("poncho_back", "body", (0, 0.2, 1.9), (0.12, 0, 0), [Box(-4.0, 0, -0.1, 8.0, 6.4, 0.5, poncho)])],
                           chain("ltail", "body", (0, 8.8, 2.0), (0.6, 0, 0), [(2.0, 1.8, 1.8, 0, 0), (1.4, 1.6, 1.6, -0.2, 0)], flat(CR))])
    return P, info


def camello():                     # Duna — camella del desierto, se puede montar
    SA, SAD, SAL = (214, 176, 120), (170, 130, 80), (236, 210, 164)
    TE, GO = (40, 150, 150), (230, 190, 70)
    coins = lambda f, x, y, w, h: GO if (f == "front" and y > h - 3 and x % 3 == 0) else TE
    P, info = girl(hair3((90, 60, 40)), "long", ((90, 50, 10), (190, 120, 40), (250, 210, 140)), (50, 30, 20),
                   top=coins, waist=cloth(SAL), hips=cloth(TE), sleeve=frill(SAL, SA), upper=SK, fore=SK, hand=SK,
                   thigh=SK, calf=cloth(SAL), shoes=(boot(SAD, SA, (120, 90, 50), accent=GO), sole((100, 70, 40)), 11.6, None),
                   skirt_spec=(cloth(TE, seam=dk(TE)), flat(GO), 5.6, 6.6, 0.34),
                   extras=[[Part("veil", "head", (0, -8.2, 0), (0, 0, 0), [Box(-4.5, -0.6, -4.5, 9.0, 1.0, 9.0, flat(TE)),
                                                                          Box(-4.4, 0.2, 3.6, 8.8, 7.0, 0.6, flat(lt(TE)))])],
                           [Part("circlet", "head", (0, -7.2, -4.4), (0, 0, 0), [Box(-1.0, -0.6, -0.3, 2.0, 1.2, 0.6, gem((220, 60, 70), (255, 200, 200)))])],
                           [Part("hump_cushion", "body", (0, 1.0, 2.0), (0, 0, 0), [Box(-3.0, 0, 0, 6.0, 4.0, 2.4, lambda f, x, y, w, h: GO if y % 8 == 0 else (180, 60, 60))])],
                           mirror_pair("bangle", "right_arm", (0, 7.0, 0), (0, 0, 0), [Box(-1.6, 0, -1.2, 2.4, 1.0, 2.4, metal(GO, (255, 236, 150)))])])
    return P, info


def sniffer():                     # Olfa — sniffer antigua, olfatea semillas y tesoros
    RD, RDD, MOSS, MOSSL = (180, 70, 50), (120, 44, 32), (90, 140, 60), (130, 180, 80)
    KH = (190, 170, 120)
    mossy = lambda f, x, y, w, h: MOSSL if (x * 5 + y * 3) % 11 == 0 else MOSS
    P, info = girl(hair3((112, 150, 72)), "wild", ((110, 40, 20), (200, 90, 50), (250, 190, 140)), (60, 30, 20),
                   top=cloth(KH), waist=cloth((110, 80, 50)), hips=cloth(RD), sleeve=mossy, upper=cloth(KH), fore=SK, hand=SK,
                   thigh=SK, calf=stock(RDD, (90, 34, 24)), shoes=(boot((90, 64, 40), (120, 90, 60), (60, 40, 24)), sole((50, 34, 20)), 10.6, None),
                   skirt_spec=(pleats(RD, lt(RD), RDD), flat(MOSS), 4.6, 6.6, 0.34),
                   extras=[mirror_pair("moss_pauld", "right_arm", (0, -1.2, 0), (0, 0, 0.15), [Box(-1.8, -1.0, -1.4, 2.6, 1.4, 2.8, mossy)]),
                           [Part("torchflower", "head", (3.4, -8.2, -2.4), (0, 0, 0.3), [Box(-1.0, -1.0, -0.5, 2.0, 2.0, 1.0, flat((240, 120, 40))),
                                                                                        Box(-0.4, -0.4, -0.7, 0.8, 0.8, 0.3, flat((250, 220, 90)))])],
                           [Part("brush", "body", (3.9, 6.6, 0.4), (0, 0, -0.1), [Box(-0.3, 0, -0.3, 0.6, 2.4, 0.6, flat((140, 100, 60))),
                                                                                Box(-0.6, 2.4, -0.6, 1.2, 1.2, 1.2, flat((230, 220, 200)))])],
                           [Part("seed_bag", "body", (-3.9, 7.0, 0.4), (0, 0, 0.1), [Box(-1.2, 0, -1.2, 1.4, 2.0, 2.4, flat((150, 120, 80)))])]])
    info["fang"] = False
    return P, info


def calamar():                     # Lucía — calamar brillante, ilumina la oscuridad
    TL, TLD, CY, GL = (30, 110, 120), (16, 64, 74), (80, 230, 220), (190, 255, 240)
    dots = lambda f, x, y, w, h: GL if (x * 7 + y * 5) % 17 == 0 else (CY if (x + y) % 9 == 0 else TL)
    P, info = girl(hair3((50, 170, 170)), "long", ((10, 90, 100), (60, 220, 210), (210, 255, 250)), (10, 60, 70),
                   top=dots, waist=cloth(TLD), hips=dots, sleeve=frill(CY, TL), upper=SK, fore=SK, hand=SK,
                   thigh=SK, calf=stock(TL, TLD, CY), shoes=(boot(TLD, TL, (10, 40, 46), accent=CY), sole((10, 40, 46)), 11.6, None),
                   skirt_spec=(frill(TL, TLD), flat(CY), 5.4, 6.6, 0.5), skin_c=((236, 246, 248), (206, 224, 230)),
                   extras=[[Part("tent%d" % i, "body", (x, 9.4, 1.4), (0.35, 0, z), [Box(-0.5, 0, -0.5, 1.0, 4.4, 1.0, dots)])
                            for i, (x, z) in enumerate(((-2.4, 0.3), (-0.8, 0.1), (0.8, -0.1), (2.4, -0.3)))],
                           mirror_pair("glowclip", "head", (-3.6, -6.8, -3.4), (0, 0, -0.3), [Box(-0.6, -0.6, -0.3, 1.2, 1.2, 0.6, gem(CY, GL))])]
                   )
    return P, info


def evocadora():                   # Evoka — evocadora, colmillos mágicos y tótems
    BK, BKL, GO, GN = (40, 40, 46), (70, 70, 78), (220, 180, 60), (40, 150, 70)

    def robe(f, x, y, w, h):
        if f == "front" and abs(x - w / 2) < 1.5:
            return GO
        if f == "front" and abs(x - w / 2) < w * 0.22 and y < 6:
            return (170, 30, 40)
        return BK
    P, info = girl(hair3((50, 46, 52)), "low", ((10, 60, 20), (40, 170, 70), (180, 250, 190)), (20, 20, 24),
                   top=robe, waist=cloth(GO), hips=robe, sleeve=frill(BK, BKL), upper=cloth(BK), fore=cloth(BKL), hand=SK,
                   thigh=stock(BK, (28, 28, 32)), calf=stock(BK, (28, 28, 32)), shoes=(boot(BK, BKL, (20, 20, 24), accent=GO), sole((16, 16, 18)), 10.6, None),
                   skirt_spec=(robe, flat(GO), 6.8, 6.6, 0.24), skin_c=((214, 216, 214), (186, 188, 188)),
                   extras=[[Part("hood", "head", (0, -8.0, 0), (0, 0, 0), [Box(-4.5, -1.0, -4.5, 9.0, 1.4, 9.0, flat(BK)),
                                                                          Box(-4.5, 0.4, 3.0, 9.0, 7.0, 1.6, flat(BK))])],
                           [Part("totem", "body", (0, 1.0, -2.0), (0, 0, 0), [Box(-0.8, 0, -0.4, 1.6, 2.0, 0.6, lambda f, x, y, w, h: GN if y > 4 else GO)])],
                           mirror_pair("fang_cuff", "right_arm", (0, 6.6, 0), (0, 0, 0), [Box(-1.6, 0, -1.2, 2.4, 0.8, 2.4, flat(GO))])])
    return P, info


def murcielago():                  # Noctula — murciélago, ve en la oscuridad con su eco
    BR, BRD, RD = (70, 52, 44), (40, 30, 26), (180, 40, 60)
    wing = lambda f, x, y, w, h: BRD if (x % 6 == 0) else (60, 44, 40)
    P, info = girl(hair3((60, 44, 40)), "bob", ((120, 10, 30), (220, 40, 60), (255, 170, 180)), (30, 20, 20),
                   top=cloth(BRD), waist=cloth(RD), hips=pleats(BRD, BR, (28, 20, 18)), sleeve=frill(BRD, BR), upper=SK, fore=SK, hand=SK,
                   thigh=SK, calf=stripes(BRD, RD, 4), shoes=(boot(BRD, BR, (24, 18, 16)), sole((20, 16, 14)), 11.4, None),
                   skirt_spec=(pleats(BRD, BR, (28, 20, 18)), frill(RD, dk(RD)), 4.0, 6.6, 0.42),
                   extras=[mirror_pair("bear", "head", (-2.6, -8.6, 0), (0, 0, -0.4), [Box(-1.4, -3.4, -0.5, 2.8, 3.4, 1.0, _ear(BR, (200, 130, 130)))]),
                           mirror_pair("bwing", "body", (-0.8, 1.0, 2.0), (0.1, 0.7, 0.35), [Box(-6.4, -1.0, 0, 6.4, 5.0, 0.3, wing)]),
                           mirror_pair("bwing2", "body", (-0.8, 4.4, 2.0), (0.1, 0.6, -0.25), [Box(-4.6, -0.6, 0, 4.6, 3.0, 0.3, wing)]),
                           [Part("choker", "body", (0, -0.4, 0), (0, 0, 0), [Box(-1.3, -0.6, -1.15, 2.6, 0.6, 2.3, flat(RD))])]])
    info["fang"] = True
    return P, info


GIRLS = {"burro": burro, "cabra": cabra, "llama": llama, "camello": camello, "sniffer": sniffer,
         "calamar": calamar, "evocadora": evocadora, "murcielago": murcielago}
LOOK = {"burro": ("athletic", "gentle"), "cabra": ("petite", "big"), "llama": ("curvy", "gentle"), "camello": ("athletic", "sharp"),
        "sniffer": ("curvy", "sleepy"), "calamar": ("slim", "big"), "evocadora": ("slim", "sharp"), "murcielago": ("petite", "cat")}
