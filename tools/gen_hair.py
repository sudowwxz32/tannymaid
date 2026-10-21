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

W, L, M, D = (250, 250, 250), (228, 228, 228), (200, 200, 200), (170, 170, 170)

def strands(f, x, y, w, h):
    if f == "top":
        return W if (x + y) % 3 else L
    if f == "bottom":
        return D
    if x % 3 == 0:
        return M                     # mechones
    return L if y < h - 1 else M

def tip(f, x, y, w, h):
    if f == "bottom":
        return D
    return W if y < h * 0.4 else (L if x % 2 else M)

SPIKE_TIPS = []

def spike(name, pivot, rot, length, size):
    """Punta afilada: base gruesa + punta fina (pieza hija), un poco más inclinada."""
    base = length * 0.6
    t = size * 0.55
    SPIKE_TIPS.append(Part(name + "_tip", name, (0, -base + 0.3, 0), (rot[0] * 0.25, 0, rot[2] * 0.25),
                           [Box(-t / 2, -(length - base) - 0.3, -t / 2, t, length - base + 0.3, t, tip)]))
    return Part(name, "head", pivot, rot, [Box(-size / 2, -base, -size / 2, size, base, size, tip)])

def parts():
    P = []
    # ---- común
    P.append(Part("c_cap", "head", (0, 0, 0), (0, 0, 0), [Box(-4.6, -8.8, -4.6, 9.2, 2.6, 9.2, strands)]))
    P.append(Part("c_back", "head", (0, -7, 4.0), (0.08, 0, 0), [Box(-4.7, 0, -1.0, 9.4, 6.5, 1.6, strands)]))
    P.append(Part("c_side_r", "head", (-4.2, -7.4, -0.5), (0, 0, 0.05), [Box(-0.8, 0, -3.4, 1.3, 4.0, 7.2, strands)]))
    # ---- clásico (falso, fase 1, fase 2, blue)
    P.append(spike("s_top", (0, -8.5, -1.0), (-0.25, 0, 0), 10.0, 3.0))
    P.append(spike("s_top_r", (-2.5, -8.4, -0.5), (-0.2, 0, -0.45), 9.0, 2.7))
    P.append(spike("s_side_r", (-3.6, -7.6, 1.2), (0.1, 0, -1.0), 7.5, 2.5))
    P.append(spike("s_back", (0, -8.3, 2.4), (0.6, 0, 0), 9.0, 2.9))
    P.append(spike("s_back_r", (-2.3, -7.8, 3.0), (0.65, 0, -0.45), 8.0, 2.5))
    P.append(spike("s_low", (0, -5.8, 4.2), (1.25, 0, 0), 6.5, 2.5))
    P.append(spike("s_low_r", (-2.8, -5.5, 3.8), (1.15, 0, -0.6), 6.0, 2.3))
    P.append(spike("s_front", (1.2, -8.6, -3.4), (-0.75, 0, 0.25), 6.5, 2.3))
    P.append(Part("s_bang_r", "head", (-1.8, -8.0, -4.3), (-0.15, 0, 0.3), [Box(-0.7, 0, -0.6, 1.4, 3.2, 1.0, tip)]))
    # ---- fase 3: cabello muy largo
    P.append(Part("l_back", "head", (0, -7.6, 3.8), (0.16, 0, 0), [Box(-4.7, 0, -1.2, 9.4, 18.0, 2.6, strands)]))
    P.append(Part("l_back2", "l_back", (0, 17.5, 0), (0.08, 0, 0), [Box(-3.6, 0, -1.0, 7.2, 8.0, 2.2, strands)]))
    P.append(Part("l_tip", "l_back2", (0, 7.6, 0), (0.05, 0, 0), [Box(-2.0, 0, -0.8, 4.0, 4.5, 1.8, tip)]))
    P.append(Part("l_side_r", "head", (-4.3, -7.2, 0.2), (0, 0, 0.12), [Box(-1.3, 0, -3.2, 1.6, 10.0, 6.4, strands)]))
    P.append(spike("l_top", (0, -8.6, 0.5), (0.7, 0, 0), 4.5, 3.6))
    P.append(spike("l_top_r", (-2.4, -8.3, 0.8), (0.65, 0, -0.35), 4.0, 2.6))
    P.append(Part("l_bang", "head", (1.4, -8.0, -4.3), (-0.1, 0, -0.08), [Box(-0.8, 0, -0.6, 1.6, 5.5, 1.0, tip)]))
    # ---- Dios / Ultra Instinto: puntas más bajas y ordenadas
    P.append(spike("g_top", (0, -8.5, -0.5), (-0.45, 0, 0), 5.5, 2.5))
    P.append(spike("g_top_r", (-2.4, -8.3, 0), (-0.25, 0, -0.55), 5.0, 2.3))
    P.append(spike("g_back", (0, -8.0, 2.6), (0.85, 0, 0), 5.8, 2.5))
    P.append(spike("g_back_r", (-2.4, -7.4, 3.0), (0.9, 0, -0.5), 5.0, 2.3))
    P.append(Part("g_bang_r", "head", (-1.6, -8.0, -4.3), (-0.12, 0, 0.25), [Box(-0.7, 0, -0.6, 1.4, 2.6, 1.0, tip)]))
    P += SPIKE_TIPS
    P += mirror([p for p in P if p.name.endswith("_r") or p.name.endswith("_r_tip")])
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
    styles = [("Fase 1 / 2 / Blue", "s_", (255, 216, 58)), ("Fase 3", "l_", (255, 207, 46)),
              ("Dios", "g_", (224, 48, 76)), ("Blue", "s_", (63, 200, 255)), ("Ultra Instinto", "g_", (221, 227, 242))]
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
