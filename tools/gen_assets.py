"""Texturas y modelos de Chakra del Zorro.

- cloak.png: chakra hirviente (para el manto, las orejas y las colas; se desplaza hacia arriba)
- beast_skin.png / beast_skin_slim.png: piel roja de las fases 4 y 5 (ojos blancos, boca abierta con colmillos)
- beast_glow.png / beast_glow_slim.png: ojos y colmillos que brillan
- bones.png + SkeletonModelData.java: esqueleto de zorro (columna, costillas, omóplatos y cráneo con hocico) de la fase 5
- bubble.png: burbuja de chakra
"""
import os, sys, math, random
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import modelgen_base as mg
from modelgen_base import Box, Part, pack, mirror
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
E = os.path.join(ROOT, "src/main/resources/assets/chakrazorro/textures/entity")
JAVA = os.path.join(ROOT, "src/main/java/com/cz/client/SkeletonModelData.java")
R = random.Random(13)
os.makedirs(E, exist_ok=True)


def jit(c, a=8):
    return tuple(max(0, min(255, v + R.randint(-a, a))) for v in c[:3])


# ----------------------------------------------------------------- manto de chakra
def cloak():
    W, H = 64, 64
    img = Image.new("RGBA", (W, H))
    bubbles = [(R.random() * W, R.random() * H, 1.5 + R.random() * 3.5) for _ in range(38)]
    for y in range(H):
        for x in range(W):
            a, b = 2 * math.pi * x / W, 2 * math.pi * y / H
            v = 0.55 + 0.2 * math.sin(3 * a + b) + 0.12 * math.sin(7 * a - 2 * b + 1.3) + 0.08 * math.sin(11 * a + 3 * b)
            for bx, by, br in bubbles:          # burbujas: anillo claro con centro más oscuro
                dx = min(abs(x - bx), W - abs(x - bx))
                dy = min(abs(y - by), H - abs(y - by))
                d = math.hypot(dx, dy)
                if d < br:
                    v += 0.35 * (1 - abs(d - br * 0.75) / (br * 0.75 + 0.01)) if d > br * 0.5 else -0.15
            v = max(0.0, min(1.0, v))
            c = int(40 + 215 * v)
            img.putpixel((x, y), (c, c, c, 255))
    img.save(os.path.join(E, "cloak.png"))


# ----------------------------------------------------------------- piel de bestia
RED, RED_DK, RED_HI, BLACK = (150, 14, 14), (70, 4, 6), (196, 30, 24), (24, 4, 6)


def beast_px(x, y):
    k = (x * 5 + y * 7 + R.randint(0, 3)) % 9
    if k == 0:
        return jit(RED_HI, 6) + (255,)
    if k in (3, 6):
        return jit(RED_DK, 5) + (255,)
    return jit(RED, 8) + (255,)


def fill(img, u, v, w, h, fn):
    for y in range(v, v + h):
        for x in range(u, u + w):
            c = fn(x - u, y - v)
            if c is not None:
                img.putpixel((x, y), c)


def beast(slim):
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    glow = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    # cabeza (0,0) 8x8x8
    fill(img, 0, 0, 32, 16, lambda x, y: beast_px(x, y))
    face_u, face_v = 8, 8
    WHITE, MOUTH = (250, 250, 245, 255), (30, 0, 2, 255)

    def face(x, y):
        # ojos blancos rasgados hacia arriba
        if (y == 3 and x in (1, 2, 5, 6)) or (y == 2 and x in (2, 5)):
            return WHITE
        if y == 1 and x in (1, 2, 5, 6):
            return jit(BLACK, 3) + (255,)                     # ceño
        # boca abierta con colmillos
        if 5 <= y <= 7 and 1 <= x <= 6:
            if y == 5 and x in (1, 3, 4, 6):
                return WHITE                                  # colmillos de arriba
            if y == 7 and x in (2, 5):
                return WHITE                                  # colmillos de abajo
            return MOUTH
        return None
    fill(img, face_u, face_v, 8, 8, face)
    fill(glow, face_u, face_v, 8, 8, lambda x, y: WHITE if face(x, y) == WHITE else None)
    # cuerpo, brazos, piernas (capas base)
    fill(img, 16, 16, 24, 16, lambda x, y: beast_px(x, y))
    fill(img, 0, 16, 16, 16, lambda x, y: beast_px(x, y))
    fill(img, 16, 48, 16, 16, lambda x, y: beast_px(x, y))
    aw = 3 if slim else 4
    fill(img, 40, 16, 8 + 2 * aw, 16, lambda x, y: beast_px(x, y))
    fill(img, 32, 48, 8 + 2 * aw, 16, lambda x, y: beast_px(x, y))
    # garras negras en las manos y los pies
    for (u, v, w) in ((40, 20, 8 + 2 * aw), (32, 52, 8 + 2 * aw), (0, 20, 16), (16, 52, 16)):
        fill(img, u, v + 10, w, 2, lambda x, y: jit(BLACK, 4) + (255,))
    sfx = "_slim" if slim else ""
    img.save(os.path.join(E, "beast_skin%s.png" % sfx))
    glow.save(os.path.join(E, "beast_glow%s.png" % sfx))


# ----------------------------------------------------------------- burbuja
def bubble():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5) / 8
            ring = max(0, 1 - abs(d - 0.8) * 7)
            core = max(0, 1 - d) * 0.25
            hi = max(0, 1 - math.hypot(x - 5, y - 5) / 2.2) * 0.8
            c = int(255 * min(1, ring + core + hi))
            img.putpixel((x, y), (c, c, c, 255))
    img.save(os.path.join(E, "bubble.png"))


# ----------------------------------------------------------------- esqueleto de zorro
BONE, BONE_HI, BONE_DK = (226, 218, 196), (244, 238, 220), (168, 156, 132)


def bone(f, x, y, w, h):
    if f == "top":
        return jit(BONE_HI, 5)
    if f == "bottom" or x == 0 or x == w - 1:
        return jit(BONE_DK, 5)
    if (x * 3 + y * 7) % 11 == 0:
        return jit(BONE_DK, 4)                   # grietas
    return jit(BONE, 6)


def skull_face(f, x, y, w, h):
    if f == "front":
        if y in (1, 2) and (x in (1, 2) or x in (w - 3, w - 2)):
            return (20, 6, 6)                    # cuencas de los ojos
        if y == 0:
            return jit(BONE_DK, 4)
    return bone(f, x, y, w, h)


def teeth(f, x, y, w, h):
    if f == "front" and y == h - 1:
        return (250, 248, 236) if x % 2 == 0 else (60, 20, 20)
    return bone(f, x, y, w, h)


def skeleton():
    P = []
    # ---- columna por la espalda (de la nuca a la cadera) y cola de hueso
    for i in range(7):
        y = 0.5 + i * 1.75
        P.append(Part("spine%d" % i, "body", (0, y, 2.4), (0.05, 0, 0), [Box(-0.9, -0.6, -0.4, 1.8, 1.3, 1.6, bone),
                                                                       Box(-0.4, -0.9, 0.9, 0.8, 0.8, 1.2, bone)]))
    # ---- costillas que abrazan el torso
    for i, y in enumerate((2.6, 4.4, 6.2, 8.0)):
        L = 3.9 - i * 0.15
        P.append(Part("rib%d_r" % i, "body", (-0.6, y, 2.5), (0, 0, 0.12 + i * 0.04),
                      [Box(-L, -0.35, -0.5, L, 0.8, 1.0, bone), Box(-L - 0.5, -0.35, -4.3, 0.8, 0.8, 4.1, bone)]))
    # ---- omóplatos
    P.append(Part("blade_r", "body", (-2.2, 1.6, 2.4), (0.15, 0.15, 0.2), [Box(-1.8, -0.4, -0.2, 2.6, 3.6, 0.8, bone)]))
    # ---- cadera
    P.append(Part("pelvis", "body", (0, 11.0, 0), (0, 0, 0), [Box(-4.3, -0.8, -2.4, 8.6, 1.6, 4.8, bone)]))
    # ---- cráneo de zorro sobre la cabeza: bóveda, pómulos, hocico largo con dientes, mandíbula y orejas
    P.append(Part("skull", "head", (0, 0, 0), (0, 0, 0), [
        Box(-4.7, -9.0, -4.7, 9.4, 3.2, 9.4, bone),
        Box(-4.75, -6.2, -4.75, 9.5, 2.2, 0.6, skull_face),
        Box(-4.75, -6.0, -4.2, 0.6, 3.4, 7.4, bone), Box(4.15, -6.0, -4.2, 0.6, 3.4, 7.4, bone)]))
    P.append(Part("snout", "head", (0, -4.0, -4.6), (0.08, 0, 0), [
        Box(-2.4, -1.6, -5.4, 4.8, 2.4, 5.6, teeth),
        Box(-1.0, -2.0, -5.0, 2.0, 0.6, 4.8, bone)]))
    P.append(Part("jaw", "head", (0, -1.2, -4.4), (0.25, 0, 0), [Box(-2.1, 0, -4.8, 4.2, 1.2, 5.0, teeth)]))
    P.append(Part("ear_r", "head", (-3.0, -8.8, 0.6), (-0.15, 0, -0.3), [Box(-1.2, -4.0, -0.8, 2.4, 4.2, 1.6, bone),
                                                                         Box(-0.6, -5.6, -0.4, 1.2, 1.8, 0.8, bone)]))
    P += mirror([p for p in P if p.name.endswith("_r")])
    return P


def skeleton_java(P, w, h):
    L = ["// GENERADO por tools/gen_assets.py — no editar a mano.", "package com.cz.client;", "",
         "import net.minecraft.client.model.geom.PartPose;", "import net.minecraft.client.model.geom.builders.*;", "",
         "import java.util.HashMap;", "import java.util.Map;", "",
         "public final class SkeletonModelData {", "    private SkeletonModelData() {}", "",
         "    public static LayerDefinition createLayer() {",
         "        MeshDefinition mesh = new MeshDefinition();",
         "        Map<String, PartDefinition> d = new HashMap<>();",
         '        d.put("body", mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO));',
         '        d.put("head", mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO));']
    for p in P:
        cubes = "CubeListBuilder.create()"
        for b in p.boxes:
            cubes += ".texOffs(%d, %d).addBox(%s, %s, %s, %s, %s, %s)" % (b.u, b.v, mg.fnum(b.x), mg.fnum(b.y), mg.fnum(b.z), mg.fnum(b.w), mg.fnum(b.h), mg.fnum(b.d))
        L.append('        d.put("%s", d.get("%s").addOrReplaceChild("%s", %s, PartPose.offsetAndRotation(%s, %s, %s, %s, %s, %s)));' % (
            p.name, p.parent, p.name, cubes, mg.fnum(p.pivot[0]), mg.fnum(p.pivot[1]), mg.fnum(p.pivot[2]),
            mg.fnum(p.rot[0]), mg.fnum(p.rot[1]), mg.fnum(p.rot[2])))
    L += ["        return LayerDefinition.create(mesh, %d, %d);" % (w, h), "    }", "}", ""]
    return "\n".join(L)


def main():
    cloak()
    beast(False)
    beast(True)
    bubble()
    P = skeleton()
    w, h = pack(P, 64)
    img = mg.build_texture(P, w, h)
    img.save(os.path.join(E, "bones.png"))
    open(JAVA, "w").write(skeleton_java(P, w, h))
    print("ok, esqueleto %d piezas, textura %dx%d" % (len(P), w, h))
    return P, img


if __name__ == "__main__":
    main()
