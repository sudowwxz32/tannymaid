# Linterna (Forge 1.20.1)

La primera noche de un mundo nuevo llega una **luz verde** que te eleva al cielo y te pregunta **¿Tienes miedo?**
- **Sí**: vuelves a donde estabas y el mod termina para ti.
- **No**: recibes el **Anillo Verde** y el **Candelabro de Poder**, y caes como un cometa en otro lugar con una
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
  **Candelabro de Poder** (en la mano o colocado).
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
