"""Texturas 16x16 de los objetos de Laury."""
import os
from PIL import Image
OUT = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src/main/resources/assets/laury/textures/item")
os.makedirs(OUT, exist_ok=True)

def img(rows, pal):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != "." and ch in pal:
                im.putpixel((x, y), pal[ch] + (255,))
    return im

HANDLE = {"h": (92, 60, 34), "H": (60, 38, 20)}

horn = ["................",
        "..........bB....",
        ".........bBB....",
        "........bBBl....",
        ".......bBBl.....",
        "......bBBl......",
        ".....bBBl.......",
        "....bBBl........",
        "...bBBl.........",
        "..gGGl..........",
        ".gGGgl..........",
        ".gyGg...........",
        "..gg............",
        "................",
        "................",
        "................"]
img(horn, {"b": (40, 60, 150), "B": (28, 44, 120), "l": (70, 100, 200), "g": (230, 190, 60), "G": (200, 150, 40), "y": (255, 240, 150)}).save(f"{OUT}/laury_summon_horn.png")

fire = ["..............oO",
        ".............oYO",
        "............oYrO",
        "...........oYrO.",
        "..........oYrO..",
        ".........oYrO...",
        "........oYrO....",
        ".......oYrO.....",
        "..dd..oYrO......",
        "..dDdoYrO.......",
        "...dDDrO........",
        "....dDd.........",
        "...hHdDd........",
        "..hHh.dd........",
        ".rHh............",
        ".rr............."]
img(fire, {"o": (255, 140, 30), "O": (180, 40, 20), "Y": (255, 230, 120), "r": (220, 60, 30), "d": (40, 60, 150), "D": (90, 120, 220), **HANDLE}).save(f"{OUT}/fire_dragon_sword.png")

force = ["..............wW",
         ".............wsW",
         "............wscW",
         "...........wscW.",
         "..........wscW..",
         ".........wscW...",
         "........wscW....",
         ".......wscW.....",
         "..gg..wscW......",
         "..gGgwscW.......",
         "...gGGcW........",
         "....gGg.........",
         "...hHgGg........",
         "..hHh.gg........",
         ".iHh............",
         ".ii............."]
img(force, {"w": (240, 240, 250), "W": (120, 120, 140), "s": (200, 210, 230), "c": (90, 140, 255), "g": (240, 200, 60), "G": (190, 140, 30), "i": (210, 210, 220), **HANDLE}).save(f"{OUT}/force_dragon_sword.png")

mace = [".....kKKKKk.....",
        "....kKsSSsKk....",
        "....KSbbbbSK....",
        "....KSbBBbSK....",
        "....KSbbbbSK....",
        "....kKsSSsKk....",
        ".....kKhhKk.....",
        ".......hH.......",
        ".......hH.......",
        ".......hH.......",
        ".......hH.......",
        ".......hH.......",
        ".......hH.......",
        "......bhHb......",
        ".......bb.......",
        "................"]
img(mace, {"k": (60, 60, 70), "K": (100, 100, 115), "s": (170, 170, 185), "S": (210, 210, 220), "b": (40, 60, 150), "B": (90, 130, 230), **HANDLE}).save(f"{OUT}/dragon_mace.png")

scythe = ["....gGGGGGg.....",
          "..gGllllllGg....",
          ".gGl......hGg...",
          "gGl.......hH....",
          "Gl.......hH.....",
          "l.......hH......",
          "........hH......",
          ".......hH.......",
          "......hH........",
          ".....hH.........",
          "....bhH.........",
          "....hH..........",
          "...hH...........",
          "..hH............",
          ".bH.............",
          ".b.............."]
img(scythe, {"g": (60, 130, 60), "G": (40, 92, 40), "l": (200, 240, 210), "b": (40, 60, 150), **HANDLE}).save(f"{OUT}/dragon_scythe.png")
print("ok")
