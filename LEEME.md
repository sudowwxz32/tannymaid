# TannyMaid Adventures — Forge 1.20.1 (compatible con Cobblemon 1.5.2)

Tanny es una maid de aventuras: dulce, leal y un poco coqueta. Te sigue, te defiende,
cosecha, ordena cofres, carga tus cosas y pelea junto a su único Pokémon: **Lucario**.

- Minecraft **1.20.1** · Forge **47.x**
- Cobblemon **1.5.2 (Forge)**: opcional pero recomendado. Sin Cobblemon, Tanny funciona igual pero pelea sola.

---

## 1. Cómo convertir este proyecto en el archivo .jar

Necesitas internet y un **JDK 17** (por ejemplo Temurin 17: https://adoptium.net).

**Windows**
1. Descomprime el ZIP.
2. Abre la carpeta `tannymaid`, escribe `cmd` en la barra de direcciones y presiona Enter.
3. Ejecuta: `gradlew build`

**Linux / macOS**
```
cd tannymaid
chmod +x gradlew
./gradlew build
```

La primera vez tarda varios minutos (descarga Minecraft y Forge). Al terminar, el mod está en:

```
build/libs/tannymaid-forge-1.20.1-1.2.0.jar
```

## 2. Instalación

Copia en la carpeta `mods` de tu instalación Forge 1.20.1:
- `tannymaid-forge-1.20.1-1.2.0.jar`
- `Cobblemon-forge-1.5.2+1.20.1.jar` y sus dependencias (Kotlin for Forge, Architectury API si tu versión lo pide)

Para un servidor, el mod va tanto en el servidor como en los clientes.

## 3. Invocar a Tanny

Crafteo **sin forma**: `1 lingote de oro + 1 mapa vacío` → **Contrato de Tanny Maid**.
Usa el contrato (clic derecho en el suelo o al aire) y Tanny aparece vinculada a ti.
Solo puedes tener una Tanny; si usas otro contrato estando ella cargada, simplemente viene a ti.

## 4. Controles

| Acción | Resultado |
|---|---|
| Clic derecho | Abre su inventario: su vida, su modelo 3D, la barra de herramientas (pico, pala, hacha, espada, escudo) y su mochila de 27 espacios. La espada y el escudo se ven en sus manos; el escudo bloquea la mitad de los golpes |
| Shift + clic derecho | Cambia de modo: Seguir → Quedarse → Defensa → Granja |
| Clic derecho con comida / poción | La cura (si está debilitada, la reanima) |

## 5. Comandos

| Comando | Qué hace |
|---|---|
| `/tanny` | Lista de órdenes |
| `/tanny follow` | Te sigue y te defiende (radio 10) |
| `/tanny stay` | Se queda en el punto actual |
| `/tanny sethome` | Marca tu posición como su casa |
| `/tanny home` | Va a casa y se queda ahí (se teletransporta si está lejos o en otra dimensión) |
| `/tanny tp` | Se teletransporta a ti (también entre dimensiones) |
| `/tanny organize` | Ordena todos los cofres y barriles en un radio de 8 bloques alrededor de ella |
| `/tanny farm` | Cosecha cultivos maduros y vuelve a sembrar en un radio de 8 bloques |
| `/tanny defend` | Defensa activa: te sigue y ataca hostiles en radio 16 |
| `/tanny inventory` | Abre su inventario desde cualquier lugar |
| `/tanny lucario` | Saca o guarda a Lucario |
| `/tanny collect` | Activa/desactiva recoger objetos del suelo |
| `/tanny status` | Vida, modo, estado de Lucario y posición |
| `/tanny dismiss` | La despide: te devuelve su inventario, herramientas y el contrato |
| `/tanny recolectar <madera\|piedra\|carbon\|hierro\|tierra> [1-64]` | Busca y pica esos bloques cerca (máx. 64 por pedido). Piedra, carbón y hierro necesitan un pico en su barra |
| `/tanny craftear <objeto> [cantidad]` | Fabrica con lo que tenga en su mochila: `palos`, `tablones`, `pico_piedra`, `hacha_hierro`, `espada_diamante`, `escudo`, `pokebola`, `superball`, `ultraball`… Si faltan cosas: "Amo, requiero de más materiales para lo que me pidió." |
| `/tanny pan` | Te da 24 panes (cada 5 minutos) |
| `/tanny seguro` | Te lleva al lugar seguro más cercano: la superficie si estás bajo tierra, tu cama, su casa o el punto de aparición |
| `/tanny parar` | Deja de recolectar |
| `/tanny gardevoir` | Saca o guarda a Gardevoir (siempre a tu lado) |
| `/tanny bayas` | Te da bayas curativas de Cobblemon: Aranja, Zidra, Ziuela, Zanama… (cada 5 min) |
| `/tanny pc` | Abre tus cajas de Pokémon (el PC de Cobblemon) sin necesitar el bloque |
| `/tanny pokebolas` | Abre su bolsa de Poké Balls |
| `/tanny avisar <pokémon>` | Tanny vigila ese Pokémon: cuando aparece cerca (64 m) te avisa en el chat y sale un medidor de metros con flecha |
| `/tanny noavisar <pokémon\|todos>` · `/tanny avisos` | Dejar de vigilar / ver la lista |

### Menú (clic derecho)

- Pestaña **Equipo**: armadura (casco, pechera, pantalones, botas: le da protección real y se ve puesta),
  herramientas y mochila de 27.
- Pestaña **Poké Balls**: bolsa de 27 que solo acepta Poké Balls (las que craftea o recoge van ahí solas)
  y botón para abrir tus cajas.
- Pestaña **PC Pokémon**: abre el PC de Cobblemon.
- Panel **Órdenes** a la derecha: seguir, quedarse, defender, granja, casa, venir, ordenar cofres,
  recoger, Lucario, Gardevoir, pan, bayas y lugar seguro.

## 6. Mecánicas

**No puede morir.** Al llegar a 0 de vida queda *debilitada*: se sienta, no recibe daño y se
recupera sola en 60 segundos. Si está en lava, fuego o el vacío, se teletransporta a casa o
contigo. Puedes reanimarla al instante con una poción, manzana dorada, botella de miel, pastel
o con pociones, bayas y revivir de Cobblemon. La comida normal acorta el tiempo de descanso.

**Defensa.** Ataca a mobs hostiles cerca de ti, a quien te lastime y a los hostiles que tú
golpees. Prioriza a quien te está atacando. Deja en paz a endermans y piglins zombificados
que no estén enojados. No te ataca a ti ni a otros Pokémon.

**Lucario (nivel 50).** Es un Lucario real de Cobblemon. Sale solo cuando Tanny entra en
combate y vuelve a su Poké Ball unos 15 segundos después de terminar la pelea. Golpea cuerpo
a cuerpo y lanza *Esfera Aural* a distancia. Si se debilita, descansa 60 segundos antes de
volver a salir. No se puede capturar (la Poké Ball rebota y te la devuelve), no se puede
cambiar ni liberar, y nunca se guarda en tu equipo.

**Gardevoir (nivel 55, hembra).** Siempre está a tu lado, aunque Tanny se quede en casa.
Es más protectora que Lucario: vigila todo lo hostil que se te acerque, se pone entre tú y el
enemigo, te quita un 30 % del daño que recibes y te cura con *Deseo* cuando tienes poca vida.
Ataca con *Psíquico* y *Fuerza Lunar* (un poco más fuerte que Lucario). Es muy cariñosa: de vez
en cuando te toma la mano, te abraza o te deja corazones. Si se debilita vuelve en 60 segundos.

**Permanencia.** Su posición, modo, casa, vida e inventario se guardan con el mundo. Si cambias
de dimensión o reapareces tras morir estando en modo seguir/defensa, viaja contigo.

**Expresiones y animaciones.** Parpadeo natural, respiración, gestos de cabeza al hablar,
sonrisa al hablar, cara de sorpresa al recibir daño, mirada concentrada en combate y pose
sentada con ojos cerrados al debilitarse. Mide ~1.66 m.

## 7. Notas y límites conocidos

- **Apariencia 1.2.0.** Texturas en alta resolución (4x): ojos grandes estilo anime con iris en
  degradado y brillos, cejas, rubor y 6 expresiones. Cabello 3D con capas, flequillo puntiagudo,
  mechones largos, ahoge y brillo anime; vestido 3D con falda redonda y enaguas, delantal, lazos,
  puños, medias con volante y zapatos. Todo se genera con `python3 tools/gen_tanny.py`
  (`python3 tools/gen_tanny.py preview CARPETA` dibuja vistas previas).
- **Modelo (versión anterior).** Cuerpo humanoide de brazos delgados con textura propia (falda con vuelo, mangas
  abullonadas, delantal, lazo rojo, calcetas) y **cabello 3D**: bob corto con volumen,
  flequillo en 4 mechones, mechones que enmarcan la cara, pelitos desordenados arriba y la
  cofia de maid en 3D. El cabello se mece al caminar y respirar. No es un modelo Bedrock
  hecho en Blockbench. La forma del cabello está en `client/TannyHairModel.java` (cada pieza
  tiene posición y tamaño en píxeles) y su textura en `textures/entity/tanny/hair.png`.
  Las texturas se regeneran con `python3 tools/generate_textures.py`.
- **Integración con Cobblemon** por reflexión: el mod compila sin Cobblemon. Si alguna versión de
  Cobblemon cambia sus clases internas, Lucario no saldrá (se registra un error en el log) y
  Tanny seguirá peleando sola.
- Lucario aparece como Pokémon salvaje controlado por Tanny. Un jugador podría iniciarle una
  batalla Pokémon por turnos; si pierde, se debilita y vuelve a salir más tarde.
- `/tanny tp` solo funciona si la zona donde está Tanny está cargada. Si no, el comando te dice
  sus últimas coordenadas conocidas.
- La granja trabaja con cultivos tipo trigo/zanahoria/papa/remolacha (y los de otros mods que
  usen `CropBlock`). No corta calabazas, sandías ni caña de azúcar.
- Este proyecto no se pudo compilar en el entorno donde se escribió (sin acceso a los
  servidores de Forge). Si `gradlew build` muestra algún error, cópialo y se corrige.

---

## 8. Obtener el .jar SIN instalar nada (GitHub lo compila por ti)

1. Crea una cuenta gratis en https://github.com e inicia sesión.
2. Arriba a la derecha: **+ → New repository**. Ponle un nombre (ej. `tannymaid`),
   márcalo **Public** o **Private** y pulsa **Create repository**.
3. En la página del repositorio vacío, pulsa **uploading an existing file**.
4. Abre la carpeta `tannymaid` del ZIP, selecciona **todo su contenido** (incluida la carpeta
   `.github`) y arrástralo a la página. Pulsa **Commit changes**.
   - Si tu sistema oculta la carpeta `.github`: en el repositorio pulsa **Add file → Create new file**,
     escribe como nombre `.github/workflows/build.yml` y pega el contenido de ese archivo.
5. Ve a la pestaña **Actions**. Verás "Compilar TannyMaid (.jar)" ejecutándose (tarda ~5–10 min).
   Si no arranca solo, entra en él y pulsa **Run workflow**.
6. Cuando tenga ✅ verde, entra en la ejecución y, abajo en **Artifacts**, descarga
   **TannyMaid-jar**. Es un ZIP que contiene `tannymaid-forge-1.20.1-1.2.0.jar`.
7. Copia ese `.jar` a tu carpeta `mods`.

Si sale ❌ rojo, abre el paso "Compilar el mod", copia el error y envíalo para corregirlo.
