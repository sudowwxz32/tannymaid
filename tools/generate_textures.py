"""Genera las texturas de Tanny (formato skin 64x64 brazos delgados) y el icono del contrato.
Uso: python3 tools/generate_textures.py  (desde la raíz del proyecto)"""
import os, random
from PIL import Image

OUT = "src/main/resources/assets/tannymaid/textures"
random.seed(7)

SKIN = (198, 140, 98); SKIN_SH = (178, 122, 84); SKIN_HI = (214, 158, 116)
HAIR = (22, 20, 26); HAIR_HI = (48, 44, 58); HAIR_DK = (10, 9, 12)
EYE = (110, 64, 34); EYE_DK = (62, 34, 18); EYE_HI = (240, 228, 214); WHITE_EYE = (245, 242, 238)
LASH = (26, 18, 18); BLUSH = (226, 128, 128); LIP = (196, 90, 96)
BLACK = (28, 26, 32); BLACK_HI = (46, 43, 52); WHITE = (246, 244, 240); WHITE_SH = (218, 214, 210)
RED = (206, 52, 74); PINK = (240, 150, 170); SHOE = (60, 36, 30)
T = (0, 0, 0, 0)

def faces(u, v, w, h, d):
    return {"top": (u+d, v, w, d), "bottom": (u+d+w, v, w, d), "right": (u, v+d, d, h),
            "front": (u+d, v+d, w, h), "left": (u+d+w, v+d, d, h), "back": (u+2*d+w, v+d, w, h)}

def jitter(c, a=6):
    if len(c) == 4: return c
    return tuple(max(0, min(255, x + random.randint(-a, a))) for x in c)

def paint(img, u, v, w, h, d, fn):
    for name, (x0, y0, fw, fh) in faces(u, v, w, h, d).items():
        for y in range(fh):
            for x in range(fw):
                c = fn(name, x, y, fw, fh)
                if c is None: continue
                img.putpixel((x0+x, y0+y), c if len(c) == 4 else jitter(c) + (255,))

def head_base(face, x, y, w, h):
    if face == "top": return HAIR_HI if (x+y) % 3 == 0 else HAIR
    if face == "bottom": return SKIN_SH
    if face == "back": return HAIR_DK if y == 7 else HAIR
    if face in ("left", "right"):
        if y <= 6: return HAIR if y > 0 else HAIR_HI
        return SKIN_SH
    # frente: cabello + flequillo bob
    if y == 0: return HAIR_HI
    if y == 1: return HAIR
    if y == 2: return HAIR if x in (0, 1, 3, 6, 7) else SKIN
    if x in (0, 7) and y <= 6: return HAIR
    return SKIN

def draw_face(img, expr):
    fx, fy = 8, 8
    P = lambda x, y, c: img.putpixel((fx+x, fy+y), c + (255,))
    for y in range(3, 8):
        for x in range(1, 7):
            P(x, y, SKIN if y < 7 else SKIN_SH)
    if expr in ("normal", "focused", "surprised"):
        if expr == "focused":
            P(1, 3, LASH); P(2, 3, LASH); P(5, 3, LASH); P(6, 3, LASH)
            P(1, 4, EYE_DK); P(2, 4, EYE); P(5, 4, EYE); P(6, 4, EYE_DK)
            P(1, 5, SKIN); P(2, 5, SKIN); P(5, 5, SKIN); P(6, 5, SKIN)
        else:
            P(1, 3, LASH); P(2, 3, LASH); P(5, 3, LASH); P(6, 3, LASH)
            P(1, 4, EYE_HI); P(2, 4, EYE_DK); P(5, 4, EYE_DK); P(6, 4, EYE_HI)
            P(1, 5, EYE); P(2, 5, EYE); P(5, 5, EYE); P(6, 5, EYE)
            if expr == "surprised":
                P(1, 2, LASH); P(6, 2, LASH)
    elif expr == "happy":      # ojos cerrados sonriendo ^ ^
        P(1, 5, LASH); P(2, 4, LASH); P(5, 4, LASH); P(6, 5, LASH)
    elif expr in ("blink", "tired"):
        P(1, 5, LASH); P(2, 5, LASH); P(5, 5, LASH); P(6, 5, LASH)
    P(1, 6, BLUSH); P(6, 6, BLUSH)
    if expr == "surprised":
        P(3, 6, LIP); P(4, 6, LIP); P(3, 7, (120, 50, 56)); P(4, 7, (120, 50, 56))
    elif expr == "tired":
        P(3, 7, LIP); P(4, 7, LIP)
    elif expr == "focused":
        P(3, 7, LIP); P(4, 7, LIP)
    else:  # sonrisa
        P(2, 6, SKIN_SH); P(3, 7, LIP); P(4, 7, LIP); P(5, 6, SKIN_SH)

def hat_overlay(face, x, y, w, h):
    # El volumen del cabello y la cofia ahora son 3D (hair.png + TannyHairLayer)
    return T
    if face == "top":
        if y in (2, 3): return WHITE if x % 2 == 0 else WHITE_SH
        return HAIR if (x + y) % 2 == 0 else HAIR_HI
    if face == "back": return HAIR if y <= 6 else T
    if face in ("left", "right"):
        if y <= 6: return HAIR if (x + y) % 4 else HAIR_HI
        if y == 7 and x in (2, 3, 4, 5): return HAIR_DK
        return T
    if face == "front":
        if y == 0: return WHITE if x % 2 == 0 else WHITE_SH
        if y == 1 and x in (0, 7): return HAIR
        if y == 1 and x in (2, 5): return HAIR_HI
        return T
    return T

def body_base(face, x, y, w, h):
    if face == "top": return BLACK
    if face == "bottom": return BLACK
    if face == "front":
        if y == 0: return SKIN if x in (3, 4) else WHITE           # cuello y cuello de camisa
        if y == 1: return RED if x in (3, 4) else (WHITE if x in (2, 5) else BLACK)   # lazo rojo
        if 2 <= y <= 6 and 2 <= x <= 5: return WHITE if y > 2 else WHITE_SH       # pechera del delantal
        if y == 7: return WHITE_SH if 1 <= x <= 6 else BLACK         # cintura
        if y >= 8 and 1 <= x <= 6: return WHITE if y < 11 else PINK  # delantal
        return BLACK_HI if y in (3, 4) and x in (1, 6) else BLACK
    if face == "back":
        if y == 7: return WHITE
        if y in (6, 8) and x in (2, 3, 4, 5): return WHITE_SH        # lazo grande en la espalda
        if y == 8 and x in (3, 4): return WHITE
        return BLACK
    if face in ("left", "right"):
        if y == 7: return WHITE_SH
        return BLACK
    return BLACK

def jacket_overlay(face, x, y, w, h):
    # falda con vuelo
    if face == "top": return T
    if face == "bottom": return BLACK
    if y < 8: return T
    if y == 11: return WHITE                                   # encaje
    if face == "front" and 1 <= x <= 6: return WHITE if y < 10 else WHITE_SH
    if face == "back" and y == 8 and x in (2, 5): return WHITE
    return BLACK_HI if (x + y) % 3 == 0 else BLACK

def arm_base(face, x, y, w, h):
    if face == "top": return BLACK
    if face == "bottom": return SKIN_SH
    if y <= 2: return BLACK
    if y == 3: return WHITE
    if y >= 10: return SKIN_HI if face == "front" else SKIN
    return SKIN

def sleeve_overlay(face, x, y, w, h):
    # manga abullonada
    if face == "top": return BLACK_HI
    if face == "bottom": return T
    if y <= 2: return BLACK_HI if (x + y) % 2 == 0 else BLACK
    if y == 3: return WHITE
    return T

def leg_base(face, x, y, w, h):
    if face == "top": return BLACK
    if face == "bottom": return SHOE
    if y <= 3: return BLACK
    if y == 4: return WHITE                                    # bajo de la falda
    if y == 5: return SKIN
    if y == 6: return RED if face in ("front", "back") else WHITE   # franja de la calceta
    if 7 <= y <= 9: return WHITE if y != 9 else WHITE_SH       # calcetas hasta la rodilla
    return SHOE if y == 10 else (40, 24, 20)                   # zapatos

def pants_overlay(face, x, y, w, h):
    if face in ("top", "bottom"): return T
    if y <= 3: return BLACK_HI if (x + y) % 3 == 0 else BLACK
    if y == 4: return WHITE
    return T

def skin(expr):
    img = Image.new("RGBA", (64, 64), T)
    paint(img, 0, 0, 8, 8, 8, head_base)
    draw_face(img, expr)
    paint(img, 32, 0, 8, 8, 8, hat_overlay)
    paint(img, 16, 16, 8, 12, 4, body_base)
    paint(img, 16, 32, 8, 12, 4, jacket_overlay)
    paint(img, 40, 16, 3, 12, 4, arm_base)      # brazo derecho (slim)
    paint(img, 40, 32, 3, 12, 4, sleeve_overlay)
    paint(img, 32, 48, 3, 12, 4, arm_base)      # brazo izquierdo
    paint(img, 48, 48, 3, 12, 4, sleeve_overlay)
    paint(img, 0, 16, 4, 12, 4, leg_base)       # pierna derecha
    paint(img, 0, 32, 4, 12, 4, pants_overlay)
    paint(img, 16, 48, 4, 12, 4, leg_base)      # pierna izquierda
    paint(img, 0, 48, 4, 12, 4, pants_overlay)
    return img

def hair_texture():
    """Textura del cabello 3D: filas 0-47 mechones negros con brillo; 48-63 cofia blanca."""
    img = Image.new("RGBA", (64, 64), T)
    rnd = random.Random(11)
    for x in range(64):
        streak = rnd.choice([HAIR, HAIR, HAIR, HAIR_HI, HAIR_DK])
        for y in range(48):
            c = streak
            if (x * 7 + y) % 11 == 0: c = HAIR_HI
            if y % 12 in (10, 11) and x % 3 == 0: c = HAIR_DK
            img.putpixel((x, y), jitter(c, 4) + (255,))
    for x in range(64):
        for y in range(48, 56):   # diadema
            img.putpixel((x, y), (WHITE if (x + y) % 4 else WHITE_SH) + (255,))
        for y in range(56, 64):   # volante con encaje
            c = WHITE if (x % 3) else (236, 214, 220)
            if y % 2 == 0 and x % 4 == 1: c = PINK
            img.putpixel((x, y), c + (255,))
    return img

def contract():
    img = Image.new("RGBA", (16, 16), T)
    PAPER = (238, 222, 184); PAPER_SH = (214, 192, 148); INK = (92, 70, 56); GOLD = (232, 186, 52); GOLD_DK = (170, 120, 30)
    for y in range(1, 15):
        for x in range(3, 13):
            c = PAPER if (x + y) % 5 else PAPER_SH
            if x in (3, 12) or y in (1, 14): c = GOLD if (x + y) % 2 else GOLD_DK
            img.putpixel((x, y), c + (255,))
    for y in (4, 6, 8):
        for x in range(5, 11):
            if (x + y) % 4: img.putpixel((x, y), INK + (255,))
    heart = [(8, 10), (9, 10), (11, 10), (12, 10), (7, 11), (8, 11), (9, 11), (10, 11), (11, 11), (12, 11), (13, 11),
             (8, 12), (9, 12), (10, 12), (11, 12), (12, 12), (9, 13), (10, 13), (11, 13), (10, 14)]
    for (x, y) in heart:
        x2 = x - 1
        if 0 <= x2 < 16: img.putpixel((x2, y), (RED if (x + y) % 3 else PINK) + (255,))
    return img

os.makedirs(f"{OUT}/entity/tanny", exist_ok=True)
os.makedirs(f"{OUT}/item", exist_ok=True)
names = {"normal": "tanny", "blink": "tanny_blink", "happy": "tanny_happy",
         "surprised": "tanny_surprised", "focused": "tanny_focused", "tired": "tanny_tired"}
for expr, name in names.items():
    random.seed(7)  # misma textura base en todas las expresiones
    skin(expr).save(f"{OUT}/entity/tanny/{name}.png")
hair_texture().save(f"{OUT}/entity/tanny/hair.png")
contract().save(f"{OUT}/item/tanny_contract.png")
print("ok")
