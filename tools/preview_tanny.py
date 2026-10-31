"""Vista previa de Tanny con el cabello de mechones: python3 tools/preview_tanny.py DIR [expresión]"""
import math, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_tanny as G
import hair_mesh as H
from PIL import Image, ImageDraw

LIGHT = H.norm((0.35, -0.8, -0.5))


def main(outdir, expr="tanny"):
    hparts, ha, oparts, oa = G.hair_parts()[0], None, None, None
    hp, _ = G.hair_parts()
    G.pack(hp, 64)
    op, _ = G.outfit_parts()
    G.pack(op, 64)
    TEX = G.TEX
    sk = Image.open(os.path.join(TEX, expr + ".png")).convert("RGBA")
    ht = Image.open(os.path.join(TEX, "hair.png")).convert("RGBA")
    ot = Image.open(os.path.join(TEX, "outfit.png")).convert("RGBA")
    quads = []
    base = [("head", (0, 0, 0), (0, 0), (-4, -8, -4, 8, 8, 8)), ("body", (0, 0, 0), (16, 16), (-4, 0, -2, 8, 12, 4)),
            ("right_arm", (-5, 2.5, 0), (40, 16), (-2, -2, -2, 3, 12, 4)), ("left_arm", (5, 2.5, 0), (32, 48), (-1, -2, -2, 3, 12, 4)),
            ("right_leg", (-1.9, 12, 0), (0, 16), (-2, 0, -2, 4, 12, 4)), ("left_leg", (1.9, 12, 0), (16, 48), (-2, 0, -2, 4, 12, 4))]
    for name, piv, (u, v), bx in base:
        quads += G.quads_for(sk, *bx, u, v, [(piv, (0, 0, 0))], 1)
    keep = ("headband", "ruffle", "hb_bow")
    tf = G.chains(hp, [("head", (0, 0, 0))])
    for p in hp:
        if not p.name.startswith(keep):
            continue
        for b in p.boxes:
            quads += G.quads_for(ht, b.x, b.y, b.z, b.w, b.h, b.d, b.u, b.v, tf[p.name], 1)
    oanch = [("body", (0, 0, 0)), ("right_arm", (-5, 2.5, 0)), ("left_arm", (5, 2.5, 0)),
             ("right_leg", (-1.9, 12, 0)), ("left_leg", (1.9, 12, 0))]
    tf = G.chains(op, oanch)
    for p in op:
        for b in p.boxes:
            quads += G.quads_for(ot, b.x, b.y, b.z, b.w, b.h, b.d, b.u, b.v, tf[p.name], 2)
    for q, c, n in H.mesh_quads():
        l = 0.62 + 0.45 * max(0.0, -(n[0] * LIGHT[0] + n[1] * LIGHT[1] + n[2] * LIGHT[2]) * -1 + 0.0)
        l = 0.55 + 0.5 * max(0.0, n[0] * -LIGHT[0] + n[1] * -LIGHT[1] + n[2] * -LIGHT[2])
        quads.append((q, tuple(int(min(255, v * l)) for v in c)))
    G.draw(quads, os.path.join(outdir, "tanny_preview.png"), 14, 150,
           [(math.pi, 0.05), (math.pi - 0.7, 0.12), (math.pi / 2, 0.05), (-0.6, 0.12)], (300, 520))
    head = [qc for qc in quads if min(p[1] for p in qc[0]) < 1.5]
    G.draw(head, os.path.join(outdir, "tanny_face.png"), 30, 300,
           [(math.pi, 0.0), (math.pi - 0.6, 0.15), (math.pi / 2, 0.05), (0.3, 0.25)], (380, 420))


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2] if len(sys.argv) > 2 else "tanny")
