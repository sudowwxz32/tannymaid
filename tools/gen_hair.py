"""Cabello saiyajin 3D (gris claro; se tiñe con el color de cada fase al renderizar).

Cada mechón es una punta de 4 segmentos (raíz gruesa -> punta fina) que se mueve sola.
Convención del modelo: -y = arriba, -z = cara.  xRot negativo inclina la punta hacia atrás,
zRot negativo la inclina hacia el lado derecho (-x).

Prefijos de estilo:
  c_ común (cráneo)   s_ clásico (Falso, Fase 1, Blue)   x_ mechón extra de la Fase 2
  l_ Fase 3 (largo)    g_ Dios / Ultra Instinto           f_ Fase 4
  e_ Ultra Ego         b_ Bestia                          k_ Legendario      r_ Rosé
Marcas en el nombre:  _h = mechón colgante (se mece al caminar)
Sufijos de segmento:  (raíz) _m _t _p   ·  mechón que cae: (raíz) _e
"""
import os, sys, math
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import modelgen_base as mg
from modelgen_base import Box, Part, pack, build_texture, mirror

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX = os.path.join(ROOT, "src/main/resources/assets/blockdragonsuperz/textures/entity/saiyan_hair.png")
JAVA = os.path.join(ROOT, "src/main/java/com/bdsz/client/HairModelData.java")

G = [(128, 128, 128), (160, 160, 160), (190, 190, 190), (216, 216, 216), (238, 238, 238), (255, 255, 255)]


def shade(level):
    """level 0 = raíz (oscura) .. 3 = punta (clara). Bordes más oscuros para que cada mechón se distinga."""
    def fn(f, x, y, w, h):
        base = G[1 + level] if level < 4 else G[5]
        if f == "top":
            return G[min(5, 2 + level)]
        if f == "bottom":
            return G[max(0, level)]
        if x == 0 or x == w - 1:
            return G[max(0, level)]                 # contorno
        if y == h - 1 and level == 0:
            return G[0]                             # raíz en sombra
        if (x + y * 2) % 5 == 0:
            return G[min(5, 2 + level)]             # brillo
        return base
    return fn


def strands(f, x, y, w, h):
    if f == "top":
        return G[4] if (x + y) % 3 else G[3]
    if f == "bottom":
        return G[1]
    if x % 3 == 0:
        return G[2]
    return G[3] if y < h - 1 else G[2]


TAILS = []


def spike(name, pivot, rot, length, size, bend=(0.0, 0.0), parent="head", sink=0.6):
    """Punta en 4 segmentos que se afina hasta un pico; bend = giro extra (x, z) en cada unión."""
    ls = [length * 0.34, length * 0.28, length * 0.22, length * 0.18]
    ss = [size, size * 0.74, size * 0.48, max(0.5, size * 0.24)]
    bx, bz = bend
    names = [name, name + "_m", name + "_t", name + "_p"]
    for i in (1, 2, 3):
        TAILS.append(Part(names[i], names[i - 1], (0, -ls[i - 1] + 0.25, 0), (bx, 0, bz),
                          [Box(-ss[i] / 2, -ls[i] - 0.25, -ss[i] / 2, ss[i], ls[i] + 0.25, ss[i], shade(i))]))
    return Part(name, parent, pivot, rot, [Box(-ss[0] / 2, -ls[0], -ss[0] / 2, ss[0], ls[0] + sink, ss[0], shade(0))])


def out(name, pivot, rx, rz, length, size, bend=0.06, **kw):
    """Punta que se curva un poco más hacia afuera en cada segmento (sigue el sentido de su giro)."""
    sx = -bend if rx < -0.05 else (bend * 0.6 if rx > 0.05 else 0.0)
    sz = -bend if rz < -0.05 else (bend if rz > 0.05 else 0.0)
    return spike(name, pivot, (rx, 0, rz), length, size, (sx, sz), **kw)


def strand(name, pivot, rot, length, w, d=1.0):
    """Mechón que cae (flequillo / patilla), más fino hacia la punta."""
    TAILS.append(Part(name + "_e", name, (0, length * 0.58 - 0.1, 0), (rot[0] * 0.6, 0, rot[2] * 0.7),
                      [Box(-w * 0.3, 0, -d / 2 + 0.1, w * 0.6, length * 0.47, d - 0.2, shade(2))]))
    return Part(name, "head", pivot, rot, [Box(-w / 2, 0, -d / 2, w, length * 0.6, d, shade(1))])


def hang(name, pivot, rx, rz, length, size):
    """Mechón largo colgante (cae por la espalda) que se mece al caminar."""
    return spike(name, pivot, (rx, 0, rz), length, size, (-0.05, 0.0))


def parts():
    TAILS.clear()
    P = []
    # ---- común: cráneo cubierto (arriba, nuca y lados) + mechones redondeados + patillas
    P.append(Part("c_cap", "head", (0, 0, 0), (0, 0, 0), [Box(-4.3, -8.6, -4.3, 8.6, 2.0, 8.6, strands)]))
    P.append(Part("c_back", "head", (0, 0, 0), (0, 0, 0), [Box(-4.3, -8.0, 3.45, 8.6, 6.0, 0.9, strands)]))
    P.append(Part("c_side_r", "head", (0, 0, 0), (0, 0, 0), [Box(-4.4, -8.0, -2.0, 0.9, 4.0, 5.6, strands)]))
    P.append(out("c_k1", (0, -8.3, -2.0), 0.15, 0, 3.0, 4.6, sink=0.8))
    P.append(out("c_k2", (0, -8.3, 1.4), -0.4, 0, 3.2, 4.6, sink=0.8))
    P.append(out("c_k3", (0, -6.4, 3.9), -1.2, 0, 3.0, 4.4, sink=0.8))
    P.append(out("c_k4", (0, -3.4, 4.0), -1.75, 0, 2.6, 3.8, sink=0.8))
    P.append(out("c_k_r1", (-2.7, -8.1, -1.4), 0.1, -0.5, 2.8, 4.0, sink=0.8))
    P.append(out("c_k_r2", (-2.7, -8.0, 1.8), -0.45, -0.5, 2.8, 4.0, sink=0.8))
    P.append(out("c_k_r3", (-3.9, -6.4, 0.2), -0.1, -1.25, 2.6, 3.6, sink=0.8))
    P.append(out("c_k_r4", (-2.8, -4.0, 3.8), -1.6, -0.5, 2.4, 3.4, sink=0.8))
    P.append(strand("c_burn_r", (-4.2, -4.8, -2.4), (0, 0, 0.06), 3.8, 1.3))

    # ---- clásico (Falso, Fase 1, Blue): corona de puntas que se abren hacia arriba y afuera
    P.append(out("s_c1", (0, -8.3, -0.6), -0.08, 0, 11.5, 4.3))
    P.append(out("s_c2", (0, -8.2, 2.2), -0.55, 0, 11.0, 4.1))
    P.append(out("s_c3", (0, -6.6, 3.9), -1.05, 0, 9.5, 3.9))
    P.append(out("s_c4", (0, -3.8, 4.1), -1.5, 0, 7.5, 3.5))
    P.append(out("s_r1", (-2.3, -8.3, -1.3), 0.05, -0.4, 10.5, 3.9))
    P.append(out("s_r2", (-2.4, -8.2, 1.6), -0.45, -0.45, 10.0, 3.8))
    P.append(out("s_r3", (-3.7, -7.2, -0.6), 0.05, -0.95, 9.0, 3.5))
    P.append(out("s_r4", (-3.8, -6.6, 2.4), -0.6, -0.95, 8.5, 3.4))
    P.append(out("s_r5", (-2.6, -6.2, 3.8), -1.0, -0.5, 8.5, 3.4))
    P.append(out("s_r6", (-3.9, -4.6, 1.0), -0.3, -1.4, 6.5, 3.1))
    P.append(out("s_r7", (-3.0, -3.6, 3.9), -1.4, -0.6, 6.5, 3.1))
    P.append(out("s_f1", (1.2, -8.4, -3.2), 0.35, 0.15, 6.0, 3.1))
    P.append(out("s_f2_r", (-1.6, -8.4, -3.2), 0.4, -0.25, 5.5, 2.9))
    P.append(strand("s_b1", (1.7, -8.1, -4.35), (-0.1, 0, -0.2), 4.6, 1.6))
    P.append(strand("s_b2_r", (-2.5, -8.1, -4.3), (-0.1, 0, 0.3), 3.8, 1.4))
    P.append(strand("x_bang", (-0.5, -8.2, -4.45), (-0.1, 0, 0.12), 7.5, 1.6))      # Fase 2: mechón que cae

    # ---- Rosé: como la foto; puntas más rectas hacia arriba, corona alta
    P.append(out("r_c1", (0, -8.4, -1.0), 0.06, 0, 12.5, 4.4))
    P.append(out("r_c2", (0, -8.3, 1.8), -0.3, 0, 12.0, 4.2))
    P.append(out("r_c3", (0, -7.0, 3.8), -0.75, 0, 10.0, 3.9))
    P.append(out("r_c4", (0, -4.6, 4.1), -1.25, 0, 7.5, 3.5))
    P.append(out("r_r1", (-2.4, -8.3, -1.6), 0.08, -0.32, 11.5, 3.9))
    P.append(out("r_r2", (-2.5, -8.2, 1.4), -0.3, -0.38, 11.0, 3.8))
    P.append(out("r_r3", (-3.7, -7.4, -0.4), 0.0, -0.75, 9.5, 3.5))
    P.append(out("r_r4", (-3.8, -6.8, 2.4), -0.45, -0.8, 9.0, 3.4))
    P.append(out("r_r5", (-3.9, -5.0, 0.6), -0.1, -1.2, 7.0, 3.1))
    P.append(out("r_r6", (-2.8, -5.2, 3.8), -0.95, -0.6, 7.5, 3.1))
    P.append(out("r_f1", (0.5, -8.5, -3.3), 0.28, 0.06, 7.0, 3.3))
    P.append(out("r_f2_r", (-2.2, -8.4, -3.3), 0.3, -0.3, 6.5, 3.1))
    P.append(strand("r_b1", (2.3, -8.1, -4.35), (-0.1, 0, -0.18), 4.0, 1.5))
    P.append(strand("r_b2_r", (-1.0, -8.1, -4.35), (-0.1, 0, 0.15), 3.6, 1.4))

    # ---- Bestia: cabello enorme y parado, casi vertical, con un mechón largo al frente
    P.append(out("b_c1", (0, -8.4, -0.6), 0.0, 0, 21.0, 4.8, bend=0.02))
    P.append(out("b_c2", (0, -8.3, 2.0), -0.16, 0, 20.0, 4.6, bend=0.02))
    P.append(out("b_c3", (0, -7.4, 3.8), -0.38, 0, 17.0, 4.2, bend=0.03))
    P.append(out("b_c4", (0, -5.2, 4.1), -0.75, 0, 12.0, 3.8, bend=0.03))
    P.append(out("b_r1", (-2.4, -8.3, -1.6), 0.02, -0.15, 19.5, 4.2, bend=0.02))
    P.append(out("b_r2", (-2.5, -8.2, 1.4), -0.14, -0.2, 19.0, 4.2, bend=0.02))
    P.append(out("b_r3", (-3.7, -7.4, -0.4), 0.0, -0.36, 16.0, 3.8, bend=0.03))
    P.append(out("b_r4", (-3.8, -6.8, 2.4), -0.25, -0.4, 15.0, 3.7, bend=0.03))
    P.append(out("b_r5", (-3.9, -5.4, 0.6), -0.05, -0.68, 11.0, 3.4))
    P.append(out("b_r6", (-2.8, -5.6, 3.8), -0.55, -0.4, 12.0, 3.4))
    P.append(out("b_f1", (0.8, -8.5, -3.3), 0.1, 0.05, 13.0, 3.8, bend=0.02))
    P.append(out("b_f2_r", (-2.2, -8.4, -3.3), 0.12, -0.14, 12.0, 3.5, bend=0.02))
    P.append(strand("b_bang", (-1.2, -8.2, -4.45), (-0.12, 0, 0.14), 10.0, 1.9))

    # ---- Legendario: melena salvaje enorme que se abre en todas direcciones
    P.append(out("k_c1", (0, -8.4, -0.8), -0.05, 0, 15.0, 5.0, bend=0.08))
    P.append(out("k_c2", (0, -8.2, 2.0), -0.55, 0, 15.5, 4.9, bend=0.08))
    P.append(out("k_c3", (0, -6.8, 3.9), -1.0, 0, 14.5, 4.7, bend=0.08))
    P.append(out("k_c4", (0, -4.0, 4.2), -1.45, 0, 12.5, 4.3, bend=0.07))
    P.append(out("k_c5", (0, -2.4, 4.0), -1.7, 0, 9.0, 3.9, bend=0.05))
    P.append(out("k_r1", (-2.4, -8.3, -1.6), 0.05, -0.45, 14.5, 4.5, bend=0.09))
    P.append(out("k_r2", (-2.5, -8.2, 1.4), -0.45, -0.5, 14.5, 4.5, bend=0.09))
    P.append(out("k_r3", (-3.8, -7.2, -0.6), 0.05, -0.78, 13.5, 4.1, bend=0.08))
    P.append(out("k_r4", (-3.9, -6.6, 2.4), -0.55, -0.82, 13.5, 4.1, bend=0.08))
    P.append(out("k_r5", (-4.0, -5.0, 0.4), -0.15, -1.12, 11.5, 3.7, bend=0.07))
    P.append(out("k_r6", (-3.0, -5.6, 3.9), -1.0, -0.6, 12.5, 3.9, bend=0.08))
    P.append(out("k_r7", (-3.4, -3.4, 3.4), -1.35, -0.6, 10.5, 3.5, bend=0.06))
    P.append(out("k_f1", (0.8, -8.5, -3.3), 0.25, 0.12, 8.5, 3.7))
    P.append(out("k_f2_r", (-2.2, -8.4, -3.3), 0.3, -0.35, 8.0, 3.5))

    # ---- Fase 3: melena muy larga hasta la cintura, hecha de mechones en punta
    P.append(hang("l_h0", (0, -7.0, 3.9), -3.0, 0, 30.0, 7.0))
    P.append(hang("l_h1_r", (-2.6, -6.6, 3.6), -3.0, -0.16, 28.0, 6.0))
    P.append(hang("l_h2_r", (-4.0, -6.0, 2.0), -3.05, -0.3, 24.0, 5.4))
    P.append(hang("l_h3", (0, -3.6, 4.1), -3.05, 0, 26.0, 6.0))
    P.append(hang("l_h4_r", (-2.4, -3.0, 3.9), -3.05, -0.2, 24.0, 5.2))
    P.append(hang("l_h5_r", (-4.2, -6.4, -1.0), -3.08, -0.22, 15.0, 4.0))
    P.append(out("l_t1", (0, -8.4, 0.2), -0.5, 0, 6.5, 4.6))
    P.append(out("l_t2_r", (-2.4, -8.2, 0.4), -0.4, -0.45, 6.0, 4.0))
    P.append(out("l_t3_r", (-3.6, -7.4, -1.0), 0.0, -0.85, 5.0, 3.4))
    P.append(out("l_t4", (0.6, -8.4, -2.6), 0.3, 0.1, 5.0, 3.6))
    P.append(strand("l_bang", (1.3, -8.2, -4.4), (-0.12, 0, -0.08), 8.0, 1.7))

    # ---- Dios / Ultra Instinto: puntas medianas y ordenadas
    P.append(out("g_c1", (0.3, -8.4, -1.0), 0.12, -0.05, 7.5, 3.7))
    P.append(out("g_c2", (0, -8.0, 2.4), -0.75, 0, 7.5, 3.6))
    P.append(out("g_c3", (0, -5.4, 4.0), -1.3, 0, 6.0, 3.3))
    P.append(out("g_r1", (-2.3, -8.3, -0.6), 0.1, -0.45, 7.0, 3.4))
    P.append(out("g_r2", (-1.0, -8.5, 1.0), -0.4, -0.15, 7.2, 3.5))
    P.append(out("g_r3", (-2.4, -7.4, 3.0), -0.9, -0.45, 6.5, 3.2))
    P.append(out("g_r4", (-3.5, -7.2, 0.8), -0.25, -0.8, 6.0, 3.1))
    P.append(out("g_r5", (-3.1, -5.0, 3.4), -1.25, -0.55, 5.5, 3.0))
    P.append(out("g_f1", (1.0, -8.4, -3.2), 0.45, 0.2, 5.5, 3.1))
    P.append(out("g_f2_r", (-1.6, -8.4, -3.2), 0.5, -0.3, 5.0, 2.9))
    P.append(strand("g_b1_r", (-1.5, -8.1, -4.35), (-0.12, 0, 0.22), 3.6, 1.4))
    P.append(strand("g_b2", (1.8, -8.1, -4.35), (-0.1, 0, -0.2), 3.2, 1.3))

    # ---- Fase 4: puntas salvajes arriba y melena larga por la espalda
    P.append(out("f_c1", (0.3, -8.4, -0.8), 0.05, -0.05, 8.5, 4.1))
    P.append(out("f_c2", (0, -8.2, 1.8), -0.7, 0, 8.5, 4.1))
    P.append(out("f_r1", (-2.3, -8.3, -0.5), -0.15, -0.55, 8.0, 3.7))
    P.append(out("f_r2", (-3.6, -7.2, 1.0), -0.4, -1.05, 7.5, 3.5))
    P.append(hang("f_h0", (0, -6.4, 3.9), -2.95, 0, 22.0, 5.6))
    P.append(hang("f_h1_r", (-2.2, -6.2, 3.7), -2.95, -0.3, 20.0, 5.0))
    P.append(hang("f_h2_r", (-3.8, -5.8, 2.2), -2.9, -0.5, 16.0, 4.4))
    P.append(hang("f_h3", (0, -3.0, 4.0), -3.0, 0, 18.0, 4.8))
    P.append(hang("f_h4_r", (-4.1, -6.0, -0.6), -2.9, -0.35, 11.0, 3.4))
    P.append(strand("f_b1", (1.3, -8.2, -4.4), (-0.15, 0, -0.25), 5.5, 1.6))
    P.append(strand("f_b2_r", (-1.6, -8.2, -4.4), (-0.15, 0, 0.3), 5.0, 1.5))
    P.append(strand("f_b3", (-0.1, -8.3, -4.45), (-0.05, 0, 0.05), 4.0, 1.4))

    # ---- Ultra Ego: cabello como llamas que sube recto, con pico en la frente
    P.append(out("e_c1", (0, -8.5, -2.2), 0.05, 0, 13.0, 4.5, bend=0.03))
    P.append(out("e_c2", (0, -8.5, 0.6), -0.12, 0, 15.0, 4.7, bend=0.03))
    P.append(out("e_c3", (0, -8.0, 3.0), -0.4, 0, 13.0, 4.3, bend=0.04))
    P.append(out("e_c4", (0, -5.8, 4.0), -0.8, 0, 9.0, 3.7))
    P.append(out("e_r1", (-2.3, -8.4, -1.8), 0.05, -0.2, 12.5, 4.1, bend=0.03))
    P.append(out("e_r2", (-2.4, -8.3, 1.2), -0.2, -0.24, 13.0, 4.1, bend=0.03))
    P.append(out("e_r3", (-3.6, -7.6, -0.4), 0.0, -0.42, 10.5, 3.7))
    P.append(out("e_r4", (-3.4, -7.2, 2.4), -0.4, -0.45, 10.0, 3.5))
    P.append(out("e_r5", (-2.4, -6.4, 3.7), -0.7, -0.28, 9.0, 3.4))
    P.append(strand("e_peak", (0, -8.3, -4.45), (-0.06, 0, 0), 3.4, 2.4))

    P += TAILS
    P += mirror([p for p in P if "_r" in p.name])
    return P


CHUNK = 120


def java_file(P, w, h):
    lines = ["// GENERADO por tools/gen_hair.py — no editar a mano.", "package com.bdsz.client;", "",
             "import net.minecraft.client.model.geom.ModelPart;", "import net.minecraft.client.model.geom.PartPose;",
             "import net.minecraft.client.model.geom.builders.*;", "", "import java.util.HashMap;", "import java.util.Map;", "",
             "public final class HairModelData {", "    private HairModelData() {}", "",
             "    public static LayerDefinition createLayer() {",
             "        MeshDefinition mesh = new MeshDefinition();",
             "        Map<String, PartDefinition> d = new HashMap<>();",
             '        d.put("head", mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO));']
    chunks = [P[i:i + CHUNK] for i in range(0, len(P), CHUNK)]
    for i in range(len(chunks)):
        lines.append("        def%d(d);" % i)
    lines += ["        return LayerDefinition.create(mesh, %d, %d);" % (w, h), "    }", ""]
    for i, ch in enumerate(chunks):
        lines.append("    private static void def%d(Map<String, PartDefinition> d) {" % i)
        for p in ch:
            cubes = "CubeListBuilder.create()"
            for b in p.boxes:
                cubes += ".texOffs(%d, %d).addBox(%s, %s, %s, %s, %s, %s)" % (
                    b.u, b.v, mg.fnum(b.x), mg.fnum(b.y), mg.fnum(b.z), mg.fnum(b.w), mg.fnum(b.h), mg.fnum(b.d))
            lines.append('        d.put("%s", d.get("%s").addOrReplaceChild("%s", %s, PartPose.offsetAndRotation(%s, %s, %s, %s, %s, %s)));' % (
                p.name, p.parent, p.name, cubes, mg.fnum(p.pivot[0]), mg.fnum(p.pivot[1]), mg.fnum(p.pivot[2]),
                mg.fnum(p.rot[0]), mg.fnum(p.rot[1]), mg.fnum(p.rot[2])))
        lines += ["    }", ""]
    # bake: nombre -> pieza, más el padre de cada pieza
    lines += ["    public static final Map<String, String> PARENT = new HashMap<>();", "    static {"]
    for i in range(len(chunks)):
        lines.append("        par%d();" % i)
    lines += ["    }", ""]
    for i, ch in enumerate(chunks):
        lines.append("    private static void par%d() {" % i)
        for p in ch:
            lines.append('        PARENT.put("%s", "%s");' % (p.name, p.parent))
        lines += ["    }", ""]
    lines += ["    public static Map<String, ModelPart> bake(ModelPart root) {",
              "        Map<String, ModelPart> m = new HashMap<>();",
              '        m.put("root", root);',
              '        m.put("head", root.getChild("head"));']
    order = [p.name for p in P]
    lines += ["        for (String n : ORDER) m.put(n, m.get(PARENT.get(n)).getChild(n));",
              "        return m;", "    }", "",
              "    private static final String[] ORDER = {"]
    for i in range(0, len(order), 8):
        lines.append("            " + ", ".join('"%s"' % n for n in order[i:i + 8]) + ",")
    lines += ["    };", "}", ""]
    return "\n".join(lines)


def main(preview_dir=None):
    P = parts()
    w, h = pack(P, 128)
    img = build_texture(P, w, h)
    os.makedirs(os.path.dirname(TEX), exist_ok=True)
    img.save(TEX)
    open(JAVA, "w").write(java_file(P, w, h))
    print("hair %dx%d, %d partes" % (w, h, len(P)))
    return P, img


if __name__ == "__main__":
    main()
