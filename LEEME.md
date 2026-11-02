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

## 7.8.0 — Kaori vuela, casas, generador y comercio; fusiones y domo
- **Kaori**: la armadura le queda bien (modelo más holgado y la ropa 3D se oculta debajo). Vuela contigo cuando
  vuelas (pose de Superman, estela). Lanza ráfagas de ki y, contra enemigos fuertes, una onda de ki explosiva.
- **Generador Protector**: Kaori lo alimenta con los lingotes de cobre de su mochila (sola cuando le queda poca
  energía) y lo enciende o apaga desde su menú.
- **Casas-cápsula** que se desbloquean pasando tiempo con ella: Choza; Casa segura de piedra (1 día de Minecraft);
  Casa del Guerrero Z con generador y huerto de semillas (3 días); Base Cápsula con encantamientos (6 días).
- **Comercio**: cerca de ella los aldeanos te hacen descuento. En su menú: Ofertas (lo mejor y lo que piden),
  Comerciar (vende por ti y compra lo mejor con 30% de descuento) y Conseguir (va por lo que falta).
- **Fusiones**: tu misma especie ya no te ataca ni te hace daño (zombis, esqueletos, creepers, endermans, wardens, gólems).
- **Domo protector**: los proyectiles enemigos se revisan cada tick con su trayectoria (ninguno atraviesa) y nada de
  afuera te lastima aunque estés en el borde del domo.

## 7.9.0 — Vuelo de Kaori arreglado y una sola Kaori
- Vuela como el jugador: boca abajo con la cabeza al frente, el cuerpo sigue la dirección del vuelo (sube y baja
  inclinándose), suspendida queda de pie con las piernas colgando. Ya no se asfixia ni se daña con bloques o paredes.
- Solo puede haber una Kaori por jugador: la cápsula trae a la misma (con su mochila, equipo y casa) aunque esté
  lejos, la copia vieja desaparece sola, y el huevo no crea otra si ya tienes una.

## 8.0.0 — Bloque Especial 4: los tres Ultra Instintos
- **Lista de Entrenamiento VII** (mapa, estrella del Nether, 2 diamantes, 2 fragmentos de amatista, lingote de ki
  azul y negro; requiere el Bloque Especial 3) desbloquea el **Bloque Especial 4**:
  - **Ultra Instinto Imperfecto**: cabello negro estilo Dios, aura gris con rayos blancos; transformación blanca,
    exagerada y dramática: esfera de ki blanca que destella, rayos, ráfagas de ki y un ciclón blanco hasta el cielo.
  - **Ultra Instinto Perfecto**: cabello plateado, aura blanca-azul clara; transformación tranquila; cuerpo un poco
    encorvado, como cansado.
  - **Ultra Instinto Propio**: aura blanca pura y divina (halo), transformación pacífica; pose serena y relajada.
- **El cuerpo se mueve solo**: esquivan golpes, flechas, bolas de fuego y explosiones (60 %, 80 % y 90 %); se
  teletransportan al cielo, a otro punto del suelo o detrás del atacante (y contraatacan); ven venir proyectiles,
  TNT y creepers y se apartan antes del impacto. Están entre las fases más fuertes (por encima de Gohan Bestia,
  Rosé y Legendario Z).
- **Kaori** ya no vuela: cuando vuelas saca una pequeña nave de una cápsula y te acompaña sentada en ella.
  Dos fases nuevas: **Super Saiyajin 3** (cabello largo) y **Super Saiyajin Dios** (cabello rojo).

## 8.0.1 — Arreglo del Bloque Especial 4
- El bloque desbloqueado se recortaba al 3 cada vez que se leían tus datos: los Ultra Instintos quedaban bloqueados
  (te transformabas en la fase que ya tenías elegida, p. ej. Blue) y el bloque "se volvía a bloquear". Ya se guarda
  bien. `/raza bloque` también acepta el nivel 8.

## 8.1.0 — Fusión con el Dragón del End
- Lanza el arete cerca del **Dragón del End** (hasta 10 bloques) y te fusionas: la **fusión más poderosa** del mod
  (poder x80), más alto y fornido, alas, cuernos, púas y cola 3D, ojos morados, engreído (pose con las manos en la
  cintura y se burla de sus enemigos). Fuerza V, Resistencia IV, Regeneración III, inmune al fuego, planea con Shift.
  **X**: Aliento de dragón. Afinidad: los tres Ultra Instintos, Legendario Fase 3 y Ultra Ego.
- **25 % de probabilidad de quedarte fusionado para siempre.** Para salir: `/desfusionar` o el **Elixir de
  Separación** (aliento de dragón, lágrima de ghast, manzana dorada y fragmento de amatista).
- Las poses del Ultra Instinto Perfecto (encorvado) y Propio (sereno) ahora se ven mientras te transformas.
