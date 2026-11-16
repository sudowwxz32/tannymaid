"""Totally Waifu? 3.0 — genera los modelos de las 18 chicas (4 de las mesas + 14 de animales y monstruos).

Uso: python3 tools/gen_all.py [preview DIR]
"""
import math, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_tanny as G
import gen_blocky as B
import gen_mobgirls as M
import gen_girls4 as N
from PIL import Image, ImageDraw

ALL = dict(B.WAIFUS)
ALL.update(M.GIRLS)
ALL.update(N.GIRLS)
DEFAULT_SKIN = (B.SKIN, B.SKIN_SH)


def make(name):
    """Construye la chica con su silueta (y anota ojos, marcas y colmillo)."""
    shape, eye = N.LOOK.get(name, ("std", "round"))
    B.SHAPE = B.SHAPES[shape]
    try:
        P, info = ALL[name]()
    finally:
        B.SHAPE = B.SHAPES["std"]
    info.setdefault("eye", eye)
    info["marks"] = list(info.get("marks", [])) + N.MARKS.get(name, [])
    info["fang"] = info.get("fang", False) or name in N.FANG
    return P, info


def build(name):
    P, info = make(name)
    B.SKIN, B.SKIN_SH = info.get("skin", DEFAULT_SKIN)
    P = B.mirror_all(P)
    out = os.path.join(B.TEXR, name)
    os.makedirs(out, exist_ok=True)
    for f in os.listdir(out):
        os.remove(os.path.join(out, f))
    for expr, suf in B.EXPRS:
        B.face_tex(expr, info["iris"], info["lash"], info["hair"], eye=info["eye"], marks=info["marks"], fang=info["fang"]).save(os.path.join(out, "face" + suf + ".png"))
    G.S = 8
    G.AO = True
    G.jit = lambda c, a=4: c
    w, h = G.pack(P, 64)
    G.build_texture(P, w, h).save(os.path.join(out, "body.png"))
    B.SKIN, B.SKIN_SH = DEFAULT_SKIN
    return P, (w, h)


def main():
    java = ["// GENERADO por tools/gen_all.py — no editar a mano.", "package com.totallywaifu.client;", "",
            "import net.minecraft.client.model.geom.ModelPart;", "import net.minecraft.client.model.geom.PartPose;",
            "import net.minecraft.client.model.geom.builders.*;", "", "import java.util.LinkedHashMap;", "import java.util.Map;", "",
            "/** Modelos de bloques de las 18 waifus (cuerpo alto y esbelto + ropa y accesorios de cada una). */",
            "public final class WaifuModels {", "    private WaifuModels() {}", "",
            "    public static final float ARM_X = %s, ARM_Y = %s, LEG_X = %s, LEG_Y = %s;" % tuple(G.fnum(v) for v in (B.ARM_X, B.ARM_Y, B.LEG_X, B.LEG_Y)), "",
            "    public static LayerDefinition face() {",
            "        MeshDefinition mesh = new MeshDefinition();",
            "        mesh.getRoot().addOrReplaceChild(\"head\", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.ZERO);",
            "        return LayerDefinition.create(mesh, 32, 16);", "    }", ""]
    layer = ["    public static LayerDefinition layer(String id) {", "        return switch (id) {"]
    bake = ["    public static Map<String, ModelPart> bake(String id, ModelPart root) {", "        return switch (id) {"]
    for name in ALL:
        P, (w, h) = build(name)
        cap = name.capitalize()
        java += G.java_layer("body" + cap, P, B.ANCHORS, w, h) + [""]
        java += G.java_bake("bakeBody" + cap, P, B.ANCHORS) + [""]
        layer.append('            case "%s" -> body%s();' % (name, cap))
        bake.append('            case "%s" -> bakeBody%s(root);' % (name, cap))
        print("%s: %d piezas, textura %dx%d" % (name, len(P), w * 8, h * 8))
    layer += ['            default -> bodyKozue();', "        };", "    }", ""]
    bake += ['            default -> bakeBodyKozue(root);', "        };", "    }", ""]
    java += layer + bake + ["}", ""]
    open(B.JAVA, "w").write("\n".join(java))


def preview(outdir, names=None):
    os.makedirs(outdir, exist_ok=True)
    G.S = 8
    tiles = []
    for name in names or ALL:
        P, info = make(name)
        P = B.mirror_all(P)
        G.pack(P, 64)
        out = os.path.join(B.TEXR, name)
        bt = Image.open(os.path.join(out, "body.png")).convert("RGBA")
        ft = Image.open(os.path.join(out, "face.png")).convert("RGBA")
        f512 = Image.new("RGBA", (512, 256), (0, 0, 0, 0))
        f512.paste(ft, (0, 0))
        quads = G.quads_for(f512, -4, -8, -4, 8, 8, 8, 0, 0, [((0, 0, 0), (0, 0, 0))], 1)
        tf = G.chains(P, B.ANCHORS)
        for p in P:
            for b in p.boxes:
                quads += G.quads_for(bt, b.x, b.y, b.z, b.w, b.h, b.d, b.u, b.v, tf[p.name], 1)
        path = os.path.join(outdir, name + "_preview.png")
        G.draw(quads, path, 12.5, 140, [(math.pi, 0.05), (math.pi - 0.7, 0.1), (0.0, 0.05)], (250, 470))
        tiles.append((name, Image.open(path)))
    w, h = tiles[0][1].size
    cols = 6
    rows = math.ceil(len(tiles) / cols)
    sheet = Image.new("RGB", (w * cols, (h + 20) * rows), (226, 232, 240))
    d = ImageDraw.Draw(sheet)
    for i, (n, im) in enumerate(tiles):
        x, y = (i % cols) * w, (i // cols) * (h + 20)
        sheet.paste(im, (x, y + 20))
        d.text((x + 10, y + 4), n, fill=(60, 40, 80))
    sheet.save(os.path.join(outdir, "sheet.png"))


if __name__ == "__main__":
    main()
    if len(sys.argv) > 2 and sys.argv[1] == "preview":
        preview(sys.argv[2], sys.argv[3].split(",") if len(sys.argv) > 3 else None)
