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

## Novedades 1.3.0: estilo cúbico detallado con cara de anime
Las cuatro comparten ahora la misma base de diseño:
- Complexión delgada y atlética.
- Melena **bob** en bloques, con flequillo suave, volumen a los lados y algún mechón rebelde.
- Ojos grandes de anime con brillos marcados.
- **Orejas puntiagudas de elfa** con la punta de color.
- Piel pálida y limpia.

Cada una conserva su color y su ropa de oficio:
| Waifu | Cabello | Ojos | Puntas de las orejas |
|---|---|---|---|
| **Shiori** (la descripción exacta) | negro | turquesa | lavanda |
| Kozue | castaño | verdes | verde hoja |
| Homura | rojo, con dos coletitas | ámbar | naranja |
| Kura | rubio | azules | rosa |

**Estilo**: geometría estrictamente cúbica (low-poly Minecraft), con texturas pintadas en alta resolución, limpias y sin ruido.

**Capas separadas**, cada una con su textura:
- `skin*.png`: cuerpo y cara, con 6 expresiones.
- `clothes.png`: la ropa 3D.
- `hair.png`: el cabello, las orejas y los accesorios.
- La **armadura** del juego: el panel tiene 4 ranuras (casco, pechera, grebas, botas). La ropa que tapa la armadura se oculta sola.

**Pose**: en reposo están de pie, con los brazos un poco levantados, sosteniendo su objeto frente al pecho:
- Kozue: una mesa de crafteo.
- Homura: un horno.
- Kura: un cofre.
- Shiori: un libro encantado.

**Skins de jugador**: la carpeta `skins/` tiene la skin de cada una en formato "slim" (brazos delgados):
- 64x64, para usarla como jugador.
- 512x512 (HD).
