# Block Dragon Super Z (Forge 1.20.1)

`/raza sayajin` · **K** menú de fases y Kaioken · **P** transformarse · **U** aura.
Cabello 3D: `python3 tools/gen_hair.py`

## 7.1.0 — Fusiones y más movilidad

**Aretes de Fusión** (mesa de crafteo, da 2): `oro hierro oro / oro vara-de-blaze oro / _ hierro _`.
1. Clic derecho: te pones un arete (animación). Shift + clic derecho: te lo quitas.
2. Clic derecho otra vez (o Q): lanzas el otro arete. Si lo recoge un zombi, esqueleto, enderman, creeper,
   gólem de hierro o warden, empieza la fusión: esfera de poder amarilla, rayos y ciclón amarillo hasta el cielo.
3. Mezclas tu apariencia con la del mob, ganas sus habilidades (tecla **X**: habilidad especial) y conservas
   tus fases y transformaciones. Duración: zombi 12 min, esqueleto 10, enderman 9, creeper 8, gólem 7, warden 5.
   Al terminar (o si mueres) los aretes se rompen.

Movilidad: pose de recarga con pulsos de energía, golpes con giro de cadera e impulso hacia el enemigo,
retroceso al lanzar Kamehameha / Big Bang, vuelo que se inclina en las curvas, flota subiendo y bajando y
cámara que se mece un poco al volar.

## 7.2.0 — Traje de fusión 3D
Tecla **N**: separarte de la fusión cuando quieras (los aretes se rompen). Al fusionarte llevas un traje 3D:
pantalón de karate saiyajin azul (zubon holgado con puño en el tobillo), obi azul con nudo y puntas, guantes
blancos y el pecho y los hombros del mob, con piezas 3D propias: costillas y columna (esqueleto), placa de hierro
y enredaderas (gólem), zarcillos brillantes y alma en el pecho (warden), jirones de camisa (zombi), púas con
punta morada (enderman) y capucha verde (creeper). La cara solo toma un toque del mob. Geometría y texturas:
`python3 tools/gen_fusion_gi.py`.
