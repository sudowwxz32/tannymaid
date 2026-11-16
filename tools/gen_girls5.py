"""Totally Waifu? 5.0 — 8 waifus extra nuevas (se desbloquean al completar la colección, junto a las 4 sorpresas)."""
import math
import gen_blocky as B
from gen_blocky import Part, Box, flat, pleats, band, hair_p, frill, metal, gem, stock, boot, sole, bow, chain
from gen_mobgirls import girl, hair3, cloth, lt, dk
from gen_girls4 import mirror_pair, stripes, WHITE

SK = B.skin_p


def delfin():
    BL, BLL, BLD = (120, 152, 192), (196, 214, 236), (80, 108, 150)

    def suit(f, x, y, w, h):
        return BLL if f == "front" and abs(x - w / 2) < w * 0.25 else BL
    fin = flat(BLD)
    P, info = girl(hair3((104, 136, 176)), "wavy", ((20, 60, 120), (70, 140, 220), (200, 230, 255)), (30, 50, 80),
                   top=suit, waist=SK, hips=suit, sleeve=SK, upper=SK, fore=SK, hand=SK, thigh=SK, calf=SK,
                   shoes=(boot(WHITE, BLL, BL), sole(BL), 12.4, None),
                   skirt_spec=(frill(BLL, BL), flat(WHITE), 2.6, 7.2, 0.55),
                   extras=[[Part("dfin", "body", (0, 2.6, 1.9), (-0.55, 0, 0), [Box(-0.4, -3.4, 0, 0.8, 3.4, 2.2, fin)])],
                           mirror_pair("fluke", "body", (-0.4, 9.2, 1.9), (0.5, 0.3, 0.4), [Box(-2.8, 0, -0.3, 2.8, 1.4, 0.6, fin)]),
                           [Part("pearls", "body", (0, -0.4, -1.3), (0.2, 0, 0), [Box(-1.6, 0, -0.3, 3.2, 0.6, 0.5, flat((250, 246, 236))),
                                                                              Box(-0.5, 0.4, -0.4, 1.0, 1.0, 0.6, gem((240, 200, 230), (255, 250, 255)))])],
                           [Part("shell_clip", "head", (3.6, -6.6, -3.4), (0, 0, 0.3), [Box(-0.9, -0.9, -0.3, 1.8, 1.8, 0.6, flat((250, 190, 200)))])]])
    return P, info


def tortuga():
    G, GL, GD = (100, 160, 80), (150, 200, 110), (60, 110, 50)
    SH, SHD = (120, 100, 60), (80, 64, 36)
    hexes = lambda f, x, y, w, h: SHD if ((x + (y // 9) * 5) % 10 == 0 or y % 9 == 0) else (G if (x // 10 + y // 9) % 3 == 0 else SH)
    P, info = girl(hair3((96, 156, 76)), "buns", ((40, 80, 20), (110, 170, 60), (210, 250, 160)), (40, 60, 30),
                   top=cloth((214, 220, 140)), waist=cloth(GD), hips=cloth(G), sleeve=frill((214, 220, 140), (190, 196, 120)), upper=SK, fore=SK, hand=SK,
                   thigh=SK, calf=stock(GL, G), shoes=(boot(GD, G, dk(GD)), sole(dk(GD)), 11.4, None),
                   skirt_spec=(pleats(G, GL, GD), flat((214, 220, 140)), 4.2, 6.6, 0.34),
                   extras=[[Part("shell", "body", (0, 0.6, 1.9), (0.06, 0, 0), [Box(-3.8, -0.4, 0, 7.6, 8.2, 2.8, hexes),
                                                                               Box(-3.2, 0.2, 2.6, 6.4, 7.0, 0.8, hexes)])],
                           [Part("seagrass", "head", (-3.6, -7.4, -3.4), (0, 0, -0.3), [Box(-0.3, -2.2, -0.3, 0.6, 2.4, 0.6, flat((50, 140, 70)))])]])
    return P, info


def loro():
    R, Y, BLU, GR = (226, 44, 40), (250, 210, 60), (50, 110, 220), (60, 180, 90)
    feather = lambda f, x, y, w, h: BLU if (y // 6) % 3 == 0 else (GR if (y // 6) % 3 == 1 else BLU)
    P, info = girl(hair3((222, 50, 44)), "spiky", ((20, 20, 20), (60, 60, 60), (200, 200, 200)), (40, 20, 20),
                   top=cloth(R), waist=cloth(Y), hips=cloth(R), sleeve=feather, upper=feather, fore=SK, hand=SK,
                   thigh=SK, calf=stock(Y, (220, 180, 40)), shoes=(boot(BLU, lt(BLU), dk(BLU)), sole(dk(BLU)), 11.0, None),
                   skirt_spec=(pleats(BLU, lt(BLU), dk(BLU)), flat(Y), 4.0, 6.6, 0.42),
                   extras=[[Part("plume%d" % i, "head", (x, -8.8, 0.6), (-0.5, 0, r), [Box(-0.5, -3.0, -0.4, 1.0, 3.0, 0.8, flat(c))])
                            for i, (x, r, c) in enumerate(((-1.0, -0.3, Y), (0.0, 0.0, R), (1.0, 0.3, BLU)))],
                           mirror_pair("awingf", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.0, 0.0, -0.4, 0.6, 7.0, 3.0, feather)]),
                           chain("ptail", "body", (0, 8.6, 1.9), (0.5, 0, 0), [(3.0, 2.0, 0.4, 0, 0), (2.6, 1.6, 0.4, -0.1, 0)], flat(R))])
    return P, info


def oso():
    W, WS = (246, 248, 250), (214, 220, 228)
    SC = (70, 120, 200)
    fur = lambda f, x, y, w, h: WS if (x * 3 + y * 5) % 23 == 0 else W
    P, info = girl(hair3((238, 242, 246)), "short", ((20, 30, 50), (60, 80, 120), (200, 220, 250)), (40, 44, 60),
                   top=fur, waist=cloth(W), hips=fur, sleeve=fur, upper=fur, fore=fur, hand=flat((60, 60, 70)),
                   thigh=SK, calf=fur, shoes=(boot(W, WS, (190, 196, 206)), sole((60, 60, 70)), 9.6, flat(W)),
                   skirt_spec=(fur, flat(WS), 4.4, 6.6, 0.36),
                   extras=[mirror_pair("bear_ear", "head", (-3.4, -9.0, 0.4), (0, 0, -0.25), [Box(-1.4, -2.2, -0.7, 2.8, 2.6, 1.4, lambda f, x, y, w, h: (236, 200, 206) if f == "front" and 2 < x < w - 3 and y > 4 else W)]),
                           [Part("scarf", "body", (0, 0, 0), (0, 0, 0), [Box(-2.4, -1.0, -1.6, 4.8, 1.4, 3.2, cloth(SC))])],
                           chain("scarf_tail", "body", (1.4, 0.2, -1.6), (-0.1, 0, 0.1), [(2.6, 1.2, 0.4, 0, 0), (2.2, 1.2, 0.4, 0.1, 0)], cloth(SC)),
                           [Part("fish", "body", (3.8, 6.8, 0.2), (0, 0, -0.2), [Box(-0.4, 0, -0.9, 0.8, 1.2, 1.8, flat((200, 120, 90)))])]])
    return P, info


def strider():
    CR, CRD, PU, OR = (160, 44, 54), (110, 26, 36), (110, 70, 140), (250, 140, 40)
    wave = lambda f, x, y, w, h: PU if (y + (x // 4) % 3) % 10 == 0 else CR
    P, info = girl(hair3((170, 46, 56)), "long", ((60, 10, 20), (180, 60, 70), (255, 170, 170)), (60, 20, 26),
                   top=wave, waist=cloth(CRD), hips=wave, sleeve=cloth(CR), upper=SK, fore=cloth(CR), hand=SK,
                   thigh=stock(PU, dk(PU)), calf=stock(PU, dk(PU)), shoes=(boot(CRD, CR, (60, 14, 20), accent=OR), sole((40, 10, 14), OR), 9.0, None),
                   skirt_spec=(wave, flat(OR), 3.8, 6.6, 0.36),
                   extras=[[Part("shair%d" % i, "head", (x, -8.8, z), (r, 0, rz), [Box(-0.2, -3.2, -0.2, 0.4, 3.2, 0.4, flat((240, 220, 200)))])
                            for i, (x, z, r, rz) in enumerate(((-2.0, -1.0, -0.4, -0.4), (0.0, -1.6, -0.5, 0.0), (2.0, -1.0, -0.4, 0.4), (-1.0, 1.4, 0.4, -0.2), (1.2, 1.4, 0.4, 0.2)))],
                           [Part("warm_scarf", "body", (0, 0, 0), (0, 0, 0), [Box(-2.4, -1.0, -1.6, 4.8, 1.3, 3.2, stripes(PU, OR, 4))])]])
    return P, info


def shulker():
    PU, PUL, PUD, YL = (150, 100, 170), (190, 150, 210), (100, 60, 120), (236, 216, 150)
    shell = lambda f, x, y, w, h: PUD if (y == h - 2 or x % 12 == 0) else PU
    P, info = girl(hair3((206, 178, 226)), "short", ((120, 90, 20), (230, 200, 90), (255, 250, 200)), (80, 60, 90),
                   top=cloth(PU), waist=cloth(YL), hips=cloth(PU), sleeve=frill(PUL, PU), upper=SK, fore=SK, hand=SK,
                   thigh=SK, calf=stock(PUD, dk(PUD)), shoes=(boot(PU, PUL, PUD), sole(PUD), 11.4, None),
                   skirt_spec=(pleats(PU, PUL, PUD), flat(YL), 4.6, 6.6, 0.32),
                   extras=[[Part("shell_top", "head", (0, 0, 0), (0, 0, 0), [Box(-5.0, -10.0, -5.0, 10.0, 2.4, 10.0, shell),
                                                                            Box(-5.0, -8.0, 1.6, 10.0, 4.0, 3.4, shell)])],
                           [Part("shell_lid", "head", (0, -7.6, -5.0), (0, 0, 0), [Box(-4.6, 0, -0.3, 9.2, 0.6, 0.6, flat(YL))])],
                           [Part("bullet", "body", (3.9, 6.6, 0.4), (0, 0, 0), [Box(-0.6, 0, -0.6, 1.2, 1.2, 1.2, gem((240, 240, 200), (255, 255, 240)))])]])
    return P, info


def phantom():
    BL, BLD, TE = (44, 64, 120), (24, 34, 70), (90, 240, 200)
    memb = lambda f, x, y, w, h: TE if (y < 3 or x < 2) else ((60, 84, 150) if (x + y) % 11 else BLD)
    P, info = girl(hair3((40, 56, 104)), "hime", ((10, 80, 60), (60, 230, 180), (200, 255, 240)), (20, 30, 60),
                   top=cloth(BL), waist=cloth(BLD), hips=cloth(BL), sleeve=cloth(BL), upper=cloth(BL), fore=cloth(BLD), hand=SK,
                   thigh=stock(BLD, (14, 20, 44)), calf=stock(BLD, (14, 20, 44)), shoes=(boot(BLD, BL, (10, 14, 30), accent=TE), sole((10, 14, 30)), 8.6, None),
                   skirt_spec=(lambda f, x, y, w, h: None if (y > h - 6 and (x // 5) % 2 == 0) else BL, flat(TE), 5.4, 6.6, 0.3),
                   extras=[mirror_pair("pwing", "body", (-1.0, 1.4, 2.0), (0.1, 0.5, 0.25), [Box(-7.0, -1.0, 0, 7.0, 5.4, 0.3, memb)]),
                           mirror_pair("pwing_tip", "body", (-1.0, 1.4, 2.0), (0.1, 0.5, 0.65), [Box(-9.0, -0.6, 0.05, 2.4, 1.4, 0.25, flat(TE))])])
    return P, info


def guardian():
    TE, TEL, TED, OR = (88, 158, 146), (140, 200, 186), (50, 104, 96), (240, 140, 50)
    bricks = lambda f, x, y, w, h: TED if (y % 8 == 0 or (x + (y // 8) * 6) % 12 == 0) else TE
    spikes = []
    for i, (par, piv, rot) in enumerate((("head", (-3.4, -8.6, -1.0), (-0.3, 0, -0.5)), ("head", (3.4, -8.6, 1.0), (0.3, 0, 0.5)), ("head", (0, -9.0, 2.6), (0.6, 0, 0)),
                                          ("right_arm", (0, -1.6, 0), (0, 0, 0.4)), ("left_arm", (0, -1.6, 0), (0, 0, -0.4)))):
        spikes.append(Part("gspike%d" % i, par, piv, rot, [Box(-0.35, -2.4, -0.35, 0.7, 2.4, 0.7, lambda f, x, y, w, h: OR if y < 4 else (236, 226, 200))]))
    P, info = girl(hair3((96, 168, 156)), "side", ((120, 50, 0), (240, 140, 40), (255, 220, 150)), (30, 70, 64),
                   top=bricks, waist=cloth(TED), hips=bricks, sleeve=metal(TE, TEL), upper=SK, fore=metal(TE, TEL), hand=SK,
                   thigh=SK, calf=bricks, shoes=(boot(TED, TE, dk(TED)), sole(dk(TED)), 9.8, metal(TE, TEL)),
                   skirt_spec=(bricks, flat(OR), 4.6, 6.6, 0.3),
                   extras=[spikes, [Part("geye", "body", (0, 2.2, -2.0), (0, 0, 0), [Box(-1.0, -1.0, -0.4, 2.0, 2.0, 0.6,
                                                                                       lambda f, x, y, w, h: (40, 20, 10) if abs(x - w / 2) < 2 and abs(y - h / 2) < 3 else OR)])]])
    return P, info


GIRLS = {"delfin": delfin, "tortuga": tortuga, "loro": loro, "oso": oso, "strider": strider, "shulker": shulker, "phantom": phantom, "guardian": guardian}
LOOK = {"delfin": ("slim", "big"), "tortuga": ("petite", "gentle"), "loro": ("petite", "big"), "oso": ("curvy", "round"),
        "strider": ("slim", "gentle"), "shulker": ("std", "round"), "phantom": ("slim", "gentle"), "guardian": ("athletic", "big")}
