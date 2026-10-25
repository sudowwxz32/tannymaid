"""Cabello saiyajin 3D LISO (sin segmentos de cubos).

Cada mechón es una punta continua: una malla de 4 lados que se afina hasta un pico y se curva suavemente.
Este script describe los mechones y genera HairSpikes.java (datos) + las texturas; la malla se construye
en el juego (SaiyanHairLayer) para que cada mechón pueda doblarse y moverse de forma fluida.

Convención: -y = arriba, -z = cara.  xRot negativo inclina hacia atrás, zRot negativo hacia la derecha (-x).
Prefijos: c_ cráneo · s_ clásico · x_ Fase 2 · l_ Fase 3 · g_ Dios/UI · f_ Fase 4 · e_ Ultra Ego · b_ Bestia · k_ Legendario · r_ Rosé
"""
import os, sys, math
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEXDIR = os.path.join(ROOT, "src/main/resources/assets/blockdragonsuperz/textures/entity")
JAVA = os.path.join(ROOT, "src/main/java/com/bdsz/client/HairSpikes.java")

SPIKES = []


class S:
    def __init__(self, name, pos, rx, rz, length, width, bx=0.0, bz=0.0, flat=1.0, hang=False, taper=0.0):
        self.name, self.pos, self.rx, self.rz = name, pos, rx, rz
        self.length, self.width, self.bx, self.bz, self.flat, self.hang = length, width, bx, bz, flat, hang
        self.taper = taper          # 0 = termina en punta; >0 = lámina que solo se adelgaza un poco


def out(name, pivot, rx, rz, length, size, bend=0.06, sink=0.6):
    """Punta que se curva un poco hacia afuera (sigue el sentido de su giro)."""
    sx = -bend if rx < -0.05 else (bend * 0.6 if rx > 0.05 else 0.0)
    sz = -bend if rz < -0.05 else (bend if rz > 0.05 else 0.0)
    return S(name, pivot, rx, rz, length + sink, size * 0.8, sx * 3, sz * 3)


def hang(name, pivot, rx, rz, length, size):
    """Mechón largo colgante (se mece al caminar)."""
    return S(name, pivot, rx, rz, length, size * 0.85, -0.15, 0.0, flat=0.6, hang=True)


def sheet(name, pivot, rx, rz, length, size):
    """Cabello largo o recto hecho de un haz de mechones 3D redondeados (nada liso ni plano)."""
    n = 3 if size >= 4.0 else 2
    side_z = abs(pivot[0]) > 3.5                 # en los lados el haz se reparte de adelante hacia atrás
    locks = []
    for i in range(n):
        f = (i - (n - 1) / 2.0)
        off = f * size * 0.42
        pos = (pivot[0], pivot[1] + abs(f) * 0.3, pivot[2] + off) if side_z else (pivot[0] + off, pivot[1] + abs(f) * 0.3, pivot[2])
        drz = 0.0 if side_z else f * 0.07
        locks.append(S(name + "abc"[i], pos, rx + abs(f) * 0.04, rz + drz, length * (1.0 - abs(f) * 0.08 + (0.05 if i == 1 else 0)),
                       size * 0.52, -0.12, 0.0, flat=0.85, hang=True, taper=0.72))
    return locks


def strand(name, pivot, rot, length, w, d=1.0):
    """Mechón que cae sobre la frente (flequillo / patilla): plano y fino."""
    return S(name, pivot, math.pi + rot[0], rot[2], length * 1.05, w * 0.8, 0.25, rot[2] * 0.8, flat=0.8)


def spikes():
    P = []
    P.append(out("c_k1", (0, -8.3, -2.0), 0.15, 0, 3.0, 4.6, sink=0.8))
    P.append(out("c_k2", (0, -8.3, 1.4), -0.4, 0, 3.2, 4.6, sink=0.8))
    P.append(out("c_k3", (0, -6.4, 3.9), -1.2, 0, 3.0, 4.4, sink=0.8))
    P.append(out("c_k4", (0, -3.4, 4.0), -1.75, 0, 2.6, 3.8, sink=0.8))
    P.append(out("c_k_r1", (-2.7, -8.1, -1.4), 0.1, -0.5, 2.8, 4.0, sink=0.8))
    P.append(out("c_k_r2", (-2.7, -8.0, 1.8), -0.45, -0.5, 2.8, 4.0, sink=0.8))
    P.append(out("c_k_r3", (-3.9, -6.4, 0.2), -0.1, -1.25, 2.6, 3.6, sink=0.8))
    P.append(out("c_k_r4", (-2.8, -4.0, 3.8), -1.6, -0.5, 2.4, 3.4, sink=0.8))
    P.append(strand("c_burn_r", (-4.2, -4.8, -2.4), (0, 0, 0.06), 3.8, 1.3))

    # ---- clásico (Falso, Fase 1, Blue): corona de puntas que se abren hacia arriba y afuera
    P.append(out("s_c1", (0, -8.3, -0.6), -0.08, 0, 11.5, 4.3))
    P.append(out("s_c2", (0, -8.2, 2.2), -0.55, 0, 11.0, 4.1))
    P.append(out("s_c3", (0, -6.6, 3.9), -1.05, 0, 9.5, 3.9))
    P.append(out("s_c4", (0, -3.8, 4.1), -1.5, 0, 7.5, 3.5))
    P.append(out("s_r1", (-2.3, -8.3, -1.3), 0.05, -0.4, 10.5, 3.9))
    P.append(out("s_r2", (-2.4, -8.2, 1.6), -0.45, -0.45, 10.0, 3.8))
    P.append(out("s_r3", (-3.7, -7.2, -0.6), 0.05, -0.95, 9.0, 3.5))
    P.append(out("s_r4", (-3.8, -6.6, 2.4), -0.6, -0.95, 8.5, 3.4))
    P.append(out("s_r5", (-2.6, -6.2, 3.8), -1.0, -0.5, 8.5, 3.4))
    P.append(out("s_r6", (-3.9, -4.6, 1.0), -0.3, -1.4, 6.5, 3.1))
    P.append(out("s_r7", (-3.0, -3.6, 3.9), -1.4, -0.6, 6.5, 3.1))
    P.append(out("s_f1", (1.2, -8.4, -3.2), 0.35, 0.15, 6.0, 3.1))
    P.append(out("s_f2_r", (-1.6, -8.4, -3.2), 0.4, -0.25, 5.5, 2.9))
    P.append(strand("s_b1", (1.7, -8.1, -4.35), (-0.1, 0, -0.2), 4.6, 1.6))
    P.append(strand("s_b2_r", (-2.5, -8.1, -4.3), (-0.1, 0, 0.3), 3.8, 1.4))
    P.append(strand("x_bang", (-0.5, -8.2, -4.45), (-0.1, 0, 0.12), 7.5, 1.6))      # Fase 2: mechón que cae

    # ---- Rosé: como la foto; puntas más rectas hacia arriba, corona alta
    P.append(out("r_c1", (0, -8.4, -1.0), 0.06, 0, 12.5, 4.4))
    P.append(out("r_c2", (0, -8.3, 1.8), -0.3, 0, 12.0, 4.2))
    P.append(out("r_c3", (0, -7.0, 3.8), -0.75, 0, 10.0, 3.9))
    P.append(out("r_c4", (0, -4.6, 4.1), -1.25, 0, 7.5, 3.5))
    P.append(out("r_r1", (-2.4, -8.3, -1.6), 0.08, -0.32, 11.5, 3.9))
    P.append(out("r_r2", (-2.5, -8.2, 1.4), -0.3, -0.38, 11.0, 3.8))
    P.append(out("r_r3", (-3.7, -7.4, -0.4), 0.0, -0.75, 9.5, 3.5))
    P.append(out("r_r4", (-3.8, -6.8, 2.4), -0.45, -0.8, 9.0, 3.4))
    P.append(out("r_r5", (-3.9, -5.0, 0.6), -0.1, -1.2, 7.0, 3.1))
    P.append(out("r_r6", (-2.8, -5.2, 3.8), -0.95, -0.6, 7.5, 3.1))
    P.append(out("r_f1", (0.5, -8.5, -3.3), 0.28, 0.06, 7.0, 3.3))
    P.append(out("r_f2_r", (-2.2, -8.4, -3.3), 0.3, -0.3, 6.5, 3.1))
    P.append(strand("r_b1", (2.3, -8.1, -4.35), (-0.1, 0, -0.18), 4.0, 1.5))
    P.append(strand("r_b2_r", (-1.0, -8.1, -4.35), (-0.1, 0, 0.15), 3.6, 1.4))

    # ---- Bestia: cabello enorme y parado, casi vertical, con un mechón largo al frente
    P.append(out("b_c1", (0, -8.4, -0.6), 0.0, 0, 21.0, 4.8, bend=0.02))
    P.append(out("b_c2", (0, -8.3, 2.0), -0.16, 0, 20.0, 4.6, bend=0.02))
    P.append(out("b_c3", (0, -7.4, 3.8), -0.38, 0, 17.0, 4.2, bend=0.03))
    P.append(out("b_c4", (0, -5.2, 4.1), -0.75, 0, 12.0, 3.8, bend=0.03))
    P.append(out("b_r1", (-2.4, -8.3, -1.6), 0.02, -0.15, 19.5, 4.2, bend=0.02))
    P.append(out("b_r2", (-2.5, -8.2, 1.4), -0.14, -0.2, 19.0, 4.2, bend=0.02))
    P.append(out("b_r3", (-3.7, -7.4, -0.4), 0.0, -0.36, 16.0, 3.8, bend=0.03))
    P.append(out("b_r4", (-3.8, -6.8, 2.4), -0.25, -0.4, 15.0, 3.7, bend=0.03))
    P.append(out("b_r5", (-3.9, -5.4, 0.6), -0.05, -0.68, 11.0, 3.4))
    P.append(out("b_r6", (-2.8, -5.6, 3.8), -0.55, -0.4, 12.0, 3.4))
    P.append(out("b_f1", (0.8, -8.5, -3.3), 0.1, 0.05, 13.0, 3.8, bend=0.02))
    P.append(out("b_f2_r", (-2.2, -8.4, -3.3), 0.12, -0.14, 12.0, 3.5, bend=0.02))
    P.append(strand("b_bang", (-1.2, -8.2, -4.45), (-0.12, 0, 0.14), 10.0, 1.9))

    # ---- Legendario: melena salvaje enorme que se abre en todas direcciones
    P.append(out("k_c1", (0, -8.4, -0.8), -0.05, 0, 15.0, 5.0, bend=0.08))
    P.append(out("k_c2", (0, -8.2, 2.0), -0.55, 0, 15.5, 4.9, bend=0.08))
    P.append(out("k_c3", (0, -6.8, 3.9), -1.0, 0, 14.5, 4.7, bend=0.08))
    P.append(out("k_c4", (0, -4.0, 4.2), -1.45, 0, 12.5, 4.3, bend=0.07))
    P.append(out("k_c5", (0, -2.4, 4.0), -1.7, 0, 9.0, 3.9, bend=0.05))
    P.append(out("k_r1", (-2.4, -8.3, -1.6), 0.05, -0.45, 14.5, 4.5, bend=0.09))
    P.append(out("k_r2", (-2.5, -8.2, 1.4), -0.45, -0.5, 14.5, 4.5, bend=0.09))
    P.append(out("k_r3", (-3.8, -7.2, -0.6), 0.05, -0.78, 13.5, 4.1, bend=0.08))
    P.append(out("k_r4", (-3.9, -6.6, 2.4), -0.55, -0.82, 13.5, 4.1, bend=0.08))
    P.append(out("k_r5", (-4.0, -5.0, 0.4), -0.15, -1.12, 11.5, 3.7, bend=0.07))
    P.append(out("k_r6", (-3.0, -5.6, 3.9), -1.0, -0.6, 12.5, 3.9, bend=0.08))
    P.append(out("k_r7", (-3.4, -3.4, 3.4), -1.35, -0.6, 10.5, 3.5, bend=0.06))
    P.append(out("k_f1", (0.8, -8.5, -3.3), 0.25, 0.12, 8.5, 3.7))
    P.append(out("k_f2_r", (-2.2, -8.4, -3.3), 0.3, -0.35, 8.0, 3.5))

    # ---- Legendario Fase 3: puntas extra, más finas y erizadas entre las de la melena
    P.append(out("q_c1", (0, -8.5, 0.6), -0.3, 0, 17.0, 3.2, bend=0.1))
    P.append(out("q_c2", (0, -7.6, 3.2), -0.8, 0, 16.0, 3.0, bend=0.1))
    P.append(out("q_c3", (0, -5.2, 4.1), -1.25, 0, 14.0, 2.8, bend=0.09))
    P.append(out("q_r1", (-1.4, -8.5, -0.4), -0.1, -0.25, 16.5, 3.0, bend=0.11))
    P.append(out("q_r2", (-3.2, -8.0, 0.4), -0.25, -0.65, 15.0, 2.9, bend=0.11))
    P.append(out("q_r3", (-3.8, -6.2, 1.6), -0.5, -0.95, 14.0, 2.8, bend=0.1))
    P.append(out("q_r4", (-2.6, -6.6, 3.6), -0.85, -0.45, 15.0, 2.8, bend=0.1))
    P.append(out("q_r5", (-4.1, -3.8, 2.0), -1.1, -1.25, 11.0, 2.6, bend=0.08))
    P.append(out("q_f1_r", (-1.0, -8.5, -3.0), 0.15, -0.15, 9.5, 2.8, bend=0.08))

    # ---- Fase 3: melena muy larga hasta la cintura, hecha de mechones en punta
    P.append(hang("l_h0", (0, -7.0, 3.9), -3.0, 0, 30.0, 7.0))
    P.append(hang("l_h1_r", (-2.6, -6.6, 3.6), -3.0, -0.16, 28.0, 6.0))
    P.append(hang("l_h2_r", (-4.0, -6.0, 2.0), -3.05, -0.3, 24.0, 5.4))
    P.append(hang("l_h3", (0, -3.6, 4.1), -3.05, 0, 26.0, 6.0))
    P.append(hang("l_h4_r", (-2.4, -3.0, 3.9), -3.05, -0.2, 24.0, 5.2))
    P.append(hang("l_h5_r", (-4.2, -6.4, -1.0), -3.08, -0.22, 15.0, 4.0))
    P.append(out("l_t1", (0, -8.4, 0.2), -0.5, 0, 6.5, 4.6))
    P.append(out("l_t2_r", (-2.4, -8.2, 0.4), -0.4, -0.45, 6.0, 4.0))
    P.append(out("l_t3_r", (-3.6, -7.4, -1.0), 0.0, -0.85, 5.0, 3.4))
    P.append(out("l_t4", (0.6, -8.4, -2.6), 0.3, 0.1, 5.0, 3.6))
    P.append(strand("l_bang", (1.3, -8.2, -4.4), (-0.12, 0, -0.08), 8.0, 1.7))

    # ---- Dios / Ultra Instinto: puntas medianas y ordenadas
    P.append(out("g_c1", (0.3, -8.4, -1.0), 0.12, -0.05, 7.5, 3.7))
    P.append(out("g_c2", (0, -8.0, 2.4), -0.75, 0, 7.5, 3.6))
    P.append(out("g_c3", (0, -5.4, 4.0), -1.3, 0, 6.0, 3.3))
    P.append(out("g_r1", (-2.3, -8.3, -0.6), 0.1, -0.45, 7.0, 3.4))
    P.append(out("g_r2", (-1.0, -8.5, 1.0), -0.4, -0.15, 7.2, 3.5))
    P.append(out("g_r3", (-2.4, -7.4, 3.0), -0.9, -0.45, 6.5, 3.2))
    P.append(out("g_r4", (-3.5, -7.2, 0.8), -0.25, -0.8, 6.0, 3.1))
    P.append(out("g_r5", (-3.1, -5.0, 3.4), -1.25, -0.55, 5.5, 3.0))
    P.append(out("g_f1", (1.0, -8.4, -3.2), 0.45, 0.2, 5.5, 3.1))
    P.append(out("g_f2_r", (-1.6, -8.4, -3.2), 0.5, -0.3, 5.0, 2.9))
    P.append(strand("g_b1_r", (-1.5, -8.1, -4.35), (-0.12, 0, 0.22), 3.6, 1.4))
    P.append(strand("g_b2", (1.8, -8.1, -4.35), (-0.1, 0, -0.2), 3.2, 1.3))

    # ---- Fase 4: puntas salvajes arriba y melena larga por la espalda
    P.append(out("f_c1", (0.3, -8.4, -0.8), 0.05, -0.05, 8.5, 4.1))
    P.append(out("f_c2", (0, -8.2, 1.8), -0.7, 0, 8.5, 4.1))
    P.append(out("f_r1", (-2.3, -8.3, -0.5), -0.15, -0.55, 8.0, 3.7))
    P.append(out("f_r2", (-3.6, -7.2, 1.0), -0.4, -1.05, 7.5, 3.5))
    P.append(hang("f_h0", (0, -6.4, 3.9), -2.95, 0, 22.0, 5.6))
    P.append(hang("f_h1_r", (-2.2, -6.2, 3.7), -2.95, -0.3, 20.0, 5.0))
    P.append(hang("f_h2_r", (-3.8, -5.8, 2.2), -2.9, -0.5, 16.0, 4.4))
    P.append(hang("f_h3", (0, -3.0, 4.0), -3.0, 0, 18.0, 4.8))
    P.append(hang("f_h4_r", (-4.1, -6.0, -0.6), -2.9, -0.35, 11.0, 3.4))
    P.append(out("f_s1_r", (-4.3, -7.0, -1.6), -0.15, -1.15, 8.5, 3.6))
    P.append(out("f_s2_r", (-4.4, -5.2, 0.4), -0.5, -1.4, 8.0, 3.4))
    P.append(out("f_s3_r", (-4.3, -3.4, 2.2), -0.9, -1.6, 6.5, 3.0))
    P.append(hang("f_h5_r", (-4.5, -4.2, -1.8), -3.0, -0.3, 11.0, 3.4))
    P.append(hang("f_h6_r", (-4.4, -3.0, 1.0), -2.95, -0.45, 13.0, 3.6))
    P.append(strand("f_b1", (1.3, -8.2, -4.4), (-0.15, 0, -0.25), 5.5, 1.6))
    P.append(strand("f_b2_r", (-1.6, -8.2, -4.4), (-0.15, 0, 0.3), 5.0, 1.5))
    P.append(strand("f_b3", (-0.1, -8.3, -4.45), (-0.05, 0, 0.05), 4.0, 1.4))

    # ---- Ultra Ego: cabello como llamas que sube recto, con pico en la frente
    P.append(out("e_c1", (0, -8.5, -2.2), 0.05, 0, 13.0, 4.5, bend=0.03))
    P.append(out("e_c2", (0, -8.5, 0.6), -0.12, 0, 15.0, 4.7, bend=0.03))
    P.append(out("e_c3", (0, -8.0, 3.0), -0.4, 0, 13.0, 4.3, bend=0.04))
    P.append(out("e_c4", (0, -5.8, 4.0), -0.8, 0, 9.0, 3.7))
    P.append(out("e_r1", (-2.3, -8.4, -1.8), 0.05, -0.2, 12.5, 4.1, bend=0.03))
    P.append(out("e_r2", (-2.4, -8.3, 1.2), -0.2, -0.24, 13.0, 4.1, bend=0.03))
    P.append(out("e_r3", (-3.6, -7.6, -0.4), 0.0, -0.42, 10.5, 3.7))
    P.append(out("e_r4", (-3.4, -7.2, 2.4), -0.4, -0.45, 10.0, 3.5))
    P.append(out("e_r5", (-2.4, -6.4, 3.7), -0.7, -0.28, 9.0, 3.4))
    P.append(strand("e_peak", (0, -8.3, -4.45), (-0.06, 0, 0), 3.4, 2.4))

    # =========================================================== PEINADOS BASE (elegidos con /raza saiyajin)
    # ---- m1 Puntas salvajes: grandes puntas hacia los lados y atrás, flequillo de tres mechones
    P.append(out("m1_c1", (0, -8.3, -0.4), -0.25, 0, 8.0, 3.8))
    P.append(out("m1_c2", (0, -7.6, 3.0), -1.0, 0, 8.0, 3.8))
    P.append(out("m1_c3", (0, -4.6, 4.1), -1.55, 0, 6.5, 3.4))
    P.append(out("m1_r1", (-2.4, -8.2, -1.0), -0.1, -0.75, 7.5, 3.6))
    P.append(out("m1_r2", (-3.6, -7.0, 1.4), -0.5, -1.2, 7.0, 3.4))
    P.append(out("m1_r3", (-3.8, -4.8, 2.6), -1.0, -1.35, 6.0, 3.0))
    P.append(out("m1_r4", (-2.6, -6.4, 3.6), -1.1, -0.6, 7.0, 3.2))
    P.append(strand("m1_b1", (1.6, -8.1, -4.35), (-0.12, 0, -0.3), 5.0, 1.8))
    P.append(strand("m1_b2_r", (-1.8, -8.1, -4.35), (-0.12, 0, 0.35), 4.6, 1.7))
    P.append(strand("m1_b3", (0, -8.2, -4.45), (-0.1, 0, 0.1), 4.0, 1.6))
    # ---- m2 Puntas con mechón: puntas medianas y un mechón largo sobre la frente
    P.append(out("m2_c1", (0, -8.3, -0.2), -0.35, 0, 6.5, 3.6))
    P.append(out("m2_c2", (0, -7.4, 3.1), -1.05, 0, 6.5, 3.6))
    P.append(out("m2_c3", (0, -4.6, 4.1), -1.6, 0, 5.0, 3.2))
    P.append(out("m2_r1", (-2.4, -8.2, -0.8), -0.2, -0.7, 6.0, 3.4))
    P.append(out("m2_r2", (-3.6, -6.8, 1.6), -0.6, -1.15, 5.5, 3.2))
    P.append(out("m2_r3", (-2.6, -6.2, 3.6), -1.15, -0.55, 5.5, 3.0))
    P.append(strand("m2_bang", (-0.8, -8.2, -4.45), (-0.12, 0, 0.15), 7.5, 1.9))
    P.append(strand("m2_b1", (2.0, -8.1, -4.35), (-0.1, 0, -0.3), 4.0, 1.6))
    # ---- m3 Llama alta: todo el cabello sube recto, con pico en la frente
    P.append(out("m3_c1", (0, -8.5, -1.8), 0.05, 0, 9.5, 4.2, bend=0.03))
    P.append(out("m3_c2", (0, -8.4, 1.0), -0.15, 0, 10.5, 4.3, bend=0.03))
    P.append(out("m3_c3", (0, -7.6, 3.2), -0.45, 0, 9.0, 4.0))
    P.append(out("m3_r1", (-2.3, -8.4, -1.4), 0.05, -0.22, 9.0, 3.8, bend=0.03))
    P.append(out("m3_r2", (-2.4, -8.2, 1.4), -0.22, -0.26, 9.5, 3.8, bend=0.03))
    P.append(out("m3_r3", (-3.5, -7.4, 0.0), 0.0, -0.45, 7.5, 3.4))
    P.append(out("m3_r4", (-3.0, -6.6, 2.8), -0.5, -0.4, 7.0, 3.2))
    P.append(strand("m3_peak", (0, -8.3, -4.45), (-0.06, 0, 0), 3.4, 2.4))
    # ---- m4 Melena tranquila: mechones medianos que caen hacia atrás, sin picos parados
    P.append(out("m4_c1", (0, -8.2, 0.4), -0.9, 0, 7.0, 4.2))
    P.append(out("m4_c2", (0, -6.4, 3.8), -1.5, 0, 7.5, 4.0))
    P.append(out("m4_c3", (0, -3.6, 4.1), -2.0, 0, 6.5, 3.6))
    P.append(out("m4_r1", (-2.6, -8.0, 0.2), -0.8, -0.8, 6.5, 3.8))
    P.append(out("m4_r2", (-3.8, -6.4, 1.6), -1.3, -1.1, 6.5, 3.4))
    P.append(out("m4_r3", (-3.2, -4.2, 3.4), -1.9, -0.7, 6.0, 3.2))
    P.append(strand("m4_b1_r", (-1.4, -8.1, -4.4), (-0.12, 0, 0.3), 5.0, 2.0))
    P.append(strand("m4_b2", (1.4, -8.1, -4.4), (-0.12, 0, -0.25), 4.6, 1.9))
    # ---- m5 Corte liso con raya al medio: cortinas lisas a los lados y atrás
    P.append(strand("m5_b_r", (-1.3, -8.3, -4.45), (-0.15, 0, 0.5), 6.0, 3.0))
    P.append(strand("m5_b2_r", (-3.2, -8.0, -4.2), (-0.1, 0, 0.2), 6.5, 2.4))
    P.append(sheet("m5_s_r", (-4.3, -7.6, -0.8), -3.06, -0.1, 7.5, 4.6))
    P.append(sheet("m5_k", (0, -7.2, 3.9), -3.02, 0, 8.0, 7.0))
    P.append(sheet("m5_k_r", (-2.8, -7.2, 3.6), -3.04, -0.12, 7.5, 4.6))
    # ---- m6 Desordenado con picos: muchas puntas cortas en todas direcciones
    for i, (x, y, z, rx, rz, L) in enumerate([(0, -8.4, -1.4, 0.1, 0.1, 5.0), (0.6, -8.3, 1.2, -0.5, 0.2, 5.5), (0, -6.8, 3.7, -1.1, -0.1, 5.0),
                                              (0, -4.2, 4.1, -1.7, 0.1, 4.0), (-2.4, -8.2, -2.2, 0.3, -0.6, 4.5), (-2.6, -8.1, 0.8, -0.4, -0.9, 5.0),
                                              (-3.8, -6.6, -0.6, -0.1, -1.3, 4.5), (-3.6, -5.2, 2.6, -1.0, -1.1, 4.0), (-1.8, -7.4, 3.4, -0.9, -0.4, 5.0)]):
        P.append(out("m6_s%d_r" % i if x != 0 else "m6_s%d" % i, (x, y, z), rx, rz, L, 3.2, bend=0.1))
    P.append(strand("m6_b1", (1.2, -8.1, -4.4), (-0.15, 0, -0.4), 4.2, 1.7))
    P.append(strand("m6_b2_r", (-1.5, -8.1, -4.4), (-0.15, 0, 0.45), 4.0, 1.7))
    P.append(strand("m6_b3", (0, -8.2, -4.45), (-0.1, 0, -0.05), 3.4, 1.5))

    # ---- w1 Corte corto liso: melena corta hasta el cuello con flequillo
    P.append(sheet("w1_k", (0, -7.6, 3.9), -3.04, 0, 7.5, 7.4))
    P.append(sheet("w1_s_r", (-4.3, -7.6, 0.4), -3.1, -0.16, 7.0, 5.0))
    P.append(sheet("w1_f_r", (-4.2, -7.6, -2.6), -3.12, -0.12, 6.2, 3.4))
    P.append(strand("w1_b1", (0.4, -8.3, -4.45), (-0.1, 0, -0.1), 4.4, 3.0))
    P.append(strand("w1_b2_r", (-2.4, -8.2, -4.4), (-0.1, 0, 0.25), 4.0, 2.4))
    P.append(strand("w1_b3", (2.8, -8.2, -4.4), (-0.1, 0, -0.3), 3.8, 2.2))
    # ---- w2 Coletas: dos coletas que caen detrás de las orejas y flequillo
    P.append(sheet("w2_t_r", (-3.8, -5.6, 2.6), -2.85, -0.35, 13.0, 3.6))
    P.append(sheet("w2_k", (0, -7.4, 3.9), -3.05, 0, 5.0, 6.6))
    P.append(sheet("w2_s_r", (-4.3, -7.4, -1.6), -3.1, -0.1, 5.0, 3.0))
    P.append(strand("w2_b1", (0, -8.3, -4.45), (-0.1, 0, 0.05), 4.0, 2.6))
    P.append(strand("w2_b2_r", (-2.4, -8.2, -4.4), (-0.1, 0, 0.3), 3.6, 2.2))
    # ---- w3 Puntas rebeldes: puntas hacia arriba y atrás, como una cola de picos
    P.append(out("w3_c1", (0, -8.4, 0.2), -0.55, 0, 9.5, 4.2, bend=0.08))
    P.append(out("w3_c2", (0, -7.4, 3.2), -1.2, 0, 9.0, 4.0, bend=0.08))
    P.append(out("w3_r1", (-2.4, -8.2, 0.4), -0.5, -0.6, 8.0, 3.6, bend=0.08))
    P.append(out("w3_r2", (-3.6, -6.8, 1.8), -0.9, -1.0, 7.0, 3.2))
    P.append(out("w3_t", (0, -5.0, 4.0), -1.85, 0, 8.0, 3.8, bend=0.1))
    P.append(strand("w3_b1", (1.6, -8.1, -4.4), (-0.12, 0, -0.3), 5.0, 1.8))
    P.append(strand("w3_b2_r", (-1.6, -8.1, -4.4), (-0.12, 0, 0.3), 5.0, 1.8))
    P.append(sheet("w3_s_r", (-4.3, -7.2, -1.8), -3.08, -0.12, 6.0, 2.8))
    # ---- w4 Melena larga con flequillo: cabello largo hasta la cintura
    P.append(sheet("w4_k", (0, -7.6, 3.9), -3.02, 0, 18.0, 7.4))
    P.append(sheet("w4_k_r", (-2.8, -7.4, 3.6), -3.04, -0.1, 16.0, 4.8))
    P.append(sheet("w4_s_r", (-4.3, -7.6, -0.6), -3.1, -0.12, 14.0, 4.4))
    P.append(strand("w4_b1", (0.6, -8.3, -4.45), (-0.08, 0, -0.08), 5.4, 3.0))
    P.append(strand("w4_b2_r", (-2.4, -8.3, -4.45), (-0.08, 0, 0.2), 5.0, 2.6))
    # ---- w5 Cola de caballo alta
    P.append(sheet("w5_t", (0, -8.2, 3.4), -2.5, 0, 15.0, 4.4))
    P.append(sheet("w5_k", (0, -7.0, 3.9), -3.05, 0, 4.0, 6.6))
    P.append(strand("w5_b1", (1.4, -8.2, -4.45), (-0.1, 0, -0.25), 4.6, 2.0))
    P.append(strand("w5_b2_r", (-1.6, -8.2, -4.45), (-0.1, 0, 0.3), 4.2, 2.0))
    P.append(sheet("w5_s_r", (-4.3, -7.2, -2.0), -3.08, -0.1, 6.5, 2.4))
    # ---- w6 Melena larga ondulada con raya al lado
    P.append(sheet("w6_k", (0, -7.6, 3.9), -2.98, 0, 20.0, 7.6))
    P.append(sheet("w6_s_r", (-4.3, -7.6, 0.0), -3.0, -0.22, 17.0, 4.8))
    P.append(sheet("w6_f_r", (-4.1, -7.8, -2.6), -3.05, -0.18, 15.0, 3.4))
    P.append(strand("w6_b1", (1.0, -8.3, -4.45), (-0.12, 0, -0.45), 6.0, 3.4))
    # ---- w7 Moño con flequillo
    P.append(out("w7_bun", (0, -7.6, 3.6), -1.15, 0, 4.2, 6.0, bend=0.0, sink=1.4))
    P.append(out("w7_bun2", (0, -8.6, 2.6), -0.85, 0, 3.0, 5.0, bend=0.0, sink=1.2))
    P.append(sheet("w7_s_r", (-4.3, -7.4, -1.6), -3.1, -0.1, 6.0, 2.8))
    P.append(strand("w7_b1", (0, -8.3, -4.45), (-0.1, 0, 0.05), 4.0, 3.2))
    P.append(strand("w7_b2_r", (-2.6, -8.2, -4.4), (-0.1, 0, 0.3), 3.6, 2.2))

    # ---- Blue Evolución: puntas altas y afiladas que suben como llamas
    P.append(out("v_c1", (0, -8.5, -1.8), 0.0, 0, 13.0, 4.0, bend=0.04))
    P.append(out("v_c2", (0, -8.5, 0.8), -0.15, 0, 14.5, 4.2, bend=0.04))
    P.append(out("v_c3", (0, -7.8, 3.2), -0.42, 0, 12.5, 3.9, bend=0.05))
    P.append(out("v_c4", (0, -5.6, 4.1), -0.85, 0, 9.0, 3.4))
    P.append(out("v_r1", (-2.3, -8.4, -1.4), 0.02, -0.2, 12.5, 3.8, bend=0.04))
    P.append(out("v_r2", (-2.4, -8.3, 1.4), -0.2, -0.26, 13.0, 3.8, bend=0.04))
    P.append(out("v_r3", (-3.6, -7.6, -0.2), 0.0, -0.45, 10.5, 3.4))
    P.append(out("v_r4", (-3.3, -7.0, 2.6), -0.45, -0.5, 10.0, 3.2))
    P.append(strand("v_peak", (0, -8.3, -4.45), (-0.06, 0, 0), 3.6, 2.4))

    # los haces de mechones (sheet) se expanden en mechones sueltos
    P = [x for item in P for x in (item if isinstance(item, list) else [item])]
    # los peinados elegidos y Blue Evolución son más gruesos
    for sp in P:
        g = sp.name[:sp.name.index("_") + 1]
        if (g[0] in "mw" and g[1].isdigit()) or g == "v_":
            sp.width *= 1.12
    # volumen: cada punta grande lleva mechones de relleno a su lado
    extra = []
    for s in P:
        g = s.name[:s.name.index("_") + 1]
        if g == "c_" or s.hang or s.rx > 2.0 or s.length < 6.0:
            continue
        if abs(s.pos[0]) < 0.5 and abs(s.rz) < 0.05:
            for side in (-1, 1):
                extra.append(S(s.name + "v" + ("a" if side < 0 else "b"), (s.pos[0] + side * 1.2, s.pos[1] + 0.3, s.pos[2] + 0.4),
                               s.rx - 0.12, side * 0.28, s.length * 0.78, s.width * 0.82, s.bx, side * 0.15, s.flat))
        else:
            sg = -1 if s.rz < 0 else 1
            extra.append(S(s.name + "v", (s.pos[0], s.pos[1] + 0.4, s.pos[2] + 0.9), s.rx - 0.2, s.rz + sg * 0.2,
                           s.length * 0.8, s.width * 0.85, s.bx, s.bz, s.flat, False, s.taper))
    P = P + extra
    out_ = []
    for s in P:
        out_.append(s)
        if "_r" in s.name:
            x, y, z = s.pos
            out_.append(S(s.name.replace("_r", "_l"), (-x, y, z), s.rx, -s.rz, s.length, s.width, s.bx, -s.bz, s.flat, s.hang, s.taper))
    return out_


# ------------------------------------------------------------------ malla (igual que en el juego)
STEPS = 6


def direction(a, b):
    return (math.cos(a) * math.sin(b), -math.cos(a) * math.cos(b), -math.sin(a))


def norm(v):
    l = math.sqrt(sum(c * c for c in v)) or 1.0
    return tuple(c / l for c in v)


def cross(a, b):
    return (a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0])


def mesh(s, sway=(0.0, 0.0)):
    """Anillos (lista de 4 esquinas + normales) a lo largo del mechón."""
    d0 = direction(s.rx, s.rz)
    ref = (0, 0, 1) if abs(d0[2]) < 0.9 else (1, 0, 0)
    c = tuple(s.pos[i] - d0[i] * 0.6 for i in range(3))
    rings = []
    for k in range(STEPS + 1):
        t = k / STEPS
        a = s.rx + s.bx * t * t + sway[0] * t ** 1.5
        b = s.rz + s.bz * t * t + sway[1] * t ** 1.5
        d = direction(a, b)
        u = norm(cross(d, ref))
        v = cross(d, u)
        w = s.width * ((1 - s.taper * t) if s.taper > 0 else (1 - t) ** 0.85)
        corners, normals = [], []
        for j in range(4):
            ph = math.pi / 4 + j * math.pi / 2
            n = tuple(u[i] * math.cos(ph) + v[i] * math.sin(ph) * s.flat for i in range(3))
            corners.append(tuple(c[i] + u[i] * w * math.cos(ph) + v[i] * w * s.flat * math.sin(ph) for i in range(3)))
            normals.append(norm(n))
        rings.append((corners, normals, t))
        c = tuple(c[i] + d[i] * s.length / STEPS for i in range(3))
    return rings


def java(sp):
    groups = [s.name[:s.name.index("_") + 1] for s in sp]
    L = ["// GENERADO por tools/gen_hair.py — no editar a mano.", "package com.bdsz.client;", "",
         "/** Mechones del cabello: x, y, z (raíz), xRot, zRot, largo, ancho, curva x, curva z, aplanado, colgante. */",
         "public final class HairSpikes {", "    private HairSpikes() {}", "",
         "    public static final String[] GROUP = {"]
    for i in range(0, len(groups), 12):
        L.append("            " + ", ".join('"%s"' % g for g in groups[i:i + 12]) + ",")
    L += ["    };", "", "    public static final float[][] S = {"]
    for s in sp:
        vals = [s.pos[0], s.pos[1], s.pos[2], s.rx, s.rz, s.length, s.width, s.bx, s.bz, s.flat, 1.0 if s.hang else 0.0, s.taper]
        L.append("            {" + ", ".join(("%.4f" % v).rstrip("0").rstrip(".") + "F" for v in vals) + "},  // " + s.name)
    L += ["    };", "}", ""]
    return "\n".join(L)


def textures():
    # degradado del mechón: raíz más oscura -> punta brillante, con vetas finas
    img = Image.new("RGBA", (16, 32))
    for y in range(32):
        t = y / 31
        for x in range(16):
            base = 150 + 105 * t ** 0.8
            if x % 4 == 0:
                base -= 18
            if x % 4 == 2:
                base += 10
            v = int(max(0, min(255, base)))
            img.putpixel((x, y), (v, v, v, 255))
    img.save(os.path.join(TEXDIR, "hair_strand.png"))
    # cráneo (cubos del casquete)
    cap = Image.new("RGBA", (64, 32))
    for y in range(32):
        for x in range(64):
            v = 200 if (x + y) % 5 else 176
            if x % 3 == 0:
                v -= 14
            cap.putpixel((x, y), (v, v, v, 255))
    cap.save(os.path.join(TEXDIR, "saiyan_hair.png"))


def main():
    sp = spikes()
    open(JAVA, "w").write(java(sp))
    textures()
    print("%d mechones" % len(sp))
    return sp


if __name__ == "__main__":
    main()
