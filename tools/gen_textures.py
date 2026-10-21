"""Texturas: aura (remolino de energía), núcleo de ki, semilla del ermitaño y su planta."""
import os, random, math
from PIL import Image
R = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A = os.path.join(R, "src/main/resources/assets/blockdragonsuperz/textures")
os.makedirs(A + "/entity", exist_ok=True); os.makedirs(A + "/item", exist_ok=True); os.makedirs(A + "/block", exist_ok=True)
rnd = random.Random(4)

# aura: vetas verticales suaves (energySwirl la desplaza hacia arriba)
aura = Image.new("RGBA", (64, 32))
cols = [rnd.random() for _ in range(64)]
for x in range(64):
    for y in range(32):
        v = 0.5 + 0.5 * math.sin(x * 0.7 + cols[x] * 6) * math.sin(y * 0.4 + cols[(x + 7) % 64] * 6)
        v = max(0.0, v) ** 1.6
        c = int(40 + 215 * v)
        aura.putpixel((x, y), (c, c, c, 255))
aura.save(A + "/entity/aura.png")

core = Image.new("RGBA", (64, 32))
for x in range(64):
    for y in range(32):
        c = 235 + rnd.randint(0, 20)
        core.putpixel((x, y), (c, c, c, 200))
core.save(A + "/entity/ki_core.png")

def px(img, pts, col):
    for (x, y) in pts:
        img.putpixel((x, y), col + (255,))

# semilla del ermitaño
bean = Image.new("RGBA", (16, 16))
G, GL, GD = (92, 168, 64), (150, 214, 96), (52, 112, 40)
shape = ["................",
         "................",
         "................",
         "........dd......",
         "......dgggd.....",
         ".....dgLLggd....",
         "....dgLLgggd....",
         "....dgLggggd....",
         "...dggggggdd....",
         "...dgggggdd.....",
         "...dggggdd......",
         "....dddd........",
         "................",
         "................",
         "................",
         "................"]
for y, row in enumerate(shape):
    for x, ch in enumerate(row):
        if ch == "g": bean.putpixel((x, y), G + (255,))
        elif ch == "L": bean.putpixel((x, y), GL + (255,))
        elif ch == "d": bean.putpixel((x, y), GD + (255,))
bean.save(A + "/item/senzu_bean.png")

# planta: 4 etapas
STEM, LEAF, LEAF2, POD = (60, 120, 40), (80, 160, 52), (110, 190, 70), (150, 214, 96)
for stage in range(4):
    img = Image.new("RGBA", (16, 16))
    h = 4 + stage * 3
    for col in (4, 8, 11):
        for y in range(16 - h, 16):
            img.putpixel((col, y), STEM + (255,))
        for i in range(stage + 1):
            ly = 16 - h + 1 + i * 3
            if ly < 15:
                for dx in (-2, -1, 1, 2):
                    xx = col + dx
                    if 0 <= xx < 16 and abs(dx) <= 1 + (i % 2):
                        img.putpixel((xx, ly), (LEAF if dx < 0 else LEAF2) + (255,))
    if stage == 3:
        for (x, y) in [(3, 6), (7, 4), (10, 7), (5, 9), (12, 5)]:
            img.putpixel((x, y), POD + (255,)); img.putpixel((x + 1, y), POD + (255,)); img.putpixel((x, y + 1), (92, 168, 64, 255))
    img.save(A + "/block/senzu_crop_stage%d.png" % stage)
print("ok")
