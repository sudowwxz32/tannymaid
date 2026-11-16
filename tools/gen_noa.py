"""Totally Waifu? — aspecto "Noa" en estilo de bloques (como la hoja de personaje de referencia).

Bob negro con flequillo recto y puntas abultadas a los lados, orejas de elfa con interior lavanda, ojos turquesa
con pestaña gruesa, piel pálida lavanda, chaqueta celeste corta con cuello esponjoso, lazo negro y bolsillos,
enterizo negro con emblema rojo, guantes sin dedos, medias oscuras transparentes y botas blancas de plataforma
con detalles rojos. Todo con cubos (estética Minecraft) y texturas limpias a 8 px por unidad.

Uso: python3 tools/gen_noa.py [preview DIR]
"""
import math, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_tanny as G
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/totallywaifu/textures/entity/noa")
JAVA = os.path.join(ROOT, "src/main/java/com/totallywaifu/client/NoaModels.java")
Part, Box, mix = G.Part, G.Box, G.mix
T = (0, 0, 0, 0)
ANCHORS = [("head", (0, 0, 0)), ("hat", (0, 0, 0)), ("body", (0, 0, 0)), ("right_arm", (-5, 2, 0)), ("left_arm", (5, 2, 0)),
           ("right_leg", (-1.9, 12, 0)), ("left_leg", (1.9, 12, 0))]

HAIR, HAIR_HI, HAIR_DK = (36, 38, 52), (62, 66, 86), (20, 20, 30)
SKIN, SKIN_SH, SKIN_DK = (242, 230, 244), (218, 200, 226), (190, 168, 204)
EAR_IN = (222, 168, 204)
JK, JK_HI, JK_DK, JK_LN = (168, 206, 224), (204, 230, 240), (126, 166, 188), (92, 128, 150)
BLK, BLK_HI, BLK_DK = (36, 36, 46), (60, 60, 76), (22, 22, 30)
RED, RED_DK = (210, 40, 48), (150, 24, 32)
GREY, GREY_HI = (128, 132, 146), (176, 180, 192)
TIGHT, TIGHT_DK, TIGHT_HI = (122, 104, 136), (88, 74, 102), (168, 148, 182)
BOOT, BOOT_SH, BOOT_DK, SOLE = (240, 242, 248), (206, 212, 224), (156, 162, 178), (92, 96, 110)
TEAL, TEAL_HI, TEAL_DK = (40, 176, 168), (140, 236, 220), (18, 96, 100)


# ------------------------------------------------------------------ pintores (colores limpios, borde suave)
def flat(c, edge=None, top=None, bottom=None):
    dk = edge or tuple(int(v * 0.8) for v in c)

    def fn(f, x, y, w, h):
        if f == "top":
            return top or tuple(min(255, int(v * 1.06)) for v in c)
        if f == "bottom":
            return bottom or dk
        if x == 0 or x == w - 1 or y == h - 1:
            return dk
        return c
    return fn


def hair(f, x, y, w, h):
    """Cabello negro azulado con mechones verticales marcados y brillo arriba."""
    if f == "top":
        return HAIR_HI if (x // 8 + y // 8) % 3 == 0 else HAIR
    if f == "bottom":
        return HAIR_DK
    stripe = (x // 6) % 4
    c = HAIR_HI if stripe == 0 and y < h * 0.35 else HAIR_DK if stripe == 2 else HAIR
    if y >= h - 2:
        c = HAIR_DK
    return c


def jacket(f, x, y, w, h):
    """Chaqueta celeste con costuras y sombra suave abajo."""
    if f == "top":
        return JK_HI
    if f == "bottom":
        return JK_DK
    if y >= h - 6:
        return JK_DK if y >= h - 2 else JK
    if x % 24 == 0 or y % 26 == 0:
        return JK_LN
    return mix(JK_HI, JK, y / max(1, h))


def sleeve(f, x, y, w, h):
    if f in ("top", "bottom"):
        return JK_HI if f == "top" else JK_DK
    if y >= h - 8:
        return JK_DK if (y // 3) % 2 == 0 else JK_LN          # puño con pliegues
    if y % 20 == 0:
        return JK_LN
    return mix(JK_HI, JK, y / max(1, h))


def fluffy(f, x, y, w, h):
    """Cuello esponjoso (blanco celeste) con textura de pelusa."""
    k = ((x * 7 + y * 13) % 9)
    return JK_HI if k < 3 else (232, 244, 250) if k < 6 else JK


def black(f, x, y, w, h):
    if f == "top":
        return BLK_HI
    if f == "bottom":
        return BLK_DK
    if x == 0 or x == w - 1:
        return BLK_DK
    return BLK if (x + y) % 17 else BLK_HI


def shorts(f, x, y, w, h):
    if f == "front" and abs(x - w * 0.3) < 1:
        return GREY                                            # cremallera
    return black(f, x, y, w, h)


def tights(f, x, y, w, h):
    """Medias oscuras transparentes: se ve la piel, más clara en rodillas y en el frente."""
    if f in ("top", "bottom"):
        return TIGHT
    t = y / max(1, h)
    c = TIGHT if f == "front" else TIGHT_DK
    if f == "front" and 0.55 < t < 0.95 and abs(x - w / 2) < w * 0.3:
        c = mix(c, TIGHT_HI, 0.5)
    if (x * 5 + y * 3) % 23 == 0:
        c = mix(c, TIGHT_DK, 0.6)
    if x == 0 or x == w - 1:
        c = mix(c, TIGHT_DK, 0.6)
    return c


def boot(f, x, y, w, h):
    if f == "top":
        return BOOT
    if f == "bottom":
        return BOOT_DK
    if y < 3:
        return RED if f in ("front", "left", "right") and (x // 6) % 3 == 0 else BOOT_SH
    if f in ("left", "right") and (y // 10) % 2 == 1 and 3 < x < w - 3:
        return BOOT_SH                                          # placas laterales
    if x == 0 or x == w - 1:
        return BOOT_DK
    return BOOT


def sole(f, x, y, w, h):
    if f == "top" and y < h * 0.3:
        return RED                                              # punta roja
    return SOLE if f != "top" else BOOT_SH


def glove(f, x, y, w, h):
    return BLK if (x + y) % 9 else BLK_HI


def hand(f, x, y, w, h):
    if y < h * 0.45:
        return BLK if f != "bottom" else BLK_DK                 # guante sin dedos
    return SKIN if f != "bottom" else SKIN_SH


def ear(f, x, y, w, h):
    if f == "front" and 2 < y < h - 2 and x > 3:
        return EAR_IN
    return SKIN if f not in ("bottom",) else SKIN_SH


def emblem(f, x, y, w, h):
    if f == "front" and abs(x - w / 2) < 2 and 3 < y < h - 3:
        return (250, 240, 240)
    return RED


# ------------------------------------------------------------------ geometría
def parts():
    P = []
    # ---- cabello: casco, nuca, costados, puntas abultadas, flequillo recto y mechones frontales
    P.append(Part("hair_top", "head", (0, 0, 0), (0, 0, 0), [Box(-4.6, -9.0, -4.7, 9.2, 1.6, 9.6, hair)]))
    P.append(Part("hair_back", "head", (0, 0, 0), (0, 0, 0), [Box(-4.7, -8.0, -0.2, 9.4, 6.4, 5.0, hair)]))
    P.append(Part("hair_side_r", "head", (0, 0, 0), (0, 0, 0), [Box(-4.95, -8.0, -4.0, 1.5, 4.6, 7.8, hair)]))
    P.append(Part("hair_puff_r", "head", (-4.7, -3.8, 0.3), (0, 0, 0.32), [Box(-2.2, -1.2, -3.8, 2.8, 4.6, 7.6, hair)]))
    P.append(Part("hair_puff2_r", "head", (-4.2, -1.2, 1.0), (0, 0, 0.18), [Box(-2.0, -0.8, -2.6, 2.4, 2.0, 5.4, hair)]))
    P.append(Part("hair_nape", "head", (0, -1.8, 2.8), (-0.25, 0, 0), [Box(-4.9, -0.6, -1.6, 9.8, 2.6, 3.6, hair)]))
    P.append(Part("hair_lock_r", "head", (0, 0, 0), (0, 0, 0), [Box(-4.75, -6.2, -4.55, 1.35, 5.4, 1.5, hair)]))
    bottoms = [-5.2, -4.9, -5.5, -5.0, -5.1, -5.6, -4.9, -5.3]
    for i, b in enumerate(bottoms):
        x0 = -4.45 + i * 1.1125
        P.append(Part("bang%d" % i, "head", (0, 0, 0), (0, 0, 0), [Box(x0, -8.6, -4.75, 1.12, -(-8.6 - b), 0.9, hair)]))
    # ---- orejas de elfa (salen hacia los lados bajo el cabello)
    P.append(Part("ear_r", "head", (-4.3, -2.6, 0.8), (0, 0.2, -0.12), [Box(-4.2, -1.0, -0.6, 4.2, 2.0, 1.2, ear),
                                                                       Box(-5.6, -0.8, -0.45, 1.5, 1.1, 0.9, ear)]))
    # ---- torso: enterizo negro con busto, cintura y short
    P.append(Part("top", "body", (0, 0, 0), (0, 0, 0), [Box(-3.6, 0.0, -1.85, 7.2, 6.2, 3.7, black)]))
    P.append(Part("bust", "body", (0, 1.7, -1.8), (-0.3, 0, 0), [Box(-3.2, 0.0, -1.05, 6.4, 2.7, 1.15, black)]))
    P.append(Part("waist", "body", (0, 0, 0), (0, 0, 0), [Box(-3.1, 6.0, -1.65, 6.2, 2.4, 3.3, black)]))
    P.append(Part("shorts", "body", (0, 0, 0), (0, 0, 0), [Box(-3.95, 8.3, -2.05, 7.9, 3.9, 4.1, shorts)]))
    P.append(Part("belt", "body", (0, 0, 0), (0, 0, 0), [Box(-4.05, 8.0, -2.15, 8.1, 0.8, 4.3,
              lambda f, x, y, w, h: GREY_HI if f == "front" and abs(x - w * 0.42) < 3 else GREY)]))
    P.append(Part("emblem", "body", (0, 3.4, -2.75), (0, 0, 0), [Box(-0.6, -0.9, -0.2, 1.2, 1.9, 0.35, emblem)]))
    P.append(Part("strap_belt", "body", (1.4, 7.0, -2.2), (0, 0, 0.45), [Box(-0.3, 0, -0.15, 0.6, 3.6, 0.3, flat(BLK_HI))]))
    P.append(Part("tab_r", "body", (-2.6, 11.6, -2.2), (0, 0, 0), [Box(-0.22, 0, -0.15, 0.44, 1.5, 0.3, flat(RED))]))
    P.append(Part("tab2_r", "body", (-1.6, 11.6, -2.2), (0, 0, 0), [Box(-0.18, 0, -0.12, 0.36, 1.1, 0.24, flat(GREY_HI))]))
    # ---- chaqueta corta abierta, cuello esponjoso y lazo
    P.append(Part("jk_back", "body", (0, 0, 0), (0, 0, 0), [Box(-4.35, -0.4, 1.55, 8.7, 6.0, 1.05, jacket)]))
    P.append(Part("jk_side_r", "body", (0, 0, 0), (0, 0, 0), [Box(-4.45, -0.4, -2.45, 1.05, 6.0, 4.9, jacket)]))
    P.append(Part("jk_front_r", "body", (0, 0, 0), (0, 0, 0), [Box(-4.45, -0.4, -2.65, 2.85, 6.0, 0.6, jacket)]))
    P.append(Part("collar", "body", (0, 0, 0), (0, 0, 0), [Box(-3.5, -1.9, -2.85, 7.0, 2.3, 5.5, fluffy)]))
    P.append(Part("bow_knot", "body", (0, -0.4, -2.95), (0, 0, 0), [Box(-0.55, -0.45, -0.3, 1.1, 0.9, 0.55, flat(BLK))]))
    P.append(Part("bow_wing_r", "bow_knot", (-0.4, 0, 0), (0, 0, 0.25), [Box(-1.6, -0.55, -0.2, 1.6, 1.1, 0.4, flat(BLK))]))
    P.append(Part("bow_wing_l", "bow_knot", (0.4, 0, 0), (0, 0, -0.25), [Box(0, -0.55, -0.2, 1.6, 1.1, 0.4, flat(BLK))]))
    P.append(Part("bow_tail_r", "bow_knot", (-0.4, 0.3, 0), (0, 0, 0.12), [Box(-0.3, 0, -0.15, 0.55, 4.2, 0.3, flat(BLK_HI))]))
    P.append(Part("bow_tail_l", "bow_knot", (0.4, 0.3, 0), (0, 0, -0.12), [Box(-0.25, 0, -0.15, 0.55, 3.6, 0.3, flat(BLK_HI))]))
    # ---- mangas grandes con bolsillo y puño, guante sin dedos
    P.append(Part("sleeve_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-3.25, -2.6, -2.6, 5.0, 10.2, 5.2, sleeve)]))
    P.append(Part("shoulder_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-3.1, -3.05, -2.45, 4.7, 0.9, 4.9, jacket)]))
    P.append(Part("pocket_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-3.6, 0.6, -1.3, 0.45, 2.8, 2.6, flat(JK_HI, JK_LN)),
                                                                 Box(-3.75, 0.3, -1.4, 0.3, 0.9, 2.8, flat(JK_DK, JK_LN))]))
    P.append(Part("patch_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.4, 1.2, -2.85, 1.8, 1.6, 0.35, flat(JK_HI, JK_LN))]))
    P.append(Part("glove_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.0, 7.5, -1.9, 2.9, 1.6, 3.8, glove)]))
    P.append(Part("hand_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-1.85, 9.0, -1.65, 2.6, 2.6, 3.3, hand)]))
    # ---- piernas: medias, botas blancas de plataforma con detalles rojos
    P.append(Part("thigh_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.3, -0.2, -2.25, 4.5, 5.8, 4.5, tights)]))
    P.append(Part("calf_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.0, 5.4, -2.0, 4.0, 2.4, 4.0, tights)]))
    P.append(Part("boot_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.45, 7.4, -2.55, 4.9, 3.4, 5.0, boot)]))
    P.append(Part("cuff_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.85, 6.4, -2.95, 5.7, 1.8, 5.8, boot)]))
    P.append(Part("flap_r", "right_leg", (0, 6.0, -3.0), (-0.15, 0, 0), [Box(-1.5, 0, -0.5, 3.0, 3.0, 0.55, flat(BOOT, BOOT_SH))]))
    P.append(Part("buckle_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.6, 8.8, -2.75, 5.2, 0.6, 5.4, flat(BOOT_DK))]))
    P.append(Part("foot_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.45, 10.4, -3.5, 4.9, 0.8, 5.9, boot)]))
    P.append(Part("sole_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.6, 11.2, -3.75, 5.2, 0.8, 6.3, sole)]))
    P.append(Part("thigh_strap", "left_leg", (0, 0, 0), (0, 0, 0), [Box(-2.45, 2.0, -2.4, 4.9, 0.6, 4.8, flat(BLK))]))
    P.append(Part("strap_tab", "left_leg", (2.45, 2.3, -1.0), (0, 0, 0), [Box(0, -0.3, -0.3, 0.35, 1.6, 0.6, flat(RED))]))
    m = G.mirror([p for p in P if p.name.endswith("_r") and p.parent in ("head", "body", "right_arm", "right_leg")])
    for p in m:
        p.parent = p.parent.replace("right", "left")
    return P + m


# ------------------------------------------------------------------ cara (16x16 → 64 px, píxel nítido)
EYE = {
    "normal": ["LLLLLL", ".DTTHW", ".TTTTW", "..ttt."],
    "focused": ["......", "LLLLLL", ".DTTHW", "..ttt."],
    "tired": ["......", "......", "LLLLLL", "..tTt."],
    "surprised": [".LLLLL", ".WTTHW", ".TTTTW", ".TTttW", "..ttt."],
    "blink": ["......", "......", "......", "LLLLLL"],
    "happy": ["......", "..LLL.", ".L...L", "......"],
}
MOUTH = {"normal": ["....", ".MM."], "focused": ["....", ".MM."], "tired": ["....", ".mm."], "surprised": ["....", ".oo."],
         "blink": ["....", ".MM."], "happy": ["m..m", ".MM."]}


def face_tex(expr):
    N = 16
    g = [[SKIN] * N for _ in range(N)]
    for y in range(N):
        for x in range(N):
            c = SKIN
            if y < 6:
                c = mix(SKIN_SH, SKIN, y / 6)
            if min(x, N - 1 - x) < 1:
                c = mix(c, SKIN_SH, 0.6)
            if y >= 15:
                c = SKIN_SH
            g[y][x] = c
    col = {"L": (28, 26, 40), "D": (60, 50, 80), "T": TEAL_DK, "t": TEAL, "H": TEAL_HI, "W": (236, 236, 244),
           "M": (176, 132, 168), "m": (214, 186, 214), "o": (120, 60, 90)}
    eye = EYE[expr]
    ey = 8 - (1 if expr == "surprised" else 0)
    for j, row in enumerate(eye):
        for i, ch in enumerate(row):
            if ch == ".":
                continue
            g[ey + j][1 + i] = col[ch]
            g[ey + j][N - 2 - i] = col[ch]
    # el brillo y el blanco van hacia el lado de afuera en ambos ojos (espejo)
    for (cx,) in ((3,), (12,)):
        pass
    if expr in ("normal", "happy", "blink"):
        for x in (2, 3, 12, 13):
            g[11][x] = mix(g[11][x], (240, 170, 200), 0.35)
    m = MOUTH[expr]
    for j, row in enumerate(m):
        for i, ch in enumerate(row):
            if ch != ".":
                g[12 + j][6 + i] = col[ch]

    img = Image.new("RGBA", (N, N))
    for y in range(N):
        for x in range(N):
            img.putpixel((x, y), g[y][x] + (255,))
    face = img.resize((64, 64), Image.NEAREST)
    tex = Image.new("RGBA", (256, 128), T)
    S = 8

    def fill(u, v, w, h, c):
        tex.paste(Image.new("RGBA", (w * S, h * S), c + (255,)), (u * S, v * S))
    fill(8, 0, 8, 8, HAIR)          # arriba
    fill(16, 0, 8, 8, SKIN_SH)      # abajo
    fill(0, 8, 8, 8, SKIN_SH)       # lados
    fill(16, 8, 8, 8, SKIN_SH)
    fill(24, 8, 8, 8, HAIR)         # atrás
    tex.paste(face, (64, 64))
    return tex


EXPRS = [("normal", ""), ("blink", "_blink"), ("happy", "_happy"), ("surprised", "_surprised"), ("focused", "_focused"), ("tired", "_tired")]


def main():
    os.makedirs(OUT, exist_ok=True)
    for expr, suf in EXPRS:
        face_tex(expr).save(os.path.join(OUT, "face" + suf + ".png"))
    G.S = 8
    G.AO = True
    G.jit = lambda c, a=4: c
    P = parts()
    w, h = G.pack(P, 64)
    G.build_texture(P, w, h).save(os.path.join(OUT, "body.png"))
    java = ["// GENERADO por tools/gen_noa.py — no editar a mano.", "package com.totallywaifu.client;", "",
            "import net.minecraft.client.model.geom.ModelPart;", "import net.minecraft.client.model.geom.PartPose;",
            "import net.minecraft.client.model.geom.builders.*;", "", "import java.util.LinkedHashMap;", "import java.util.Map;", "",
            "/** Aspecto Noa (estilo de bloques). */", "public final class NoaModels {", "    private NoaModels() {}", "",
            "    public static LayerDefinition face() {",
            "        MeshDefinition mesh = new MeshDefinition();",
            "        mesh.getRoot().addOrReplaceChild(\"head\", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.ZERO);",
            "        return LayerDefinition.create(mesh, 32, 16);", "    }", ""]
    java += G.java_layer("body", P, ANCHORS, w, h) + [""]
    java += G.java_bake("bake", P, ANCHORS) + ["}", ""]
    open(JAVA, "w").write("\n".join(java))
    print("noa: %d piezas, textura %dx%d" % (len(P), w * 8, h * 8))
    return P


def preview(outdir):
    os.makedirs(outdir, exist_ok=True)
    P = parts()
    G.S = 8
    G.pack(P, 64)
    bt = Image.open(os.path.join(OUT, "body.png")).convert("RGBA")
    ft = Image.open(os.path.join(OUT, "face.png")).convert("RGBA")
    face512 = Image.new("RGBA", (512, 256), T)
    face512.paste(ft, (0, 0))
    quads = G.quads_for(face512, -4, -8, -4, 8, 8, 8, 0, 0, [((0, 0, 0), (0, 0, 0))], 1)
    tf = G.chains(P, ANCHORS)
    for p in P:
        for b in p.boxes:
            quads += G.quads_for(bt, b.x, b.y, b.z, b.w, b.h, b.d, b.u, b.v, tf[p.name], 1)
    G.draw(quads, os.path.join(outdir, "noa_preview.png"), 13, 140,
           [(math.pi, 0.05), (math.pi / 2, 0.05), (math.pi - 0.7, 0.1)], (280, 480))
    head = [qc for qc in quads if min(p[1] for p in qc[0]) < 1.0]
    G.draw(head, os.path.join(outdir, "noa_face.png"), 30, 300, [(math.pi, 0.02)], (420, 340))


if __name__ == "__main__":
    main()
    if len(sys.argv) > 2 and sys.argv[1] == "preview":
        preview(sys.argv[2])
