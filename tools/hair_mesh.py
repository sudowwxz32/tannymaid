"""Cabello 3D de Tanny hecho de mechones curvos (malla), no de cajas.

Cada mechón nace en el cuero cabelludo, se pega a la forma de la cabeza (una "caja redondeada")
y luego cae con vuelo y la punta curvada hacia adentro, como un bob de anime.
La misma función de trayectoria existe en Java (client/TannyHairMesh.java); este archivo
genera la tabla de mechones (client/TannyHairLocks.java) y la vista previa.
"""
import math, os, random

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAVA = os.path.join(ROOT, "src/main/java/com/tanny/tannymaid/client/TannyHairLocks.java")

CY = -4.0          # centro de la cabeza (y)
R = 4.42           # radio de la "caja redondeada"
P = 4.0            # exponente (4 = cubo con esquinas suaves)
SCALP_STEPS = 6
HANG_STEPS = 9
SIDES = 4

# columnas de la tabla
#  0 phi   1 th0   2 th1   3 hang   4 flare   5 curl   6 width   7 thick   8 lift   9 shade   10 kind
#  kind: 0 = capa / bob, 1 = flequillo, 2 = mechón largo de la cara, 3 = ahoge
KIND_BOB, KIND_BANG, KIND_LONG, KIND_AHOGE = 0, 1, 2, 3


def locks():
    """Peinado tomboy de anime desordenado: picos lisos en todas direcciones, mucho flequillo
    (dos capas, peinado hacia un lado), varios mechones a los lados de la cara y nuca corta."""
    rnd = random.Random(41)
    L = []
    j = lambda a: rnd.uniform(-a, a)
    # coronilla: picos desordenados que salen hacia arriba y afuera
    for i in range(15):
        phi = i / 15 * 2 * math.pi + 0.2 + j(0.12)
        back = 0.5 - 0.5 * math.cos(phi)
        L.append([phi, 0.04 + j(0.03), 0.9 + 0.2 * back + j(0.08), 1.5 + j(0.5), 0.75 + j(0.3), -0.35 + j(0.25),
                  2.5 + j(0.3), 1.0, 0.55 + j(0.12), 1.0, KIND_BOB])
    # nuca y parte de atrás: cortos, con las puntas hacia afuera en distintas direcciones
    for i in range(11):
        phi = math.radians(112 + i * 13.6) + j(0.06)
        L.append([phi, 0.2, math.pi / 2 + 0.1 + j(0.06), 2.6 + j(0.45), 0.32 + j(0.15), -0.45 + j(0.3),
                  2.5, 1.0, 0.3, 1.0, KIND_BOB])
    # lados: hasta la oreja, dos capas
    for s_ in (-1, 1):
        for deg in (68, 80, 92, 104):
            L.append([s_ * math.radians(deg + j(3)), 0.2, math.pi / 2 + 0.05, 2.5 + j(0.35), 0.22 + j(0.1), -0.25 + j(0.2),
                      2.3, 0.95, 0.3, 1.0, KIND_BOB])
        for deg in (74, 98):
            L.append([s_ * math.radians(deg + j(3)), 0.3, math.pi / 2, 2.0 + j(0.3), 0.45, -0.4, 2.0, 0.9, 0.65, 1.0, KIND_BOB])
    # mucho pelo a los lados de la cara: mechones que bajan por las mejillas hasta la mandíbula
    for s_ in (-1, 1):
        for deg, hang, w, lift in ((44, 3.4, 1.7, 0.45), (50, 4.4, 1.9, 0.55), (57, 3.9, 1.9, 0.5), (63, 3.2, 1.8, 0.6), (53, 3.0, 1.5, 0.85)):
            L.append([s_ * math.radians(deg + j(2)), 0.3, math.pi / 2 - 0.08 + j(0.04), hang + j(0.3), 0.05 + j(0.05),
                      0.2 + j(0.15), w, 0.9, lift, 1.0, KIND_LONG])
    # flequillo desordenado: dos capas, peinado hacia un lado, de largos distintos
    for deg, hang in ((-38, 2.0), (-29, 2.3), (-20, 2.0), (-11, 2.35), (-2, 2.1), (7, 2.35), (16, 2.0), (25, 2.3), (34, 2.0), (41, 1.8)):
        L.append([math.radians(deg + j(2.5)), 0.3, 1.0 + j(0.05), hang + j(0.2), 0.12 + j(0.08), -0.15 + j(0.15),
                  2.0, 0.8, 0.5, 1.0, KIND_BANG])
    for deg, hang in ((-33, 1.4), (-15, 1.6), (3, 1.5), (21, 1.6), (36, 1.3)):
        L.append([math.radians(deg + j(3)), 0.25, 0.92, hang, 0.35, -0.3, 1.9, 0.75, 0.85, 1.0, KIND_BANG])
    # ahoge
    L.append([math.radians(-12), 0.0, 0.0, 3.0, 0, 0, 1.1, 0.7, 0.6, 1.0, KIND_AHOGE])
    return L


def se_scale(dx, dy, dz):
    s = (abs(dx) ** P + abs(dy) ** P + abs(dz) ** P) ** (1.0 / P)
    return R / max(1e-6, s)


def norm(v):
    l = math.sqrt(v[0] ** 2 + v[1] ** 2 + v[2] ** 2) or 1.0
    return (v[0] / l, v[1] / l, v[2] / l)


def path(lock, sway_x=0.0, sway_z=0.0):
    """Devuelve [(punto, normal_afuera, t)] a lo largo del mechón."""
    phi, th0, th1, hang, flare, curl, width, thick, lift = lock[:9]
    kind = int(lock[10])
    sp, cp = math.sin(phi), math.cos(phi)
    out_h = (sp, 0.0, -cp)
    pts = []
    if kind == KIND_AHOGE:
        # nace arriba y se curva hacia adelante formando un gancho
        base = (sp * 0.6, CY - R - 0.2, -cp * 0.6 - 0.8)
        for k in range(HANG_STEPS + 1):
            t = k / HANG_STEPS
            ang = -0.25 + t * 2.4 + sway_x * t
            d = (sp * 0.0 + math.sin(sway_z) * 0.3, -math.cos(ang), -math.sin(ang))
            if k == 0:
                p = base
            else:
                p = (p[0] + d[0] * hang / HANG_STEPS, p[1] + d[1] * hang / HANG_STEPS, p[2] + d[2] * hang / HANG_STEPS)
            pts.append((p, (1.0, 0.0, 0.0), t))
        return pts
    total = SCALP_STEPS + (HANG_STEPS if hang > 0 else 0)
    for k in range(SCALP_STEPS + 1):
        th = th0 + (th1 - th0) * k / SCALP_STEPS
        d = (math.sin(th) * sp, -math.cos(th), -math.sin(th) * cp)
        s = se_scale(*d) + lift
        p = (d[0] * s, CY + d[1] * s, d[2] * s)
        pts.append((p, d, k / total))
    if hang <= 0:
        return pts
    # tangente al final del cuero cabelludo
    tan = norm((math.cos(th1) * sp, math.sin(th1), -math.cos(th1) * cp))
    p = pts[-1][0]
    step = hang / HANG_STEPS
    for k in range(1, HANG_STEPS + 1):
        t = k / HANG_STEPS
        down = norm((out_h[0] * flare, 1.0, out_h[2] * flare))
        blend = min(1.0, t * 2.2)
        d = norm(tuple(tan[i] * (1 - blend) + down[i] * blend for i in range(3)))
        # punta curvada hacia adentro (bob)
        c = curl * max(0.0, (t - 0.55) / 0.45) ** 1.5
        d = norm((d[0] - out_h[0] * c, d[1], d[2] - out_h[2] * c))
        # flequillo con raya al medio: se abre hacia los lados
        if kind == KIND_BANG:
            sw = (phi * 0.7 - 0.75) * t       # todo el flequillo se peina hacia un lado
            d = norm((d[0] + cp * sw, d[1], d[2] + sp * sw))
        # vaivén: hacia atrás (x) y de lado (z)
        tw = t ** 1.5
        d = norm((d[0] + sway_z * tw, d[1], d[2] + sway_x * tw))
        p = (p[0] + d[0] * step, p[1] + d[1] * step, p[2] + d[2] * step)
        n = norm((p[0], 0.0, p[2])) if abs(p[0]) + abs(p[2]) > 0.01 else out_h
        pts.append((p, n, (SCALP_STEPS + k) / total))
    return pts


def width_at(lock, t):
    kind = int(lock[10])
    w = lock[6]
    if kind == KIND_AHOGE:
        return w * (1.0 - 0.9 * t)
    root = 0.75 + 0.25 * min(1.0, t / 0.2)
    return w * root * max(0.0, 1.0 - t) ** 0.85


def ring(p, tdir, n, w, k):
    """Sección elíptica plana: ancha a lo largo de la superficie, delgada hacia afuera."""
    b = norm((tdir[1] * n[2] - tdir[2] * n[1], tdir[2] * n[0] - tdir[0] * n[2], tdir[0] * n[1] - tdir[1] * n[0]))
    nn = norm((b[1] * tdir[2] - b[2] * tdir[1], b[2] * tdir[0] - b[0] * tdir[2], b[0] * tdir[1] - b[1] * tdir[0]))
    if nn[0] * n[0] + nn[1] * n[1] + nn[2] * n[2] < 0:
        nn = (-nn[0], -nn[1], -nn[2])
    out = []
    for j in range(SIDES):
        a = 2 * math.pi * j / SIDES
        ca, sa = math.cos(a), math.sin(a)
        off = tuple(b[i] * ca * w / 2 + nn[i] * sa * k / 2 for i in range(3))
        out.append((tuple(p[i] + off[i] for i in range(3)), norm(tuple(b[i] * ca + nn[i] * sa * 1.6 for i in range(3)))))
    return out


HAIR = (26, 24, 34)
SHINE = (118, 122, 162)


HAIR_UNIFORM = (40, 36, 46)


def color_at(lock, p, n, t):
    """Color uniforme: la forma la marca la luz sobre los mechones lisos."""
    return HAIR_UNIFORM


CAP_TH = 10
CAP_PHI = 28


def cap_point(th, phi):
    d = (math.sin(th) * math.sin(phi), -math.cos(th), -math.sin(th) * math.cos(phi))
    s = se_scale(*d) + 0.05
    return (d[0] * s, CY + d[1] * s, d[2] * s), d


def cap_max_th(phi):
    """El casquete baja más por detrás (nuca) que por delante (frente)."""
    back = 0.5 - 0.5 * math.cos(phi)
    return 1.0 + 1.05 * back


def cap_quads():
    quads = []
    for i in range(CAP_PHI):
        p0, p1 = 2 * math.pi * i / CAP_PHI, 2 * math.pi * (i + 1) / CAP_PHI
        for j in range(CAP_TH):
            m0, m1 = cap_max_th(p0), cap_max_th(p1)
            a0, a1 = j / CAP_TH, (j + 1) / CAP_TH
            q = [cap_point(m0 * a0, p0)[0], cap_point(m1 * a0, p1)[0], cap_point(m1 * a1, p1)[0], cap_point(m0 * a1, p0)[0]]
            quads.append((q, HAIR_UNIFORM, cap_point(m0 * a0, p0)[1]))
    return quads


def mesh_quads(sway_x=0.0, sway_z=0.0, helmet=False):
    quads = cap_quads()
    for lock in locks():
        pts = path(lock, sway_x, sway_z)
        rings = []
        for i, (p, n, t) in enumerate(pts):
            a = pts[max(0, i - 1)][0]
            b = pts[min(len(pts) - 1, i + 1)][0]
            tdir = norm((b[0] - a[0], b[1] - a[1], b[2] - a[2]))
            rings.append((ring(p, tdir, n, width_at(lock, t), lock[7] * (0.5 + 0.5 * (1 - t))), color_at(lock, p, n, t)))
        for i in range(len(rings) - 1):
            r0, c0 = rings[i]
            r1, _ = rings[i + 1]
            for j in range(SIDES):
                jj = (j + 1) % SIDES
                q = [r0[j][0], r0[jj][0], r1[jj][0], r1[j][0]]
                nrm = norm(tuple(r0[j][1][k] + r0[jj][1][k] for k in range(3)))
                quads.append((q, c0, nrm))
    return quads


# ================================================================= mechones de cubos (estilo vóxel)
VOX_STEP = 0.72
PALETTE = [(34, 27, 30), (54, 41, 41), (76, 58, 54), (20, 16, 18)]
WAVE_AMP = 0.24
WAVE_FREQ = 1.3


def vox_hash(a, b):
    return ((a * 31 + b) * 2654435761) & 0xffffffff


def resample(pts):
    """Puntos cada VOX_STEP a lo largo del mechón: [(p, n, t, s)]."""
    out = []
    acc = 0.0
    need = 0.0
    for i in range(len(pts) - 1):
        (a, na, ta), (b, nb, tb) = pts[i], pts[i + 1]
        seg = math.sqrt(sum((b[k] - a[k]) ** 2 for k in range(3)))
        while need <= acc + seg + 1e-6:
            f = (need - acc) / seg if seg > 1e-6 else 0.0
            p = tuple(a[k] + (b[k] - a[k]) * f for k in range(3))
            n = norm(tuple(na[k] + (nb[k] - na[k]) * f for k in range(3)))
            out.append((p, n, ta + (tb - ta) * f, need))
            need += VOX_STEP
        acc += seg
    return out


def frame(p_prev, p_next, n):
    t = norm(tuple(p_next[k] - p_prev[k] for k in range(3)))
    b = norm((t[1] * n[2] - t[2] * n[1], t[2] * n[0] - t[0] * n[2], t[0] * n[1] - t[1] * n[0]))
    m = norm((b[1] * t[2] - b[2] * t[1], b[2] * t[0] - b[0] * t[2], b[0] * t[1] - b[1] * t[0]))
    if m[0] * n[0] + m[1] * n[1] + m[2] * n[2] < 0:
        m = (-m[0], -m[1], -m[2])
    return t, b, m


def voxels(index, lock, sway_x=0.0, sway_z=0.0):
    """Cubos de un mechón: (centro, T, B, N, medio_largo, medio_ancho, medio_grosor, color)."""
    kind = int(lock[10])
    rs = resample(path(lock, sway_x, sway_z))
    cubes = []
    wavy = 0.0 if kind in (KIND_AHOGE,) else (0.6 if kind == KIND_BANG else 1.0)
    for i, (p, n, t, sl) in enumerate(rs):
        a = rs[max(0, i - 1)][0]
        b = rs[min(len(rs) - 1, i + 1)][0]
        if a == b:
            continue
        T, B, N = frame(a, b, n)
        amp = WAVE_AMP * wavy * min(1.0, t * 2.5)
        off = math.sin(sl * WAVE_FREQ + lock[0] * 5.0) * amp
        c = tuple(p[k] + B[k] * off for k in range(3))
        w = width_at(lock, t)
        hb = max(0.32, min(0.95, w * 0.34))
        hn = max(0.32, hb * 0.8)
        ht = VOX_STEP * 0.62
        # franjas de color a lo largo del mechón (como un mechón de vóxeles), con un toque al azar
        h = vox_hash(index, i)
        band = (index + i // 2) % 3
        col = PALETTE[3 if h % 7 == 0 else band]
        if c[1] < -7.3:
            col = tuple(min(255, int(v * 1.15)) for v in col)
        shade = lock[9]
        col = tuple(int(v * shade) for v in col)
        cubes.append((c, T, B, N, ht, hb, hn, col))
    return cubes


def cube_quads(c, T, B, N, ht, hb, hn, col):
    q = []
    axes = [(T, ht), (B, hb), (N, hn)]
    for ai in range(3):
        for sg in (-1, 1):
            ax, ha = axes[ai]
            u, hu = axes[(ai + 1) % 3]
            v, hv = axes[(ai + 2) % 3]
            ctr = tuple(c[k] + ax[k] * ha * sg for k in range(3))
            corners = []
            for (su, sv) in ((-1, -1), (1, -1), (1, 1), (-1, 1)):
                corners.append(tuple(ctr[k] + u[k] * hu * su + v[k] * hv * sv for k in range(3)))
            q.append((corners, col, tuple(ax[k] * sg for k in range(3))))
    return q


def voxel_quads(sway_x=0.0, sway_z=0.0):
    quads = cap_quads()
    for i, lock in enumerate(locks()):
        for cube in voxels(i, lock, sway_x, sway_z):
            quads += cube_quads(*cube)
    return quads


def fjava(v):
    s = ("%.4f" % v).rstrip("0").rstrip(".")
    return ("0" if s in ("-0", "") else s) + "F"


def write_java():
    L = locks()
    out = ["// GENERADO por tools/hair_mesh.py — no editar a mano.", "package com.tanny.tannymaid.client;", "",
           "/** Mechones de Tanny: phi, th0, th1, caída, vuelo, curva, ancho, grosor, separación, tono, tipo. */",
           "public final class TannyHairLocks {", "    private TannyHairLocks() {}", "",
           "    public static final float CY = %s, R = %s, P = %s;" % (fjava(CY), fjava(R), fjava(P)),
           "    public static final int SCALP_STEPS = %d, HANG_STEPS = %d, SIDES = %d;" % (SCALP_STEPS, HANG_STEPS, SIDES),
           "    public static final float VOX_STEP = %s, WAVE_AMP = %s, WAVE_FREQ = %s;" % (fjava(VOX_STEP), fjava(WAVE_AMP), fjava(WAVE_FREQ)),
           "    public static final int[] PALETTE = {" + ", ".join("0x%02X%02X%02X" % c for c in PALETTE) + "};",
           "", "    public static final float[][] L = {"]
    for l in L:
        out.append("            {" + ", ".join(fjava(v) for v in l) + "},")
    out += ["    };", "}", ""]
    open(JAVA, "w").write("\n".join(out))
    return len(L)


if __name__ == "__main__":
    print("mechones:", write_java())
