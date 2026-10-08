# 🧪 Lista de pruebas de Panthrixs Galaxy

Recorre esta lista en orden en tu PC (`gradlew.bat runClient`). Marca cada casilla cuando funcione.
Si algo falla, copia el error de `run/logs/latest.log` (busca `Exception` o `panthrixsgalaxy`).

## ⚡ Comandos de prueba (Fase 24)
| Comando | Qué hace |
|---|---|
| `/pgtp <planeta>` | Te lleva a la superficie: `moon`, `mars`, `asteroids`, `mercury`, `venus`, `pluto`, `xenoria`, `nyx`, `earth` |
| `/pgkit starter` | Banco, traje, bombonas y mochila llenas, batería, bidones, cohete básico y 9 plataformas |
| `/pgkit explorer` | Traje, mochila avanzada, cohete avanzado, **nave** llena, 2 módulos habitables, pistola azul y espada roja |
| `/pgkit endgame` | Traje, mochila experimental, **nave avanzada** llena, 4 módulos, pistola blanca, espada negra, baliza |
| `/pgrefill` | Llena de oxígeno, energía y combustible todo lo que llevas |
| `/pgvacuum true/false` | Simula que no hay aire (para probar el oxígeno en la Tierra) |

## 0. Arranque
- [ ] El juego arranca sin errores y aparece "Panthrixs Galaxy" en la lista de mods.
- [ ] Hay 3 pestañas en el creativo (principal, bloques, equipo).
- [ ] Se crean `config/panthrixsgalaxy-common.toml` y `-client.toml`.
- [ ] Crear un mundo nuevo **no se cuelga** (genera las 9 dimensiones al usarlas, no al crear).
- [ ] Al entrar por primera vez recibes el **libro guía** (16 páginas, en tu idioma) y un mensaje en el chat.
- [ ] Sal y vuelve a entrar, o muere: **no** te da otro libro. `/pgguide` sí te da otro.

## 1. Tierra (Fases 1-10)
- [ ] Minerales en el creativo; herramientas de cada nivel pican lo suyo.
- [ ] Banco de Ingeniería: la guía muestra las 11 categorías (3 filas) y las recetas en gris.
- [ ] Traje completo, bombona, mochila (tecla **B**), recargador de oxígeno.
- [ ] `/pgvacuum true`: sin casco te ahogas; con casco y bombona baja el oxígeno; sin traje completo, daño por temperatura.
- [ ] Generador → cable → celda: la energía viaja. Clic con batería en el generador: se carga.
- [ ] Refinería + carbón + energía → bidón lleno.
- [ ] Cohete básico en plataforma 3×3, combustible, **ESPACIO**: cuenta atrás, despegue, nubes, cielo negro.

## 2. Espacio y Luna (Fases 11-12)
- [ ] Llegas al Espacio: estrellas, Sol, Tierra debajo, Luna y Marte en el cielo.
- [ ] ESPACIO cambia el destino (solo Tierra/Luna/Marte con el cohete). Navega a la Luna: descenso y aterrizaje.
- [ ] Luna: gravedad baja, cráteres, Tierra en el cielo, minerales lunares. Logro "Un pequeño paso".
- [ ] Despegue desde la Luna sin plataforma y vuelta a la Tierra.

## 3. Marte (Fase 13)
- [ ] Cohete avanzado → Marte. Cielo naranja, montañas, hielo con agua.
- [ ] Tormenta de polvo (`/time set 0`): niebla, polvo, viento. Paneles solares producen menos.

## 4. Naves (Fase 14)
- [ ] `/pgkit explorer` → coloca la nave en el suelo, súbete, ESPACIO: motores. W/S, flotar, aterrizar.
- [ ] Sube por encima de Y 450 → Espacio. Panel: Tierra y 5 planetas más cercanos.
- [ ] Mayús + clic en la nave: bodega + barras + "Recoger nave" (conserva todo).

## 5. Armas (Fases 15, 16, 16B)
- [ ] Las 7 pistolas disparan su color; la verde casi no gasta; la púrpura atraviesa armaduras.
- [ ] Las 7 espadas se encienden (Mayús + clic), cambian de modelo, gastan energía, efecto especial.
- [ ] Guardia con la espada (clic mantenido) devuelve los láseres de un alien soldado.

## 6. Criaturas y jefe (Fases 17-18)
- [ ] Luna: crawlers y escorpiones lunares (no aparecen junto a antorchas).
- [ ] Marte: gusano que emerge, escorpiones, crawlers, exploradores (pacíficos), soldados (disparan).
- [ ] Baliza alienígena en Marte → la Reina: barra de jefe, 3 fases, escudo, crías, salto, lluvia de ácido. Suelta necronita.

## 7. Bases (Fase 19)
- [ ] Módulo habitable en la Luna: entras, cierras la puerta → aire. Abres → fuga. Logro "Base lunar".
- [ ] Recargador de oxígeno normal funciona dentro de la base.
- [ ] Depósito de 54 huecos; tanque de oxígeno con bombonas.

## 8. Asteroides y planetas (Fases 20-21)
- [ ] `/pgtp asteroids`: asteroides con minerales, restos de naves con cofre, puestos alienígenas con guardias.
- [ ] `/pgtp mercury`, `venus` (niebla y lava), `pluto` (noche, hielo), `xenoria` (aliens, colmena de la Reina).
- [ ] Con la nave avanzada: rebote en los gigantes gaseosos ("no hay suelo").

## 9. Logros y secretos (Fase 22)
- [ ] Tecla **L**: pestaña Panthrixs Galaxy con el árbol.
- [ ] Astronauta (5 min en la Luna), Espectro de colores, Cazador marciano (10), Más allá de la Tierra (3 planetas).
- [ ] Monolito (`/setblock ~2 ~ ~ panthrixsgalaxy:pg_monolith`) → "EL LADO OSCURO" y coordenadas.
- [ ] `/pgtp nyx` → "Más allá de las estrellas". Nyx no sale en el cielo ni en el panel de la nave.

## 10. Configuración y rendimiento (Fase 23)
- [ ] `oxygenPerSecond = 0` → el oxígeno no baja.
- [ ] Red grande de cables: los TPS (F3 → arriba a la izquierda, o `/forge tps`) siguen en 20.

## 11. Multijugador (opcional pero importante)
- [ ] `gradlew.bat runServer` (acepta el EULA en `run/eula.txt`): el **servidor arranca** sin errores.
      (Si falla con "Attempted to load class ... for invalid dist DEDICATED_SERVER", algo de pantalla se usa en el servidor: avísame.)
- [ ] Dos jugadores: se ven las mochilas, los cohetes y naves de otros; la Reina tiene barra para los dos.
