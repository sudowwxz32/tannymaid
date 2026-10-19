"""
Generador de modelos y texturas de Laury.

Describe cada pieza 3D una sola vez y produce:
  - src/main/java/com/laury/laurymod/client/LauryGeneratedModels.java  (geometría Forge)
  - texturas PNG alineadas con esa geometría
  - la skin base (64x64, brazos delgados) con 6 expresiones
Uso:  python3 tools/gen_models.py           (desde la raíz del proyecto)
      python3 tools/gen_models.py preview DIR   (además dibuja vistas previas)
"""
import math, os, random, sys
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX = os.path.join(ROOT, "src/main/resources/assets/laury/textures/entity")
JAVA = os.path.join(ROOT, "src/main/java/com/laury/laurymod/client/LauryGeneratedModels.java")
T = (0, 0, 0, 0)

# ----------------------------------------------------------------- paleta
SKIN, SKIN_SH, SKIN_HI = (241, 208, 186), (224, 188, 164), (249, 222, 204)
HAIR, HAIR_HI, HAIR_DK = (26, 36, 84), (44, 62, 128), (14, 20, 50)
HORN, HORN_HI, HORN_DK = (30, 46, 124), (56, 84, 172), (16, 24, 66)
GREEN, GREEN_HI, GREEN_DK = (60, 124, 50), (82, 150, 64), (40, 92, 34)
BLACK, BLACK_HI = (30, 30, 35), (46, 46, 54)
STOCK, STOCK_HI = (24, 24, 29), (38, 38, 45)
SHOE, SOLE = (44, 44, 52), (18, 18, 20)
EYE, EYE_DK, EYE_HI = (46, 178, 86), (22, 112, 52), (226, 255, 232)
LASH, BLUSH, LIP = (20, 24, 46), (238, 160, 160), (214, 120, 124)
BONE, BONE_HI = (32, 52, 142), (56, 84, 186)
MEM, MEM_DK = (62, 130, 56), (42, 98, 40)
SCALE, SCALE_HI, SCALE_DK = (32, 54, 140), (52, 82, 182), (18, 30, 88)
BELLY, BELLY_SH = (78, 132, 196), (60, 108, 170)
DEYE = (96, 240, 132)
CLAW = (220, 222, 232)

rnd = random.Random(5)


def jit(c, a=5):
    if len(c) == 4:
        return c
    return tuple(max(0, min(255, v + rnd.randint(-a, a))) for v in c)


# ----------------------------------------------------------------- pintores
def solid(c, a=5):
    return lambda f, x, y, w, h: jit(c, a)


def scales(base, hi, dk):
    def fn(f, x, y, w, h):
        if (x + 2 * y) % 5 == 0:
            return jit(hi, 4)
        if (x * 3 + y) % 7 == 0:
            return jit(dk, 4)
        return jit(base, 5)
    return fn


def dragon_body(f, x, y, w, h):
    if f == "bottom":
        return jit(BELLY_SH if (x + y) % 3 == 0 else BELLY, 4)
    if f == "top":
        if x in (w // 2 - 1, w // 2):
            return jit(SCALE_DK, 3)          # cresta
        return scales(SCALE, SCALE_HI, SCALE_DK)(f, x, y, w, h)
    if f in ("left", "right", "front", "back") and y >= h - 2:
        return jit(BELLY, 4)
    return scales(SCALE, SCALE_HI, SCALE_DK)(f, x, y, w, h)


def tail_seg(f, x, y, w, h):
    if f == "bottom":
        return jit(MEM if (x + y) % 2 else MEM_DK, 4)   # vientre verde
    if f in ("left", "right") and y >= h - 1:
        return jit(MEM, 4)
    return scales(SCALE, SCALE_HI, SCALE_DK)(f, x, y, w, h)


def membrane_xy(f, x, y, w, h):
    if f in ("front", "back"):
        if x % 5 == 0 or (x + y) % 9 == 0:
            return jit(MEM_DK, 3)            # venas
        return jit(MEM, 5)
    return jit(BONE, 4)


def membrane(f, x, y, w, h):
    if f in ("top", "bottom"):
        if x % 6 == 0 or y % 7 == 0:
            return jit(MEM_DK, 3)            # venas
        return jit(MEM, 5)
    return jit(BONE, 4)


def hair_fn(f, x, y, w, h):
    c = HAIR
    if (x * 5 + y) % 9 == 0:
        c = HAIR_HI
    if y == h - 1 and x % 2 == 0:
        c = HAIR_DK
    return jit(c, 4)


def horn_fn(f, x, y, w, h):
    if (x + y) % 3 == 0:
        return jit(HORN_HI, 4)
    return jit(HORN if y % 2 else HORN_DK, 4)


def hoodie(f, x, y, w, h):
    return jit(GREEN_HI if (x + y) % 6 == 0 else GREEN, 5)


def dragon_head(f, x, y, w, h):
    if f == "front" and y == 2 and x in (1, w - 2):
        return DEYE                          # ojos verdes brillantes
    if f in ("left", "right") and y == 2 and x in (1, 2):
        return DEYE
    if f == "bottom":
        return jit(BELLY, 4)
    return scales(SCALE, SCALE_HI, SCALE_DK)(f, x, y, w, h)


def snout(f, x, y, w, h):
    if f == "front" and y == 0 and x in (1, w - 2):
        return (10, 14, 30)                  # fosas nasales
    if f == "bottom":
        return jit(BELLY, 4)
    return scales(SCALE, SCALE_HI, SCALE_DK)(f, x, y, w, h)


def jaw(f, x, y, w, h):
    if f == "top":
        if x % 2 == 0 and (x < 2 or x > w - 3 or y < 1):
            return CLAW                      # dientes
        return (150, 40, 60)                 # boca
    return jit(BELLY if f == "bottom" else SCALE, 4)


def foot(f, x, y, w, h):
    if f == "front" and y == h - 1 and x % 2 == 0:
        return CLAW
    if f == "bottom":
        return jit(SCALE_DK, 3)
    return scales(SCALE, SCALE_HI, SCALE_DK)(f, x, y, w, h)


def boot(f, x, y, w, h):
    if f == "bottom" or (f != "top" and y == h - 1):
        return jit(SOLE, 2)
    if f == "front" and y == 0 and x % 2 == 1:
        return (70, 70, 80)                  # cordones
    return jit(SHOE, 4)


def shorts_fn(f, x, y, w, h):
    if f != "top" and f != "bottom" and y == 0:
        return jit(GREEN_DK, 3)              # bajo de la sudadera
    return jit(BLACK_HI if (x + y) % 4 == 0 else BLACK, 4)


def hem_fn(f, x, y, w, h):
    if f != "top" and f != "bottom" and y == h - 1:
        return GREEN_DK
    return GREEN if (x + y) % 5 else GREEN_HI


def ear_fn(f, x, y, w, h):
    return jit(SKIN_SH if f in ("bottom", "back") else SKIN, 3)


def string_fn(f, x, y, w, h):
    return jit((214, 226, 200) if y < h - 1 else (180, 190, 170), 3)


# ----------------------------------------------------------------- estructura
class Box:
    def __init__(self, x, y, z, w, h, d, paint):
        self.x, self.y, self.z, self.w, self.h, self.d, self.paint = x, y, z, w, h, d, paint
        self.u = self.v = 0


class Part:
    def __init__(self, name, parent, pivot=(0, 0, 0), rot=(0, 0, 0), boxes=()):
        self.name, self.parent, self.pivot, self.rot, self.boxes = name, parent, pivot, rot, list(boxes)


def mirror(parts, src_suffix="_r", dst_suffix="_l"):
    out = []
    for p in parts:
        name = p.name.replace(src_suffix, dst_suffix)
        parent = p.parent.replace(src_suffix, dst_suffix)
        px, py, pz = p.pivot
        rx, ry, rz = p.rot
        boxes = [Box(-(b.x + b.w), b.y, b.z, b.w, b.h, b.d, b.paint) for b in p.boxes]
        out.append(Part(name, parent, (-px, py, pz), (rx, -ry, -rz), boxes))
    return out


def pack(parts, width):
    rects = []
    for p in parts:
        for b in p.boxes:
            rw = math.ceil(2 * (b.d + b.w))
            rh = math.ceil(b.d + b.h)
            rects.append((rh, rw, b))
    rects.sort(key=lambda r: (-r[0], -r[1]))
    x = y = shelf = 0
    for rh, rw, b in rects:
        if x + rw > width:
            x, y, shelf = 0, y + shelf, 0
        b.u, b.v = x, y
        x += rw + 1
        shelf = max(shelf, rh + 1)
    height = y + shelf
    h = 32
    while h < height:
        h *= 2
    return width, h


def face_rects(b):
    u, v, w, h, d = b.u, b.v, b.w, b.h, b.d
    return {
        "top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h), "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h),
    }


def paint_box(img, b):
    for f, (fx, fy, fw, fh) in face_rects(b).items():
        if fw <= 0 or fh <= 0:
            continue
        cw, ch = max(1, math.ceil(fw)), max(1, math.ceil(fh))
        for py in range(math.floor(fy), math.ceil(fy + fh)):
            for px in range(math.floor(fx), math.ceil(fx + fw)):
                cx, cy = px + 0.5, py + 0.5
                if not (fx <= cx < fx + fw or fw < 1) or not (fy <= cy < fy + fh or fh < 1):
                    continue
                lx = min(cw - 1, int(cx - fx)) if fw >= 1 else 0
                ly = min(ch - 1, int(cy - fy)) if fh >= 1 else 0
                c = b.paint(f, lx, ly, cw, ch)
                if c is None:
                    continue
                if 0 <= px < img.width and 0 <= py < img.height:
                    img.putpixel((px, py), (c if len(c) == 4 else c + (255,)))


def fnum(v):
    s = ("%.3f" % v).rstrip("0").rstrip(".")
    if s in ("-0", ""):
        s = "0"
    return s + "F"


def java_layer(method, parts, anchors, tw, th):
    lines = ["    public static LayerDefinition %s() {" % method,
             "        MeshDefinition mesh = new MeshDefinition();",
             "        PartDefinition root = mesh.getRoot();"]
    var = {"root": "root"}
    for a, pivot in anchors:
        vn = "p_" + a
        lines.append('        PartDefinition %s = root.addOrReplaceChild("%s", CubeListBuilder.create(), '
                     'PartPose.offset(%s, %s, %s));' % (vn, a, fnum(pivot[0]), fnum(pivot[1]), fnum(pivot[2])))
        var[a] = vn
    for p in parts:
        vn = "p_" + p.name
        cubes = "CubeListBuilder.create()"
        for b in p.boxes:
            cubes += ".texOffs(%d, %d).addBox(%s, %s, %s, %s, %s, %s)" % (
                b.u, b.v, fnum(b.x), fnum(b.y), fnum(b.z), fnum(b.w), fnum(b.h), fnum(b.d))
        lines.append('        PartDefinition %s = %s.addOrReplaceChild("%s", %s, PartPose.offsetAndRotation(%s, %s, %s, %s, %s, %s));' % (
            vn, var[p.parent], p.name, cubes, fnum(p.pivot[0]), fnum(p.pivot[1]), fnum(p.pivot[2]),
            fnum(p.rot[0]), fnum(p.rot[1]), fnum(p.rot[2])))
        var[p.name] = vn
    lines.append("        return LayerDefinition.create(mesh, %d, %d);" % (tw, th))
    lines.append("    }")
    return lines


def java_bake(method, parts, anchors):
    lines = ["    public static Map<String, ModelPart> %s(ModelPart root) {" % method,
             "        Map<String, ModelPart> m = new HashMap<>();",
             '        m.put("root", root);']
    path = {}
    for a, _ in anchors:
        path[a] = 'root.getChild("%s")' % a
        lines.append('        m.put("%s", %s);' % (a, path[a]))
    for p in parts:
        parent_expr = 'm.get("%s")' % p.parent
        lines.append('        m.put("%s", %s.getChild("%s"));' % (p.name, parent_expr, p.name))
    lines.append("        return m;")
    lines.append("    }")
    return lines


# ================================================================= LAURY HUMANA
def features():
    P = []
    # --- cabello corto y desordenado
    P.append(Part("hair_top", "head", (0, 0, 0), (0, 0, 0), [Box(-4.6, -8.7, -4.6, 9.2, 2.5, 9.2, hair_fn)]))
    P.append(Part("hair_back", "head", (0, -6.6, 4.0), (0.12, 0, 0), [Box(-4.7, 0, -1.1, 9.4, 6.4, 1.7, hair_fn)]))
    P.append(Part("hair_side_r", "head", (-4.15, -7.0, 0), (0, 0, 0.05), [Box(-0.8, 0, -4.2, 1.35, 5.4, 8.3, hair_fn)]))
    for i, (x, l, zr) in enumerate([(-3.3, 3.4, 0.1), (-1.7, 2.5, 0.04), (0.0, 3.0, -0.02), (1.7, 2.3, -0.05), (3.3, 3.3, -0.1)]):
        P.append(Part("bang_%d" % i, "head", (x, -8.0, -4.35), (-0.14, 0, zr), [Box(-0.95, 0, -0.5, 1.9, l, 0.8, hair_fn)]))
    P.append(Part("spike_0", "head", (-1.5, -8.5, -1.5), (-0.5, 0.3, -0.5), [Box(-0.6, -1.8, -0.6, 1.2, 1.8, 1.6, hair_fn)]))
    P.append(Part("spike_1", "head", (1.8, -8.5, 1.0), (0.4, -0.2, 0.55), [Box(-0.6, -1.5, -0.6, 1.2, 1.5, 1.4, hair_fn)]))
    P.append(Part("spike_2", "head", (0.2, -8.4, 3.4), (0.7, 0, 0.1), [Box(-0.7, -1.4, -0.6, 1.4, 1.4, 1.2, hair_fn)]))
    # --- orejas puntiagudas
    P.append(Part("ear_r", "head", (-4.1, -3.9, 0.6), (0, 0.35, 0.38), [Box(-2.3, -1.0, -0.5, 2.5, 2.0, 1.0, ear_fn)]))
    P.append(Part("ear_r_tip", "ear_r", (-2.2, -0.1, 0), (0, 0, 0.4), [Box(-1.5, -0.55, -0.4, 1.7, 1.1, 0.8, ear_fn)]))
    # --- cuernos azul oscuro
    P.append(Part("horn_r", "head", (-2.5, -7.9, -0.4), (-0.25, 0, -0.42), [Box(-1.05, -3.0, -1.05, 2.1, 3.6, 2.1, horn_fn)]))
    P.append(Part("horn_r2", "horn_r", (0, -2.7, 0), (-0.3, 0, 0.18), [Box(-0.8, -2.8, -0.8, 1.6, 3.2, 1.6, horn_fn)]))
    P.append(Part("horn_r3", "horn_r2", (0, -2.6, 0), (-0.4, 0, 0.3), [Box(-0.5, -2.3, -0.5, 1.0, 2.6, 1.0, horn_fn)]))
    # --- sudadera: busto, capucha, cordones, bajo con vuelo
    P.append(Part("bust", "body", (0, 3.0, -2.0), (-0.32, 0, 0), [Box(-3.6, 0, -1.1, 7.2, 3.2, 1.2, hoodie)]))
    P.append(Part("hood", "body", (0, 0.3, 2.0), (0.28, 0, 0), [Box(-3.8, -0.8, 0, 7.6, 3.4, 1.8, hoodie)]))
    P.append(Part("string_r", "body", (-1.2, 1.2, -2.2), (0, 0, 0), [Box(-0.25, 0, -0.25, 0.5, 3.4, 0.5, string_fn)]))
    P.append(Part("hem", "body", (0, 8.4, 0), (0, 0, 0), [Box(-4.45, 0, -2.4, 8.9, 1.8, 4.8, hem_fn)]))
    # --- caderas (shorts)
    P.append(Part("hips", "body", (0, 10.0, 0), (0, 0, 0), [Box(-4.7, 0, -2.5, 9.4, 2.7, 5.0, shorts_fn)]))
    # --- alas de dragón (hueso azul, membrana verde)
    P.append(Part("wing_r", "body", (-1.6, 2.4, 2.1), (0, 0.5, 0.55),
                  [Box(-7.0, -0.7, -0.7, 7.0, 1.4, 1.4, solid(BONE)), Box(-6.6, 0.6, -0.15, 6.6, 6.0, 0.3, membrane_xy)]))
    P.append(Part("wing_r2", "wing_r", (-7.0, 0, 0), (0, 0, -1.55),
                  [Box(-9.0, -0.6, -0.6, 9.0, 1.2, 1.2, solid(BONE)), Box(-8.6, 0.5, -0.15, 8.6, 6.5, 0.3, membrane_xy)]))
    # --- cola de dragón
    P.append(Part("tail1", "body", (0, 10.6, 1.6), (-0.95, 0, 0),
                  [Box(-1.6, -1.6, 0, 3.2, 3.2, 5.0, tail_seg), Box(-0.3, -2.6, 1.2, 0.6, 1.0, 2.2, solid(SCALE_DK))]))
    sizes = [(2.7, 5.0, 0.28), (2.2, 5.0, 0.26), (1.7, 4.6, 0.22), (1.2, 4.0, 0.18)]
    for i, (s, l, r) in enumerate(sizes):
        boxes = [Box(-s / 2, -s / 2, 0, s, s, l, tail_seg)]
        if i < 2:
            boxes.append(Box(-0.25, -s / 2 - 0.9, 1.0, 0.5, 0.9, 2.0, solid(SCALE_DK)))
        P.append(Part("tail%d" % (i + 2), "tail%d" % (i + 1), (0, 0, 4.7 if i < 3 else 4.3), (r, 0, 0), boxes))
    P.append(Part("tail_tip", "tail5", (0, 0, 3.8), (0, 0, 0), [Box(-1.5, -0.3, 0, 3.0, 0.6, 2.6, solid(BONE))]))
    # --- puños de la sudadera y botas
    P.append(Part("cuff_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-2.3, 7.4, -2.3, 3.6, 1.3, 4.6, hem_fn)]))
    P.append(Part("cuff_l", "left_arm", (0, 0, 0), (0, 0, 0), [Box(-1.3, 7.4, -2.3, 3.6, 1.3, 4.6, hem_fn)]))
    P.append(Part("boot_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.3, 9.5, -2.7, 4.6, 2.7, 5.1, boot)]))
    P.append(Part("boot_l", "left_leg", (0, 0, 0), (0, 0, 0), [Box(-2.3, 9.5, -2.7, 4.6, 2.7, 5.1, boot)]))

    mirrorable = [p for p in P if p.name.startswith(("hair_side_r", "ear_r", "horn_r", "string_r", "wing_r"))]
    P += mirror(mirrorable)
    anchors = [("head", (0, 0, 0)), ("body", (0, 0, 0)), ("right_arm", (-5, 2, 0)), ("left_arm", (5, 2, 0)),
               ("right_leg", (-1.9, 12, 0)), ("left_leg", (1.9, 12, 0))]
    return P, anchors


# ================================================================= LAURY DRAGÓN
def dragon():
    P = []
    P.append(Part("dbody", "root", (0, 10, 0), (0, 0, 0), [Box(-6, -5, -9, 12, 10, 18, dragon_body)]))
    for i in range(4):
        P.append(Part("dspike_%d" % i, "dbody", (0, -5, -6 + i * 4.2), (0.3, 0, 0),
                      [Box(-0.6, -2.4, -1, 1.2, 2.4, 2.2, solid(SCALE_DK))]))
    P.append(Part("neck1", "dbody", (0, -1.5, -8), (-0.75, 0, 0), [Box(-3, -3, -7, 6, 6, 8, dragon_body),
                                                                  Box(-0.5, -4.6, -5, 1, 1.8, 2.5, solid(SCALE_DK))]))
    P.append(Part("neck2", "neck1", (0, 0, -6.5), (-0.25, 0, 0), [Box(-2.6, -2.6, -7, 5.2, 5.2, 8, dragon_body),
                                                                  Box(-0.5, -4.0, -5, 1, 1.6, 2.5, solid(SCALE_DK))]))
    P.append(Part("dhead", "neck2", (0, 0, -6.5), (1.0, 0, 0), [Box(-4, -4, -6, 8, 7, 7, dragon_head),
                                                              Box(-2.6, -1.5, -12, 5.2, 4, 6.5, snout)]))
    P.append(Part("djaw", "dhead", (0, 2.6, -5.5), (0.05, 0, 0), [Box(-2.4, 0, -6.8, 4.8, 1.6, 7, jaw)]))
    P.append(Part("dhorn_r", "dhead", (-2.6, -3.4, -1), (0.55, -0.3, 0), [Box(-0.9, -0.9, 0, 1.8, 1.8, 6, horn_fn)]))
    P.append(Part("dhorn_r2", "dhorn_r", (0, 0, 5.6), (0.5, 0, 0), [Box(-0.6, -0.6, 0, 1.2, 1.2, 4.5, horn_fn)]))
    P.append(Part("dleg_fr", "dbody", (-4.6, 3, -6), (0, 0, 0), [Box(-1.6, 0, -1.6, 3.2, 8, 3.2, scales(SCALE, SCALE_HI, SCALE_DK))]))
    P.append(Part("dfoot_fr", "dleg_fr", (0, 8, 0), (0, 0, 0), [Box(-2, 0, -3.6, 4, 3, 5, foot)]))
    P.append(Part("dleg_br", "dbody", (-5.2, 2, 6), (0, 0, 0), [Box(-2, 0, -2.6, 4, 8, 5.2, scales(SCALE, SCALE_HI, SCALE_DK))]))
    P.append(Part("dfoot_br", "dleg_br", (0, 8, 0), (0, 0, 0), [Box(-2.1, 0, -3.8, 4.2, 4, 5.2, foot)]))
    P.append(Part("dwing_r", "dbody", (-5.5, -4.5, -3), (0, 0, 0.1),
                  [Box(-13, -1, -1, 13, 2, 2, solid(BONE)), Box(-13, -0.25, 1, 13, 0.5, 12, membrane)]))
    P.append(Part("dwing_r2", "dwing_r", (-13, 0, 0), (0, 0, 0),
                  [Box(-15, -0.8, -0.8, 15, 1.6, 1.6, solid(BONE)), Box(-15, -0.25, 0.8, 15, 0.5, 15, membrane),
                   Box(-16.5, -0.6, -0.6, 1.5, 1.2, 1.2, solid(CLAW))]))
    P.append(Part("dtail1", "dbody", (0, -1.5, 8.5), (-0.25, 0, 0), [Box(-3, -3, 0, 6, 6, 9, tail_seg),
                                                                     Box(-0.5, -4.6, 3, 1, 1.8, 3, solid(SCALE_DK))]))
    tail = [(4.8, 9, 0.05), (3.8, 9, 0.06), (2.8, 8, 0.08), (1.8, 7, 0.1)]
    for i, (s, l, r) in enumerate(tail):
        boxes = [Box(-s / 2, -s / 2, 0, s, s, l, tail_seg)]
        if i < 2:
            boxes.append(Box(-0.5, -s / 2 - 1.5, 3, 1, 1.5, 2.5, solid(SCALE_DK)))
        P.append(Part("dtail%d" % (i + 2), "dtail%d" % (i + 1), (0, 0, 8.5 if i < 2 else (7.6 if i == 2 else 7.2)), (r, 0, 0), boxes))
    P.append(Part("dtail_tip", "dtail5", (0, 0, 6.6), (0, 0, 0), [Box(-3, -0.5, 0, 6, 1, 5, membrane)]))
    mirrorable = [p for p in P if p.name.startswith(("dhorn_r", "dleg_fr", "dfoot_fr", "dleg_br", "dfoot_br", "dwing_r"))]
    P += mirror(mirrorable)
    return P, []


# ================================================================= SKIN BASE
def skin(expr):
    rnd.seed(9)
    img = Image.new("RGBA", (64, 64), T)

    def region(u, v, w, h, d, fn):
        b = Box(0, 0, 0, w, h, d, fn)
        b.u, b.v = u, v
        paint_box(img, b)

    def head(f, x, y, w, h):
        if f == "front":
            return SKIN if y >= 2 else HAIR
        if f == "bottom":
            return SKIN_SH
        if f in ("left", "right"):
            return HAIR if y < 6 else SKIN_SH
        return hair_fn(f, x, y, w, h)

    def body(f, x, y, w, h):
        if f in ("top",):
            return GREEN
        if y >= 10:
            return shorts_fn(f, x, y - 10, w, h)
        if f == "front":
            if y == 0 and 2 <= x <= 5:
                return SKIN                          # cuello
            if 6 <= y <= 8 and 1 <= x <= 6:
                return GREEN_DK if y == 6 or x in (1, 6) else GREEN   # bolsillo canguro
        if f == "back" and y <= 2:
            return GREEN_DK                          # sombra de la capucha
        return hoodie(f, x, y, w, h)

    def arm(f, x, y, w, h):
        if f == "top":
            return GREEN
        if f == "bottom":
            return SKIN_SH
        return SKIN if y >= 10 else hoodie(f, x, y, w, h)

    def leg(f, x, y, w, h):
        if f == "top":
            return BLACK
        if f == "bottom":
            return SOLE
        if y <= 2:
            return BLACK
        if y == 3:
            return SKIN
        if y == 4:
            return STOCK_HI                          # borde de la media
        if y <= 9:
            return jit(STOCK, 3)
        return SHOE

    region(0, 0, 8, 8, 8, head)
    region(16, 16, 8, 12, 4, body)
    region(40, 16, 3, 12, 4, arm)
    region(32, 48, 3, 12, 4, arm)
    region(0, 16, 4, 12, 4, leg)
    region(16, 48, 4, 12, 4, leg)

    # cara
    P = lambda x, y, c: img.putpixel((8 + x, 8 + y), c + (255,))
    for yy in range(2, 8):
        for xx in range(8):
            P(xx, yy, SKIN if yy < 7 else SKIN_SH)
    P(1, 2, HAIR); P(6, 2, HAIR)
    if expr in ("normal", "surprised"):
        for a, b, hi in ((1, 2, 2), (5, 6, 5)):
            P(a, 3, LASH); P(b, 3, LASH)
            P(a, 4, EYE if hi != a else EYE_HI); P(b, 4, EYE if hi != b else EYE_HI)
            P(a, 5, EYE_DK); P(b, 5, EYE_DK)
        if expr == "surprised":
            P(1, 2, LASH); P(6, 2, LASH)
    elif expr == "angry":
        P(1, 3, LASH); P(2, 3, SKIN); P(5, 3, SKIN); P(6, 3, LASH)
        P(1, 4, LASH); P(2, 4, LASH); P(5, 4, LASH); P(6, 4, LASH)
        P(1, 5, EYE_DK); P(2, 5, EYE); P(5, 5, EYE); P(6, 5, EYE_DK)
    elif expr == "happy":
        P(1, 5, LASH); P(2, 4, LASH); P(5, 4, LASH); P(6, 5, LASH)
    else:  # blink / tired
        for xx in (1, 2, 5, 6):
            P(xx, 5, LASH)
    P(1, 6, BLUSH); P(6, 6, BLUSH)
    if expr == "surprised":
        P(3, 7, (120, 50, 60)); P(4, 7, (120, 50, 60))
    elif expr == "angry":
        P(3, 7, LIP); P(4, 7, LIP); P(2, 7, SKIN_SH)
    elif expr == "tired":
        P(3, 7, LIP)
    else:
        P(2, 6, SKIN_SH); P(3, 7, LIP); P(4, 7, LIP); P(5, 6, SKIN_SH)
    return img


# ================================================================= salida
def build_texture(parts, w, h):
    rnd.seed(3)
    img = Image.new("RGBA", (w, h), T)
    for p in parts:
        for b in p.boxes:
            paint_box(img, b)
    return img


def main():
    os.makedirs(TEX, exist_ok=True)
    fparts, fanchors = features()
    fw, fh = pack(fparts, 64)
    dparts, danchors = dragon()
    dw, dh = pack(dparts, 128)
    build_texture(fparts, fw, fh).save(os.path.join(TEX, "laury_features.png"))
    build_texture(dparts, dw, dh).save(os.path.join(TEX, "laury_dragon.png"))
    for expr, name in [("normal", "laury"), ("blink", "laury_blink"), ("happy", "laury_happy"),
                       ("surprised", "laury_surprised"), ("angry", "laury_angry"), ("tired", "laury_tired")]:
        skin(expr).save(os.path.join(TEX, name + ".png"))

    out = ["// GENERADO por tools/gen_models.py — no editar a mano.",
           "package com.laury.laurymod.client;", "",
           "import net.minecraft.client.model.geom.ModelPart;",
           "import net.minecraft.client.model.geom.PartPose;",
           "import net.minecraft.client.model.geom.builders.*;", "",
           "import java.util.HashMap;", "import java.util.Map;", "",
           "public final class LauryGeneratedModels {",
           "    private LauryGeneratedModels() {}", ""]
    out += java_layer("createFeatures", fparts, fanchors, fw, fh) + [""]
    out += java_bake("bakeFeatures", fparts, fanchors) + [""]
    out += java_layer("createDragon", dparts, danchors, dw, dh) + [""]
    out += java_bake("bakeDragon", dparts, danchors)
    out += ["}", ""]
    with open(JAVA, "w") as f:
        f.write("\n".join(out))
    print("features %dx%d (%d partes), dragon %dx%d (%d partes)" % (fw, fh, len(fparts), dw, dh, len(dparts)))
    return fparts, fanchors, dparts


# ================================================================= vista previa
def rot(v, rx, ry, rz):
    x, y, z = v
    y, z = y * math.cos(rx) - z * math.sin(rx), y * math.sin(rx) + z * math.cos(rx)
    x, z = x * math.cos(ry) + z * math.sin(ry), -x * math.sin(ry) + z * math.cos(ry)
    x, y = x * math.cos(rz) - y * math.sin(rz), x * math.sin(rz) + y * math.cos(rz)
    return (x, y, z)


def world_transforms(parts, anchors):
    tf = {"root": []}
    for a, piv in anchors:
        tf[a] = [(piv, (0, 0, 0))]
    for p in parts:
        tf[p.name] = tf[p.parent] + [(p.pivot, p.rot)]
    return tf


def apply_chain(chain, v):
    for piv, r in reversed(chain):
        v = rot(v, *r)
        v = (v[0] + piv[0], v[1] + piv[1], v[2] + piv[2])
    return v


def quads_for(img, x0, y0, z0, w, h, d, u, v, chain, infl=0.0):
    quads = []
    x0 -= infl; y0 -= infl; z0 -= infl
    x1, y1, z1 = x0 + w + 2 * infl, y0 + h + 2 * infl, z0 + d + 2 * infl
    b = Box(0, 0, 0, w, h, d, None); b.u, b.v = u, v
    fr = face_rects(b)
    corners = {
        "front": ((x0, y0, z0), (x1, y0, z0), (x0, y1, z0)),
        "back": ((x1, y0, z1), (x0, y0, z1), (x1, y1, z1)),
        "right": ((x0, y0, z1), (x0, y0, z0), (x0, y1, z1)),
        "left": ((x1, y0, z0), (x1, y0, z1), (x1, y1, z0)),
        "top": ((x0, y0, z1), (x1, y0, z1), (x0, y0, z0)),
        "bottom": ((x0, y1, z0), (x1, y1, z0), (x0, y1, z1)),
    }
    shade = {"front": 1.0, "back": 0.72, "left": 0.84, "right": 0.84, "top": 1.1, "bottom": 0.6}
    for f, (a, bb, c) in corners.items():
        fx, fy, fw, fh = fr[f]
        if fw <= 0 or fh <= 0:
            continue
        nx, ny = max(1, math.ceil(fw)), max(1, math.ceil(fh))
        for i in range(nx):
            for j in range(ny):
                px, py = int(fx + (i + 0.5) * fw / nx), int(fy + (j + 0.5) * fh / ny)
                if not (0 <= px < img.width and 0 <= py < img.height):
                    continue
                col = img.getpixel((px, py))
                if col[3] == 0:
                    continue
                def L(s, t):
                    return tuple(a[k] + (bb[k] - a[k]) * s + (c[k] - a[k]) * t for k in range(3))
                q = [apply_chain(chain, L(i / nx, j / ny)), apply_chain(chain, L((i + 1) / nx, j / ny)),
                     apply_chain(chain, L((i + 1) / nx, (j + 1) / ny)), apply_chain(chain, L(i / nx, (j + 1) / ny))]
                quads.append((q, tuple(int(min(255, ch * shade[f])) for ch in col[:3])))
    return quads


def draw(quads, path, scale, cy, title):
    views = [("frente", math.pi, 0.08), ("3/4", math.pi - 0.75, 0.15), ("perfil", math.pi / 2, 0.06), ("espalda 3/4", -0.75, 0.15)]
    W = 260
    out = Image.new("RGB", (W * 4, 560), (206, 222, 236))
    dr = ImageDraw.Draw(out)
    for k, (name, yaw, pitch) in enumerate(views):
        proj = []
        for q, col in quads:
            pts, dep = [], 0
            for (x, y, z) in q:
                X = x * math.cos(yaw) - z * math.sin(yaw)
                Z = x * math.sin(yaw) + z * math.cos(yaw)
                Y, Z = y * math.cos(pitch) - Z * math.sin(pitch), y * math.sin(pitch) + Z * math.cos(pitch)
                pts.append((W * k + W / 2 - X * scale, cy + Y * scale))
                dep += Z
            proj.append((dep / 4, pts, col))
        proj.sort(key=lambda t: t[0])
        for _, pts, col in proj:
            dr.polygon(pts, fill=col)
        dr.text((W * k + 10, 540), name, fill=(40, 40, 40))
    dr.text((10, 8), title, fill=(30, 30, 30))
    out.save(path)


def preview(outdir, fparts, fanchors, dparts):
    sk = Image.open(os.path.join(TEX, "laury.png")).convert("RGBA")
    ft = Image.open(os.path.join(TEX, "laury_features.png")).convert("RGBA")
    dt = Image.open(os.path.join(TEX, "laury_dragon.png")).convert("RGBA")
    quads = []
    base = [("head", (0, 0, 0), (0, 0), (-4, -8, -4, 8, 8, 8), (32, 0)),
            ("body", (0, 0, 0), (16, 16), (-4, 0, -2, 8, 12, 4), (16, 32)),
            ("right_arm", (-5, 2, 0), (40, 16), (-2, -2, -2, 3, 12, 4), (40, 32)),
            ("left_arm", (5, 2, 0), (32, 48), (-1, -2, -2, 3, 12, 4), (48, 48)),
            ("right_leg", (-1.9, 12, 0), (0, 16), (-2, 0, -2, 4, 12, 4), (0, 32)),
            ("left_leg", (1.9, 12, 0), (16, 48), (-2, 0, -2, 4, 12, 4), (0, 48))]
    for name, piv, (u, v), bx, (ou, ov) in base:
        chain = [(piv, (0, 0, 0))]
        quads += quads_for(sk, *bx, u, v, chain)
    tf = world_transforms(fparts, fanchors)
    for p in fparts:
        for b in p.boxes:
            quads += quads_for(ft, b.x, b.y, b.z, b.w, b.h, b.d, b.u, b.v, tf[p.name])
    draw(quads, os.path.join(outdir, "laury_preview.png"), 12, 210, "Laury (humana)")
    dq = []
    tf = world_transforms(dparts, [])
    for p in dparts:
        for b in p.boxes:
            dq += quads_for(dt, b.x, b.y, b.z, b.w, b.h, b.d, b.u, b.v, tf[p.name])
    draw(dq, os.path.join(outdir, "laury_dragon_preview.png"), 5.5, 330, "Laury (dragon, escala modelo)")


if __name__ == "__main__":
    fp, fa, dp = main()
    if len(sys.argv) > 2 and sys.argv[1] == "preview":
        preview(sys.argv[2], fp, fa, dp)
