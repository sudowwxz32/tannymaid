"""Texturas: pelaje rojo de la Fase 4 (brazos, costados y espalda; el pecho queda libre), brazaletes azules,
cola roja y la textura de fuego del aura."""
import os, random, math
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
E = os.path.join(ROOT, "src/main/resources/assets/blockdragonsuperz/textures/entity")
R = random.Random(4)
RED, RED_HI, RED_DK = (196, 38, 40), (228, 72, 62), (140, 22, 26)
BLUE, BLUE_HI, BLUE_DK = (40, 70, 200), (80, 120, 236), (24, 40, 130)
T = (0, 0, 0, 0)


def fur(x, y):
    k = (x * 7 + y * 5 + R.randint(0, 2)) % 7
    c = RED_HI if k == 0 else RED_DK if k in (3, 5) else RED
    return tuple(max(0, min(255, v + R.randint(-10, 10))) for v in c) + (255,)


def fill(img, x0, y0, w, h, fn):
    for y in range(y0, y0 + h):
        for x in range(x0, x0 + w):
            c = fn(x - x0, y - y0, w, h)
            if c is not None:
                img.putpixel((x, y), c)


def arm(img, u, v, w):
    """Brazo (ancho w): pelaje arriba, brazalete azul en la muñeca, mano libre."""
    d = 4
    def side(x, y, ww, hh):
        if y >= 11:
            return None                                   # mano
        if y in (9, 10):
            c = BLUE_HI if y == 9 else BLUE if x % 2 else BLUE_DK
            return c + (255,)
        if y == 8 and R.random() < 0.4:
            return None                                   # borde del pelaje irregular
        return fur(x, y)
    fill(img, u + d, v, w, d, lambda x, y, ww, hh: fur(x, y))                 # arriba (hombro)
    fill(img, u, v + d, d, 12, side)                                          # derecha
    fill(img, u + d, v + d, w, 12, side)                                      # frente
    fill(img, u + d + w, v + d, d, 12, side)                                  # izquierda
    fill(img, u + d + w + d, v + d, w, 12, side)                              # atrás


def body(img):
    u, v = 16, 16
    fill(img, u + 4, v, 8, 4, lambda x, y, w, h: fur(x, y) if (x in (0, 1, 6, 7) or y >= 2) else None)   # hombros
    def front(x, y, w, h):
        # el pecho queda libre: solo un poco de pelo en los costados
        if y >= 10:
            return None
        if x == 0 or x == 7:
            return fur(x, y) if y >= 1 else None
        if (x == 1 or x == 6) and 2 <= y <= 8 and R.random() < 0.5:
            return fur(x, y)
        return None
    def sidef(x, y, w, h):
        return None if y >= 10 else fur(x, y)
    def back(x, y, w, h):
        return None if y >= 10 else fur(x, y)
    fill(img, u, v + 4, 4, 12, sidef)
    fill(img, u + 4, v + 4, 8, 12, front)
    fill(img, u + 12, v + 4, 4, 12, sidef)
    fill(img, u + 16, v + 4, 8, 12, back)


def skin_tex(slim):
    img = Image.new("RGBA", (64, 64), T)
    body(img)
    w = 3 if slim else 4
    arm(img, 40, 16, w)
    arm(img, 32, 48, w)
    img.save(os.path.join(E, "ssj4_fur_slim.png" if slim else "ssj4_fur.png"))


def tail():
    img = Image.new("RGBA", (32, 32))
    for y in range(32):
        for x in range(32):
            img.putpixel((x, y), fur(x, y))
    img.save(os.path.join(E, "tail.png"))


def fire():
    """Fuego del aura: lenguas verticales suaves que se repiten de arriba a abajo (se desplazan hacia arriba)."""
    W, H = 64, 64
    img = Image.new("RGBA", (W, H))
    for y in range(H):
        for x in range(W):
            a = 2 * math.pi * x / W
            b = 2 * math.pi * y / H
            v = 0.5 + 0.22 * math.sin(3 * a + b) + 0.16 * math.sin(7 * a - 2 * b + 1.3) + 0.1 * math.sin(13 * a + 3 * b + 0.4)
            v += 0.12 * math.sin(5 * a + 4 * b) * math.sin(2 * b)
            v = max(0.0, min(1.0, v)) ** 1.6
            c = int(40 + 215 * v)
            img.putpixel((x, y), (c, c, c, 255))
    img.save(os.path.join(E, "aura_fire.png"))


if __name__ == "__main__":
    skin_tex(False)
    skin_tex(True)
    tail()
    fire()
    print("ok")
