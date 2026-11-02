"""Texturas de las fusiones (capa encima de la skin del jugador, formato 64x64 a 2x = 128x128).
Cada fusión pinta solo algunas zonas, así se mezcla la apariencia del mob con la del jugador.
Además genera la capa brillante (ojos/alma) y el arete. Uso: python3 tools/gen_fusion.py"""
import os, random
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src/main/resources/assets/blockdragonsuperz/textures/entity/fusion")
S = 2
T = (0, 0, 0, 0)
R = random.Random(4)


def faces(u, v, w, h, d):
    return {"top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d), "right": (u, v + d, d, h),
            "front": (u + d, v + d, w, h), "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h)}


PARTS = {  # nombre: (u, v, w, h, d)
    "head": (0, 0, 8, 8, 8), "body": (16, 16, 8, 12, 4), "rarm": (40, 16, 4, 12, 4), "larm": (32, 48, 4, 12, 4),
    "rleg": (0, 16, 4, 12, 4), "lleg": (16, 48, 4, 12, 4),
}


def paint(img, part, fn):
    u, v, w, h, d = PARTS[part]
    for f, (fx, fy, fw, fh) in faces(u, v, w, h, d).items():
        for y in range(fy * S, (fy + fh) * S):
            for x in range(fx * S, (fx + fw) * S):
                c = fn(f, (x - fx * S) / S, (y - fy * S) / S, fw, fh)
                if c:
                    img.putpixel((x, y), c if len(c) == 4 else tuple(max(0, min(255, k + R.randint(-6, 6))) for k in c) + (255,))


def all_parts(img, fn, parts=("head", "body", "rarm", "larm", "rleg", "lleg")):
    for p in parts:
        paint(img, p, lambda f, x, y, w, h, p=p: fn(p, f, x, y, w, h))


# ----------------------------------------------------------------- zombi
def zombie():
    G, GD, SH, PA = (78, 140, 60), (54, 104, 42), (0, 160, 160), (60, 50, 150)

    def fn(p, f, x, y, w, h):
        if p == "head":
            if f == "front":
                if x >= 4 or y >= 5:                        # media cara verde
                    if 5 <= x <= 6.9 and 3 <= y < 4:
                        return (12, 12, 14)                 # ojo hundido
                    if y >= 6 and 2 <= x < 6:
                        return (40, 30, 30)                 # boca
                    return G if (int(x * 2) + int(y * 2)) % 5 else GD
            if f == "left" and y >= 3:
                return G
            return None
        if p in ("rarm", "larm"):
            if y >= 4:
                return G if (int(x * 3) + int(y)) % 4 else GD   # antebrazos verdes
            return None
        if p == "body":
            if f in ("front", "back") and y >= 6:
                if (int(x * 2) * 7 + int(y * 2) * 3) % 9 < 3:
                    return G                                 # agujeros de la camisa rota
                return SH if (int(x) + int(y)) % 3 else (0, 130, 130)
            return None
        if p in ("rleg", "lleg") and y >= 7:
            return PA if (int(x * 2) + int(y)) % 4 else (44, 36, 120)
        return None
    return fn


# ----------------------------------------------------------------- esqueleto
def skeleton():
    B, BD, K = (226, 222, 210), (160, 156, 146), (20, 18, 20)

    def fn(p, f, x, y, w, h):
        if p == "head":
            if f == "front" and y >= 5:
                if y >= 6.5 and int(x * 2) % 2 == 0:
                    return BD                                 # dientes
                if 3.5 <= x < 4.5 and 5 <= y < 6:
                    return K                                  # nariz
                return B
            if f == "front" and x >= 4.5 and 2.5 <= y < 4.5:
                return K if 5 <= x < 7 else B                 # cuenca de un ojo
            return None
        if p == "body":
            if f == "front" and 1 <= y < 9:
                if 3.5 <= x < 4.5:
                    return B                                  # esternón
                if int(y * 2) % 3 == 0 and 1 <= x < 7:
                    return B                                  # costillas
                return None
            if f == "back" and 3.5 <= x < 4.5:
                return BD                                     # columna
            return None
        if p in ("rarm", "larm", "rleg", "lleg"):
            if f in ("front", "back") and 1.5 <= x < 2.5:
                return B if int(y) % 4 else BD                # hueso
            if f in ("left", "right") and 1.5 <= x < 2.5 and y >= 3:
                return BD
            return None
        return None
    return fn


# ----------------------------------------------------------------- enderman
def enderman():
    K, KD = (22, 18, 26), (36, 30, 44)

    def fn(p, f, x, y, w, h):
        if p in ("rarm", "larm", "rleg", "lleg"):
            return K if (int(x * 2) + int(y * 2)) % 6 else KD
        if p == "body":
            if f in ("left", "right") or x < 2 or x >= 6:
                return K
            return None
        if p == "head":
            if f == "front":
                if y < 3 or x < 1 or x >= 7:
                    return K
                if 3 <= y < 4:
                    return (60, 10, 70)                       # bajo los ojos morados (brillan aparte)
                return None
            if f in ("left", "right", "back", "top"):
                return K if y < 5 or f != "top" else None
        return None
    return fn


# ----------------------------------------------------------------- creeper
def creeper():
    CS = [(13, 138, 30), (78, 203, 62), (40, 170, 40), (150, 230, 140), (30, 110, 30)]

    def camo(x, y):
        return CS[(int(x * 2) * 7 + int(y * 2) * 13 + int(x * 2) * int(y * 2)) % 5]

    def fn(p, f, x, y, w, h):
        if p == "body":
            if f == "front" and 2 <= y < 7 and 1 <= x < 7:
                # cara de creeper en el pecho
                if (1 <= x < 3 or 5 <= x < 7) and 2 <= y < 4:
                    return (10, 10, 10)
                if 3 <= x < 5 and 3.5 <= y < 7:
                    return (10, 10, 10)
                if (2 <= x < 3 or 5 <= x < 6) and 5 <= y < 7:
                    return (10, 10, 10)
            return camo(x, y)
        if p in ("rarm", "larm"):
            return camo(x, y) if y >= 2 else None
        if p in ("rleg", "lleg"):
            return camo(x, y) if y < 9 else (20, 70, 20)
        if p == "head" and f in ("top", "back", "left", "right"):
            return camo(x, y) if f == "top" or y < 3 else None
        return None
    return fn


# ----------------------------------------------------------------- gólem
def golem():
    I, ID, V = (206, 198, 184), (150, 142, 130), (66, 140, 50)

    def iron(x, y):
        if (int(x * 2) + int(y * 3)) % 11 == 0:
            return ID                                         # grietas
        return I if int(y) % 4 else ID

    def fn(p, f, x, y, w, h):
        vine = (int(x * 2) * 3 + int(y * 2)) % 13 < 2
        if p in ("rarm", "larm"):
            return V if vine else iron(x, y)
        if p == "body":
            if f == "front" and 2.5 <= x < 5.5 and y < 3:
                return None                                   # cuello
            return V if vine else iron(x, y)
        if p in ("rleg", "lleg"):
            return V if vine else (iron(x, y) if y < 10 else ID)
        if p == "head" and f == "front":
            if 3.5 <= x < 4.5 and 4 <= y < 7:
                return (176, 160, 140)                        # nariz de gólem
            if 1 <= x < 7 and 2 <= y < 2.8:
                return (60, 50, 44)                           # ceja
            return None
        return None
    return fn


# ----------------------------------------------------------------- warden
def warden():
    W, WD, WL = (16, 58, 64), (8, 34, 40), (40, 110, 110)

    def fn(p, f, x, y, w, h):
        vein = (int(x * 2) * 5 + int(y * 2) * 3) % 17 == 0
        if p in ("rarm", "larm", "rleg", "lleg", "body"):
            if p == "body" and f == "front" and 2 <= x < 6 and 2 <= y < 7:
                return (20, 70, 76)                           # pecho (el alma brilla aparte)
            return WL if vein else (W if (int(x) + int(y)) % 3 else WD)
        if p == "head":
            if f == "front":
                if y < 2.5 or x < 1 or x >= 7:
                    return W
                if y >= 6.5:
                    return (200, 210, 200) if int(x * 2) % 2 == 0 else WD    # dientes
                return None
            return W
        return None
    return fn


# ----------------------------------------------------------------- capas brillantes
def glow_enderman(p, f, x, y, w, h):
    if p == "head" and f == "front" and 2.5 <= y < 3.5 and (1 <= x < 3.5 or 4.5 <= x < 7):
        return (230, 120, 255, 255)
    return None


def glow_warden(p, f, x, y, w, h):
    if p == "body" and f == "front" and 2.5 <= x < 5.5 and 3 <= y < 6:
        return (60, 255, 240, 255)
    if p == "head" and f in ("left", "right") and 1 <= y < 3:
        return (40, 220, 210, 255)                            # zarcillos que brillan
    return None


def glow_golem(p, f, x, y, w, h):
    if p == "head" and f == "front" and 3 <= y < 3.8 and (2 <= x < 3 or 5 <= x < 6):
        return (230, 60, 40, 255)
    return None


def glow_creeper(p, f, x, y, w, h):
    return None


def main():
    os.makedirs(OUT, exist_ok=True)
    for name, fn in (("zombie", zombie()), ("skeleton", skeleton()), ("enderman", enderman()),
                     ("creeper", creeper()), ("golem", golem()), ("warden", warden())):
        img = Image.new("RGBA", (64 * S, 64 * S), T)
        all_parts(img, fn)
        # la capa izquierda usa las regiones de la derecha reflejadas
        img.save(os.path.join(OUT, name + ".png"))
    for name, fn in (("enderman", glow_enderman), ("warden", glow_warden), ("golem", glow_golem),
                     ("zombie", glow_creeper), ("skeleton", glow_creeper), ("creeper", glow_creeper)):
        img = Image.new("RGBA", (64 * S, 64 * S), T)
        all_parts(img, fn)
        img.save(os.path.join(OUT, name + "_glow.png"))
    ear = Image.new("RGBA", (16, 16), T)
    for y in range(16):
        for x in range(16):
            ear.putpixel((x, y), (246, 196, 60, 255) if (x + y) % 4 else (255, 236, 150, 255))
    ear.save(os.path.join(OUT, "earring.png"))
    print("fusiones ok")


if __name__ == "__main__":
    main()
