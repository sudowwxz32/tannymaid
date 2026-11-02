"""Kaori, la acompañante mecánica e inventora (diseño original del mod).

Cabello largo vino-ciruela en coleta alta con flequillo de lado, ojos verde esmeralda, gafas de mecánica
sobre la cabeza, chaqueta verde oliva abierta con mangas remangadas, top blanco, cinturón con estuches de
cápsulas, shorts cargo grises con bolsillos, guantes sin dedos y botas altas cafés.
Reutiliza los pintores de gen_tanny.py y la malla de mechones de hair_mesh.py.

Uso: python3 tools/gen_kaori.py [preview DIR]
"""
import math, os, random, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_tanny as G
import hair_mesh as H
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/blockdragonsuperz/textures/entity/kaori")
JAVA_DIR = os.path.join(ROOT, "src/main/java/com/bdsz/client/kaori")
HAIR_COLOR = (128, 42, 74)

OLIVE, OLIVE_HI, OLIVE_DK = (92, 108, 58), (118, 136, 76), (64, 76, 40)
GREY, GREY_HI, GREY_DK = (92, 96, 104), (118, 122, 132), (66, 68, 76)
BROWN, BROWN_HI, BROWN_DK = (112, 70, 40), (146, 96, 58), (78, 46, 26)
LEATHER = (38, 30, 30)
BRASS = (214, 170, 72)


def palette():
    G.SKIN, G.SKIN_SH, G.SKIN_HI, G.SKIN_DK = (242, 208, 184), (226, 186, 162), (252, 224, 204), (198, 152, 128)
    G.HAIR, G.HAIR_HI, G.HAIR_DK = HAIR_COLOR, (160, 64, 98), (90, 26, 50)
    G.EYE_DK, G.EYE_MID, G.EYE_HI, G.EYE_RING = (16, 90, 58), (40, 170, 104), (150, 236, 180), (12, 70, 44)
    G.PUPIL_W = 1.6
    G.EYE_STYLE = "big"
    G.BROW_RGB = (84, 24, 44)
    G.BLUSH_ON = False
    G.body_paint, G.arm_paint, G.leg_paint = body_paint, arm_paint, leg_paint


# ------------------------------------------------------------------ base de la skin (bajo la ropa 3D)
def body_paint(f, x, y, w, h):
    if f == "front" and y < 4 and 0.36 * w <= x <= 0.64 * w:
        return G.SKIN
    if y >= 0.62 * h:
        return G.mix(GREY, GREY_HI, (x % 10) / 20.0)
    return G.mix(G.WHITE, G.WHITE_SH, (x % 12) / 24.0)


def arm_paint(f, x, y, w, h):
    t = y / h
    if f == "top" or t < 0.55:
        return OLIVE
    if t > 0.75:
        return LEATHER
    return G.mix(G.SKIN_HI, G.SKIN, (t - 0.55) * 4)


def leg_paint(f, x, y, w, h):
    t = y / h
    if f == "top" or t < 0.32:
        return GREY
    if f == "bottom" or t > 0.52:
        return BROWN
    return G.mix(G.SKIN_HI, G.SKIN, (t - 0.32) * 4)


# ------------------------------------------------------------------ pintores de la ropa 3D
def top_paint(f, x, y, w, h):
    """Top blanco de cuello alto con una costura y una pequeña insignia de engranaje (diseño propio)."""
    if y < 3 and f != "bottom":
        return G.WHITE_SH
    if f == "front":
        cx, cy = w * 0.5, h * 0.42
        r = math.hypot(x - cx, y - cy)
        if r < w * 0.09 and (r > w * 0.04 or ((x // 2 + y // 2) % 2 == 0 and r < w * 0.03)):
            return (232, 120, 60)
    return G.mix(G.WHITE, G.WHITE_SH, (x % 12) / 24.0)


def bust_top(f, x, y, w, h):
    return G.mix(G.WHITE, G.WHITE_SH, (x % 12) / 24.0 + (0.2 if f in ("top", "bottom") else 0))


def jacket_paint(f, x, y, w, h):
    """Chaqueta abierta: el centro del frente es transparente (se ve el top)."""
    if f == "front" and 0.3 * w <= x <= 0.7 * w:
        return None
    if f == "bottom":
        return None
    if f == "front" and (abs(x - 0.3 * w) < 2 or abs(x - 0.7 * w) < 2):
        return OLIVE_DK                                  # borde de la solapa
    if f == "front" and 0.08 * w < x < 0.24 * w and 0.48 * h < y < 0.68 * h:
        return OLIVE_DK if (y - int(0.48 * h)) < 2 else OLIVE_HI   # bolsillo de pecho
    if f == "back" and 0.3 * h < y < 0.55 * h:
        cx, cy = w / 2, 0.42 * h
        if math.hypot(x - cx, (y - cy) * 1.2) < w * 0.16 and math.hypot(x - cx, (y - cy) * 1.2) > w * 0.1:
            return (232, 120, 60)                         # engranaje naranja en la espalda
    if y >= h - 3 and f != "top":
        return OLIVE_DK
    return G.mix(OLIVE, OLIVE_HI, abs(((x + y // 3) % 14) / 14.0 - 0.5))


def collar_paint(f, x, y, w, h):
    return OLIVE_DK if f in ("bottom", "back") else G.mix(OLIVE_HI, OLIVE, y / max(1, h - 1))


def sleeve_paint(f, x, y, w, h):
    if f == "bottom":
        return None
    if f == "top":
        return OLIVE_HI
    return G.mix(OLIVE, OLIVE_HI, abs((x % 10) / 10.0 - 0.5)) if y % 9 else OLIVE_DK


def cuff_paint(f, x, y, w, h):
    if f in ("top", "bottom"):
        return None
    return OLIVE_HI if y < h / 2 else OLIVE_DK


def glove_paint(f, x, y, w, h):
    if f in ("top", "bottom"):
        return None
    if y < 2:
        return (70, 60, 60)
    return LEATHER if (x + y) % 5 else (58, 48, 48)


def belt_paint(f, x, y, w, h):
    if f == "front" and abs(x - w / 2) < 3:
        return BRASS if 1 <= y < h - 1 else BROWN_DK       # hebilla
    return BROWN_DK if y in (0, h - 1) else BROWN


def pouch_paint(f, x, y, w, h):
    """Estuche de cápsulas: cuero café con tapa y un broche dorado."""
    if f == "front" and y < h * 0.4:
        return BROWN_DK if not (abs(x - w / 2) < 2 and y > h * 0.25) else BRASS
    if f == "top":
        return BROWN_DK
    return G.mix(BROWN, BROWN_HI, (x % 8) / 16.0)


def capsule_paint(f, x, y, w, h):
    """Cápsula blanca con una banda naranja (las que Kaori lleva en el cinturón)."""
    if abs(y - h / 2) < 1.5:
        return (232, 120, 60)
    return G.WHITE if y < h / 2 else G.WHITE_SH


def shorts_paint(f, x, y, w, h):
    if f == "top":
        return GREY_DK
    if y >= h - 2 and f not in ("top", "bottom"):
        return GREY_DK
    t = (x % 10) / 10.0
    return G.mix(GREY, GREY_HI, abs(t - 0.5))


def cargo_paint(f, x, y, w, h):
    if f in ("top",) or y < 2:
        return GREY_DK
    return G.mix(GREY_HI, GREY, y / max(1, h - 1))


def boot_paint(f, x, y, w, h):
    if f == "bottom" or (f != "top" and y >= h - 2):
        return (40, 30, 26)                               # suela
    if f == "top":
        return None
    if y < 3:
        return BROWN_DK                                   # borde de la caña
    if f == "front" and (y // 3) % 2 == 0 and abs(x - w / 2) < 2:
        return (200, 190, 170)                            # cordones
    return G.mix(BROWN, BROWN_HI, abs((x % 9) / 9.0 - 0.5))


def toe_paint(f, x, y, w, h):
    if f == "bottom" or (f != "top" and y >= h - 1):
        return (40, 30, 26)
    return G.mix(BROWN_HI, BROWN, y / max(1, h - 1))


def strap_paint(f, x, y, w, h):
    return (46, 40, 44) if (x + y) % 4 else (70, 62, 66)


def lens_paint(f, x, y, w, h):
    """Gafas de mecánica: aro de latón y cristal ámbar con brillo."""
    if f == "front":
        cx, cy = w / 2, h / 2
        r = math.hypot((x - cx) / w, (y - cy) / h)
        if r > 0.38:
            return BRASS
        if (x - cx) < -w * 0.08 and (y - cy) < -h * 0.08:
            return (255, 236, 170)
        return G.mix((236, 150, 50), (150, 70, 20), r * 2.4)
    return G.mix(BRASS, (150, 110, 40), y / max(1, h - 1))


def scrunchie_paint(f, x, y, w, h):
    return (232, 120, 60) if (x + y) % 3 else (250, 160, 96)


# ------------------------------------------------------------------ piezas 3D
def head_parts():
    P = []
    # gafas de mecánica sobre la cabeza (correa y dos lentes)
    P.append(Part("goggles", "head", (0, -9.15, -2.2), (-0.3, 0, 0), []))
    P.append(Part("lens_r", "goggles", (-1.75, 0, -0.6), (0, 0.12, 0), [Box(-1.35, -1.05, -0.75, 2.7, 2.1, 1.5, lens_paint)]))
    P.append(Part("goggle_bridge", "goggles", (0, 0, -0.5), (0, 0, 0), [Box(-0.5, -0.3, -0.3, 1.0, 0.6, 0.6, strap_paint)]))
    P.append(Part("goggle_strap", "goggles", (0, 0, 0.4), (0, 0, 0), [Box(-4.2, -0.35, -0.3, 8.4, 0.7, 0.6, strap_paint)]))
    # coletero de la coleta alta
    P.append(Part("scrunchie", "head", (0, -7.5, 4.15), (0.75, 0, 0), [Box(-1.5, -1.5, -0.9, 3.0, 3.0, 1.8, scrunchie_paint)]))
    # pestañas 3D (iguales a las de ojos grandes)
    P.append(Part("lid_r", "head", (-1.25, -4.62, -4.02), (0, 0, 0.19), [Box(-0.68, -0.21, -0.26, 1.36, 0.42, 0.34, G.lash3d)]))
    P.append(Part("lidb_r", "head", (-2.55, -4.65, -4.02), (0, 0, -0.15), [Box(-0.68, -0.21, -0.26, 1.36, 0.42, 0.34, G.lash3d)]))
    P.append(Part("lash_r", "head", (-3.62, -4.1, -4.04), (0, 0.2, -0.755), [Box(-0.6, -0.18, -0.24, 1.2, 0.36, 0.32, G.lash3d)]))
    m = G.mirror([p for p in P if p.name.endswith("_r")])
    P += m
    return P, [("head", (0, 0, 0))]


def outfit_parts():
    P = []
    P.append(Part("top", "body", (0, 0, 0), (0, 0, 0), [Box(-4.25, -0.3, -2.25, 8.5, 7.6, 4.5, top_paint)]))
    P.append(Part("bust", "body", (0, 2.9, -2.2), (-0.28, 0, 0), [Box(-3.3, 0, -0.85, 6.6, 2.9, 0.95, bust_top)]))
    P.append(Part("jacket", "body", (0, 0, 0), (0, 0, 0), [Box(-4.65, -0.45, -2.65, 9.3, 8.9, 5.3, jacket_paint)]))
    P.append(Part("collar_back", "body", (0, -0.35, 2.35), (0.35, 0, 0), [Box(-3.4, -1.6, -0.35, 6.8, 1.6, 0.7, collar_paint)]))
    P.append(Part("collar_r", "body", (-2.3, -0.3, -2.55), (-0.25, 0.3, -0.25), [Box(-0.9, 0, -0.35, 1.8, 2.6, 0.7, collar_paint)]))
    P.append(Part("belt", "body", (0, 0, 0), (0, 0, 0), [Box(-4.55, 7.15, -2.6, 9.1, 1.25, 5.2, belt_paint)]))
    P.append(Part("pouch_r", "body", (-3.55, 7.7, -2.55), (0, 0.2, 0), [Box(-1.0, 0, -1.1, 2.0, 2.3, 1.3, pouch_paint)]))
    P.append(Part("capsule_r", "body", (-4.6, 7.9, 0.6), (0, 0, 0.12), [Box(-0.45, -0.1, -0.6, 0.9, 1.9, 1.2, capsule_paint)]))
    P.append(Part("capsule_case", "body", (0, 7.6, 2.55), (0, 0, 0), [Box(-1.6, 0, 0, 3.2, 2.2, 1.2, pouch_paint)]))
    P.append(Part("shorts", "body", (0, 0, 0), (0, 0, 0), [Box(-4.35, 7.7, -2.35, 8.7, 4.6, 4.7, shorts_paint)]))
    P.append(Part("sleeve_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.4, -2.45, -2.4, 3.8, 6.6, 4.8, sleeve_paint)]))
    P.append(Part("cuff_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.6, 3.4, -2.6, 4.2, 1.5, 5.2, cuff_paint)]))
    P.append(Part("glove_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.22, 7.3, -2.22, 3.44, 2.9, 4.44, glove_paint)]))
    P.append(Part("short_leg_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.3, -0.2, -2.3, 4.6, 3.8, 4.6, shorts_paint)]))
    P.append(Part("cargo_r", "right_leg", (-2.25, 0.9, 0), (0, 0, 0.05), [Box(-0.8, 0, -1.3, 0.9, 2.4, 2.6, cargo_paint)]))
    P.append(Part("boot_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.3, 6.2, -2.3, 4.6, 5.95, 4.6, boot_paint)]))
    P.append(Part("toe_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.3, 10.1, -3.05, 4.6, 2.05, 0.8, toe_paint)]))
    m = G.mirror([p for p in P if p.name.endswith("_r")])
    for p in m:
        p.parent = p.parent.replace("right", "left")
    P += m
    anchors = [("body", (0, 0, 0)), ("right_arm", (-5, 2.5, 0)), ("left_arm", (5, 2.5, 0)),
               ("right_leg", (-1.9, 12, 0)), ("left_leg", (1.9, 12, 0))]
    return P, anchors


Part, Box = G.Part, G.Box


# ------------------------------------------------------------------ cabello
def kaori_locks():
    """Coleta alta: todo el cabello se recoge hacia la coronilla de atrás y cae largo en una coleta;
    flequillo de lado, mechones que enmarcan la cara y patillas sobre las orejas."""
    rnd = random.Random(23)
    j = lambda a: rnd.uniform(-a, a)
    K = H
    L = []
    # cuero cabelludo peinado hacia atrás (cubre la cabeza, sin caída)
    for i in range(16):
        phi = i / 16 * 2 * math.pi + 0.1
        L.append([phi, 0.05, 1.45 if abs(math.cos(phi)) < 0.7 else 1.25, 0.0, 0, 0, 2.9, 0.9, 0.55, 1.0, K.KIND_BOB])
    # nuca recogida
    for i in range(9):
        phi = math.radians(130 + i * 12.5)
        L.append([phi, 0.9, math.pi / 2 + 0.25, 0.0, 0, 0, 2.6, 0.9, 0.45, 1.0, K.KIND_BOB])
    # la coleta: nace en la coronilla de atrás y cae larga con vuelo
    for i in range(13):
        phi = math.pi + math.radians(-30 + i * 5) + j(0.03)
        L.append([phi, 0.32 + j(0.03), 0.7 + j(0.04), 15.0 + j(1.2), 0.1 + j(0.05), 0.2 + j(0.1),
                  2.7, 0.95, 1.35 + j(0.15), 1.0, K.KIND_LONG])
    for i in range(7):
        phi = math.pi + math.radians(-18 + i * 6)
        L.append([phi, 0.35, 0.66, 12.5 + j(0.8), 0.2, 0.35, 2.5, 0.9, 1.75, 1.0, K.KIND_LONG])
    # patillas sobre las orejas
    for s_ in (-1, 1):
        for deg in (78, 92):
            L.append([s_ * math.radians(deg), 0.3, math.pi / 2, 2.4 + j(0.3), 0.15, -0.2, 2.2, 0.9, 0.35, 1.0, K.KIND_BOB])
    # mechones que enmarcan la cara
    for s_ in (-1, 1):
        L.append([s_ * math.radians(48), 0.3, math.pi / 2 - 0.06, 6.4, 0.05, 0.25, 1.8, 0.85, 0.55, 1.0, K.KIND_LONG])
        L.append([s_ * math.radians(57), 0.3, math.pi / 2 - 0.04, 5.0, 0.08, 0.3, 1.7, 0.85, 0.7, 1.0, K.KIND_LONG])
    # flequillo de lado (más largo a la derecha de ella)
    for deg, hang, w in ((-40, 2.6, 2.0), (-30, 2.9, 2.1), (-19, 2.7, 2.0), (-8, 2.4, 2.0), (3, 2.1, 1.9),
                         (13, 1.8, 1.9), (23, 1.6, 1.9), (33, 1.5, 1.9), (41, 1.4, 1.8)):
        L.append([math.radians(deg), 0.3, 1.0, hang, 0.12, -0.12, w, 0.8, 0.55, 1.0, K.KIND_SOFTBANG])
    for deg in (-26, 8):
        L.append([math.radians(deg), 0.25, 0.92, 1.8, 0.32, -0.3, 1.9, 0.75, 0.9, 1.0, K.KIND_SOFTBANG])
    return L


def fjava(v):
    return H.fjava(v)


def write_java(hparts, hanchors, oparts, oanchors, hw, hh, ow, oh):
    os.makedirs(JAVA_DIR, exist_ok=True)
    out = ["// GENERADO por tools/gen_kaori.py — no editar a mano.", "package com.bdsz.client.kaori;", "",
           "import net.minecraft.client.model.geom.ModelPart;", "import net.minecraft.client.model.geom.PartPose;",
           "import net.minecraft.client.model.geom.builders.*;", "", "import java.util.LinkedHashMap;", "import java.util.Map;", "",
           "public final class KaoriModels {", "    private KaoriModels() {}", ""]
    out += G.java_layer("createHead", hparts, hanchors, hw, hh) + [""]
    out += G.java_bake("bakeHead", hparts, hanchors) + [""]
    out += G.java_layer("createOutfit", oparts, oanchors, ow, oh) + [""]
    out += G.java_bake("bakeOutfit", oparts, oanchors) + ["}", ""]
    open(os.path.join(JAVA_DIR, "KaoriModels.java"), "w").write("\n".join(out))
    L = kaori_locks()
    out = ["// GENERADO por tools/gen_kaori.py — no editar a mano.", "package com.bdsz.client.kaori;", "",
           "/** Mechones de Kaori: phi, th0, th1, caída, vuelo, curva, ancho, grosor, separación, tono, tipo. */",
           "public final class KaoriHairLocks {", "    private KaoriHairLocks() {}", "",
           "    public static final float CY = %s, R = %s, P = %s;" % (fjava(H.CY), fjava(H.R), fjava(H.P)),
           "    public static final int SCALP_STEPS = %d, HANG_STEPS = %d, SIDES = %d;" % (H.SCALP_STEPS, H.HANG_STEPS, H.SIDES),
           "    public static final float VOX_STEP = %s, WAVE_AMP = %s, WAVE_FREQ = %s;" % (fjava(H.VOX_STEP), fjava(H.WAVE_AMP), fjava(H.WAVE_FREQ)),
           "    public static final int[] PALETTE = {" + ", ".join("0x%02X%02X%02X" % c for c in H.PALETTE) + "};",
           "", "    public static final float[][] L = {"]
    for l in L:
        out.append("            {" + ", ".join(fjava(v) for v in l) + "},")
    out += ["    };", "}", ""]
    open(os.path.join(JAVA_DIR, "KaoriHairLocks.java"), "w").write("\n".join(out))


def main():
    palette()
    os.makedirs(OUT, exist_ok=True)
    for expr, name in [("normal", "kaori"), ("blink", "kaori_blink"), ("happy", "kaori_happy"),
                       ("surprised", "kaori_surprised"), ("focused", "kaori_focused"), ("tired", "kaori_tired")]:
        G.skin(expr).save(os.path.join(OUT, name + ".png"))
    hp, ha = head_parts()
    hw, hh = G.pack(hp, 64)
    G.build_texture(hp, hw, hh).save(os.path.join(OUT, "head.png"))
    op, oa = outfit_parts()
    ow, oh = G.pack(op, 64)
    G.build_texture(op, ow, oh).save(os.path.join(OUT, "outfit.png"))
    G.strand_texture().save(os.path.join(OUT, "hair_strand.png"))
    write_java(hp, ha, op, oa, hw, hh, ow, oh)
    print("kaori ok: cabeza %dx%d · ropa %dx%d · mechones %d" % (hw, hh, ow, oh, len(kaori_locks())))


def preview(outdir):
    sk = Image.open(os.path.join(OUT, "kaori.png")).convert("RGBA")
    ot = Image.open(os.path.join(OUT, "outfit.png")).convert("RGBA")
    ct = Image.open(os.path.join(OUT, "head.png")).convert("RGBA")
    quads = []
    base = [((0, 0, 0), (0, 0), (-4, -8, -4, 8, 8, 8)), ((0, 0, 0), (16, 16), (-4, 0, -2, 8, 12, 4)),
            ((-5, 2.5, 0), (40, 16), (-2, -2, -2, 3, 12, 4)), ((5, 2.5, 0), (32, 48), (-1, -2, -2, 3, 12, 4)),
            ((-1.9, 12, 0), (0, 16), (-2, 0, -2, 4, 12, 4)), ((1.9, 12, 0), (16, 48), (-2, 0, -2, 4, 12, 4))]
    for piv, (u, v), bx in base:
        quads += G.quads_for(sk, *bx, u, v, [(piv, (0, 0, 0))], 1)
    op, oa = outfit_parts()
    G.pack(op, 64)
    tf = G.chains(op, oa)
    for p in op:
        for b in p.boxes:
            quads += G.quads_for(ot, b.x, b.y, b.z, b.w, b.h, b.d, b.u, b.v, tf[p.name], 2)
    hp, ha = head_parts()
    G.pack(hp, 64)
    tf = G.chains(hp, ha)
    for p in hp:
        for b in p.boxes:
            quads += G.quads_for(ct, b.x, b.y, b.z, b.w, b.h, b.d, b.u, b.v, tf[p.name], 1)
    Ld = H.norm((0.35, -0.8, -0.5))
    for q, c, n in H.mesh_quads(table=kaori_locks(), color=HAIR_COLOR):
        l = 0.55 + 0.5 * max(0.0, -(n[0] * Ld[0] + n[1] * Ld[1] + n[2] * Ld[2]))
        quads.append((q, tuple(int(min(255, v * l)) for v in c)))
    G.draw(quads, os.path.join(outdir, "kaori_preview.png"), 14, 150,
           [(math.pi, 0.05), (math.pi - 0.7, 0.12), (math.pi / 2, 0.05), (-0.6, 0.12)], (300, 520))
    head = [qc for qc in quads if min(p[1] for p in qc[0]) < 1.5]
    G.draw(head, os.path.join(outdir, "kaori_face.png"), 30, 300, [(math.pi, 0.0), (math.pi - 0.6, 0.15), (0.0, 0.1)], (380, 420))


if __name__ == "__main__":
    main()
    if len(sys.argv) > 2 and sys.argv[1] == "preview":
        preview(sys.argv[2])
