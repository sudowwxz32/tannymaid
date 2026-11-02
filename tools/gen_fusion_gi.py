"""Traje 3D de las fusiones: pantalón de karate saiyajin azul (zubon) holgado, obi azul con nudo y puntas,
guantes blancos, y el pecho y los hombros del mob con el que te fusionaste, con piezas 3D propias de cada
uno (costillas del esqueleto, enredaderas del gólem, zarcillos del warden, camisa rota del zombi...).

Genera:
  - client/FusionGiModels.java     geometría (brazos anchos y delgados)
  - textures/entity/fusion/gi_<mob>.png y gi_<mob>_glow.png  (misma distribución de UV para todos)
Uso: python3 tools/gen_fusion_gi.py
"""
import math, os, random
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX = os.path.join(ROOT, "src/main/resources/assets/blockdragonsuperz/textures/entity/fusion")
JAVA = os.path.join(ROOT, "src/main/java/com/bdsz/client/FusionGiModels.java")
S = 4
T = (0, 0, 0, 0)
R = random.Random(9)
MOB = "zombie"
GLOW = False

BLUE, BLUE_HI, BLUE_DK = (36, 70, 170), (60, 100, 210), (22, 44, 120)
OBI, OBI_DK = (26, 54, 150), (14, 32, 100)
WHITE, WHITE_SH = (240, 240, 236), (206, 206, 202)


def mix(a, b, t):
    t = max(0.0, min(1.0, t))
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))


def jit(c, a=4):
    return tuple(max(0, min(255, v + R.randint(-a, a))) for v in c)


class Box:
    def __init__(self, x, y, z, w, h, d, paint):
        self.x, self.y, self.z, self.w, self.h, self.d, self.paint = x, y, z, w, h, d, paint
        self.u = self.v = 0


class Part:
    def __init__(self, name, parent, pivot, rot, boxes):
        self.name, self.parent, self.pivot, self.rot, self.boxes = name, parent, pivot, rot, boxes


def pack(parts, width):
    rects = []
    for p in parts:
        for b in p.boxes:
            rects.append((math.ceil(b.d + b.h), math.ceil(2 * (b.d + b.w)), b))
    rects.sort(key=lambda r: (-r[0], -r[1]))
    x = y = shelf = 0
    for rh, rw, b in rects:
        if x + rw > width:
            x, y, shelf = 0, y + shelf, 0
        b.u, b.v = x, y
        x += rw + 1
        shelf = max(shelf, rh + 1)
    h = 32
    while h < y + shelf:
        h *= 2
    return width, h


def faces(u, v, w, h, d):
    return {"top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d), "right": (u, v + d, d, h),
            "front": (u + d, v + d, w, h), "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h)}


def paint_box(img, b):
    for f, (fx, fy, fw, fh) in faces(b.u, b.v, b.w, b.h, b.d).items():
        x0, y0 = int(round(fx * S)), int(round(fy * S))
        cw, ch = max(1, int(round((fx + fw) * S)) - x0), max(1, int(round((fy + fh) * S)) - y0)
        for py in range(y0, y0 + ch):
            for px in range(x0, x0 + cw):
                c = b.paint(f, (px - x0) / cw, (py - y0) / ch, cw, ch)
                if c is None or not (0 <= px < img.width and 0 <= py < img.height):
                    continue
                img.putpixel((px, py), c if len(c) == 4 else jit(c, 3) + (255,))


def solid_glow(c):
    """En la textura de brillo, solo pinta lo que debe brillar."""
    return c if GLOW else None


# ----------------------------------------------------------------- piezas comunes
def pants(f, u, v, w, h):
    if GLOW:
        return None
    if f == "top":
        return BLUE_DK
    fold = abs(((u * 6) % 1.0) - 0.5) * 2                    # pliegues verticales del zubon
    c = mix(BLUE_HI, BLUE_DK, fold * 0.7 + v * 0.25)
    if v > 0.9:
        c = BLUE_DK
    return c


def cuff(f, u, v, w, h):
    return None if GLOW else (BLUE_DK if v < 0.5 else mix(BLUE_DK, (10, 20, 60), 0.4))


def obi(f, u, v, w, h):
    if GLOW:
        return None
    if v < 0.15 or v > 0.85:
        return OBI_DK
    return OBI if int(u * 20) % 5 else OBI_DK


def glove(f, u, v, w, h):
    if GLOW:
        return None
    if v < 0.28 and f != "bottom":
        return WHITE_SH if v < 0.12 else WHITE               # puño del guante
    if f == "bottom":
        return WHITE_SH
    return mix(WHITE, WHITE_SH, v * 0.5)


# ----------------------------------------------------------------- pecho del mob
def chest(f, u, v, w, h):
    m = MOB
    if m == "zombie":
        if GLOW:
            return None
        G, GD = (78, 140, 60), (54, 104, 42)
        if f in ("front", "back") and v > 0.45 and (int(u * 12) * 7 + int(v * 14) * 3) % 9 < 3:
            return (0, 150, 150)                              # jirones de la camisa
        return G if (int(u * 16) + int(v * 20)) % 5 else GD
    if m == "skeleton":
        if GLOW:
            return None
        return (40, 38, 40) if (int(u * 10) + int(v * 12)) % 7 else (60, 58, 60)   # oscuro detrás de las costillas
    if m == "enderman":
        if GLOW:
            if f == "front" and (int(u * 24) * 13 + int(v * 30) * 7) % 37 == 0:
                return (210, 110, 255, 255)                   # chispas moradas
            return None
        return (22, 18, 26) if (int(u * 14) + int(v * 18)) % 6 else (36, 30, 44)
    if m == "creeper":
        if GLOW:
            return None
        CS = [(13, 138, 30), (78, 203, 62), (40, 170, 40), (150, 230, 140), (30, 110, 30)]
        if f == "front":
            x, y = u * 8, v * 8
            if ((1.5 <= x < 3 or 5 <= x < 6.5) and 1 <= y < 2.6) or (3 <= x < 5 and 2.6 <= y < 5.5) \
                    or ((2.2 <= x < 3 or 5 <= x < 5.8) and 4.2 <= y < 6):
                return (10, 10, 10)                           # cara de creeper en el pecho
        return CS[(int(u * 16) * 7 + int(v * 16) * 13 + int(u * 16) * int(v * 16)) % 5]
    if m == "golem":
        if GLOW:
            return None
        I, ID = (206, 198, 184), (150, 142, 130)
        if (int(u * 10) + int(v * 10) * 3) % 11 == 0:
            return ID
        if f == "front" and (abs(u - 0.15) < 0.04 or abs(u - 0.85) < 0.04) and int(v * 6) % 2 == 0:
            return (120, 112, 100)                            # remaches
        return I if int(v * 8) % 3 else ID
    if m == "dragon":
        D, DD, DL = (26, 22, 32), (14, 12, 18), (52, 40, 64)
        eye = f == "front" and 0.3 < u < 0.7 and 0.3 < v < 0.5 and (int(u * 20) + int(v * 20)) % 2 == 0
        if GLOW:
            if eye:
                return (220, 90, 255, 255)                    # brillo morado del dragón en el pecho
            return None
        if (int(u * 12) + int(v * 10) * 2) % 5 == 0:
            return DL                                         # escamas
        return D if (int(u * 8) + int(v * 8)) % 3 else DD
    if m == "warden":
        W, WD = (16, 58, 64), (8, 34, 40)
        soul = f == "front" and 0.25 < u < 0.75 and 0.2 < v < 0.75
        if GLOW:
            if soul and (int(u * 20) + int(v * 20)) % 3:
                return (60, 255, 240, 255)
            return None
        if soul:
            return (24, 80, 86)
        return W if (int(u * 10) + int(v * 12)) % 3 else WD
    return (128, 128, 128)


def shoulder(f, u, v, w, h):
    """Hombros / brazo superior con la piel del mob."""
    if GLOW:
        return chest(f, u, v, w, h) if MOB == "enderman" else None
    if MOB == "skeleton":
        return (226, 222, 210) if 0.35 < u < 0.65 or f in ("top",) else (60, 58, 60)
    return chest(f, u, v * 0.6 + 0.4, w, h)


# ----------------------------------------------------------------- piezas 3D de cada mob
def bone(f, u, v, w, h):
    return None if GLOW else ((226, 222, 210) if (int(u * 10) + int(v * 6)) % 7 else (180, 176, 164))


def vine(f, u, v, w, h):
    return None if GLOW else ((66, 140, 50) if int(v * 10) % 3 else (44, 104, 34))


def tendril(f, u, v, w, h):
    if GLOW:
        return (40, 230, 220, 255) if v < 0.5 else None
    return (16, 58, 64) if v >= 0.5 else (30, 120, 120)


def rag(f, u, v, w, h):
    return None if GLOW else ((0, 150, 150) if (int(u * 8) + int(v * 10)) % 4 else (0, 110, 110))


def plate(f, u, v, w, h):
    return None if GLOW else ((196, 188, 174) if int(v * 6) % 2 else (150, 142, 130))


def ender_spike(f, u, v, w, h):
    if GLOW:
        return (210, 110, 255, 255) if v > 0.7 else None
    return (22, 18, 26)


def horn(f, u, v, w, h):
    if GLOW:
        return None
    return (200, 196, 210) if v < 0.35 else (40, 36, 46)


def wing(f, u, v, w, h):
    """Ala de dragón: membrana negra-morada con huesos claros."""
    if GLOW:
        return (200, 80, 255, 255) if (f in ("front", "back") and int(u * 9) % 3 == 0 and v > 0.3 and int(v * 10) % 4 == 0) else None
    if f in ("front", "back") and (int(u * 9) % 3 == 0 or v < 0.08):
        return (70, 60, 84)                                   # huesos del ala
    return (30, 22, 40) if (int(u * 12) + int(v * 10)) % 4 else (44, 30, 58)


def dragon_spike(f, u, v, w, h):
    return None if GLOW else ((60, 52, 72) if v > 0.5 else (24, 20, 30))


def build(slim):
    aw = 3 if slim else 4                 # ancho del brazo
    ax = -2 if slim else -3               # borde del brazo derecho
    P = []
    # pecho del mob (un poco más ancho que el torso) y obi
    P.append(Part("chest", "body", (0, 0, 0), (0, 0, 0), [Box(-4.35, -0.3, -2.35, 8.7, 7.6, 4.7, chest)]))
    P.append(Part("obi", "body", (0, 0, 0), (0, 0, 0), [Box(-4.6, 7.0, -2.6, 9.2, 1.5, 5.2, obi)]))
    P.append(Part("obi_knot", "body", (-1.2, 7.8, -2.7), (0, 0, 0.2), [Box(-0.8, -0.8, -0.6, 1.6, 1.6, 0.9, obi)]))
    P.append(Part("obi_tail_a", "body", (-1.5, 8.4, -2.75), (-0.08, 0, 0.25), [Box(-0.55, 0, -0.25, 1.1, 3.6, 0.4, obi)]))
    P.append(Part("obi_tail_b", "body", (-0.6, 8.4, -2.75), (-0.05, 0, -0.1), [Box(-0.5, 0, -0.25, 1.0, 3.0, 0.4, obi)]))
    # pantalón holgado: cadera y piernas anchas con puño en el tobillo
    P.append(Part("hips", "body", (0, 0, 0), (0, 0, 0), [Box(-4.45, 8.3, -2.45, 8.9, 3.9, 4.9, pants)]))
    for side, leg in (("r", "right_leg"), ("l", "left_leg")):
        P.append(Part("pant_" + side, leg, (0, 0, 0), (0, 0, 0), [Box(-2.65, -0.3, -2.65, 5.3, 9.6, 5.3, pants)]))
        P.append(Part("ankle_" + side, leg, (0, 0, 0), (0, 0, 0), [Box(-2.25, 9.2, -2.25, 4.5, 1.3, 4.5, cuff)]))
    # brazos: hombro con la piel del mob y guantes blancos
    for side, arm, x0 in (("r", "right_arm", ax), ("l", "left_arm", -1)):
        P.append(Part("shoulder_" + side, arm, (0, 0, 0), (0, 0, 0), [Box(x0 - 0.3, -2.3, -2.3, aw + 0.6, 5.0, 4.6, shoulder)]))
        P.append(Part("glove_" + side, arm, (0, 0, 0), (0, 0, 0), [Box(x0 - 0.3, 6.3, -2.3, aw + 0.6, 3.9, 4.6, glove)]))
    # --- esqueleto: costillas y columna
    for i, y in enumerate((1.4, 2.8, 4.2, 5.6)):
        P.append(Part("skeleton_rib%d" % i, "body", (0, y, -2.45), (0, 0, 0), [Box(-3.6 + i * 0.15, -0.3, -0.3, 7.2 - i * 0.3, 0.6, 0.55, bone)]))
    P.append(Part("skeleton_sternum", "body", (0, 0.8, -2.6), (0, 0, 0), [Box(-0.45, 0, -0.3, 0.9, 5.6, 0.55, bone)]))
    P.append(Part("skeleton_spine", "body", (0, 0.3, 2.45), (0, 0, 0), [Box(-0.5, 0, -0.25, 1.0, 6.9, 0.6, bone)]))
    # --- gólem: placa de hierro en el pecho y enredaderas que cuelgan de los hombros
    P.append(Part("golem_plate", "body", (0, 1.0, -2.55), (0, 0, 0), [Box(-3.2, 0, -0.5, 6.4, 4.4, 0.6, plate)]))
    for i, (x, l) in enumerate(((-3.6, 6.0), (-1.2, 3.6), (2.4, 5.2), (3.8, 2.8))):
        P.append(Part("golem_vine%d" % i, "body", (x, -0.2, -2.5 if i % 2 == 0 else 2.5), (0.05, 0, 0.04 * (i - 1.5)),
                      [Box(-0.3, 0, -0.2, 0.6, l, 0.4, vine)]))
    # --- warden: zarcillos que brillan a los lados de la cabeza
    for side, s_ in (("r", -1), ("l", 1)):
        P.append(Part("warden_tendril_" + side, "head", (s_ * 3.6, -7.6, 0.4), (-0.2, 0, s_ * 0.85),
                      [Box(-0.6, -4.2, -0.6, 1.2, 4.4, 1.2, tendril)]))
    # --- zombi: jirones de camisa colgando
    P.append(Part("zombie_rag_a", "body", (-2.2, 6.6, -2.5), (0.12, 0, 0.18), [Box(-1.0, 0, -0.15, 2.0, 2.6, 0.3, rag)]))
    P.append(Part("zombie_rag_b", "body", (2.4, 6.4, 2.5), (-0.1, 0, -0.15), [Box(-1.1, 0, -0.15, 2.2, 2.2, 0.3, rag)]))
    # --- enderman: púas oscuras en los hombros con la punta morada
    for side, arm, x0 in (("r", "right_arm", ax), ("l", "left_arm", -1)):
        P.append(Part("enderman_spike_" + side, arm, (x0 + aw / 2, -2.4, 0), (0, 0, 0),
                      [Box(-0.5, -1.8, -0.5, 1.0, 1.9, 1.0, ender_spike)]))
    # --- dragón: cuernos, alas, púas en la espalda y cola
    for side, s_ in (("r", -1), ("l", 1)):
        P.append(Part("dragon_horn_" + side, "head", (s_ * 2.6, -8.0, 1.2), (-0.65, 0, s_ * 0.25),
                      [Box(-0.6, -3.4, -0.6, 1.2, 3.6, 1.2, horn)]))
        P.append(Part("dragon_wing_" + side, "body", (s_ * 1.6, 1.2, 2.5), (0.2, s_ * 0.45, s_ * 0.35),
                      [Box(-11.0 if s_ < 0 else 0.0, -6.0, 0.0, 11.0, 12.0, 0.4, wing)]))
    for i in range(4):
        P.append(Part("dragon_spike%d" % i, "body", (0, 0.8 + i * 2.0, 2.4), (0.35, 0, 0), [Box(-0.45, -0.45, 0, 0.9, 0.9, 1.5, dragon_spike)]))
    P.append(Part("dragon_tail", "body", (0, 10.0, 2.2), (0.75, 0, 0), [Box(-1.1, 0, -1.1, 2.2, 8.0, 2.2, chest)]))
    # --- creeper: capucha verde a la espalda
    P.append(Part("creeper_hood", "body", (0, 0.2, 2.4), (0.3, 0, 0), [Box(-3.6, -0.8, 0, 7.2, 3.0, 1.6, chest)]))
    return P


ANCHORS = [("head", (0, 0, 0)), ("body", (0, 0, 0)), ("right_arm", (-5, 2, 0)), ("left_arm", (5, 2, 0)),
           ("right_leg", (-1.9, 12, 0)), ("left_leg", (1.9, 12, 0))]


def fnum(v):
    s = ("%.4f" % v).rstrip("0").rstrip(".")
    return ("0" if s in ("-0", "") else s) + "F"


def java_layer(name, parts, w, h):
    L = ["    public static LayerDefinition %s() {" % name, "        MeshDefinition mesh = new MeshDefinition();",
         "        PartDefinition root = mesh.getRoot();"]
    var = {}
    for a, piv in ANCHORS:
        L.append('        PartDefinition p_%s = root.addOrReplaceChild("%s", CubeListBuilder.create(), PartPose.offset(%s, %s, %s));'
                 % (a, a, fnum(piv[0]), fnum(piv[1]), fnum(piv[2])))
        var[a] = "p_" + a
    for p in parts:
        cubes = "CubeListBuilder.create()"
        for b in p.boxes:
            cubes += ".texOffs(%d, %d).addBox(%s, %s, %s, %s, %s, %s)" % (b.u, b.v, fnum(b.x), fnum(b.y), fnum(b.z), fnum(b.w), fnum(b.h), fnum(b.d))
        L.append('        %s.addOrReplaceChild("%s", %s, PartPose.offsetAndRotation(%s, %s, %s, %s, %s, %s));' % (
            var[p.parent], p.name, cubes, fnum(p.pivot[0]), fnum(p.pivot[1]), fnum(p.pivot[2]),
            fnum(p.rot[0]), fnum(p.rot[1]), fnum(p.rot[2])))
    L += ["        return LayerDefinition.create(mesh, %d, %d);" % (w, h), "    }"]
    return L


def main():
    global MOB, GLOW
    os.makedirs(TEX, exist_ok=True)
    wide = build(False)
    slim = build(True)
    tw, th = pack(wide, 64)
    # el modelo delgado usa las mismas UV que el ancho (mismas cajas salvo el ancho de los brazos)
    for a, b in zip(wide, slim):
        for x, y in zip(a.boxes, b.boxes):
            y.u, y.v = x.u, x.v
    for mob in ("zombie", "skeleton", "enderman", "creeper", "golem", "warden", "dragon"):
        for glow in (False, True):
            MOB, GLOW = mob, glow
            R.seed(5)
            img = Image.new("RGBA", (tw * S, th * S), T)
            for p in wide:
                for b in p.boxes:
                    paint_box(img, b)
            img.save(os.path.join(TEX, "gi_%s%s.png" % (mob, "_glow" if glow else "")))
    names = sorted({p.name for p in wide})
    out = ["// GENERADO por tools/gen_fusion_gi.py — no editar a mano.", "package com.bdsz.client;", "",
           "import net.minecraft.client.model.geom.PartPose;", "import net.minecraft.client.model.geom.builders.*;", "",
           "public final class FusionGiModels {", "    private FusionGiModels() {}", "",
           "    /** Piezas (hijas de head, body, brazos o piernas). Las que empiezan con el nombre del mob solo se ven con esa fusión. */",
           "    public static final String[][] PARTS = {"]
    for p in wide:
        out.append('            {"%s", "%s"},' % (p.parent, p.name))
    out += ["    };", ""]
    out += java_layer("createWide", wide, tw, th) + [""]
    out += java_layer("createSlim", slim, tw, th) + ["}", ""]
    open(JAVA, "w").write("\n".join(out))
    print("traje de fusión %dx%d, %d piezas" % (tw, th, len(wide)))


if __name__ == "__main__":
    main()
