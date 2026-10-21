"""Cabello saiyajin 3D (gris claro, se tiñe con el color de cada fase al renderizar).
Prefijos: c_ = siempre, s_ = puntas clásicas, l_ = cabello largo (fase 3), g_ = Dios / Ultra Instinto."""
import os, sys, math
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import modelgen_base as mg
from modelgen_base import Box, Part, pack, paint_box, java_layer, java_bake, build_texture, mirror
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX = os.path.join(ROOT, "src/main/resources/assets/blockdragonsuperz/textures/entity/saiyan_hair.png")
JAVA = os.path.join(ROOT, "src/main/java/com/bdsz/client/HairModelData.java")

W, L, M, D, DD = (255, 255, 255), (236, 236, 236), (212, 212, 212), (184, 184, 184), (150, 150, 150)

def shade(level):
    """Degradado a lo largo de la punta: 0 = raíz (más oscuro), 2 = punta (más claro)."""
    base = [M, L, W][level]
    dark = [D, M, L][level]
    def fn(f, x, y, w, h):
        if f == "bottom":
            return dark
        if f == "top":
            return W
        if x == 0 or x == w - 1:
            return dark                      # borde: da volumen
        return base if (x + y) % 4 else [L, W, W][level]
    return fn

def strands(f, x, y, w, h):
    if f == "top":
        return W if (x + y) % 3 else L
    if f == "bottom":
        return D
    if x % 3 == 0:
        return M
    return L if y < h - 1 else M

TAILS = []

def spike(name, pivot, rot, length, size, bend=0.07, parent="head"):
    """Punta en 3 segmentos (raíz gruesa, medio, punta fina) que se curva hacia afuera."""
    l0, l1, l2 = length * 0.42, length * 0.33, length * 0.32
    s0, s1, s2 = size, size * 0.74, size * 0.44
    bx = rot[0] * bend * 2
    bz = rot[2] * bend * 2
    TAILS.append(Part(name + "_m", name, (0, -l0 + 0.25, 0), (bx, 0, bz),
                      [Box(-s1 / 2, -l1 - 0.25, -s1 / 2, s1, l1 + 0.25, s1, shade(1))]))
    TAILS.append(Part(name + "_t", name + "_m", (0, -l1 + 0.2, 0), (bx, 0, bz),
                      [Box(-s2 / 2, -l2 - 0.2, -s2 / 2, s2, l2 + 0.2, s2, shade(2))]))
    return Part(name, parent, pivot, rot, [Box(-s0 / 2, -l0, -s0 / 2, s0, l0, s0, shade(0))])

def strand(name, pivot, rot, length, w, d=1.0):
    """Mechón que cae (flequillo / patilla), más fino hacia la punta."""
    TAILS.append(Part(name + "_e", name, (0, length * 0.6 - 0.1, 0), (rot[0] * 0.5, 0, rot[2] * 0.6),
                      [Box(-w * 0.32, 0, -d / 2 + 0.1, w * 0.64, length * 0.45, d - 0.2, shade(1))]))
    return Part(name, "head", pivot, rot, [Box(-w / 2, 0, -d / 2, w, length * 0.6, d, shade(0))])

def parts():
    P = []
    # ---- común: volumen sobre la cabeza, nuca y patillas
    P.append(Part("c_cap", "head", (0, 0, 0), (0, 0, 0), [Box(-4.6, -8.9, -4.6, 9.2, 2.7, 9.2, strands)]))
    P.append(Part("c_back", "head", (0, -7.2, 4.0), (0.1, 0, 0), [Box(-4.7, 0, -1.0, 9.4, 6.2, 1.6, strands)]))
    P.append(Part("c_side_r", "head", (-4.2, -7.6, -0.4), (0, 0, 0.06), [Box(-0.8, 0, -3.6, 1.3, 3.6, 7.4, strands)]))
    P.append(strand("c_burn_r", (-4.15, -4.6, -2.6), (0, 0, 0.08), 3.6, 1.1))

    # ---- clásico (falso, fase 1, Blue) y fase 2 (x_ = mechón extra)
    P.append(spike("s_top", (0.3, -8.4, -1.4), (0.12, 0, -0.03), 10.5, 3.9))
    P.append(spike("s_top_r", (-2.3, -8.3, -0.9), (0.2, 0, -0.34), 9.5, 3.6))
    P.append(spike("s_top2_r", (-1.1, -8.4, 1.2), (0.45, 0, -0.14), 9.8, 3.7))
    P.append(spike("s_side_r", (-3.4, -7.6, 0.4), (0.3, 0, -0.68), 7.5, 3.3))
    P.append(spike("s_side2_r", (-3.5, -6.4, 2.4), (0.62, 0, -0.85), 6.5, 3.0))
    P.append(spike("s_back", (0, -8.2, 2.6), (0.78, 0, 0), 9.2, 3.8))
    P.append(spike("s_back_r", (-2.3, -7.8, 3.0), (0.86, 0, -0.34), 8.2, 3.4))
    P.append(spike("s_low", (0, -5.9, 4.0), (1.25, 0, 0), 6.8, 3.4))
    P.append(spike("s_low_r", (-2.7, -5.6, 3.7), (1.15, 0, -0.48), 6.2, 3.1))
    P.append(spike("s_front", (1.0, -8.5, -3.3), (-0.5, 0, 0.16), 6.2, 3.0))
    P.append(spike("s_front_r", (-1.6, -8.4, -3.2), (-0.56, 0, -0.2), 5.2, 2.7))
    P.append(strand("s_bang", (2.2, -8.1, -4.35), (-0.12, 0, -0.18), 3.8, 1.5))
    P.append(strand("x_bang", (-0.9, -8.2, -4.4), (-0.08, 0, 0.12), 6.5, 1.3))      # fase 2: mechón que cae

    # ---- fase 3: cabello muy largo
    P.append(Part("l_back", "head", (0, -7.6, 3.8), (0.16, 0, 0), [Box(-4.8, 0, -1.2, 9.6, 18.0, 2.6, strands)]))
    P.append(Part("l_back2", "l_back", (0, 17.5, 0), (0.08, 0, 0), [Box(-3.8, 0, -1.0, 7.6, 8.0, 2.2, strands)]))
    P.append(Part("l_tip", "l_back2", (0, 7.6, 0), (0.05, 0, 0), [Box(-2.2, 0, -0.8, 4.4, 4.5, 1.8, shade(2))]))
    for i, (x, z, r) in enumerate([(-3.2, 1.0, -0.2), (3.2, 1.0, 0.2), (-1.4, 2.0, -0.08), (1.4, 2.0, 0.08)]):
        P.append(Part("l_lock%d" % i, "l_back", (x, 2.0 + i * 3, 1.2), (0.1, 0, r), [Box(-1.0, 0, -0.6, 2.0, 12.0, 1.2, shade(0))]))
    P.append(Part("l_side_r", "head", (-4.3, -7.3, 0.2), (0, 0, 0.13), [Box(-1.3, 0, -3.3, 1.6, 10.5, 6.6, strands)]))
    P.append(spike("l_top", (0, -8.7, 0.6), (0.72, 0, 0), 5.5, 3.8))
    P.append(spike("l_top_r", (-2.4, -8.4, 0.8), (0.66, 0, -0.35), 5.0, 3.0))
    P.append(strand("l_bang", (1.4, -8.1, -4.35), (-0.1, 0, -0.08), 7.5, 1.6))

    # ---- Dios / Ultra Instinto: puntas medianas, ordenadas y elegantes
    P.append(spike("g_top", (0.3, -8.5, -0.9), (0.0, 0, -0.05), 6.8, 3.4))
    P.append(spike("g_top_r", (-2.2, -8.3, -0.3), (0.12, 0, -0.42), 6.2, 3.1))
    P.append(spike("g_mid_r", (-1.0, -8.5, 1.3), (0.48, 0, -0.18), 6.4, 3.2))
    P.append(spike("g_back", (0, -8.0, 2.8), (0.9, 0, 0), 6.8, 3.4))
    P.append(spike("g_back_r", (-2.3, -7.5, 3.1), (0.95, 0, -0.42), 5.8, 3.0))
    P.append(spike("g_side_r", (-3.4, -7.5, 0.8), (0.35, 0, -0.75), 5.2, 2.8))
    P.append(strand("g_bang_r", (-1.5, -8.1, -4.35), (-0.12, 0, 0.22), 3.4, 1.4))
    P.append(strand("g_bang2", (1.8, -8.1, -4.35), (-0.1, 0, -0.2), 3.0, 1.3))

    P += TAILS
    P += mirror([p for p in P if "_r" in p.name and not p.name.startswith("l_lock")])
    return P

def main(preview_dir=None):
    P = parts()
    w, h = pack(P, 64)
    img = build_texture(P, w, h)
    os.makedirs(os.path.dirname(TEX), exist_ok=True)
    img.save(TEX)
    anchors = [("head", (0, 0, 0))]
    out = ["// GENERADO por tools/gen_hair.py — no editar a mano.", "package com.bdsz.client;", "",
           "import net.minecraft.client.model.geom.ModelPart;", "import net.minecraft.client.model.geom.PartPose;",
           "import net.minecraft.client.model.geom.builders.*;", "", "import java.util.HashMap;", "import java.util.Map;", "",
           "public final class HairModelData {", "    private HairModelData() {}", ""]
    out += java_layer("createLayer", P, anchors, w, h) + [""] + java_bake("bake", P, anchors) + ["}", ""]
    open(JAVA, "w").write("\n".join(out))
    print("hair %dx%d, %d partes" % (w, h, len(P)))
    if preview_dir:
        preview(P, anchors, img, preview_dir)

def preview(P, anchors, img, outdir):
    from PIL import ImageDraw
    styles = [("Fase 1", ("s_",), (255, 216, 58)), ("Fase 2", ("s_", "x_"), (255, 228, 92)), ("Fase 3", ("l_",), (255, 207, 46)),
              ("Dios", ("g_",), (224, 48, 76)), ("Blue", ("s_",), (63, 200, 255)), ("Ultra Instinto", ("g_",), (221, 227, 242))]
    tf = mg.world_transforms(P, anchors)
    skin = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for yy in range(16):
        for xx in range(32):
            skin.putpixel((xx, yy), (200, 160, 130, 255))
    W_ = 220
    out = Image.new("RGB", (W_ * len(styles), 300), (30, 34, 48))
    dr = ImageDraw.Draw(out)
    for k, (title, pref, col) in enumerate(styles):
        tinted = Image.new("RGBA", img.size)
        for yy in range(img.height):
            for xx in range(img.width):
                r, g, b, a = img.getpixel((xx, yy))
                tinted.putpixel((xx, yy), (r * col[0] // 255, g * col[1] // 255, b * col[2] // 255, a))
        quads = mg.quads_for(skin, -4, -8, -4, 8, 8, 8, 0, 0, [((0, 0, 0), (0, 0, 0))])
        for p in P:
            if not (p.name.startswith("c_") or p.name.startswith(pref)):
                continue
            for b in p.boxes:
                quads += mg.quads_for(tinted, b.x, b.y, b.z, b.w, b.h, b.d, b.u, b.v, tf[p.name])
        yaw, pitch = math.pi - 0.7, 0.18
        proj = []
        for q, c in quads:
            pts, dep = [], 0
            for (x, y, z) in q:
                X = x * math.cos(yaw) - z * math.sin(yaw)
                Z = x * math.sin(yaw) + z * math.cos(yaw)
                Y, Z = y * math.cos(pitch) - Z * math.sin(pitch), y * math.sin(pitch) + Z * math.cos(pitch)
                pts.append((W_ * k + W_ / 2 - X * 7, 175 + Y * 7))
                dep += Z
            proj.append((dep / 4, pts, c))
        proj.sort(key=lambda t: t[0])
        for _, pts, c in proj:
            dr.polygon(pts, fill=c)
        dr.text((W_ * k + 10, 280), title, fill=(230, 230, 230))
    out.save(os.path.join(outdir, "bdsz_hair_preview.png"))

if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else None)
