# Linterna (Forge 1.20.1)

La primera noche de un mundo nuevo llega una **luz verde** que te eleva al cielo y te pregunta **¿Tienes miedo?**
- **Sí**: vuelves a donde estabas y el mod termina para ti.
- **No**: recibes el **Anillo Verde** y el **Lámpara de Poder**, y caes como un cometa en otro lugar con una
  explosión que destroza el suelo.

## El anillo (tenlo en el inventario)
| Tecla | Poder |
|---|---|
| L | Volar (silueta verde) |
| Y | Armadura de energía semitransparente: fuerza, velocidad, regeneración y resistencia |
| Alt+P | Pico holograma verde: pica muy rápido |
| C | Intangible: atraviesas paredes, nada te daña y los monstruos te pierden |
| V | Armas de energía que disparan solas a los monstruos |
| Alt+M | Martillo gigante que destroza todo delante |
| Alt+T | Traje verde y negro con botas y guantes blancos (se desbloquea tras 10 min de uso) |
| M | Catálogo: pared, barrera, domo de defensa, plataforma, jaula, bombas, espada, esmeraldas falsas |

- Con el anillo **en la mano**, clic derecho levanta animales, mobs, jugadores, objetos o bloques; otro clic los lanza.
- El anillo tiene **energía** (barra abajo a la izquierda). Recárgalo con **clic izquierdo** usando el
  **Lámpara de Poder** (en la mano o colocado).
- Comandos: `/linterna estado`; para operadores `/linterna llamar`, `reiniciar`, `recargar`, `anillo`.

Texturas y modelos: `python3 tools/gen_linterna.py`.

## Novedades 1.1.0
- **Catálogo (M)** rediseñado como holograma verde, con pestañas *Constructos* y *Armas*.
- **Armas vanilla de energía** en el catálogo: espada, hacha, arco, ballesta, tridente y escudo, todas encantadas e irrompibles. Duran 3 minutos.
- **Armas nuevas:** Cañón de energía (dispara 3 rayos) y Guadaña de energía (golpe en área).
- **Rayo de selección:** al hacer clic derecho con el anillo sale un rayo verde hasta el objetivo, ya sea un bloque, un material, un animal o un enemigo. El rayo se mantiene mientras lo sostienes y el objetivo tiene una silueta verde.
- **Silueta verde** alrededor del portador cuando vuela, usa la armadura, está intangible o tiene las armas activas.
- **Armadura (Y)** con forma real de armadura que se ajusta al cuerpo y sigue sus movimientos.
- **Traje (Alt+T)** ajustado a la piel: oculta la capa exterior de la skin para que encaje bien.

## Novedades 1.2.0
- **Armas holograma verde** (como el pico de energía): espada, **hacha**, arco (sin flechas), **escudo**, lanza (lanza jabalinas de luz que atraviesan), mazo (onda expansiva al caer golpeando), cañón y guadaña. Se crean en el catálogo (M) → pestaña *Armas*.
- **El anillo siempre protege**: sin daño por caídas, fuego, lava, ahogo, asfixia, frío, rayos, cactus, magia ni wither. Reduce todos los golpes (más con la armadura) y, si tiene al menos 10 % de energía, **te salva de un golpe mortal**.
- **No pierdes el anillo ni la lámpara al morir**: vuelven contigo al reaparecer (el anillo directo a su ranura). Las armas de energía se desvanecen.
- **Ranura del anillo**: botón *Ranura del anillo* en el catálogo (M) o el botón **◎** del inventario. Con el anillo puesto se ve en tu dedo y funciona con la mano vacía:
  - clic izquierdo al aire → disparos de energía;
  - clic derecho a lo lejos → rayo de selección (Shift + clic derecho de cerca).
- **Proyectiles del anillo**: clic al aire con el anillo dispara pequeñas balas de energía.
- **Abducción exagerada**: un rayo de luz verde cae del cielo directo sobre ti, relámpagos, un ciclón verde te envuelve y te sube al cielo siendo intangible (atraviesas techos y montañas), la pantalla se tiñe de verde y la pregunta llega entre truenos.
- **Aterrizaje de héroe**: caes como cometa, frenas cerca del suelo, cráter limpio y tres ondas expansivas verdes; aterrizas en tierra firme (no en el océano) y el anillo ya está puesto en tu dedo.
- **Anillo nuevo**: modelo 3D con gema brillante, barra de energía en el objeto y nunca desaparece si se tira.
- **Lámpara de Poder**: el candelabro ahora es una lámpara de aceite antigua verde, con cristal, llama verde y luz máxima. Recarga con clic izquierdo o derecho.
