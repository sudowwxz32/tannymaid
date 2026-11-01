"""Texturas de Yunn, la enfermera Pokémon (diseño original): piel blanca, cabello lavanda,
ojos azules redondos, uniforme rosa y blanco con corazón. Reutiliza los pintores de gen_tanny
cambiando la paleta. Uso: python3 tools/gen_yunn.py [preview DIR]"""
import math, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_tanny as G
import hair_mesh as H
from PIL import Image

OUT = os.path.join(G.ROOT, "src/main/resources/assets/tannymaid/textures/entity/yunn")
HAIR_COLOR = (196, 158, 222)


def palette():
    G.SKIN, G.SKIN_SH, G.SKIN_HI, G.SKIN_DK = (250, 228, 218), (238, 208, 198), (255, 240, 232), (218, 180, 170)
    G.HAIR, G.HAIR_HI, G.HAIR_DK = HAIR_COLOR, (216, 186, 236), (160, 122, 190)
    G.EYE_DK, G.EYE_MID, G.EYE_HI, G.EYE_RING = (34, 62, 150), (84, 146, 232), (176, 220, 255), (30, 52, 126)
    G.BLACK, G.BLACK_HI, G.BLACK_DK = (242, 156, 186), (252, 188, 210), (216, 124, 158)
    G.RED, G.RED_DK, G.RED_HI = (232, 82, 126), (180, 50, 92), (250, 140, 172)
    G.SHOE, G.SHOE_HI = (232, 120, 160), (252, 190, 212)
    G.PUPIL_W = 1.9
    G.BLUSH_ON = True
    G.BLUSH = (246, 150, 168)


def main():
    palette()
    os.makedirs(OUT, exist_ok=True)
    for expr, name in [("normal", "yunn"), ("blink", "yunn_blink"), ("happy", "yunn_happy"),
                       ("surprised", "yunn_surprised"), ("focused", "yunn_focused"), ("tired", "yunn_tired")]:
        G.skin(expr).save(os.path.join(OUT, name + ".png"))
    op, _ = G.outfit_parts()
    ow, oh = G.pack(op, 64)
    G.build_texture(op, ow, oh).save(os.path.join(OUT, "outfit.png"))
    yp, _ = G.yunn_cap_parts()
    yw, yh = G.pack(yp, 64)
    G.build_texture(yp, yw, yh).save(os.path.join(OUT, "cap.png"))
    print("yunn ok")


def preview(outdir):
    sk = Image.open(os.path.join(OUT, "yunn.png")).convert("RGBA")
    ot = Image.open(os.path.join(OUT, "outfit.png")).convert("RGBA")
    ct = Image.open(os.path.join(OUT, "cap.png")).convert("RGBA")
    quads = []
    base = [((0, 0, 0), (0, 0), (-4, -8, -4, 8, 8, 8)), ((0, 0, 0), (16, 16), (-4, 0, -2, 8, 12, 4)),
            ((-5, 2.5, 0), (40, 16), (-2, -2, -2, 3, 12, 4)), ((5, 2.5, 0), (32, 48), (-1, -2, -2, 3, 12, 4)),
            ((-1.9, 12, 0), (0, 16), (-2, 0, -2, 4, 12, 4)), ((1.9, 12, 0), (16, 48), (-2, 0, -2, 4, 12, 4))]
    for piv, (u, v), bx in base:
        quads += G.quads_for(sk, *bx, u, v, [(piv, (0, 0, 0))], 1)
    op, _ = G.outfit_parts()
    G.pack(op, 64)
    tf = G.chains(op, [("body", (0, 0, 0)), ("right_arm", (-5, 2.5, 0)), ("left_arm", (5, 2.5, 0)),
                       ("right_leg", (-1.9, 12, 0)), ("left_leg", (1.9, 12, 0))])
    for p in op:
        for b in p.boxes:
            quads += G.quads_for(ot, b.x, b.y, b.z, b.w, b.h, b.d, b.u, b.v, tf[p.name], 2)
    yp, _ = G.yunn_cap_parts()
    G.pack(yp, 64)
    tf = G.chains(yp, [("head", (0, 0, 0))])
    for p in yp:
        for b in p.boxes:
            quads += G.quads_for(ct, b.x, b.y, b.z, b.w, b.h, b.d, b.u, b.v, tf[p.name], 1)
    L = H.norm((0.35, -0.8, -0.5))
    for q, c, n in H.mesh_quads(table=H.yunn_locks(), color=HAIR_COLOR):
        l = 0.55 + 0.5 * max(0.0, -(n[0] * L[0] + n[1] * L[1] + n[2] * L[2]))
        quads.append((q, tuple(int(min(255, v * l)) for v in c)))
    G.draw(quads, os.path.join(outdir, "yunn_preview.png"), 14, 150,
           [(math.pi, 0.05), (math.pi - 0.7, 0.12), (math.pi / 2, 0.05), (-0.6, 0.12)], (300, 520))
    head = [qc for qc in quads if min(p[1] for p in qc[0]) < 1.5]
    G.draw(head, os.path.join(outdir, "yunn_face.png"), 30, 300, [(math.pi, 0.0), (math.pi - 0.6, 0.15)], (380, 420))


if __name__ == "__main__":
    main()
    if len(sys.argv) > 2 and sys.argv[1] == "preview":
        preview(sys.argv[2])
