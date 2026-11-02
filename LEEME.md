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

## 7.3.0 — Poder de fusión, habilidades y poses
- **Poder ×40** fusionado en estado base (×1.5 extra si la transformación tiene afinidad con la fusión).
  Sin Modo Ozaru mientras estás fusionado.
- **Tiempo**: transformarte gasta más rápido el tiempo de fusión y el Kaioken aún más (el HUD muestra la
  velocidad ⚡ o ★ Afinidad). Afinidades: Zombi (SSJ4, Legendario, Bestia), Esqueleto (Ultra, Dios, Blue),
  Enderman (Ultra Ego, Rosé, Dios), Creeper (SSJ2, SSJ3, Bestia), Gólem (Blue, Blue Evolución, SSJ1),
  Warden (Legendario II/III, Ultra Ego).
- **Habilidades (X)**: Zombi: no necesitas comer. Creeper: sisea, parpadea y explota (sin dañarte). Esqueleto:
  arco de ki amarillo con 5 flechas de energía. Gólem: al golpear, tus brazos se cubren de ki con forma de brazos
  de gólem. Warden: rayo sónico, sientes las vibraciones (lo que se mueve brilla) y das Oscuridad a otros jugadores.
- **Poses de base** al estar quieto: brazos cruzados (gólem), brazos atrás (warden), brazos al frente (zombi),
  mano en la cadera (esqueleto), brazos colgando (enderman), agazapado (creeper). Aura de partículas de la fusión.
- **Al separarte**: el mob vuelve a aparecer a tu lado; quedas agotado unos segundos (debilidad, lentitud),
  sin ki y en estado base.

## 7.4.0 — Kaori, la acompañante
Kaori es una mecánica e inventora (diseño original): coleta alta color vino, ojos verdes que pestañean, gafas de
mecánica, chaqueta oliva con mangas remangadas, top blanco, cinturón con estuches de cápsulas, shorts cargo y botas;
toda la ropa tiene piezas 3D. Amable y muy educada, pero altanera, ruda y fuerte.
- **Invocarla**: Cápsula de Kaori (hierro, oro, redstone y un reloj) o el huevo del creativo. Clic derecho: su menú;
  Shift + clic derecho: su mochila (27 ranuras, armadura, arma y escudo).
- **Regalos**: 3 Semillas del Ermitaño (al terminártelas hay que esperar 2 días de Minecraft) y 20 Tazones de Arroz
  una vez al día (vida, regeneración, mitad del ki y 5 minutos sin hambre).
- **Cápsula-choza**: lanza una cápsula y aparece una choza amueblada (cama, cofre, horno, ahumador, barril, mesa de
  crafteo, faroles); se vuelve a guardar desde el menú.
- **Ayuda**: fabrica con su mochila (palos, herramientas, antorchas, cofre, cama… o el nombre en inglés), farmea
  madera, piedra, carbón, hierro, tierra y arena, ordena cofres, recoge objetos y hace agricultura (cosecha, siembra
  y ara junto al agua).
- **Combate**: golpes fuertes con ki, ráfagas de ki a distancia, escudo; protege a su dueño. Tiene 30 de vida, no
  puede morir: se come una Semilla del Ermitaño de su mochila o descansa hasta recuperarse.
- Arte: `python3 tools/gen_kaori.py` (texturas, ropa 3D y mechones).

## 7.5.0 — Kaori mejorada
- Kaori aparece al craftear su cápsula: **3 bloques de hierro, 3 bloques de cobre, 1 diamante y 1 Semilla del
  Ermitaño** (sin forma). La cápsula sirve después para traerla a tu lado.
- Cara nueva, distinta a la de Tanny: ojos pixel-art estilo Minecraft (azul oscuro, brillo cuadrado, pestaña recta),
  sonrisa ladeada; sin pestañas 3D de anime. Aretes de tuerca dorada y llave inglesa en el cinturón.

## 7.6.0 — Kaori: cara de skin clásica y Super Saiyajin
- Cabello negro y ojos verdes; cara estilo skin clásica de Minecraft (cada ojo: blanco por fuera e iris verde de
  tres tonos por dentro), como la imagen de referencia.
- Se transforma desde su menú: **Base, SSJ 1 y SSJ 2** (cabello dorado brillante que se eleva, aura dorada, rayos en
  SSJ 2, grito de transformación, onda de choque; más daño, velocidad, armadura y vida; ráfagas de ki doradas).
  Al quedar sin vida vuelve a su estado base.
- Semillas del Ermitaño: las que regala Kaori solo se comen; las de aldeas y cofres sí se pueden plantar.

## 7.7.0 — Ojos de Kaori y chaqueta roja
- Ojos amarillos con cejas negras, pestaña superior gruesa con alita hacia afuera, brillo y sin nariz; pestañea, y
  cambia las cejas según la expresión (seria, sorprendida, cansada).
- Chaqueta roja con mangas remangadas y engranaje dorado en la espalda.
