"""Mineral de Ki, lingotes y dos armaduras ORIGINALES (Traje del Guerrero naranja / Armadura de Combate azul)."""
import os, sys, json, random, math
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import modelgen_base as mg
from modelgen_base import Box, paint_box
from PIL import Image, ImageDraw

R = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A = R + "/src/main/resources/assets/blockdragonsuperz"
D = R + "/src/main/resources/data/blockdragonsuperz"
T = (0, 0, 0, 0)
rnd = random.Random(21)
for d in [A + "/textures/block", A + "/textures/item", A + "/textures/models/armor", A + "/blockstates", A + "/models/block",
          A + "/models/item", D + "/recipes", D + "/loot_tables/blocks", D + "/worldgen/configured_feature", D + "/worldgen/placed_feature",
          D + "/forge/biome_modifier", R + "/src/main/resources/data/minecraft/tags/blocks/mineable"]:
    os.makedirs(d, exist_ok=True)

def jit(c, a=8):
    return tuple(max(0, min(255, v + rnd.randint(-a, a))) for v in c)

# ---------------------------------------------------------------- mineral
def ore(base, name):
    img = Image.new("RGBA", (16, 16))
    for x in range(16):
        for y in range(16):
            img.putpixel((x, y), jit(base, 10) + (255,))
    for cx, cy in [(4, 4), (11, 6), (6, 11), (12, 12)]:
        for dx, dy in [(0, 0), (1, 0), (0, 1), (-1, 0), (0, -1), (1, 1)]:
            x, y = cx + dx, cy + dy
            if 0 <= x < 16 and 0 <= y < 16:
                img.putpixel((x, y), ((26, 22, 34) if (dx, dy) != (0, 0) else (150, 90, 255)) + (255,))
    img.save(f"{A}/textures/block/{name}.png")
ore((125, 125, 125), "ki_ore")
ore((72, 72, 78), "deepslate_ki_ore")

def ingot(main, dark, light, name):
    shape = ["................", "................", "................", "................",
             "...........LL...", ".........LmmmD..", ".......LmmmmmD..", ".....LmmmmmmD...",
             "...LmmmmmmmD....", "..LmmmmmmmD.....", "..DmmmmmmD......", "...DDmmmD.......",
             ".....DDD........", "................", "................", "................"]
    img = Image.new("RGBA", (16, 16))
    for y, row in enumerate(shape):
        for x, ch in enumerate(row):
            c = {"m": main, "D": dark, "L": light}.get(ch)
            if c: img.putpixel((x, y), c + (255,))
    img.save(f"{A}/textures/item/{name}.png")
ingot((40, 38, 46), (18, 16, 22), (90, 80, 120), "black_ki_ingot")
ingot((240, 128, 30), (170, 76, 14), (255, 200, 120), "orange_ki_ingot")
ingot((40, 90, 210), (20, 48, 130), (120, 180, 255), "blue_ki_ingot")

# ---------------------------------------------------------------- armaduras (diseños originales)
SETS = {
    "warrior": {"base": (238, 124, 30), "base2": (214, 102, 20), "accent": (28, 28, 32), "accent2": (60, 60, 66), "trim": (252, 160, 70)},
    "battle": {"base": (34, 70, 190), "base2": (26, 54, 150), "accent": (196, 200, 212), "accent2": (240, 242, 248), "trim": (150, 156, 170)},
}

def region(img, u, v, w, h, d, fn):
    b = Box(0, 0, 0, w, h, d, fn)
    b.u, b.v = u, v
    paint_box(img, b)

def fold(c, x, y):
    """Pliegues de tela."""
    if (x * 3 + y * 5) % 11 == 0:
        return tuple(max(0, v - 18) for v in c)
    return jit(c, 5)

def gi_layer1(c):
    img = Image.new("RGBA", (64, 32), T)
    base, base2, belt, belt2, light = c["base"], c["base2"], c["accent"], c["accent2"], c["trim"]
    def head(f, x, y, w, h):              # cinta de karate (hachimaki) con nudo atrás
        if f in ("top", "bottom"):
            return None
        if y == 2:
            return belt
        if f == "back" and y in (3, 4, 5) and x in (3, 4):
            return belt2                  # puntas del nudo
        return None
    region(img, 0, 0, 8, 8, 8, head)
    def body(f, x, y, w, h):              # chaqueta del gi con cuello cruzado y cinturón negro
        if f == "top":
            return base
        if f == "bottom":
            return None
        if y in (8, 9):
            if f == "front" and x in (3, 4):
                return belt2              # nudo del cinturón
            return belt
        if f == "front" and y in (10, 11) and x in (2, 5):
            return belt                   # puntas del cinturón colgando
        if f == "front":
            # cuello cruzado en V: el pecho queda descubierto (se ve la piel)
            lapel_l = 3 - y // 2
            lapel_r = 4 + y // 2
            if y < 6 and lapel_l < x < lapel_r:
                return None
            if y < 7 and (x == lapel_l or x == lapel_r):
                return base2              # borde del cuello
        if f == "back" and x in (3, 4) and y < 8:
            return base2                  # costura central
        return fold(base, x, y)
    region(img, 16, 16, 8, 12, 4, body)
    def arm(f, x, y, w, h):               # mangas hasta el codo, antebrazos libres
        if f == "top":
            return base
        if y <= 5:
            return fold(base, x, y)
        if y == 6:
            return base2                  # puño doblado
        return None
    region(img, 40, 16, 4, 12, 4, arm)
    def shoe(f, x, y, w, h):              # zapatillas negras de artes marciales
        if f == "bottom":
            return (14, 14, 16)
        if y < 10:
            return None
        return belt if y == 11 or f != "front" else belt2
    region(img, 0, 16, 4, 12, 4, shoe)
    return img

def gi_layer2(c):
    img = Image.new("RGBA", (64, 32), T)
    base, base2 = c["base"], c["base2"]
    def legs(f, x, y, w, h):              # pantalón ancho del gi
        if f == "top":
            return base
        if f == "bottom" or y >= 10:
            return None
        if y == 9:
            return base2                  # dobladillo
        return fold(base, x, y)
    region(img, 0, 16, 4, 12, 4, legs)
    def waist(f, x, y, w, h):
        if y < 10:
            return None
        return fold(base, x, y)
    region(img, 16, 16, 8, 12, 4, waist)
    return img

def suit_layer1(c):
    img = Image.new("RGBA", (64, 32), T)
    base, base2, silver, white, gray = c["base"], c["base2"], c["accent"], c["accent2"], c["trim"]
    def head(f, x, y, w, h):              # diadema plateada con gema azul
        if f in ("top", "bottom"):
            return None
        if y == 1:
            if f == "front" and x in (3, 4):
                return base
            return silver
        return None
    region(img, 0, 0, 8, 8, 8, head)
    def body(f, x, y, w, h):              # uniforme azul completo con cuello alto y costuras plateadas
        if f == "top":
            return base
        if f == "bottom":
            return base2
        if y == 0:
            return silver                 # cuello alto
        if y == 9:
            return gray if not (f == "front" and x in (3, 4)) else white   # cinturón con hebilla
        if f in ("left", "right") and x == 1:
            return silver                 # costura lateral
        if f == "front" and y in (1, 2) and x in (2, 5):
            return base2
        return jit(base, 5)
    region(img, 16, 16, 8, 12, 4, body)
    def arm(f, x, y, w, h):               # mangas largas hasta la muñeca
        if f == "top":
            return base
        if f in ("left", "right") and x == 1 and y < 10:
            return silver
        if y == 10:
            return silver
        if y == 11:
            return None
        return jit(base, 5)
    region(img, 40, 16, 4, 12, 4, arm)
    def boot(f, x, y, w, h):              # botas blancas
        if f == "bottom":
            return (200, 202, 210)
        if y < 6:
            return None
        if y == 6:
            return gray                   # borde superior de la bota
        if y == 11:
            return (205, 208, 216)        # suela
        return white if (x + y) % 6 else (226, 228, 236)
    region(img, 0, 16, 4, 12, 4, boot)
    return img

def suit_layer2(c):
    img = Image.new("RGBA", (64, 32), T)
    base, base2, silver = c["base"], c["base2"], c["accent"]
    def legs(f, x, y, w, h):
        if f == "top":
            return base
        if f == "bottom" or y >= 7:
            return None                   # debajo van las botas blancas
        if f in ("left", "right") and x == 1:
            return silver
        return jit(base, 5)
    region(img, 0, 16, 4, 12, 4, legs)
    def waist(f, x, y, w, h):
        if y < 10:
            return None
        return jit(base, 5)
    region(img, 16, 16, 8, 12, 4, waist)
    return img

gi_layer1(SETS["warrior"]).save(f"{A}/textures/models/armor/warrior_layer_1.png")
gi_layer2(SETS["warrior"]).save(f"{A}/textures/models/armor/warrior_layer_2.png")
suit_layer1(SETS["battle"]).save(f"{A}/textures/models/armor/battle_layer_1.png")
suit_layer2(SETS["battle"]).save(f"{A}/textures/models/armor/battle_layer_2.png")

# íconos 16x16
ICONS = {
    "warrior": {
        "helmet": ["................", "................", "................", "................", "................",
                   "..kkkkkkkkkkkk..", "..kkkkkkkkkkkk..", "............kk..", ".............kk.", "..............k.",
                   "................", "................", "................", "................", "................", "................"],
        "chestplate": ["................", "..oooo....oooo..", ".oooooO..Ooooooo", ".ooooooO.Oooooo.", "..oooooOOoooooo.",
                       "...oooooOooooo..", "...oooooooooooo.", "...oooooooooooo.", "...oooooooooooo.", "...kkkkkkkkkkkk.",
                       "...ooooookoooooo", "...oooooo.kooooo", "...ooooo...kooo.", "................", "................", "................"],
        "leggings": ["................", "...ooooooooooo..", "...ooooooooooo..", "...ooooooooooo..", "...oooo..ooooo..",
                     "...oooo..oooo...", "...oooo..oooo...", "...oooo..oooo...", "...oooo..oooo...", "...oooo..oooo...",
                     "...OOOO..OOOO...", "................", "................", "................", "................", "................"],
        "boots": ["................", "................", "................", "................", "................",
                  "................", "................", "................", "...kkk....kkk...", "...kkk....kkk...",
                  "..kkkkk..kkkkk..", "..KKKKK..KKKKK..", "................", "................", "................", "................"],
    },
    "battle": {
        "helmet": ["................", "................", "................", "................", "................",
                   "..ssssssssssss..", "..sssssbbsssss..", "................", "................", "................",
                   "................", "................", "................", "................", "................", "................"],
        "chestplate": ["................", "..bbbbssssbbbb..", ".bbbbbbbbbbbbbb.", ".bbbbbbbbbbbbbb.", "..bbbbbbbbbbbb..",
                       "..sbbbbbbbbbbs..", "..sbbbbbbbbbbs..", "...bbbbbbbbbb...", "...bbbbbbbbbb...", "...ggggwwgggg...",
                       "...bbbbbbbbbb...", "...bbbbbbbbbb...", "................", "................", "................", "................"],
        "leggings": ["................", "...bbbbbbbbbbb..", "...bbbbbbbbbbb..", "...bbbbbbbbbbb..", "...sbbb..bbbs...",
                     "...sbbb..bbbs...", "...sbbb..bbbs...", "...sbbb..bbbs...", "...sbbb..bbbs...", "...bbbb..bbbb...",
                     "................", "................", "................", "................", "................", "................"],
        "boots": ["................", "................", "................", "................", "................",
                  "...ggg....ggg...", "...www....www...", "...www....www...", "...www....www...", "...www....www...",
                  "..wwwww..wwwww..", "..GGGGG..GGGGG..", "................", "................", "................", "................"],
    },
}
PAL = {"o": (238, 124, 30), "O": (200, 96, 18), "k": (28, 28, 32), "K": (12, 12, 14),
       "b": (34, 70, 190), "s": (196, 200, 212), "g": (150, 156, 170), "w": (240, 242, 248), "G": (200, 202, 210)}
for kind, pieces in ICONS.items():
    for piece, rows in pieces.items():
        img = Image.new("RGBA", (16, 16))
        for y, row in enumerate(rows):
            for x, ch in enumerate(row):
                col = PAL.get(ch)
                if col: img.putpixel((x, y), col + (255,))
        img.save(f"{A}/textures/item/{kind}_{piece}.png")

# ---------------------------------------------------------------- datos
def jdump(obj, path):
    json.dump(obj, open(path, "w"), indent=2, ensure_ascii=False)

for b in ["ki_ore", "deepslate_ki_ore"]:
    jdump({"variants": {"": {"model": f"blockdragonsuperz:block/{b}"}}}, f"{A}/blockstates/{b}.json")
    jdump({"parent": "minecraft:block/cube_all", "textures": {"all": f"blockdragonsuperz:block/{b}"}}, f"{A}/models/block/{b}.json")
    jdump({"parent": f"blockdragonsuperz:block/{b}"}, f"{A}/models/item/{b}.json")
    jdump({"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"blockdragonsuperz:{b}"}],
                                                   "conditions": [{"condition": "minecraft:survives_explosion"}]}]}, f"{D}/loot_tables/blocks/{b}.json")
    for kind, cook in [("smelting", 200), ("blasting", 100)]:
        jdump({"type": f"minecraft:{kind}", "ingredient": {"item": f"blockdragonsuperz:{b}"}, "result": "blockdragonsuperz:black_ki_ingot",
               "experience": 1.0, "cookingtime": cook}, f"{D}/recipes/black_ki_ingot_from_{kind}_{b}.json")
jdump({"replace": False, "values": ["blockdragonsuperz:ki_ore", "blockdragonsuperz:deepslate_ki_ore"]},
      R + "/src/main/resources/data/minecraft/tags/blocks/mineable/pickaxe.json")
jdump({"replace": False, "values": ["blockdragonsuperz:ki_ore", "blockdragonsuperz:deepslate_ki_ore"]},
      R + "/src/main/resources/data/minecraft/tags/blocks/needs_iron_tool.json")
for color in ["orange", "blue"]:
    jdump({"type": "minecraft:crafting_shapeless", "ingredients": [{"item": "blockdragonsuperz:black_ki_ingot"}, {"item": f"minecraft:{color}_dye"}],
           "result": {"item": f"blockdragonsuperz:{color}_ki_ingot", "count": 1}}, f"{D}/recipes/{color}_ki_ingot.json")
for kind, ing in [("warrior", "orange_ki_ingot"), ("battle", "blue_ki_ingot")]:
    for piece in ["helmet", "chestplate", "leggings", "boots"]:
        jdump({"type": "minecraft:crafting_shaped", "pattern": ["III", "IAI", "III"],
               "key": {"I": {"item": f"blockdragonsuperz:{ing}"}, "A": {"item": f"minecraft:diamond_{piece}"}},
               "result": {"item": f"blockdragonsuperz:{kind}_{piece}", "count": 1}}, f"{D}/recipes/{kind}_{piece}.json")
        jdump({"parent": "minecraft:item/generated", "textures": {"layer0": f"blockdragonsuperz:item/{kind}_{piece}"}}, f"{A}/models/item/{kind}_{piece}.json")
for it in ["black_ki_ingot", "orange_ki_ingot", "blue_ki_ingot"]:
    jdump({"parent": "minecraft:item/generated", "textures": {"layer0": f"blockdragonsuperz:item/{it}"}}, f"{A}/models/item/{it}.json")
# generación: igual que el diamante (y -64 a 16), más común cuanto más profundo
jdump({"type": "minecraft:ore", "config": {"size": 5, "discard_chance_on_air_exposure": 0.4, "targets": [
    {"target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:stone_ore_replaceables"}, "state": {"Name": "blockdragonsuperz:ki_ore"}},
    {"target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:deepslate_ore_replaceables"}, "state": {"Name": "blockdragonsuperz:deepslate_ki_ore"}}]}},
    f"{D}/worldgen/configured_feature/ki_ore.json")
jdump({"feature": "blockdragonsuperz:ki_ore", "placement": [
    {"type": "minecraft:count", "count": 6}, {"type": "minecraft:in_square"},
    {"type": "minecraft:height_range", "height": {"type": "minecraft:trapezoid", "min_inclusive": {"absolute": -80}, "max_inclusive": {"absolute": 16}}},
    {"type": "minecraft:biome"}]}, f"{D}/worldgen/placed_feature/ki_ore.json")
jdump({"type": "forge:add_features", "biomes": "#minecraft:is_overworld", "features": "blockdragonsuperz:ki_ore", "step": "underground_ores"},
      f"{D}/forge/biome_modifier/ki_ore.json")

names_es = {"block.blockdragonsuperz.ki_ore": "Mineral de Ki", "block.blockdragonsuperz.deepslate_ki_ore": "Mineral de Ki de pizarra profunda",
            "item.blockdragonsuperz.black_ki_ingot": "Lingote de Ki Negro", "item.blockdragonsuperz.orange_ki_ingot": "Lingote de Ki Naranja",
            "item.blockdragonsuperz.blue_ki_ingot": "Lingote de Ki Azul",
            "item.blockdragonsuperz.warrior_helmet": "Cinta de Karate", "item.blockdragonsuperz.warrior_chestplate": "Gi de Karate",
            "item.blockdragonsuperz.warrior_leggings": "Pantalón de Karate", "item.blockdragonsuperz.warrior_boots": "Zapatillas de Karate",
            "item.blockdragonsuperz.battle_helmet": "Diadema del Uniforme", "item.blockdragonsuperz.battle_chestplate": "Uniforme Azul",
            "item.blockdragonsuperz.battle_leggings": "Pantalón del Uniforme Azul", "item.blockdragonsuperz.battle_boots": "Botas Blancas"}
names_en = {"block.blockdragonsuperz.ki_ore": "Ki Ore", "block.blockdragonsuperz.deepslate_ki_ore": "Deepslate Ki Ore",
            "item.blockdragonsuperz.black_ki_ingot": "Black Ki Ingot", "item.blockdragonsuperz.orange_ki_ingot": "Orange Ki Ingot",
            "item.blockdragonsuperz.blue_ki_ingot": "Blue Ki Ingot",
            "item.blockdragonsuperz.warrior_helmet": "Karate Headband", "item.blockdragonsuperz.warrior_chestplate": "Karate Gi",
            "item.blockdragonsuperz.warrior_leggings": "Karate Pants", "item.blockdragonsuperz.warrior_boots": "Karate Shoes",
            "item.blockdragonsuperz.battle_helmet": "Uniform Headband", "item.blockdragonsuperz.battle_chestplate": "Blue Uniform",
            "item.blockdragonsuperz.battle_leggings": "Blue Uniform Pants", "item.blockdragonsuperz.battle_boots": "White Boots"}
import glob
L = A + "/lang/"
d = json.load(open(L + "en_us.json")); d.update(names_en); jdump(d, L + "en_us.json")
for f in glob.glob(L + "es_*.json"):
    d = json.load(open(f)); d.update(names_es); jdump(d, f)

# ---------------------------------------------------------------- vista previa 3D
def preview(out):
    skin = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    def fill(u, v, w, h, d, col):
        b = Box(0, 0, 0, w, h, d, lambda f, x, y, ww, hh: col); b.u, b.v = u, v; paint_box(skin, b)
    fill(0, 0, 8, 8, 8, (200, 160, 130)); fill(16, 16, 8, 12, 4, (200, 160, 130)); fill(40, 16, 3, 12, 4, (200, 160, 130))
    fill(0, 16, 4, 12, 4, (90, 70, 60))
    W = 300
    img = Image.new("RGB", (W * 4, 360), (150, 160, 180)); dr = ImageDraw.Draw(img)
    for k, (kind, yaw) in enumerate([("warrior", math.pi), ("warrior", -0.6), ("battle", math.pi), ("battle", -0.6)]):
        l1 = Image.open(f"{A}/textures/models/armor/{kind}_layer_1.png").convert("RGBA")
        l2 = Image.open(f"{A}/textures/models/armor/{kind}_layer_2.png").convert("RGBA")
        quads = []
        parts = [((0, 0, 0), skin, (0, 0), (-4, -8, -4, 8, 8, 8), 0), ((0, 0, 0), skin, (16, 16), (-4, 0, -2, 8, 12, 4), 0),
                 ((-5, 2, 0), skin, (40, 16), (-3, -2, -2, 4, 12, 4), 0), ((5, 2, 0), skin, (40, 16), (-1, -2, -2, 4, 12, 4), 0),
                 ((-1.9, 12, 0), skin, (0, 16), (-2, 0, -2, 4, 12, 4), 0), ((1.9, 12, 0), skin, (0, 16), (-2, 0, -2, 4, 12, 4), 0),
                 ((0, 0, 0), l1, (0, 0), (-4, -8, -4, 8, 8, 8), 1.0), ((0, 0, 0), l1, (16, 16), (-4, 0, -2, 8, 12, 4), 1.0),
                 ((-5, 2, 0), l1, (40, 16), (-3, -2, -2, 4, 12, 4), 1.0), ((5, 2, 0), l1, (40, 16), (-1, -2, -2, 4, 12, 4), 1.0),
                 ((-1.9, 12, 0), l1, (0, 16), (-2, 0, -2, 4, 12, 4), 1.0), ((1.9, 12, 0), l1, (0, 16), (-2, 0, -2, 4, 12, 4), 1.0),
                 ((0, 0, 0), l2, (16, 16), (-4, 0, -2, 8, 12, 4), 0.5),
                 ((-1.9, 12, 0), l2, (0, 16), (-2, 0, -2, 4, 12, 4), 0.5), ((1.9, 12, 0), l2, (0, 16), (-2, 0, -2, 4, 12, 4), 0.5)]
        for piv, tex, (u, v), bx, infl in parts:
            quads += mg.quads_for(tex, *bx, u, v, [(piv, (0, 0, 0))], infl)
        proj = []
        for q, col in quads:
            pts, dep = [], 0
            for (x, y, z) in q:
                X = x * math.cos(yaw) - z * math.sin(yaw); Z = x * math.sin(yaw) + z * math.cos(yaw)
                pts.append((W * k + W / 2 - X * 9, 110 + y * 9)); dep += Z
            proj.append((dep / 4, pts, col))
        proj.sort(key=lambda t: t[0])
        for _, pts, col in proj: dr.polygon(pts, fill=col)
    dr.text((10, 340), "Gi de Karate (frente / espalda)", fill=(255, 255, 255))
    dr.text((W * 2 + 10, 340), "Uniforme Azul (frente / espalda)", fill=(255, 255, 255))
    img.save(out)

if len(sys.argv) > 1:
    preview(sys.argv[1] + "/bdsz_armor_preview.png")
print("ok")
