# Totally Waifu? (Forge 1.20.1)

La mesa de crafteo, el horno, el cofre y la mesa de encantamientos cobran vida como chicas anime (diseños originales).

## Cómo despertarlas
Fabrica un **Corazón de Waifu** (tinte rosa en forma de corazón con un lingote de oro en el centro):

```
P . P
P G P
. P .
```

Úsalo sobre una mesa de crafteo, un horno, un cofre o una mesa de encantamientos **colocados**. El bloque se convierte en la waifu, que conserva lo que tenía dentro.

- **Clic derecho**: abre el menú de su mesa.
- **Shift + clic derecho**: abre su panel, con su mochila, los modos **Seguir / Quieta / Trabajar** y el botón **♥ Guardar** (se guarda en un corazón para llevarla).
- **Regálale una flor**: se cura y se pone feliz.
- Si la derrotan, no muere: se esconde en su corazón con todas sus cosas.

## Las cuatro
| Waifu | Bloque | Funciones |
|---|---|---|
| **Kozue** | Mesa de crafteo | Mesa de crafteo. **Pedidos**: pon un objeto en su ranura de pedido y lo fabrica sola con lo que tenga en la mochila. Trabajando: tala árboles naturales y mina menas expuestas. |
| **Homura** | Horno | Su horno funciona siempre (incluso cerrado) y **al doble de velocidad**; se recarga solo desde su mochila. Trabajando: caza animales de granja (nunca deja menos de 2 de cada especie) y mina carbón. |
| **Kura** | Cofre | **Cofre doble (54)**. Recoge todo lo que hay tirado y vacía las mochilas llenas de las demás (no toca su primera fila). Botón **Ordenar**. |
| **Shiori** | Mesa de encantamientos | Encanta con **poder máximo** (como 15 estanterías) y te presta su lapislázuli. Dispara magia a los monstruos, guarda su experiencia, **repara tu equipo** con ella y te la da con **Dar XP**. Trabajando: mina lapislázuli. |

En modo **Seguir** te acompañan y te defienden.

## Novedades 1.1.0 — modelos estilizados
- **Cuerpo propio** (ya no usan la skin del jugador): cuello, pecho, busto, cintura estrecha, cadera ancha, piernas largas que se afinan (muslo, pantorrilla y pie), brazos delgados con hombros y cabeza un poco más pequeña.
- **Caras de anime en alta resolución**: ojos grandes con iris en degradado, aro, pupila y dos brillos, pestañas gruesas con alita, cejas, rubor con rayitas y boca. Seis expresiones (normal, parpadeo, feliz, sorprendida, concentrada y cansada).
- **Sombreado cel** en toda la ropa: luz desde arriba, lados más oscuros y contorno fino.
- **Ropa más detallada**: medias con liga, botas con cordones, lazos 3D, enagua, cofia, gafas de forja, delantal con llama, capa, mangas acampanadas, gargantilla y joyas.
- Pose de reposo con la cadera ladeada y la falda que se abre al caminar.

## Novedades 1.2.0: cuerpos lisos, sin cubos
- El cuerpo ahora es una **malla suave**: cada parte (cuello, torso, brazos, piernas, falda, mangas, botas y cinturón) está hecha de anillos redondeados que se afinan y ensanchan, con normales suaves. Ya no se notan los cubos: se ve liso y redondeado.
  - Torso con busto, cintura estrecha y cadera.
  - Piernas con muslo, rodilla, pantorrilla, tobillo y pie.
  - Brazos con hombro, codo, muñeca y mano.
- **Cabeza redondeada** con la cara pintada al frente.
- **Caras nuevas a 128 px**:
  - Ojos grandes y limpios: iris en degradado, pupila, brillos y pestaña gruesa con alita.
  - Pliegue del párpado, cejas afiladas y rubor suave.
  - Seis expresiones.
- **Texturas limpias**: colores lisos sin ruido de píxeles, pliegues suaves en la tela y contornos finos entre prendas.
- La falda se abre de verdad al caminar: se deforma la malla.
- Generador: `tools/gen_smooth.py` (la vista previa usa la misma geometría que el juego).

## Novedades 1.4.0 (sobre el estilo de la 1.2.0)
- **Caras nuevas al estilo de las skins detalladas** (no anime):
  - Ojos almendrados y entrecerrados con pestaña gruesa y alita, sombra de párpado, iris de tres tonos con pupila y brillo, y pestaña inferior.
  - Cejas finas, nariz sombreada, labios con dos tonos y rubor suave.
  - Seis expresiones.
- **Más altas y estilizadas**: piernas más largas y finas (muslo, rodilla, pantorrilla y tobillo), cabeza un poco más pequeña y tamaño general mayor.
- **Torso femenino**: hombros y espalda más estrechos, cintura de avispa y cadera ancha; brazos más finos y largos y cuello esbelto.
- **Cabello mejorado**:
  - Mechones finos extra entre los mechones, en capas, para dar más volumen.
  - Cada mechón tiene su propio tono.
  - Degradado de la raíz a las puntas y brillo en la coronilla.

## Novedades 1.5.0: aspecto "Noa"
- Aspecto nuevo **Noa**, en estilo de bloques como la hoja de personaje de referencia:
  - Bob negro con flequillo recto y puntas abultadas a los lados.
  - Orejas de elfa con interior lavanda, ojos turquesa con pestaña gruesa y piel pálida.
  - Chaqueta celeste corta con cuello esponjoso, lazo negro, bolsillos y puños.
  - Enterizo negro con emblema rojo, cinturón y tiras rojas.
  - Guantes sin dedos, medias oscuras transparentes con liga y botas blancas de plataforma con detalles rojos.
- En el panel de cualquier waifu (Shift + clic derecho), el botón **Aspecto** cambia entre su ropa y Noa. Sus funciones no cambian y el aspecto se guarda.
- Cuando está quieta, sostiene las manos frente al pecho.
- Generador: `tools/gen_noa.py`.

## Novedades 1.6.0: aspecto personalizado (modelos de Blockbench)
- Tercer aspecto: **personalizado**. El mod carga un modelo de **Blockbench (Bedrock, .geo.json)** y su textura desde cualquier **paquete de recursos**:
  - `assets/totallywaifu/geo/custom.geo.json`
  - `assets/totallywaifu/textures/entity/custom/custom.png`
- Los huesos `head`, `body`, `rightArm`, `leftArm`, `rightLeg` y `leftLeg` se animan; los demás siguen a su padre. Se respetan los pivotes, las rotaciones de huesos y de cubos, `inflate` y `mirror` (con UV de caja).
- Se recarga con F3 + T, sin reiniciar.
- `python3 tools/gen_noa.py pack DIR` genera el paquete **TotallyWaifu_Personalizado.zip** con Noa ya exportado para editarlo en Blockbench.
