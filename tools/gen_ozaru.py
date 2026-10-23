"""Modelo del Ozaru (mono gigante): geometría Java + textura de pelaje.
Unidades de píxel del modelo; el suelo está en y = 24 y la cara mira hacia -z."""
import os, sys, random
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import modelgen_base as mg
from modelgen_base import Box, Part, pack, build_texture, mirror

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX = os.path.join(ROOT, "src/main/resources/assets/blockdragonsuperz/textures/entity/ozaru.png")
JAVA = os.path.join(ROOT, "src/main/java/com/bdsz/client/OzaruModelData.java")

R = random.Random(11)
FUR, FUR_HI, FUR_DK = (112, 70, 38), (138, 90, 52), (78, 46, 24)
SKIN, SKIN_SH = (222, 178, 128), (196, 150, 104)


def j(c, a=8):
    return tuple(max(0, min(255, v + R.randint(-a, a))) for v in c)


def fur(f, x, y, w, h):
    if f == "top":
        return j(FUR_HI, 6)
    if f == "bottom":
        return j(FUR_DK, 6)
    k = (x * 7 + y * 3) % 6
    if k == 0:
        return j(FUR_HI, 6)          # mechones claros
    if k == 3 or y % 4 == 3:
        return j(FUR_DK, 6)          # mechones oscuros
    return j(FUR, 8)


def furdark(f, x, y, w, h):
    return j(FUR_DK, 6)


def skin(f, x, y, w, h):
    if f in ("bottom",) or y == h - 1:
        return j(SKIN_SH, 5)
    return j(SKIN, 6)


def face(f, x, y, w, h):
    """Placa de la cara: ojos rojos brillantes."""
    if f == "front":
        if y in (1, 2) and (x in (1, 2) or x in (w - 3, w - 2)):
            return (255, 30, 20) if y == 2 else (255, 120, 90)
        if y == 0:
            return j(FUR_DK, 4)
    return skin(f, x, y, w, h)


def muzzle(f, x, y, w, h):
    if f == "front" and y == 0 and x in (2, w - 3):
        return (40, 24, 16)          # fosas nasales
    if f == "front" and y == h - 1:
        return (70, 30, 24)          # boca
    return skin(f, x, y, w, h)


def white(f, x, y, w, h):
    return (240, 236, 220)


def eyes(f, x, y, w, h):
    return (255, 40, 20)


LEATHER, LEATHER_SH = (92, 62, 48), (70, 46, 36)
CHEST, CHEST_DK = (126, 80, 44), (96, 58, 30)


def chestfur(f, x, y, w, h):
    """Pecho y vientre café (pelaje), con líneas de músculo marcadas."""
    if f == "front" and (x == w // 2 or y == h // 2):
        return j(CHEST_DK, 4)
    return j(CHEST, 7) if (x + y) % 4 else j(FUR, 5)


def leather(f, x, y, w, h):
    return j(LEATHER_SH if y == h - 1 or f == "bottom" else LEATHER, 5)


def nose(f, x, y, w, h):
    if f == "front" and y == h - 1 and x in (0, w - 1):
        return (20, 12, 10)
    return j((58, 36, 28), 4)


def ear(f, x, y, w, h):
    if f in ("left", "right") and 0 < x < w - 1 and 0 < y < h - 1:
        return j((176, 118, 92), 5)
    return j(FUR, 6)


def parts():
    P = []
    P.append(Part("body", "root", (0, 12, 2), (0.45, 0, 0), [
        Box(-5.5, -9, -3.5, 11, 9.5, 7, fur),
        Box(-4, -8.5, -3.9, 8, 8, 0.6, chestfur)]))
    P.append(Part("chest", "body", (0, -8, 0), (0.15, 0, 0), [
        Box(-7.5, -11, -4.5, 15, 11.5, 9, fur),
        Box(-5.5, -10, -5.0, 11, 7, 0.6, chestfur),
        Box(-6, -12.5, -2.5, 12, 2, 6, fur),
        Box(-6.5, -12, -5.2, 13, 2.6, 4, furdark)]))
    # ---- cabeza: cráneo grande, ceja gruesa, hocico largo, mandíbula y colmillos
    P.append(Part("head", "chest", (0, -10.5, -3), (-0.6, 0, 0), [
        Box(-5, -9, -5, 10, 9.5, 9, fur),
        Box(-4, -10.6, -3.5, 8, 1.8, 6.5, furdark),
        Box(-5.2, -7.6, -6.6, 10.4, 2.0, 1.8, furdark),
        Box(-4.2, -5.8, -5.7, 8.4, 3.2, 0.8, face),
        Box(-3.8, -3.8, -10.5, 7.6, 3.6, 5.2, muzzle),
        Box(-1.8, -4.2, -10.9, 3.6, 1.4, 1.0, nose)]))
    P.append(Part("eyes", "head", (0, 0, 0), (0, 0, 0), [
        Box(-3.3, -4.9, -5.85, 2.0, 1.0, 0.15, eyes), Box(1.3, -4.9, -5.85, 2.0, 1.0, 0.15, eyes)]))
    P.append(Part("cheek_r", "head", (-5.2, -4.5, -1.5), (0, 0, 0.15), [Box(-1.6, -2.5, -3.5, 1.6, 6, 6, fur)]))
    P.append(Part("ear_r", "head", (-5.2, -6.0, 0.8), (0, 0.3, -0.25), [Box(-2.6, -2.2, -1.3, 2.6, 3.8, 2.6, ear)]))
    P.append(Part("tuft", "head", (0, -10.2, -0.5), (-0.35, 0, 0), [Box(-1.3, -3.2, -1.3, 2.6, 3.4, 2.6, furdark)]))
    P.append(Part("tuft_r", "head", (-2.6, -9.9, 0.2), (-0.2, 0, -0.45), [Box(-1.1, -2.6, -1.1, 2.2, 2.8, 2.2, furdark)]))
    P.append(Part("fang_r", "head", (-2.6, -0.3, -9.6), (0, 0, 0), [Box(-0.45, 0, -0.4, 0.9, 1.9, 0.8, white)]))
    P.append(Part("jaw", "head", (0, 0.0, -5.2), (0, 0, 0), [
        Box(-3.4, -0.2, -4.9, 6.8, 2.0, 5.0, skin),
        Box(-2.9, -1.3, -4.6, 0.8, 1.2, 0.7, white), Box(2.1, -1.3, -4.6, 0.8, 1.2, 0.7, white)]))
    # ---- brazos largos de gorila
    P.append(Part("arm_r", "chest", (-8.6, -9, 0), (-0.6, 0, 0.12), [Box(-2.9, -1.5, -2.9, 5.8, 11, 5.8, fur)]))
    P.append(Part("fore_r", "arm_r", (0, 9.2, 0), (0, 0, 0), [Box(-2.7, 0, -2.7, 5.4, 10, 5.4, fur)]))
    P.append(Part("hand_r", "fore_r", (0, 9.6, 0), (0, 0, 0), [Box(-3.0, 0, -3.3, 6, 3, 6.6, leather)]))
    # ---- piernas cortas y gruesas
    P.append(Part("leg_r", "root", (-3.6, 12, 2), (0, 0, 0), [Box(-2.9, -1, -3, 5.8, 7.5, 6, fur)]))
    P.append(Part("shin_r", "leg_r", (0, 6.0, 0), (0, 0, 0), [Box(-2.6, 0, -2.6, 5.2, 4.6, 5.2, fur)]))
    P.append(Part("foot_r", "shin_r", (0, 4.2, 0), (0, 0, 0), [Box(-2.9, 0, -4.6, 5.8, 1.8, 6.8, leather)]))
    # ---- cola peluda
    parent = "body"
    for i in range(6):
        s = 2.4 - i * 0.22
        P.append(Part("tail%d" % i, parent, (0, -1.0, 3.2) if i == 0 else (0, 0, 3.6), (0.35, 0, 0) if i == 0 else (0.3, 0, 0),
                      [Box(-s / 2, -s / 2, 0, s, s, 4.0, fur)]))
        parent = "tail%d" % i
    P += mirror([p for p in P if "_r" in p.name])
    return P


def java_file(P, w, h):
    L = ["// GENERADO por tools/gen_ozaru.py — no editar a mano.", "package com.bdsz.client;", "",
         "import net.minecraft.client.model.geom.ModelPart;", "import net.minecraft.client.model.geom.PartPose;",
         "import net.minecraft.client.model.geom.builders.*;", "", "import java.util.HashMap;", "import java.util.Map;", "",
         "public final class OzaruModelData {", "    private OzaruModelData() {}", "",
         "    public static LayerDefinition createLayer() {",
         "        MeshDefinition mesh = new MeshDefinition();",
         "        Map<String, PartDefinition> d = new HashMap<>();",
         '        d.put("root", mesh.getRoot());']
    for p in P:
        cubes = "CubeListBuilder.create()"
        for b in p.boxes:
            cubes += ".texOffs(%d, %d).addBox(%s, %s, %s, %s, %s, %s)" % (
                b.u, b.v, mg.fnum(b.x), mg.fnum(b.y), mg.fnum(b.z), mg.fnum(b.w), mg.fnum(b.h), mg.fnum(b.d))
        L.append('        d.put("%s", d.get("%s").addOrReplaceChild("%s", %s, PartPose.offsetAndRotation(%s, %s, %s, %s, %s, %s)));' % (
            p.name, p.parent, p.name, cubes, mg.fnum(p.pivot[0]), mg.fnum(p.pivot[1]), mg.fnum(p.pivot[2]),
            mg.fnum(p.rot[0]), mg.fnum(p.rot[1]), mg.fnum(p.rot[2])))
    L += ["        return LayerDefinition.create(mesh, %d, %d);" % (w, h), "    }", "",
          "    public static Map<String, ModelPart> bake(ModelPart root) {",
          "        Map<String, ModelPart> m = new HashMap<>();", '        m.put("root", root);']
    for p in P:
        L.append('        m.put("%s", m.get("%s").getChild("%s"));' % (p.name, p.parent, p.name))
    L += ["        return m;", "    }", "}", ""]
    return "\n".join(L)


def main():
    P = parts()
    w, h = pack(P, 128)
    img = build_texture(P, w, h)
    img.save(TEX)
    open(JAVA, "w").write(java_file(P, w, h))
    print("ozaru %dx%d, %d partes" % (w, h, len(P)))
    return P, img


if __name__ == "__main__":
    main()
