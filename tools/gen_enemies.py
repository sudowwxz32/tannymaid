"""Enemigos originales: modelos 3D (Java) + texturas (normal y brillante).

Cada enemigo es humanoide (cabeza, cuerpo, brazos y piernas en posiciones estándar, para poder animarlo
como un HumanoidModel) con piezas extra propias. Un píxel pintado como (r, g, b, 'G') brilla en la oscuridad.
Uso: python3 tools/gen_enemies.py [carpeta_vista_previa]
"""
import os, sys, math, random
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import modelgen_base as mg
from modelgen_base import Box, Part, pack, mirror
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX = os.path.join(ROOT, "src/main/resources/assets/blockdragonsuperz/textures/entity/enemy")
JAVA = os.path.join(ROOT, "src/main/java/com/bdsz/client/EnemyModels.java")
R = random.Random(9)


def jit(c, a=6):
    return tuple(max(0, min(255, v + R.randint(-a, a))) for v in c[:3])


def G(c):
    return tuple(c[:3]) + ("G",)


def solid(c, a=6):
    return lambda f, x, y, w, h: jit(c, a)


def metal(base, hi, dk, lines=4):
    def fn(f, x, y, w, h):
        if f == "top":
            return jit(hi, 4)
        if f == "bottom":
            return jit(dk, 4)
        if x == 0 or x == w - 1:
            return jit(dk, 3)
        if lines and (y % lines == lines - 1):
            return jit(dk, 3)                    # líneas de los paneles
        if (x + y) % 7 == 0:
            return jit(hi, 4)
        return jit(base, 5)
    return fn


def humanoid(head, body, arm, leg, hw=8):
    """Piezas estándar (mismas posiciones que un jugador)."""
    return [
        Part("head", "root", (0, 0, 0), (0, 0, 0), [Box(-hw / 2, -8, -hw / 2, hw, 8, hw, head)]),
        Part("hat", "root", (0, 0, 0), (0, 0, 0), []),
        Part("body", "root", (0, 0, 0), (0, 0, 0), [Box(-4, 0, -2, 8, 12, 4, body)]),
        Part("right_arm", "root", (-5, 2, 0), (0, 0, 0), [Box(-3, -2, -2, 4, 12, 4, arm)]),
        Part("left_arm", "root", (5, 2, 0), (0, 0, 0), [Box(-1, -2, -2, 4, 12, 4, arm)]),
        Part("right_leg", "root", (-1.9, 12, 0), (0, 0, 0), [Box(-2, 0, -2, 4, 12, 4, leg)]),
        Part("left_leg", "root", (1.9, 12, 0), (0, 0, 0), [Box(-2, 0, -2, 4, 12, 4, leg)]),
    ]


def sym(P, names):
    """Copia en espejo (lado izquierdo) de las piezas indicadas, colgándolas del brazo/pierna izquierdos."""
    out = []
    for m in mirror([p for p in P if p.name in names]):
        m.parent = m.parent.replace("right_arm", "left_arm").replace("right_leg", "left_leg")
        out.append(m)
    return out


def face(skin, eye, eyes_row=4, mouth=None, top=None, top_rows=0, brow=None):
    """Cara: ojos brillantes en la fila indicada; pelo/casco opcional en las filas de arriba."""
    def fn(f, x, y, w, h):
        if top and (f == "top" or y < top_rows or (f == "back" and y < h - 2)):
            return jit(top, 5)
        if f == "front":
            if y == eyes_row and (x in (1, 2) or x in (w - 3, w - 2)):
                return G(eye)
            if brow and y == eyes_row - 1 and (x in (1, 2) or x in (w - 3, w - 2)):
                return jit(brow, 3)
            if mouth and y == h - 2 and 2 < x < w - 3:
                return jit(mouth, 3)
        return jit(skin, 5)
    return fn


# =================================================================== Androide de Combate
def androide():
    SKIN, HAIR, SUIT, SILVER, CYAN = (198, 204, 214), (40, 44, 62), (34, 44, 82), (170, 178, 192), (90, 230, 255)

    def head(f, x, y, w, h):
        if f == "top" or (y < 2 and f != "front") or (f == "front" and y == 0) or (f == "back" and y < 6):
            return jit(HAIR, 4)
        if f == "front" and y == 3:
            return G(CYAN) if 0 < x < w - 1 else jit(SILVER, 3)       # visor
        if f == "front" and y == 6 and 2 < x < 5:
            return jit((120, 124, 136), 3)
        return jit(SKIN, 4)

    def body(f, x, y, w, h):
        if f == "front" and 3 <= x <= 4 and 3 <= y <= 4:
            return G(CYAN)                                             # núcleo
        if y == h - 2:
            return jit(SILVER, 3)                                      # cinturón
        if f == "front" and x in (1, w - 2):
            return jit(SILVER, 4)                                      # franjas
        return jit(SUIT, 5)

    def arm(f, x, y, w, h):
        if y >= 6:
            return jit(SILVER, 5) if y < h - 2 else jit((150, 156, 168), 4)
        return jit(SUIT, 5)

    def leg(f, x, y, w, h):
        if 4 <= y <= 5 and f == "front":
            return jit(SILVER, 3)                                      # rodillera
        if y >= h - 3:
            return jit((24, 26, 36), 4)                                # botas
        return jit(SUIT, 5)

    P = humanoid(head, body, arm, leg)
    P.append(Part("ear_r", "head", (-4.2, -4.5, 0), (0, 0, 0), [Box(-0.8, -1, -1, 0.8, 2, 2, lambda f, x, y, w, h: G(CYAN) if f in ("right", "left") else jit(SILVER))]))
    P += sym(P, ["ear_r"])
    P.append(Part("pad_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-3.5, -2.6, -2.5, 4.6, 2.6, 5, metal(SILVER, (210, 214, 224), (110, 116, 130), 0))]))
    P.append(Part("pad_l", "left_arm", (0, 0, 0), (0, 0, 0), [Box(-1.1, -2.6, -2.5, 4.6, 2.6, 5, metal(SILVER, (210, 214, 224), (110, 116, 130), 0))]))
    P.append(Part("collar", "body", (0, 0, 0), (0, 0, 0), [Box(-4.3, -0.3, -2.3, 8.6, 1.6, 4.6, metal(SILVER, (210, 214, 224), (110, 116, 130), 0))]))
    return P


# =================================================================== Robot Guardián
def robot():
    OL, OL_HI, OL_DK, RED, YEL, BLK = (104, 112, 82), (136, 146, 108), (64, 70, 50), (255, 50, 40), (232, 190, 40), (30, 30, 30)
    M = metal(OL, OL_HI, OL_DK)

    def head(f, x, y, w, h):
        if f == "front" and y == 2 and 0 < x < w - 1:
            return G(RED)
        return M(f, x, y, w, h)

    def body(f, x, y, w, h):
        if y >= h - 2:
            return jit(YEL if (x + y) % 4 < 2 else BLK, 3)            # franjas de peligro
        if f == "front" and w // 2 - 1 <= x <= w // 2 and 4 <= y <= 5:
            return G(RED)
        if f == "front" and 2 <= y <= 7 and x in (2, w - 3):
            return jit(OL_DK, 3)                                       # rejillas
        return M(f, x, y, w, h)

    P = [
        Part("head", "root", (0, 0, 0), (0, 0, 0), [Box(-3, -5, -3.5, 6, 5, 7, head)]),
        Part("hat", "root", (0, 0, 0), (0, 0, 0), []),
        Part("body", "root", (0, 0, 0), (0, 0, 0), [Box(-6, -1.5, -3.5, 12, 13.5, 7, body)]),
        Part("right_arm", "root", (-5, 2, 0), (0, 0, 0), [Box(-6.5, -3, -3, 6, 12, 6, M)]),
        Part("left_arm", "root", (5, 2, 0), (0, 0, 0), [Box(0.5, -3, -3, 6, 12, 6, M)]),
        Part("right_leg", "root", (-1.9, 12, 0), (0, 0, 0), [Box(-2.6, 0, -2.6, 5.2, 12, 5.2, M)]),
        Part("left_leg", "root", (1.9, 12, 0), (0, 0, 0), [Box(-2.6, 0, -2.6, 5.2, 12, 5.2, M)]),
    ]
    P.append(Part("antenna", "head", (0, -5, 0), (0, 0, 0), [Box(-0.5, -3, -0.5, 1, 3, 1, lambda f, x, y, w, h: G(RED) if y == 0 else jit(BLK))]))
    P.append(Part("cannon", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-6, 8, -4.5, 5, 4.5, 7, lambda f, x, y, w, h: G(RED) if f == "front" and 1 <= x <= 3 and 1 <= y <= 3 else jit(BLK, 6))]))
    P.append(Part("claw", "left_arm", (0, 0, 0), (0, 0, 0), [Box(1, 9, -3.5, 5, 3.5, 7, M)]))
    P.append(Part("foot_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-3, 10, -4.5, 6, 2, 7.5, metal(OL_DK, OL, BLK, 0))]))
    P.append(Part("foot_l", "left_leg", (0, 0, 0), (0, 0, 0), [Box(-3, 10, -4.5, 6, 2, 7.5, metal(OL_DK, OL, BLK, 0))]))
    P.append(Part("shoulder_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-7, -4, -3.5, 7, 3, 7, metal(OL_HI, (170, 180, 140), OL_DK, 0))]))
    P.append(Part("shoulder_l", "left_arm", (0, 0, 0), (0, 0, 0), [Box(0, -4, -3.5, 7, 3, 7, metal(OL_HI, (170, 180, 140), OL_DK, 0))]))
    return P


# =================================================================== Soldado del Imperio
def soldado():
    SKIN, TEAL, TEAL_DK, BLK, RED = (148, 128, 170), (40, 150, 150), (24, 96, 100), (28, 28, 34), (255, 60, 60)
    head = face(SKIN, (255, 230, 90), eyes_row=5, mouth=(100, 80, 120))

    def body(f, x, y, w, h):
        return jit(BLK, 4)

    def arm(f, x, y, w, h):
        return jit((200, 200, 206), 4) if y >= h - 3 else jit(BLK, 4)

    def leg(f, x, y, w, h):
        return jit(TEAL, 5) if y >= h - 4 else jit(BLK, 4)

    def plate(f, x, y, w, h):
        if f == "front" and x == w // 2:
            return jit(BLK, 3)
        if y == 0 or x == 0 or x == w - 1:
            return jit(TEAL_DK, 3)
        return jit(TEAL, 6)

    def helm(f, x, y, w, h):
        if f == "front" and y == h - 1:
            return jit(TEAL_DK, 3)
        return jit(BLK, 4) if f != "top" else jit(TEAL_DK, 4)

    P = humanoid(head, body, arm, leg)
    P.append(Part("helmet", "head", (0, 0, 0), (0, 0, 0), [Box(-4.5, -8.6, -4.5, 9, 3.4, 9, helm)]))
    P.append(Part("visor", "head", (0, 0, 0), (0, 0, 0), [Box(-4.2, -5.6, -4.65, 8.4, 1.2, 0.4, lambda f, x, y, w, h: G(RED))]))
    P.append(Part("chest", "body", (0, 0, 0), (0, 0, 0), [Box(-4.6, -0.4, -2.6, 9.2, 8, 5.2, plate)]))
    P.append(Part("pauldron_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-4, -3, -3, 5.4, 3.4, 6, plate)]))
    P.append(Part("pauldron_l", "left_arm", (0, 0, 0), (0, 0, 0), [Box(-1.4, -3, -3, 5.4, 3.4, 6, plate)]))
    return P


# =================================================================== Engendro Bio
def engendro():
    CH, CH_HI, CH_DK, OR = (42, 62, 128), (70, 96, 170), (24, 34, 76), (255, 140, 40)
    C = metal(CH, CH_HI, CH_DK, 0)

    def head(f, x, y, w, h):
        if f == "front" and y == 3 and (x in (1, 2) or x in (w - 3, w - 2)):
            return G(OR)
        if f == "front" and y == 2 and (x == 2 or x == w - 3):
            return jit(CH_DK, 3)
        return C(f, x, y, w, h)

    def body(f, x, y, w, h):
        if f in ("front", "back") and y % 3 == 1:
            return jit(OR if f == "front" else CH_DK, 4)               # marcas
        return C(f, x, y, w, h)

    P = humanoid(head, body, C, C)
    P[0].boxes = [Box(-4, -7, -4.5, 8, 7, 8, head)]
    P.append(Part("mandible_r", "head", (-2.5, -1, -4.5), (0.2, 0, 0.2), [Box(-1, 0, -1.5, 1.4, 2.2, 1.4, solid(CH_DK))]))
    P.append(Part("crest", "head", (0, -7, 1), (-0.6, 0, 0), [Box(-1, -4, -1, 2, 4, 2, solid(CH_HI))]))
    P.append(Part("crest_r", "head", (-2.4, -6.5, 1.5), (-0.8, 0, -0.4), [Box(-0.8, -3, -0.8, 1.6, 3, 1.6, solid(CH_HI))]))
    for i in range(3):
        P.append(Part("spine%d" % i, "body", (0, 2 + i * 3.2, 2), (-1.0, 0, 0), [Box(-0.8, -3, -0.8, 1.6, 3, 1.6, solid(OR, 8))]))
    P += sym(P, ["mandible_r", "crest_r"])
    P.append(Part("claw_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-3.5, 9.5, -3, 1.2, 4, 1.2, solid((230, 220, 200))), Box(-1.5, 9.5, -3, 1.2, 4, 1.2, solid((230, 220, 200)))]))
    P.append(Part("claw_l", "left_arm", (0, 0, 0), (0, 0, 0), [Box(0.3, 9.5, -3, 1.2, 4, 1.2, solid((230, 220, 200))), Box(2.3, 9.5, -3, 1.2, 4, 1.2, solid((230, 220, 200)))]))
    return P


# =================================================================== Bio-Titán (jefe)
def titan():
    BL, BL_HI, OR, BONE = (32, 30, 38), (60, 56, 70), (255, 120, 30), (222, 210, 186)

    def crack(f, x, y, w, h):
        if (x * 3 + y * 5) % 17 == 0 and f != "top":
            return G(OR)                                               # grietas brillantes
        if y % 3 == 2:
            return jit((22, 20, 26), 3)
        return jit(BL if (x + y) % 5 else BL_HI, 5)

    def head(f, x, y, w, h):
        if f == "front" and y == 4 and (x in (1, 2) or x in (w - 3, w - 2)):
            return G(OR)
        if f == "front" and y == 6 and 1 < x < w - 2:
            return jit(BONE, 4)                                        # dientes
        return crack(f, x, y, w, h)

    P = humanoid(head, crack, crack, crack)
    P.append(Part("horn_r", "head", (-3, -7, 0), (0.7, 0, -0.35), [Box(-0.9, -6, -0.9, 1.8, 6, 1.8, solid(BONE))]))
    P.append(Part("horn_tip_r", "horn_r", (0, -5.8, 0), (0.6, 0, 0), [Box(-0.5, -4, -0.5, 1, 4, 1, solid(BONE))]))
    P.append(Part("chestplate", "body", (0, 0, 0), (0, 0, 0), [Box(-4.6, -0.4, -2.6, 9.2, 6.5, 5.2, crack)]))
    P.append(Part("spike_r", "right_arm", (-1.5, -2.5, 0), (0, 0, -0.5), [Box(-1, -4.5, -1, 2, 4.5, 2, solid(BONE))]))
    P.append(Part("guard_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-3.5, 5, -2.5, 5, 5, 5, crack)]))
    P.append(Part("claw_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-3.2, 10, -2.8, 1, 3.5, 1, solid(BONE)), Box(-1, 10, -2.8, 1, 3.5, 1, solid(BONE))]))
    P.append(Part("knee_r", "right_leg", (0, 0, 0), (0, 0, 0), [Box(-2.5, 4, -2.8, 5, 3, 1.2, solid(BONE))]))
    P += sym(P, ["spike_r", "guard_r", "claw_r", "knee_r", "horn_r", "horn_tip_r"])
    return P


# =================================================================== Demonio Arcano (jefe)
def demonio():
    SK, SK_DK, ROBE, ROBE_DK, GOLD, YEL, CAPE, HORN = (110, 60, 140), (80, 40, 104), (40, 24, 50), (26, 14, 34), (214, 170, 60), (255, 230, 60), (110, 20, 34), (36, 26, 30)
    head = face(SK, YEL, eyes_row=4, mouth=(40, 10, 30), brow=SK_DK)

    def robe(f, x, y, w, h):
        if (f in ("front", "back") and (x == 0 or x == w - 1)) or (y == h - 1 and f != "top"):
            return jit(GOLD, 6)                                        # ribete dorado
        if f == "front" and x == w // 2 and y < h - 2:
            return jit(GOLD, 6)
        return jit(ROBE if (x + y) % 4 else ROBE_DK, 4)

    def body(f, x, y, w, h):
        if f == "front" and 3 <= x <= 4 and 4 <= y <= 5:
            return G((240, 60, 220))                                   # gema
        if y == 7:
            return jit(GOLD, 5)
        return robe(f, x, y, w, h)

    def arm(f, x, y, w, h):
        return jit(SK, 5) if y >= h - 4 else robe(f, x, y, w, h)

    P = humanoid(head, body, arm, robe)
    P.append(Part("horn_r", "head", (-3, -7.5, -1), (0.2, 0, -0.6), [Box(-0.9, -3.5, -0.9, 1.8, 3.5, 1.8, solid(HORN))]))
    P.append(Part("horn2_r", "horn_r", (0, -3.3, 0), (0.7, 0, 0.5), [Box(-0.6, -3.5, -0.6, 1.2, 3.5, 1.2, solid(HORN))]))
    P.append(Part("cape", "body", (0, 0, 2.1), (0.12, 0, 0), [Box(-4.6, -0.2, 0, 9.2, 21, 0.7,
                  lambda f, x, y, w, h: jit(GOLD, 5) if (x == 0 or x == w - 1) else jit(CAPE, 6))]))
    P.append(Part("collar", "body", (0, 0, 0), (0, 0, 0), [Box(-4.8, -1.2, -2.6, 9.6, 2.2, 5.2, solid(CAPE))]))
    P += sym(P, ["horn_r", "horn2_r"])
    return P


# =================================================================== Emperador Galáctico (jefe)
def emperador():
    SK, BLK, GOLD, GOLD_DK, CRIM, RED = (122, 140, 172), (24, 22, 30), (226, 182, 62), (160, 120, 36), (150, 22, 40), (255, 50, 60)
    head = face(SK, RED, eyes_row=4, mouth=(70, 80, 100), brow=(90, 104, 132))

    def armor(f, x, y, w, h):
        if f in ("front", "back") and w >= 6 and (x == 0 or x == w - 1):
            return jit(GOLD, 5)
        if y % 4 == 3 and f in ("front", "back"):
            return jit(GOLD_DK, 4)
        return jit(BLK, 4)

    def chest(f, x, y, w, h):
        if f == "front" and 2 <= x <= w - 3 and 1 <= y <= 4:
            return jit(CRIM, 5) if not (x == w // 2 and y == 2) else G(RED)
        return armor(f, x, y, w, h)

    def gaunt(f, x, y, w, h):
        return jit(GOLD, 6) if y >= h - 5 else armor(f, x, y, w, h)

    P = humanoid(head, armor, gaunt, armor)
    P.append(Part("crown", "head", (0, 0, 0), (0, 0, 0), [Box(-4.5, -9, -4.5, 9, 2.6, 9, metal(GOLD, (250, 220, 120), GOLD_DK, 0))]))
    for i, x in enumerate((-3.2, -1.1, 1.1, 3.2)):
        P.append(Part("point%d" % i, "crown", (x, -9, -3.8), (0.15, 0, 0), [Box(-0.6, -2.5 - (i in (1, 2)) * 1.2, -0.6, 1.2, 2.5 + (i in (1, 2)) * 1.2, 1.2, solid(GOLD))]))
    P.append(Part("crown_gem", "crown", (0, 0, 0), (0, 0, 0), [Box(-0.8, -8.4, -4.75, 1.6, 1.4, 0.3, lambda f, x, y, w, h: G((80, 200, 255)))]))
    P.append(Part("chest", "body", (0, 0, 0), (0, 0, 0), [Box(-4.6, -0.4, -2.6, 9.2, 7, 5.2, chest)]))
    P.append(Part("pauldron_r", "right_arm", (0, 0, 0), (0, 0, 0), [Box(-4.2, -3.2, -3, 5.8, 3.6, 6, armor)]))
    P.append(Part("pauldron_l", "left_arm", (0, 0, 0), (0, 0, 0), [Box(-1.6, -3.2, -3, 5.8, 3.6, 6, armor)]))
    P.append(Part("cape", "body", (0, 0, 2.2), (0.1, 0, 0), [Box(-5, -0.4, 0, 10, 22, 0.7,
                  lambda f, x, y, w, h: jit(GOLD, 5) if (x == 0 or x == w - 1 or y == h - 1) else jit(CRIM, 6))]))
    return P


KINDS = {"androide": androide, "robot_guardian": robot, "soldado_imperial": soldado, "engendro_bio": engendro,
         "bio_titan": titan, "demonio_arcano": demonio, "emperador_galactico": emperador}


# =================================================================== salida
def paint(parts, w, h, glow):
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    for p in parts:
        for b in p.boxes:
            for f, (fx, fy, fw, fh) in mg.face_rects(b).items():
                if fw <= 0 or fh <= 0:
                    continue
                cw, ch = max(1, math.ceil(fw)), max(1, math.ceil(fh))
                for py in range(math.floor(fy), math.ceil(fy + fh)):
                    for px in range(math.floor(fx), math.ceil(fx + fw)):
                        lx = min(cw - 1, max(0, int(px + 0.5 - fx)))
                        ly = min(ch - 1, max(0, int(py + 0.5 - fy)))
                        c = b.paint(f, lx, ly, cw, ch)
                        if c is None or not (0 <= px < w and 0 <= py < h):
                            continue
                        is_glow = len(c) == 4 and c[3] == "G"
                        if glow:
                            img.putpixel((px, py), tuple(c[:3]) + (255,) if is_glow else (0, 0, 0, 0))
                        else:
                            img.putpixel((px, py), tuple(c[:3]) + (255,))
    return img


def java(models):
    L = ["// GENERADO por tools/gen_enemies.py — no editar a mano.", "package com.bdsz.client;", "",
         "import net.minecraft.client.model.geom.PartPose;", "import net.minecraft.client.model.geom.builders.*;", "",
         "import java.util.HashMap;", "import java.util.Map;", "",
         "public final class EnemyModels {", "    private EnemyModels() {}", "",
         "    public static LayerDefinition create(String id) {", "        return switch (id) {"]
    for kid in models:
        L.append('            case "%s" -> %s();' % (kid, kid))
    L += ['            default -> throw new IllegalArgumentException(id);', "        };", "    }", ""]
    for kid, (P, w, h) in models.items():
        L += ["    private static LayerDefinition %s() {" % kid,
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
        L += ["        return LayerDefinition.create(mesh, %d, %d);" % (w, h), "    }", ""]
    L += ["}", ""]
    return "\n".join(L)


def main(preview=None):
    os.makedirs(TEX, exist_ok=True)
    models = {}
    for kid, fn in KINDS.items():
        P = fn()
        w, h = pack(P, 64)
        paint(P, w, h, False).save(os.path.join(TEX, kid + ".png"))
        paint(P, w, h, True).save(os.path.join(TEX, kid + "_glow.png"))
        models[kid] = (P, w, h)
        print(kid, w, h, len(P))
    open(JAVA, "w").write(java(models))
    return models


if __name__ == "__main__":
    main()
